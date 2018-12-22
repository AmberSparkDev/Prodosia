/*
 * Copyright (c) 2018 RoseLaLuna
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

import com.Bluefix.Prodosia.Data.DataHandler.LocalStorageHandler;
import com.Bluefix.Prodosia.Data.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class TaglistHandlerTest extends DataHandlerTest<Taglist>
{
    private Taglist taglist;


    //region abstract method implementation


    public TaglistHandlerTest(boolean useLocalStorage)
    {
        super(useLocalStorage);
    }

    @Override
    protected LocalStorageHandler<Taglist> getHandler()
    {
        return TaglistHandler.handler();
    }

    @Override
    protected Taglist getItem()
    {
        return taglist;
    }


    //endregion


    @Before
    public void setUp() throws Exception
    {
        taglist = new Taglist("test0", "test0 taglist", false);
    }


    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().remove(taglist);
    }



    @Test
    public void testGetTaglistById() throws URISyntaxException, SQLException, IOException, BaringoApiException, LoginException
    {
        TaglistHandler.handler().set(taglist);

        Taglist t = TaglistHandler.getTaglistById(taglist.getId());
        Assert.assertEquals(taglist, t);
    }


    @Test
    public void testGetTaglistByAbbreviation() throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        Taglist t = TaglistHandler.getTaglistByAbbreviation("test0");
        Assert.assertNull(t);

        TaglistHandler.handler().set(taglist);

        t = TaglistHandler.getTaglistByAbbreviation("test0");
        Assert.assertEquals(taglist, t);
    }



    //region Taglist clearing

    @Test
    public void testClearNullTaglist() throws URISyntaxException, SQLException, IOException, LoginException, BaringoApiException
    {
        TaglistHandler.handler().clear(null);
    }


    @Test
    public void testClearTaglist() throws SQLException, URISyntaxException, IOException, LoginException, BaringoApiException
    {
        Taglist t = TaglistHandler.getTaglistByAbbreviation("test0");
        Assert.assertNull(t);

        TaglistHandler.handler().set(taglist);

        t = TaglistHandler.getTaglistByAbbreviation("test0");
        Assert.assertEquals(taglist, t);

        TaglistHandler.handler().clear(t);

        t = TaglistHandler.getTaglistByAbbreviation("test0");
        Assert.assertNull(t);
    }



    //endregion




}