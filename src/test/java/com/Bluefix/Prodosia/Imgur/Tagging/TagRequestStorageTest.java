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

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.*;

public class TagRequestStorageTest
{
    private TagRequestStorage handler;

    private Taglist taglist;
    private TagRequest request;


    @Before
    public void setUp() throws Exception
    {
        taglist = new Taglist(-1,"my_abbreviation", "my_description", true);
        TaglistHandler.handler().add(taglist);

        this.handler = TagRequestStorage.handler();

        HashSet<Taglist> tlSet = new HashSet<>();
        tlSet.add(taglist);

        this.request = new TagRequest("a", null, tlSet, Rating.ALL, "filters");
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().remove(taglist);
    }


    @Test
    public void testFunctionalityWithLocalStorage() throws Exception
    {
        handler.setLocalStorage(true);

        ArrayList<TagRequest> requests = handler.getAll();

        if (requests.contains(request))
            fail();

        handler.add(request);
        requests = handler.getAll();

        if (!requests.contains(request))
            fail();

        handler.remove(request);
        requests = handler.getAll();

        if (requests.contains(request))
            fail();


    }

    @Test
    public void testFunctionalityWithoutLocalStorage() throws Exception
    {
        handler.setLocalStorage(false);

        ArrayList<TagRequest> requests = handler.getAll();

        if (requests.contains(request))
            fail();

        handler.add(request);
        requests = handler.getAll();

        if (!requests.contains(request))
            fail();

        handler.remove(request);
        requests = handler.getAll();

        if (requests.contains(request))
            fail();
    }

    @Test
    public void testMergeWithLocalStorage() throws Exception
    {
        handler.setLocalStorage(true);

        ArrayList<TagRequest> requests = handler.getAll();
        int size = requests.size();

        if (requests.contains(request))
            fail();

        handler.add(request);
        requests = handler.getAll();

        Assert.assertEquals(size + 1, requests.size());

        handler.add(request);
        requests = handler.getAll();

        Assert.assertEquals(size + 1, requests.size());

        handler.remove(request);
        requests = handler.getAll();

        if (requests.contains(request))
            fail();
    }

    @Test
    public void testMergeWithoutLocalStorage() throws Exception
    {
        handler.setLocalStorage(false);

        ArrayList<TagRequest> requests = handler.getAll();
        int size = requests.size();

        if (requests.contains(request))
            fail();

        handler.add(request);
        requests = handler.getAll();

        Assert.assertEquals(size + 1, requests.size());

        handler.add(request);
        requests = handler.getAll();

        Assert.assertEquals(size + 1, requests.size());

        handler.remove(request);
        requests = handler.getAll();

        if (requests.contains(request))
            fail();
    }
}