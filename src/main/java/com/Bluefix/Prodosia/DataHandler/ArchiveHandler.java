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

import com.Bluefix.Prodosia.DataType.Archive.Archive;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
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

/**
 * Handler class for archives. This stores and retrieves archive-setups
 * to and from the database.
 */
public class ArchiveHandler extends LocalStorageHandler<Archive>
{
    //region Singleton and Constructor

    private static ArchiveHandler me;

    public synchronized static ArchiveHandler handler()
    {
        if (me == null)
            me = new ArchiveHandler();

        return me;
    }

    private ArchiveHandler()
    {
        super(true);
    }

    //endregion



    @Override
    public void set(Archive archive) throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        super.set(archive);
        GuiUpdate.updateArchives();
    }

    @Override
    public void remove(Archive archive) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        super.remove(archive);
        GuiUpdate.updateArchives();
    }


    /**
     * Remove an item from the storage.
     *
     * @param archive
     */
    @Override
    protected void removeItem(Archive archive) throws SQLException
    {
        dbRemoveArchive(archive);
    }

    /**
     * Set the item, replacing the existing item if applicable.
     *
     * @param archive
     * @return The old item that was replaced, or null if no item had to be replaced.
     * @throws Exception
     */
    @Override
    protected Archive setItem(Archive archive) throws SQLException
    {
        return dbSetArchive(archive);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<Archive> getAllItems() throws SQLException
    {
        return dbGetArchives();
    }

    //region Database

    /**
     * Set the archive in the database.
     *
     * This method prevents duplicate archives from being stored, but will allow for
     * several archives per channel.
     * @param archive
     * @return
     */
    private static synchronized Archive dbSetArchive(Archive archive) throws SQLException
    {
        if (archive == null)
            return null;

        // retrieve the old archive (if applicable)
        Archive oldArchive = dbGetArchive(archive.getTaglist().getId(), archive.getChannelId());

        // complete the old archive and replace it.
        dbRemoveArchive(oldArchive);

        // insert the archive data.
        String query =
                "INSERT INTO Archive " +
                "(taglistId, description, channel, ratings, filters) " +
                "VALUES (?,?,?,?,?);";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, archive.getTaglist().getId());
        prep.setString(2, archive.getDescription());
        prep.setString(3, archive.getChannelId());
        prep.setString(4, archive.dbGetRatings());
        prep.setString(5, archive.getFilters());
        SqlDatabase.execute(prep);

        // return the old archive.
        return oldArchive;
    }



    private static synchronized void dbRemoveArchive(Archive archive) throws SQLException
    {
        if (archive == null)
            return;

        // if the data fits, complete the taglist.
        String query =
                "DELETE FROM Archive " +
                "WHERE taglistId = ? AND " +
                        "channel = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, archive.getTaglist().getId());
        prep.setString(2, archive.getChannelId());

        SqlDatabase.execute(prep);
    }

    private static synchronized ArrayList<Archive> dbGetArchives() throws SQLException
    {
        String query =
                "SELECT taglistId, description, channel, ratings, filters " +
                "FROM Archive;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the archive and return.
        return parseArchives(rs);
    }

    /**
     * Retrieve the archive for a specific taglist id and channel id.
     * @param taglistId
     * @param channelId
     * @return
     */
    private static synchronized Archive dbGetArchive(long taglistId, String channelId) throws SQLException
    {
        String query =
                "SELECT taglistId, description, channel, ratings, filters " +
                "FROM Archive " +
                "WHERE taglistId = ? AND channel = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, taglistId);
        prep.setString(2, channelId);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the archive and return.
        ArrayList<Archive> parsedResult = parseArchives(rs);

        if (parsedResult.isEmpty())
            return null;

        return parsedResult.get(0);
    }



    private static ArrayList<Archive> parseArchives(ResultSet rs) throws SQLException
    {
        ArrayList<Archive> archives = new ArrayList<>();

        while (rs.next())
        {
            long taglistId = rs.getLong(1);
            String description = rs.getString(2);
            String channelId = rs.getString(3);
            String ratings = rs.getString(4);
            String filters = rs.getString(5);

            archives.add(new Archive(taglistId, description, channelId, ratings, filters));
        }

        return archives;
    }

    //endregion

    //region Helper methods

    /**
     * Retrieve all archives that correspond to the given taglist.
     * @param t The specified taglist.
     * @return An arraylist with the archives that correspond to the taglist.
     */
    public static ArrayList<Archive> getArchivesForTaglist(Taglist t) throws SQLException
    {
        ArrayList<Archive> archives = new ArrayList<>(handler().getAll());

        archives.removeIf(a ->
                !a.getTaglist().equals(t));

        return archives;
    }

    //endregion
}
