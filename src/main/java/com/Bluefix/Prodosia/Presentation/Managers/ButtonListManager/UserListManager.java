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

package com.Bluefix.Prodosia.Presentation.Managers.ButtonListManager;


import com.Bluefix.Prodosia.Data.DataHandler.UserHandler;
import com.Bluefix.Prodosia.Data.DataType.User.User;
import com.Bluefix.Prodosia.Presentation.Managers.ListManager.LimitedGuiListManager;
import com.Bluefix.Prodosia.Presentation.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.Presentation.User.EditUserWindow;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

/**
 * A GUI Manager that will keep track of a list of users.
 */
public class UserListManager extends LimitedGuiListManager<Button>
{
    private Button[] buttonItems;

    /**
     * The maximum amount of users that can be visible at a time,
     * before they need to be filtered.
     */
    private static final int MaximumUsersVisible = 50;


    /**
     * Initialize a new Userlist Manager with a VBox element.
     * Depending on the methods called, this object will manage the items
     * in the vbox.
     * @param vbox The VBox element that is used to display the users.
     */
    public UserListManager(VBox vbox) throws Exception
    {
        super(vbox, MaximumUsersVisible);
    }


    //region IListManager implementation

    @Override
    protected Button[] listItems() throws Exception
    {
        ArrayList<User> data = UserHandler.handler().getAll();
        Button[] buttons = new Button[data.size()];

        for (int i = 0; i < data.size(); i++)
        {
            User u = data.get(i);

            Button button = new Button(u.getImgurName());
            button.setMaxWidth(Double.MAX_VALUE);
            //button.getStyleClass().add("trackerButton");

            // set button action
            button.setOnAction(event ->
            {
                EditUserWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.USER_EDIT);

                try
                {
                    controller.init(u);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            });

            buttons[i] = button;
        }

        this.buttonItems = buttons;

        return buttons;
    }

    /**
     * Requests the underlying subclass to dereference the current items.
     */
    @Override
    protected void dereference()
    {
        if (this.buttonItems == null || this.buttonItems.length == 0)
            return;

        for (Button b : this.buttonItems)
            b.setOnAction(null);

        this.buttonItems = null;
    }

    //endregion
}
