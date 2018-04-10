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

package com.Bluefix.Prodosia.DataType.User;

import java.util.Iterator;

/**
 * Helper class for user filters.
 */
public class Filter
{


    public static String getPatternForFilters(Iterator<String> filters)
    {
        // if there are no filter items, return an empty pattern.
        if (!filters.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();

        // make the pattern case-insensitive.
        sb.append("(?i)(");

        // add all the separate filters.
        while (filters.hasNext())
        {
            String f = filters.next();

            sb.append(f + "|");
        }

        // complete the last latch
        sb.setLength(sb.length()-1);

        // close the pattern and return
        sb.append(")");

        return sb.toString();
    }

}
