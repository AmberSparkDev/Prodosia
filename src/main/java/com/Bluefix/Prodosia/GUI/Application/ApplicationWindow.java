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

package com.Bluefix.Prodosia.GUI.Application;

import com.Bluefix.Prodosia.DataType.Data;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.ApiKeys.ApiKeysWindow;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.GuiCheckboxListManager;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.TaglistClManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.TaglistListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.TrackerListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.UserListManager;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.Prefix.PrefixWindow;
import com.Bluefix.Prodosia.GUI.Taglist.EditTaglistWindow;
import com.Bluefix.Prodosia.GUI.Tracker.EditTrackerWindow;
import com.Bluefix.Prodosia.GUI.User.EditUserWindow;
import com.Bluefix.Prodosia.Logger.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;


public class ApplicationWindow
{
    //region Singleton

    private static ApplicationWindow me;

    private static ApplicationWindow handler()
    {
        return me;
    }

    //endregion

    //region Archives

    @FXML private VBox arch_taglistOverview;
    @FXML private Button arch_addArchiveButton;
    @FXML private Label arch_selectedTaglist;
    @FXML private VBox arch_selectionBox;

    /**
     * The ApplicationWindow_Archive class manages all logic necessary for the Archives to function.
     */
    private ApplicationWindow_Archive archiveGui;

    private void initializeArchive() throws Exception
    {
        if (archiveGui != null)
            return;

        archiveGui = new ApplicationWindow_Archive(arch_taglistOverview, arch_selectionBox, arch_selectedTaglist, arch_addArchiveButton);
    }

    //endregion

    //region Status

    @FXML private TextArea statusConsole;

    @FXML
    private void gotoApiKeys()
    {
        // set `ApiKeysWindow` as active window.
        ApiKeysWindow akw = VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
    }

    public void gotoPrefix(ActionEvent actionEvent) throws Exception
    {
        PrefixWindow pfw = VistaNavigator.loadVista(VistaNavigator.AppStage.PREFIX);
        pfw.init();
    }

    public void gotoImportExport(ActionEvent actionEvent)
    {
        VistaNavigator.loadVista(VistaNavigator.AppStage.IMPORT_EXPORT);
    }

    private void initializeStatusWindow()
    {
        // set the output of the logger to the textarea
        Logger.setupOutput(statusConsole);
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
        controller.init(null);
    }

    //endregion

    //region Taglists

    @FXML private VBox taglists_overview;

    @FXML
    private void addTaglist(ActionEvent actionEvent)
    {
        EditTaglistWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TAGLIST_EDIT);
        controller.init(null);
    }

    //endregion

    //region Users

    @FXML private VBox users_overview;

    @FXML
    private void addUser(ActionEvent actionEvent) throws Exception
    {
        EditUserWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.USER_EDIT);
        controller.init(null);
    }

    //endregion

    //region Constructor

    public ApplicationWindow()
    {

    }

    //endregion

    //region Initialization

    @FXML
    private void initialize() throws Exception
    {
        // set a singleton variable.
        ApplicationWindow.me = this;

        /* Status */
        initializeStatusWindow();



        /* Tag a Post */
        initializeTap();

        // init list managers
        setupListManagers();

        // init Archive
        initializeArchive();

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

    private TrackerListManager tlm;
    private TaglistListManager tllm;
    private UserListManager ulm;

    private GuiCheckboxListManager tap_taglist_cl;


    private void setupListManagers()
    {
        // tracker list
        try
        {
            tlm = new TrackerListManager(trackers_overview);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        try
        {
            tllm = new TaglistListManager(taglists_overview);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        try
        {
            ulm = new UserListManager(users_overview);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        try
        {
            // setup checkbox gui list managers.
            tap_taglist_cl = new TaglistClManager(tap_taglistSelector);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        // setup listeners for their respective filters.

        tracker_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            tlm.filter(newVal);
        });

        taglist_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            tllm.filter(newVal);
        });

        user_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            ulm.filter(newVal);
        });
    }

    /**
     * Update the components now that we changed data somewhere else.
     */
    public void updateListManagers() throws Exception
    {
        tlm.update();
        tllm.update();
        ulm.update();

        // update the archiving functionality.
        archiveGui.update();
    }


    public static void update()
    {
        Platform.runLater(() ->
        {
            try
            {
                handler().updateListManagers();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Update the list containing the users.
     * @throws Exception
     */
    public static void updateUsers()
    {
        Platform.runLater(() ->
        {
            try
            {
                handler().ulm.update();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public static void updateTaglists()
    {
        Platform.runLater(() ->
        {
            try
            {
                handler().tllm.update();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public static void updateTrackers()
    {
        Platform.runLater(() ->
        {
            try
            {
                handler().tlm.update();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public static void updateArchives()
    {
        Platform.runLater(() ->
        {
            try
            {
                handler().archiveGui.update();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }




    //endregion

    /**
     * Temporary test method.
     * @param actionEvent
     */
    public void test(ActionEvent actionEvent) throws Exception
    {


    }





}














































