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

package com.Bluefix.Prodosia.DataType.Tracker;

import com.Bluefix.Prodosia.DataType.Taglist.Taglist;

import java.util.Objects;

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
    /**
     * The id in the database.
     */
    private long id;

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
        this.id = -1;
        this.imgurName = imgurName;
        this.imgurId = imgurId;
        this.discordName = discordName;
        this.discordTag = discordTag;
        this.discordId = discordId;
        this.permissions = permissions;
    }

    public Tracker(long id, String imgurName, long imgurId, String discordName, int discordTag, long discordId, TrackerPermissions permissions)
    {
        this.id = id;
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

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    //endregion

    //region Comparison methods

    /**
     * Compare the object to see if it is equal.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tracker tracker = (Tracker) o;
        return imgurId == tracker.imgurId &&
                discordId == tracker.discordId;
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(imgurId, discordId);
    }


    //endregion

    //region Permissions

    /**
     * Indicate whether this Tracker has permission to edit the specified taglist.
     * @param t The taglist in question.
     * @return True iff the tracker may alter the taglist, false otherwise.
     */
    public boolean hasPermission(Taglist t)
    {
        return this.permissions.hasPermission(t);
    }

    //endregion
}
