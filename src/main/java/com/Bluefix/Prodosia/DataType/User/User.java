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

import com.Bluefix.Prodosia.DataType.TagRequest;

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

    public User(String imgurName, long imgurId, Iterator<UserSubscription> subscriptionData) throws Exception
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

        while (subscriptionData.hasNext())
        {
            UserSubscription us = subscriptionData.next();

            this.subscriptions.put(us.getTaglist().getId(), us);
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
}


































