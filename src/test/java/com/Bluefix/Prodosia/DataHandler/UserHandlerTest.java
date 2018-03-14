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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.*;

public class UserHandlerTest
{
    private UserHandler handler;
    private Taglist taglist;
    private User user;



    @Before
    public void setUp() throws Exception
    {
        taglist = new Taglist(-1,"my_abbreviation", "my_description", true);
        TaglistHandler.handler().add(taglist);

        handler = UserHandler.handler();

        ArrayList<UserSubscription> subData = new ArrayList<>();

        HashSet<Rating> ratings = new HashSet<>();
        ratings.add(Rating.EXPLICIT);
        ratings.add(Rating.QUESTIONABLE);
        ratings.add(Rating.SAFE);

        subData.add(new UserSubscription(taglist, ratings, "filter"));



        user = new User("4a70ab7a-7966-4c44-93b4-49770b74813d", 1, subData.iterator());
    }

    @After
    public void tearDown() throws Exception
    {
        TaglistHandler.handler().remove(taglist);
    }


    @Test
    public void testFunctionalityWithLocalStorage() throws Exception
    {
        handler.setLocalStorage(true);

        ArrayList<User> users = handler.getAll();

        if (users.contains(user))
            fail();

        handler.add(user);
        users = handler.getAll();

        if (!users.contains(user))
            fail();

        handler.remove(user);
        users = handler.getAll();

        if (users.contains(user))
            fail();


    }

    @Test
    public void testFunctionalityWithoutLocalStorage() throws Exception
    {
        handler.setLocalStorage(false);

        ArrayList<User> users = handler.getAll();

        if (users.contains(user))
            fail();

        handler.add(user);
        users = handler.getAll();

        if (!users.contains(user))
            fail();

        handler.remove(user);
        users = handler.getAll();

        if (users.contains(user))
            fail();
    }
}