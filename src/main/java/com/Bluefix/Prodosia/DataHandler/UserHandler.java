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


import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.SQLite.SqlBuilder;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


/**
 * Handler for user management.
 *
 * Strictly for imgur users that are part of a taglist.
 */
public class UserHandler extends LocalStorageHandler<User>
{
    //region Singleton and Constructor

    private static UserHandler me;

    /**
     * Retrieve the UserHandler object.
     * @return The UserHandler object.
     */
    public static UserHandler handler()
    {
        if (me == null)
            me = new UserHandler();

        return me;
    }

    private UserHandler()
    {
        super(true);
    }

    //endregion

    //region Local Storage Handler implementation

    /**
     * Add an item to the storage.
     *
     * @param user
     */
    @Override
    protected void addItem(User user) throws Exception
    {
        dbAddUser(user);
    }

    /**
     * Remove an item from the storage.
     *
     * @param user
     */
    @Override
    protected void removeItem(User user) throws Exception
    {
        dbRemoveUser(user);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    protected ArrayList<User> getAllItems() throws Exception
    {
        return dbGetUsers();
    }

    //endregion

    //region Database management.

    private synchronized static void dbAddUser(User u) throws Exception
    {
        if (u == null)
            return;

        // first insert the user with its data and retrieve its user id
        String query0 =
                "INSERT INTO User " +
                "(name, imgurId) " +
                "VALUES (?,?);";

        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        prep0.setString(1, u.getImgurName());
        prep0.setLong(2, u.getImgurId());

        // retrieve the rowid of the user we are about to add.
        String query1 = "SELECT last_insert_rowid();";
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

        long userIndex = rs.getLong(1);

        // now that we know the user-id, we can start inserting the user-subscriptions.
        for (UserSubscription us : u.getSubscriptions())
        {
            String query2 =
                    "INSERT INTO UserSubscription " +
                    "(userId, taglistId, ratings, filters) " +
                    "VALUES (?,?,?,?);";

            PreparedStatement prep2 = SqlDatabase.getStatement(query2);
            prep2.setLong(1, userIndex);
            prep2.setLong(2, us.getTaglist().getId());
            prep2.setString(3, us.getDbRating());
            prep2.setString(4, us.getFilters());

            SqlDatabase.execute(prep2);
        }
    }

    private synchronized static void dbRemoveUser(User u) throws Exception
    {
        // first retrieve the user id
        long userIndex = getUserId(u);

        // remove the user from the user table and the usersubscriptions.
        SqlBuilder sb = SqlBuilder.Builder();

        String query0 = "DELETE FROM User WHERE id = ?;";
        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        prep0.setLong(1, userIndex);
        sb.execute(prep0);

        String query1 = "DELETE FROM UserSubscription WHERE userId = ?;";
        PreparedStatement prep1 = SqlDatabase.getStatement(query1);
        prep1.setLong(1, userIndex);
        sb.execute(prep1);

        sb.commit();
    }

    private synchronized static ArrayList<User> dbGetUsers() throws Exception
    {
        // select all users with their respective usersubscription data.
        String query =
                "SELECT U.id, " +
                        "U.name, " +
                        "U.imgurId, " +
                        "US.taglistId, " +
                        "US.ratings, " +
                        "US.filters " +
                "FROM User as U " +
                "INNER JOIN UserSubscription as US ON U.id = US.userId;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        HashMap<Long, ArrayList<UserSubscription>> subMap = new HashMap<>();
        HashMap<Long, DbUserData> userMap = new HashMap<>();

        // parse all available users and their respective subscriptions.
        while (rs.next())
        {
            long userId = rs.getLong(1);

            // if this is the first time reading the user, parse the DbUserData
            if (!userMap.containsKey(userId))
            {
                String imgurName = rs.getString(2);
                long imgurId = rs.getLong(3);

                userMap.put(userId, new DbUserData(imgurName, imgurId));
            }

            // retrieve the Arraylist for the usersubscription if it existed.
            ArrayList<UserSubscription> arrSub = subMap.get(userId);

            // if it did not exist, create one and add it to the map.
            if (arrSub == null)
            {
                arrSub = new ArrayList<>();
                subMap.put(userId, arrSub);
            }

            // add the subscription data to the arraylist.
            long taglistId = rs.getLong(4);
            String ratings = rs.getString(5);
            String filters = rs.getString(6);

            arrSub.add(new UserSubscription(taglistId, ratings, filters));
        }


        // parse the user list
        ArrayList<User> finalList = new ArrayList<>();

        for (Long key : userMap.keySet())
        {
            // get the user data and subscription data and join them.
            DbUserData ud = userMap.get(key);
            ArrayList<UserSubscription> aus = subMap.get(key);

            if (ud == null || aus == null)
            {
                // TODO: show a warning to the user. Non-critical, but not supposed to happen and might indicate
                // database corruption.
                continue;
            }

            finalList.add(new User(ud.imgurName, ud.imgurId, aus.iterator()));
        }

        return finalList;
    }

    /**
     * Small struct to temporarily store user data while we are reading out the database.
     */
    private static class DbUserData
    {
        private String imgurName;
        private long imgurId;

        public DbUserData(String imgurName, long imgurId)
        {
            this.imgurName = imgurName;
            this.imgurId = imgurId;
        }

        public String getImgurName()
        {
            return imgurName;
        }

        public long getImgurId()
        {
            return imgurId;
        }
    }

    //endregion

    //region Database helpers

    /**
     * Retrieve the imgur id for the specified user. Will return -1 if it did not exist.
     * @param u The specified user.
     * @return The imgur id for the specified user, or -1 if it did not exist in the database.
     * @throws SQLException
     */
    private synchronized static long getUserId(User u) throws Exception
    {
        // only the imgur id needs to be unique.
        String query =
                "SELECT id FROM User WHERE imgurId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, u.getImgurId());

        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result == null)
            throw new Exception("SqlDatabase exception: query result was null");

        if (result.size() != 1)
            throw new Exception("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // if the user was not found, return -1
        if (!rs.next())
            return -1;

        return rs.getLong(1);
    }

    //endregion








}
