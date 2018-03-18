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

package com.Bluefix.Prodosia.DataType.Comments;

import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;

/**
 * Request to post a comment.
 */
public interface ICommentRequest
{
    /**
     * Retrieve the imgur-id that should be replied to.
     * @return
     */
    String getImgurId() throws BaringoApiException, IOException, URISyntaxException;

    /**
     * Retrieve the parent-id that should be replied to. Return -1 to indicate there is no existing
     * parent comment.
     * @return
     */
    Comment getParent() throws BaringoApiException, IOException, URISyntaxException;

    /**
     * Retrieve all comments that should be executed by this tag request.
     * @return
     */
    LinkedList<String> getComments() throws Exception;


    /**
     * Indicate whether the entry deep-equals the other request.
     * @param cq
     * @return
     */
    boolean deepEquals(ICommentRequest cq);


    /**
     * Remove the item from the storage.
     */
    void remove() throws Exception;



}
