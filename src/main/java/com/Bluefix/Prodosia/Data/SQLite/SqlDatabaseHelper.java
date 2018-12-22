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

package com.Bluefix.Prodosia.Data.SQLite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class that helps with initiating the database.
 */
public class SqlDatabaseHelper
{

    //region Version

    /**
     * Retrieve the database version.
     * @return
     */
    public static int getVersion() throws SQLException
    {
        // execute the type and retrieve the resultset.
        String sql =
                "SELECT version FROM Info LIMIT 1;";

        PreparedStatement s = SqlDatabase.getStatement(sql);
        ResultSet rs = SqlDatabase.query(s).get(0);

        int value = -1;

        if (rs.next())
        {
            value = rs.getInt(1);
        }

        // close the ResultSet
        rs.close();
        s.close();

        return value;
    }

    public synchronized static void setVersion(int version, String createdBy) throws SQLException
    {

        String sqlDel =
                "DELETE FROM Info;";
        String sqlInsert =
                "INSERT INTO Info (version, created_by) " +
                        "VALUES (?, ?); ";

        try
        {
            // prepare the queries
            PreparedStatement s0 = SqlDatabase.getStatement(sqlDel);
            PreparedStatement s1 = SqlDatabase.getStatement(sqlInsert);

            s1.setInt(1, version);
            s1.setString(2, createdBy);

            // build the query.
            SqlDatabase.execute(s0, s1);

            s0.close();
            s1.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    //endregion




    public static boolean tableExists(String table) throws SQLException
    {
        String sql =
                "SELECT name FROM sqlite_master " +
                "WHERE type='table' AND name=?;";


        PreparedStatement s0 = SqlDatabase.getStatement(sql);
        s0.setString(1, table);

        ResultSet set = SqlDatabase.query(s0).get(0);

        boolean value = set.next();

        // close the resultset
        set.close();
        s0.close();

        return value;
    }



}
