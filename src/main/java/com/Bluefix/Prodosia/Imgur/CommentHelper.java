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

package com.Bluefix.Prodosia.Imgur;

import com.github.kskelm.baringo.model.Comment;

import java.util.LinkedList;
import java.util.List;

public class CommentHelper
{
    /**
     * Check if the list of comments contains a comment with the specified commentId.
     * @param comments The collection of comments.
     * @param commentId The id of the comment to be checked.
     * @return True iff the comment was contained in the collection, false otherwise.
     */
    public static boolean containsComment(List<Comment> comments, long commentId)
    {
        List<Comment> curComments = comments;

        while (!curComments.isEmpty())
        {
            List<Comment> newComments = new LinkedList<>();

            for (Comment c : curComments)
            {
                // if this comment corresponds with the comment-id we are looking for, return true.
                if (c.getId() == commentId)
                    return true;

                // add all the children to the list of new comments.
                newComments.addAll(c.getChildren());
            }

            curComments = newComments;
        }

        return false;
    }

}
