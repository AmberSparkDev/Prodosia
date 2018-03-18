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

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Comments.FeedbackRequest;
import com.Bluefix.Prodosia.DataType.Comments.ICommentRequest;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.Filter;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Imgur.Tagging.CommentExecution;
import com.Bluefix.Prodosia.Imgur.Tagging.TagRequestStorage;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class TagCommand implements ICommandFunc
{
    /**
     * Execute a tag command
     * @param ci Information pertaining to the command.
     * @param arguments The parameters for the command.
     * @throws Exception
     */
    @Override
    public void execute(CommandInformation ci, String[] arguments) throws Exception
    {
        // parse the tag request and attempt to add it to the queue.
        parseTagRequest(ci, arguments);
    }


    /**
     * Parse the arguments into a proper Tag Request.
     *
     * If something is amiss about the arguments or provided information, the
     * method will attempt to provide information to the user.
     * @param ci The Command Information
     * @param arguments The arguments for the tag command.
     * @throws Exception
     */
    private static void parseTagRequest(CommandInformation ci, String[] arguments) throws Exception
    {
        int pointer = 0;

        // if the imgur post id was not supplied in the command-information, we expect it to
        // be the first argument.
        String imgurId;

        if (    ci.getParentComment() == null &&
                (ci.getImgurId() == null || ci.getImgurId().trim().isEmpty()))
        {
            imgurId = arguments[pointer++];
        }
        else
        {
            imgurId = ci.getImgurId();
        }

        HashSet<Taglist> taglists = new HashSet<>();
        Rating rating = Rating.UNKNOWN;
        ArrayList<String> filters = new ArrayList<>();
        String parentComment = null;

        boolean wasTaglist = false;


        // loop through the remaining arguments and ascertain what they are.
        while (pointer < arguments.length)
        {
            // retrieve the current argument and increment the pointer. 
            String curArg = arguments[pointer++].toLowerCase();

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
            } else if (curArg.startsWith("\"") && curArg.endsWith("\""))
            {
                // since this is a quote, it will be the parent comment.
                parentComment = curArg.replace("\"", "");
            } else
            {
                // check if there is a taglist that corresponds to this argument.
                Taglist tl = TaglistHandler.getTaglistByAbbreviation(curArg);

                if (tl != null)
                {
                    wasTaglist = true;

                    // check if the user has permissions to tag to this taglist.
                    if (ci.getTracker().hasPermission(tl))
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
                msgNotAllowed(ci);
            else
                msgNoTaglists(ci);

            return;
        }

        // if no rating was supplied, remove all taglists that require a rating.
        if (rating == Rating.UNKNOWN)
        {
            for (Taglist t : taglists)
            {
                if (t.hasRatings())
                    taglists.remove(t);
            }
        }

        // if no taglists were remaining, indicate this to the user.
        if (taglists.size() <= 0)
        {
            msgForgotRating(ci);
            return;
        }

        // get the pattern String for the filters
        String filterPattern = Filter.getPatternForFilters(filters.iterator());


        if (parentComment != null)
        {
            // since it was requested that we make a new parent-comment, send a Feedback Request
            // and handle the rest of the TagRequest there

            FeedbackRequest fr = new TagParentFeedbackRequest(
                    parentComment, imgurId, taglists, rating, filterPattern);

            CommentExecution.executeFeedbackRequest(fr);
        }
        else
        {
            // parse the tag request and add it to the queue.
            TagRequest tr =
                    new TagRequest(imgurId, ci.getParentComment(), taglists, rating, filterPattern);

            TagRequestStorage.handler().add(tr);
        }
    }





    /**
     * A Feedback Request class that handles a tag request once a parent comment has been created.
     */
    private static class TagParentFeedbackRequest extends FeedbackRequest
    {
        private String imgurId;
        private HashSet<Taglist> taglists;
        private Rating rating;
        private String filters;

        public TagParentFeedbackRequest(String comment, String imgurId, HashSet<Taglist> taglists, Rating rating, String filters)
        {
            super(comment);
            this.imgurId = imgurId;
            this.taglists = taglists;
            this.rating = rating;
            this.filters = filters;
        }

        /**
         * This method will be executed after the comment has been posted.
         *
         * @param commentId The comment-id's of the comment(s) that were posted.
         */
        @Override
        public void runAfter(long commentId)
        {
            try
            {
                Comment pComment = ImgurManager.client().commentService().getComment(commentId);

                // parse the tag request and add it to the queue.
                TagRequest tr =
                        new TagRequest(imgurId, pComment, taglists, rating, filters.toString());

                TagRequestStorage.handler().add(tr);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        /**
         * Retrieve the imgur-id that should be replied to.
         *
         * @return
         */
        @Override
        public String getImgurId()
        {
            return this.imgurId;
        }

        /**
         * Retrieve the parent-id that should be replied to. Return null if no
         * parent comment was available.
         *
         * @return
         */
        @Override
        public Comment getParent() throws BaringoApiException, IOException, URISyntaxException
        {
            return null;
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
            return equals(cq);
        }

        /**
         * Remove the item from the storage.
         */
        @Override
        public void remove() throws Exception
        {
            // not necessary, since there is no storage for this feedback request.
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TagParentFeedbackRequest that = (TagParentFeedbackRequest) o;
            return Objects.equals(imgurId, that.imgurId) &&
                    Objects.equals(taglists, that.taglists) &&
                    rating == that.rating &&
                    Objects.equals(filters, that.filters);
        }

        @Override
        public int hashCode()
        {

            return Objects.hash(imgurId, taglists, rating, filters);
        }
    }









    //region Messages

    private static void msgNoTaglists(CommandInformation ci) throws Exception
    {
        ci.reply("Error! No taglists were found.");
    }

    private static void msgNotAllowed(CommandInformation ci) throws Exception
    {
        ci.reply("Error! Tracker does not have permission for the indicated taglists");
    }

    private static void msgForgotRating(CommandInformation ci) throws Exception
    {
        ci.reply("Error! All provided taglists require a rating. Please provide one (s / q / e).");
    }



    //endregion






    @Override
    public String info()
    {
        return "Not implemented.";
    }
}
