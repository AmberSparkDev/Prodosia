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

package com.Bluefix.Prodosia.DataType.Comments;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Imgur.Tagging.TagRequestComments;
import com.Bluefix.Prodosia.Imgur.Tagging.TagRequestStorage;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Simple struct for a tag request.
 */
public class TagRequest implements ICommentRequest
{
    private String imgurId;
    private Comment parentComment;
    private long parentId;
    private HashSet<Taglist> taglists;
    private Rating rating;
    private String filters;


    public TagRequest(String imgurId, Comment parentComment, HashSet<Taglist> taglists, Rating rating, String filters)
    {
        if ((imgurId == null || imgurId.trim().isEmpty()) &&
            parentComment == null)
            throw new IllegalArgumentException("The imgur-id and parentComment cannot be both empty. ");

        if (taglists == null || taglists.isEmpty())
            throw new IllegalArgumentException("The taglists supplied cannot be null or empty");

        if (imgurId != null)
            this.imgurId = imgurId.trim();

        this.parentComment = parentComment;

        this.taglists = taglists;
        this.rating = rating;
        this.filters = filters;
    }

    /**
     * Create a new TagRequest object, loaded in from the database.
     * @param imgurId
     * @param parentId
     * @param taglists
     * @param rating
     * @param filters
     */
    public TagRequest(String imgurId, long parentId, String taglists, int rating, String filters) throws Exception
    {
        if ((imgurId == null || imgurId.trim().isEmpty()) &&
                parentComment == null)
            throw new IllegalArgumentException("The imgur-id and parentComment cannot be both empty. ");

        if (taglists == null || taglists.isEmpty())
            throw new IllegalArgumentException("The taglists supplied cannot be null or empty");

        if (imgurId != null)
            this.imgurId = imgurId.trim();

        this.parentId = parentId;

        this.taglists = new HashSet<>();
        String[] tlArr = taglists.split(";");

        for (String t : tlArr)
        {
            this.taglists.add(TaglistHandler.getTaglistById(Long.parseLong(t)));
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

    //region Comment Request implementation

    /**
     * Retrieve the imgur id for the post.
     * @return
     */
    @Override
    public String getImgurId() throws BaringoApiException, IOException, URISyntaxException
    {
        // if the imgur id is null or empty, return it from the parent comment instead.
        if (this.imgurId == null || this.imgurId.isEmpty())
        {
            return getParent().getImageId();
        }

        return imgurId;
    }



    /**
     * Retrieve the parent-id that should be replied to. Return null when
     * no parent comment was available.
     *
     * @return
     */
    @Override
    public Comment getParent() throws BaringoApiException, IOException, URISyntaxException
    {
        if (this.parentComment == null && this.parentId < 0)
            return null;

        if (this.parentComment == null)
        {
            this.parentComment = ImgurManager.client().commentService().getComment(this.parentId);
        }

        return this.parentComment;
    }

    public long getParentId()
    {
        if (this.parentComment != null)
            return this.parentComment.getId();

        return this.parentId;
    }

    /**
     * Retrieve all comments that should be executed by this tag request.
     *
     * @return
     */
    @Override
    public LinkedList<String> getComments() throws Exception
    {
        return TagRequestComments.parseCommentsForTagRequest(this);
    }

    /**
     * Indicate whether the entry deep-equals the other request.
     *
     * @param cq
     * @return
     */
    @Override
    public boolean deepEquals(ICommentRequest cq)
    {
        if (this == cq) return true;
        if (cq == null || getClass() != cq.getClass()) return false;
        TagRequest that = (TagRequest) cq;
        return Objects.equals(imgurId, that.imgurId) &&
                Objects.equals(getParentId(), that.getParentId()) &&
                Objects.equals(taglists, that.taglists) &&
                rating == that.rating &&
                Objects.equals(filters, that.filters);
    }

    /**
     * Remove the item from the storage.
     */
    @Override
    public void remove() throws Exception
    {
        TagRequestStorage.handler().remove(this);
    }

    //endregion

    //region Equals

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagRequest that = (TagRequest) o;

        // for a valid parent id, return true if they are equal.
        if (this.getParentId() == that.getParentId() && this.getParentId() >= 0)
            return true;

        // if the parent id wasn't specified, return the equality of the imgur id.
        return Objects.equals(imgurId, that.imgurId);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(imgurId, parentId);
    }


    //endregion

    /**
     * Merge two requests together. Only possible if they share the same post-id.
     * @param o the TagRequest object to be merged.
     * @return A merged tag-request for the same imgur-id.
     */
    public TagRequest merge(TagRequest o) throws BaringoApiException, IOException, URISyntaxException
    {
        if (!this.getImgurId().equals(o.getImgurId()))
            throw new IllegalArgumentException("Merging is only supported for the same post");

        // default to the other rating
        Rating mRat = o.rating;

        //merge the taglists.
        HashSet<Taglist> mTag = new HashSet<>();
        mTag.addAll(this.taglists);
        mTag.addAll(o.taglists);

        // default to the other parentComment
        Comment parentComment;

        if (o.parentComment != null)
            parentComment = o.parentComment;
        else
            parentComment = this.parentComment;

        // default to the other filter
        return new TagRequest(this.imgurId, parentComment, mTag, mRat, o.filters);
    }
}













