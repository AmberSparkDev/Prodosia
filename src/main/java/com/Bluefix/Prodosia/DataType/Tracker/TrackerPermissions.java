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
        ADMIN,
        TRACKER
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


    public TrackerPermissions(TrackerType type, String... taglists)
    {
        this.type = type;
        this.taglists = taglists;
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


    //endregion

}
