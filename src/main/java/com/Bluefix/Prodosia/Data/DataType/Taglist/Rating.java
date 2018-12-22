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

package com.Bluefix.Prodosia.Data.DataType.Taglist;

/**
 * The rating modifier for a post.
 */
    public enum Rating
{
    ALL(0),
    SAFE(1),
    QUESTIONABLE(2),
    EXPLICIT(3),
    UNKNOWN(4);

    private int value;

    private Rating(int value)
    {
        this.value = value;
    }

    public int getValue()
    {
        return value;
    }

    public static Rating parseValue(int value)
    {
        switch (value)
        {
            case 0:
                return ALL;

            case 1:
                return SAFE;

            case 2:
                return QUESTIONABLE;

            case 3:
                return EXPLICIT;

            case 4:
                return UNKNOWN;

            default:
                throw new IllegalArgumentException("This rating value was not recognized");
        }
    }
}

