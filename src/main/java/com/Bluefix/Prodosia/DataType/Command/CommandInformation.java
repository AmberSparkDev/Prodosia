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
     * The parent-id that pertains to the command.
     */
    private long parentId;


    private Comment parentComment;


    //region builder

    protected CommandInformation()
    {

    }

    public CommandInformation setTracker(Tracker t)
    {
        this.tracker = t;
        return this;
    }

    public CommandInformation setImgurId(String imgurId)
    {
        this.imgurId = imgurId;
        return this;
    }

    public CommandInformation setParentId(long parentId) throws BaringoApiException, IOException, URISyntaxException
    {
        this.parentId = parentId;

        // retrieve the comment if necessary.
        if (parentComment == null || parentComment.getId() != this.parentId)
        {
            this.parentComment =
                    ImgurManager.client().commentService().getComment(this.parentId);
        }

        return this;
    }

    public CommandInformation setParentComment(Comment parentComment)
    {
        this.parentComment = parentComment;
        this.parentId = this.parentComment.getId();

        return this;
    }



    //endregion





    public Tracker getTracker()
    {
        return tracker;
    }

    public String getImgurId()
    {
        return imgurId;
    }

    public long getParentId()
    {
        return parentId;
    }

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
