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

import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.Discord.Archive.ArchiveManager;
import com.Bluefix.Prodosia.Imgur.Tagging.CommentExecution;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;
import com.github.kskelm.baringo.util.BaringoApiException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Imgur helper class that handles Tag Requests.
 * It has a collection of all open tag requests that still need to be (partially) handled.
 *
 * `CommentExecution` uses this class to tag the items in the queue. This class merely handles the storage.
 */
public class TagRequestStorage extends LocalStorageHandler<TagRequest>
{
    //region Singleton and constructor

    private static TagRequestStorage me;

    public static TagRequestStorage handler()
    {
        if (me == null)
        {
            me = new TagRequestStorage();

            // start an underlying thread that handles the tagrequests.
            if (!CommentExecution.handler().isAlive())
                CommentExecution.handler().start();
        }

        return me;
    }

    private TagRequestStorage()
    {
        super(true);
    }

    //endregion

    //region Local Storage Handler implementation

    /**
     * Add an item to the collection.
     * @param tagRequest The item to be added.
     */
    @Override
    public void set(TagRequest tagRequest) throws SQLException, BaringoApiException, IOException, URISyntaxException, LoginException
    {
        // this method is overridden to check for items that can be merged together.

        // if there is overlap with an already-existing tagrequest with the same imgur-id,
        // merge the two.
        ArrayList<TagRequest> requests = super.getAll();

        int index = requests.indexOf(tagRequest);

        // if there was a duplicate tag-request in the queue, merge it and delete the old one.
        if (index >= 0)
        {
            TagRequest oldT = requests.get(index);

            // merge-order is important, since the values from the new request are given priority
            TagRequest merge = oldT.merge(tagRequest);

            // complete the old item and add the new one.
            super.remove(oldT);
            super.set(merge);
        }
        else
        {
            super.set(tagRequest);

            // Let the archive manager handle the tagrequest as well.
            ArchiveManager.handleTagRequest(tagRequest);
        }
    }

    /**
     * Add a tag request to the queue. The class will automatically handle when this gets tagged.
     *
     * @param tagRequest
     */
    @Override
    protected TagRequest setItem(TagRequest tagRequest) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        return dbSetTagrequest(tagRequest);
    }

    /**
     * Remove the item from the queue.
     *
     * @param tagRequest
     */
    @Override
    protected void removeItem(TagRequest tagRequest) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        dbRemoveTagrequest(tagRequest);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<TagRequest> getAllItems() throws SQLException
    {
        return dbGetTagrequests();
    }

    //endregion

    //region Database Management

    private synchronized static TagRequest dbSetTagrequest(TagRequest t) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        if (t == null)
            return null;

        // retrieve the old tag request
        TagRequest oldRequest = dbGetTagrequest(t.getImgurId(), t.getParentId());

        // complete the old tag request to replace it with the new one.
        dbRemoveTagrequest(oldRequest);

        String query =
                "INSERT INTO TagQueue " +
                "(imgurId, parentComment, taglists, rating, filters, cleanComments) " +
                "VALUES (?,?,?,?,?,?);";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, t.getImgurId());
        prep.setLong(2, t.getParentId());
        prep.setString(3, t.getDbTaglists());
        prep.setInt(4, t.getRating().getValue());
        prep.setString(5, t.getFilter());
        prep.setBoolean(6, t.isCleanComments());

        SqlDatabase.execute(prep);

        assert(prep.isClosed());

        return oldRequest;
    }

    private synchronized static void dbRemoveTagrequest(TagRequest t) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        // if the tag request is null, skip
        if (t == null)
            return;

        String query =
                "DELETE FROM TagQueue " +
                "WHERE imgurId = ? AND parentComment = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, t.getImgurId());
        prep.setLong(2, t.getParentId());
        SqlDatabase.execute(prep);

        assert(prep.isClosed());
    }

    private synchronized static TagRequest dbGetTagrequest(String imgurId, long parentId) throws SQLException
    {
        String query =
                "SELECT imgurId, parentComment, taglists, rating, filters, cleanComments " +
                "FROM TagQueue " +
                "WHERE imgurId = ? AND parentComment = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, imgurId);
        prep.setLong(2, parentId);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return
        ArrayList<TagRequest> parsedRequests = parseTagrequests(rs);

        prep.close();
        assert(prep.isClosed());

        if (parsedRequests.isEmpty())
            return null;

        return parsedRequests.get(0);
    }

    private synchronized static ArrayList<TagRequest> dbGetTagrequests() throws SQLException
    {
        String query =
                "SELECT imgurId, parentComment, taglists, rating, filters, cleanComments " +
                "FROM TagQueue;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return
        ArrayList<TagRequest> parsedRequests = parseTagrequests(rs);

        prep.close();
        assert(prep.isClosed());

        return parsedRequests;
    }

    private static ArrayList<TagRequest> parseTagrequests(ResultSet rs) throws SQLException
    {
        ArrayList<TagRequest> output = new ArrayList<>();

        while (rs.next())
        {
            String imgurId = rs.getString(1);
            long parentComment = rs.getLong(2);
            String taglists = rs.getString(3);
            int rating = rs.getInt(4);
            String filters = rs.getString(5);
            boolean cleanComments = rs.getBoolean(6);

            output.add(new TagRequest(imgurId, parentComment, taglists, rating, filters, cleanComments));
        }

        // close the resultset
        rs.close();

        return output;
    }

    //endregion


}





























