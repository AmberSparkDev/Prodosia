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

package com.Bluefix.Prodosia.DataType.Comments.TagRequest;

import com.Bluefix.Prodosia.DataType.Comments.ICommentRequest;
import com.Bluefix.Prodosia.DataType.Comments.SimpleCommentRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Exception.BaringoExceptionHelper;
import com.Bluefix.Prodosia.DataHandler.CommentDeletionStorage;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.DataHandler.SimpleCommentRequestStorage;
import com.Bluefix.Prodosia.Imgur.Tagging.TagRequestComments;
import com.Bluefix.Prodosia.DataHandler.TagRequestStorage;
import com.Bluefix.Prodosia.Logger.Logger;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A Tag Request that pertains to tagging a post.
 *
 * The Tag Request will create a parent comment if it wasn't provided.
 * The Tag Request will filter out users that were already tagged on a post.
 */
public class TagRequest extends BaseTagRequest implements ICommentRequest
{
    /**
     * The maximum amount of times that the tag request can be attempted to be posted again
     * before it fails.
     */
    private static final int MaximumRetries = 500;

    private String imgurId;
    private Comment parentComment;
    private long parentId;


    /**
     * Boolean to indicate whether the Tag Request was already started once.
     */
    private boolean isStarted;


    public TagRequest(String imgurId, Comment parentComment, HashSet<Taglist> taglists, Rating rating, String filters, boolean cleanComments)
    {
        super(taglists, rating, filters, cleanComments);

        if (imgurId != null)
            this.imgurId = imgurId.trim();

        this.parentComment = parentComment;

        checkCreationConditions();
        defaultValues();
    }

    /**
     * Create a new TagRequest object, loaded in from the database.
     * @param imgurId
     * @param parentId
     * @param taglists
     * @param rating
     * @param filters
     */
    public TagRequest(String imgurId, long parentId, String taglists, int rating, String filters, boolean cleanComments) throws SQLException
    {
        super(taglists, rating, filters, cleanComments);

        if (imgurId != null)
            this.imgurId = imgurId.trim();

        this.parentId = parentId;

        checkCreationConditions();
        defaultValues();
    }

    protected TagRequest(String imgurId, Comment parentComment, BaseTagRequest btr)
    {
        super(btr);

        if (imgurId != null)
            this.imgurId = imgurId.trim();

        this.parentComment = parentComment;

        checkCreationConditions();
        defaultValues();
    }


    private void checkCreationConditions()
    {
        if ((imgurId == null || imgurId.isEmpty()) &&
                parentComment == null)
            throw new IllegalArgumentException("The imgur-id and parentComment cannot be both empty. ");

        if (    imgurId != null &&
                parentComment != null &&
                !parentComment.getImageId().equals(imgurId))
            throw new IllegalArgumentException("The imgur-id and parentComment do not correspond to the same post. ");
    }

