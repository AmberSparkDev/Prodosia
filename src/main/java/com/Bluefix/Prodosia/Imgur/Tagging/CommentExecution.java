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

import com.Bluefix.Prodosia.DataHandler.SimpleCommentRequestStorage;
import com.Bluefix.Prodosia.DataHandler.TagRequestStorage;
import com.Bluefix.Prodosia.DataType.Comments.*;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
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
 * 1 GET call every time the comments for a TagRequest are checked (should hopefully only occur once).
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
     * taglists don't clog the tag requests. Preferable to have it
     * over the amount of comments per minute to minimize the amount of wasted
     * comments.
     */
    private static final int SimultaneousTagRequest = 10;

    /**
     * The default delay in milliseconds how long it takes for the
     * comment posts to reset.
     */
    private static final int DefaultCommentDelay = 60000;

    /**
     * The shorter delay in case no comments were posted.
     */
    private static final int DefaultShortDelay = 10000;


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

        this.feedbackRequests = new LinkedList<>();
    }


    //endregion

    //region Feedback Request

    /**
     * A list of entries of comments that request feedback. These have the highest priority in the system since
     * another part of the application is waiting for the feedback.
     */
    private LinkedList<FeedbackRequest> feedbackRequests;

    public LinkedList<FeedbackRequest> getFeedbackRequests()
    {
        return feedbackRequests;
    }

    /**
     * Execute a FeedbackRequest comment. After the comment has been posted, it will call the callback
     * method.
     * @param fr The feedbackrequest to be executed.
     */
    public static void executeFeedbackRequest(FeedbackRequest fr)
    {
        if (fr == null)
            return;

        // add the feedback request to the queue.
        handler().feedbackRequests.addLast(fr);
    }

    //endregion




    //region in-memory queue

    /**
     * Queue for action items.
     * This maps Command Requests to the actual comments it is requesting.
     */
    private HashMap<ICommentRequest, LinkedList<String>> actions;

    /**
     * Remove an item from the queue.
     * @param item
     */
    private void removeItem(ICommentRequest item)
    {
        actions.remove(item);
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
        LinkedList<String> comments = new LinkedList<>(item.getComments());

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
     * This counter keeps track of how many comments can still be posted during this cycle.
     */
    private int commentCounter;

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

            // reset the comment counter.
            this.commentCounter = CommentsPerMinute;

            try
            {
                // first execute the feedback requests, since they are a priority.
                feedbackRequests();

                updateQueue();

                // if the queue is empty and no feedback requests were handled,
                // don't post the comments and use a shorter delay.
                if (isEmptyQueue() && commentCounter == CommentsPerMinute)
                {
                    delay = DefaultShortDelay;
                }
                else
                {
                    postComments();
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
                    // if this fails, it is most likely an application shutdown upon which we are done anyways.
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
    private void feedbackRequests() throws BaringoApiException, IOException, URISyntaxException
    {
        while (commentCounter > 0 && !feedbackRequests.isEmpty())
        {
            FeedbackRequest fr = feedbackRequests.removeFirst();


            // even if one individual feedback request fails, the others should still pass.
            try
            {
                LinkedList<String> comments = fr.getComments();

                if (comments.size() != 1)
                    throw new IllegalArgumentException("FeedbackRequest is only supposed to have one comment.");

                long commentId = postComment(fr, comments.get(0));
                commentCounter--;

                fr.setCommentId(commentId);
                fr.start();
            }
            catch (Exception e)
            {
                // ignore individual exceptions
                e.printStackTrace();
            }
        }
    }


    /**
     * Update all the queues that hold Comment Requests. 
     * @throws Exception
     */
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
        // complete all items from the queue that were empty.
        ArrayList<ICommentRequest> deletionList = new ArrayList<>();

        for (Map.Entry<ICommentRequest, LinkedList<String>> entry : actions.entrySet())
        {
            if (entry.getValue() == null || entry.getValue().isEmpty())
                deletionList.add(entry.getKey());
        }

        // complete the item
        for (ICommentRequest icr : deletionList)
        {
            // force a cast to object to ensure that no custom equals is called in the deletion.
            Object icrObj = icr;
            this.actions.remove(icrObj);

            // handle deletion by the ICommentRequest object.
            icr.complete();
        }
    }


    /**
     * Retrieves items from the `SimpleCommentRequestStorage` and adds them to our queue.
     * @throws Exception
     */
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
     * Update the tag request queue we maintain. Retrieves items from `TagRequestStorage`
     * and adds them to the action list if the maximum amount of simultaneous
     * active requests hasn't been exceeded yet.
     *
     * If a tag request was updated, refresh its comment list.
     * @throws Exception
     */
    private void updateTagRequests() throws Exception
    {
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

            // if the item wasn't in the queue anymore, complete it from the list
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


            // finally, complete the tagrequest from the queue-items since it was already handled.
            queueItems.remove(index);
        }

        // if the queue is still the full length, skip this phase.
        if (actions.size() >= SimultaneousTagRequest)
            return;


        // if there is still room in the queue, add new items
        for (int i = 0; i < SimultaneousTagRequest - actions.size() && i < queueItems.size(); i++)
        {
            TagRequest newItem = queueItems.get(i);
            int comments = addItem(newItem);

        }
    }




    /**
     * Post the comments of the respective tag requests to their posts.
     *
     * This method will loop through the actions in the queue in no particular order
     * and will post comments until either its comment counter runs out or all entries
     * in the queue were empty. The method uses a breadth-first approach to attempt to
     * spread out the comments between the queue items.
     */
    private void postComments() throws BaringoApiException, IOException, URISyntaxException
    {
        Set<Map.Entry<ICommentRequest, LinkedList<String>>> entries = actions.entrySet();

        while (commentCounter > 0)
        {
            boolean allEmpty = true;

            for (Map.Entry<ICommentRequest, LinkedList<String>> e : entries)
            {
                // if the comment list is empty, ignore
                if (e.getValue().isEmpty())
                {
                    continue;
                }

                // post one of the strings of the current collection and complete it
                String comment = e.getValue().remove(0);
                allEmpty = false;

                if (commentCounter <= 0)
                    return;

                // post a reply.
                try
                {
                    postComment(e.getKey(), comment);
                }
                catch (Exception ex)
                {
                    // if posting a comment fails for whatever reason, it shouldn't affect the other queries.
                    ex.printStackTrace();
                }

                // since the Imgur API can fail but still succeed in posting a comment, an attempt should always be counted.
                commentCounter--;

                // if we exceed the limit, return.
                if (commentCounter <= 0)
                    return;

            }

            // if all entries were empty, simply return to continue
            if (allEmpty)
                return;
        }
    }

    /**
     * Post a comment according to the Comment Request data.
     * @param icr The Comment Request meta-data.
     * @param comment The comment to be posted.
     * @return The command id of the comment that was posted.
     * @throws BaringoApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    private long postComment(ICommentRequest icr, String comment) throws BaringoApiException, IOException, URISyntaxException
    {
        // retrieve the parent comment
        Comment parentComment = icr.getParent();

        // if no parent comment was known, simply post directly to the post.
        if (parentComment == null)
        {
            // post the comment.
            return ImgurManager.client().commentService().addComment(icr.getImgurId(), comment);
        }
        else
        {
            // post the reply
            return ImgurManager.client().commentService().addReply(parentComment, comment);
        }
    }

    //endregion


}
