/*
 * Copyright (c) 2018 RoseLaLuna
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

package com.Bluefix.Prodosia.Presentation.Managers.DeletableItemList;

import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.LinkedList;
import java.util.List;

/**
 * Create a new editable list with Label entries.
 */
public class EditableReadonlyTextfieldList extends EditableItemList<TextField>
{
    private Iterable<String> items;

    /**
     * Create a new list with deletable items.
     *
     * @param root The room within which the items can be placed.
     */
    public EditableReadonlyTextfieldList(Pane root, Iterable<String> items)
    {
        super(root);

        this.items = items;
        super.update();
    }


    /**
     * Create a new list with deletable items.
     *
     * @param root The room within which the items can be placed.
     * @param items The items that should be displayed by default.
     * @param fixedEntries The amount of 0-based entries that should not be deletable.
     */
    public EditableReadonlyTextfieldList(Pane root, Iterable<String> items, int fixedEntries)
    {
        super(root, fixedEntries);

        this.items = items;
        super.update();
    }



    /**
     * Retrieve all items that pertain to this list.
     *
     * @return All region-items that should be included in the pane, in
     * the proper order.
     */
    @Override
    protected List<TextField> listItems()
    {
        LinkedList<TextField> output = new LinkedList<>();

        if (this.items == null)
            return output;

        for (String i : items)
        {
            TextField newItem = new TextField(i);
            newItem.setMaxWidth(Double.MAX_VALUE);
            //newItem.setPrefWidth(Double.MAX_VALUE);
            newItem.setEditable(false);

            output.add(newItem);
        }

        return output;
    }

    /**
     * Given an input string for the label, create a new
     *
     * @param data
     * @return
     */
    @Override
    protected TextField createItem(String data)
    {
        TextField output = new TextField(data);
        output.setMaxWidth(Double.MAX_VALUE);
        output.setEditable(false);
        return output;
    }


    /**
     * Retrieve the original items from the editable list, the ones that weren't altered yet.
     * @return An iterable object containing the original items before the last store-call.
     */
    public Iterable<String> getOriginalItems()
    {
        return this.items;
    }


    public Iterable<String> getItems()
    {
        LinkedList<String> items = new LinkedList<>();

        if (super.items == null)
            return items;

        for (TextField t : super.items)
            items.add(t.getText());

        return items;
    }

    /**
     * Override our items with the items from the editable list, thus finalizing them.
     */
    public void store()
    {
        this.items = getItems();
    }
}
