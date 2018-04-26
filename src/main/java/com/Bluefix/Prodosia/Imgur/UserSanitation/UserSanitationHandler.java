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

package com.Bluefix.Prodosia.Imgur.UserSanitation;

import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Helper class for the User Sanitation module.
 *
 * This class will store the Imgur-id's of users in a
 * queue.
 */
public class UserSanitationHandler
{


    /**
     * Fetch a number of users from the queue and remove them.
     * This method will automatically refresh the queue if it emptied.
     * @param amount
     * @return
     */
    public static HashSet<Long> fetchUsers(int amount) throws SQLException
    {
        HashSet<Long> entries = getEntries(amount);

        // if there weren't enough entries to fill the entire amount, reset the queue.
        if (entries.size() < amount)
        {
            // we can exclude the entries we were already going to process.
            initializeQueue(entries);

            entries.addAll(getEntries(amount - entries.size()));
        }

        return entries;
    }


    /**
     * Retrieve as many entries from the queue as were still available,
     * to a maximum of the amount.
     * @param amount
     * @return
     */
    private static HashSet<Long> getEntries(int amount) throws SQLException
    {
        HashSet<Long> entries = new HashSet<>();

        String query =
                "SELECT imgurId FROM UserSanitation LIMIT ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setInt(1, amount);

        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        while (rs.next())
            entries.add(rs.getLong(1));

        // close the resultset
        rs.close();
        prep.close();

        return entries;
    }


    /**
     * Initialize the queue from the current user entries.
     * @param exclude the Imgur-ids to exclude.
     */
    private static void initializeQueue(HashSet<Long> exclude) throws SQLException
    {
        // initialize a HashSet with all the Imgur-ids from the existing users.
        HashSet<Long> imgurIds = new HashSet<>();

        ArrayList<User> users = UserHandler.handler().getAll();

        for (User u : users)
        {
            if (exclude.contains(u.getImgurId()))
                continue;

            imgurIds.add(u.getImgurId());
        }

        // add all items to the database.
        for (Long l : imgurIds)
        {
            String query =
                    "INSERT INTO UserSanitation (imgurId) VALUES (?);";

            PreparedStatement prep = SqlDatabase.getStatement(query);
            prep.setLong(1, l);
            SqlDatabase.execute(prep);
            prep.close();
        }
    }

}

















