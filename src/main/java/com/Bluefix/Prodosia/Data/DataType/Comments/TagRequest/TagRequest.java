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

import com.Bluefix.Prodosia.Data.DataHandler.CommentDeletionStorage;
import com.Bluefix.Prodosia.Data.DataHandler.SimpleCommentRequestStorage;
import com.Bluefix.Prodosia.Data.DataHandler.TagRequestStorage;
import com.Bluefix.Prodosia.Data.DataType.Comments.ICommentRequest;
import com.Bluefix.Prodosia.Data.DataType.Comments.SimpleCommentRequest;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Business.Exception.BaringoExceptionHelper;
import com.Bluefix.Prodosia.Business.Imgur.CommentHelper;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Business.Imgur.Tagging.TagRequestComments;
import com.Bluefix.Prodosia.Business.Logger.Logger;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import javax.security.auth.login.LoginException;
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

    /**
     * The amount of retries that should be skipped before attempting to finish the post.
     */
    public static final int PostDelay = 20;

    private String imgurId;
    private Comment parentComment;
    private long parentId;


    /**
     * Boolean to indicate whether the Tag Request was already started once.
     */
    private boolean isStarted;

    /**
     * Boolean to indicate whether the Tag Request was completed, successfully or not.
     */
    private boolean isCompleted;


    public TagRequest(String imgurId, Comment parentComment, HashSet<Taglist> taglists, Rating rating, String filters, boolean cleanComments)
    {
        super(taglists, rating, filters, cleanComments);

        if (imgurId != null)
            this.imgurId = imgurId.trim();

        this.parentComment = parentComment;
        this.parentId = -1;

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
        this.parentId = -1;

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
        this.isCompleted = false;

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

    private int delay;

    /**
     * Retrieve all comments that should be executed by this tag request.
     *
     * @return
     */
    @Override
    public LinkedList<String> getComments()
    {
        try
        {
            // if we weren't started yet, check to see if the parent comment is still valid (if applicable)
            if (!isStarted)
            {
                isStarted = true;
                delay = 0;

                getParent();
                Logger.logMessage("Starting tag on \"" + this.getImgurId() + "\"", Logger.Severity.INFORMATIONAL);
            }
        }
        catch (BaringoApiException ex)
        {
            isStarted = false;

            if (BaringoExceptionHelper.isBadRequest(ex) ||
                    BaringoExceptionHelper.isNotFound(ex))
                parentIsInvalid = true;

            // don't initiate any comments for this post.
            return new LinkedList<>();
        } catch (Exception e)
        {
            isStarted = false;
            e.printStackTrace();

            // don't initiate any comments for this post.
            return new LinkedList<>();
        }

        try
        {
            // If the post was just tagged, check whether it was completed before starting
            // the delay countdown.
            if (delay == PostDelay)
            {
                // retrieve the post comments
                lastKnownComments = ImgurManager.client().galleryService().getItemComments(this.getImgurId(), Comment.Sort.Best);
                LinkedList<String> trComments = TagRequestComments.parseCommentsForTagRequest(this, lastKnownComments);

                // if there were no more comments to be posted, the tag request is done.
                if (trComments == null || trComments.isEmpty())
                {
                    postIsComplete = true;
                    delay--;
                    return new LinkedList<>();
                }

                // if the comments weren't empty, we will start the countdown. Notify the user.
                Logger.logMessage("Postponing tag on \"" + getImgurId() + "\" for " + PostDelay + " minute" +
                        (PostDelay == 1 ? "" : "s"));
            }


            // if the delay hasn't passed, return an empty list.
            if (delay-- > 0)
                return new LinkedList<>();

            // set the delay.
            delay = PostDelay;

            // retrieve the post comments
            lastKnownComments = ImgurManager.client().galleryService().getItemComments(this.getImgurId(), Comment.Sort.Best);

            // ensure that the parent comment is in the last-known comments.
            if (!CommentHelper.containsComment(lastKnownComments, getParentId()))
            {
                parentIsInvalid = true;
                return new LinkedList<>();
            }

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

            return new LinkedList<>();
        }
        catch (Exception ex)
        {
            // generic exception could be anything. We simply return an empty list, hoping that
            // the issues resolves itself. Worst case scenario, the time-out in "complete" will catch it.
            ex.printStackTrace();

            return new LinkedList<>();
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
    public void complete()
    {
        // if we were already completed, return
        if (isCompleted)
            return;

        try
        {
            // if the parent has become invalid, delete this tag request.
            if (parentIsInvalid)
            {
                Logger.logMessage("Parent-comment for post \"" + getImgurId() + "\" was deleted.", Logger.Severity.INFORMATIONAL);
                TagRequestStorage.handler().remove(this);
                isCompleted = true;

                // retrieve the actual tag comments and delete them if applicable.
                if (this.isCleanComments())
                {
                    initiateCleanComments();
                }

                return;
            }

            // if the post has become invalid or complete, delete this tag request.
            if (postIsInvalid)
            {
                Logger.logMessage("Post \"" + getImgurId() + "\" was deleted.", Logger.Severity.INFORMATIONAL);
                TagRequestStorage.handler().remove(this);
                isCompleted = true;
                return;
            }

            // if the post was complete, delete this tag request.
            if (postIsComplete)
            {
                Logger.logMessage("Post \"" + getImgurId() + "\" successfully tagged.");
                TagRequestStorage.handler().remove(this);
                isCompleted = true;

                // retrieve the amount of users that were posted.
                int amount = TagRequestComments.findNumberOfMyMentions(lastKnownComments);

                // retrieve the actual tag comments and delete them if applicable.
                if (this.isCleanComments())
                {
                    initiateCleanComments();
                }

                // finally, post the reply
                postSuccessfullCompletion(amount);


                return;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        } catch (SQLException e)
        {
            e.printStackTrace();
        } catch (LoginException e)
        {
            e.printStackTrace();
        } catch (BaringoApiException e)
        {
            e.printStackTrace();
        }


        // if we are over the maximum amount of retries, delete this tag request.
        if (counter++ > MaximumRetries)
        {
            try
            {
                Logger.logMessage("Post \"" + getImgurId() + "\" has timed out.", Logger.Severity.WARNING);
                TagRequestStorage.handler().remove(this);
            } catch (IOException e)
            {
                e.printStackTrace();
            } catch (BaringoApiException e)
            {
                e.printStackTrace();
            } catch (SQLException e)
            {
                e.printStackTrace();
            } catch (URISyntaxException e)
            {
                e.printStackTrace();
            }

            isCompleted = true;

            // retrieve the actual tag comments and delete them if applicable.
            if (this.isCleanComments())
            {
                initiateCleanComments();
            }

            return;
        }

        // if neither of the three is the case, the post should not be deleted yet.
    }


    /**
     * Attempt to initiate a cleanup of the tag comments.
     */
    private void initiateCleanComments()
    {
        // if the comment cleanup fails in any way, it should not affect application behavior.
        try
        {
            List<Comment> tagComments = TagRequestComments.findMyTagComments(lastKnownComments);

            for (Comment c : tagComments)
            {
                try
                {
                    CommentDeletionStorage.handler().set(c.getId());
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public boolean isCompleted()
    {
        return isCompleted;
    }

    private void postSuccessfullCompletion(int amount) throws BaringoApiException, IOException, URISyntaxException, LoginException, SQLException
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
        return Objects.equals(imgurId, that.imgurId);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(imgurId);
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













