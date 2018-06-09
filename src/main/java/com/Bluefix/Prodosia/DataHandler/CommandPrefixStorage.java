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

import com.Bluefix.Prodosia.Prefix.CommandPrefix;
import com.Bluefix.Prodosia.SQLite.SqlDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CommandPrefixStorage extends LocalStorageHandler<CommandPrefix>
{
    //region Singleton and Constructor

    private static CommandPrefixStorage me;

    public static CommandPrefixStorage handler()
    {
        if (me == null)
            me = new CommandPrefixStorage();

        return me;
    }

    private CommandPrefixStorage()
    {
        super(true);
    }

    //endregion



    //region Local Storage Handler implementation

    /**
     * Retrieve the prepared statements necessary for adding an item.
     *
     * @param commandPrefix
     */
    @Override
    CommandPrefix setItem(CommandPrefix commandPrefix) throws SQLException
    {
        return dbSetCPrefix(commandPrefix);
    }





    /**
     * Remove an item from the storage.
     *
     * @param commandPrefix
     */
    @Override
    void removeItem(CommandPrefix commandPrefix) throws SQLException
    {
        dbRemoveCPrefix(commandPrefix);
    }

    /**
     * Retrieve all items from the storage in no particular order.
     *
     * @return
     */
    @Override
    ArrayList<CommandPrefix> getAllItems() throws SQLException
    {
        return dbGetCPrefixes();
    }

    //endregion

    //region Database management

    private static CommandPrefix dbSetCPrefix(CommandPrefix cp) throws SQLException
    {
        // skip if the command prefix is null
        if (cp == null)
            return null;

        // retrieve the old prefix item.
        CommandPrefix oldPrefix = dbGetCPrefix(cp.getType());

        // complete the old prefix if it existed.
        dbRemoveCPrefix(oldPrefix);

        String query =
                "INSERT INTO CommandPrefix " +
                "(type, regex) VALUES (?,?);";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setInt(1, cp.getType().getValue());
        prep.setString(2, cp.getRegex());

        SqlDatabase.execute(prep);

        assert(prep.isClosed());

        return oldPrefix;
    }

    private static void dbRemoveCPrefix(CommandPrefix cp) throws SQLException
    {
        // skip if the command prefix is null
        if (cp == null)
            return;

        String query =
                "DELETE FROM CommandPrefix " +
                "WHERE type = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setInt(1, cp.getType().getValue());

        SqlDatabase.execute(prep);

        assert(prep.isClosed());
    }

    private static CommandPrefix dbGetCPrefix(CommandPrefix.Type type) throws SQLException
    {
        String query =
                "SELECT type, regex " +
                "FROM CommandPrefix " +
                "WHERE type = ?;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        prep.setInt(1, type.getValue());
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        // parse the query and return the result
        ArrayList<CommandPrefix> parseResult = parsePrefixes(rs);

        prep.close();
        assert(prep.isClosed());

        if (parseResult.isEmpty())
            return null;

        return parseResult.get(0);
    }

    private static ArrayList<CommandPrefix> dbGetCPrefixes() throws SQLException
    {
        String query =
                "SELECT type, regex " +
                "FROM CommandPrefix;";

        PreparedStatement prep = SqlDatabase.getStatement(query);
        ArrayList<ResultSet> result = SqlDatabase.query(prep);

        if (result.size() != 1)
            throw new SQLException("SqlDatabase exception: Expected result size did not match (was " + result.size() + ")");

        ResultSet rs = result.get(0);

        ArrayList<CommandPrefix> parseResult = parsePrefixes(rs);

        prep.close();
        assert(prep.isClosed());

        return parseResult;
    }

    private static ArrayList<CommandPrefix> parsePrefixes(ResultSet rs) throws SQLException
    {
        ArrayList<CommandPrefix> output = new ArrayList<>();

        while (rs.next())
        {
            CommandPrefix.Type type = CommandPrefix.Type.parseType(rs.getInt(1));
            String regex = rs.getString(2);

            output.add(new CommandPrefix(type, regex));
        }

        // close the resultset.
        rs.close();

        return output;
    }


    //endregion

    //region Helper methods

    /**
     * Get the command-prefix for the specified type, or null if it did not exist.
     * @param type The type for which to fetch the command-prefix.
     * @return The commandprefix corresponding to the type if it existed, or null otherwise.
     * @throws Exception
     */
    public static CommandPrefix getPrefixForType(CommandPrefix.Type type) throws SQLException
    {
        ArrayList<CommandPrefix> items = handler().getAll();

        for (CommandPrefix i : items)
        {
            if (i.getType() == type)
                return i;
        }

        return null;
    }

    //endregion
}
