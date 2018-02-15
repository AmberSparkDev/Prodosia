/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.GUI.Managers.ButtonListManager;


import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.User;
import com.Bluefix.Prodosia.GUI.Managers.ListManager.GuiListManager;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.User.EditUserWindow;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * A GUI Manager that will keep track of a list of users.
 */
public class UserListManager extends GuiListManager<Button>
{


    /**
     * Initialize a new Userlist Manager with a VBox element.
     * Depending on the methods called, this object will manage the items
     * in the vbox.
     * @param vbox The VBox element that is used to display the users.
     */
    public UserListManager(VBox vbox)
    {
        super(vbox);
    }


    //region IListManager implementation

    @Override
    protected Button[] listItems()
    {
        User[] data = UserHandler.getUsersSorted();
        Button[] buttons = new Button[data.length];

        for (int i = 0; i < data.length; i++)
        {
            User u = data[i];

            Button button = new Button(u.getImgurName());
            button.setMaxWidth(Double.MAX_VALUE);
            //button.getStyleClass().add("trackerButton");

            // set button action
            button.setOnAction(event ->
            {
                EditUserWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.USER_EDIT);
                controller.initialize(u);
            });

            buttons[i] = button;
        }

        return buttons;
    }

    //endregion
}
