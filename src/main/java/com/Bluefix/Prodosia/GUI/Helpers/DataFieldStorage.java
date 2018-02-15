/*
 * Copyright (c) 2018 Bas Boellaard
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

package com.Bluefix.Prodosia.GUI.Helpers;

/**
 * Helper class that takes a variable amount of strings and
 * stores them in the proper order. These can be retrieve at a later date.
 *
 * The class creates copies of the stored data.
 *
 * In order to create a DataFieldStorage object, call its static `store` method.
 */
public class DataFieldStorage
{
    private String[] data;

    /**
     * Private constructor
     */
    private DataFieldStorage(String[] data)
    {
        this.data = data;
    }


    /**
     * Create a new DataFieldStorage object that stores the specified fields.
     * @param fields The fields to be stored.
     * @return A new DataFieldStorage object.
     */
    public static DataFieldStorage store(String... fields)
    {
        String[] data = new String[fields.length];

        int counter = 0;

        for (String field : fields)
        {
            data[counter++] = new String(field);
        }

        return new DataFieldStorage(data);
    }

    /**
     * Retrieve the stored fields.
     * @return The fields that were stored in this DataFieldStorage object.
     */
    public String[] retrieve()
    {
        return data;
    }



}
