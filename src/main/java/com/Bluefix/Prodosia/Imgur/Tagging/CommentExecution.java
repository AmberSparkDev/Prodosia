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

import com.Bluefix.Prodosia.DataType.Comments.FeedbackRequest;
import com.Bluefix.Prodosia.DataType.Comments.ICommentRequest;
import com.Bluefix.Prodosia.DataType.Comments.SimpleCommentRequest;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Logger.Logger;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class continually scans for Comment requests
 *
 * API costs:
 * 6 POST calls per minute for posting comments.
 * 1 GET call per unique TagRequest added.
 *
 * Although the ImgurIntervalRunner could have been used for this class, it is
 * not expected to cause a serious strain on the Imgur API due to the low amount of
 * GET requests and the fact that only 60*6 = 360 (out of 1250) POST requests
 * can be physically executed by this class.
 */
public class CommentExecution extends Thread
{

    /**
     * The amount of tag requests that should be executed.
     * Recommended to have higher than 1, so that exceptionally large
     * taglists don't clog the tag requests.
     */
    private static final int SimultaneousTagRequest = 4;

    /**
     * The default delay in milliseconds how long it takes for the
     * comment posts to reset.
     */
    private static final int DefaultCommentDelay = 60000;


    /**
     * The amount of comments that imgur allows per minute.
     */
    private static final int CommentsPerMinute = 6;

    //region Singleton and Constructor

    private static CommentExecution me;

    public static CommentExecution handler()
    {
        if (me == null)
            me = new CommentExecution();

        return me;
    }

    /**
     * Create a new Tag Execution object.
     */
    private CommentExecution()
    {
        this.actions = new HashMap<>();
        this.parentMap = new HashMap<>();

        this.feedbackRequests = new LinkedList<>();
    }


    //endregion

    //region Feedback Request

    private LinkedList<FeedbackRequest> feedbackRequests;

    public static void executeFeedbackRequest(FeedbackRequest fr)
    {
        // add the feedback request to the queue.
        handler().feedbackRequests.addLast(fr);
    }

    //endregion




    //region in-memory queue

    /**
     * Queue for action items.
     */
    private HashMap<ICommentRequest, LinkedList<String>> actions;

    private void removeItem(ICommentRequest item)
    {
        actions.remove(item);
        parentMap.remove(item);
    }

    /**
     * Add a new TagRequest to the action queue.
     * @param item The item to be added.
     * @return The amount of comments that the new item will post.
     */
    private int addItem(ICommentRequest item) throws Exception
    {
        // if the item already existed, we will be replacing it.
        removeItem(item);

        // find the comments for the tag-request
        LinkedList<String> comments = item.getComments();

        // add the item to the list.
        actions.put(item, comments);

        return comments.size();
    }

