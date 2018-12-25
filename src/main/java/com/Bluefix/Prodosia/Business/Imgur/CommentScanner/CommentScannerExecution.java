/*
 * Copyright (c) 2018 RoseLaLuna
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

package com.Bluefix.Prodosia.Business.Imgur.CommentScanner;

import com.Bluefix.Prodosia.Business.Command.CommandRecognition;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.IImgurManager;
import com.Bluefix.Prodosia.Data.DataHandler.CommentScannerStorage;
import com.Bluefix.Prodosia.Data.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.Data.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.Data.DataType.Command.ImgurCommandInformation;
import com.Bluefix.Prodosia.Data.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Data.DataType.Tracker.TrackerBookmark;
import com.Bluefix.Prodosia.Business.Exception.BaringoExceptionHelper;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ApiDistribution;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Business.Module.ImgurIntervalRunner;
import com.Bluefix.Prodosia.Business.Prefix.CommandPrefix;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * The Comment Scanner module takes the Trackers found in `TrackerHandler` and scans through the
 * comments until it finds the last known comment.
 */
public class CommentScannerExecution extends ImgurIntervalRunner
{
    private IImgurManager _imgurManager;

    /**
     * Indicate the maximum amount of requests that can be handled within a cycle.
     * This is a hard cap.
     */
    public static int MaximumGetRequestsPerCycle = 20;

    /**
     * The minimum amount of time in milliseconds that this module should wait in between runs.
     */
    public static int MinimumWaitBetweenCyclesMillis = 20000;


    public CommentScannerExecution(
            IImgurManager imgurManager,
            ILogger logger,
            ILogger appLogger
    )
    {
        super(ApiDistribution.CommentModule, logger, appLogger);

        if (imgurManager == null)
            throw new IllegalArgumentException("This module cannot function without the Imgur Manager.");

        // store the dependencies.
        _imgurManager = imgurManager;

        trackerMap = new HashMap<>();
        requestCounter = 0;
    }

    /**
     * The request counter that keeps track of how many requests have been executed.
     */
    private int requestCounter;

