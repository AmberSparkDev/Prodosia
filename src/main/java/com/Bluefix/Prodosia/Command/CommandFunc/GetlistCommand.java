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

package com.Bluefix.Prodosia.Command.CommandFunc;

import com.Bluefix.Prodosia.Command.CommandHelper.TagRequestParser;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Command.FileTransferable;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.Imgur.Tagging.TagRequestComments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class GetlistCommand implements ICommandFunc
{
    @Override
    public void execute(CommandInformation ci, String[] arguments) throws Exception
    {
        // if there was no tracker data provided, do not execute the method.
        if (ci.getTracker() == null)
            throw new IllegalArgumentException("A tracker was not provided!");

        // if the command information doesn't allow for file transfer, return
        if (!(ci instanceof FileTransferable))
        {
            msgOnlyUsableOnFileTransferable(ci);
            return;
        }


        // filter out all arguments that don't pertain to the tag request.
        LinkedList<String> tagRequestArguments = new LinkedList<>();
        String syntaxPattern = "%n\n";

        for (String a : arguments)
        {
            if (a.startsWith("\"") && a.endsWith("\""))
            {
                // this is a quote and as such it will be the parent comment.
                syntaxPattern = a.substring(1, a.length()-1);
            }
            else
            {
                tagRequestArguments.addLast(a);
            }
        }

        // parse the tag request from the remaining comments.
        TagRequestParser.ParseTagRequestResult trr = TagRequestParser.parseTagRequest(ci.getTracker(), tagRequestArguments.toArray(new String[0]));

        switch (trr.getTagRequestStatus())
        {
            case NO_TAGLISTS:
                msgNoTaglists(ci);
                return;
            case NO_TAGLISTS_ALLOWED:
                msgNotAllowed(ci);
                return;
            case NO_RATING:
                msgForgotRating(ci);
                return;
        }

        // find all users that correspond to this tag request.
        ArrayList<User> users = TagRequestComments.findUsersForTagRequest(trr.getTagRequest());

        // sort the users.
        Collections.sort(users, new User.AlphabeticalComparator());


        // write all these users to a temporary file
        try
        {
            File temp = File.createTempFile("list", ".txt");

            BufferedWriter bw = new BufferedWriter(new FileWriter(temp));
            int counter = 0;

            for (User u : users)
            {
                String item = syntaxPattern;

                item = item.replace("%n", u.getImgurName());
                item = item.replace("%i", "" + u.getImgurId());
                item = item.replace("%c", "" + counter++);

                bw.write(item);
            }

            bw.close();

            // finally, send the file to the user.
            ((FileTransferable)ci).sendFile(temp);
        } catch (Exception ex)
        {
            msgErrorWhileWritingFile(ci);
        }


    }



    //region Messages

    private static void msgOnlyUsableOnFileTransferable(CommandInformation ci) throws Exception
    {
        String msg = "This command can only be used on a platform that allows for file transfer.";

        ci.reply(msg);
    }

    private static void msgNoTaglists(CommandInformation ci) throws Exception
    {
        ci.reply("Error! No taglists were found.");
    }

    private static void msgNotAllowed(CommandInformation ci) throws Exception
    {
        ci.reply("Error! Tracker does not have permission for the indicated taglists");
    }

    private static void msgForgotRating(CommandInformation ci) throws Exception
    {
        ci.reply("Error! All provided taglists require a rating. Please provide one (s / q / e).");
    }

    private static void msgErrorWhileWritingFile(CommandInformation ci) throws Exception
    {
        ci.reply("Something went wrong while trying to write your file :<");
    }

    //endregion





    @Override
    public String info()
    {
        return null;
    }
}
