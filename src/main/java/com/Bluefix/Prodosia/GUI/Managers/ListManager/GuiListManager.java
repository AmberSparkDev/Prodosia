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

package com.Bluefix.Prodosia.GUI.Managers.ListManager;

import javafx.scene.control.Labeled;
import javafx.scene.layout.Pane;

import java.util.regex.Pattern;

/**
 * Interface for a list manager.
 */
public abstract class GuiListManager<T extends Labeled>
{
    protected Pane root;
    protected T[] items;

    protected double itemHeight;


    /**
     * Create a new GuiListManager object that is linked to the root pane.
     * This list-manager will instantiate itself by filling the items
     * from `listItems()`
     * @param root The root in which the items will be displayed.
     */
    public GuiListManager(Pane root) throws Exception
    {
        this.root = root;

        fill();
    }



    /**
     * Fill the root-pane with the items that we retrieve from the subclass.
     */
    private void fill() throws Exception
    {
        root.getChildren().clear();

        // retrieve the items.
        items = listItems();

        if (items == null || items.length <= 0)
            return;

        for (T item : items)
        {
            root.getChildren().add(item);
        }

        // set the default item height
        itemHeight = items[0].getPrefHeight();
    }

    /**
     * Retrieve all items that pertain to this list.
     * @return All Region-items that should be included in the Pane, in
     * the proper order.
     */
    protected abstract T[] listItems() throws Exception;



    /**
     * Temporarily filter the items in the list based on whether they
     * match the expression.
     * @param regexp the Pattern expression to match.
     */
    public void filter(String regexp)
    {
        if (this.items == null)
            return;

        Pattern pat = Pattern.compile(".*" + regexp + ".*");


        for (T t : items)
        {
            String text = t.getText();
            boolean match = pat.matcher(text).matches();

            // if the button text does not match, hide the button
            if (match)
            {
                t.setDisable(false);
                t.setVisible(true);
                t.setMaxHeight(itemHeight);
                t.setPrefHeight(itemHeight);
                t.setMinHeight(itemHeight);
            }
            else
            {
                t.setDisable(true);
                t.setVisible(false);
                t.setMaxHeight(0.0);
                t.setPrefHeight(0.0);
                t.setMinHeight(0.0);
            }
        }
    }
}
