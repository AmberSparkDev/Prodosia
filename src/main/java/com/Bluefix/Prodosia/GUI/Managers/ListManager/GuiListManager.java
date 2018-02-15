/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
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
    public GuiListManager(Pane root)
    {
        this.root = root;

        fill();
    }



    /**
     * Fill the root-pane with the items that we retrieve from the subclass.
     */
    private void fill()
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
    protected abstract T[] listItems();



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
