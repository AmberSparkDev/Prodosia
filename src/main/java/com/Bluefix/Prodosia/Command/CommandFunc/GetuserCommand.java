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

import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Handles the command to retrieve information about a user.
 */
public class GetuserCommand implements ICommandFunc
{
    @Override
    public void execute(CommandInformation ci, String[] arguments) throws Exception
    {
        // if there was no tracker data provided, do not execute the method.
        if (ci.getTracker() == null)
            throw new IllegalArgumentException("A tracker was not provided!");

        if (arguments == null || arguments.length == 0)
        {
            msgNoArguments(ci);
            return;
        }

        LinkedList<String> responses = new LinkedList<>();

        for (String a : arguments)
        {
            String line = "(" + a + "): ";

            try
            {
                line += responseForUser(a) + "\n";
            } catch (Exception e)
            {
                e.printStackTrace();
                line += "An error occurred while retrieving this user.\n";
            }

            responses.add(line);
        }

        ci.reply(responses.toArray(new String[0]));

    }


    private static String responseForUser(String user) throws Exception
    {
        User u = UserHandler.getUserByImgurName(user);

        if (u == null)
            return "No subscription data found.";

        return responseForUserSubscriptions(u.getSubscriptions());
    }

    private static String responseForUserSubscriptions(Collection<UserSubscription> col)
    {
        StringBuilder sb = new StringBuilder("\n```");

        for (UserSubscription us : col)
        {
            // taglist abbreviation
            StringBuilder line = new StringBuilder("(" + us.getTaglist().getAbbreviation() + ")");


            String filters = us.getFilters();
            boolean hasRatings = us.getTaglist().hasRatings();
            boolean hasFilters = filters != null && !filters.trim().isEmpty();

            if (hasRatings || hasFilters)
                line.append(" -> ");

            // show ratings if applicable
            if (hasRatings)
            {
                line.append("(");

                boolean rs = false, rq = false, re = false;

                if (us.hasRating(Rating.SAFE) || us.hasRating(Rating.ALL))
                {
                    line.append("s, ");
                    rs = true;
                }

                if (us.hasRating(Rating.QUESTIONABLE) || us.hasRating(Rating.ALL))
                {
                    line.append("q, ");
                    rq = true;
                }

                if (us.hasRating(Rating.EXPLICIT) || us.hasRating(Rating.ALL))
                {
                    line.append("e, ");
                    re = true;
                }

                if (!rs && !rq && !re)
                {
                    line.append("no ratings");
                }
                else
                {
                    line.setLength(line.length()-2);
                }

                line.append(") ");
            }

            // show filters if applicable
            if (hasFilters)
                line.append("{" + filters.trim() + "}");

            sb.append(line.toString().trim() + "\n");
        }


        sb.append("```");

        return sb.toString();
    }



    //region Messages

    private static void msgNoArguments(CommandInformation ci) throws Exception
    {
        String msg = "No users were provided with the command.";

        ci.reply(msg);
    }

    //endregion



    @Override
    public String info()
    {
        return "Please visit https://github.com/RoseLaLuna/Prodosia/wiki/Getuser-Command for information";
    }
}
