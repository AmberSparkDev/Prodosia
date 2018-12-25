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

package com.Bluefix.Prodosia.Business.Imgur.CommentDeletion;

import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.IImgurManager;
import com.Bluefix.Prodosia.Data.DataHandler.CommentDeletionStorage;
import com.Bluefix.Prodosia.Business.Exception.BaringoExceptionHelper;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ApiDistribution;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Business.Module.ImgurIntervalRunner;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Module class for deleting the items provided in `CommentDeletionStorage`
 */
public class CommentDeletionExecution extends ImgurIntervalRunner
{
    private IImgurManager _imgurManager;

    /**
     * If this option is enabled, the deletion of a comment is ensured, but
     * the total amount of deletions per hours will decrease to half capacity.
     */
    private static final boolean ensureDeletion = false;

    /**
     * Separate the cycles in parts of 6, 10 minutes in between each.
     */
    private static final int DeletionItemsPerCycle = ApiDistribution.DeletionModule / 6;



    /**
     * Create a new Module object for the Comment Deletion functionality.
     */
    public CommentDeletionExecution(
            IImgurManager imgurManager,
            ILogger logger,
            ILogger appLogger

    )
    {
        super(ApiDistribution.DeletionModule, logger, appLogger);

        // store the dependencies
        _imgurManager = imgurManager;

        this.deletedComments = new HashSet<>();
        requestCounter = 0;
    }




    private HashSet<Long> deletedComments;

    private int requestCounter;

    @Override
    public void run()
    {
        // if the imgur manager client wasn't initialized, skip the run.
        if (_imgurManager.getClient() == null)
            return;

        try
        {
            // check on the deletedComments if comment-deletion was ensured.
            if (ensureDeletion)
            {
                // terrible name, but I need a set to store all the items that can be
                // deleted afterwards.
                HashSet<Long> deletionDeletion = new HashSet<>();
                Iterator<Long> deletions = deletedComments.iterator();

                while (deletions.hasNext() && requestCounter < DeletionItemsPerCycle)
                {
                    Long value = deletions.next();
                    try
                    {
                        requestCounter++;
                        Comment com = _imgurManager.getClient().commentService().getComment(value);

                        if (com == null)
                            deletionDeletion.add(value);
                    }
                    catch (BaringoApiException ex)
                    {
                        if (BaringoExceptionHelper.isNotFound(ex) ||
                                BaringoExceptionHelper.isBadRequest(ex))
                            deletionDeletion.add(value);
                    }
                }

                for (Long l : deletionDeletion)
                {
                    deletedComments.remove(l);
                    CommentDeletionStorage.handler().remove(l);
                }
            }
            else
            {
                for (Long l : deletedComments)
                    CommentDeletionStorage.handler().remove(l);
            }

            // retrieve as many items from the Deletion Storage as our cycle permits.
            ArrayList<Long> deletions = CommentDeletionStorage.handler().getAll();

            Iterator<Long> dIt = deletions.iterator();

            while (dIt.hasNext() && requestCounter < DeletionItemsPerCycle)
            {
                // delete the item from imgur and from the storage.
                long value = dIt.next();
                requestCounter++;
                try
                {
                    _imgurManager.getClient().commentService().deleteComment(value);
                } catch (BaringoApiException ex)
                {
                    if (    !BaringoExceptionHelper.isNotFound(ex) &&
                            !BaringoExceptionHelper.isBadRequest(ex))
                        ex.printStackTrace();
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }

                deletedComments.add(value);
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                // whether successful or not, always sleep in this thread.
                Thread.sleep(20000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
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

    @Override
    public String getName()
    {
        return "CommentDeletion";
    }
}
