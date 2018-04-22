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

package com.Bluefix.Prodosia.Command.CommandHelper;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.BaseTagRequest;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.User.Filter;
import com.github.kskelm.baringo.model.Comment;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class hosts methods that allows for arguments to easily
 * be transferred to a Tag Request.
 */
public class TagRequestParser
{


    /**
     * Generates a base tag request, containing the tag information but not the
     * post information regarding a Tag Request.
     * @param tracker
     * @param arguments
     * @return
     * @throws Exception
     */
    public static ParseTagRequestResult parseTagRequest(Tracker tracker, String[] arguments) throws Exception
    {
        if (tracker == null)
            return new ParseTagRequestResult(null, ParseTagRequestStatus.NO_TRACKER);

        HashSet<Taglist> taglists = new HashSet<>();
        Rating rating = Rating.UNKNOWN;
        ArrayList<String> filters = new ArrayList<>();

        boolean wasTaglist = false;


        // loop through the remaining arguments and ascertain what they are.
        for (String arg : arguments)
        {
            // retrieve the current argument and increment the pointer.
            String curArg = arg.toLowerCase();

            // check if the current pointer is a rating
            if ("s".equals(curArg))
            {
                rating = Rating.SAFE;
            } else if ("q".equals(curArg))
            {
                rating = Rating.QUESTIONABLE;
            } else if ("e".equals(curArg))
            {
                rating = Rating.EXPLICIT;
            } else
            {
                // check if there is a taglist that corresponds to this argument.
                Taglist tl = TaglistHandler.getTaglistByAbbreviation(curArg);

                if (tl != null)
                {
                    wasTaglist = true;

                    // check if the user has permissions to tag to this taglist.
                    if (tracker.hasPermission(tl))
                    {
                        taglists.add(tl);
                    }
                }
                else
                {
                    // if the item was not a taglist or rating, it must be considered a filter.
                    filters.add(curArg);
                }
            }
        }


        // sanity check
        // if no taglists were supplied or allowed, return null
        if (taglists.size() <= 0)
        {
            // if there was a taglist, the tracker wasn't allowed
            if (wasTaglist)
                return new ParseTagRequestResult(null, ParseTagRequestStatus.NO_TAGLISTS_ALLOWED);
            else
                return new ParseTagRequestResult(null, ParseTagRequestStatus.NO_TAGLISTS);
        }

        // if no rating was supplied, complete all taglists that require a rating.
        if (rating == Rating.UNKNOWN)
        {
            for (Taglist t : taglists)
            {
                if (t.hasRatings())
                    taglists.remove(t);
            }

            // change the rating to include "all"
            rating = Rating.ALL;
        }

        // if no taglists were remaining, indicate this to the user.
        if (taglists.size() <= 0)
        {
            return new ParseTagRequestResult(null, ParseTagRequestStatus.NO_RATING);
        }

        // get the pattern String for the filters
        String filterPattern = Filter.getPatternForFilters(filters.iterator());

        return new ParseTagRequestResult(
                new BaseTagRequest(taglists, rating, filterPattern, true),
                ParseTagRequestStatus.OK);
    }

    public enum ParseTagRequestStatus
    {
        OK,
        NO_TRACKER,
        NO_TAGLISTS,
        NO_TAGLISTS_ALLOWED,
        NO_RATING,
    }


    public static class ParseTagRequestResult
    {
        private BaseTagRequest tagRequest;
        private ParseTagRequestStatus TagRequestStatus;

        public ParseTagRequestResult(BaseTagRequest tagRequest, ParseTagRequestStatus tagRequestStatus)
        {
            this.tagRequest = tagRequest;
            TagRequestStatus = tagRequestStatus;
        }

        public BaseTagRequest getTagRequest()
        {
            return tagRequest;
        }

        public ParseTagRequestStatus getTagRequestStatus()
        {
            return TagRequestStatus;
        }
    }


}
