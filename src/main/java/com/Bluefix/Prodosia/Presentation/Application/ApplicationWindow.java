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

package com.Bluefix.Prodosia.Presentation.Application;

import com.Bluefix.Prodosia.Business.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.Business.Logger.ApplicationWindowLogger;
import com.Bluefix.Prodosia.Presentation.ApiKeys.ApiKeysWindow;
import com.Bluefix.Prodosia.Presentation.Managers.ButtonListManager.TaglistListManager;
import com.Bluefix.Prodosia.Presentation.Managers.ButtonListManager.TrackerListManager;
import com.Bluefix.Prodosia.Presentation.Managers.ButtonListManager.UserListManager;
import com.Bluefix.Prodosia.Presentation.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.Presentation.Prefix.PrefixWindow;
import com.Bluefix.Prodosia.Presentation.Taglist.EditTaglistWindow;
import com.Bluefix.Prodosia.Presentation.Tracker.EditTrackerWindow;
import com.Bluefix.Prodosia.Presentation.User.EditUserWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
        ApplicationWindowLogger.setupOutput(statusConsole);
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

    //region Initialization

    @FXML
    private void initialize() throws Exception
    {
        // set a singleton variable.
        ApplicationWindow.me = this;

        /* Status */
        initializeStatusWindow();


        // init list managers
        setupListManagers();

        // init Archive
        initializeArchive();
    }





    //endregion

    //region List Managers

    @FXML private TextField tracker_filter;
    @FXML private TextField taglist_filter;
    @FXML private TextField user_filter;

    private TrackerListManager tlm;
    private TaglistListManager tllm;
    private UserListManager ulm;


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

        // taglist list
        try
        {
            tllm = new TaglistListManager(taglists_overview);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        // user list
        try
        {
            ulm = new UserListManager(users_overview);
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
    private void updateListManagers() throws Exception
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
}














































