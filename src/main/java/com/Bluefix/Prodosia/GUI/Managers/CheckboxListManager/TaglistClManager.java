/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist;
import javafx.scene.layout.Pane;

public class TaglistClManager extends GuiCheckboxListManager
{
    /**
     * Create a new GuiListManager object that is linked to the root pane.
     * This list-manager will instantiate itself by filling the items
     * from `listItems()`
     *
     * @param root The root in which the items will be displayed.
     */
    public TaglistClManager(Pane root)
    {
        super(root);
    }

    @Override
    protected String[] listOptions()
    {
        Taglist[] taglists = TaglistHandler.getTaglistsSorted();

        if (taglists == null || taglists.length <= 0)
            return null;

        String[] out = new String[taglists.length];

        for (int i = 0; i < taglists.length; i++)
        {
            out[i] = taglists[i].getAbbreviation();
        }

        return out;
    }
}
