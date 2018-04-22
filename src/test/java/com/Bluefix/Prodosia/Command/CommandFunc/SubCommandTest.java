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

import com.Bluefix.Prodosia.Command.CommandFunc.Subscription.SubCommand;
import com.Bluefix.Prodosia.Command.CommandRecognition;
import com.Bluefix.Prodosia.Prefix.CommandPrefixStorage;
import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.Prefix.CommandPrefix;
import com.Bluefix.Prodosia.DataType.Command.ImgurCommandInformation;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SubCommandTest
{
    /**
     * The name used for testing.
     *
     * The account will not change its name, so it can be presumed to always
     * be available.
     */
    private static final String TestImgurName = "mashedstew";

    private ICommandFunc subCommand;

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
        subCommand = new SubCommand();

        tlTest0 = new Taglist("test0", "test0 taglist", false);
        tlTest1 = new Taglist("test1", "test1 taglist", true);

        TaglistHandler.handler().set(tlTest0);
        TaglistHandler.handler().set(tlTest1);
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().remove(tlTest0);
        TaglistHandler.handler().remove(tlTest1);


        User u = UserHandler.getUserByImgurName(TestImgurName);

        if (u != null)
            UserHandler.handler().remove(u);
    }


    //region Bad Weather tests

    @Test(expected = IllegalArgumentException.class)
    public void testNoTracker() throws Exception
    {
        subCommand.execute(commandInformation, new String[0]);
    }

    @Test
    public void testNoArguments() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        subCommand.execute(commandInformation, new String[0]);

        verify(commandInformation).reply("Error! The username was not provided.");
    }

    @Test
    public void testNoSubscriptionData() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        String[] arguments = new String[]{TestImgurName};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply("Error! There was no subscription data detected.");
    }

    @Test
    public void testEmptyUsername() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        String[] arguments = new String[]{"@", "test0"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply("Error! The username was not provided.");
    }

    @Test
    public void testNoPermissions() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        String[] arguments = new String[]{TestImgurName, "test0"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "Error! The tracker has no permissions for any of the indicated lists (test0).");
    }


    @Test
    public void testNoRatings() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest1)).thenReturn(true);


        String[] arguments = new String[]{TestImgurName, "test1"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "The following taglists require ratings: (test1)");
    }

    @Test
    public void testNoTaglists() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        String[] arguments = new String[]{TestImgurName, "NotATaglist"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "Error! There was no subscription data detected.");
    }


    @Test
    public void testIncorrectUsername() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);
        String[] arguments = new String[]{"all", "test0"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "Error! User \"all\" could not be stored. Please check the username.");
    }

    //endregion

    //region Foggy weather tests
    // tests that aren't entirely erroneous (they still pass), but partially incorrect.

    @Test
    public void testFwNoRatingSupplied() throws Exception
    {
        User user = UserHandler.getUserByImgurName(TestImgurName);

        Assert.assertTrue(
                user == null ||
                            (user.getSubscription(tlTest0.getId()) == null &&
                             user.getSubscription(tlTest1.getId()) == null));

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);
        when(tracker.hasPermission(tlTest1)).thenReturn(true);
        String[] arguments = new String[]{TestImgurName, "test1", "test0"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "Successfully subscribed user \"" + TestImgurName + "\" to taglists (test0).");

        if (user == null)
            user = UserHandler.getUserByImgurName(TestImgurName);

        Assert.assertNotNull(user.getSubscription(tlTest0.getId()));
        Assert.assertNull(user.getSubscription(tlTest1.getId()));
    }



    //endregion

    //region Good weather tests

    @Test
    public void testNoRatingTaglist() throws Exception
    {
        User user = UserHandler.getUserByImgurName(TestImgurName);

        Assert.assertTrue(
                user == null ||
                        user.getSubscription(tlTest0.getId()) == null
        );

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);
        String[] arguments = new String[]{TestImgurName, "test0"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "Successfully subscribed user \"" + TestImgurName + "\" to taglists (test0).");

        if (user == null)
            user = UserHandler.getUserByImgurName(TestImgurName);

        Assert.assertTrue(user.getSubscription(tlTest0.getId()) != null);
    }


    @Test
    public void testRatingTaglist() throws Exception
    {
        User user = UserHandler.getUserByImgurName(TestImgurName);

        Assert.assertTrue(
                user == null ||
                        user.getSubscription(tlTest1.getId()) == null
        );

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest1)).thenReturn(true);
        String[] arguments = new String[]{TestImgurName, "test1", "s", "q", "e"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "Successfully subscribed user \"" + TestImgurName + "\" to taglists (test1).");

        if (user == null)
            user = UserHandler.getUserByImgurName(TestImgurName);

        UserSubscription us = user.getSubscription(tlTest1.getId());

        Assert.assertNotNull(us);
        Assert.assertTrue(us.hasRating(Rating.SAFE));
        Assert.assertTrue(us.hasRating(Rating.QUESTIONABLE));
        Assert.assertTrue(us.hasRating(Rating.EXPLICIT));
        Assert.assertTrue(us.getFilters() == null || us.getFilters().isEmpty());
    }

    @Test
    public void testFilter() throws Exception
    {
        User user = UserHandler.getUserByImgurName(TestImgurName);

        Assert.assertTrue(
                user == null ||
                        user.getSubscription(tlTest0.getId()) == null
        );

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);
        String[] arguments = new String[]{TestImgurName, "test0", "filter"};

        subCommand.execute(commandInformation, arguments);

        verify(commandInformation).reply(
                "Successfully subscribed user \"" + TestImgurName + "\" to taglists (test0).");

        if (user == null)
            user = UserHandler.getUserByImgurName(TestImgurName);

        UserSubscription us = user.getSubscription(tlTest0.getId());
        Assert.assertNotNull(us);
        Assert.assertTrue(us.getFilters().toLowerCase().contains("filter"));
    }



    //endregion









}