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

package com.Bluefix.Prodosia.DataHandler;

import com.Bluefix.Prodosia.Data.DataHandler.CommentScannerStorage;
import com.Bluefix.Prodosia.Data.DataHandler.LocalStorageHandler;
import com.Bluefix.Prodosia.Data.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.Data.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Data.DataType.Tracker.TrackerBookmark;
import com.Bluefix.Prodosia.Data.DataType.Tracker.TrackerPermissions;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Date;

public class CommentScannerStorageTest extends DataHandlerTest<TrackerBookmark>
{
    private static final String TestImgurName = "mashedstew";
    private static final long TestImgurId = 33641050;

    private TrackerBookmark value;

    private Tracker tracker;



    //region abstract method implementation


    public CommentScannerStorageTest(boolean useLocalStorage)
    {
        super(useLocalStorage);
    }

    @Override
    protected LocalStorageHandler<TrackerBookmark> getHandler()
    {
        return CommentScannerStorage.handler();
    }

    @Override
    protected TrackerBookmark getItem()
    {
        return value;
    }

    //endregion

    @Before
    public void setUp() throws Exception
    {
        // create a new Tracker
        TrackerPermissions perm = new TrackerPermissions(TrackerPermissions.TrackerType.ADMIN);
        tracker = new Tracker(TestImgurName, TestImgurId, "", "", "", perm);
        TrackerHandler.handler().set(tracker);

        value = new TrackerBookmark(0, new Date(Long.MAX_VALUE), tracker);
    }

    @After
    public void tearDown() throws Exception
    {
        CommentScannerStorage.handler().remove(value);
        TrackerHandler.handler().remove(tracker);
    }


    @Test
    public void testGetBookmarkByImgurId() throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        TrackerBookmark tb = CommentScannerStorage.getBookmarkByImgurId(TestImgurId);
        Assert.assertNull(tb);

        CommentScannerStorage.handler().set(value);

        tb = CommentScannerStorage.getBookmarkByImgurId(TestImgurId);
        Assert.assertEquals(value, tb);
    }


}