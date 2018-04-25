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

package com.Bluefix.Prodosia.GUI.Managers.ListManager;

import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.regex.Pattern;

/**
 * Interface for a list manager.
 */
public abstract class GuiListManager<T extends Labeled> implements AutoCloseable
{
    protected Pane root;
    private VBox guiEntries;
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
     * Create a new empty GuiListManager.
     *
     * Will start functioning properly after `setRoot` has been set.
     */
    protected GuiListManager()
    {

    }

    protected void setRoot(Pane root) throws Exception
    {
        this.root = root;
        fill();
    }




    private void initializeVbox()
    {
        this.guiEntries = new VBox();
        this.guiEntries.setMaxWidth(Double.MAX_VALUE);

        if (this.root != null)
        {
            this.root.getChildren().clear();
            this.root.getChildren().add(this.guiEntries);
        }
        this.guiEntries.minWidthProperty().bind(this.root.widthProperty());
        this.guiEntries.maxWidthProperty().bind(this.root.maxWidthProperty());
    }


    private void myDereference()
    {
        dereference();

        this.guiEntries.getChildren().clear();
        this.guiEntries = null;
    }



    /**
     * Fill the root-pane with the items that we retrieve from the subclass.
     */
    private void fill() throws Exception
    {
        if (guiEntries != null)
        {
            // dereference the items we had.
            myDereference();
        }

        // retrieve the items.
        items = listItems();

        if (items == null || items.length <= 0)
            return;

        // and we as well will clear the entries.
        initializeVbox();




        for (T item : items)
        {
            guiEntries.getChildren().add(item);
            item.minWidthProperty().bind(this.guiEntries.widthProperty());
        }

        // set the default item height
        itemHeight = items[0].getPrefHeight();

        guiEntries.layout();
        root.layout();
    }

    /**
     * Retrieve all items that pertain to this list.
     * @return All Region-items that should be included in the Pane, in
     * the proper order.
     */
    protected abstract T[] listItems() throws Exception;


    /**
     * Requests the underlying subclass to dereference the current items.
     */
    protected abstract void dereference();


    private String lastFilter;

    /**
     * Temporarily filter the items in the list based on whether they
     * match the expression.
     * @param regexp the Pattern expression to match.
     */
    public void filter(String regexp)
    {
        this.lastFilter = regexp;

        if (this.items == null || this.items.length == 0)
            return;

        Pattern pat = getFilterPattern(regexp);


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

    protected Pattern getFilterPattern(String regexp)
    {
        return Pattern.compile("(?i).*" + regexp + ".*");
    }



    /**
     * Update the list manager by re-initializing its components.
     */
    public void update() throws Exception
    {
        if (this.root == null)
            return;

        fill();

        if (this.lastFilter != null)
            filter(this.lastFilter);
    }


    @Override
    public void close()
    {
        if (this.guiEntries != null)
            this.guiEntries.getChildren().clear();

        this.root = null;
        this.guiEntries = null;
    }
}
