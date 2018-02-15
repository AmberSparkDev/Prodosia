/*
 * Copyright (c) 2018 Bas Boellaard
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


import com.Bluefix.Prodosia.DataType.User;

import java.util.Arrays;
import java.util.Comparator;

public class UserHandler
{

    /**
     * retrieve all users in the system.
     * @return All users in the system.
     */
    public static User[] getUsers()
    {
        User u0 = new User("user 0", 1, null);

        return new User[]{u0};
    }

    /**
     * retrieve all the taglists sorted.
     * @return All taglists ordered by their abbreviation.
     */
    public static User[] getUsersSorted()
    {
        User[] users = getUsers();
        Arrays.sort(users, new UserComparator());

        return users;
    }

    /**
     * Comparator class for taglists. Compares by abbreviation.
     */
    static class UserComparator implements Comparator<User>
    {
        @Override
        public int compare(User o1, User o2)
        {
            return o1.getImgurName().compareTo(o2.getImgurName());
        }
    }
}
