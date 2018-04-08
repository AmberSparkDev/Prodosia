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

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.github.kskelm.baringo.util.BaringoApiException;

import javax.activation.CommandInfo;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Handles the command to subscribe a user.
 */
public class SubCommand implements ICommandFunc
{
    @Override
    public void execute(CommandInformation ci, String[] arguments) throws Exception
    {
        // if there was no tracker data provided, do not execute the method.
        if (ci.getTracker() == null)
            throw new IllegalArgumentException("A tracker was not provided!");

        // first see if the arguments could contain the data.
        if (arguments == null || arguments.length == 0)
        {
            msgUsernameNotProvided(ci);
            return;
        }

        if (arguments.length == 1)
        {
            msgNoUserSubscriptions(ci);
            return;
        }

        // first, parse the user-name
        String username = arguments[0];
        // trim the `@` if it was provided.
        if (username.startsWith("@"))
            username = username.replaceFirst("@", "").trim();

        // if the resulting username is invalid, let the tracker know
        if (username.isEmpty())
        {
            msgUsernameNotProvided(ci);
            return;
        }

        int pointer = 1;
        HashSet<UserSubscription> subData = new HashSet<>();

        Taglist curTaglist = null;
        HashSet<Rating> curRatings = null;
        HashSet<String> filters = null;

        HashSet<String> permissionMissing = new HashSet<>();
        HashSet<String> needsRatings = new HashSet<>();


        while (pointer < arguments.length)
        {
            // get the current argument and increment the pointer.
            String curArg = arguments[pointer++].toLowerCase();

            // attempt to parse the current argument as a taglist.
            Taglist tmpTl = TaglistHandler.getTaglistByAbbreviation(curArg);

            // if the tracker has no permissions for this taglist, simply ignore it.
            if (!ci.getTracker().hasPermission(tmpTl))
            {
                permissionMissing.add(curArg);
                curTaglist = null;
                continue;
            }

            // if a new taglist is provided, switch data and store the old taglist.
            if (tmpTl != null)
            {
                // if there was subscription data available, add it to the subscription data
                UserSubscription us = parseUserSubscription(curTaglist, curRatings, filters);

                if (us == null && curTaglist != null)
                    needsRatings.add(curArg);

                if (us != null)
                    subData.add(us);

                // reset the taglist, ratings and filters.
                curTaglist = tmpTl;
                curRatings = new HashSet<>();
                filters = new HashSet<>();
                continue;
            }

            // if no taglist was recognized, ignore this argument.
            if (curTaglist == null)
                continue;

            // check to see if the arguments was a rating
            if ("s".equals(curArg))
            {
                curRatings.add(Rating.SAFE);
            } else if ("q".equals(curArg))
            {
                curRatings.add(Rating.QUESTIONABLE);
            } else if ("e".equals(curArg))
            {
                curRatings.add(Rating.EXPLICIT);
            } else
            {
                // if the argument was neither a taglist or rating, it will be a filter.
                filters.add(curArg);
            }
        }

        // finally, parse the last known usersubscription
        UserSubscription us = parseUserSubscription(curTaglist, curRatings, filters);

        if (us != null)
            subData.add(us);

        // if no subscription data was provided, notify the user.
        if (subData.isEmpty())
        {
            if (!permissionMissing.isEmpty())
            {
                msgMissingPermission(ci, permissionMissing);
            } else if (!needsRatings.isEmpty())
            {
                msgRatingRequired(ci, needsRatings);
            } else
            {
                // default simply means no taglists were supplied.
                msgNoUserSubscriptions(ci);
            }

            return;
        }

        // attempt to add the user.
        User u = User.retrieveUser(username, subData);

        if (u == null)
        {
            msgCheckUser(ci, username);
            return;
        }

        // store the user
        u.store();

        // reply to the user with a success message.
        LinkedList<String> taglistNames = new LinkedList<>();

        for (UserSubscription tmpUs : subData)
        {
            taglistNames.add(tmpUs.getTaglist().getAbbreviation());
        }

        msgSuccess(ci, username, taglistNames);
    }

    private static UserSubscription parseUserSubscription(Taglist tl, HashSet<Rating> ratings, Iterable<String> filters)
    {
        // if no taglist was supplied, return null
        if (tl == null)
            return null;

        // if no ratings were supplied and the taglist requires ratings, return null
        if (tl.hasRatings() && (ratings == null || ratings.isEmpty()))
            return null;

        // parse the filters into a single string
        StringBuilder filter = new StringBuilder();

        for (String f : filters)
        {
            // spaces are used as separators between filters
            filter.append(f + " ");
        }

        // create a new user-subscription.
        return new UserSubscription(tl, ratings, filter.toString().trim());
    }


    //region Messages

    private static void msgSuccess(CommandInformation ci, String username, Iterable<String> taglists) throws Exception
    {
        StringBuilder msg =
                new StringBuilder("Successfully subscribed user \"" + username + "\" to taglists (");

        for (String t : taglists)
        {
            msg.append(t + ", ");
        }

        msg.setLength(msg.length()-2);
        msg.append(").");

        ci.reply(msg.toString());
    }

    private static void msgUsernameNotProvided(CommandInformation ci) throws Exception
    {
        String msg = "Error! The username was not provided.";

        ci.reply(msg);
    }

    private static void msgNoUserSubscriptions(CommandInformation ci) throws Exception
    {
        String msg = "Error! There was no subscription data detected.";

        ci.reply(msg);
    }

    private static void msgMissingPermission(CommandInformation ci, Iterable<String> taglists) throws Exception
    {
        StringBuilder msg =
                new StringBuilder("Error! The tracker has no permissions for any of the indicated lists (");

        for (String s : taglists)
        {
            msg.append(s + ", ");
        }

        msg.setLength(msg.length()-2);
        msg.append(").");

        ci.reply(msg.toString());
    }

    private static void msgRatingRequired(CommandInformation ci, Iterable<String> taglists) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        sb.append("The following taglists require ratings: (");

        for (String s : taglists)
        {
            sb.append(s + ", ");
        }

        sb.setLength(sb.length()-2);
        sb.append(")");

        ci.reply(sb.toString());

    }

    private static void msgCheckUser(CommandInformation ci, String username) throws Exception
    {
        String msg = "Error! User \"" + username + "\" could not be stored. Please check the username.";

        ci.reply(msg);
    }

    //endregion




    @Override
    public String info()
    {
        return "Please visit https://github.com/bboellaard/Prodosia/wiki/Subscription-Command for information";
    }
}
