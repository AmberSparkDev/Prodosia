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

package com.Bluefix.Prodosia.Presentation.Managers.ListManager;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.regex.Pattern;

/**
 * This class puts a limitation on the amount of items that can be displayed.
 */
public abstract class LimitedGuiListManager<T extends Labeled> extends GuiListManager<T>
{
    private Pane root;
    private Label selectedLabel;
    private Label infoLabel;
    private AnchorPane content;

    private int maximumItems;


    /**
     * Create a new GuiListManager object that is linked to the root pane.
     * This list-manager will instantiate itself by filling the items
     * from `listItems()`
     *
     * @param root The root in which the items will be displayed.
     * @param maximumItems The maximum amount of items displayed at a time.
     */
    public LimitedGuiListManager(Pane root, int maximumItems) throws Exception
    {
        super();

        this.maximumItems = maximumItems;
        this.root = root;
        initializeGraphics();

        super.setRoot(this.content);
        this.selectedLabel.setText("0 selected");
        this.infoLabel.setText("Use filter to show.");

        // initialize our filter method with an empty filter.
        this.filter("");
    }


    /**
     * Create the JavaFX components needed to host this.
     */
    private void initializeGraphics()
    {
        // create a new VBox to keep our components in
        VBox vbox = new VBox();

        // create the selection info label and add it.
        this.selectedLabel = new Label();
        this.selectedLabel.prefWidthProperty().bind(vbox.widthProperty());
        this.selectedLabel.setAlignment(Pos.CENTER);
        this.selectedLabel.setMinWidth(139);
        vbox.getChildren().add(this.selectedLabel);

        // create a new Label and add it to the vbox
        this.infoLabel = new Label();
        this.infoLabel.prefWidthProperty().bind(vbox.widthProperty());
        //this.infoLabel.setMinHeight(0.0);
        this.infoLabel.setAlignment(Pos.CENTER);
        this.infoLabel.setMinWidth(140);
        vbox.getChildren().add(this.infoLabel);

        // create the main content in the form of an anchor-pane.
        this.content = new AnchorPane();
        //this.content.setMinHeight(0.0);
        this.content.prefWidthProperty().bind(vbox.widthProperty());
        vbox.getChildren().add(this.content);


        // add the vbox to the root and bind its height and width.
        root.getChildren().add(vbox);
        vbox.prefWidthProperty().bind(root.widthProperty());
        vbox.prefHeightProperty().bind(root.heightProperty());
    }



    private String lastFilter;

    @Override
    public void filter(String regexp)
    {
        this.lastFilter = regexp;

        if (super.items == null || super.items.length == 0)
        {
            this.selectedLabel.setText("0 selected");
            hideInfoLabel();
            return;
        }

        Pattern pat = super.getFilterPattern(regexp);

        int counter = 0;

        // first, find out how many items will still be visible afterwards.
        // simultaneously, hide all elements.
        for (T t : super.items)
        {
            t.setDisable(true);
            t.setVisible(false);
            t.setMaxHeight(0.0);
            t.setPrefHeight(0.0);
            t.setMinHeight(0.0);

            if (pat.matcher(t.getText()).matches())
                counter++;
        }

        // update the amount of selected users.
        this.selectedLabel.setText(counter + " selected");

        // if the counter exceeded the maximum amount of allowed users, return
        if (counter > this.maximumItems)
        {
            showInfoLabel();
        }
        else
        {
            hideInfoLabel();

            // let the super-class filter the items.
            super.filter(regexp);
        }
    }


    private void hideInfoLabel()
    {
        this.infoLabel.prefHeightProperty().unbind();
        this.infoLabel.setMinHeight(0.0);
        this.infoLabel.setPrefHeight(0.0);
        this.infoLabel.setVisible(false);
    }

    private void showInfoLabel()
    {
        this.infoLabel.setMinHeight(this.selectedLabel.getMinHeight());
        this.infoLabel.prefHeightProperty().bind(this.selectedLabel.heightProperty());
        this.infoLabel.setVisible(true);
    }

    @Override
    public void update() throws Exception
    {
        super.update();

        if (this.lastFilter != null)
            filter(this.lastFilter);
    }
}























