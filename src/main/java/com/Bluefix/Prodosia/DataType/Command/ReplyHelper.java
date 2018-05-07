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

import java.util.LinkedList;

public class ReplyHelper
{

    /**
     * Prepare the reply to the user, making sure that the individual entries to not
     * overstep their boundary.
     * @param entries All separate entries. The return list will be at least this large.
     * @param maxLength The maximum allowed length per item.
     * @param useNumbering Whether items that are split should be numbered together.
     * @return A list with all the entries without exceeding maximum length.
     */
    public static LinkedList<String> prepareReply(String[] entries, int maxLength, boolean useNumbering)
    {
        LinkedList<String> mEn = new LinkedList<>();

        if (entries == null || entries.length == 0)
            return mEn;

        for (String s : entries)
        {
            // split the item if it's longer than the maximum comment length
            if (s.length() > maxLength)
            {
                int counter = 1;
                StringBuilder lineBuilder = new StringBuilder();

                String[] split = s.split("\\s+");

                for (String myS : split)
                {
                    // if the String in itself is longer than the length, simply cut it up.
                    int comp = maxLength;

                    if (useNumbering)
                        comp -= 4 + Math.log10(counter);

                    if (myS.length() > comp)
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
                            if (useNumbering)
                                lineBuilder.append("(" + counter++ + ") ");

                            String substring = myS.substring(curIndex,
                                    Math.min(myS.length(), curIndex + maxLength - lineBuilder.length()));

                            curIndex += substring.length();
                            lineBuilder.append(substring);

                            // add the linebuilder to the entries
                            mEn.add(lineBuilder.toString().trim());
                        }
                    }
                    else
                    {
                        // add the item to the linebuilder if possible
                        if (myS.length() + lineBuilder.length() + 1 < maxLength)
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

        return mEn;
    }


}
