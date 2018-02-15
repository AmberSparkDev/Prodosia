/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager;

import com.Bluefix.Prodosia.GUI.Managers.ListManager.GuiListManager;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

/**
 * Abstract helper class that can create and maintain a list of
 * checkboxes. Contains helpers functions to keep track of the
 * checkboxes that are selected.
 */
public abstract class GuiCheckboxListManager extends GuiListManager<CheckBox>
{


    /**
     * Create a new GuiListManager object that is linked to the root pane.
     * This list-manager will instantiate itself by filling the items
     * from `listItems()`
     *
     * @param root The root in which the items will be displayed.
     */
    public GuiCheckboxListManager(Pane root)
    {
        super(root);
    }


    /**
     * Retrieve the items that are selected.
     * @return
     */
    public String[] getSelectedItems()
    {
        ArrayList<String> out = new ArrayList<>();

        for (CheckBox cb : super.items)
        {
            if (cb.isSelected())
            {
                out.add(cb.getText());
            }
        }

        String[] outArr = new String[out.size()];

        return out.toArray(outArr);
    }


    /**
     * Fill a list of checkboxes.
     * @return
     */
    @Override
    protected CheckBox[] listItems()
    {
        String[] options = listOptions();

        if (options == null || options.length <= 0)
            return null;

        CheckBox[] out = new CheckBox[options.length];

        for (int i = 0; i < options.length; i++)
        {
            CheckBox cb = new CheckBox(options[i]);
            cb.setMaxWidth(Double.MAX_VALUE);

            out[i] = cb;
        }

        return out;
    }

    /**
     * Retrieve all the options available for this checkbox-list.
     * @return
     */
    protected abstract String[] listOptions();



}
