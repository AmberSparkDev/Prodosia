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

import com.Bluefix.Prodosia.Command.CommandHelper.TagRequestParser;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Comments.FeedbackRequest;
import com.Bluefix.Prodosia.DataType.Comments.ICommentRequest;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.BaseTagRequest;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Imgur.Tagging.CommentExecution;
import com.Bluefix.Prodosia.DataHandler.TagRequestStorage;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
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
        // if there was no tracker data provided, do not execute the method.
        if (ci.getTracker() == null)
        {
            throw new IllegalArgumentException("A tracker was not provided!");
        }

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
        if (arguments == null || arguments.length == 0)
        {
            msgNoArguments(ci);
            return;
        }

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

        // if there was no known imgur-id, the tag request cannot complete.
        if (imgurId == null || imgurId.isEmpty())
        {
            msgNoImgurId(ci);
            return;
        }


        // filter out all arguments that don't pertain to the tag request.
        LinkedList<String> tagRequestArguments = new LinkedList<>();
        String parentComment = null;

        for (int i = pointer; i < arguments.length; i++)
        {
            String arg = arguments[i];

            if (arg.startsWith("\"") && arg.endsWith("\""))
            {
                // this is a quote and as such it will be the parent comment.
                parentComment = arg.substring(1, arg.length()-1);
            }
            else
            {
                tagRequestArguments.addLast(arg);
            }
        }

        // parse the tag request from the remaining comments.
        TagRequestParser.ParseTagRequestResult trr = TagRequestParser.parseTagRequest(ci.getTracker(), tagRequestArguments.toArray(new String[0]));

        switch (trr.getTagRequestStatus())
        {
            case NO_TAGLISTS:
                msgNoTaglists(ci);
                return;
            case NO_TAGLISTS_ALLOWED:
                msgNotAllowed(ci);
                return;
            case NO_RATING:
                msgForgotRating(ci);
                return;
        }




        if (parentComment != null)
        {
            // since it was requested that we make a new parent-comment, send a Feedback Request
            // and handle the rest of the TagRequest there

            FeedbackRequest fr = new TagParentFeedbackRequest(
                    parentComment, imgurId, trr.getTagRequest());

            CommentExecution.executeFeedbackRequest(fr);
        }
        else
        {
            // parse the tag request and add it to the queue.
            TagRequest tr = trr.getTagRequest().parseTagRequest(imgurId, ci.getParentComment());

            TagRequestStorage.handler().set(tr);
        }
    }





    /**
     * A Feedback Request class that handles a tag request once a parent comment has been created.
     */
    private static class TagParentFeedbackRequest extends FeedbackRequest
    {
        private BaseTagRequest btr;
        private String imgurId;

        public TagParentFeedbackRequest(String comment, String imgurId, BaseTagRequest btr)
        {
            super(comment);
            this.imgurId = imgurId;

            this.btr = btr;
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

                // if the parent comment could not be posted, cancel the tag request for now.
                if (pComment == null)
                    return;

                // parse the tag request and add it to the queue.
                TagRequest tr = btr.parseTagRequest(pComment);

                TagRequestStorage.handler().set(tr);
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
        public void complete()
        {
            // not necessary, since there is no storage for this feedback request.
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TagParentFeedbackRequest that = (TagParentFeedbackRequest) o;
            return Objects.equals(btr, that.btr) &&
                    Objects.equals(imgurId, that.imgurId);
        }

        @Override
        public int hashCode()
        {

            return Objects.hash(btr, imgurId);
        }
    }









    //region Messages

    private static void msgNoArguments(CommandInformation ci) throws Exception
    {
        ci.reply("Error! No tag data provided.");
    }

    private static void msgNoImgurId(CommandInformation ci) throws Exception
    {
        ci.reply("Error! No Imgur post id was provided.");
    }

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
        return "Please visit https://github.com/RoseLaLuna/Prodosia/wiki/Tag-Command for information";
    }
}
