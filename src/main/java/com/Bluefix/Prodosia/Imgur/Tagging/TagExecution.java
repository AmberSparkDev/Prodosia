/*
 * Copyright (c) 2018 J.S. Boellaard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.Bluefix.Prodosia.Imgur.Tagging;

import com.Bluefix.Prodosia.DataType.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Logger.Logger;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class continually scans for tag requests
 *
 * API costs:
 * 6 POST calls per minute for posting comments.
 * 1 GET call per unique TagRequest added.
 */
public class TagExecution extends Thread
{

    /**
     * The amount of tag requests that should be executed.
     * Recommended to have higher than 1, so that exceptionally large
     * taglists don't clog the tag requests.
     */
    private static final int SimultaneousTagRequest = 4;


    /**
     * The amount of comments that imgur allows per minute.
     */
    private static final int CommentsPerMinute = 6;

    //region Singleton and Constructor

    private static TagExecution me;

    public static TagExecution tagExecution()
    {
        if (me == null)
            me = new TagExecution();

        return me;
    }

    /**
     * Create a new Tag Execution object.
     */
    private TagExecution()
    {
        this.actions = new HashMap<>();
        this.parentMap = new HashMap<>();
    }


    //endregion




    //region in-memory queue

    /**
     * Queue for action items.
     */
    private HashMap<TagRequest, LinkedList<String>> actions;

    private void removeItem(TagRequest item)
    {
        actions.remove(item);
        parentMap.remove(item);
    }

    /**
     * Add a new TagRequest to the action queue.
     * @param item The item to be added.
     * @return The amount of comments that the new item will post.
     */
    private int addItem(TagRequest item)
    {
        // if the item already existed, we will be replacing it.
        removeItem(item);

        // find the comments for the tag-request
        LinkedList<String> comments = TagRequestComments.parseCommentsForTagRequest(item);

        // add the item to the list.
        actions.put(item, comments);

        return comments.size();
    }

    //endregion


    //region Thread logic

