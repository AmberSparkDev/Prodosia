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


import com.Bluefix.Prodosia.Discord.DiscordManager;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.util.BaringoApiException;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * A tracker-builder to create tracker objects.
 *
 * This class helps you by retrieving missing data from the API where necessary.
 */
public class TrackerBuilder
{
    //region Variables and Constructor

    private String imgurName;
    private long imgurId;
    private String discordName;
    private String discordTag;
    private String discordId;
    private TrackerPermissions trackerPermissions;

    public TrackerBuilder()
    {

    }

    public static TrackerBuilder builder()
    {
        return new TrackerBuilder();
    }

    //endregion

    //region Builder methods

    public Tracker build()
    {
        return new Tracker(imgurName, imgurId, discordName, discordTag, discordId, trackerPermissions);
    }


    /**
     * Set the imgur name for the tracker. This method will use up 1 GET request to retrieve the imgur id
     * @param imgurName
     * @return
     */
    public TrackerBuilder setImgurName(String imgurName) throws BaringoApiException, IOException, URISyntaxException
    {
        Account acc = ImgurManager.client().accountService().getAccount(imgurName);

        this.imgurName = imgurName;
        this.imgurId = acc.getId();

        return this;
    }

    /**
     * Set the permissions for the Tracker
     * @param permissions
     * @return
     */
    public TrackerBuilder setPermissions(TrackerPermissions permissions)
    {
        this.trackerPermissions = permissions;
        return this;
    }

    public TrackerBuilder setDiscordId(String discordId) throws Exception
    {
        // TODO: obtain discord data from id
        this.discordId = discordId;

        User u = DiscordManager.manager().getUserById(discordId);

        this.discordName = u.getName();
        this.discordTag = u.getDiscriminator();

        return this;
    }



    //endregion

}
