/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
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
