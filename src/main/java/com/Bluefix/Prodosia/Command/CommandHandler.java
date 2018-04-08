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

package com.Bluefix.Prodosia.Command;

import com.Bluefix.Prodosia.Command.CommandFunc.*;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class parses and redirects commands fed to it.
 *
 * The purpose of this class is to provide a generic
 * command interface that can be used by any user input
 * (Imgur / Discord / etc.)
 */
public class CommandHandler
{
    //region CommandMap Functionality
    private static HashMap<String, ICommandFunc> commandMap;

    /**
     * Retrieve the command function object for the command if it exists.
     * @param command The command that was issued.
     * @return The command function object for the specified command, or null if it doesn't exist.
     */
    private static ICommandFunc getFunc(String command)
    {
        // intialize the commandMap if it did not exist.
        if (commandMap == null)
            initializeCommandMap();

        return commandMap.get(command.toLowerCase());
    }

    /**
     * Initialize all commands in the commandmap.
     *
     * `help` and `list` are reserved commands and cannot be added to the map.
     *
     * All commands added to the map should be lowercase and lowercase-unique.
     */
    private static void initializeCommandMap()
    {
        if (commandMap != null)
            return;

        commandMap = new HashMap<>();

        // add all the different functionality classes.
        commandMap.put("test", new TestCommand());
        commandMap.put("tag", new TagCommand());
        commandMap.put("sub", new SubCommand());
        commandMap.put("unsub", new UnsubCommand());
        commandMap.put("getuser", new GetuserCommand());
        commandMap.put("getlist", new GetlistCommand());
    }

    //endregion



    //region Parse Command


    /**
     * Execute the command specified.
     *
     * The very first item in the command, separated by a space
     * from the rest of the data, will be considered the command.
     * The rest will be considered the arguments.
     * @param ci The command information pertaining to the execution request.
     * @param command The command to be executed.
     * @return
     */
    public static void execute(CommandInformation ci, String command)
    {
        String trimmed = command.trim();

        int firstSpace = trimmed.indexOf(' ');

        if (firstSpace < 0)
            execute(ci, trimmed, new String[]{});
        else
        {
            // split the command from the arguments and execute.
            String actualCommand = trimmed.substring(0, firstSpace);
            String arguments = trimmed.substring(firstSpace + 1);

            execute(ci, actualCommand, arguments);
        }
    }

    /**
     * Execute the command specified.
     * @param command The command to be executed.
     * @param arguments The arguments, separated by a space
     * @return The command result after execution.
     */
    public static void execute(CommandInformation ci, String command, String arguments)
    {
        // keep parts between quotes together.
        String[] quoteSplit = arguments.trim().split("\"");

        ArrayList<String> splitItems = new ArrayList<>();

        boolean isQuote = arguments.startsWith("\"");

        for (String s : quoteSplit)
        {
            // if this is a quote, paste the entire part with quotes.
            if (isQuote)
            {
                splitItems.add("\"" + s + "\"");
            }
            else
            {
                // since it wasn't a quote, split by arguments.
                String[] splitArguments = s.split("\\s+");

                for (String sa : splitArguments)
                {
                    splitItems.add(sa);
                }
            }

            isQuote = !isQuote;
        }

        execute(ci, command, splitItems.toArray(new String[0]));
    }

    /**
     * Execute the command specified.
     * @param command The command to be executed.
     * @param arguments The arguments for the command.
     * @return The command result after execution.
     */
    public static void execute(CommandInformation ci, String command, String[] arguments)
    {
        String lCom = command.toLowerCase();

        // filter out any empty arguments.
        ArrayList<String> myArguments = new ArrayList<String>();

        for (String myArg : arguments)
        {
            if (myArg == null)
                continue;

            String tArg = myArg.trim();

            if (!tArg.isEmpty())
                myArguments.add(tArg);
        }

        String[] fArguments = myArguments.toArray(new String[0]);


        // If the command is either "help" or "list", execute separately.
        if ("help".equals(lCom))
        {
            try
            {
                executeHelp(ci, fArguments);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } else if ("list".equals(lCom))
        {
            try
            {
                executeList(ci, fArguments);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        } else
        {
            // if we can find the command, execute it.
            ICommandFunc func = getFunc(lCom);

            if (func == null)
            {
                try
                {
                    commandNotFound(ci, lCom);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            } else
            {
                // Start a thread for execution
                ThreadedFuncCall tfc = new ThreadedFuncCall(func, ci, fArguments);
                tfc.start();
            }
        }
    }

    //endregion

    /**
     * Helper class to execute methods asynchronous.
     */
    private static class ThreadedFuncCall extends Thread
    {
        private ICommandFunc icf;
        private CommandInformation ci;
        private String[] arguments;


        /**
         * Start a new Threaded Function Call.
         * @param icf
         * @param ci
         * @param arguments
         */
        private ThreadedFuncCall(ICommandFunc icf, CommandInformation ci, String[] arguments)
        {
            this.icf = icf;
            this.ci = ci;
            this.arguments = arguments;
        }

        /**
         * Execute the function.
         */
        @Override
        public void run()
        {
            try
            {
                icf.execute(ci, arguments);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }



    //region Custom commands

    /**
     * Execute the `help` command. This command can provide the user with information
     * regarding the available commands in the system and acts as an entry point for an
     * unknown user.
     * @param arguments The arguments provided with the help command.
     * @return The result object which contains the provided information as its message.
     */
    private static void executeHelp(CommandInformation ci, String[] arguments) throws Exception
    {
        // change the message depending on whether arguments were supplied.
        String message;

        if (arguments.length == 0)
        {
            message = "Use the `list` command to retrieve a list of possible commands.\n" +
                    "Use `help` with the specific command(s) as argument for information.";
        }
        else
        {
            StringBuilder bMessage = new StringBuilder();

            for (String arg : arguments)
            {
                ICommandFunc func = getFunc(arg);

                // Parse the argument.
                if ("help".equals(arg))
                {
                    // simply ignore
                }
                else if ("list".equals(arg))
                {
                    bMessage.append("(" + arg + "): " + listInformation());
                }
                else if (func == null)
                {
                    bMessage.append("(" + arg + "): The command does not exist.\n");
                }
                else
                {
                    // since the command exists, supply its information.
                    bMessage.append("(" + arg + "): " + func.info() + "\n");
                }
            }


            message = bMessage.toString();
        }

        // output the message to the user.
        ci.reply(message);
    }


    /**
     * Commandlist stored String for optimization.
     */
    private static String commandList = null;

    /**
     * Execute the `list` command. This command lists the available commands.
     * @param arguments Any arguments pertaining to this command.
     * @return The result object which contains the commands listed as its message.
     */
    private static void executeList(CommandInformation ci, String[] arguments) throws Exception
    {
        // init the command list if it was null.
        if (commandList == null)
        {
            // init the commands if this wasn't done yet.
            initializeCommandMap();

            StringBuilder message = new StringBuilder();

            // list all the methods in the command-map
            String[] commands = commandMap.keySet().toArray(new String[0]);

            // sort them according to natural ordering.
            Arrays.sort(commands);

            // build the string
            for (String item : commands)
            {
                message.append(item + "\n");
            }

            commandList = message.toString();
        }

        // output the list to the user.
        ci.reply(commandList);
    }

    //endregion

    //region Helper text

    private static void commandNotFound(CommandInformation ci, String lCom) throws Exception
    {
        String message = "Command \"" + lCom + "\" was not recognized!";

        // output the message to the user.
        ci.reply(message);
    }

    private static String listInformation()
    {
        return
                "Will list all of the known commands.";
    }

    //endregion





}
