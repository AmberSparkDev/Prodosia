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
import com.Bluefix.Prodosia.DataType.TagRequest;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Imgur helper class that handles Tag Requests.
 * It has a collection of all open tag requests that still need to be (partially) handled.
 *
 * `TagExecution` uses this class to tag the items in the queue. This class merely handles the storage.
 */
public class TagRequestHandler extends LocalStorageHandler<TagRequest>
{
    /**
     * The amount of tag requests that should be executed.
     * Recommended to have higher than 1, so that exceptionally large
     * taglists don't clog the tag requests.
     */
    private static final int SimultaneousTagRequest = 4;


    //region Singleton and constructor

    private static TagRequestHandler me;

    /**
     * The thread that handles the actual tagging.
     */
    private static TagExecution tagExecution;

    public static TagRequestHandler handler()
    {
        if (me == null)
        {
            me = new TagRequestHandler();

            // start an underlying thread that handles the tagrequests.
            tagExecution = new TagExecution();
            tagExecution.start();
        }

        return me;
    }

    private TagRequestHandler()
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
    public void add(TagRequest tagRequest) throws Exception
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

            // remove the old item and add the new one.
            super.remove(oldT);
            super.add(merge);
        }
        else
        {
            super.add(tagRequest);
        }
    }

    /**
     * Add a tag request to the queue. The class will automatically handle when this gets tagged.
     *
     * @param tagRequest
     */
    @Override
    protected void addItem(TagRequest tagRequest) throws Exception
    {
        dbAddTagrequest(tagRequest);
    }

    /**
     * Remove the item from the queue.
     *
     * @param tagRequest
     */
    @Override
    protected void removeItem(TagRequest tagRequest) throws Exception
    {
        dbRemoveTagrequest(tagRequest);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<TagRequest> getAllItems() throws Exception
    {
        return dbGetTagrequests();
    }

    //endregion

    //region Database Management

    private synchronized static void dbAddTagrequest(TagRequest t) throws Exception
    {
        if (t == null)
            return;

        String query =
                "INSERT INTO TagQueue " +
                "(imgurId, parentComment, taglists, rating, filters) " +
                "VALUES (?,?,?,?,?);";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, t.getImgurId());
        prep.setString(2, t.getParentComment());
        prep.setString(3, t.getDbTaglists());
        prep.setInt(4, t.getRating().getValue());
        prep.setString(5, t.getFilters());

        SqlDatabase.execute(prep);
    }

    private synchronized static void dbRemoveTagrequest(TagRequest t) throws SQLException
    {
        String query =
                "DELETE FROM TagQueue " +
                "WHERE imgurId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, t.getImgurId());
        SqlDatabase.execute(prep);
    }

    private synchronized static ArrayList<TagRequest> dbGetTagrequests() throws Exception
    {
        String query =
                "SELECT imgurId, parentComment, taglists, rating, filters " +
                "FROM TagQueue;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);
        ArrayList<TagRequest> output = new ArrayList<>();

        while (rs.next())
        {
            String imgurId = rs.getString(1);
            String parentComment = rs.getString(2);
            String taglists = rs.getString(3);
            int rating = rs.getInt(4);
            String filters = rs.getString(5);

            output.add(new TagRequest(imgurId, parentComment, taglists, rating, filters));
        }

        return output;
    }

    //endregion


}





























