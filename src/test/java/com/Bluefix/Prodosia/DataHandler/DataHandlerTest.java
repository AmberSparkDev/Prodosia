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


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * Generic test class for any storage handlers that use `LocalStorageHandler`
 * @param <T> The type of item that is being stored.
 */
@RunWith(Parameterized.class)
public abstract class DataHandlerTest<T>
{
    protected abstract LocalStorageHandler<T> getHandler();

    protected abstract T getItem();



    @Parameterized.Parameters
    public static Collection localStorage()
    {
        return Arrays.asList(new Boolean[]{ true, false});
    }

    public DataHandlerTest(boolean useLocalStorage)
    {
        getHandler().setLocalStorage(useLocalStorage);
    }


    //region Bad weather tests

    @Test
    public void testSetNullItem() throws Exception
    {
        ArrayList<T> items = getHandler().getAll();
        int size = items.size();

        getHandler().set(null);

        Assert.assertEquals(size, getHandler().getAll().size());
    }

    @Test
    public void testDeleteNullItem() throws Exception
    {
        ArrayList<T> items = getHandler().getAll();
        int size = items.size();

        getHandler().remove(null);

        Assert.assertEquals(size, getHandler().getAll().size());
    }

    //endregion

    //region Good weather tests

    @Test
    public void testAddItem() throws Exception
    {
        ArrayList<T> items = getHandler().getAll();

        Assert.assertFalse(items.contains(getItem()));

        getHandler().set(getItem());
        items = getHandler().getAll();

        Assert.assertTrue(items.contains(getItem()));
    }

    @Test
    public void testAddAndRemoveItem() throws Exception
    {
        ArrayList<T> items = getHandler().getAll();

        Assert.assertFalse(items.contains(getItem()));

        getHandler().set(getItem());
        items = getHandler().getAll();

        Assert.assertTrue(items.contains(getItem()));

        getHandler().remove(getItem());
        items = getHandler().getAll();

        Assert.assertFalse(items.contains(getItem()));


    }


    @Test
    public void testReplaceItem() throws Exception
    {
        ArrayList<T> items = getHandler().getAll();
        int size = items.size();

        Assert.assertFalse(items.contains(getItem()));

        getHandler().set(getItem());
        items = getHandler().getAll();

        Assert.assertTrue(items.contains(getItem()));
        Assert.assertEquals(size+1, items.size());

        getHandler().set(getItem());
        items = getHandler().getAll();

        Assert.assertTrue(items.contains(getItem()));
        Assert.assertEquals(size+1, items.size());
    }

    //endregion





}
