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

import com.Bluefix.Prodosia.GUI.Managers.ListManager.GuiListManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * A GUI Manager that allows for a pane with buttons.
 */
public abstract class ButtonListManager extends GuiListManager<Button>
{

    /**
     * Instantiate a new buttonlistmanager.
     * @param root
     * @throws Exception
     */
    public ButtonListManager(Pane root) throws Exception
    {
        super(root);
    }

    /**
     * Retrieve all items that pertain to this list.
     *
     * @return All Region-items that should be included in the Pane, in
     * the proper order.
     */
    @Override
    protected Button[] listItems() throws Exception
    {
        Iterable<String> data = listButtonItems();
        ArrayList<Button> buttons = new ArrayList<>();

        int counter = 0;

        for (String entry : data)
        {
            Button button = new Button(entry);
            button.setMaxWidth(Double.MAX_VALUE);

            // set button action
            button.setOnAction(getEventHandlerForButton(counter++));

            buttons.add(button);
        }

        return buttons.toArray(new Button[0]);
    }


    /**
     * List all the buttons in the collection. Each individual button-item will
     * request the superclass what the action of the button should be through
     * `getEventHandlerForButton`
     * @return A list
     * @throws Exception
     */
    protected abstract Iterable<String> listButtonItems() throws Exception;

    /**
     * Get the event-handler for the button item.
     * @param entry The index of the entry that was provided.
     * @return An event handler for the specified button, or null if no action for the button was specified.
     */
    protected abstract EventHandler<ActionEvent> getEventHandlerForButton(int entry);








}
