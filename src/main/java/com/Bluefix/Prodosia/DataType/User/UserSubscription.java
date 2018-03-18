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

package com.Bluefix.Prodosia.DataType.User;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;

import java.util.HashSet;

public class UserSubscription
{
    private Taglist taglist;
    private HashSet<Rating> ratings;
    private String filters;

    /**
     * Create a new UserSubscription object.
     * @param taglist The taglist for which this subscription applies.
     * @param ratings The ratings for which this user wishes to receive content.
     * @param filters The filters for the specified user.
     */
    public UserSubscription(Taglist taglist, HashSet<Rating> ratings, String filters)
    {
        if (taglist == null)
            throw new IllegalArgumentException("Taglist can't be null");

        this.taglist = taglist;
        this.ratings = ratings;
        this.filters = filters;
    }

    /**
     * Parse a new UserSubscription object from the database.
     * @param taglistId The taglist id
     * @param ratings The parsed rating values
     * @param filters The filters
     */
    public UserSubscription(long taglistId, String ratings, String filters) throws Exception
    {
        this.taglist = TaglistHandler.getTaglistById(taglistId);

        if (this.taglist == null)
            throw new IllegalArgumentException("The taglist-id was not recognized.");

        // parse the ratings and add them to the hashset.
        String[] split = ratings.split(";");
        this.ratings = new HashSet<>();
        for (String s : split)
        {
            this.ratings.add(Rating.parseValue(Integer.parseInt(s)));
        }

        this.filters = filters;
    }




    public Taglist getTaglist()
    {
        return taglist;
    }

    public String getDbRating()
    {
        if (this.ratings == null)
            return "";

        StringBuilder s = new StringBuilder();

        for (Rating r : ratings)
        {
            s.append(r.getValue() + ";");
        }

        return s.toString();
    }

    public String getFilters()
    {
        return filters;
    }


    //region Tag Request

    /**
     * Returns true iff this UserSubscription object would be part of a tag request.
     * @param tr the tag request to check against.
     * @return True iff part of the tag request, false otherwise.
     */
    public boolean partOf(TagRequest tr)
    {
        // first check to see if our taglist is contained in the tag request.
        if (!tr.getTaglists().contains(this.taglist))
            return false;

        // next, check to see if any of our ratings match with the taglist.
        // this check is only used if the taglist incorporates ratings at all.
        if (this.taglist.hasRatings())
        {
            if (this.ratings == null || !this.ratings.contains(tr.getRating()))
            {
                return false;
            }
        }

        // finally check to see if any filters apply
        if (this.filters != null && this.filters.matches(tr.getFilters()))
        {

            return false;
        }

        // since everything passed, this subscription is part of the tag request.
        return true;
    }

    //endregion


}
