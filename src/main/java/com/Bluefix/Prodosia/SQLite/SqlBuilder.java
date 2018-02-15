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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A class that can help in executing several queries
 */
public class SqlBuilder
{
    //region Data and variables

    public enum QueryType
    {
        EXECUTE,
        QUERY,
        UPDATE
    }

    /**
     * A PreparedStatement together with an indication of which method
     * should be used to call it.
     */
    public static class StoredQuery
    {
        public PreparedStatement statement;
        public QueryType type;

        public StoredQuery(PreparedStatement statement, QueryType type)
        {
            this.statement = statement;
            this.type = type;
        }
    }

    private ArrayList<StoredQuery> queue;

    //endregion

    //region Constructor and initial builder

    private SqlBuilder()
    {
        queue = new ArrayList<>();
    }



    public static SqlBuilder Builder()
    {
        return new SqlBuilder();
    }

    //endregion

    //region builder methods

    /**
     * Commit all the statements to the database.
     * @return an array with the results of the individual queries.
     */
    public ArrayList<Object> commit() throws SQLException
    {
        StoredQuery[] array = new StoredQuery[queue.size()];

        queue.toArray(array);

        return SqlDatabase.Commit(array);
    }

    /**
     * Append a query-statement to the end of the queue.
     * @param statement The SQL statement.
     * @return The builder object.
     */
    public SqlBuilder query(PreparedStatement statement)
    {
        queue.add(new StoredQuery(statement, QueryType.QUERY));
        return this;
    }

    /**
     * Append an execution statement to the end of the queue.
     * @param statement The SQL statement.
     * @return The builder object.
     */
    public SqlBuilder execute(PreparedStatement statement)
    {
        queue.add(new StoredQuery(statement, QueryType.EXECUTE));
        return this;
    }

    /**
     * Append an update statement to the end of the queue.
     * @param statement The SQL statement.
     * @return The builder object.
     */
    public SqlBuilder update(PreparedStatement statement)
    {
        queue.add(new StoredQuery(statement, QueryType.UPDATE));
        return this;
    }

    //endregion



}
