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

import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Handler for taglist management.
 */
public class TaglistHandler extends LocalStorageHandler<Taglist>
{
    //region Singleton and Constructor

    private static TaglistHandler me;

    public static TaglistHandler handler()
    {
        if (me == null)
            me = new TaglistHandler();

        return me;
    }

    private TaglistHandler()
    {
        super(true);
    }

    //endregion


    //region Local Storage Handler implementation

    /**
     * Add an item to the storage.
     *
     * @param t
     */
    @Override
    protected Taglist setItem(Taglist t) throws Exception
    {
        return dbSetTaglist(t);
    }

    /**
     * Remove an item from the storage.
     *
     * @param t
     */
    @Override
    protected void removeItem(Taglist t) throws Exception
    {
        dbRemoveTaglist(t);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<Taglist> getAllItems() throws Exception
    {
        return dbGetTaglists();
    }

    //endregion

    //region Database management

    private synchronized static Taglist dbSetTaglist(Taglist t) throws Exception
    {
        if (t == null)
            return null;

        // retrieve the old taglist
        Taglist oldTaglist = dbGetTaglist(t.getAbbreviation());

        // remove the old taglist to replace it.
        dbRemoveTaglist(oldTaglist);

        // insert the tracker into the database.
        // if this replaces a taglist, use the old taglist id.
        String query;
        if (oldTaglist == null)
        {
            query = "INSERT INTO Taglist " +
                    "(abbreviation, description, hasRatings) " +
                    "VALUES (?,?,?);";
        }
        else
        {
            query = "INSERT INTO Taglist " +
                    "(id, abbreviation, description, hasRatings) " +
                    "VALUES (?, ?,?,?);";
        }

        // set the arguments
        PreparedStatement prep = SqlDatabase.getStatement(query);
        int argCounter = 1;

        if (oldTaglist != null)
        {
            prep.setLong(argCounter++, oldTaglist.getId());
        }

        prep.setString(argCounter++, t.getAbbreviation());
        prep.setString(argCounter++, t.getDescription());
        prep.setBoolean(argCounter++, t.hasRatings());

        SqlDatabase.execute(prep);

        return oldTaglist;
    }

    private synchronized static void dbRemoveTaglist(Taglist t) throws SQLException
    {
        // skip if the taglist was null
        if (t == null)
            return;

        // if the abbreviation fits, remove the taglist.
        String query =
                "DELETE FROM Taglist " +
                "WHERE abbreviation = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, t.getAbbreviation());

        SqlDatabase.execute(prep);
    }

    private synchronized static ArrayList<Taglist> dbGetTaglists() throws Exception
    {
        String query =
                "SELECT id, abbreviation, description, hasRatings " +
                "FROM Taglist;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        return parseTaglists(rs);
    }

    private synchronized static Taglist dbGetTaglist(String abbreviation) throws Exception
    {
        String query =
                "SELECT id, abbreviation, description, hasRatings " +
                "FROM Taglist " +
                "WHERE abbreviation = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, abbreviation);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return
        ArrayList<Taglist> parseResult = parseTaglists(rs);

        if (parseResult.isEmpty())
            return null;

        return parseResult.get(0);
    }

    private static ArrayList<Taglist> parseTaglists(ResultSet rs) throws SQLException
    {
        ArrayList<Taglist> taglists = new ArrayList<>();

        while (rs.next())
        {
            long id = rs.getLong(1);
            String abbr = rs.getString(2);
            String desc = rs.getString(3);
            boolean hasRatings = rs.getBoolean(4);

            taglists.add(new Taglist(id, abbr, desc, hasRatings));
        }

        return taglists;
    }

    //endregion


    //region Helper methods

    /**
     * Retrieve the taglist that corresponds to the id.
     * @param taglistId
     * @return
     */
    public static synchronized Taglist getTaglistById(long taglistId) throws Exception
    {
        // TODO: if this methods is used frequently, it might be worth making a HashMap<long, Taglist>
        ArrayList<Taglist> taglists = handler().getAll();

        for (Taglist tl : taglists)
        {
            if (taglistId == tl.getId())
                return tl;
        }

        return null;
    }

    /**
     * Retrieve a taglist with the specified abbreviation
     * @param abbreviation The abbreviation of the taglist.
     * @return The taglist if it existed, or null otherwise.
     * @throws Exception
     */
    public static synchronized Taglist getTaglistByAbbreviation(String abbreviation) throws Exception
    {
        if (abbreviation == null || abbreviation.isEmpty())
            return null;

        return dbGetTaglist(abbreviation);
    }


    //endregion

}
