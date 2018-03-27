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

package com.Bluefix.Prodosia.SQLite;

import com.Bluefix.Prodosia.DataHandler.TrackerHandler;

import java.sql.*;
import java.util.ArrayList;

public class SqlDatabase
{
    //region Variables

    /**
     * The current expected version of the database.
     */
    public static final int DatabaseVersion = 1;
    public static final String CreatedBy =
            "Bluefix Development";

    private Connection conn;

    //endregion

    //region Singleton and constructor

    private SqlDatabase()
    {

    }

    private static SqlDatabase myDatabase;

    public static synchronized SqlDatabase Database()
    {
        if (myDatabase == null)
        {
            try
            {
                myDatabase = new SqlDatabase();
                myDatabase.connect();
                myDatabase.conn.setAutoCommit(false);
                myDatabase.updateDatabase();

            } catch (SQLException e)
            {
                e.printStackTrace();
            }
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
    private void createDatabase() throws SQLException
    {
        // create the database and set the Version.
        String[] tableQueries = SqlStatement.createDatabaseStatement();

        for (String q : tableQueries)
        {
            myDatabase.conn.prepareStatement(q).execute();
        }

        SqlDatabaseHelper.setVersion(DatabaseVersion, CreatedBy);
    }


    /**
     * Update the database, based on its current version.
     */
    private void updateDatabase()
    {
        // first check the version of the database.
        try
        {
            // check if the 'Info' table exists.
            if (!SqlDatabaseHelper.tableExists( "Info"))
            {
                createDatabase();
                return;
            }

            // On subsequent updates, compare the version to the current version and apply the update.
            int dbVersion = SqlDatabaseHelper.getVersion();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //endregion

    //region Execute queries

    /**
     * Execute (several) query statements in succession.
     * @param statement The statement to be executed.
     * @return null on query failure, otherwise a dataset for each individual query
     * @throws SQLException SQL exception, indicative of an erroneous query.
     */
    public synchronized static ArrayList<ResultSet> query(PreparedStatement... statement) throws SQLException
    {
        return commitAll(SqlBuilder.QueryType.QUERY, statement);
    }

    /**
     * Execute (several) execution statements in succession.
     * @param statement The statement to be executed.
     * @return null on query failure, otherwise a boolean result for each query, indicating success.
     * @throws SQLException SQL exception, indicative of an erroneous query.
     */
    public synchronized static ArrayList<Boolean> execute(PreparedStatement... statement) throws SQLException
    {
        return commitAll(SqlBuilder.QueryType.EXECUTE, statement);
    }

    /**
     * Execute (several) update statements in succession.
     * @param statement The statement to be executed.
     * @return null on query failure, otherwise an integer for each individual query, indicating the amount of rows changed.
     * @throws SQLException SQL exception, indicative of an erroneous query.
     */
    public synchronized static ArrayList<Integer> update(PreparedStatement... statement) throws SQLException
    {
        return commitAll(SqlBuilder.QueryType.UPDATE, statement);
    }


    /**
     * Execute the prepared statements according to the same query type.
     * @param type The type of all statements.
     * @param statement The statements to be executed.
     * @param <T> The expected return-values
     * @return An arraylist with the expected return values.
     * @throws SQLException SQL exception, indicative of an erroneous query.
     */
    private static <T> ArrayList<T> commitAll(SqlBuilder.QueryType type, PreparedStatement... statement) throws SQLException
    {
        SqlBuilder.StoredQuery[] queries = new SqlBuilder.StoredQuery[statement.length];

        for (int i = 0; i < queries.length; i++)
        {
            queries[i] = new SqlBuilder.StoredQuery(statement[i], type);
        }

        return Commit(queries);
    }

    /**
     * Execute all queued queries. Will roll back if any are erroneous.
     * @param query The queries to be executed.
     * @param <T> The expected return type. Use 'Object' if there are mixed kinds of queries.
     * @return null on query failure, otherwise the dataset of results based on the queries executed.
     * @throws SQLException SQL exception, indicative of an erroneous query.
     */
    public synchronized static <T extends Object> ArrayList<T> Commit(SqlBuilder.StoredQuery... query) throws SQLException
    {
        // init an arraylist with the proper size.
        ArrayList<T> out = new ArrayList<T>(query.length);

        try
        {
            // attempt to execute the queries in order.
            for (int i = 0; i < query.length; i++)
            {
                switch (query[i].type)
                {
                    case EXECUTE:
                        out.add(i, (T) Boolean.valueOf(query[i].statement.execute()));
                        break;
                    case QUERY:
                        out.add(i, (T) query[i].statement.executeQuery());
                        break;
                    case UPDATE:
                        out.add(i, (T) Integer.valueOf(query[i].statement.executeUpdate()));
                        break;
                    default:
                        throw new IllegalArgumentException("The type type was not recognized.");
                }
            }

            // commit the queries and return the values.
            Database().conn.commit();

        }
        catch (SQLException e)
        {
            // print stacktrace.
            e.printStackTrace();

            // rollback the changes.
            Database().conn.rollback();

            // return null to indicate failure
            return null;
        }


        return out;
    }




    /**
     * Generate a prepared statement from the sql type.
     * @param sql The sql query
     * @return A prepared SQL Statement.
     * @throws SQLException If the SQL statement is erroneous.
     */
    public static PreparedStatement getStatement(String sql) throws SQLException
    {
        return Database().conn.prepareStatement(sql);
    }

    //endregion


}
