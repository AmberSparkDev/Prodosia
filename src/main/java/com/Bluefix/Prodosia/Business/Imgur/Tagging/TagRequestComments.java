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

package com.Bluefix.Prodosia.Business.Imgur.Tagging;

import com.Bluefix.Prodosia.Business.Algorithm.Partition;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Data.DataHandler.UserHandler;
import com.Bluefix.Prodosia.Data.DataType.Comments.StatComment;
import com.Bluefix.Prodosia.Data.DataType.Comments.TagRequest.BaseTagRequest;
import com.Bluefix.Prodosia.Data.DataType.User.User;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This helper class handles the comment collections for tag requests
 */
public class TagRequestComments
{



    /**
     * Parse the comments that are necessary for the specified tag request.
     *
     *
     * @param tr
     * @param postComments Optionally the comments of the post that this tag request will be posted to.
     * @return
     * @throws Exception
     */
    public static LinkedList<String> parseCommentsForTagRequest(BaseTagRequest tr, List<Comment> postComments) throws Exception
    {
        // find all the users necessary for the tag request.
        ArrayList<String> users = findUsernamesForTagRequest(tr);

        HashSet<String> mentionedUsers = findMentionedUsers(postComments);

        users.removeIf(u -> mentionedUsers.contains(u.toLowerCase()));

        // convert the users into tag-entries
        LinkedList<String> tagEntries = new LinkedList<>();

        for (String u : users)
            tagEntries.add("@" + u + " ");

        // partition the users
        // we use the max comment length + 1 because the last space can be omitted.
        LinkedList<String> partitionResult = Partition.partitionEntries(tagEntries, StatComment.MaxCommentLength + 1);

        // trim the final space from every item.
        LinkedList<String> output = new LinkedList<>();

        for (String s : partitionResult)
            output.add(s.trim());

        return output;
    }

    //region User handling

    /**
     * Retrieve all users for the specified tag request.
     * @param tr the tag request to be used as filter.
     * @return A list of users that correspond with the tag request.
     * @throws Exception Issue retrieving users in `UserHandler`
     */
    public static ArrayList<User> findUsersForTagRequest(BaseTagRequest tr) throws Exception
    {
        ArrayList<User> output = new ArrayList<>();

        // retrieve all users and filter out the ones that do not adhere to the tag request filters.
        ArrayList<User> users = UserHandler.handler().getAll();

        for (User u : users)
        {
            // add all users that were part of the tag request.
            if (u.partOfTagRequest(tr))
                output.add(u);
        }

        return output;
    }

    private static ArrayList<String> findUsernamesForTagRequest(BaseTagRequest tr) throws Exception
    {
        ArrayList<String> items = new ArrayList<>();

        ArrayList<User> users = findUsersForTagRequest(tr);

        if (users == null || users.isEmpty())
            return items;

        for (User u : users)
        {
            items.add(u.getImgurName());
        }

        return items;
    }


    /**
     * The pattern for retrieving imgur mentions.
     */
    private static final Pattern userMentionPattern = Pattern.compile("(\\G|\\s|\\A)@([^@\\s]+)(\\s|$)");

    /**
     * Retrieve all mentioned users from the post. This includes people that have posted a comment themselves.
     * @param postComments
     * @return
     */
    private static HashSet<String> findMentionedUsers(List<Comment> postComments)
    {
        HashSet<String> mentions = new HashSet<>();

        List<Comment> currentTier = postComments;

        while (!currentTier.isEmpty())
        {
            List<Comment> newTier = new LinkedList<>();

            for (Comment c : currentTier)
            {
                newTier.addAll(c.getChildren());

                mentions.add(c.getAuthorName().toLowerCase());

                // check to see if the comment contains mentions.
                Matcher m = userMentionPattern.matcher(c.getComment());

                while (m.find())
                {
                    mentions.add(m.group(2).toLowerCase());
                }
            }


            currentTier = newTier;
        }

        return mentions;
    }


    private static final Pattern pureMentionCommentPattern = Pattern.compile("\\A(@[^@\\s]+(\\s|\\z)+)+");


    /**
     * Retrieve the amount of Comments that the bot account has posted for us on this post.
     * Will only count mentions that were purely posted for mentions, without any other text.
     * @param comments
     * @return
     * @throws BaringoApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static int findNumberOfMyMentions(List<Comment> comments) throws BaringoApiException, IOException, URISyntaxException
    {
        if (comments == null || comments.isEmpty())
            return 0;

        List<Comment> myTagComments = findMyTagComments(comments);

        if (myTagComments == null || myTagComments.isEmpty())
            return 0;

        int counter = 0;

        for (Comment c : myTagComments)
        {
            // add the amount of occurences of `@` to the counter.
            String tmpRep = c.getComment().replace("@", "");
            counter += c.getComment().length() - tmpRep.length();
        }

        return counter;
    }

    public static List<Comment> findMyTagComments(List<Comment> comments) throws BaringoApiException, IOException, URISyntaxException
    {
        if (comments == null || comments.isEmpty())
            return null;

        String posterName = ImgurManager.client().getAuthenticatedUserName();

        if (posterName == null || posterName.trim().isEmpty())
            return null;

        List<Comment> result = new LinkedList<>();


        List<Comment> commentTier = comments;

        while (!commentTier.isEmpty())
        {
            List<Comment> newTier = new LinkedList<>();

            for (Comment c : commentTier)
            {
                newTier.addAll(c.getChildren());

                if (c.getAuthorName().equals(posterName))
                {
                    if (pureMentionCommentPattern.matcher(c.getComment()).matches())
                    {
                        result.add(c);
                    }
                }
            }

            commentTier = newTier;
        }

        return result;
    }


    //endregion
}






















