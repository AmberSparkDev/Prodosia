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
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.GUI.GuiUpdate;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;
import com.github.kskelm.baringo.util.BaringoApiException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

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

    /**
     * Clear an item from the collection.
     * This method will also remove *any* references to this taglist in UserSubscriptions,
     * TrackerPermissions and Archives.
     *
     * @param taglist The item to be cleared.
     */
    public void clear(Taglist taglist) throws SQLException, BaringoApiException, IOException, URISyntaxException, LoginException
    {
        if (taglist == null)
            return;

        // if the taglist has no id, it hasn't been stored and can't be permanently removed.
        if (taglist.getId() < 0)
            throw new IllegalArgumentException("This method can only be applied to a taglist that was stored beforehand.");

        dbClearTaglist(taglist);

        // finally, remove the taglist in the conventional way.
        super.remove(taglist);
    }


    //region Local Storage Handler implementation

    @Override
    public void set(Taglist t) throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        super.set(t);
        GuiUpdate.updateTaglists();
    }

    @Override
    public void remove(Taglist t) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        super.remove(t);
        GuiUpdate.updateTaglists();
    }

    /**
     * Add an item to the storage.
     *
     * @param t
     */
    @Override
    Taglist setItem(Taglist t) throws SQLException
    {
        return dbSetTaglist(t);
    }

    /**
     * Remove an item from the storage.
     *
     * @param t
     */
    @Override
    void removeItem(Taglist t) throws SQLException
    {
        dbRemoveTaglist(t);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    ArrayList<Taglist> getAllItems() throws SQLException
    {
        return dbGetTaglists();
    }

    //endregion

    //region Database management

    private synchronized static Taglist dbSetTaglist(Taglist t) throws SQLException
    {
        if (t == null)
            return null;

        // retrieve the old taglist
        Taglist oldTaglist = dbGetTaglist(t.getAbbreviation());

        // complete the old taglist to replace it.
        dbRemoveTaglist(oldTaglist);

        // insert the tracker into the database.
        // if this replaces a taglist, use the old taglist id.
        String query;

        query = "INSERT INTO Taglist " +
                "(" + (oldTaglist != null ? "id," : "") + "abbreviation,description,hasRatings) " +
                "VALUES (" + (oldTaglist != null ? "?," : "") + "?,?,?);";

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

        // if the abbreviation fits, complete the taglist.
        String query =
                "DELETE FROM Taglist " +
                "WHERE abbreviation = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, t.getAbbreviation());

        SqlDatabase.execute(prep);
    }

    //region Database dependency clearing

    /**
     * This method removes any references or dependencies on this taglist, but
     * does not remove the actual taglist itself.
     * @param t
     * @throws SQLException
     */
    private synchronized static void dbClearTaglist(Taglist t) throws SQLException, LoginException, IOException, BaringoApiException, URISyntaxException
    {
        dbClearUserDependencies(t);
        dbClearTrackerPermissions(t);
        dbClearArchiveDependencies(t);

        // ensure that the handlers are refreshed.
        UserHandler.handler().refresh();
        //TrackerHandler.handler().refresh();
        ArchiveHandler.handler().refresh();
    }

    /**
     * Clear all user-subscriptions that rely on this taglist and clear
     * any users that no longer have any active subscriptions.
     * @param t
     */
    private synchronized static void dbClearUserDependencies(Taglist t) throws SQLException
    {
        // delete all usersubscriptions that pertain to this taglist.
        String query0 =
                "DELETE FROM UserSubscription " +
                "WHERE taglistId = ?;";

        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        prep0.setLong(1, t.getId());
        SqlDatabase.execute(prep0);

        assert(prep0.isClosed());

        // Check to see if any users exist that do not have any user-subscriptions.
        String query1 =
                "SELECT id FROM User WHERE id NOT IN " +
                        "(SELECT userId FROM UserSubscription);";

        PreparedStatement prep1 = SqlDatabase.getStatement(query1);
        ArrayList<ResultSet> result = SqlDatabase.query(prep1);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        HashSet<Long> userIds = new HashSet<>();

        while (rs.next())
            userIds.add(rs.getLong(1));

        // close the resultset
        rs.close();

        prep1.close();
        assert(prep1.isClosed());

        // delete all the users that have no remaining user-subscriptions.
        for (Long l : userIds)
        {
            String delQuery =
                    "DELETE FROM User " +
                    "WHERE id = ?;";

            PreparedStatement delPrep = SqlDatabase.getStatement(delQuery);
            delPrep.setLong(1, l);

            SqlDatabase.execute(delPrep);

            assert(delPrep.isClosed());
        }
    }

    /**
     * Clear all permissions that rely on the specified taglist.
     * @param t
     */
    private synchronized static void dbClearTrackerPermissions(Taglist t) throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        // retrieve all trackers
        ArrayList<Tracker> trackers = new ArrayList<>(TrackerHandler.handler().getAll());

        for (Tracker tracker : trackers)
        {
            // if the taglist had to be removed from the specified tracker, update it in the system.
            if (tracker.removeTaglistDependency(t))
                TrackerHandler.handler().set(tracker);
        }
    }

    /**
     * Clear all actives pertaining to the specified taglist.
     * @param t
     */
    private synchronized static void dbClearArchiveDependencies(Taglist t) throws SQLException
    {
        String query =
                "DELETE FROM Archive " +
                "WHERE taglistId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, t.getId());
        SqlDatabase.execute(prep);

        assert(prep.isClosed());
    }


    //endregion


    private synchronized static ArrayList<Taglist> dbGetTaglists() throws SQLException
    {
        String query =
                "SELECT id, abbreviation, description, hasRatings " +
                "FROM Taglist;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return
        ArrayList<Taglist> parseResult = parseTaglists(rs);

        prep.close();
        assert(prep.isClosed());

        return parseResult;
    }

    private synchronized static Taglist dbGetTaglist(String abbreviation) throws SQLException
    {
        String query =
                "SELECT id, abbreviation, description, hasRatings " +
                "FROM Taglist " +
                "WHERE UPPER(abbreviation) = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, abbreviation.toUpperCase());
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the result and return
        ArrayList<Taglist> parseResult = parseTaglists(rs);

        prep.close();
        assert(prep.isClosed());

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

        // close the resultset
        rs.close();

        return taglists;
    }

    //endregion


    //region Helper methods

    /**
     * Retrieve the taglist that corresponds to the id.
     * @param taglistId
     * @return
     */
    public static synchronized Taglist getTaglistById(long taglistId) throws SQLException
    {
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
    public static synchronized Taglist getTaglistByAbbreviation(String abbreviation) throws SQLException
    {
        if (abbreviation == null || abbreviation.isEmpty())
            return null;

        return dbGetTaglist(abbreviation);
    }


    //endregion



}
