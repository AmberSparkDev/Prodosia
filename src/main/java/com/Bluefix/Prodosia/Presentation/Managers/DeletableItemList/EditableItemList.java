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

package com.Bluefix.Prodosia.Presentation.Managers.DeletableItemList;

import javafx.geometry.Insets;
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
 *
 * It is possible to provide a number of fixed entries. The 0-based entries
 * from `listItems()` cannot be deleted from the list.
 * @param <T>
 */
public abstract class EditableItemList<T extends Control>
{
    /**
     * The primary control that everything will be placed in.
     */
    protected Pane root;

    /**
     * The button to add new entries.
     */
    private Button addButton;

    /**
     * The collection of actual entries.
     */
    private VBox entries;

    /**
     * The amount of fixed entries that cannot be deleted.
     */
    private int fixedEntries;


    protected ArrayList<T> items;


    /**
     * Create a new list with deletable items.
     * @param root The control within which the items will be placed.
     */
    public EditableItemList(Pane root)
    {
        this.root = root;
        this.fixedEntries = 0;

        // initialize the vbox within a scrollpane.
        initializeComponents();

        fill();
    }


    /**
     * Create a new list with deletable items.
     * @param root The control within which the items will be placed.
     * @param fixedEntries The amount of 0-based fixed entries that cannot be removed.
     */
    public EditableItemList(Pane root, int fixedEntries)
    {
        if (fixedEntries < 0)
            throw new IllegalArgumentException("The amount of fixed entries cannot be less than 0");

        this.root = root;
        this.fixedEntries = fixedEntries;

        // initialize the vbox within a scrollpane.
        initializeComponents();

        fill();
    }


    /**
     * Initialize the components within the root control. This method should only be called once.
     */
    private void initializeComponents()
    {
        // initialize a scrollpane that will contain all the contents.
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPadding(new Insets(0));

        scrollPane.prefWidthProperty().bind(this.root.widthProperty());
        scrollPane.prefHeightProperty().bind(this.root.heightProperty());

        scrollPane.setFitToWidth(true);

        // the order of the items is as follows
        // ScrollPane
        //   VBox
        //    - Button
        //    - Line
        //    - VBox
        //      - entries

        VBox layout = new VBox();
        layout.setPadding(new Insets(1));

        // button to add new entries
        this.addButton = new Button();
        this.addButton.setText("Add");
        this.addButton.setOnAction(event -> addEntry());
        layout.getChildren().add(this.addButton);

        // separator
        Separator sep = new Separator();
        sep.setOrientation(Orientation.HORIZONTAL);
        layout.getChildren().add(sep);

        // the actual entries
        this.entries = new VBox();
        this.entries.prefWidthProperty().bind(layout.widthProperty());
        layout.getChildren().add(this.entries);

        scrollPane.setContent(layout);

        this.root.getChildren().add(scrollPane);
    }

    /**
     * Add a new entry to the deletable list by requesting the user to input a new String value.
     */
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
     * Fill the root-pane with deletable items. This method will reset *any* changes made so far.
     */
    private void fill()
    {
        if (root == null)
            return;

        // retrieve the items.
        items = new ArrayList<>(listItems());

        fillFromItems();
    }

    /**
     * This method will reset the contents with what is currently known about the control. This will
     * retain any changes made.
     */
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
            hbox.prefWidthProperty().bind(this.entries.widthProperty());
            item.prefWidthProperty().bind(hbox.widthProperty());

            Button deleteButton = getItemButton(counter);
            if (counter < fixedEntries)
                deleteButton.setDisable(true);

            hbox.getChildren().add(deleteButton);
            hbox.getChildren().add(item);

            counter++;

            entries.getChildren().add(hbox);
        }
    }


    /**
     * Generate a button for the specified index.
     * @param index The index of the item in the list.
     * @return A new button for deletion purposes.
     */
    private Button getItemButton(int index)
    {
        Button button = new Button();

        button.setMaxSize(29, 29);
        button.setPrefSize(29, 29);
        button.setMinSize(29, 29);
        button.setText("-");

        button.setOnAction(event ->
        {
            deleteItem(index);
        });

        return button;
    }

    /**
     * Delete the indicated item from the list.
     * @param index The index of the item to be deleted.
     */
    private void deleteItem(int index)
    {
        this.items.remove(index);
        fillFromItems();
    }

    /**
     * Update the list-manager to retrieve its original items.
     * Be mindful that this undoes *any* edits done to the list so far!
     *
     * Usually necessary after a superclass constructor since the data can potentially not be provided right away.
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
     * Given an input string, create a new control
     * @param data The String value provided by the user.
     * @return A new control item.
     */
    protected abstract T createItem(String data);

}
