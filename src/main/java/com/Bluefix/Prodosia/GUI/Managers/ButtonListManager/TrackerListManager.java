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

package com.Bluefix.Prodosia.GUI.Managers.ButtonListManager;


import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerComparator;
import com.Bluefix.Prodosia.GUI.Managers.ListManager.GuiListManager;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.Tracker.EditTrackerWindow;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A GUI Manager that will keep track of a list of users.
 */
public class TrackerListManager extends GuiListManager<Button>
{


    /**
     * Initialize a new Tracker Manager with a VBox element.
     * Depending on the methods called, this object will manage the items
     * in the vbox.
     * @param vbox The VBox element that is used to display the users.
     */
    public TrackerListManager(VBox vbox) throws Exception
    {
        super(vbox);
    }



    //region GuiListManager implementation

    @Override
    protected Button[] listItems() throws Exception
    {
        // retrieve the tracker data and sort is alphabetically. 
        ArrayList<Tracker> data = TrackerHandler.handler().getAll();
        Collections.sort(data, new TrackerComparator());



        Button[] buttons = new Button[data.size()];

        for (int i = 0; i < data.size(); i++)
        {
            Tracker tr = data.get(i);

            Button button = new Button(tr.getName());
            button.setMaxWidth(Double.MAX_VALUE);
            //button.getStyleClass().add("trackerButton");

            // set button action
            button.setOnAction(event ->
            {
                EditTrackerWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TRACKER_EDIT);
                controller.init(tr);
            });

            buttons[i] = button;
        }

        return buttons;
    }

    //endregion
}
