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

package com.Bluefix.Prodosia.Command.CommandFunc.Subscription;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.github.kskelm.baringo.model.Comment;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for subscription commands.
 */
public class SubComHelper
{
    private static final Pattern tagPattern = Pattern.compile(".+[Tt][Aa][Gg]\\s+(.+)\\z");


    /**
     * Retrieve the taglists that were tagged on this post by the specified tracker.
     * @param comments The comments of the post from which to extrapolate the taglists from.
     * @param tracker The tracker that issued the tag command.
     * @return A list of taglists that were tagged by this tracker.
     */
    public static List<Taglist> getTaglistsFromComments(List<Comment> comments, Tracker tracker)
    {
        // first, find all comments that were made by the tracker. Filter out the ones that contain
        // the `tag` command.
        LinkedList<String> tagArguments = new LinkedList<>();

        List<Comment> tierItems = comments;

        while (!tierItems.isEmpty())
        {
            LinkedList<Comment> newTier = new LinkedList<>();


            for (Comment c : tierItems)
            {
                newTier.addAll(c.getChildren());

                if (c.getAuthorId() != tracker.getImgurId())
                    continue;

                Matcher m = tagPattern.matcher(c.getComment());

                if (m.find())
                {
                    tagArguments.add(m.group(1));
                }
            }


            tierItems = newTier;
        }

        // if no tracker comments could be found, return null.
        if (tagArguments.isEmpty())
            return null;


        // attempt to retrieve all taglists tagged by the tracker.
        List<Taglist> result = new LinkedList<>();

        for (String arg : tagArguments)
        {
            // ignore everything up to the "tag" command.
            String comment = arg.toLowerCase().trim();

            if (comment.isEmpty())
                continue;

            String[] split = comment.split("\\s+");

            for (String s : split)
            {
                if (s.trim().isEmpty())
                    continue;

                try
                {
                    Taglist tl = TaglistHandler.getTaglistByAbbreviation(s);

                    if (tl != null)
                        result.add(tl);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }


    /**
     * Retrieve the comments underneath the same parent comment as ours.
     * @param comments The comments of the post from which to extrapolate these comments.
     * @param commandComment The command comment.
     * @return The list of comments that have the same parent comment as the command comment.
     */
    public static List<Comment> getCommentsFromSameTier(List<Comment> comments, Comment commandComment)
    {
        // special case if the command comment had no parent.
        if (commandComment.getParentId() <= 0)
            return comments;

        // attempt to find the parent comment.
        List<Comment> tierItems = comments;

        while (!tierItems.isEmpty())
        {
            LinkedList<Comment> newTier = new LinkedList<>();


            for (Comment c : tierItems)
            {
                newTier.addAll(c.getChildren());

                if (c.getId() == commandComment.getParentId())
                {
                    return c.getChildren();
                }
            }

            tierItems = newTier;
        }

        return null;
    }




}
