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

package com.Bluefix.Prodosia.Imgur.Tagging;

import com.Bluefix.Prodosia.DataType.TagRequest;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This helper class handles the comment collections for tag requests
 */
public class TagRequestComments
{
    /**
     * Maximum amount of characters that fit in a comment.
     */
    public static final int MaxCommentLength = 140;


    public static LinkedList<String> parseCommentsForTagRequest(TagRequest tr)
    {
        // find all the users necessary for the tag request.
        ArrayList<String> users = findUsersForTagRequest(tr);

        // for now, use a very rudimentary algorithm that simply adds people to a line
        // so long as they fit
        // TODO: use a proper algorithm.
        LinkedList<String> output = new LinkedList<>();

        StringBuilder sb = new StringBuilder();

        for (String s : users)
        {
            if (s == null || s.isEmpty())
                continue;

            // if this user will exceed comment length, store the comment and begin a new one.
            if (sb.length() + s.length() + 2 > MaxCommentLength)
            {
                output.add(sb.toString());

                sb = new StringBuilder();
            }

            sb.append("@" + s + " ");
        }

        if (sb.length() > 0)
            output.add(sb.toString());

        return output;
    }

    //region User handling

    private static ArrayList<String> findUsersForTagRequest(TagRequest tr)
    {
        ArrayList<String> users = new ArrayList<>();

        for (int i = 0; i < 20; i++)
        {
            users.add("mashedstew");
        }


        return users;
    }

    //endregion
}
