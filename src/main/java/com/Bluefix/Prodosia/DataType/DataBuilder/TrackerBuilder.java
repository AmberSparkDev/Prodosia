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
