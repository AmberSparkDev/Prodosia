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

package com.Bluefix.Prodosia.Prefix;

import com.Bluefix.Prodosia.GUI.Managers.DeletableItemList.EditableReadonlyTextfieldList;
import javafx.scene.layout.Pane;

import java.util.LinkedList;

/**
 * Create a new editable list that automatically maintains, edits and stores
 * any changes to the prefix.
 */
public class EditablePrefixList
{
    private EditableReadonlyTextfieldList ertl;
    private CommandPrefix.Type type;

    /**
     * Create a new list for the specified prefix type.
     *
     * @param type the type of prefix this list will maintain.
     * @param root  The room within which the items can be placed.
     */
    public EditablePrefixList(CommandPrefix.Type type, Pane root) throws Exception
    {
        this.type = type;

        initialize(type, root);
    }

    private void initialize(CommandPrefix.Type type, Pane root) throws Exception
    {
        CommandPrefix cp = CommandPrefixStorage.getPrefixForType(type);

        LinkedList<String> items = null;

        if (cp != null)
            items = CommandPrefix.parseitemsFromPattern(cp.getRegex());

        // some of the prefixes have fixed entries that cannot be deleted.
        int fixed = 0;

        switch (type)
        {
            case DISCORD:
                fixed = 1;
                break;
        }

        ertl = new EditableReadonlyTextfieldList(root, items, fixed);
    }


    /**
     * Store the new items and update the list to incorporate these new items.
     */
    public void store() throws Exception
    {
        ertl.store();
        Iterable<String> items = ertl.getOriginalItems();

        String newPattern = CommandPrefix.parsePatternForItems(items);

        CommandPrefix newCp = new CommandPrefix(type, newPattern);

        CommandPrefixStorage.handler().set(newCp);
    }

    /**
     * Reset the entries of the list back to their original.
     */
    public void reset()
    {
        ertl.update();
    }
}
