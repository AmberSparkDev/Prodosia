/*
 * Copyright (c) 2018 RoseLaLuna
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

package com.Bluefix.Prodosia.Data.DataHandler;

import com.Bluefix.Prodosia.Business.Imgur.Tagging.CommentExecution;
import com.Bluefix.Prodosia.Business.Imgur.Tagging.ICommentExecution;
import com.Bluefix.Prodosia.Data.DataType.Comments.SimpleCommentRequest;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.Bluefix.Prodosia.Data.SQLite.SqlBuilder;
import com.Bluefix.Prodosia.Data.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Imgur helper class that handles Tag Requests.
 * It has a collection of all open tag requests that still need to be (partially) handled.
 * <p>
 * `CommentExecution` uses this class to tag the items in the queue. This class merely handles the storage.
 */
public class SimpleCommentRequestStorage extends LocalStorageHandler<SimpleCommentRequest>
{
    private ILogger _logger;
    private ILogger _appLogger;


    public SimpleCommentRequestStorage(ILogger logger,
                                       ILogger appLogger)
    {
        super(true);
        _logger = logger;
        _appLogger = appLogger;
    }


    private SimpleCommentRequest dbSetCommentRequest(SimpleCommentRequest t) throws SQLException
    {
        if (t == null)
            return null;

        // retrieve the old comment request.
        SimpleCommentRequest oldRequest = dbGetCommentRequest(t.getId());

        // remove the old request
        dbRemoveCommentRequest(oldRequest);

        // if the id was already known, insert with id.
        boolean hasId = t.getId() >= 0;


        String query =
                "INSERT INTO CommentQueue " +
                        "(" + (hasId ? "id, " : "") + "imgurId, parentId, lines) " +
                        "VALUES (" + (hasId ? "?," : "") + "?,?,?);";

        SqlBuilder builder = SqlBuilder.Builder();

        // set the parameters.
        PreparedStatement prep = SqlDatabase.getStatement(query);
        int counter = 1;

        if (hasId)
            prep.setLong(counter++, t.getId());

        prep.setString(counter++, t.getImgurId());
        prep.setLong(counter++, t.getParentId());
        prep.setString(counter++, t.dbParseComments());

        // execute the query. Update the id if none was known beforehand.
        if (hasId)
        {
            SqlDatabase.execute(prep);
        } else
        {
            t.setId(SqlDatabase.getAffectedRow(prep));
        }

        assert (prep.isClosed());

        return oldRequest;
    }

    private void dbRemoveCommentRequest(SimpleCommentRequest t) throws SQLException
    {
        // if the request is null, skip
        if (t == null)
            return;

        String query =
                "DELETE FROM CommentQueue " +
                        "WHERE id = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, t.getId());
        SqlDatabase.execute(prep);

        assert (prep.isClosed());
    }

    private SimpleCommentRequest dbGetCommentRequest(long id) throws SQLException
    {
        String query =
                "SELECT id, imgurId, parentId, lines " +
                        "FROM CommentQueue " +
                        "WHERE id = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, id);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return.
        ArrayList<SimpleCommentRequest> parsedResult = parseCommentRequests(rs);

        prep.close();
        assert (prep.isClosed());

        if (parsedResult.isEmpty())
            return null;

        return parsedResult.get(0);
    }

    //endregion

    //region Database Management

    private ArrayList<SimpleCommentRequest> dbGetCommentRequests() throws SQLException
    {
        String query =
                "SELECT id, imgurId, parentId, lines " +
                        "FROM CommentQueue;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return.
        ArrayList<SimpleCommentRequest> parsedResult = parseCommentRequests(rs);

        prep.close();
        assert (prep.isClosed());

        return parsedResult;
    }

    private ArrayList<SimpleCommentRequest> parseCommentRequests(ResultSet rs) throws SQLException
    {
        ArrayList<SimpleCommentRequest> output = new ArrayList<>();

        while (rs.next())
        {
            long id = rs.getLong(1);
            String imgurId = rs.getString(2);
            long parentId = rs.getLong(3);
            String comments = rs.getString(4);

            output.add(SimpleCommentRequest.parse(id, imgurId, parentId, comments));
        }

        // close the resultset
        rs.close();

        return output;
    }

    /**
     * Add a tag request to the queue. The class will automatically handle when this gets tagged.
     *
     * @param tagRequest
     */
    @Override
    protected SimpleCommentRequest setItem(SimpleCommentRequest tagRequest) throws SQLException
    {
        return dbSetCommentRequest(tagRequest);
    }

    /**
     * Remove the item from the queue.
     *
     * @param tagRequest
     */
    @Override
    protected void removeItem(SimpleCommentRequest tagRequest) throws SQLException
    {
        dbRemoveCommentRequest(tagRequest);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<SimpleCommentRequest> getAllItems() throws SQLException
    {
        return dbGetCommentRequests();
    }

    //endregion


}





























