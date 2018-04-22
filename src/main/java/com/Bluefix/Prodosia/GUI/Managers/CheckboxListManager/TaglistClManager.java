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

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Taglist.TaglistComparator;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class TaglistClManager extends GuiCheckboxListManager
{
    /**
     * Create a new GuiListManager object that is linked to the root pane.
     * This list-manager will instantiate itself by filling the items
     * from `listItems()`
     *
     * @param root The root in which the items will be displayed.
     */
    public TaglistClManager(Pane root) throws Exception
    {
        super(root);
    }

    @Override
    protected String[] listOptions() throws Exception
    {
        ArrayList<Taglist> taglists = TaglistHandler.handler().getAll();

        if (taglists == null || taglists.size() <= 0)
            return null;

        // first sort the items alphabetically
        taglists.sort(new TaglistComparator());

        String[] out = new String[taglists.size()];

        for (int i = 0; i < taglists.size(); i++)
        {
            out[i] = taglists.get(i).getAbbreviation();
        }

        return out;
    }


    /**
     * This method will select any entries in the list that correspond with the taglists in the hashset.
     * @param taglists
     */
    public void applyTaglists(HashSet<Taglist> taglists)
    {
        if (super.items == null)
            return;

        for (CheckBox  cb : super.items)
        {
            cb.setSelected(false);

            boolean found = false;
            Iterator<Taglist> tIt = taglists.iterator();

            while (tIt.hasNext() && !found)
            {
                if (tIt.next().getAbbreviation().equals(cb.getText()))
                {
                    cb.setSelected(true);
                    found = true;
                }
            }
        }
    }
}
