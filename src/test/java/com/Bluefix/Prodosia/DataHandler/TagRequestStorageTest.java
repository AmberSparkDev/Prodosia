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

import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

public class TagRequestStorageTest extends DataHandlerTest<TagRequest>
{
    private static final String TestImgurId = "ZqWMmil";

    private Taglist taglist;
    private TagRequest request;


    //region abstract method implementation


    public TagRequestStorageTest(boolean useLocalStorage)
    {
        super(useLocalStorage);
    }

    @Override
    protected LocalStorageHandler<TagRequest> getHandler()
    {
        return TagRequestStorage.handler();
    }

    @Override
    protected TagRequest getItem()
    {
        return request;
    }


    //endregion




    @Before
    public void setUp() throws Exception
    {
        taglist = new Taglist("test0", "test0 taglist", false);
        TaglistHandler.handler().set(taglist);

        HashSet<Taglist> tlSet = new HashSet<>();
        tlSet.add(taglist);

        this.request = new TagRequest(TestImgurId, null, tlSet, Rating.ALL, "filters", true);
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().clear(taglist);
        TagRequestStorage.handler().remove(request);
    }




    //region Test merge

    @Test
    public void testMerge() throws LoginException, SQLException, IOException, BaringoApiException, URISyntaxException
    {
        // create a new TagRequest that will merge
        Taglist tlTest1 = new Taglist("test1", "test1 taglist", false);
        TaglistHandler.handler().set(tlTest1);

        HashSet<Taglist> tlSet = new HashSet<>();
        tlSet.add(tlTest1);

        TagRequest newRequest = new TagRequest(TestImgurId, null, tlSet, Rating.EXPLICIT, "nofilters", true);

        // setup the initial request.
        Assert.assertEquals(-1, TagRequestStorage.handler().getAll().indexOf(request));
        Assert.assertFalse(TagRequestStorage.handler().getAll().contains(request));

        int size = TagRequestStorage.handler().getAll().size();

        TagRequestStorage.handler().set(request);

        Assert.assertTrue(TagRequestStorage.handler().getAll().contains(request));
        Assert.assertTrue(TagRequestStorage.handler().getAll().indexOf(request) >= 0);

        Assert.assertEquals(size+1, TagRequestStorage.handler().getAll().size());

        // now add the new request that will merge.
        TagRequestStorage.handler().set(newRequest);

        Assert.assertTrue(TagRequestStorage.handler().getAll().contains(request));
        Assert.assertEquals(size+1, TagRequestStorage.handler().getAll().size());

        int index = TagRequestStorage.handler().getAll().indexOf(request);

        TagRequest storedRequest = TagRequestStorage.handler().getAll().get(index);

        Assert.assertTrue(storedRequest.getTaglists().contains(taglist));
        Assert.assertTrue(storedRequest.getTaglists().contains(tlTest1));
        Assert.assertTrue(storedRequest.getRating() == Rating.EXPLICIT);
        Assert.assertEquals("nofilters", storedRequest.getFilter());

        TaglistHandler.handler().clear(tlTest1);
    }

    //endregion



}





































