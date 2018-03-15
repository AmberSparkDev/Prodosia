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

package com.Bluefix.Prodosia.Imgur.CommentScanner;

import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.github.kskelm.baringo.model.Comment;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Parses comments that were retrieved for trackers.
 */
public class CommentScannerParser
{

    public static void parseComments(Tracker t, List<Comment> comments)
    {
        System.out.println("\n\n\n\n########\n\n(" + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME) + " ; User = " + t.getImgurName() + "): Going to parse the following " + comments.size() + " comments:");

        for (Comment c : comments)
        {
            System.out.println(c.getComment());
        }

        System.out.println("\n\n########\n\n\n");

    }


}
