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

package com.Bluefix.Prodosia.Command.CommandFunc.Subscription;

import com.Bluefix.Prodosia.Command.CommandFunc.ICommandFunc;
import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Command.ImgurCommandInformation;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.BaringoClient;
import com.github.kskelm.baringo.GalleryService;
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnsuballCommandTest
{
    /**
     * The name used for testing.
     *
     * The account will not change its name, so it can be presumed to always
     * be available.
     */
    private static final String TestImgurName = "mashedstew";

    private static final int TestImgurId = 33641050;

    private static final String TestImgurPost = "ZqWMmil";

    private static final String TestTaglist = "test0";

    private ICommandFunc unsuballCommand;

    private Taglist tlTest0;
    //private Taglist tlTest1;


    @Mock
    CommandInformation commandInformation;

    @Mock
    ImgurCommandInformation ici;

    @Mock
    Tracker tracker;

    @Mock
    Comment parentComment;

    @Mock
    BaringoClient client;

    @Mock
    GalleryService gallery;

    @Mock
    Comment tagComment;

    @Mock
    Comment subComment;

    @Mock
    Account selfAccount;

    @Mock
    Comment rootComment;

    /*
     * - Tag Comment (somewhere) (1)
     * - Root comment (2)
     *   - parentComment (3)
     *   - subscription comment(s) (4+)
     *
     *   Authenticated accounts:
     *   1 = Poster
     *   2 = Tracker
     *   TestImgurId = Subscription user
     */

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception
    {
        unsuballCommand = new UnsuballCommand();

        ImgurManager.setClient(client);
        when(client.galleryService()).thenReturn(gallery);
        when(client.getAuthenticatedAccount()).thenReturn(selfAccount);

        when(selfAccount.getId()).thenReturn(1);


        when(tracker.getName()).thenReturn(TestImgurName);
        when(tracker.getImgurId()).thenReturn(new Long(2));

        when(tagComment.getComment()).thenReturn("@Prefix tag test0");
        when(tagComment.getId()).thenReturn(new Long(1));
        when(tagComment.getAuthorId()).thenReturn(2);

        when(parentComment.getComment()).thenReturn("Parent Comment");
        when(parentComment.getId()).thenReturn(new Long(3));
        when(parentComment.getAuthorId()).thenReturn(2);
        when(parentComment.getParentId()).thenReturn(new Long(2));

        when(subComment.getId()).thenReturn(new Long(4));
        when(subComment.getAuthorId()).thenReturn(TestImgurId);
        when(subComment.getParentId()).thenReturn(new Long(2));

        when(rootComment.getId()).thenReturn(new Long(2));
        when(rootComment.getAuthorId()).thenReturn(2);


        tlTest0 = new Taglist("test0", "test0 taglist", false);

        TaglistHandler.handler().set(tlTest0);
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().clear(tlTest0);


        User u = UserHandler.getUserByImgurName(TestImgurName);

        if (u != null)
            UserHandler.handler().remove(u);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoTracker() throws Exception
    {
        unsuballCommand.execute(ici, new String[0]);
    }


    @Test
    public void testNoImgurCommand() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        unsuballCommand.execute(commandInformation, new String[0]);

        verify(commandInformation).reply("This command can only be used through Imgur comments.");
    }

    @Test
    public void testNoParentComment() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);

        unsuballCommand.execute(ici, new String[0]);

        verify(ici).reply("This command cannot be executed in the root of a post.");
    }

    @Test
    public void testTooManyArguments() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);

        unsuballCommand.execute(ici, new String[2]);

        verify(ici).reply("There were too many arguments! Did you forget to put the syntax between \"\" quotation marks?");
    }

    @Test
    public void testNoQuotationMarks() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);

        unsuballCommand.execute(ici, new String[]{"NOQUOTE"});

        verify(ici).reply("There were no \"\" quotation marks around the argument. Please check your syntax.");
    }

    @Test
    public void testNoImgurPost() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);

        unsuballCommand.execute(ici, new String[0]);

        verify(ici).reply("Something went wrong and I can't find the imgur-id of the post :<");
    }

    @Test
    public void testErrorRetrievingComments() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenThrow(new BaringoApiException(""));

        unsuballCommand.execute(ici, new String[0]);

        verify(ici).reply("Something went wrong and I can't find the post comments :<");
    }

    @Test
    public void testNoPreviouslyTaggedContent() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenReturn(new LinkedList<>());

        unsuballCommand.execute(ici, new String[0]);

        verify(ici).reply("There were no taglists detected that were previously tagged by 'mashedstew'");
    }

    @Test
    public void testNoPermittedTaglists() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        LinkedList<Comment> coll = new LinkedList<>();
        coll.add(tagComment);
        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenReturn(coll);

        unsuballCommand.execute(ici, new String[0]);

        verify(ici).reply("You don't have permissions for any of the indicated taglists.");
    }


    @Test
    public void testNoUnsubscriptionCommentDetected() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        LinkedList<Comment> coll = new LinkedList<>();
        coll.add(tagComment);
        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenReturn(coll);

        when(tracker.hasPermission(tlTest0)).thenReturn(true);

        unsuballCommand.execute(ici, new String[0]);

        verify(ici).reply("Could not find any comments that indicate an unsubscription request. Is your pattern correct?");
    }


    //endregion

    //region Good weather

    @Test
    public void testUnsubscribeSingleUser() throws Exception
    {
        User user = simpleSubscription();
        Assert.assertNull(UserHandler.getUserByImgurId(TestImgurId));
        UserHandler.handler().set(user);
        Assert.assertNotNull(UserHandler.getUserByImgurId(TestImgurId));

        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        List<Comment> comments = getCollection();

        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenReturn(comments);

        when(tracker.hasPermission(tlTest0)).thenReturn(true);

        unsuballCommand.execute(ici, new String[0]);

        verify(ici).reply("Successfully unsubscribed 1 users from taglist (test0)");

        Assert.assertNull(UserHandler.getUserByImgurId(TestImgurId));
    }

    private List<Comment> getCollection()
    {
        List<Comment> childComments = new LinkedList<>();

        when(subComment.getComment()).thenReturn(".");
        when(subComment.getAuthorName()).thenReturn(TestImgurName);

        childComments.add(subComment);
        childComments.add(parentComment);

        when(rootComment.getChildren()).thenReturn(childComments);
        when(rootComment.getComment()).thenReturn("Reply to this comment to unsubscribe");

        LinkedList<Comment> coll = new LinkedList<>();
        coll.add(rootComment);
        coll.add(tagComment);

        return coll;
    }

    private User simpleSubscription() throws SQLException
    {
        HashSet<Rating> nonRatingRatings = new HashSet<>();
        nonRatingRatings.add(Rating.ALL);

        UserSubscription us = new UserSubscription(tlTest0, nonRatingRatings, null);
        HashSet<UserSubscription> hus = new HashSet<>();
        hus.add(us);

        return new User(TestImgurName, TestImgurId, hus);
    }


    //endregion












}















