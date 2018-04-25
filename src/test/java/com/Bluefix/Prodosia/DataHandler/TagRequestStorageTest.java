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

import com.Bluefix.Prodosia.DataHandler.TagRequestStorage;
import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Comment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.*;

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
}