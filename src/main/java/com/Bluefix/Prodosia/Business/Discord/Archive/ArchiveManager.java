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

package com.Bluefix.Prodosia.Business.Discord.Archive;

import com.Bluefix.Prodosia.Data.DataHandler.ArchiveHandler;
import com.Bluefix.Prodosia.Data.DataType.Archive.Archive;
import com.Bluefix.Prodosia.Data.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.Business.Discord.DiscordManager;
import com.Bluefix.Prodosia.Business.Logger.Logger;
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
public class ArchiveManager
{
    /**
     * The maximum amount of comments that should be kept to check history for duplicates.
     */
    private static final int HistoryLimit = 50;

    //region Singleton and constructor

    private static ArchiveManager me;

    private synchronized static ArchiveManager manager()
    {
        if (me == null)
        {
            assert(HistoryLimit > 0);
            me = new ArchiveManager();
        }

        return me;
    }

    private ArchiveManager()
    {
        this.channels = new HashMap<>();
    }

    //endregion

    //region Channel list

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
        private ChannelInfo(String channelId)
        {
            // retrieve the message history from the comments.
            try
            {
                this.textChannel = DiscordManager.manager().getTextChannelById(channelId);


                MessageHistory mh = textChannel.getHistory();
                this.comments = new LinkedList<>();

                for (Message m : mh.getRetrievedHistory())
                {
                    comments.addLast(m.getContentRaw());
                }
            }
            catch (LoginException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            // TODO: Check if the message-history is stored in the right order.
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

    //endregion

    //region Archiving logic


    /**
     * Posts a tag request item to the specified archive channels if applicable.
     * @param tagRequest
     */
    public static void handleTagRequest(TagRequest tagRequest) throws SQLException
    {
        // update the channel-list according to the archives.
        manager().update();

        // find the channels that correspond with the tag request
        for (Map.Entry<Archive, ChannelInfo> e : manager().channels.entrySet())
        {
            try
            {
                // if this channel was part of the specified tag request, post it.
                if (e.getKey().isPartOf(tagRequest))
                {
                    e.getValue().postMessage(tagRequest.getArchiveMessage());
                }

            } catch (IOException e1)
            {
                Logger.logMessage(e1.getMessage(), Logger.Severity.ERROR);

                e1.printStackTrace();
            } catch (BaringoApiException e1)
            {
                Logger.logMessage(e1.getMessage(), Logger.Severity.ERROR);
                e1.printStackTrace();
            } catch (URISyntaxException e1)
            {
                Logger.logMessage(e1.getMessage(), Logger.Severity.ERROR);
                e1.printStackTrace();
            }
            catch (Exception ex)
            {
                Logger.logMessage(ex.getMessage(), Logger.Severity.ERROR);
                ex.printStackTrace();
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

            channels.put(a, new ChannelInfo(a.getChannelId()));
        }
    }


    //endregion
}
