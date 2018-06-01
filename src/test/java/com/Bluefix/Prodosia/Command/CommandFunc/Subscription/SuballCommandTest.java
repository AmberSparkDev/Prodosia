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
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.User.User;
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

import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SuballCommandTest
{
    /**
     * The name used for testing.
     *
     * The account will not change its name, so it can be presumed to always
     * be available.
     */
    private static final String TestImgurName = "mashedstew";

    private static final String TestImgurPost = "ZqWMmil";

    private static final String TestTaglist = "test0";

    private ICommandFunc suballCommand;

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
     * - Tag Comment (somewhere)
     * - Root comment
     *   - parentComment
     *   - subscription comment(s)
     */

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();


    @Before
    public void setUp() throws Exception
    {
        suballCommand = new SuballCommand();

        ImgurManager.setClient(client);
        when(client.galleryService()).thenReturn(gallery);
        when(client.getAuthenticatedAccount()).thenReturn(selfAccount);

        when(selfAccount.getId()).thenReturn(2);


        when(tracker.getName()).thenReturn(TestImgurName);
        when(tracker.getImgurId()).thenReturn(new Long(0));

        when(tagComment.getComment()).thenReturn("@Prefix tag test0");
        when(tagComment.getId()).thenReturn(new Long(1));

        when(parentComment.getComment()).thenReturn("Parent Comment");
        when(parentComment.getId()).thenReturn(new Long(2));
        when(parentComment.getParentId()).thenReturn(new Long(3));

        when(subComment.getId()).thenReturn(new Long(1));
        when(subComment.getAuthorId()).thenReturn(1);
        when(subComment.getParentId()).thenReturn(new Long(3));

        when(rootComment.getId()).thenReturn(new Long(3));
        when(rootComment.getAuthorId()).thenReturn(1);


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

    // region Bad Weather tests

    @Test(expected = IllegalArgumentException.class)
    public void testNoTracker() throws Exception
    {
        suballCommand.execute(ici, new String[0]);
    }

    @Test
    public void testNoParentComment() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);

        suballCommand.execute(ici, new String[2]);

        verify(ici).reply("This command cannot be executed in the root of a post.");
    }

    @Test
    public void testNoImgurCommand() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        suballCommand.execute(commandInformation, new String[0]);

        verify(commandInformation).reply("This command can only be used through Imgur comments.");
    }

    @Test
    public void testTooManyArguments() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);

        suballCommand.execute(ici, new String[2]);

        verify(ici).reply("There were too many arguments! Did you forget to put the syntax between \"\" quotation marks?");
    }

    @Test
    public void testArgumentNotBetweenQuotes() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);

        suballCommand.execute(ici, new String[] { "NOTQUOTE"});

        verify(ici).reply("There were no \"\" quotation marks around the argument. Please check your syntax.");
    }


    @Test
    public void testNoSpecifiedPost() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);

        suballCommand.execute(ici, new String[0]);

        verify(ici).reply("Something went wrong and I can't find the imgur-id of the post :<");
    }


    @Test
    public void testErrorRequestingComments() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenThrow(new BaringoApiException(""));

        suballCommand.execute(ici, new String[0]);

        verify(ici).reply("Something went wrong and I can't find the post comments :<");
    }


    @Test
    public void testNoTaggedContent() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);
        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenReturn(new LinkedList<>());

        suballCommand.execute(ici, new String[0]);

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

        suballCommand.execute(ici, new String[0]);

        verify(ici).reply("You don't have permissions for any of the indicated taglists.");
    }


    @Test
    public void testNoSubscriptionsDetected() throws Exception
    {
        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        LinkedList<Comment> coll = new LinkedList<>();
        coll.add(tagComment);
        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenReturn(coll);

        when(tracker.hasPermission(tlTest0)).thenReturn(true);

        suballCommand.execute(ici, new String[0]);

        verify(ici).reply("Could not find any comments that indicate a subscription request. Is your pattern correct?");
    }

    //endregion

    //region Good weather

    @Test
    public void testSubscribeSingleUser() throws Exception
    {
        Assert.assertNull(UserHandler.getUserByImgurName(TestImgurName));

        when(ici.getTracker()).thenReturn(tracker);
        when(ici.getParentComment()).thenReturn(parentComment);
        when(ici.getImgurId()).thenReturn(TestImgurPost);

        List<Comment> comments = getCollection();

        when(gallery.getItemComments(TestImgurPost, Comment.Sort.Best)).thenReturn(comments);

        when(tracker.hasPermission(tlTest0)).thenReturn(true);

        suballCommand.execute(ici, new String[0]);

        verify(ici).reply("Successfully subscribed 1 users to taglist (test0)");
        Assert.assertNotNull(UserHandler.getUserByImgurName(TestImgurName));
    }

    private List<Comment> getCollection()
    {
        List<Comment> childComments = new LinkedList<>();

        when(subComment.getComment()).thenReturn(".");
        when(subComment.getAuthorName()).thenReturn(TestImgurName);

        childComments.add(subComment);
        childComments.add(parentComment);

        when(rootComment.getChildren()).thenReturn(childComments);
        when(rootComment.getComment()).thenReturn("Reply to this comment to subscribe");

        LinkedList<Comment> coll = new LinkedList<>();
        coll.add(rootComment);
        coll.add(tagComment);

        return coll;
    }

    //endregion
}

























