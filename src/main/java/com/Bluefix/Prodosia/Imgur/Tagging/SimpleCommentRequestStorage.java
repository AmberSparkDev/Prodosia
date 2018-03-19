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

package com.Bluefix.Prodosia.Imgur.Tagging;

import com.Bluefix.Prodosia.DataHandler.LocalStorageHandler;
import com.Bluefix.Prodosia.DataType.Comments.SimpleCommentRequest;
import com.Bluefix.Prodosia.SQLite.SqlBuilder;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;
import com.github.kskelm.baringo.util.BaringoApiException;

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
public class SimpleCommentRequestStorage extends LocalStorageHandler<SimpleCommentRequest>
{
    //region Singleton and constructor

    private static SimpleCommentRequestStorage me;

    public static SimpleCommentRequestStorage handler()
    {
        if (me == null)
        {
            me = new SimpleCommentRequestStorage();

            // start an underlying thread that handles the tagrequests.
            if (!CommentExecution.handler().isAlive())
                CommentExecution.handler().start();
        }

        return me;
    }

    private SimpleCommentRequestStorage()
    {
        super(true);
    }

    //endregion

    //region Local Storage Handler implementation

    /**
     * Add a tag request to the queue. The class will automatically handle when this gets tagged.
     *
     * @param tagRequest
     */
    @Override
    protected SimpleCommentRequest setItem(SimpleCommentRequest tagRequest) throws Exception
    {
        return dbSetCommentRequest(tagRequest);
    }

    /**
     * Remove the item from the queue.
     *
     * @param tagRequest
     */
    @Override
    protected void removeItem(SimpleCommentRequest tagRequest) throws Exception
    {
        dbRemoveCommentRequest(tagRequest);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<SimpleCommentRequest> getAllItems() throws Exception
    {
        return dbGetCommentRequests();
    }

    //endregion

    //region Database Management

    private synchronized static SimpleCommentRequest dbSetCommentRequest(SimpleCommentRequest t) throws Exception
    {
        if (t == null)
            return null;

        // retrieve the old comment request.
        SimpleCommentRequest oldRequest = dbGetCommentRequest(t.getImgurId(), t.getParentId());

        // remove the old request
        dbRemoveCommentRequest(oldRequest);

        String query =
                "INSERT INTO CommentQueue " +
                "(imgurId, parentId, lines) " +
                "VALUES (?,?,?);";

        SqlBuilder builder = SqlBuilder.Builder();

        for (String line : t.getComments())
        {
            PreparedStatement prep = SqlDatabase.getStatement(query);
            prep.setString(1, t.getImgurId());
            prep.setLong(2, t.getParent().getId());
            prep.setString(3, line);

            builder = builder.execute(prep);
        }

        builder.commit();

        return oldRequest;
    }

    private synchronized static void dbRemoveCommentRequest(SimpleCommentRequest t) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        // if the request is null, skip
        if (t == null)
            return;

        String query =
                "DELETE FROM CommentQueue " +
                "WHERE imgurId = ? AND parentId = ? AND lines = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, t.getImgurId());
        prep.setLong(2, t.getParent().getId());
        prep.setString(3, t.dbParseComments());
        SqlDatabase.execute(prep);
    }

    private synchronized static SimpleCommentRequest dbGetCommentRequest(String imgurId, long parentId) throws Exception
    {
        String query =
                "SELECT imgurId, parentId, lines " +
                "FROM CommentQueue " +
                "WHERE imgurId = ? AND parentId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, imgurId);
        prep.setLong(2, parentId);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return.
        ArrayList<SimpleCommentRequest> parsedResult = parseCommentRequests(rs);

        if (parsedResult.isEmpty())
            return null;

        return parsedResult.get(0);
    }


    private synchronized static ArrayList<SimpleCommentRequest> dbGetCommentRequests() throws Exception
    {
        String query =
                "SELECT imgurId, parentId, lines " +
                "FROM CommentQueue;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        return parseCommentRequests(rs);
    }

    private static ArrayList<SimpleCommentRequest> parseCommentRequests(ResultSet rs) throws SQLException
    {
        ArrayList<SimpleCommentRequest> output = new ArrayList<>();

        while (rs.next())
        {
            String imgurId = rs.getString(1);
            long parentId = rs.getLong(2);
            String comments = rs.getString(3);

            output.add(SimpleCommentRequest.parse(imgurId, parentId, comments));
        }

        return output;
    }

    //endregion


}





























