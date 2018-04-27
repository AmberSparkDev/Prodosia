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
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.GUI.GuiUpdate;
import com.Bluefix.Prodosia.SQLite.SqlBuilder;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;
import com.github.kskelm.baringo.util.BaringoApiException;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
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
        super(false);
        this.isGuiUpdate = true;
    }

    //endregion


    //region Gui Update

    private boolean isGuiUpdate;

    /**
     * Set whether the GUI should automatically update.
     * @param enable
     */
    public void enableGuiUpdate(boolean enable)
    {
        this.isGuiUpdate = enable;
    }

    //endregion

    //region Local Storage Handler implementation


    @Override
    public void set(User user) throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        super.set(user);

        if (isGuiUpdate)
            GuiUpdate.updateUsers();
    }

    @Override
    public void remove(User user) throws SQLException, BaringoApiException, IOException, URISyntaxException
    {
        super.remove(user);
        if (isGuiUpdate)
            GuiUpdate.updateUsers();
    }

    /**
     * Add an item to the storage.
     *
     * @param user
     */
    @Override
    User setItem(User user) throws SQLException
    {
        return dbSetUser(user);
    }

    /**
     * Remove an item from the storage.
     *
     * @param user
     */
    @Override
    void removeItem(User user) throws SQLException
    {
        dbRemoveUser(user);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    ArrayList<User> getAllItems() throws SQLException
    {
        return dbGetUsers();
    }

    //endregion

    //region Database management.

    private synchronized static User dbSetUser(User u) throws SQLException
    {
        // if there was no user, return null
        if (u == null)
            return null;

        User oldUser = dbGetUser(u.getImgurId());

        // if there was an old user, replace it with the new one.
        dbRemoveUser(oldUser);

        // first insert the user with its data and retrieve its user id
        String query0 =
                "INSERT INTO User " +
                "(name, imgurId) " +
                "VALUES (?,?);";

        PreparedStatement prep0 = SqlDatabase.getStatement(query0);
        prep0.setString(1, u.getImgurName());
        prep0.setLong(2, u.getImgurId());

        long userIndex = SqlDatabase.getAffectedRow(prep0);

        assert(prep0.isClosed());

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

            assert(prep2.isClosed());
        }

        return oldUser;
    }

    private synchronized static void dbRemoveUser(User u) throws SQLException
    {
        if (u == null)
            return;

        // first retrieve the user id
        long userIndex = getUserId(u);

        // complete the user from the user table and the usersubscriptions.
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

        assert(prep0.isClosed());
        assert(prep1.isClosed());
    }


    /**
     * Retrieve a user from the database based on its imgur id.
     * @param imgurId
     * @return
     */
    private synchronized static User dbGetUser(long imgurId) throws SQLException
    {
        // select the specified user with its respective usersubscription data.
        String query =
                "SELECT U.id, " +
                        "U.name, " +
                        "U.imgurId, " +
                        "US.taglistId, " +
                        "US.ratings, " +
                        "US.filters " +
                "FROM User as U " +
                "INNER JOIN UserSubscription as US ON U.id = US.userId " +
                "WHERE U.imgurId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, imgurId);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the users and return
        ArrayList<User> parsedUsers = parseUsers(rs);

        prep.close();
        assert(prep.isClosed());

        if (parsedUsers.isEmpty())
            return null;

        return parsedUsers.get(0);
    }


    /**
     * Retrieve a user from the database based on its Imgur name.
     * @param imgurName
     * @return
     */
    private synchronized static User dbGetUser(String imgurName) throws SQLException
    {
        // select the specified user with its respective usersubscription data.
        String query =
                "SELECT U.id, " +
                        "U.name, " +
                        "U.imgurId, " +
                        "US.taglistId, " +
                        "US.ratings, " +
                        "US.filters " +
                        "FROM User as U " +
                        "INNER JOIN UserSubscription as US ON U.id = US.userId " +
                        "WHERE U.name = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setString(1, imgurName);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the users and return
        ArrayList<User> parsedUsers = parseUsers(rs);

        prep.close();
        assert(prep.isClosed());

        if (parsedUsers.isEmpty())
            return null;

        return parsedUsers.get(0);
    }




    private synchronized static ArrayList<User> dbGetUsers() throws SQLException
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
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the users and return
        ArrayList<User> parsedUsers = parseUsers(rs);

        prep.close();
        assert(prep.isClosed());

        return parsedUsers;
    }

    private static ArrayList<User> parseUsers(ResultSet rs) throws SQLException
    {
        HashMap<Long, HashSet<UserSubscription>> subMap = new HashMap<>();
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
            HashSet<UserSubscription> arrSub = subMap.get(userId);

            // if it did not exist, create one and add it to the map.
            if (arrSub == null)
            {
                arrSub = new HashSet<>();
                subMap.put(userId, arrSub);
            }

            // add the subscription data to the arraylist.
            long taglistId = rs.getLong(4);
            String ratings = rs.getString(5);
            String filters = rs.getString(6);

            arrSub.add(new UserSubscription(taglistId, ratings, filters));
        }

        // close the resultset
        rs.close();


        // parse the user list
        ArrayList<User> finalList = new ArrayList<>();

        for (Long key : userMap.keySet())
        {
            // get the user data and subscription data and join them.
            DbUserData ud = userMap.get(key);
            HashSet<UserSubscription> aus = subMap.get(key);

            if (ud == null || aus == null)
            {
                // Perhaps show a warning to the user. Non-critical, but not supposed to happen and might indicate
                // database corruption.
                continue;
            }

            finalList.add(new User(ud.imgurName, ud.imgurId, aus));
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
    private synchronized static long getUserId(User u) throws SQLException
    {
        // only the imgur id needs to be unique.
        String query =
                "SELECT id FROM User WHERE imgurId = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, u.getImgurId());

        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result == null)
            throw new SQLException("SqlDatabase exception: query result was null");

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // if the user was not found, return -1
        if (!rs.next())
            return -1;

        long value = rs.getLong(1);

        // close the resultset
        rs.close();

        assert(prep.isClosed());

        return value;
    }

    //endregion


    //region Helper

    /**
     * Returns the User with the corresponding imgur id, or null if it did not exist.
     * @param imgurId The imgur id of the user.
     * @return The corresponding user if it existed, otherwise null
     * @throws Exception
     */
    public static User getUserByImgurId(long imgurId) throws SQLException
    {
        if (!handler().useLocalStorage)
        {
            return dbGetUser(imgurId);
        }

        ArrayList<User> users = handler().getAll();

        for (User u : users)
        {
            if (u.getImgurId() == imgurId)
                return u;
        }

        return null;
    }

    /**
     * Retrieve the Imgur user by its name. If the user could not be found, returns null.
     * @param name The Imgur name of the user.
     * @return The Imgur User object, or null if it could not be found.
     * @throws Exception
     */
    public static User getUserByImgurName(String name) throws SQLException
    {
        if (!handler().useLocalStorage)
        {
            return dbGetUser(name);
        }

        ArrayList<User> users = handler().getAll();

        for (User u : users)
        {
            if (u.getImgurName().equals(name))
                return u;
        }

        return null;
    }


    /**
     * This retrieves the amount of users that will not persist
     * should this taglist be deleted. This means that these users
     * only have the specified taglist as UserSubscription.
     *
     * This method does not actually remove any items.
     * @param t
     * @return
     */
    public static int amountOfUserDependencies(Taglist t) throws SQLException
    {
        if (t == null)
            return 0;

        // if the taglist has no id, it hasn't been stored and can't be permanently removed.
        if (t.getId() < 0)
            return 0;

        String query =
                "SELECT COUNT(*) " +
                        "FROM UserSubscription " +
                        "WHERE taglistId = ? AND " +
                        "userId NOT IN (" +
                        "SELECT userId " +
                        "FROM UserSubscription " +
                        "WHERE taglistId != ?);";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setLong(1, t.getId());
        prep.setLong(2, t.getId());
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        if (!rs.next())
            throw new SQLException("Sql Exception! Could not find the count.");

        int value = rs.getInt(1);

        // close the resultset
        rs.close();

        assert(prep.isClosed());

        return value;
    }

    //endregion









}
