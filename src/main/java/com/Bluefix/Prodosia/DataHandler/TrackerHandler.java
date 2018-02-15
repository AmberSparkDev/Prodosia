package com.Bluefix.Prodosia.DataHandler;


import com.Bluefix.Prodosia.DataType.Tracker.Tracker;

/**
 * Handler for Tracker management.
 */
public class TrackerHandler
{




    public static Tracker[] getAllTrackers()
    {
        // TODO: Link method to SQLite database.
        Tracker t0 = new Tracker("name0", 1, null,1234, 1, null);
        Tracker t1 = new Tracker("name1asdfasdfasdfasdf", 2, "",1234, 2, null);
        Tracker t2 = new Tracker("name2", 3, "",1234, 3, null);
        Tracker t3 = new Tracker("name3", 4, "",1234, 4, null);
        Tracker t4 = new Tracker("name4", 5, "",1234, 5, null);
        Tracker t5 = new Tracker("name5", 6, "",1234, 6, null);
        Tracker t6 = new Tracker("name6", 7, "",1234, 7, null);
        Tracker t7 = new Tracker("name7", 7, "",1234, 7, null);
        Tracker t8 = new Tracker("name8", 7, "",1234, 7, null);
        Tracker t9 = new Tracker("name9", 7, "",1234, 7, null);
        Tracker t10 = new Tracker("name10", 7, "",1234, 7, null);
        Tracker t11 = new Tracker("name11", 7, "",1234, 7, null);
        Tracker t12 = new Tracker("name12", 7, "",1234, 7, null);
        Tracker t13 = new Tracker("name13", 7, "",1234, 7, null);
        Tracker t14 = new Tracker("name14", 7, "",1234, 7, null);
        Tracker t15 = new Tracker("name15", 7, "",1234, 7, null);
        Tracker t16 = new Tracker("name16", 7, "",1234, 7, null);
        Tracker t17 = new Tracker("name17", 7, "",1234, 7, null);

        return new Tracker[]{t0, t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17};
    }



    public static void deleteTracker(Tracker tracker)
    {
        // TODO: delete tracker from local storage and perm storage.
        // TODO: update the tracker-list.
    }


    public static void UpdateTracker(Tracker oldTracker, Tracker newTracker)
    {
        // if the old tracker isn't null, delete it from the system.
        if (oldTracker != null)
        {
            // TODO: delete old tracker from system.
        }

        // TODO: add the new tracker to the system.
    }
}
