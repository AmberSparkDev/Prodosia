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

package com.Bluefix.Prodosia.GUI.Managers.DeletableItemList;

import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Manages a List-manager that will contain deletable items.
 * @param <T>
 */
public abstract class DeletableItemList<T extends Labeled>
{
    protected Pane root;
    private Button addButton;
    private VBox entries;

    protected ArrayList<T> items;


    /**
     * Create a new list with deletable items.
     * @param root The room within which the items can be placed.
     */
    public DeletableItemList(Pane root)
    {
        this.root = root;

        // initialize the vbox within a scrollpane.
        initializeComponents();

        fill();
    }


    private void initializeComponents()
    {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // the order of the items is as follows
        // ScrollPane
        //   VBox
        //    - Button
        //    - Line
        //    - VBox
        //      - entries

        VBox layout = new VBox();

        this.addButton = new Button();
        this.addButton.setText("Add");
        this.addButton.setOnAction(event -> addEntry());
        layout.getChildren().add(this.addButton);

        Separator sep = new Separator();
        sep.setOrientation(Orientation.HORIZONTAL);
        layout.getChildren().add(sep);

        this.entries = new VBox();
        layout.getChildren().add(this.entries);

        scrollPane.setContent(layout);
    }

    private void addEntry()
    {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Add Entry");
        dialog.setHeaderText("Please enter a new entry");
        dialog.setContentText("");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(value ->
        {
            if (value.trim().isEmpty())
                return;

            this.items.add(createItem(value));
            fillFromItems();
        });
    }


    /**
     * Fill the root-pane with deletable items.
     */
    private void fill()
    {
        if (root == null)
            return;

        // retrieve the items.
        items = new ArrayList<T>(listItems());

        fillFromItems();
    }

    private void fillFromItems()
    {
        entries.getChildren().clear();

        if (items == null || items.isEmpty())
            return;

        Iterator<T> tIt = items.iterator();
        int counter = 0;

        while (tIt.hasNext())
        {
            T item = tIt.next();

            HBox hbox = new HBox();
            hbox.getChildren().add(getItemButton(counter++));
            hbox.getChildren().add(item);

            entries.getChildren().add(hbox);
        }
    }


    private Button getItemButton(int index)
    {
        Button button = new Button();
        button.setMaxSize(25, 25);
        button.setPrefSize(25, 25);
        button.setMinSize(25, 25);
        button.setText("-");

        button.setOnAction(event ->
        {
            deleteItem(index);
        });

        return button;
    }

    private void deleteItem(int index)
    {
        this.items.remove(index);
        fillFromItems();
    }

    /**
     * Update the list-manager to retrieve its original items.
     * Be mindful that this undoes *any* edits done to the list so far!
     */
    public void update()
    {
        fill();
    }





    /**
     * Retrieve all items that pertain to this list.
     * @return All region-items that should be included in the pane, in
     * the proper order.
     */
    protected abstract List<T> listItems();

    /**
     * Given an input string for the label, create a new
     * @param data
     * @return
     */
    protected abstract T createItem(String data);

}
