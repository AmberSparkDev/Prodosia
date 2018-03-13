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
    public GuiCheckboxListManager(Pane root) throws Exception
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