    /**
     * Continually execute the tag request logic. This thread remains running perpetually until
     * application termination.
     */
    @Override
    public void run()
    {
        // Thread runs continually until application shutdown.
        while (true)
        {
            try
            {
                updateQueue();
                postComments();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    Thread.sleep(60000);
                } catch (InterruptedException e)
                {

                }
            }
        }
    }

    /**
     * Update the current queue we maintain.
     *
     * If a tag request was updated, refresh its comment list.
     * @throws Exception
     */
    private void updateQueue() throws Exception
    {
        // remove all items from the queue that were empty.
        ArrayList<TagRequest> deletionList = new ArrayList<>();

        for (Map.Entry<TagRequest, LinkedList<String>> entry : actions.entrySet())
        {
            if (entry.getValue() == null || entry.getValue().isEmpty())
                deletionList.add(entry.getKey());
        }

        // remove the item from the global tagrequest queue as well.
        for (TagRequest tr : deletionList)
        {
            actions.remove(tr);
            TagRequestHandler.handler().remove(tr);
            Logger.logMessage("Post \"" + tr.getImgurId() + "\" successfully tagged.");
        }


        // if the queue is still the full length, skip this phase.
        if (actions.size() >= SimultaneousTagRequest)
            return;

        // retrieve all current tag requests. Create a local copy.
        ArrayList<TagRequest> queueItems = new ArrayList<>(TagRequestHandler.handler().getAll());

        // if an old tagRequest has changed, replace it.
        for (TagRequest tr : actions.keySet())
        {
            // find the corresponding item in queueItems.
            int index = queueItems.indexOf(tr);

            // if the item wasn't in the queue anymore, remove it from the list
            if (index < 0)
            {
                removeItem(tr);
                continue;
            }

            TagRequest myTr = queueItems.get(index);

            // replace the item if it isn't deepequal. This indicates that the entry was
            // changed (most likely merged).
            if (!tr.deepEquals(myTr))
            {
                removeItem(tr);
                addItem(myTr);
            }

            // if the entry was equal, we are good.


            // finally, remove the tagrequest from the queue-items since it was already handled.
            queueItems.remove(index);
        }

        // if there is still room in the queue, add new items
        for (int i = 0; i < SimultaneousTagRequest - actions.size() && i < queueItems.size(); i++)
        {
            TagRequest newItem = queueItems.get(i);
            int comments = addItem(newItem);
            Logger.logMessage("Start tag on \"" + newItem.getImgurId() + "\"\n" +
                    "Estimated time: " + estimatedTime(comments) + "m", Logger.Severity.INFORMATIONAL);
        }
    }


    private int estimatedTime(int comments)
    {
        // make an estimate on the amount of minutes based on how busy the list is right now
        return (int)Math.ceil((comments / (double)CommentsPerMinute) / actions.size());
    }







    /**
     * Post the comments of the respective tag requests to their posts.
     */
    private void postComments() throws BaringoApiException, IOException, URISyntaxException
    {
        int posted = 0;

        Set<Map.Entry<TagRequest, LinkedList<String>>> entries = actions.entrySet();

        while (posted < CommentsPerMinute)
        {
            boolean allEmpty = true;

            for (Map.Entry<TagRequest, LinkedList<String>> e : entries)
            {
                // if the comment list is empty, ignore
                if (e.getValue().isEmpty())
                {
                    continue;
                }



                // post one of the strings of the currently collection and remove it
                String comment = e.getValue().remove(0);
                allEmpty = false;

                // add to the comment limit if we had to create a parent comment.
                if (findParent(e.getKey()))
                    posted++;

                if (posted >= CommentsPerMinute)
                    return;

                // increment the comment limit as we post a reply.
                postComment(e.getKey(), comment);
                posted++;

                // if we exceed the limit, return.
                if (posted >= CommentsPerMinute)
                    return;

            }

            // if all entries were empty, simply return to continue
            if (allEmpty)
                return;
        }
    }

    private HashMap<TagRequest, Comment> parentMap;


    /**
     * Find or create the parent comment for the specified TagRequest
     * @return true iff a comment was posted for this.
     */
    private boolean findParent(TagRequest tr) throws BaringoApiException, IOException, URISyntaxException
    {
        if (!parentMap.containsKey(tr))
        {
            // if there was no parent comment, create one.
            Comment parentComment;
            long pC = tr.getParentComment();

            if (pC < 0)
            {
                parentComment = postParentComment(tr.getImgurId(), tr.getTaglists().iterator());
                parentMap.put(tr, parentComment);
                return true;
            }
            else
            {
                // if the parent comment id was already known, retrieve its actual comment.
                parentComment = ImgurManager.client().commentService().getComment(tr.getParentComment());
                parentMap.put(tr, parentComment);
            }
        }

        return false;
    }

    /**
     * Post a comment according to the tag request data.
     * @param tr The tag request data.
     * @param comment The comment to be posted.
     * @throws BaringoApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    private void postComment(TagRequest tr, String comment) throws BaringoApiException, IOException, URISyntaxException
    {
        // retrieve the parent comment
        Comment parentComment = parentMap.get(tr);

        // post the reply
        ImgurManager.client().commentService().addReply(parentComment, comment);
    }


    /**
     * Create a new parent comment on the specified imgur post.
     * @param imgurId
     * @param abbreviations
     * @return
     * @throws BaringoApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    private static Comment postParentComment(String imgurId, Iterator<Taglist> abbreviations) throws BaringoApiException, IOException, URISyntaxException
    {
        StringBuilder message = new StringBuilder("Tag issued for lists (");

        while (abbreviations.hasNext())
            message.append(abbreviations.next().getAbbreviation() + ", ");


        // trim the last comma
        message.setLength(message.length()-2);
        message.append(")");

        // trim the length of the comment if it exceeds the max length
        if (message.length() > TagRequestComments.MaxCommentLength)
            message.setLength(TagRequestComments.MaxCommentLength);

        long comId = ImgurManager.client().commentService().addComment(imgurId, message.toString());
        return ImgurManager.client().commentService().getComment(comId);
    }

    //endregion


}
