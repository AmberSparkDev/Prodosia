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

package com.Bluefix.Prodosia.Command.CommandFunc;

import com.Bluefix.Prodosia.Business.Command.CommandFunc.ICommandFunc;
import com.Bluefix.Prodosia.Business.Command.CommandFunc.TagCommand;
import com.Bluefix.Prodosia.Data.DataHandler.TagRequestStorage;
import com.Bluefix.Prodosia.Data.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.Data.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.Data.DataType.Comments.FeedbackRequest;
import com.Bluefix.Prodosia.Data.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Data.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Business.Imgur.Tagging.CommentExecution;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TagCommandTest
{
    // the imgur-id of the post that is user for tag-request testing.
    private static final String TestImgurId = "ZqWMmil";

    private ICommandFunc tagCommand;

    private Taglist tlTest0;
    private Taglist tlTest1;

    @Mock
    CommandInformation commandInformation;

    @Mock
    Tracker tracker;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();


    @Before
    public void setUp() throws Exception
    {
        tagCommand = new TagCommand();

        tlTest0 = new Taglist("test0", "test0 taglist", false);
        tlTest1 = new Taglist("test1", "test1 taglist", true);

        TaglistHandler.handler().set(tlTest0);
        TaglistHandler.handler().set(tlTest1);
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().clear(tlTest0);
        TaglistHandler.handler().clear(tlTest1);

        ArrayList<TagRequest> req = new ArrayList<>(TagRequestStorage.handler().getAll());

        // make sure to remove all items from the storage that pertain to this test.
        for (TagRequest tr : req)
        {
            if (tr.getImgurId().equals(TestImgurId))
                TagRequestStorage.handler().remove(tr);
        }


    }


    //region Bad Weather tests

    @Test(expected = IllegalArgumentException.class)
    public void testNoTracker() throws Exception
    {
        tagCommand.execute(commandInformation, new String[0]);
    }

    @Test
    public void testNoParentOrImgurId() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        tagCommand.execute(commandInformation, new String[0]);

        verify(commandInformation).reply("Error! No tag data provided.");
    }

    @Test
    public void testNoImgurId() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        tagCommand.execute(commandInformation, new String[]{""});

        verify(commandInformation).reply("Error! No Imgur post id was provided.");
    }


    @Test
    public void testNoTaglists() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        tagCommand.execute(commandInformation, new String[]{TestImgurId});

        verify(commandInformation).reply("Error! No taglists were found.");
    }

    @Test
    public void testNoRatings() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest1)).thenReturn(true);

        tagCommand.execute(commandInformation, new String[]{TestImgurId, "test1"});

        verify(commandInformation).reply("Error! All provided taglists require a rating. Please provide one (s / q / e).");
    }

    @Test
    public void testTaglistNotAllowed() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        tagCommand.execute(commandInformation, new String[]{TestImgurId, "test1"});

        verify(commandInformation).reply("Error! Tracker does not have permission for the indicated taglists");
    }

    //endregion

    //region Good Weather tests

    @Test
    public void testSuccessWithoutParentComment() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        when(commandInformation.getImgurId()).thenReturn(TestImgurId);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);

        tagCommand.execute(commandInformation, new String[]{"test0"});

        ArrayList<TagRequest> tagRequests = TagRequestStorage.handler().getAll();

        boolean contained = false;

        for (TagRequest tr : tagRequests)
        {
            if (tr.getImgurId().equals(TestImgurId))
            {
                contained = tr.getParent() == null &&
                        tr.getRating() == Rating.ALL;
            }
        }

        Assert.assertTrue(contained);
    }

    @Test
    public void testSuccessWithParentComment() throws Exception
    {
        String parentComment = "parent comment";

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(commandInformation.getImgurId()).thenReturn(TestImgurId);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);

        tagCommand.execute(commandInformation, new String[]{"test0", "\"" + parentComment + "\""});

        LinkedList<FeedbackRequest> feedbackRequests = CommentExecution.handler().getFeedbackRequests();

        boolean contained = false;

        for (FeedbackRequest fbr : feedbackRequests)
        {
            if (fbr.getImgurId().equals(TestImgurId))
            {
                for (String s : fbr.getComments())
                {
                    contained = contained || parentComment.equals(s);
                }
            }
        }

        Assert.assertTrue(contained);
    }

    //endregion






}