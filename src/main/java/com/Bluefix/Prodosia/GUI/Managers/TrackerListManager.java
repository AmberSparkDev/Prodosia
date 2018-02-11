/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.GUI.Managers;


import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.Tracker;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.Tracker.EditTrackerWindow;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

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
    public TrackerListManager(VBox vbox)
    {
        super(vbox);
    }



    //region GuiListManager implementation

    @Override
    protected Button[] listItems()
    {
        Tracker[] data = TrackerHandler.getAllTrackers();
        Button[] buttons = new Button[data.length];

        for (int i = 0; i < data.length; i++)
        {
            Tracker tr = data[i];

            Button button = new Button(tr.getName());
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("trackerButton");

            // set button action
            button.setOnAction(event ->
            {
                EditTrackerWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TRACKER_EDIT);
                controller.initialize(tr);
            });

            buttons[i] = button;
        }

        return buttons;
    }

    //endregion
}
