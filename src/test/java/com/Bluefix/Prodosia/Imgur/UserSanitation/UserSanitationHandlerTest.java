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

package com.Bluefix.Prodosia.Imgur.UserSanitation;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.HashSet;

public class UserSanitationHandlerTest
{
    private static final String TestImgurName = "mashedstew";
    private static final long TestImgurId = 33641050;

    private Taglist taglist;
    private User user;

    @Before
    public void setUp() throws Exception
    {
        taglist = new Taglist("test0", "test0 taglist", false);
        TaglistHandler.handler().set(taglist);

        HashSet<UserSubscription> subData = new HashSet<>();

        HashSet<Rating> ratings = new HashSet<>();
        ratings.add(Rating.ALL);

        subData.add(new UserSubscription(taglist, ratings, "filter"));

        user = new User(TestImgurName, TestImgurId, subData);
        UserHandler.handler().set(user);
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().clear(taglist);
        UserHandler.handler().remove(user);
    }

    @Test
    public void testFetchAnyUser() throws SQLException
    {
        HashSet<Long> users = UserSanitationHandler.fetchUsers(1);

        Assert.assertNotNull(users);
        Assert.assertEquals(1, users.size());
    }


    @Test
    public void testFetchContainsUser() throws SQLException
    {
        int amountOfUsers = UserHandler.handler().getAll().size();

        Assert.assertTrue(amountOfUsers >= 1);

        HashSet<Long> users = UserSanitationHandler.fetchUsers(amountOfUsers);

        Assert.assertTrue(users.contains(TestImgurId));
    }

}