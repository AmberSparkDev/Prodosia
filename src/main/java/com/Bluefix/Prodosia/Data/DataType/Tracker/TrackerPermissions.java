/*
 * Copyright (c) 2018 RoseLaLuna
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

package com.Bluefix.Prodosia.Data.DataType.Tracker;

import com.Bluefix.Prodosia.Data.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;

/**
 * Immutable Data class to keep track of tracker permissions.
 */
public class TrackerPermissions
{
    /**
     * Indicates the type of tracker. An Admin will not have any
     * limitations.
     */
    public enum TrackerType
    {
        ADMIN(0),
        TRACKER(1);

        private int value;

        private TrackerType(int value)
        {
            this.value = value;
        }

        public int getValue()
        {
            return this.value;
        }
    }

    //region variables and constructor

    /**
     * A list with taglists. These taglists indicate permissions for the tracker
     * to edit them.
     */
    private HashSet<Taglist> taglists;

    /**
     * The type of tracker.
     */
    private TrackerType type;


    /**
     * Create a new TrackerPermissions object
     * @param type The type of tracker
     * @param taglists The taglists pertaining to this tracker, if applicable.
     */
    public TrackerPermissions(TrackerType type, Taglist... taglists)
    {
        this.type = type;
        this.taglists = new HashSet<Taglist>();

        for (Taglist t : taglists)
            this.taglists.add(t);
    }


    /**
     * Read a taglist from the database, instantiating it.
     * @param type The type of tracker
     * @param taglists The taglists, split by separators.
     */
    public TrackerPermissions(int type, String taglists) throws SQLException
    {
        switch (type)
        {
            case 0:
                this.type = TrackerType.ADMIN;
                break;
            case 1:
                this.type = TrackerType.TRACKER;
                break;

            default:
                throw new IllegalArgumentException("The type was not recognized");
        }

        String[] split = taglists.split(";");
        this.taglists = new HashSet<>();

        for (String s : split)
        {
            // skip empty taglists.
            if (s == null || s.isEmpty())
                continue;

            this.taglists.add(TaglistHandler.getTaglistById(Long.parseLong(s)));
        }
    }


    //endregion

    //region Getters

    public HashSet<Taglist> getTaglists()
    {
        return taglists;
    }

    public TrackerType getType()
    {
        return type;
    }

    public int dbGetType()
    {
        return this.type.getValue();
    }

    public String dbGetTaglists() throws SQLException
    {
        StringBuilder sb = new StringBuilder();

        for (Taglist t : taglists)
        {
            sb.append(t.getId() + ";");
        }

        return sb.toString();
    }


    //endregion

    //region comparison

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrackerPermissions that = (TrackerPermissions) o;
        return Objects.equals(taglists, that.taglists) &&
                type == that.type;
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(taglists, type);
    }


    //endregion

    //region Permission handling

    /**
     * Indicate whether this Tracker has permission to edit the specified taglist.
     * @param taglist The taglist in question.
     * @return True iff the tracker may alter the taglist, false otherwise.
     */
    public boolean hasPermission(Taglist taglist)
    {
        // if we are an admin, return true.
        if (this.type == TrackerType.ADMIN)
            return true;

        // if we aren't an admin, check to see if the taglist is in our list.
        return this.taglists.contains(taglist);
    }

    //endregion


    /**
     * Removes the reference to the specified taglist if it was contained in the tracker.
     * @param t
     * @return true iff the taglist was removed, false if the taglist was not part of this tracker.
     */
    public boolean removeTaglistDependency(Taglist t)
    {
        return this.taglists.remove(t);
    }

}
