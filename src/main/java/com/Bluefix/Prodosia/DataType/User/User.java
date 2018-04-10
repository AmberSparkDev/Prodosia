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

package com.Bluefix.Prodosia.DataType.User;

import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Comments.TagRequest.TagRequest;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.util.BaringoApiException;

import java.util.*;

public class User
{
    //region Variables

    private String imgurName;

    private long imgurId;

    /**
     * A hashmap that maps a taglist-id to the corresponding user subscription.
     */
    private HashMap<Long, UserSubscription> subscriptions;

    //endregion

    //region Constructor

    /**
     * Instantiate a new user where both the name and imgur-id are known.
     * @param imgurName The imgur-name of the user.
     * @param imgurId The imgur-id of the user.
     * @param subscriptionData The subscription-data for the user.
     * @throws Exception
     */
    public User(String imgurName, long imgurId, HashSet<UserSubscription> subscriptionData) throws Exception
    {
        if (imgurName == null || imgurName.trim().isEmpty())
            throw new IllegalArgumentException("The imgur name was empty");

        if (imgurId <= 0)
            throw new IllegalArgumentException("The imgur id was faulty");

        if (subscriptionData == null)
            throw new IllegalArgumentException("No subscription data was provided.");


        this.imgurName = imgurName;
        this.imgurId = imgurId;
        this.subscriptions = new HashMap<>();

        for (UserSubscription us : subscriptionData)
        {
            this.subscriptions.put(us.getTaglist().getId(), us);
        }
    }


    /**
     * If the imgur id of the user is unknown, parse a new user.
     *
     * Will return `null` if the user could not be found.
     * @param imgurName The imgur-name of the user.
     * @param subscriptionData The subscription data of the user.
     * @return
     */
    public static User retrieveUser(String imgurName, HashSet<UserSubscription> subscriptionData) throws Exception
    {
        try
        {
            Account acc = ImgurManager.client().accountService().getAccount(imgurName);

            if (acc == null)
                return null;

            return new User(imgurName, acc.getId(), subscriptionData);

        } catch (BaringoApiException e)
        {
            // the user probably didn't exist. simply return null.
            return null;
        }
    }



    //endregion


    public String getImgurName()
    {
        return imgurName;
    }

    public long getImgurId()
    {
        return imgurId;
    }

    /**
     * Retrieve the UserSubscription data for the specified taglist.
     * @param taglistId The taglist to retrieve the data for.
     * @return The subscription data, or null if this user was not subscribed to the taglist.
     */
    public UserSubscription getSubscription(long taglistId)
    {
        return subscriptions.get(taglistId);
    }

    /**
     * Retrieve all available subscriptions.
     * @return
     */
    public Collection<UserSubscription> getSubscriptions()
    {
        return subscriptions.values();
    }


    //region Equals

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return imgurId == user.imgurId;
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(imgurId);
    }

    //endregion

    //region Tag Request

    /**
     * Returns true iff the user should be added to the tag request.
     * @param tr The tag request in effect.
     * @return true iff the user should be tagged, false otherwise.
     */
    public boolean partOfTagRequest(TagRequest tr)
    {
        // loop through all subscriptions. If any of the subscriptions matches with the tag-request,
        // return true.
        for (UserSubscription us : subscriptions.values())
        {
            if (us.partOf(tr))
                return true;
        }

        // since none of our subscriptions matched the tag request, return false
        return false;
    }

    //endregion

    //region Storage

    /**
     * Store this user object to the database. Merge it with the existing user if necessary.
     */
    public void store() throws Exception
    {
        User other = UserHandler.getUserByImgurId(this.imgurId);

        if (other == null)
        {
            UserHandler.handler().set(this);
        }
        else
        {
            // if there was already a subscription for us in the system, merge and replace.
            User merged = merge(other);

            UserHandler.handler().remove(other);
            UserHandler.handler().set(merged);
        }
    }

    /**
     * Merge Users with the same id together.
     * @param u
     * @return
     * @throws Exception
     */
    private User merge(User u) throws Exception
    {
        if (u == null)
            return this;

        if (u.getImgurId() != this.imgurId)
            throw new IllegalArgumentException("This is an illegal merge.");

        HashSet<UserSubscription> us = new HashSet<>();

        // replace all user-subscriptions from the old user with the ones from the current user.
        for (UserSubscription myUs : u.getSubscriptions())
        {
            // only add the old item if there wasn't a newer version.
            if (!this.subscriptions.containsValue(myUs))
            {
                us.add(myUs);
            }
        }

        for (UserSubscription myUs : this.subscriptions.values())
        {
            us.add(myUs);
        }

        return new User(this.imgurName, this.imgurId, us);

    }

    //endregion

    //region Unsubscription

    /**
     * Unsubscribe the user from the specified taglists.
     * @param taglists
     * @return The taglists that were unsubscribed from.
     */
    public Iterable<String> unsubscribe(HashSet<Taglist> taglists) throws Exception
    {
        HashSet<UserSubscription> remaining = new HashSet<>();
        LinkedList<String> unsubscribed = new LinkedList<>();

        for (UserSubscription us : subscriptions.values())
        {
            if (!taglists.contains(us.getTaglist()))
                remaining.add(us);
            else
                unsubscribed.add(us.getTaglist().getAbbreviation());
        }

        // replace the item or delete the user if no taglists were remaining.
        if (remaining.isEmpty())
        {
            UserHandler.handler().remove(this);
        }
        else
        {
            UserHandler.handler().update(this,
                    new User(this.imgurName, this.imgurId, remaining));
        }

        return unsubscribed;
    }

    //endregion
}


































