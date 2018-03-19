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

package com.Bluefix.Prodosia.DataType.Command;

import com.Bluefix.Prodosia.DataType.Comments.SimpleCommentRequest;
import com.Bluefix.Prodosia.DataType.Comments.StatComment;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Imgur.Tagging.SimpleCommentRequestStorage;
import com.github.kskelm.baringo.model.Comment;
import com.sun.javafx.image.BytePixelSetter;

import java.util.LinkedList;

public class ImgurCommandInformation extends CommandInformation
{
    public ImgurCommandInformation(Tracker tracker, Comment comment)
    {
        super();
        super.setTracker(tracker);
        super.setParentComment(comment);
    }

    /**
     * Reply to the user with the following entries.
     *
     * @param entries The entries to reply to the user to.
     */
    @Override
    public void reply(String... entries) throws Exception
    {
        LinkedList<String> mEn = new LinkedList<>();

        for (String s : entries)
        {
            // split the item if it's longer than the maximum comment length
            if (s.length() > StatComment.MaxCommentLength)
            {
                int counter = 1;
                StringBuilder lineBuilder = new StringBuilder();

                String[] split = s.split("\\s+");

                for (String myS : split)
                {
                    // if the String in itself is longer than the length, simply cut it up.
                    if (myS.length() > StatComment.MaxCommentLength - (4 + Math.log10(counter)))
                    {
                        // first, post the current lineBuilder
                        if (lineBuilder.length() != 0)
                        {
                            mEn.add(lineBuilder.toString().trim());
                            lineBuilder = new StringBuilder();
                        }

                        int curIndex = 0;

                        while (curIndex < myS.length())
                        {
                            lineBuilder.append("(" + counter++ + ") ");

                            String substring = myS.substring(curIndex,
                                    Math.min(myS.length(), curIndex + StatComment.MaxCommentLength - lineBuilder.length()));

                            curIndex += substring.length();
                            lineBuilder.append(substring);

                            // add the linebuilder to the entries
                            mEn.add(lineBuilder.toString().trim());
                        }
                    }
                    else
                    {
                        // add the item to the linebuilder if possible
                        if (myS.length() + lineBuilder.length() + 1 < StatComment.MaxCommentLength)
                        {
                            lineBuilder.append(myS + " ");
                        }
                        else
                        {
                            // post the current linebuilder and start a new line
                            mEn.add(lineBuilder.toString().trim());
                            lineBuilder = new StringBuilder("(" + counter++ + ") " + myS + " ");
                        }
                    }
                }
            }
            else
            {
                // if it fits in one comment, simply add
                mEn.add(s);
            }
        }

        SimpleCommentRequest scr = new SimpleCommentRequest(
                        super.getParentComment().getImageId(),
                        super.getParentComment().getId(),
                        mEn);

        SimpleCommentRequestStorage.handler().set(scr);
    }
}
