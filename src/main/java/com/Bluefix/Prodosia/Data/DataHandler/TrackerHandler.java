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

package com.Bluefix.Prodosia.Data.DataHandler;


import com.Bluefix.Prodosia.Data.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Data.DataType.Tracker.TrackerPermissions;
import com.Bluefix.Prodosia.Presentation.GuiUpdate;
import com.Bluefix.Prodosia.Data.SQLite.SqlBuilder;
import com.Bluefix.Prodosia.Data.SQLite.SqlDatabase;
import com.github.kskelm.baringo.util.BaringoApiException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
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
public class TrackerHandler extends LocalStorageHandler<Tracker>
{
    //region Singleton and Constructor

    private static TrackerHandler me;

    /**
     * Retrieve the TrackerHandler object.
     * @return The TrackerHandler object.
     */
    public static TrackerHandler handler()
    {
        if (me == null)
            me = new TrackerHandler();

        return me;
    }

    private TrackerHandler()
    {
        super(true);
    }

    //endregion


    //region Local Storage Handler implementation

    @Override
    public void set(Tracker t) throws URISyntaxException, SQLException, IOException, LoginException, BaringoApiException
    {
        super.set(t);
        GuiUpdate.updateTrackers();
    }

    @Override
    public void remove(Tracker t) throws URISyntaxException, SQLException, IOException, BaringoApiException
    {
        super.remove(t);
        GuiUpdate.updateTrackers();
    }

    @Override
    Tracker setItem(Tracker t) throws SQLException
    {
        return dbSetTracker(t);
    }

    @Override
    void removeItem(Tracker t) throws SQLException
    {
        dbRemoveTracker(t);
    }

    @Override
    ArrayList<Tracker> getAllItems() throws SQLException
    {
        return dbGetTrackers();
    }

    //endregion


    //region Database management

    private static Tracker dbSetTracker(Tracker t) throws SQLException
    {
        if (t == null)
            return null;

        // retrieve the old tracker
        Tracker oldTracker = dbGetTracker(t.getImgurId(), t.getDiscordId());

        // delete the old tracker to replace it.
        dbRemoveTracker(oldTracker);

        // first insert the tracker
        String query0;

        // if there was an old tracker, its id should be used.
        if (oldTracker != null)
        {
            query0 =    "INSERT INTO Tracker " +
                        "(id, imgurId, imgurName, discordId, discordName, discordTag) " +
                        "VALUES (?,?,?,?,?,?);";
        }
        else
        {
            query0 =    "INSERT INTO Tracker " +
                        "(imgurId, imgurName, discordId, discordName, discordTag) " +
                        "VALUES (?,?,?,?,?);";
        }



        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        int argCounter = 1;

        if (oldTracker != null)
            prep0.setLong(argCounter++, oldTracker.getId());

        prep0.setLong(argCounter++, t.getImgurId());
        prep0.setString(argCounter++, t.getImgurName());
        prep0.setString(argCounter++, t.getDiscordId());
        prep0.setString(argCounter++, t.getDiscordName());
        prep0.setString(argCounter++, t.getDiscordTag());

        // retrieve the rowid of the freshly inserted tracker
        long trackerIndex;

        // if there was no old tracker, retrieve the index of the new tracker.
        if (oldTracker == null)
        {
            trackerIndex = SqlDatabase.getAffectedRow(prep0);
        }
        else
        {
            SqlDatabase.execute(prep0);
            trackerIndex = oldTracker.getId();
        }

        assert(prep0.isClosed());


        // update the tracker id accordingly.
        t.setId(trackerIndex);

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

        assert(prep2.isClosed());

        return oldTracker;
    }

    private static void dbRemoveTracker(Tracker t) throws SQLException
    {
        // if the item did not exist, skip
        if (t == null)
            return;

        // first, retrieve the id for the tracker.
        String query0 =
                "SELECT id " +
                "FROM Tracker as T " +
                "WHERE imgurId = ? " +
                    "AND discordId = ?;";

        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        prep0.setLong(1, t.getImgurId());
        prep0.setString(2, t.getDiscordId());

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

        // close the result-set.
        rs.close();

        assert(prep0.isClosed());

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

        assert(prep1.isClosed());
        assert(prep2.isClosed());
    }


    private static Tracker dbGetTracker(long imgurId, String discordId) throws SQLException
    {
        String query =
                "SELECT " +
                    "T.id, " +
                    "T.imgurId, " +
                    "T.imgurName, " +
                    "T.discordId, " +
                    "T.discordName, " +
                    "T.discordTag, " +
                    "P.isAdmin, " +
                    "P.taglists " +
                "FROM Tracker as T " +
                "INNER JOIN Permission as P ON T.id = P.trackerId " +
                "WHERE T.imgurId = ? AND T.discordId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, imgurId);
        prep.setString(2, discordId);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the tracker and return
        ArrayList<Tracker> parsedTrackers = parseTrackers(rs);

        prep.close();
        assert(prep.isClosed());

        if (parsedTrackers.isEmpty())
            return null;

        return parsedTrackers.get(0);
    }

    private static ArrayList<Tracker> dbGetTrackers() throws SQLException
    {
        String query =
                "SELECT " +
                    "T.id, " +
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
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the tracker and return
        ArrayList<Tracker> parsedTrackers = parseTrackers(rs);

        prep.close();
        assert(prep.isClosed());

        return parsedTrackers;
    }

    private static ArrayList<Tracker> parseTrackers(ResultSet rs) throws SQLException
    {
        ArrayList<Tracker> trackers = new ArrayList<>();

        while (rs.next())
        {
            long id = rs.getLong(1);
            long imgurId = rs.getLong(2);
            String imgurName = rs.getString(3);
            String discordId = rs.getString(4);
            String discordName = rs.getString(5);
            String discordTag = rs.getString(6);
            int permType = rs.getInt(7);
            String permTaglists = rs.getString(8);

            // initiate the permissions and create the tagger
            TrackerPermissions perm = new TrackerPermissions(permType, permTaglists);

            Tracker myT = new Tracker(id, imgurName, imgurId, discordName, discordTag, discordId, perm);

            trackers.add(myT);
        }

        // close the result-set
        rs.close();

        return trackers;
    }

    //endregion

    //region Helper methods

    /**
     * Retrieve the Tracker by imgur id. Returns the corresponding tracker or null if it didn't exist.
     * @param imgurId
     * @return
     */
    public static Tracker getTrackerByImgurId(long imgurId) throws SQLException
    {
        // return null on invalid imgur id.
        if (imgurId < 0)
            return null;

        ArrayList<Tracker> trackers = handler().getAll();

        for (Tracker t : trackers)
        {
            if (t.getImgurId() == imgurId)
                return t;
        }

        return null;
    }


    /**
     * Retrieve the tracker associated with the given discord id.
     *
     * Will return null if no tracker like that existed.
     * @param discordId The discord id of the user.
     * @return The tracker that corresponds with the discordId, or null otherwise.
     */
    public static Tracker getTrackerByDiscordId(String discordId) throws SQLException
    {
        // return null on invalid imgur id.
        if (discordId == null || discordId.trim().isEmpty())
            return null;

        String trimId = discordId.trim();

        ArrayList<Tracker> trackers = handler().getAll();

        for (Tracker t : trackers)
        {
            String tDid = t.getDiscordId();

            if (tDid.equals(trimId))
                return t;
        }

        return null;
    }


    //endregion










}