    /**
     * Retrieve whether the current queue is empty.
     * @return true iff the queue is empty, false otherwise.
     */
    private boolean isEmptyQueue()
    {
        return this.actions.isEmpty();
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
            // default delay between comment posting is 1 full minute.
            int delay = DefaultCommentDelay;

            try
            {
                // first execute the feedback requests, since they are a priority.
                int postUsed = feedbackRequests();

                updateQueue();

                // if the queue is empty, don't post the comments and use a shorter
                // delay.
                if (isEmptyQueue())
                {
                    delay = 10000;
                }
                else
                {
                    postComments(postUsed);
                }

                // update the queue again so that requests that were completed can immediately be
                // dismissed.
                updateQueue();

            } catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    Thread.sleep(delay);
                } catch (InterruptedException e)
                {

                }
            }
        }
    }


    /**
     * Execute the feedback requests if they were available. These requests always get priority over
     * any other type of comment posting.
     * @return The amount of comments that were posted by this method.
     * @throws BaringoApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    private int feedbackRequests() throws BaringoApiException, IOException, URISyntaxException
    {
        int postUsed = 0;


        while (postUsed < CommentsPerMinute && !feedbackRequests.isEmpty())
        {
            FeedbackRequest fr = feedbackRequests.removeFirst();


            // even if one individual feedback request fails, the others should still pass.
            try
            {
                LinkedList<String> comments = fr.getComments();

                if (comments.size() != 1)
                    throw new IllegalArgumentException("FeedbackRequest is only supposed to have one comment.");

                long commentId = postComment(fr, comments.get(0));
                postUsed++;

                fr.setCommentId(commentId);
                fr.start();
            }
            catch (Exception e)
            {
                // ignore individual exceptions
                e.printStackTrace();
            }
        }

        return postUsed;
    }



    private void updateQueue() throws Exception
    {
        // clean any items that were completely posted.
        cleanupEmptyItems();

        // update any simple comment requests that were available to us.
        updateSimpleCommentRequests();

        // if there was still room left in the queue, update it with tag requests.
        updateTagRequests();
    }

    /**
     * Remove all entries from the queue that were empty (i.e. completely handled)
     * @throws Exception
     */
    private void cleanupEmptyItems() throws Exception
    {
        // remove all items from the queue that were empty.
        ArrayList<ICommentRequest> deletionList = new ArrayList<>();

        for (Map.Entry<ICommentRequest, LinkedList<String>> entry : actions.entrySet())
        {
            if (entry.getValue() == null || entry.getValue().isEmpty())
                deletionList.add(entry.getKey());
        }

        // remove the item from the global tagrequest queue as well.
        for (ICommentRequest tr : deletionList)
        {
            actions.remove(tr);

            tr.remove();

            // if it was a tag request, indicate success to the user.
            if (tr instanceof TagRequest && tr.getImgurId() != null)
            {
                Logger.logMessage("Post \"" + tr.getImgurId() + "\" successfully tagged.");
            }
        }
    }

    private void updateSimpleCommentRequests() throws Exception
    {
        // we ignore the size of the queue since simple tag requests always take priority.
        ArrayList<SimpleCommentRequest> scr = new ArrayList<>(SimpleCommentRequestStorage.handler().getAll());

        // filter out all items that were already in the queue.
        for (ICommentRequest cr : actions.keySet())
        {
            scr.remove(cr);
        }

        // add all remaining items to the queue.
        for (SimpleCommentRequest mScr : scr)
        {
            addItem(mScr);
        }
    }

    /**
     * Update the tag request queue we maintain.
     *
     * If a tag request was updated, refresh its comment list.
     * @throws Exception
     */
    private void updateTagRequests() throws Exception
    {
        // if the queue is still the full length, skip this phase.
        if (actions.size() >= SimultaneousTagRequest)
            return;

        // retrieve all current tag requests. Create a local copy.
        ArrayList<TagRequest> queueItems = new ArrayList<>(TagRequestStorage.handler().getAll());

        // if an old tagRequest has changed, replace it.
        for (ICommentRequest tr : actions.keySet())
        {
            // skip the entry if it wasn't a tag request
            if (!(tr instanceof TagRequest))
                continue;

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
     * @param postUsed the amount of comment post requests that were already used.
     */
    private void postComments(int postUsed) throws BaringoApiException, IOException, URISyntaxException
    {
        int posted = postUsed;

        Set<Map.Entry<ICommentRequest, LinkedList<String>>> entries = actions.entrySet();

        while (posted < CommentsPerMinute)
        {
            boolean allEmpty = true;

            for (Map.Entry<ICommentRequest, LinkedList<String>> e : entries)
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

    private HashMap<ICommentRequest, Comment> parentMap;


    /**
     * Find or create the parent comment for the specified TagRequest
     * @return true iff a comment was posted for this.
     */
    private boolean findParent(ICommentRequest cr) throws BaringoApiException, IOException, URISyntaxException
    {
        // return if the parent was already known.
        if (parentMap.containsKey(cr))
            return false;

        // attempt to retrieve the parent comment.
        Comment parentComment = cr.getParent();

        if (parentComment == null)
        {
            // if it pertains a tag request, create a new parent comment.
            if (cr instanceof TagRequest)
            {
                TagRequest tr = (TagRequest) cr;

                parentComment = postParentComment(cr.getImgurId(), tr.getTaglists().iterator());
                parentMap.put(tr, parentComment);
                return true;
            }
        } else
        {
            // if the parent comment id was already known, retrieve its actual comment.
            parentMap.put(cr, parentComment);
        }

        return false;
    }

    /**
     * Post a comment according to the tag request data.
     * @param tr The tag request data.
     * @param comment The comment to be posted.
     * @return The command id of the comment that was posted.
     * @throws BaringoApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    private long postComment(ICommentRequest tr, String comment) throws BaringoApiException, IOException, URISyntaxException
    {
        // retrieve the parent comment
        Comment parentComment = tr.getParent();

        if (parentComment == null)
            parentComment = parentMap.get(tr);



        // if no parent comment was known, simply post directly to the post.
        if (parentComment == null)
        {
            // post the comment.
            return ImgurManager.client().commentService().addComment(tr.getImgurId(), comment);
        }
        else
        {
            // post the reply
            return ImgurManager.client().commentService().addReply(parentComment, comment);
        }
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
