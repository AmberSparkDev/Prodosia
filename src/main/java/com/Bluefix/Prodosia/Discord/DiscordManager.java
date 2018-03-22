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

package com.Bluefix.Prodosia.Discord;

import com.Bluefix.Prodosia.Command.CommandRecognition;
import com.Bluefix.Prodosia.DataHandler.CommandPrefixStorage;
import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Command.CommandPrefix;
import com.Bluefix.Prodosia.DataType.Command.DiscordCommandInformation;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Logger.Logger;
import com.Bluefix.Prodosia.Storage.KeyStorage;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.LinkedList;

public class DiscordManager
{
    //region Singleton

    private static JDA me;

    /**
     * Retrieve the JDA manager object.
     *
     * If there was no known discord key in the system, this will return null.
     * @return
     * @throws LoginException
     */
    public synchronized static JDA manager() throws Exception
    {
        if (me == null)
        {
            me = createJDA();
        }

        return me;
    }

    private static JDA createJDA() throws Exception
    {
        // retrieve the stored token
        String discordToken = KeyStorage.getDiscordToken();

        // if the token is null or empty, return null
        if (discordToken == null || discordToken.isEmpty())
            return null;


        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(discordToken)
                .addEventListener(new ReadyListener())
                .buildAsync();

        jda.addEventListener(new MessageListener());

        return jda;
    }

    private static synchronized void setupPrefix(User user) throws Exception
    {
        CommandPrefix cp = CommandPrefixStorage.getPrefixForType(CommandPrefix.Type.DISCORD);

        // if there was no existing command-prefix, parse a new one and return.
        if (cp == null)
        {
            CommandPrefixStorage.handler().set(parseNewCommandPrefix(user));
            return;
        }

        // attempt to replace the current commandprefix.
        LinkedList<String> items = new LinkedList<>();

        try
        {
            LinkedList<String> entries = CommandPrefix.parseitemsFromPattern(cp.getRegex());

            String[] newEntries = new String[entries.size()];

            // copy all items, but replace the first one with our mention.
            int counter = 0;

            for (String s : entries)
            {
                if (counter == 0)
                    newEntries[counter++] = user.getAsMention();
                else
                    newEntries[counter++] = s;
            }

            String pattern = CommandPrefix.parsePatternForItems(newEntries);
            CommandPrefix newPrefix = new CommandPrefix(CommandPrefix.Type.DISCORD, pattern);

            CommandPrefixStorage.handler().update(cp, newPrefix);

        }
        catch (Exception e)
        {
            e.printStackTrace();

            // in case of failure, use the default command-prefix.
            CommandPrefixStorage.handler().set(parseNewCommandPrefix(user));
            return;
        }
    }

    private static CommandPrefix parseNewCommandPrefix(User user)
    {
        String pattern = CommandPrefix.parsePatternForItems(user.getAsMention());
        return new CommandPrefix(CommandPrefix.Type.DISCORD, pattern);
    }

    //endregion

    //region Sanitation check

    /**
     * This method will check whether the discord token is valid, but it will not refresh the token of the
     * current manager.
     * @param discordToken
     * @return
     */
    public static boolean discordTokenIsValid(String discordToken) throws Exception
    {
        // attempt to create a new JDA object with the specified discord token.
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(discordToken)
                .buildBlocking();

        if (jda != null)
            return true;

        throw new Exception("The JDA object could not be initialized.");
    }

    /**
     * Update the Discord Manager to use a different token.
     */
    public static void update() throws Exception
    {
        if (me != null)
            me.shutdown();

        me = createJDA();

        // update the command prefix
        // this will automatically be done after the JDA object finishes initialization.
        //setupPrefix(me.getSelfUser());
    }

    //endregion


    //region Event Listener

    /**
     * Message listener for discord.
     */
    private static class MessageListener extends ListenerAdapter
    {
        @Override
        public void onMessageReceived(MessageReceivedEvent event)
        {
            System.out.println("#### Discord Message ####\n" + event.getMessage().getContentDisplay()+ "\n\n#### ####");

            // --- Trackers ---
            // Attempt to retrieve the tracker that parsed this message.
            try
            {
                Tracker t = TrackerHandler.getTrackerByDiscordId(event.getAuthor().getId());

                if (t != null)
                    trackerMessage(t, event.getMessage());

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        private void trackerMessage(Tracker t, Message event)
        {
            // attempt to parse the message as a command.

            CommandInformation ci = new DiscordCommandInformation(t, event);

            CommandRecognition.executeEntry(CommandPrefix.Type.DISCORD, ci, event.getContentDisplay());
        }
    }



    /**
     * Generic event listener for possible future expansion.
     */
    private static class ReadyListener implements EventListener
    {

        /**
         * Handles any {@link Event Event}.
         * <p>
         * <p>To get specific events with Methods like {@code onMessageReceived(MessageReceivedEvent event)}
         * take a look at: {@link ListenerAdapter ListenerAdapter}
         *
         * @param event The Event to handle.
         */
        @Override
        public void onEvent(Event event)
        {
            if (event instanceof ReadyEvent)
            {
                // update the command prefix
                try
                {
                    setupPrefix(me.getSelfUser());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                System.out.println("Discord API successfully initialized.");
            }
        }
    }


}
