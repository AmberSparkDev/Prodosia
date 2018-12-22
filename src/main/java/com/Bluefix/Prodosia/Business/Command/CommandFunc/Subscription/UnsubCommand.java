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

package com.Bluefix.Prodosia.Business.Command.CommandFunc.Subscription;

import com.Bluefix.Prodosia.Business.Command.CommandFunc.ICommandFunc;
import com.Bluefix.Prodosia.Data.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.Data.DataHandler.UserHandler;
import com.Bluefix.Prodosia.Data.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Data.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Data.DataType.User.User;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class UnsubCommand implements ICommandFunc
{
    @Override
    public void execute(CommandInformation ci, String[] arguments) throws Exception
    {
        // if there was no tracker data provided, do not execute the method
        if (ci.getTracker() == null)
            throw new IllegalArgumentException("A tracker was not provided!");

        // check if the command could be valid.
        if (arguments == null || arguments.length < 1)
        {
            msgUsernameNotProvided(ci);
            return;
        }

        // check if the user was part of the system.
        User u = UserHandler.getUserByImgurName(arguments[0]);

        if (u == null)
        {
            msgUserNotFound(ci);
            return;
        }

        // retrieve all taglists for the arguments.
        Iterator<Taglist> taglists =
                getTaglistsForArguments(ci.getTracker(), arguments).iterator();

        // if no taglists were supplied, notify the user.
        if (!taglists.hasNext())
        {
            if (arguments.length == 1)
            {
                // this should in theory never happen, since a user
                // shouldn't be able to exist in the system without at least
                // one UserSubscription
                msgNoTaglistsSetup(ci);
            }
            else
            {
                msgNoTaglistsRecognized(ci);
            }
            return;
        }

        // next, filter out all taglists that the user did not have permission to.
        HashSet<Taglist> allowedLists = new HashSet<>();

        while (taglists.hasNext())
        {
            Taglist cur = taglists.next();

            if (ci.getTracker().hasPermission(cur))
                allowedLists.add(cur);
        }

        if (allowedLists.isEmpty())
        {
            msgTaglistsNotAllowed(ci);
            return;
        }

        // unsubscribe the user from all specified taglists. If these are all taglists
        // the user had, the user should be deleted.
        Iterable<String> unsubscription = u.unsubscribe(allowedLists);
        Iterator<String> itUnsub = unsubscription.iterator();


        if (!itUnsub.hasNext())
        {
            msgNoTaglistIntersection(ci);
            return;
        }

        msgSuccess(ci, u.getImgurName(), itUnsub);
    }

    private Iterable<Taglist> getTaglistsForArguments(Tracker t, String[] arguments) throws Exception
    {
        // ignore the first argument since that is the username.

        // if no further arguments were supplied, complete the user from all taglists.
        if (arguments.length == 1)
        {
            return TaglistHandler.handler().getAll();
        }
        else
        {
            LinkedList<Taglist> lists = new LinkedList<>();

            for (int i = 1; i < arguments.length; i++)
            {
                Taglist tl = TaglistHandler.getTaglistByAbbreviation(arguments[i]);

                if (tl != null)
                    lists.add(tl);
            }

            return lists;
        }
    }

    //region Messages

    private static void msgUsernameNotProvided(CommandInformation ci) throws Exception
    {
        String message = "Error! No username was given.";

        ci.reply(message);
    }

    private static void msgUserNotFound(CommandInformation ci) throws Exception
    {
        String message = "Error! The user was not found in any taglist.";

        ci.reply(message);
    }

    private static void msgNoTaglistsSetup(CommandInformation ci) throws Exception
    {
        String message = "Error! The bot does not have any taglists set up yet.";

        ci.reply(message);
    }

    private static void msgNoTaglistsRecognized(CommandInformation ci) throws Exception
    {
        String message = "Error! None of the provided taglists were recognized.";

        ci.reply(message);
    }

    private static void msgTaglistsNotAllowed(CommandInformation ci) throws Exception
    {
        String message = "Error! The user was not allowed to unsubscribe from these taglists.";

        ci.reply(message);
    }

    private static void msgNoTaglistIntersection(CommandInformation ci) throws Exception
    {
        String message = "Notice: The user was not part of any of these taglists.";

        ci.reply(message);
    }

    private static void msgSuccess(CommandInformation ci, String username, Iterator<String> taglists) throws Exception
    {
        StringBuilder sb =
                new StringBuilder("Successfully unsubscribed user \"" + username + "\" from taglists (");

        while (taglists.hasNext())
        {
            sb.append(taglists.next() + ", ");
        }

        sb.setLength(sb.length()-2);
        sb.append(").");

        ci.reply(sb.toString());
    }

    //endregion

    @Override
    public String info()
    {
        return "Please visit https://github.com/RoseLaLuna/Prodosia/wiki/Unsubscription-Command for information";
    }
}
