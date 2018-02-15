/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.DataType.DataBuilder;

import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;

/**
 * Builder class for Tracker objects.
 */
public class TrackerBuilder
{
    //region variables
    private Tracker tracker;

    private String imgurName;
    private long imgurId;

    private String discordName;
    private int discordTag;
    private long discordId;

    private TrackerPermissions permissions;
    //endregion

    //region Constructor

    private TrackerBuilder()
    {

    }

    public static TrackerBuilder getBuilder()
    {
        return new TrackerBuilder();
    }

    //endregion


    //region Builder methods

    public TrackerBuilder setImgurData(String name, long id)
    {
        imgurName = name;
        imgurId =id;
        return this;
    }

    public TrackerBuilder setDiscordData(String name, int tag, long id)
    {
        discordName = name;
        discordTag = tag;
        discordId = id;
        return this;
    }

    public TrackerBuilder setPermissions(TrackerPermissions permissions)
    {
        this.permissions = permissions;
        return this;
    }

    public Tracker build()
    {
        return new Tracker(imgurName, imgurId, discordName, discordTag, discordId, permissions);
    }

    //endregion


}
