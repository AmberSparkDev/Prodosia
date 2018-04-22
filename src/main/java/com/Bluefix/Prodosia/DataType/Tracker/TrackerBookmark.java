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

package com.Bluefix.Prodosia.DataType.Tracker;

import com.github.kskelm.baringo.model.Comment;

import java.util.Date;
import java.util.Objects;

/**
 * Data class to keep track of a tracker bookmark. This indicates the last comment of the user that
 * was read.
 */
public class TrackerBookmark
{
    /**
     * Id of the last comment.
     */
    private long lastCommentId;

    /**
     * time of the comment
     */
    private Date lastCommentTime;

    private Tracker tracker;


    public TrackerBookmark(long lastCommentId, Date lastCommentTime, Tracker tracker)
    {
        this.lastCommentId = lastCommentId;
        this.lastCommentTime = lastCommentTime;
        this.tracker = tracker;
    }

    public long getLastCommentId()
    {
        return lastCommentId;
    }

    public Date getLastCommentTime()
    {
        return lastCommentTime;
    }

    public Tracker getTracker()
    {
        return tracker;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackerBookmark that = (TrackerBookmark) o;
        return Objects.equals(tracker, that.tracker);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(tracker);
    }
}
