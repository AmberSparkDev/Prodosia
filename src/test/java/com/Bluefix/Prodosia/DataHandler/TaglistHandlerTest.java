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

package com.Bluefix.Prodosia.DataHandler;

import com.Bluefix.Prodosia.DataType.Taglist;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TaglistHandlerTest
{
    private TaglistHandler handler;
    private Taglist taglist;

    @Before
    public void setUp() throws Exception
    {
        handler = TaglistHandler.handler();

        taglist = new Taglist("my_abbreviation", "my_description", true);
    }


    @After
    public void tearDown() throws Exception
    {
    }


    @Test
    public void testFunctionalityWithLocalStorage() throws Exception
    {
        handler.setLocalStorage(true);

        ArrayList<Taglist> taglists = handler.getAll();

        if (taglists.contains(taglist))
            fail();

        handler.add(taglist);
        taglists = handler.getAll();

        if (!taglists.contains(taglist))
            fail();

        handler.remove(taglist);
        taglists = handler.getAll();

        if (taglists.contains(taglist))
            fail();


    }

    @Test
    public void testFunctionalityWithoutLocalStorage() throws Exception
    {
        handler.setLocalStorage(false);

        ArrayList<Taglist> taglists = handler.getAll();

        if (taglists.contains(taglist))
            fail();

        handler.add(taglist);
        taglists = handler.getAll();

        if (!taglists.contains(taglist))
            fail();

        handler.remove(taglist);
        taglists = handler.getAll();

        if (taglists.contains(taglist))
            fail();
    }




}