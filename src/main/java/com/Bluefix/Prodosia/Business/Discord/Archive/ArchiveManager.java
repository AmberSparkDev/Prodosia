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

package com.Bluefix.Prodosia.Business.Discord.Archive;

import com.Bluefix.Prodosia.Business.Discord.IDiscordManager;
import com.Bluefix.Prodosia.Business.Logger.ApplicationWindowLogger;
import com.Bluefix.Prodosia.Data.DataHandler.ArchiveHandler;
import com.Bluefix.Prodosia.Data.DataType.Archive.Archive;
import com.Bluefix.Prodosia.Data.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.Business.Discord.DiscordManager;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.github.kskelm.baringo.util.BaringoApiException;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class manages the archiving functionality
 */
public class ArchiveManager implements IArchiveManager
{
    private IDiscordManager _discordManager;
    private ILogger _logger;
    private ILogger _appLogger;

    /**
     * The maximum amount of comments that should be kept to check history for duplicates.
     */
    private static final int HistoryLimit = 50;

    public ArchiveManager(IDiscordManager discordManager,
            ILogger logger,
            ILogger appLogger)
    {
        // store the dependencies
        _discordManager = discordManager;
        _logger = logger;
        _appLogger = appLogger;

        this.channels = new HashMap<>();
    }


    /**
     * Map
     * Archive -> ChannelInfo
     */
    private HashMap<Archive, ChannelInfo> channels;

    /**
     * Small class pertaining to information available on discord channels.
     */
    private static class ChannelInfo
    {
        /**
         * The comments in the visible history of the channel.
         * The first comment is the oldest.
         */
        private LinkedList<String> comments;

        /**
         * The text-channel associated with the channelid.
         */
        private TextChannel textChannel;

        /**
         * Create a new channel-info object.
         */
        private ChannelInfo(IDiscordManager discordManager,
                String channelId)
        {
            // retrieve the message history from the comments.
            this.textChannel = discordManager.getJDA().getTextChannelById(channelId);


            MessageHistory mh = textChannel.getHistory();
            this.comments = new LinkedList<>();

            for (Message m : mh.getRetrievedHistory())
            {
                comments.addLast(m.getContentRaw());
            }
        }


        /**
         * Post a message to the channel if the channel didn't contain the message.
         * @param message
         */
        private synchronized void postMessage(String message)
        {
            if (comments != null && comments.contains(message))
                return;

            if (comments == null)
                comments = new LinkedList<>();

            // append the message to the end of the comments.
            comments.addLast(message);

            // if the history has become too large, cull the older items.
            while (comments.size() > HistoryLimit)
                comments.removeFirst();

            textChannel.sendMessage(message).submit();
        }
    }




    /**
     * Posts a tag request item to the specified archive channels if applicable.
     * @param tagRequest
     */
    @Override
    public void handleTagRequest(TagRequest tagRequest) throws SQLException
    {
        // update the channel-list according to the archives.
        update();

        // find the channels that correspond with the tag request
        for (Map.Entry<Archive, ChannelInfo> e : channels.entrySet())
        {
            try
            {
                // if this channel was part of the specified tag request, post it.
                if (e.getKey().isPartOf(tagRequest))
                {
                    e.getValue().postMessage(tagRequest.getArchiveMessage());
                }

            }catch (Exception ex)
            {
                if (_logger != null)
                    _logger.error("[ArchiveManager] Exception while attempting to post archive message.\r\n" + ex.getMessage());

                if (_appLogger != null)
                    _appLogger.info("There was a problem posting to the archives.");
            }
        }
    }


    /**
     * Update the channel-list based on the archives available.
     */
    private void update() throws SQLException
    {
        ArrayList<Archive> archives = ArchiveHandler.handler().getAll();

        // if there are any archives in the map that were deleted, complete them
        HashSet<Archive> removal = new HashSet<>();

        for (Archive a : channels.keySet())
            if (!archives.contains(a))
                removal.add(a);

        for (Archive a : removal)
            channels.remove(a);

        // add any items that weren't already contained.
        for (Archive a : archives)
        {
            if (channels.containsKey(a))
                continue;

            channels.put(a, new ChannelInfo(_discordManager, a.getChannelId()));
        }
    }

}
