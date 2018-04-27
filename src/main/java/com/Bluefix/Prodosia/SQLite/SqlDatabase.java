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

import com.Bluefix.Prodosia.Prefix.CommandPrefix;
import com.Bluefix.Prodosia.DataHandler.CommandPrefixStorage;

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
            PreparedStatement prep = myDatabase.conn.prepareStatement(q);
            prep.execute();
            prep.close();

            myDatabase.conn.commit();

            assert(prep.isClosed());
        }

        SqlDatabaseHelper.setVersion(DatabaseVersion, CreatedBy);

        // initialize the default CommandPrefixes
        try
        {
            createDefaultPrefix();
        } catch (Exception e)
        {
            // exception is annoying but not detrimental to application functionality
            e.printStackTrace();
        }
    }

    /**
     * Initialize the default prefixes.
     */
    private void createDefaultPrefix() throws Exception
    {
        // Imgur
        // default prefix is: @Tagaroo
        String pattern = CommandPrefix.parsePatternForItems("@Tagaroo ");
        CommandPrefix imgCp = new CommandPrefix(CommandPrefix.Type.IMGUR, pattern);
        CommandPrefixStorage.handler().set(imgCp);

        // Discord
        // Discord command prefix is automatically initialized when discord
        // is connected, no need for us to worry about it.
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
            if (!SqlDatabaseHelper.tableExists("Info"))
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

        // attempt to execute the queries in order.
        for (int i = 0; i < query.length; i++)
        {
            try
            {
                switch (query[i].type)
                {
                    case EXECUTE:
                        out.add(i, (T) Boolean.valueOf(query[i].statement.execute()));
                        break;
                    case QUERY:
                        query[i].statement.closeOnCompletion();
                        out.add(i, (T) query[i].statement.executeQuery());
                        break;
                    case UPDATE:
                        out.add(i, (T) Integer.valueOf(query[i].statement.executeUpdate()));
                        break;
                    default:
                        throw new IllegalArgumentException("The query type was not recognized.");
                }



            } catch (SQLException e)
            {
                // print stacktrace.
                e.printStackTrace();

                // rollback the changes.
                Database().conn.rollback();

                // close all the statements.
                for (int j = 0; j < query.length; j++)
                {
                    try
                    {
                        query[j].statement.close();
                    } catch (SQLException ex)
                    {
                        // ignore
                    }
                }


                // return null to indicate failure
                return null;
            }
        }

        // close all the statements.
        for (int i = 0; i < query.length; i++)
        {
            try
            {
                if (query[i].type != SqlBuilder.QueryType.QUERY)
                    query[i].statement.close();
            } catch (SQLException ex)
            {
                // ignore
            }
        }

        // commit the queries
        Database().conn.commit();

        // return the results.
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

    //region Helper method

    /**
     * Execute a Prepared Statement and retrieve the affected row id.
     * @param prep The prepared statement to be executed.
     * @return The row-id that was affected by this execution.
     */
    public static long getAffectedRow(PreparedStatement prep) throws SQLException
    {
        String query = "SELECT last_insert_rowid();";
        PreparedStatement myPrep = getStatement(query);

        ArrayList<Object> result = SqlBuilder.Builder()
                .execute(prep)
                .query(myPrep)
                .commit();

        if (result == null)
            throw new SQLException("SqlDatabase exception: query result was null");

        if (result.size() != 2)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = (ResultSet)result.get(1);

        if (!rs.next())
            throw new SQLException("SqlDatabase exception: The database did not provide a row-id for the inserted tracker");

        long value = rs.getLong(1);

        // close the result-set
        rs.close();
        myPrep.close();

        assert(myPrep.isClosed());


        return value;
    }

    //endregion








}
