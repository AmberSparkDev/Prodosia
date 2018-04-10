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

package com.Bluefix.Prodosia.Command.CommandFunc.Subscription;

import com.Bluefix.Prodosia.Command.CommandFunc.ICommandFunc;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Command.ImgurCommandInformation;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Comment;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class UnsuballCommand implements ICommandFunc
{
    /**
     * Execute the command with the specified parameters.
     *
     * @param ci        Information pertaining to the command.
     * @param arguments The parameters for the command.
     * @return The response text for executing this command.
     */
    @Override
    public void execute(CommandInformation ci, String[] arguments) throws Exception
    {
        // if there was no tracker data provided, do not execute the method.
        if (ci.getTracker() == null)
            throw new IllegalArgumentException("A tracker was not provided!");

        // if the command-information doesn't indicate origins on imgur, return
        if (!(ci instanceof ImgurCommandInformation))
        {
            msgNotImgurComment(ci);
            return;
        }

        // if there was no parent comment, one of the requirements has failed.
        if (ci.getParentComment() == null)
            throw new IllegalArgumentException("There was no parent comment supplied!");

        // if there is more than 1 argument, the call is invalid.
        if (arguments.length > 1)
        {
            msgTooManyArguments(ci);
            return;
        }

        // if there is an argument, it should be between quotes.
        String pattern = "";
        if (arguments.length == 1)
        {
            if (!arguments[0].startsWith("\"") || !arguments[0].endsWith("\""))
            {
                msgNoQuotationMarksAroundArgument(ci);
                return;
            }

            pattern = arguments[0].substring(1, arguments[0].length()-1);
        }



        String imgurId = ci.getImgurId();

        // if there was no specified post, return
        if (imgurId == null || imgurId.trim().isEmpty())
        {
            msgNoSpecifiedImgurPost(ci);
            return;
        }

        // retrieve all comments from the post.
        List<Comment> comments;

        try
        {
            comments = ImgurManager.client().galleryService().getItemComments(imgurId, Comment.Sort.Best);
        }
        catch (Exception e)
        {
            msgErrorWhileRequestingPostComments(ci);
            return;
        }


        // get all taglists that this tracker has posted.
        List<Taglist> taglists = SubComHelper.getTaglistsFromComments(comments, ci.getTracker());

        if (taglists == null || taglists.isEmpty())
        {
            msgNoPreviouslyTaggedContentFound(ci);
            return;
        }

        // filter out all the taglists that the user doesn't have access to.
        taglists.removeIf(tl -> !ci.getTracker().hasPermission(tl));

        if (taglists.isEmpty())
        {
            msgNoTaglistsAllowed(ci);
            return;
        }


        // get all comments that correspond to our parent comment.
        List<Comment> subComments = SubComHelper.getCommentsFromSameTier(comments, ci.getParentComment());

        if (subComments != null)
        {
            // complete the calls from the tracker and the bot account itself.
            subComments.removeIf(c ->
            {
                try
                {
                    return (
                            c.getAuthorId() == ci.getTracker().getImgurId() ||
                                    c.getAuthorId() == ImgurManager.client().getAuthenticatedAccount().getId());
                } catch (Exception ex)
                {
                    // an exception here might be cause for concern, but it shouldn't impede functionality.
                    ex.printStackTrace();
                    return false;
                }
            });
        }

        if (subComments == null || subComments.isEmpty())
        {
            msgNoUnsubscriptionRequestsDetected(ci);
            return;
        }

        // for each comment, if it adheres to the pattern it will be subscribed.
        int amount = unsubscribeComments(ci, subComments, pattern, taglists);

        // if no subscriptions were made, return
        if (amount <= 0)
        {
            msgNoUnsubscriptionsPassed(ci);
            return;
        }

        msgSuccess(ci, amount, taglists);
    }

    private static int unsubscribeComments(CommandInformation ci, List<Comment> comments, String pattern, List<Taglist> taglists) throws Exception
    {
        Pattern p = null;

        if (pattern != null && !pattern.isEmpty())
            p = Pattern.compile(pattern);

        int counter = 0;

        for (Comment c : comments)
        {
            // if the comment didn't adhere to the pattern, skip it.
            if (    p != null &&
                    !p.matcher(c.getComment()).find())
                continue;

            try
            {
                User u = UserHandler.getUserByImgurId(c.getAuthorId());

                // if there was no pre-existing user with this author-id, skip it.
                if (u == null)
                    continue;

                Iterable<String> unsubscription = u.unsubscribe(new HashSet<>(taglists));

                // if at least one taglist was unsubscribed from, increment the counter.
                if (unsubscription.iterator().hasNext())
                    counter++;

            } catch (Exception e)
            {
                e.printStackTrace();
                ci.reply("Something went wrong while trying to unsubscribe \"" + c.getAuthorName() + "\"");
            }
        }

        return counter;
    }

    //region Messages

    private static void msgSuccess(CommandInformation ci, int counter, List<Taglist> taglists) throws Exception
    {
        StringBuilder msg = new StringBuilder("Successfully unsubscribed " + counter + " users from taglist");

        if (taglists.size() > 1)
            msg.append("s");

        msg.append(" (");

        for (Taglist t : taglists)
            msg.append(t.getAbbreviation() + ", ");

        msg.setLength(msg.length()-2);
        msg.append(")");

        ci.reply(msg.toString());
    }

    private static void msgNoUnsubscriptionsPassed(CommandInformation ci) throws Exception
    {
        String msg = "Something went wrong, none of the unsubscriptions have properly gone through :<";

        ci.reply(msg);
    }

    private static void msgNoUnsubscriptionRequestsDetected(CommandInformation ci) throws Exception
    {
        String msg = "Could not find any comments that indicate an unsubscription request. Is your pattern correct?";

        ci.reply(msg);
    }

    private static void msgNotImgurComment(CommandInformation ci) throws Exception
    {
        String msg = "This command can only be used through Imgur comments.";

        ci.reply(msg);
    }

    private static void msgNoSpecifiedImgurPost(CommandInformation ci) throws Exception
    {
        String msg = "Something went wrong and I can't find the imgur-id of the post :<";

        ci.reply(msg);
    }

    private static void msgTooManyArguments(CommandInformation ci) throws Exception
    {
        String msg = "There were too many arguments! Did you forget to put the syntax between \"\" quotation marks?";

        ci.reply(msg);
    }

    private static void msgNoQuotationMarksAroundArgument(CommandInformation ci) throws Exception
    {
        String msg = "There were no \"\" quotation marks around the argument. Please check your syntax.";

        ci.reply(msg);
    }

    private static void msgErrorWhileRequestingPostComments(CommandInformation ci) throws Exception
    {
        String msg = "Something went wrong and I can't find the post comments :<";

        ci.reply(msg);
    }

    private static void msgNoPreviouslyTaggedContentFound(CommandInformation ci) throws Exception
    {
        String msg = "There were no taglists detected that were previously tagged by '" + ci.getTracker().getName() + "'";

        ci.reply(msg);
    }

    private static void msgNoTaglistsAllowed(CommandInformation ci) throws Exception
    {
        String msg = "You don't have permissions for any of the indicated taglists.";

        ci.reply(msg);
    }




    /**
     * Give information on how this command should be used.
     *
     * @return
     */
    @Override
    public String info()
    {
        return "Please visit https://github.com/RoseLaLuna/Prodosia/wiki/%28Un%29suball-Command for information";
    }
}
