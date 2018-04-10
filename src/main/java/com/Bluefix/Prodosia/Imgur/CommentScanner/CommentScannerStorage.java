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

import com.Bluefix.Prodosia.DataHandler.LocalStorageHandler;
import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerBookmark;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class CommentScannerStorage extends LocalStorageHandler<TrackerBookmark>
{
    //region Singleton and Constructor

    private static CommentScannerStorage me;

    public static CommentScannerStorage handler()
    {
        if (me == null)
            me = new CommentScannerStorage();

        return me;
    }

    private CommentScannerStorage()
    {
        super(true);
    }

    //endregion


    //region Local Storage Handler implementation

    /**
     * Retrieve the prepared statements necessary for adding an item.
     *
     * @param trackerBookmark
     */
    @Override
    protected TrackerBookmark setItem(TrackerBookmark trackerBookmark) throws Exception
    {
        return dbSetBookmark(trackerBookmark);
    }

    /**
     * Remove an item from the storage.
     *
     * @param trackerBookmark
     */
    @Override
    protected void removeItem(TrackerBookmark trackerBookmark) throws Exception
    {
        dbRemoveBookmark(trackerBookmark);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<TrackerBookmark> getAllItems() throws Exception
    {
        return dbGetBookmarks();
    }

    //endregion

    //region Database management

    private synchronized static TrackerBookmark dbSetBookmark(TrackerBookmark tb) throws Exception
    {
        if (tb == null)
            return null;

        // retrieve the old tracker bookmark
        TrackerBookmark oldBookmark = dbGetBookmark(tb.getTracker().getImgurId());

        // complete the old bookmark and replace it.
        dbRemoveBookmark(oldBookmark);

        String query =
                "INSERT INTO CommentScanner " +
                "(imgurId, lastCommentId, lastCommentTime) " +
                "VALUES (?,?,?);";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, tb.getTracker().getImgurId());
        prep.setLong(2, tb.getLastCommentId());
        prep.setLong(3, tb.getLastCommentTime().getTime());

        SqlDatabase.execute(prep);

        return oldBookmark;
    }

    private synchronized static void dbRemoveBookmark(TrackerBookmark tb) throws SQLException
    {
        if (tb == null)
            return;

        String query =
                "DELETE FROM CommentScanner " +
                "WHERE imgurId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, tb.getTracker().getImgurId());

        SqlDatabase.execute(prep);
    }

    private synchronized static TrackerBookmark dbGetBookmark(long imgurId) throws Exception
    {
        String query =
                "SELECT imgurId, lastCommentId, lastCommentTime " +
                "FROM CommentScanner " +
                "WHERE imgurId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, imgurId);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse result and return
        ArrayList<TrackerBookmark> parsedResult = parseBookmarks(rs);

        if (parsedResult.isEmpty())
            return null;

        return parsedResult.get(0);
    }

    private synchronized static ArrayList<TrackerBookmark> dbGetBookmarks() throws Exception
    {
        String query =
                "SELECT imgurId, lastCommentId, lastCommentTime " +
                "FROM CommentScanner;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        return parseBookmarks(rs);
    }

    private static ArrayList<TrackerBookmark> parseBookmarks(ResultSet rs) throws Exception
    {
        ArrayList<TrackerBookmark> output = new ArrayList<>();

        while (rs.next())
        {
            long imgurId = rs.getLong(1);
            long commentId = rs.getLong(2);
            long commentTime = rs.getLong(3);

            Tracker t = TrackerHandler.getTrackerByImgurId(imgurId);

            output.add(new TrackerBookmark(commentId, new Date(commentTime), t));
        }

        return output;
    }

    //endregion

    //region Helper methods

    /**
     * Retrieve the bookmark for the user with the specified imgur id, or null if it did not exist.
     * @param imgurId The user to retrieve the bookmark for.
     * @return The bookmark for the specified user if it existed, null otherwise.
     */
    public static TrackerBookmark getBookmarkByImgurId(long imgurId) throws Exception
    {
        // ignore for an invalid imgur id
        if (imgurId < 0)
            return null;

        ArrayList<TrackerBookmark> bookmarks = handler().getAll();

        for (TrackerBookmark t : bookmarks)
        {
            if (t.getTracker().getImgurId() == imgurId)
                return t;
        }

        return null;
    }

    //endregion
}


















