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

import com.Bluefix.Prodosia.Algorithm.Partition;
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

        // convert the users into tag-entries
        LinkedList<String> tagEntries = new LinkedList<>();

        for (String u : users)
            tagEntries.add("@" + u + " ");

        // partition the users
        // we use the max comment length + 1 because the last space can be omitted.
        LinkedList<String> partitionResult = Partition.partitionEntries(tagEntries, MaxCommentLength + 1);

        // trim the final space from every item.
        LinkedList<String> output = new LinkedList<>();

        for (String s : partitionResult)
            output.add(s.trim());

        return output;
    }

    //region User handling

    private static ArrayList<String> findUsersForTagRequest(TagRequest tr)
    {

        ArrayList<String> users = new ArrayList<>();

        for (int i = 0; i < MaxCommentLength; i++)
        {
            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < i; j++)
            {
                sb.append("a");
            }

            if (sb.length() > 0)
                users.add(sb.toString());
        }


        return users;
    }

    //endregion
}
