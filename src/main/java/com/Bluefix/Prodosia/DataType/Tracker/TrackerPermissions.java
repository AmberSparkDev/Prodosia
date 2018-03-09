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

package com.Bluefix.Prodosia.DataType.Tracker;

import java.util.ArrayList;
import java.util.Arrays;
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
    private String[] taglists;

    /**
     * The type of tracker.
     */
    private TrackerType type;


    /**
     * Create a new TrackerPermissions object
     * @param type The type of tracker
     * @param taglists The taglists pertaining to this tracker, if applicable.
     */
    public TrackerPermissions(TrackerType type, String... taglists)
    {
        this.type = type;
        this.taglists = taglists;
    }


    /**
     * Read a taglist from the database, instantiating it.
     * @param type The type of tracker
     * @param taglists The taglists, split by separators.
     */
    public TrackerPermissions(int type, String taglists) throws Exception
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
                throw new Exception("The type was not recognized");
        }

        String[] split = taglists.split(" ; ");

        for (int i = 0; i < split.length; i++)
        {
            split[i] = split[i].replace(";;", ";");
        }

        this.taglists = split;
    }


    //endregion

    //region Getters

    public String[] getTaglists()
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

    public String dbGetTaglists()
    {
        StringBuilder sb = new StringBuilder();

        for (String s : taglists)
        {
            sb.append(s.replace(";", ";;") + " ; ");
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
        return Arrays.equals(taglists, that.taglists) &&
                type == that.type;
    }

    @Override
    public int hashCode()
    {

        int result = Objects.hash(type);
        result = 31 * result + Arrays.hashCode(taglists);
        return result;
    }


    //endregion

}