    private void defaultValues()
    {
        this.postIsInvalid = false;
        this.parentIsInvalid = false;
        this.postIsComplete = false;
        this.counter = 0;
        this.isStarted = false;
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
        if (this.parentComment == null && this.parentId <= 0)
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
     * This boolean indicates whether the post for whatever reason has become invalid.
     */
    private boolean postIsInvalid;

    /**
     * This boolean indicates whether the parent comment has been deleted.
     */
    private boolean parentIsInvalid;

    /**
     * This boolean indicates that the post has been successfully and completely tagged.
     */
    private boolean postIsComplete;

    private List<Comment> lastKnownComments;

    /**
     * Retrieve all comments that should be executed by this tag request.
     *
     * @return
     */
    @Override
    public LinkedList<String> getComments() throws Exception
    {
        try
        {
            // if we weren't started yet, check to see if the parent comment is still valid (if applicable)
            if (!isStarted)
            {
                isStarted = true;

                getParent();
                Logger.logMessage("Starting tag on \"" + this.getImgurId() + "\"", Logger.Severity.INFORMATIONAL);
            }
        }
        catch (BaringoApiException ex)
        {
            if (BaringoExceptionHelper.isBadRequest(ex) ||
                    BaringoExceptionHelper.isNotFound(ex))
                parentIsInvalid = true;
        }

        try
        {
            lastKnownComments = ImgurManager.client().galleryService().getItemComments(this.getImgurId(), Comment.Sort.Best);

            // if you really want to, it is possible to filter out the poster account as well.
            // The only issue is that it's 1 whole GET request for just 1 person.
            // I myself can't really justify that cost and have disabled it.
            //
            /*
            try
            {
                Album album = ImgurManager.client().albumService().getAlbum(this.getImgurId());
                String posterOp = album.getUserName();
            }
            catch (BaringoApiException ex)
            {
                ex.printStackTrace();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            */


            LinkedList<String> trComments = TagRequestComments.parseCommentsForTagRequest(this, lastKnownComments);

            // if there were no more comments to be posted, the tag request is done.
            if (trComments == null || trComments.isEmpty())
                postIsComplete = true;

            return trComments;
        }
        catch (BaringoApiException ex)
        {
            if (BaringoExceptionHelper.isBadRequest(ex) ||
                    BaringoExceptionHelper.isNotFound(ex))
                postIsInvalid = true;

            return new LinkedList<String>();
        }
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
                super.equals(that);
    }

    private int counter;

    /**
     * Remove the item from the storage.
     */
    @Override
    public void complete() throws Exception
    {
        // if the parent has become invalid, delete this tag request.
        if (parentIsInvalid)
        {
            Logger.logMessage("Parent-comment for post \"" + getImgurId() + "\" was deleted.", Logger.Severity.INFORMATIONAL);
            TagRequestStorage.handler().remove(this);
            return;
        }

        // if the post has become invalid or complete, delete this tag request.
        if (postIsInvalid)
        {
            Logger.logMessage("Post \"" + getImgurId() + "\" was deleted.", Logger.Severity.INFORMATIONAL);
            TagRequestStorage.handler().remove(this);
            return;
        }

        // if the post was complete, delete this tag request.
        if (postIsComplete)
        {
            Logger.logMessage("Post \"" + getImgurId() + "\" successfully tagged.");
            TagRequestStorage.handler().remove(this);

            // retrieve the amount of users that were posted.
            int amount = TagRequestComments.findNumberOfMyMentions(lastKnownComments);

            // retrieve the actual tag comments and delete them if applicable.
            if (this.isCleanComments())
            {
                List<Comment> tagComments = TagRequestComments.findMyTagComments(lastKnownComments);

                for (Comment c : tagComments)
                {
                    CommentDeletionStorage.handler().set(c.getId());
                }
            }

            // finally, post the reply
            postSuccessfullCompletion(amount);

            return;
        }

        // if we are over the maximum amount of retries, delete this tag request.
        if (counter++ > MaximumRetries)
        {
            Logger.logMessage("Post \"" + getImgurId() + "\" has timed out.", Logger.Severity.WARNING);
            TagRequestStorage.handler().remove(this);
            return;
        }

        // if neither of the three is the case, the post should not be deleted yet.
    }


    private void postSuccessfullCompletion(int amount) throws Exception
    {
        String message = "Successfully tagged " + amount + " users.";


        Comment parent = this.getParent();
        SimpleCommentRequest scr;

        if (parent == null)
            scr = new SimpleCommentRequest(this.getImgurId(), message);
        else
            scr = new SimpleCommentRequest(parent.getId(), message);

        SimpleCommentRequestStorage.handler().set(scr);


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
        Rating mRat = o.getRating();

        //merge the taglists.
        HashSet<Taglist> mTag = new HashSet<>();
        mTag.addAll(this.getTaglists());
        mTag.addAll(o.getTaglists());

        // default to the other parentComment
        Comment parentComment;

        if (o.parentComment != null)
            parentComment = o.parentComment;
        else
            parentComment = this.parentComment;

        // default to the other filter
        return new TagRequest(this.imgurId, parentComment, mTag, mRat, o.getFilter(), this.isCleanComments());
    }


    public String getArchiveMessage() throws BaringoApiException, IOException, URISyntaxException
    {
        return "https://imgur.com/gallery/" + getImgurId();
    }








}













