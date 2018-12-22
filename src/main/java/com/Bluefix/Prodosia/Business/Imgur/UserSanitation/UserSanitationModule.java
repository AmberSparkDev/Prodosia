/*
 * Copyright (c) 2018 RoseLaLuna
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

package com.Bluefix.Prodosia.Business.Imgur.UserSanitation;

import com.Bluefix.Prodosia.Data.DataHandler.UserHandler;
import com.Bluefix.Prodosia.Data.DataType.User.User;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ApiDistribution;
import com.Bluefix.Prodosia.Business.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Business.Module.ImgurIntervalRunner;
import com.github.kskelm.baringo.model.Account;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

public class UserSanitationModule extends ImgurIntervalRunner
{
    //region Singleton and constructor

    private static UserSanitationModule me;

    public static UserSanitationModule handler()
    {
        if (me == null)
            me = new UserSanitationModule();

        return me;
    }

    public UserSanitationModule()
    {
        super(ApiDistribution.SanitationModule);

        this.processedUser = false;
    }

    //endregion

    private boolean processedUser;

    /**
     * Execute a single cycle.
     */
    @Override
    protected void run()
    {
        processedUser = false;

        // fetch the next user.
        HashSet<Long> users = null;
        try
        {
            users = UserSanitationHandler.fetchUsers(1);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        // if there were no users, skip.
        if (users == null || users.size() <= 0)
            return;

        assert(users.size() == 1);

        // try to retrieve the user from the system.
        User u;
        try
        {

            u = UserHandler.getUserByImgurId(new ArrayList<>(users).get(0));
        } catch (SQLException e)
        {
            e.printStackTrace();
            return;
        }

        // if the user wasn't part of the system anymore, skip
        if (u == null)
            return;

        // fetch the user-name from Imgur
        processedUser = true;
        Account acc;

        try
        {
            acc = ImgurManager.client().accountService().getAccount(u.getImgurId());
        } catch (Exception e)
        {
            // an exception is annoying, but could indicate something simple (like
            // the Imgur servers being overloaded). As such, it shouldn't be considered
            // decisive (however, the API request should be considered used nonetheless).
            return;
        }

        // if the Imgur-name is the same, return
        if (acc.getUserName().equals(u.getImgurName()))
            return;

        // update the user.
        User newUser;

        try
        {
            newUser = new User(
                    acc.getUserName(),
                    u.getImgurId(),
                    new HashSet<>(u.getSubscriptions()));
        } catch (SQLException e)
        {
            e.printStackTrace();
            return;
        }

        // store the new user and make it replace the old one.
        try
        {
            UserHandler.handler().update(u, newUser);
            System.out.println("Updated user \"" + u.getImgurName() + "\" -> \"" + newUser.getImgurName() + "\"");
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Indicate the total amount of GET requests that were executed during the last cycle
     *
     * @return The maximum amount of GET requests.
     */
    @Override
    protected int projectedRequests()
    {
        // if a user was processed, return 1
        return (this.processedUser ? 1 : 0);
    }
}
