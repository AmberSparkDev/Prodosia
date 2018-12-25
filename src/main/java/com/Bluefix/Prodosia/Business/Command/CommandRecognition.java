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

package com.Bluefix.Prodosia.Business.Command;

import com.Bluefix.Prodosia.Business.Logger.ApplicationWindowLogger;
import com.Bluefix.Prodosia.Data.DataHandler.CommandPrefixStorage;
import com.Bluefix.Prodosia.Data.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.Business.Prefix.CommandPrefix;
import com.Bluefix.Prodosia.Data.Logger.ILogger;

/**
 * Executes strings if they adhere to the prefix syntax.
 */
public class CommandRecognition
{
    private ILogger _logger;
    private ILogger _appLogger;



    public CommandRecognition(ILogger logger, ILogger appLogger)
    {
        // store the dependencies
        _logger = logger;
        _appLogger = appLogger;
    }



    /**
     * This method will attempt to execute the entries if they contained a command prefix
     * that corresponds with the indicated type.
     * @param type The type of service that is checking for a command.
     * @param info The command information that is available.
     * @param entry The entry for execution
     */
    public void executeEntry(CommandPrefix.Type type, CommandInformation info, String entry)
    {
        // if the entry is empty, return
        if (entry == null || entry.trim().isEmpty())
            return;

        CommandPrefix cPref;

        try
        {
            // Retrieve the specified command prefixes
            cPref = CommandPrefixStorage.getPrefixForType(type);

            // if the command prefix wasn't set, simply ignore
            if (cPref == null)
            {
                if (_logger != null)
                    _logger.warn("[CommandRecognition] There was no prefix for the requested type (" + type.toString() + ")");

                if (_appLogger != null)
                    _appLogger.info("There was no prefix info for " + type.toString() + "!");
                return;
            }
        } catch (Exception e)
        {
            if (_logger != null)
                _logger.error("[CommandRecognition] Exception while attempting to fetch prefix for type " + type.toString() + "\r\n" + e.getMessage());

            return;
        }
        // attempt to execute the entry
        try
        {
            // retrieve the command index for the specified comment.
            int comIndex = cPref.matchIndex(entry);

            // continue if the comment contains no command.
            if (comIndex < 0)
                return;

            // execute the command.
            CommandHandler.execute(info, entry.substring(comIndex));

        }
        catch (Exception e)
        {
            if (_logger != null)
                _logger.error("[CommandRecognition] Exception while attempting to execute command.\r\n" + e.getMessage());
        }
    }
}
