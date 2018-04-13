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

package com.Bluefix.Prodosia.Imgur.CommentDeletion;

import com.Bluefix.Prodosia.Exception.BaringoExceptionHelper;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ApiDistribution;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Module.ImgurIntervalRunner;
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
    //region variables

    /**
     * If this option is enabled, the deletion of a comment is ensured, but
     * the total amount of deletions per hours will decrease to half capacity.
     */
    private static final boolean ensureDeletion = false;

    /**
     * Separate the cycles in parts of 6, 10 minutes in between each.
     */
    private static final int DeletionItemsPerCycle = ApiDistribution.DeletionModule / 6;

    //endregion

    //region Singleton and Constructor

    private static CommentDeletionExecution me;

    /**
     * Retrieve the Comment Deletion Module object.
     * @return The Comment Deletion Module object.
     */
    public static CommentDeletionExecution handler()
    {
        if (me == null)
            me = new CommentDeletionExecution();

        return me;
    }

    /**
     * Create a new Module object for the Comment Deletion functionality.
     */
    private CommentDeletionExecution()
    {
        super(ApiDistribution.DeletionModule);

        this.deletedComments = new HashSet<>();
        requestCounter = 0;
    }

    //endregion




    private HashSet<Long> deletedComments;

    private int requestCounter;

    @Override
    public void run()
    {

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
                        Comment com = ImgurManager.client().commentService().getComment(value);

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
                ImgurManager.client().commentService().deleteComment(value);
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
    protected int projectedRequests()
    {
        int tmpReq = requestCounter;
        requestCounter = 0;
        return tmpReq;
    }
}
