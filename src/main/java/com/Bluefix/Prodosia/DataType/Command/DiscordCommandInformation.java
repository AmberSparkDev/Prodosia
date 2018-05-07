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

package com.Bluefix.Prodosia.DataType.Command;

import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Discord.StatDiscord;
import net.dv8tion.jda.core.entities.Message;

import java.io.File;
import java.util.LinkedList;

public class DiscordCommandInformation extends CommandInformation implements FileTransferable
{
    private Message message;

    public DiscordCommandInformation(Tracker t, Message message)
    {
        super();
        super.setTracker(t);

        this.message = message;
    }




    /**
     * Reply to the user with the following entries.
     *
     * @param entries The entries to reply to the user to.
     */
    @Override
    public void reply(String... entries) throws Exception
    {
        LinkedList<String> replies = ReplyHelper.prepareReply(entries, StatDiscord.MaximumMessageSize, false);

        for (String r : replies)
        {
            message.getChannel().sendMessage(r).submit();
        }
    }

    /**
     * Send a file to the user.
     *
     * @param file The file to be send.
     */
    @Override
    public void sendFile(File file)
    {
        message.getChannel().sendFile(file).submit();
    }
}
