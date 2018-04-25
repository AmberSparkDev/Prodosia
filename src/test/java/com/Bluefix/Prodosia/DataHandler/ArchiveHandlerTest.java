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

import com.Bluefix.Prodosia.DataHandler.ArchiveHandler;
import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Archive.Archive;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.*;



public class ArchiveHandlerTest extends DataHandlerTest<Archive>
{
    private Taglist taglist;
    private Archive archive;




    //region abstract method implementation

    public ArchiveHandlerTest(boolean useLocalStorage)
    {
        super(useLocalStorage);
    }

    @Override
    protected LocalStorageHandler<Archive> getHandler()
    {
        return ArchiveHandler.handler();
    }

    @Override
    protected Archive getItem()
    {
        return archive;
    }

    //endregion



    @Before
    public void setUp() throws Exception
    {

        taglist = new Taglist("test0", "test0 taglist", false);
        TaglistHandler.handler().set(taglist);

        archive = new Archive(taglist, "description", "406473126898171926", new HashSet<>(), "");
    }

    @After
    public void tearDown() throws Exception
    {
        ArchiveHandler.handler().remove(archive);
        TaglistHandler.handler().clear(taglist);
    }


    @Test
    public void testGetArchiveForTaglist() throws Exception
    {
        ArrayList<Archive> archives = ArchiveHandler.getArchivesForTaglist(taglist);
        Assert.assertTrue(archives.isEmpty());

        ArchiveHandler.handler().set(archive);

        archives = ArchiveHandler.getArchivesForTaglist(taglist);
        Assert.assertEquals(1, archives.size());
    }



}