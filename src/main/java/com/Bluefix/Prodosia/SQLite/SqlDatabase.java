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

package com.Bluefix.Prodosia.SQLite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlDatabase
{
    //region Variables

    /**
     * The current expected version of the database.
     */
    public static final int DatabaseVersion = 1;

    private Connection conn;

    //endregion

    //region Singleton and constructor

    private SqlDatabase()
    {
        try
        {
            connect();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private static SqlDatabase myDatabase;

    public static SqlDatabase Database()
    {
        if (myDatabase == null)
        {
            myDatabase = new SqlDatabase();
        }

        return myDatabase;
    }

    //endregion

    //region Connect to Database

    private void connect() throws SQLException
    {
        String url = "jdbc:sqlite:database.db";

        conn = DriverManager.getConnection(url);

        System.out.println("Database was connected");

    }

    //endregion

    //region Update the database

    /**
     * Generate the database from the latest design.
     */
    private void createDatabase()
    {

    }

    /**
     * Update the database, based on its current version.
     */
    private void updateDatabase()
    {

    }


    //endregion



}
