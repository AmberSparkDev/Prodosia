/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.DataType.Tracker;

/**
 * A tracker data class.
 *
 * A tracker is someone with permission to use the bot
 * outside of its own GUI, either through imgur comments
 * or discord integration.
 *
 * It is possible to setup a tracker for either an imgur
 * account, a discord user or both.
 */
public class Tracker
{
    //region Data Getters and Setters
    private String imgurName;
    private long imgurId;

    private String discordName;
    private int discordTag;
    private long discordId;

    private TrackerPermissions permissions;

    public String getImgurName()
    {
        return imgurName;
    }

    public long getImgurId()
    {
        return imgurId;
    }

    public String getDiscordName()
    {
        return discordName;
    }

    public int getDiscordTag()
    {
        return discordTag;
    }

    public long getDiscordId()
    {
        return discordId;
    }

    public TrackerPermissions getPermissions()
    {
        return permissions;
    }

    //endregion

    //region Constructor

    public Tracker(
            String imgurName,
            long imgurId,
            String discordName,
            int discordTag,
            long discordId,
            TrackerPermissions permissions)
    {
        this.imgurName = imgurName;
        this.imgurId = imgurId;
        this.discordName = discordName;
        this.discordTag = discordTag;
        this.discordId = discordId;
        this.permissions = permissions;
    }


    //endregion


    //region Helper Methods

    /**
     * Returns the name of the tracker depending on whether the imgur
     * name or the discord name were set. Prefers imgur name.
     * @return The name of the tracker.
     */
    public String getName()
    {
        if (imgurName != null)
            return imgurName;

        return discordName;
    }

    //endregion




}