    /**
     * Execute a single cycle.
     */
    @Override
    public void run()
    {
        // skip the run if the imgur client isn't available.
        if (_imgurManager.getClient() == null)
            return;

        try
        {
            // refresh the queue items.
            refreshQueue();

            // loop through the queue items.
            loopQueue();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // force a short wait (20s)
                Thread.sleep(MinimumWaitBetweenCyclesMillis);
            } catch (InterruptedException e)
            {

            }
        }
    }

    /**
     * Indicate the maximum amount of GET requests expected to be used during the next cycle.
     *
     * @return The maximum amount of GET requests.
     */
    @Override
    public int projectedRequests()
    {
        int tmpReq = requestCounter;
        requestCounter = 0;
        return tmpReq;
    }

    /**
     * Map the imgur-id of a tracker against its queue data.
     */
    private HashMap<Long, QueueItem> trackerMap;


    /**
     * Add a tracker to the queue.
     * Attempt to retrieve its bookmark from storage. If the bookmark couldn't be retrieved, create a
     * new bookmark, costing 1 GET request.
     * @param tracker The tracker to be added.
     * @return true iff 1 GET request was used to add this item. false otherwise.
     */
    private boolean addItem(Tracker tracker) throws Exception
    {
        boolean usedGet = false;

        // if there was an old tracker that is the same, complete it first.
        trackerMap.remove(tracker.getImgurId());

        TrackerBookmark tb = CommentScannerStorage.getBookmarkByImgurId(tracker.getImgurId());

        if (tb == null)
        {
            // since the tracker-bookmark wasn't known, parse it for the user.
            tb = getBookmarkForUser(tracker);
            usedGet = true;

            // store the bookmark
            CommentScannerStorage.handler().set(tb);
        }

        // add a default queue item
        trackerMap.put(tracker.getImgurId(), new QueueItem(tb));

        return usedGet;
    }


    /**
     * Remove an item from the queue.
     * @param imgurId
     */
    private void removeItem(Long imgurId) throws Exception
    {
        trackerMap.remove(imgurId);

        // also complete it from the bookmark storage.
        TrackerBookmark tb = CommentScannerStorage.getBookmarkByImgurId(imgurId);
        CommentScannerStorage.handler().remove(tb);
    }


    private void updateQueueItem(long imgurId, QueueItem item)
    {
        trackerMap.remove(imgurId);
        trackerMap.put(imgurId, item);
    }




    /**
     * Refresh the queue by removing trackers that were deleted and
     * adding trackers that were added.
     *
     * It is *highly* recommended to make the TrackerHandler retain its
     * local storage because of how often it is accessed.
     * @return The amount of GET requests that were executed
     * @throws Exception
     */
    private synchronized void refreshQueue() throws Exception
    {
        // retrieve all current trackers.
        ArrayList<Tracker> trackers = new ArrayList<>(TrackerHandler.handler().getAll());

        // if any trackers in our queue were removed, complete them from the queue as well.
        for (Long imgId : trackerMap.keySet())
        {
            Iterator<Tracker> tIt = trackers.iterator();
            boolean occurenceFound = false;

            while (tIt.hasNext() && !occurenceFound)
            {
                if (tIt.next().getImgurId() == imgId)
                    occurenceFound = true;
            }

            // if no occurence of the imgur id was found in the tracker-list, complete it from the queue.
            if (!occurenceFound)
            {
                removeItem(imgId);
            }
        }

        // if any trackers weren't part of the queue, add them.
        for (Tracker t : trackers)
        {
            // if the tracker has no imgur id, skip it.
            if (    t.getImgurName() == null ||
                    t.getImgurName().trim().isEmpty() ||
                    t.getImgurId() < 0)
                continue;

            Set<Long> iSet = trackerMap.keySet();

            if (!iSet.contains(t.getImgurId()))
            {
                // add the item to the queue.
                if (addItem(t))
                    requestCounter++;

                if (requestCounter >= MaximumGetRequestsPerCycle)
                    return;
            }
        }
    }


    /**
     * Loop through the queue items.
     * @return The total amount of GET requests used.
     */
    private void loopQueue() throws Exception
    {
        // skip this method if the queue is empty
        if (trackerMap.isEmpty())
            return;

        processQueue();

        // while the queue isn't done yet and there are still open requests, keep going.
        while (!queueIsDone() && requestCounter < MaximumGetRequestsPerCycle)
        {
            processQueue();
        }

        // open the queue again.
        if (queueIsDone())
            openQueue();

        // if there are still requests remaining, start processing again.
        if (requestCounter < MaximumGetRequestsPerCycle)
        {
            processQueue();
        }
    }

    /**
     * Process any entries in the queue
     * @return The amount of requests that were used.
     */
    private void processQueue() throws Exception
    {
        // return if no requests were allowed
        if (requestCounter >= MaximumGetRequestsPerCycle)
            return;

        // loop through all the queue items and process them if they were still open.
        for (QueueItem q : trackerMap.values())
        {
            // if this queue-item had already reached its comment, skip it.
            if (q.isReached())
                continue;

            // retrieve the latest page and increment it.
            int curPage = q.getPage();
            q.incrementPage();

            // retrieve the current page and parse its comments.
            Tracker t = q.getBookmark().getTracker();

            // if the tracker was invalidated for some reason, skip this phase.
            if (t == null)
                continue;

            requestCounter++;
            List<Comment> trackerComments = null;

            try
            {
                trackerComments = _imgurManager.getClient().accountService().listComments(
                        t.getImgurName(),
                        Comment.Sort.Newest,
                        curPage);
            }
            catch (BaringoApiException ex)
            {
                // if the request returns a 404, update the tracker data.
                if (BaringoExceptionHelper.isNotFound(ex))
                {
                    try
                    {
                        Account acc = _imgurManager.getClient().accountService().getAccount(t.getImgurId());

                        Tracker newTracker = new Tracker(
                                t.getId(),
                                acc.getUserName(),
                                t.getImgurId(),
                                t.getDiscordName(),
                                t.getDiscordTag(),
                                t.getDiscordId(),
                                t.getPermissions());

                        TrackerHandler.handler().update(t, newTracker);
                        System.out.println("Updated Tracker \"" + t.getImgurName() + "\" -> \"" + newTracker.getImgurName() + "\"");
                        t = newTracker;
                    }
                    catch (Exception e)
                    {
                        // ignore this.
                    }
                }
            }
            catch (Exception ex)
            {
                // a single failure should not impede functionality.
            }

            if (trackerComments == null || trackerComments.isEmpty())
            {
                q.setReached();
                continue;
            }

            // set the new bookmark for the queue-item.
            Comment newestComment = trackerComments.get(0);
            if (newestComment != null)
                q.setNewBookmark(new TrackerBookmark(newestComment.getId(), newestComment.getCreatedAt(), t));

            // if any of the comments corresponded to the timestamp or id, set the queue-item as reached.
            Iterator<Comment> cIt = trackerComments.iterator();

            // Only let the application parse new comments.
            LinkedList<Comment> newComments = new LinkedList<>();

            while (cIt.hasNext() && !q.isReached())
            {
                Comment c = cIt.next();

                if (!q.isBookmark(c))
                    newComments.addFirst(c);
            }

            // Let another class parse these comments.
            for (Comment c : newComments)
            {
                CommandInformation ci = new ImgurCommandInformation(t, c);

                // create a new CommandRecognition object and execute the entry.
                CommandRecognition cr = new CommandRecognition(_logger, _appLogger);
                cr.executeEntry(CommandPrefix.Type.IMGUR, ci, c.getComment());
            }

            // if we have reached the maximum amount of allowed requests, return.
            if (requestCounter >= MaximumGetRequestsPerCycle)
                return;
        }
    }


    /**
     * Indicate whether all current queue items have been retrieved.
     * @return true iff the queue is done, false otherwise.
     */
    private boolean queueIsDone()
    {
        for (QueueItem q : trackerMap.values())
            if (!q.isReached())
                return false;

        return true;
    }


    /**
     * Open up all queue items again.
     */
    private void openQueue()
    {
        for (QueueItem q : trackerMap.values())
        {
            q.open();
        }
    }

    @Override
    public String getName()
    {
        return "CommentScanner";
    }


    //endregion

    //region QueueItem struct

    /**
     * A Queue item for the Comment Scanner. This will keep a bookmark on the latest
     * comment that was scanned for a user.
     */
    private static class QueueItem
    {
        private TrackerBookmark bookmark;

        /**
         * Boolean value to indicate whether the cycle did reach the bookmark. If it did not, we will have to continue from the page it is on.
         */
        private boolean reached;

        private int page;

        private TrackerBookmark newBookmark;

        public QueueItem(TrackerBookmark bookmark)
        {
            this.bookmark = bookmark;
            this.reached = true;
            this.page = 0;
        }

        public TrackerBookmark getBookmark()
        {
            return bookmark;
        }

        public void setNewBookmark(TrackerBookmark tb)
        {
            // if we already had a newest set, ignore
            if (newBookmark == null)
                newBookmark = tb;
        }

        public boolean isReached()
        {
            return reached;
        }

        /**
         * Returns true iff the comment is equal or beyond the bookmark.
         * @return
         */
        public boolean isBookmark(Comment c) throws Exception
        {
            if (c == null)
                return false;

            if (    this.bookmark == null ||
                    this.bookmark.getLastCommentId() == -1 ||
                    this.bookmark.getLastCommentTime().getTime() == Long.MAX_VALUE ||
                    this.bookmark.getLastCommentId() == c.getId() ||
                    this.bookmark.getLastCommentTime().compareTo(c.getCreatedAt()) >= 0)
            {
                setReached();
                return true;
            }

            return false;
        }

        /**
         * Indicate that we have reached the previous bookmark.
         */
        public void setReached() throws Exception
        {
            this.reached = true;

            // replace the bookmark with the new one
            if (newBookmark != null)
                bookmark = newBookmark;

            newBookmark = null;

            //update the bookmark for the user.
            CommentScannerStorage.handler().set(bookmark);

        }

        public int getPage()
        {
            return page;
        }

        public void incrementPage()
        {
            this.page++;
        }

        public void open()
        {
            this.reached = false;
            this.page = 0;
        }
    }


    /**
     * Parse a bookmark for the specified user. Will consume 1 GET request.
     * @param tracker The specified tracker.
     * @return A bookmark with the latest comment
     */
    private TrackerBookmark getBookmarkForUser(Tracker tracker) throws BaringoApiException, IOException, URISyntaxException
    {
        List<Comment> comments = null;

        try
        {
            comments = _imgurManager.getClient().accountService().listComments(tracker.getImgurName(), Comment.Sort.Newest, 0);
        }
        catch (Exception ex)
        {
            return defaultBookmark(tracker);
        }


        // if there were no comments, return the default bookmark.
        if (comments == null || comments.isEmpty())
            return defaultBookmark(tracker);

        Comment lastComment = comments.get(0);
        return new TrackerBookmark(lastComment.getId(), lastComment.getCreatedAt(), tracker);
    }

    /**
     * Default bookmark if the user does not have any comments.
     * @return
     */
    private static TrackerBookmark defaultBookmark(Tracker tracker)
    {
        return new TrackerBookmark(-1, new Date(Long.MAX_VALUE), tracker);
    }


    //endregion


}
