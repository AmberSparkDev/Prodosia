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

import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TrackerHandlerTest
{

    private Tracker tracker;

    private TrackerPermissions perm;

    @Before
    public void setUp() throws Exception
    {
        perm = new TrackerPermissions(TrackerPermissions.TrackerType.ADMIN);

        tracker = new Tracker(
                "imgurname",
                1,
                "discordname",
                1234,
                2,
                perm);


    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void testFunctionality() throws Exception
    {
        ArrayList<Tracker> trackers = TrackerHandler.getTrackers();

        if (trackers.contains(tracker))
            fail();

        TrackerHandler.addTracker(tracker);
        trackers = TrackerHandler.getTrackers();

        if (!trackers.contains(tracker))
            fail();

        TrackerHandler.removeTracker(tracker);
        trackers = TrackerHandler.getTrackers();

        if (trackers.contains(tracker))
            fail();


    }
}