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

package com.Bluefix.Prodosia.Imgur.CommentScanner;

import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerBookmark;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sun.util.calendar.BaseCalendar;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

public class CommentScannerStorageTest
{
    private CommentScannerStorage handler;
    private TrackerBookmark value;

    private Tracker tracker;

    @Before
    public void setUp() throws Exception
    {
        handler = CommentScannerStorage.handler();


        // create a new Tracker
        TrackerPermissions perm = new TrackerPermissions(TrackerPermissions.TrackerType.ADMIN);

        tracker = new Tracker("mashedstew", 1, null, "0000", "", perm);

        TrackerHandler.handler().set(tracker);

        value = new TrackerBookmark(0, new Date(Long.MAX_VALUE), tracker);

    }

    @After
    public void tearDown() throws Exception
    {
        TrackerHandler.handler().remove(tracker);
    }


    @Test
    public void testFunctionalityWithLocalStorage() throws Exception
    {
        handler.setLocalStorage(true);

        ArrayList<TrackerBookmark> bookmarks = handler.getAll();

        if (bookmarks.contains(value))
            fail();

        handler.set(value);
        bookmarks = handler.getAll();

        if (!bookmarks.contains(value))
            fail();

        handler.remove(value);
        bookmarks = handler.getAll();

        if (bookmarks.contains(value))
            fail();


    }

    @Test
    public void testFunctionalityWithoutLocalStorage() throws Exception
    {
        handler.setLocalStorage(false);

        ArrayList<TrackerBookmark> bookmarks = handler.getAll();

        if (bookmarks.contains(value))
            fail();

        handler.set(value);
        bookmarks = handler.getAll();

        if (!bookmarks.contains(value))
            fail();

        handler.remove(value);
        bookmarks = handler.getAll();

        if (bookmarks.contains(value))
            fail();
    }
}