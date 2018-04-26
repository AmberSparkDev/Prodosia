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

package com.Bluefix.Prodosia.DataType.Archive;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;

public class Archive
{
    private Taglist taglist;
    private String description;
    private String channelId;
    private HashSet<Rating> ratings;
    private String filters;

    //region Constructor

    /**
     * Create a new archive object.
     * @param taglist
     * @param description
     * @param channelId
     * @param ratings
     * @param filters
     */
    public Archive(Taglist taglist, String description, String channelId, HashSet<Rating> ratings, String filters)
    {
        this.taglist = taglist;
        this.description = description;
        this.channelId = channelId;
        this.ratings = ratings;
        this.filters = filters;
    }

    /**
     * Load an Archi
     * @param taglistId
     * @param description
     * @param channelId
     * @param ratings
     * @param filters
     */
    public Archive(long taglistId, String description, String channelId, String ratings, String filters) throws SQLException
    {
        this.taglist = TaglistHandler.getTaglistById(taglistId);
        this.description = description;
        this.channelId = channelId;
        dbParseRatings(ratings);
        this.filters = filters;
    }

    //endregion

    //region Getters


    public Taglist getTaglist() throws SQLException
    {
        if (this.taglist != null && this.taglist.getId() >= 0)
        {
            this.taglist = TaglistHandler.getTaglistById(this.taglist.getId());
        }

        return taglist;
    }

    public String getDescription()
    {
        return description;
    }

    public String getChannelId()
    {
        return channelId;
    }

    public String getFilters()
    {
        return filters;
    }

    public HashSet<Rating> getRatings()
    {
        return ratings;
    }

    //endregion


    //region Database parsing

    public String dbGetRatings()
    {
        if (ratings == null || ratings.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        for (Rating r : ratings)
        {
            sb.append(r.getValue() + ";");
        }

        return sb.toString();
    }

    private void dbParseRatings(String ratings)
    {
        String[] split = ratings.split(";");

        if (split.length == 0)
            return;

        this.ratings = new HashSet<>();

        for (String s : split)
        {
            if (s == null || s.trim().isEmpty())
                continue;

            this.ratings.add(Rating.parseValue(Integer.parseInt(s)));
        }
    }

    //endregion

    //region TagRequest logic

    /**
     * This method returns true iff the Archive object is part of the Tag Request and the
     * specified channel should contain the tag request data.
     * @param tagRequest
     * @return true iff the Tag-request was part of this archive, false otherwise.
     */
    public boolean isPartOf(TagRequest tagRequest)
    {
        // first check to see if the taglist is correct.
        if (!tagRequest.getTaglists().contains(this.taglist))
            return false;

        // next, check to see if the rating of the tagrequest is included with this archive.
        if (    this.taglist.hasRatings() &&
                !this.ratings.contains(tagRequest.getRating()))
                return false;

        // if the archive has filters that the tag request contains, return false
        if (    this.filters != null &&
                !this.filters.trim().isEmpty() &&
                this.filters.matches(tagRequest.getFilter()))
            return false;

        // since all items have passed, this archive is part of the tag request.
        return true;
    }

    //endregion


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Archive archive = (Archive) o;
        return Objects.equals(taglist, archive.taglist) &&
                Objects.equals(channelId, archive.channelId);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(taglist, channelId);
    }
}
