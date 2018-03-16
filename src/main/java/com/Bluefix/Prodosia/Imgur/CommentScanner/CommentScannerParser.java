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

package com.Bluefix.Prodosia.Imgur.CommentScanner;

import com.Bluefix.Prodosia.Command.CommandHandler;
import com.Bluefix.Prodosia.DataHandler.CommandPrefixStorage;
import com.Bluefix.Prodosia.DataType.CommandPrefix;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Logger.Logger;
import com.github.kskelm.baringo.model.Comment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Parses comments that were retrieved for trackers.
 */
public class CommentScannerParser
{
    public static void parseComments(Tracker t, List<Comment> comments)
    {
        CommandPrefix cPref;

        try
        {
            // Retrieve the command prefix for imgur comments.
            cPref = CommandPrefixStorage.getPrefixForType(CommandPrefix.Type.IMGUR);

            // if the command prefix wasn't set, simply ignore
            if (cPref == null)
            {
                Logger.logMessage("### There was no prefix info!", Logger.Severity.WARNING);
                return;
            }
        }
        catch (Exception e)
        {
            Logger.logMessage("CommentScannerParser::parseComments -> " + e.getMessage(), Logger.Severity.ERROR);
            return;
        }

        // loop through the comments.
        for (Comment c : comments)
        {
            // if a single comment fails something it should not inhibit execution of others comments.
            try
            {
                // retrieve the command index for the specified comment.
                int comIndex = cPref.matchIndex(c.getComment());

                // continue if the comment contains no command.
                if (comIndex < 0)
                    continue;

                // execute the command.
                CommandHandler.execute(t, c.getComment().substring(comIndex));

            } catch (Exception e)
            {
                Logger.logMessage("CommentScannerParser::parseComments -> " + e.getMessage(), Logger.Severity.ERROR);
            }
        }
    }
}
