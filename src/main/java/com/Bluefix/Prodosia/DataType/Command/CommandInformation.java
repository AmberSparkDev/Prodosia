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

package com.Bluefix.Prodosia.DataType.Command;

import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

/**
 * Information pertaining to Command Execution.
 */
public abstract class CommandInformation
{
    /**
     * The tracker that issued the command.
     */
    private Tracker tracker;

    /**
     * The imgur id that pertains to the command.
     */
    private String imgurId;

    /**
     * The parent comment that pertains to the command.
     */
    private Comment parentComment;


    //region builder

    /**
     * Set the Tracker information for this command.
     * @param t The Tracker that issued the command.
     * @return Builder feedback.
     */
    public CommandInformation setTracker(Tracker t)
    {
        this.tracker = t;
        return this;
    }

    /**
     * Set the Imgur Id of the specified post for this command.
     * @param imgurId The Imgur Id of the post.
     * @return Builder feedback.
     */
    public CommandInformation setImgurId(String imgurId)
    {
        this.imgurId = imgurId;
        return this;
    }

    /**
     * Set the parent-id of the specified parent comment for this command.
     * @param parentId The id of the parent comment.
     * @return Builder feedback.
     * @throws BaringoApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    public CommandInformation setParentId(long parentId) throws BaringoApiException, IOException, URISyntaxException
    {
        // retrieve the comment if necessary.
        if (parentComment == null || parentComment.getId() != parentId)
        {
            this.parentComment =
                    ImgurManager.client().commentService().getComment(parentId);
        }

        return this;
    }

    /**
     * Set the specified parent comment for this command.
     * @param parentComment The parent comment.
     * @return Builder feedback.
     */
    public CommandInformation setParentComment(Comment parentComment)
    {
        this.parentComment = parentComment;

        return this;
    }



    //endregion


    /**
     * Retrieve the stored Tracker.
     * @return The stored Tracker object.
     */
    public Tracker getTracker()
    {
        return tracker;
    }

    /**
     * Retrieve the imgur-id of the specified post.
     * @return The imgur-id of the specified post, or null if no specified post was indicated.
     */
    public String getImgurId()
    {
        // if the imgur id was not known, attempt to extract if from the parent comment.
        if (this.imgurId == null || this.imgurId.trim().isEmpty())
        {
            if (this.parentComment == null)
                return null;

            this.imgurId = this.parentComment.getImageId();
        }

        return imgurId;
    }

    /**
     * Retrieve the parent comment.
     * @return The parent comment object.
     */
    public Comment getParentComment()
    {
        return parentComment;
    }

    //region actions

    /**
     * Reply to the user with the following entries.
     * @param entries The entries to reply to the user to.
     */
    public abstract void reply(String... entries) throws Exception;

    //endregion
}
