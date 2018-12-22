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

package com.Bluefix.Prodosia.Data.DataType.Comments;

import java.util.LinkedList;

/**
 * The Feedback Request class is meant for comments that the user needs to know
 * the id of immediately after posting it. Using this class allows for usage of
 * `CommentExecution`
 *
 * Any FeedbackRequest items will receive priority when posting comments to
 * keep the application on hold for as short as possible.
 *
 * This class is threaded to ensure that the rest of the application
 * continues execution while `runAfter` is being executed.
 */
public abstract class FeedbackRequest extends Thread implements ICommentRequest
{
    private String comment;
    private long commentId;


    /**
     * A feedback request should only allow for one single comment.
     * @param comment
     */
    protected FeedbackRequest(String comment)
    {
        this.comment = comment;
    }


    @Override
    public LinkedList<String> getComments()
    {
        LinkedList<String> output = new LinkedList<>();
        output.add(comment);

        return output;
    }

    /**
     *
     * @param commentId
     */
    public void setCommentId(long commentId)
    {
        this.commentId = commentId;
    }

    /**
     * Run the `runAfter` command, catching any exceptions it might throw.
     */
    @Override
    public void run()
    {
        try
        {
            runAfter(this.commentId);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method will be executed after the comment has been posted.
     * @param commentId The comment-id of the comment that was posted.
     */
    protected abstract void runAfter(long commentId);



}
