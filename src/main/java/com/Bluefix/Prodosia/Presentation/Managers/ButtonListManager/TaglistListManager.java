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


import com.Bluefix.Prodosia.Data.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Data.DataType.Taglist.TaglistComparator;
import com.Bluefix.Prodosia.Presentation.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.Presentation.Taglist.EditTaglistWindow;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A GUI Manager that will keep track of a list of users.
 */
public class TaglistListManager extends ButtonListManager
{
    /**
     * Create a new GuiListManager object that is linked to the root pane.
     * This list-manager will instantiate itself by filling the items
     * from `listItems()`
     *
     * @param root The root in which the items will be displayed.
     */
    public TaglistListManager(Pane root) throws Exception
    {
        super(root);
    }


    //region IListManager implementation

    @Override
    protected Iterable<String> listButtonItems() throws Exception
    {
        // retrieve the taglists and sort them.
        ArrayList<Taglist> data = TaglistHandler.handler().getAll();
        data.sort(new TaglistComparator());

        // init the output and taglist map
        LinkedList<String> output = new LinkedList<>();
        this.taglistCollection = new Taglist[data.size()];

        int counter = 0;

        // parse a list with all button items and store the abbreviation for the event handler.
        for (Taglist t : data)
        {
            output.addLast(t.getAbbreviation());
            this.taglistCollection[counter++] = t;
        }

        return output;
    }


    private Taglist[] taglistCollection;

    /**
     * Retrieve the Event Handler for the button. This indicates what the button should do when pressed.
     * @param entry The index of the entry.
     * @return
     */
    @Override
    protected EventHandler<ActionEvent> getEventHandlerForButton(int entry)
    {
        return getEventHandlerForButton(taglistCollection[entry]);
    }

    /**
     * Indicate what a button should do when it is pressed.
     * @param taglist
     * @return
     */
    protected EventHandler<ActionEvent> getEventHandlerForButton(Taglist taglist)
    {
        return event ->
        {
            EditTaglistWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TAGLIST_EDIT);
            controller.init(taglist);
        };
    }


    //endregion
}
