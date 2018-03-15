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

package com.Bluefix.Prodosia.DataType;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;

import java.util.HashSet;
import java.util.Objects;

/**
 * Simple struct for a tag request.
 */
public class TagRequest
{
    private String imgurId;
    private long parentComment;
    private HashSet<Taglist> taglists;
    private Rating rating;
    private String filters;


    public TagRequest(String imgurId, long parentComment, HashSet<Taglist> taglists, Rating rating, String filters)
    {
        if (imgurId == null || imgurId.trim().isEmpty())
            throw new IllegalArgumentException("The imgur-id cannot be null or empty");

        if (taglists == null || taglists.isEmpty())
            throw new IllegalArgumentException("The taglists supplied cannot be null or empty");

        this.imgurId = imgurId.trim();

        this.parentComment = parentComment;

        this.taglists = taglists;
        this.rating = rating;
        this.filters = filters;
    }

    /**
     * Create a new TagRequest object, loaded in from the database.
     * @param imgurId
     * @param parentComment
     * @param taglists
     * @param rating
     * @param filters
     */
    public TagRequest(String imgurId, long parentComment, String taglists, int rating, String filters) throws Exception
    {
        if (imgurId == null || imgurId.trim().isEmpty())
            throw new IllegalArgumentException("The imgur-id cannot be null or empty");

        if (taglists == null || taglists.isEmpty())
            throw new IllegalArgumentException("The taglists supplied cannot be null or empty");

        this.imgurId = imgurId.trim();

        this.parentComment = parentComment;

        this.taglists = new HashSet<>();
        String[] tlArr = taglists.split(";");

        for (String t : tlArr)
        {
            this.taglists.add(TaglistHandler.getTaglist(Long.parseLong(t)));
        }

        this.rating = Rating.parseValue(rating);
        this.filters = filters;
    }

    public HashSet<Taglist> getTaglists()
    {
        return taglists;
    }

    public String getDbTaglists() throws Exception
    {
        StringBuilder sb = new StringBuilder();

        for (Taglist t : taglists)
            sb.append(t.getId() + ";");

        return sb.toString();
    }

    public Rating getRating()
    {
        return rating;
    }

    /**
     * Will return a regular expression that matches with filters by a user.
     * @return
     */
    public String getFilters()
    {
        return filters;
    }

    public String getImgurId()
    {
        return imgurId;
    }

    public long getParentComment()
    {
        return parentComment;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagRequest that = (TagRequest) o;
        return Objects.equals(imgurId, that.imgurId);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(imgurId);
    }


    /**
     * Check if a tag-request deep equals the other object.
     * @param o
     * @return
     */
    public boolean deepEquals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagRequest that = (TagRequest) o;
        return Objects.equals(imgurId, that.imgurId) &&
                Objects.equals(parentComment, that.parentComment) &&
                Objects.equals(taglists, that.taglists) &&
                rating == that.rating &&
                Objects.equals(filters, that.filters);
    }

    /**
     * Merge two requests together. Only possible if they share the same post-id.
     * @param o the TagRequest object to be merged.
     * @return A merged tag-request for the same imgur-id.
     */
    public TagRequest merge(TagRequest o)
    {
        if (!this.imgurId.equals(o.imgurId))
            throw new IllegalArgumentException("Merging is only supported for the same post");

        // default to the other rating
        Rating mRat = o.rating;

        //merge the taglists.
        HashSet<Taglist> mTag = new HashSet<>();
        mTag.addAll(this.taglists);
        mTag.addAll(o.taglists);

        // default to the other parentComment
        long parentComment;

        if (o.parentComment > 0)
            parentComment = o.parentComment;
        else
            parentComment = this.parentComment;

        // default to the other filter
        return new TagRequest(this.imgurId, parentComment, mTag, mRat, o.filters);
    }
}













