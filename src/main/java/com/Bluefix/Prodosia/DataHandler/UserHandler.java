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
