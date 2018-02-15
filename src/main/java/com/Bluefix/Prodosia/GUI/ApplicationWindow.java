/*
 * Copyright (c) 2018 Bas Boellaard
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

package com.Bluefix.Prodosia.GUI;

import com.Bluefix.Prodosia.DataType.Data;
import com.Bluefix.Prodosia.DataType.Rating;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.GuiCheckboxListManager;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.TaglistClManager;
import com.Bluefix.Prodosia.GUI.Managers.ListManager.GuiListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.TaglistListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.TrackerListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.UserListManager;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.Taglist.EditTaglistWindow;
import com.Bluefix.Prodosia.GUI.Tracker.EditTrackerWindow;
import com.Bluefix.Prodosia.GUI.User.EditUserWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


public class ApplicationWindow
{

    //region Status

    @FXML private TextArea statusConsole;

    @FXML
    private void gotoApiKeys()
    {
        // set `ApiKeysWindow` as active window.
        VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
    }

    //endregion

    //region Tag a Post

    @FXML private TextField tap_url;

    @FXML private TextArea tap_parentComment;

    @FXML private MenuButton tap_ratingButton;

    @FXML private VBox tap_taglistSelector;

    @FXML private void tap_ratingA(ActionEvent actionEvent)
    {
        selectRating(Rating.ALL);
    }

    @FXML private void tap_ratingS(ActionEvent actionEvent)
    {
        selectRating(Rating.SAFE);
    }

    @FXML private void tap_ratingQ(ActionEvent actionEvent)
    {
        selectRating(Rating.QUESTIONABLE);
    }

    @FXML private void tap_ratingE(ActionEvent actionEvent)
    {
        selectRating(Rating.EXPLICIT);
    }

    private Rating tap_rating;

    private void selectRating(Rating rating)
    {
        tap_rating = rating;

        tap_ratingButton.setText(Data.ToString(rating));
    }


    @FXML
    private void tap_execute(ActionEvent actionEvent)
    {
        String[] items = tap_taglist_cl.getSelectedItems();

        System.out.println("Selected items:");

        for (String i : items)
        {
            System.out.println(i);
        }

        System.out.println();
    }

    //endregion


    //region Trackers

    @FXML private VBox trackers_overview;

    @FXML
    private void addTracker(ActionEvent actionEvent)
    {
        EditTrackerWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TRACKER_EDIT);
        controller.initialize();
    }

    //endregion

    //region Taglists

    @FXML private VBox taglists_overview;

    @FXML
    private void addTaglist(ActionEvent actionEvent)
    {
        EditTaglistWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TAGLIST_EDIT);
        controller.initialize();
    }

    //endregion

    //region Users

    @FXML private VBox users_overview;

    @FXML
    private void addUser(ActionEvent actionEvent)
    {
        EditUserWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.USER_EDIT);
        controller.initialize();
    }

    //endregion

    //region Constructor

    public ApplicationWindow()
    {

    }

    //endregion

    //region Initialization

    @FXML
    private void initialize()
    {
        /* Status */
        statusConsole.setText("This is the console");


        /* Tag a Post */
        initializeTap();

        // initialize list managers
        setupListManagers();
    }

    private void initializeTap()
    {
        tap_url.setText("tap_url");
        tap_parentComment.setText("tap_parentComment");
        tap_parentComment.setDisable(true);
        selectRating(Rating.ALL);
    }

    //endregion

    //region List Managers

    @FXML private TextField tracker_filter;
    @FXML private TextField taglist_filter;
    @FXML private TextField user_filter;

    private GuiListManager[] listManagers;

    private GuiCheckboxListManager tap_taglist_cl;


    private void setupListManagers()
    {
        listManagers = new GuiListManager[3];

        // tracker list
        listManagers[0] = new TrackerListManager(trackers_overview);
        listManagers[1] = new TaglistListManager(taglists_overview);
        listManagers[2] = new UserListManager(users_overview);

        // setup listeners for their respective filters.

        tracker_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            listManagers[0].filter(newVal);
        });

        taglist_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            listManagers[1].filter(newVal);
        });

        user_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            listManagers[2].filter(newVal);
        });

        // setup checkbox gui list managers.
        tap_taglist_cl = new TaglistClManager(tap_taglistSelector);
    }


    //endregion



}
