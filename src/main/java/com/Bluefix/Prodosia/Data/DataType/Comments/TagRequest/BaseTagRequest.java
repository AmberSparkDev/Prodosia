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

package com.Bluefix.Prodosia.Data.DataType.Comments.TagRequest;

import com.Bluefix.Prodosia.Data.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.github.kskelm.baringo.model.Comment;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;

/**
 * This is a stripped-down version of the full TagRequest object.
 * It contains the information on which taglists to tag and with which
 * ratings / filters, but it doesn't yet know on which post.
 */
public class BaseTagRequest
{
    private HashSet<Taglist> taglists;
    private Rating rating;
    private String filter;
    private boolean cleanComments;

    protected BaseTagRequest(BaseTagRequest btr)
    {
        this.taglists = btr.taglists;
        this.rating = btr.rating;
        this.filter = btr.filter;
        this.cleanComments = btr.cleanComments;
    }


    public BaseTagRequest(HashSet<Taglist> taglists, Rating rating, String filter, boolean cleanComments)
    {
        if (taglists == null || taglists.isEmpty())
            throw new IllegalArgumentException("The taglists supplied cannot be null or empty");

        this.taglists = taglists;
        this.rating = rating;
        this.filter = filter;
        this.cleanComments = cleanComments;
    }


    public BaseTagRequest(String taglists, int rating, String filter, boolean cleanComments) throws SQLException
    {
        this.taglists = new HashSet<>();
        String[] tlArr = taglists.split(";");

        for (String t : tlArr)
        {
            this.taglists.add(TaglistHandler.getTaglistById(Long.parseLong(t)));
        }

        if (this.taglists == null || this.taglists.isEmpty())
            throw new IllegalArgumentException("The taglists supplied cannot be null or empty");

        this.rating = Rating.parseValue(rating);
        this.filter = filter;
        this.cleanComments = cleanComments;
    }


    public HashSet<Taglist> getTaglists()
    {
        return taglists;
    }

    public String getDbTaglists() throws SQLException
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

    public String getFilter()
    {
        return filter;
    }

    public boolean isCleanComments()
    {
        return cleanComments;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTagRequest that = (BaseTagRequest) o;
        return cleanComments == that.cleanComments &&
                Objects.equals(taglists, that.taglists) &&
                rating == that.rating &&
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(taglists, rating, filter, cleanComments);
    }

    public TagRequest parseTagRequest(String imgurId)
    {
        return new TagRequest(imgurId, null, this);
    }


    public TagRequest parseTagRequest(Comment parentComment)
    {
        return new TagRequest(null, parentComment, this);
    }

    /**
     * Parse a full Tag Request from the Base Tag Request.
     *
     * Will automatically decide between parentComment or imgur-id.
     * @param imgurId
     * @param parentComment
     * @return
     */
    public TagRequest parseTagRequest(String imgurId, Comment parentComment)
    {
        return new TagRequest(imgurId, parentComment, this);
    }
}
