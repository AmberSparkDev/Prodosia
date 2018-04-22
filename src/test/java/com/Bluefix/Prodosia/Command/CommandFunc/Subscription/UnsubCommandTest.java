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
import com.Bluefix.Prodosia.Command.CommandFunc.Subscription.UnsubCommand;
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
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnsubCommandTest
{
    private static final String TestImgurName = "mashedstew";
    private static final long TestImgurId = 33641050;

    private ICommandFunc unsubCommand;

    private Taglist tlTest0;
    private Taglist tlTest1;

    // some useful usersubscriptions for us
    UserSubscription us0;
    UserSubscription us1;


    @Mock
    CommandInformation commandInformation;

    @Mock
    Tracker tracker;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();



    @Before
    public void setUp() throws Exception
    {
        unsubCommand = new UnsubCommand();

        tlTest0 = new Taglist("test0", "test0 taglist", false);
        tlTest1 = new Taglist("test1", "test1 taglist", true);

        TaglistHandler.handler().set(tlTest0);
        TaglistHandler.handler().set(tlTest1);

        HashSet<Rating> nonRatingRatings = new HashSet<>();
        nonRatingRatings.add(Rating.ALL);

        HashSet<Rating> ratings = new HashSet<>();
        ratings.add(Rating.SAFE);
        ratings.add(Rating.QUESTIONABLE);
        ratings.add(Rating.EXPLICIT);

        us0 = new UserSubscription(tlTest0, nonRatingRatings, null);
        us1 = new UserSubscription(tlTest1, ratings, null);
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


    //region Bad Weather

    @Test(expected = IllegalArgumentException.class)
    public void testNoTracker() throws Exception
    {
        unsubCommand.execute(commandInformation, new String[0]);
    }

    @Test
    public void testNoUsernameProvided() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        unsubCommand.execute(commandInformation, new String[0]);

        verify(commandInformation).reply("Error! No username was given.");
    }

    @Test
    public void testUserNotFound() throws Exception
    {
        when(commandInformation.getTracker()).thenReturn(tracker);

        // the name should by default not be part of the system.
        // if it is, the error lies most likely elsewhere. Perhaps a test that doesn't
        // properly clean it up after itself.
        unsubCommand.execute(commandInformation, new String[] {TestImgurName});

        verify(commandInformation).reply("Error! The user was not found in any taglist.");
    }

    @Test
    public void testNoPermission() throws Exception
    {
        User u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNull(u);

        HashSet<UserSubscription> subs = new HashSet<>();
        subs.add(us0);
        u = new User(TestImgurName, TestImgurId, subs);

        UserHandler.handler().set(u);

        // ---

        when(commandInformation.getTracker()).thenReturn(tracker);

        unsubCommand.execute(commandInformation, new String[] {TestImgurName});

        verify(commandInformation).reply("Error! The user was not allowed to unsubscribe from these taglists.");

        // ---

        u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNotNull(u);
        Assert.assertNotNull(u.getSubscription(tlTest0.getId()));
    }


    @Test
    public void testArgumentsNotTaglists() throws Exception
    {
        User u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNull(u);

        HashSet<UserSubscription> subs = new HashSet<>();
        subs.add(us0);
        u = new User(TestImgurName, TestImgurId, subs);

        UserHandler.handler().set(u);

        // ---

        when(commandInformation.getTracker()).thenReturn(tracker);

        unsubCommand.execute(commandInformation, new String[] {TestImgurName, "NotATaglist"});

        verify(commandInformation).reply("Error! None of the provided taglists were recognized.");

    }


    @Test
    public void testNoTaglistIntersection() throws Exception
    {
        User u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNull(u);

        HashSet<UserSubscription> subs = new HashSet<>();
        subs.add(us0);
        u = new User(TestImgurName, TestImgurId, subs);

        UserHandler.handler().set(u);

        // ---

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);
        when(tracker.hasPermission(tlTest1)).thenReturn(true);

        unsubCommand.execute(commandInformation, new String[] {TestImgurName, "test1"});

        verify(commandInformation).reply("Notice: The user was not part of any of these taglists.");

        // ---

        Assert.assertNotNull(UserHandler.getUserByImgurName(TestImgurName));
    }


    //endregion

    //region Good Weather

    @Test
    public void testUnsubscribeUserPlainly() throws Exception
    {
        User u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNull(u);

        HashSet<UserSubscription> subs = new HashSet<>();
        subs.add(us0);
        u = new User(TestImgurName, TestImgurId, subs);

        UserHandler.handler().set(u);

        // ---

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);

        unsubCommand.execute(commandInformation, new String[] {TestImgurName});

        verify(commandInformation).reply("Successfully unsubscribed user \"" + TestImgurName + "\" from taglists (test0).");

        // ---

        Assert.assertNull(UserHandler.getUserByImgurName(TestImgurName));
    }

    @Test
    public void testUnsubscribeAndRetainUser() throws Exception
    {
        User u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNull(u);

        HashSet<UserSubscription> subs = new HashSet<>();
        subs.add(us0);
        subs.add(us1);
        u = new User(TestImgurName, TestImgurId, subs);

        UserHandler.handler().set(u);

        // ---

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);
        when(tracker.hasPermission(tlTest1)).thenReturn(true);

        unsubCommand.execute(commandInformation, new String[] {TestImgurName, "test1"});

        verify(commandInformation).reply("Successfully unsubscribed user \"" + TestImgurName + "\" from taglists (test1).");

        // ---

        u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNotNull(u);
        Assert.assertNotNull(u.getSubscription(tlTest0.getId()));
        Assert.assertNull(u.getSubscription(tlTest1.getId()));
    }

    @Test
    public void testUnsubscribeUserExplicitly() throws Exception
    {
        User u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNull(u);

        HashSet<UserSubscription> subs = new HashSet<>();
        subs.add(us0);
        subs.add(us1);
        u = new User(TestImgurName, TestImgurId, subs);

        UserHandler.handler().set(u);

        // ---

        when(commandInformation.getTracker()).thenReturn(tracker);
        when(tracker.hasPermission(tlTest0)).thenReturn(true);
        when(tracker.hasPermission(tlTest1)).thenReturn(true);

        unsubCommand.execute(commandInformation, new String[]{TestImgurName, "test0", "test1"});

        verify(commandInformation).reply("Successfully unsubscribed user \"" + TestImgurName + "\" from taglists (test0, test1).");

        // ---

        Assert.assertNull(UserHandler.getUserByImgurName(TestImgurName));
    }
    //endregion















}