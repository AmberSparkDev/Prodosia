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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class FilterTest
{
    private static final String s0 = "filter0";
    private String s1 = "filter1";
    private String s2 = "filter0 filter1";

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testNull()
    {
        String pattern = Filter.getPatternForFilters(null);

        Assert.assertEquals("", pattern);
    }


    @Test
    public void testNoFilters()
    {
        List<String> filters = new LinkedList<>();

        String pattern = Filter.getPatternForFilters(filters.iterator());

        Assert.assertEquals("", pattern);
    }

    @Test
    public void testSingleFilter()
    {
        List<String> filters = new LinkedList<>();
        filters.add("filter0");

        String pattern = Filter.getPatternForFilters(filters.iterator());

        Assert.assertEquals("(^|\\s+)(?i)(filter0)($|\\s+).*", pattern);

        Assert.assertTrue(s0.matches(pattern));
        Assert.assertFalse(s1.matches(pattern));
        Assert.assertTrue(s2.matches(pattern));
    }

    @Test
    public void testMultipleFilters()
    {
        List<String> filters = new LinkedList<>();
        filters.add("filter0");
        filters.add("filter1");
        filters.add("filter2");

        String pattern = Filter.getPatternForFilters(filters.iterator());

        Assert.assertEquals("(^|\\s+)(?i)(filter0|filter1|filter2)($|\\s+).*", pattern);

        Assert.assertTrue(s0.matches(pattern));
        Assert.assertTrue(s1.matches(pattern));
        Assert.assertTrue(s2.matches(pattern));
    }
}