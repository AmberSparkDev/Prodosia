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

import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
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

import static org.junit.Assert.*;

public class UserHandlerTest extends DataHandlerTest<User>
{
    private static final String TestImgurName = "mashedstew";
    private static final long TestImgurId = 33641050;


    private Taglist taglist;
    private User user;


    //region abstract method implementation


    public UserHandlerTest(boolean useLocalStorage)
    {
        super(useLocalStorage);
    }

    @Override
    protected LocalStorageHandler<User> getHandler()
    {
        return UserHandler.handler();
    }

    @Override
    protected User getItem()
    {
        return user;
    }


    //endregion



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
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().remove(taglist);
        UserHandler.handler().remove(user);
    }



    @Test
    public void testGetUserByImgurId() throws SQLException, LoginException, IOException, BaringoApiException, URISyntaxException
    {
        User u = UserHandler.getUserByImgurId(TestImgurId);
        Assert.assertNull(u);

        UserHandler.handler().set(user);

        u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertEquals(user, u);
    }


    @Test
    public void testGetUserByImgurName() throws SQLException, LoginException, IOException, BaringoApiException, URISyntaxException
    {
        User u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertNull(u);

        UserHandler.handler().set(user);

        u = UserHandler.getUserByImgurName(TestImgurName);
        Assert.assertEquals(user, u);
    }


}