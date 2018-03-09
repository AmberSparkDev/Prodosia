/*
 * Copyright (c) 2018 Bas Boellaard
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


import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import com.Bluefix.Prodosia.SQLite.SqlBuilder;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Handler for Tracker management.
 *
 * This handles the storage and retrieval of data from the database and keeps
 * a local memory copy for speed purposes.
 */
public class TrackerHandler
{
    //region variables

    /**
     * Local copy of the available trackers.
     */
    private ArrayList<Tracker> trackers;

    //endregion

    //region Singleton and Constructor

    private static TrackerHandler me;

    private static TrackerHandler handler()
    {
        if (me == null)
        {
            me = new TrackerHandler();
        }

        return me;
    }

    private TrackerHandler()
    {
        trackers = null;
    }


    //endregion

    //region Static visible methods

    /**
     * Add a tracker to the collection.
     * @param tracker the tracker to be added.
     * @throws SQLException
     */
    public synchronized static void addTracker(Tracker tracker) throws Exception
    {
        if (tracker == null)
            return;

        // remove the tracker if it already existed
        removeTracker(tracker);

        handler().trackers.add(tracker);
        dbAddTracker(tracker);
    }

    /**
     * Remove the tracker from the collection.
     * @param tracker The tracker to be removed.
     * @throws SQLException
     */
    public synchronized static void removeTracker(Tracker tracker) throws Exception
    {
        if (tracker == null)
            return;

        // remove the tracker from the list and database.
        handler().trackers.remove(tracker);
        dbRemoveTracker(tracker);
    }

    /**
     * Update the old tracker, replacing it with the new one.
     * @param oldTracker
     * @param newTracker
     * @throws SQLException
     */
    public synchronized static void updateTracker(Tracker oldTracker, Tracker newTracker) throws Exception
    {
        // remove the old tracker and add the new one.
        removeTracker(oldTracker);
        addTracker(newTracker);
    }

    /**
     * Retrieve all trackers in the system.
     * @return A list with all current trackers.
     */
    public synchronized static ArrayList<Tracker> getTrackers() throws Exception
    {
        if (handler().trackers == null)
        {
            handler().trackers = dbGetTrackers();
        }

        return handler().trackers;
    }

    //endregion


    //region Database management

    private synchronized static void dbAddTracker(Tracker t) throws Exception
    {
        if (t == null)
            return;

        // first insert the tracker
        String query0 =
                "INSERT INTO Tracker " +
                "(imgurId, imgurName, discordId, discordName, discordTag) " +
                "VALUES (?,?,?,?,?);";

        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        prep0.setLong(1, t.getImgurId());
        prep0.setString(2, t.getImgurName());
        prep0.setLong(3, t.getDiscordId());
        prep0.setString(4, t.getDiscordName());
        prep0.setInt(5, t.getDiscordTag());

        // retrieve the rowid of the freshly inserted tracker
        String query1 = "SELECT last_insert_rowid()";
        PreparedStatement prep1 = SqlDatabase.getStatement(query1);

        ArrayList<Object> result = SqlBuilder.Builder()
                .execute(prep0)
                .query(prep1)
                .commit();

        if (result == null)
            throw new Exception("SqlDatabase exception: query result was null");

        if (result.size() != 2)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = (ResultSet)result.get(1);

        if (!rs.next())
            throw new Exception("SqlDatabase exception: The database did not provide a row-id for the inserted tracker");

        int trackerIndex = rs.getInt(1);

        // next, insert its permissions.
        String query2 =
                "INSERT INTO Permission " +
                "(trackerId, isAdmin, taglists) " +
                "VALUES (?,?,?);";

        PreparedStatement prep2 = SqlDatabase.getStatement(query2);
        prep2.setLong(1, trackerIndex);
        prep2.setInt(2, t.getPermissions().dbGetType());
        prep2.setString(3, t.getPermissions().dbGetTaglists());

        SqlDatabase.execute(prep2);
    }

    private synchronized static void dbRemoveTracker(Tracker t) throws Exception
    {
        // first, retrieve the id for the tracker.
        String query0 =
                "SELECT id " +
                "FROM Tracker as T " +
                "WHERE imgurId = ? " +
                    "AND discordId = ?;";

        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        prep0.setLong(1, t.getImgurId());
        prep0.setLong(2, t.getDiscordId());

        ArrayList<ResultSet> result = SqlDatabase.query(prep0);

        // if the database does not contain the tracker, quietly exit.
        if (result == null)
            return;

        if (result.size() != 1)
            return;

        ResultSet rs = result.get(0);

        if (!rs.next())
            return;

        long trackerId = rs.getLong(1);

        // delete the entry from Tracker and Permission
        String query1 =
                "DELETE FROM Tracker " +
                "WHERE id = ?;";
        PreparedStatement prep1 = SqlDatabase.getStatement(query1);
        prep1.setLong(1, trackerId);

        String query2 =
                "DELETE FROM Permission " +
                "WHERE trackerId = ?;";
        PreparedStatement prep2 = SqlDatabase.getStatement(query2);
        prep2.setLong(1, trackerId);

        SqlBuilder.Builder()
                .execute(prep1)
                .execute(prep2)
                .commit();
    }

    private synchronized static ArrayList<Tracker> dbGetTrackers() throws Exception
    {
        String query =
                "SELECT " +
                    "T.imgurId, " +
                    "T.imgurName, " +
                    "T.discordId, " +
                    "T.discordName, " +
                    "T.discordTag, " +
                    "P.isAdmin, " +
                    "P.taglists " +
                "FROM Tracker as T " +
                "INNER JOIN Permission as P ON T.id = P.trackerId;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);
        ArrayList<Tracker> trackers = new ArrayList<>();

        while (rs.next())
        {
            long imgurId = rs.getLong(1);
            String imgurName = rs.getString(2);
            long discordId = rs.getLong(3);
            String discordName = rs.getString(4);
            int discordTag = rs.getInt(5);
            int permType = rs.getInt(6);
            String permTaglists = rs.getString(7);

            // initiate the permissions and create the tagger
            TrackerPermissions perm = new TrackerPermissions(permType, permTaglists);

            Tracker myT = new Tracker(imgurName, imgurId, discordName, discordTag, discordId, perm);

            trackers.add(myT);
        }

        return trackers;
    }





    //endregion
}
