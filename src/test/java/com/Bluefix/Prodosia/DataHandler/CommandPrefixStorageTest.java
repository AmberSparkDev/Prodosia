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

import com.Bluefix.Prodosia.DataType.Command.CommandPrefix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class CommandPrefixStorageTest
{
    /**
     * Keep track of the old prefix item in the database, and restore it at the end.
     */
    private CommandPrefix oldPrefix;

    private CommandPrefixStorage handler;

    private CommandPrefix tempPrefix;


    @Before
    public void setUp() throws Exception
    {
        oldPrefix = CommandPrefixStorage.getPrefixForType(CommandPrefix.Type.IMGUR);


        handler = CommandPrefixStorage.handler();
        handler.remove(oldPrefix);

        tempPrefix = new CommandPrefix(CommandPrefix.Type.IMGUR, "");

    }

    @After
    public void tearDown() throws Exception
    {
        handler.set(oldPrefix);
    }


    @Test
    public void testFunctionalityWithLocalStorage() throws Exception
    {
        handler.setLocalStorage(true);

        ArrayList<CommandPrefix> prefixes = handler.getAll();

        if (prefixes.contains(tempPrefix))
            fail();

        handler.set(tempPrefix);
        prefixes = handler.getAll();

        if (!prefixes.contains(tempPrefix))
            fail();

        handler.remove(tempPrefix);
        prefixes = handler.getAll();

        if (prefixes.contains(tempPrefix))
            fail();
    }

    @Test
    public void testFunctionalityWithoutLocalStorage() throws Exception
    {

        handler.setLocalStorage(false);

        ArrayList<CommandPrefix> prefixes = handler.getAll();

        if (prefixes.contains(tempPrefix))
            fail();

        handler.set(tempPrefix);
        prefixes = handler.getAll();

        if (!prefixes.contains(tempPrefix))
            fail();

        handler.remove(tempPrefix);
        prefixes = handler.getAll();

        if (prefixes.contains(tempPrefix))
            fail();

    }
}