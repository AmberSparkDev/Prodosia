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

import com.Bluefix.Prodosia.Command.CommandHandler;
import com.Bluefix.Prodosia.Command.CommandRecognition;
import com.Bluefix.Prodosia.DataHandler.CommandPrefixStorage;
import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandInformation;
import com.Bluefix.Prodosia.DataType.Command.CommandPrefix;
import com.Bluefix.Prodosia.DataType.Command.ImgurCommandInformation;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Comment;
import com.github.kskelm.baringo.util.BaringoApiException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.*;

public class TagCommandTest
{
    private String prefix;
    private CommandPrefix cPrefix;

    private Taglist tlTest0;
    private Taglist tlTest1;

    private UserSubscription us0;
    private UserSubscription us1;
    private UserSubscription us2;
    private UserSubscription us3;
    private UserSubscription us4;

    private String uName0;
    private String uName1;
    private String uName2;

    private User u0;
    private User u1;
    private User u2;

    private static final long imgurId = 33641050;
    private Tracker myTracker;


    @Before
    public void setUp() throws Exception
    {
        prefix = "!testPrefix ";

        cPrefix = new CommandPrefix(
                CommandPrefix.Type.TEST,
                CommandPrefix.parsePatternForItems(prefix));

        CommandPrefixStorage.handler().add(cPrefix);


        tlTest0 = new Taglist("test0", "test0 taglist", false);
        tlTest1 = new Taglist("test1", "test1 taglist", true);

        TaglistHandler.handler().add(tlTest0);
        TaglistHandler.handler().add(tlTest1);

        ArrayList<UserSubscription> sub0 = new ArrayList<>();
        ArrayList<UserSubscription> sub1 = new ArrayList<>();
        ArrayList<UserSubscription> sub2 = new ArrayList<>();

        HashSet<Rating> ratings = new HashSet<>();
        ratings.add(Rating.SAFE);

        UserSubscription us0 = new UserSubscription(tlTest0, null, null);
        UserSubscription us1 = new UserSubscription(tlTest0, null, "cows");
        UserSubscription us2 = new UserSubscription(tlTest0, ratings, null);
        UserSubscription us3 = new UserSubscription(tlTest1, ratings, null);
        UserSubscription us4 = new UserSubscription(tlTest1, null, null);
        UserSubscription us5 = new UserSubscription(tlTest1, ratings, "cows");

        sub0.add(us0);
        sub0.add(us3);
        sub1.add(us1);
        sub1.add(us4);
        sub2.add(us2);
        sub2.add(us5);

        uName0 = "mashedstew";
        uName1 = "BloomingRose";
        uName2 = "MisterThree";

        long u0Id = 33641050;
        long u1Id = 58590281;
        long u2Id = 13920225;

        u0 = new User(uName0, u0Id, sub0.iterator());
        u1 = new User(uName1, u1Id, sub1.iterator());
        u2 = new User(uName2, u2Id, sub2.iterator());

        UserHandler.handler().add(u0);
        UserHandler.handler().add(u1);
        UserHandler.handler().add(u2);


        myTracker = TrackerHandler.getTrackerByImgurId(imgurId);






    }

    @After
    public void tearDown() throws Exception
    {
        CommandPrefixStorage.handler().remove(cPrefix);

        TaglistHandler.handler().remove(tlTest0);
        TaglistHandler.handler().remove(tlTest1);

        UserHandler.handler().remove(u0);
        UserHandler.handler().remove(u1);
        UserHandler.handler().remove(u2);
    }


    /**
     * Test 0
     *
     * Expected to tag mashedstew and misterthree
     */
    @Test
    public void testTagWithRatingList() throws Exception
    {
        String command = cPrefix + "tag test1 s";

        long parentId = 1301573497;

        executeCommand(command, parentId);
    }



    private void executeCommand(String command, long parentId) throws BaringoApiException, IOException, URISyntaxException
    {
        Comment comment = ImgurManager.client().commentService().getComment(parentId);

        CommandInformation ci =
                new ImgurCommandInformation(myTracker, comment);

        CommandRecognition.executeEntry(CommandPrefix.Type.TEST, ci, command);
    }






}