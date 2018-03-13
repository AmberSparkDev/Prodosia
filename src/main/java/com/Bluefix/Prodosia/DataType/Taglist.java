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

package com.Bluefix.Prodosia.DataType;

import java.util.Objects;

/**
 * Data class for a single Taglist.
 */
public class Taglist
{


    //region Data Setters and Getters
    private String abbreviation;
    private String description;
    private boolean hasRatings;

    public String getAbbreviation()
    {
        return abbreviation;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean hasRatings()
    {
        return hasRatings;
    }

    //endregion

    //region Constructor

    public Taglist(String abbreviation, String description, boolean hasRatings)
    {
        this.abbreviation = abbreviation;
        this.description = description;
        this.hasRatings = hasRatings;
    }

    //endregion


    //region Equals

    /**
     * Equals only needs to be unique for the abbreviation.
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Taglist taglist = (Taglist) o;
        return Objects.equals(abbreviation, taglist.abbreviation);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(abbreviation);
    }


    //endregion
}
