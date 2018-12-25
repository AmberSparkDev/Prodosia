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

package com.Bluefix.Prodosia.Business.Discord;

import com.Bluefix.Prodosia.Business.Command.CommandRecognition;
import com.Bluefix.Prodosia.Data.DataHandler.CommandPrefixStorage;
import com.Bluefix.Prodosia.Data.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.Data.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.Data.DataType.Command.DiscordCommandInformation;
import com.Bluefix.Prodosia.Data.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Business.Prefix.CommandPrefix;
import com.Bluefix.Prodosia.Data.Logger.ILogger;
import com.Bluefix.Prodosia.Data.Storage.IKeyStorage;
import com.Bluefix.Prodosia.Data.Storage.KeyStorage;
import com.github.kskelm.baringo.util.BaringoApiException;
import com.sun.istack.internal.NotNull;
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
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.LinkedList;

public class DiscordManager implements IDiscordManager
{
    private IKeyStorage _keyStorage;
    private ILogger _logger;
    private ILogger _appLogger;

    private String _token;
    private JDA _jda;


    /**
     * Create a new Discord Manager that will maintain a Discord API object.
     * @param keyStorage The storage container where the discord token will be stored.
     * @param logger System logger
     * @param appLogger Application logger
     */
    public DiscordManager(@NotNull IKeyStorage keyStorage,
                          ILogger logger,
                          ILogger appLogger)
    {
        // store the dependencies
        _keyStorage = keyStorage;
        _logger = logger;
        _appLogger = appLogger;

        // initialize the JDA
        createJDA();
    }

    /**
     * Create a new Discord Manager that will maintain a Discord API object.
     * @param token The token used with the Discord Manager.
     * @param keyStorage The storage container where the discord token will be stored.
     * @param logger System logger
     * @param appLogger Application logger
     */
    public DiscordManager(String token,
            @NotNull IKeyStorage keyStorage,
                          ILogger logger,
                          ILogger appLogger)
    {
        // store the dependencies
        _keyStorage = keyStorage;
        _logger = logger;
        _appLogger = appLogger;

        _token = token;

        // initialize the JDA
        createJDA();
    }


    private void createJDA()
    {
        // if there was a former JDA object, shut it down and dereference.
        if (_jda != null)
        {
            _jda.shutdown();
            _jda = null;
        }

        // retrieve the stored token if necessary.
        if (_token == null)
        {
            try
            {
                _token = _keyStorage.getDiscordToken();
            } catch (Exception e)
            {
                if (_logger != null)
                    _logger.warn("[DiscordManager] Exception occurred while attempting to retrieve the discord token.\r\n" + e.getMessage());
            }
        }

        // if the token is null or empty, return
        if (_token == null || _token.trim().isEmpty())
            return;

        // attempt to build the JDA object.
        try
        {
            _jda = new JDABuilder(AccountType.BOT)
                    .setToken(_token)
                    .addEventListener(new ReadyListener(this))
                    .buildAsync();

            _jda.addEventListener(new MessageListener(_logger, _appLogger));
        } catch (LoginException e)
        {
            if (_logger != null)
                _logger.warn("[DiscordManager] LoginException occurred while attempting to connect to the Discord endpoint.\r\n" + e.getMessage());

            if (_appLogger != null)
                _appLogger.info("There was an error while attempting to connect to Discord.");
        }


    }


    /**
     * Setup the default prefix for this user.
     * @throws Exception
     */
    @Override
    public synchronized void updateSelfPrefix()
    {
        User user = _jda.getSelfUser();

        CommandPrefix cp = null;
        try
        {
            cp = CommandPrefixStorage.getPrefixForType(CommandPrefix.Type.DISCORD);
        } catch (SQLException e)
        {
            if (_logger != null)
                _logger.error("[DiscordManager] SQLException while attempting to retrieve the command-prefix.\r\n" + e.getMessage());
        }

        // if there was no existing command-prefix, parse a new one and return.
        if (cp == null)
        {
            try
            {
                CommandPrefixStorage.handler().set(parseNewCommandPrefix(user));
            } catch (Exception e)
            {
                if (_logger != null)
                    _logger.error("[DiscordManager] Exception thrown while attempting to store the generated command prefix.\r\n" + e.getMessage());

                if (_appLogger != null)
                    _appLogger.info("There was an error while attempting to store the discord prefix.");
            }
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
            if (_logger != null)
                _logger.warn("[DiscordManager] Exception thrown while attempting to retrieve the complete prefix pattern.\r\n" + e.getMessage());

            // in case of failure, use the default command-prefix.
            try
            {
                CommandPrefixStorage.handler().set(parseNewCommandPrefix(user));
            } catch (Exception ex)
            {
                if (_logger != null)
                    _logger.error("[DiscordManager] Exception thrown while attempting to store the generated command prefix.\r\n" + ex.getMessage());

                if (_appLogger != null)
                    _appLogger.info("There was an error while attempting to store the discord prefix.");
            }
        }
    }

    private CommandPrefix parseNewCommandPrefix(User user)
    {
        String pattern = CommandPrefix.parsePatternForItems(user.getAsMention());

        // ensure that the bot still responds to commands if the bot has a nickname.
        if (pattern.startsWith("<@!"))
        {
            pattern = pattern.replace("<@!", "<@!?");
        }
        else if (pattern.startsWith("<@"))
        {
            pattern = pattern.replace("<@", "<@!?");
        }

        return new CommandPrefix(CommandPrefix.Type.DISCORD, pattern);
    }

    //endregion

    //region Sanitation check

    /**
     * This method will check whether the discord token is valid.
     * @param discordToken The token to be validated.
     * @return
     */
    public static boolean discordTokenIsValid(String discordToken) throws Exception
    {
        // attempt to create a new JDA object with the specified discord token.
        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(discordToken)
                .buildBlocking();

        return jda != null;
    }

    @Override
    public JDA getJDA()
    {
        return _jda;
    }

    public void setupToken(String token)
    {
        _token = token;

        try
        {
            _keyStorage.setDiscordToken(_token);
        } catch (Exception e)
        {
            if (_logger != null)
                _logger.warn("[DiscordManager] An exception occurred while attempting to store the discord token.\r\n" + e.getMessage());

            if (_appLogger != null)
                _appLogger.info("Something went wrong while attempting to store the discord token.");
        }

        // finally attempt to create the JDA.
        createJDA();
    }







    /**
     * Message listener for discord.
     */
    private static class MessageListener extends ListenerAdapter
    {
        private ILogger _logger;
        private ILogger _appLogger;

        public MessageListener(ILogger logger,
                               ILogger appLogger)
        {
            // store the dependencies
            _logger = logger;
            _appLogger = appLogger;
        }


        @Override
        public void onMessageReceived(MessageReceivedEvent event)
        {
            _logger.debug("[DiscordManager] Discord Message\r\n" +
                    event.getMessage().getContentRaw());

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

            // create a command recognition object and execute the entry.
            CommandRecognition cr = new CommandRecognition(_logger, _appLogger);
            cr.executeEntry(CommandPrefix.Type.DISCORD, ci, event.getContentRaw());
        }
    }



    /**
     * Generic event listener for possible future expansion.
     */
    private static class ReadyListener implements EventListener
    {
        private IDiscordManager _discordManager;

        public ReadyListener(IDiscordManager discordManager)
        {
            _discordManager = discordManager;
        }


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
                    _discordManager.updateSelfPrefix();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                System.out.println("Discord API successfully initialized.");
            }
        }
    }


}
