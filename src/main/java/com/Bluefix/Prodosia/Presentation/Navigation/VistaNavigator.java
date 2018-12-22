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

package com.Bluefix.Prodosia.Presentation.Navigation;


import javafx.stage.Stage;

import java.io.IOException;

/**
 * Global GUI Controller
 */
public class VistaNavigator
{

    /* --- Vista urls --- */

    private static final String urlBase = "/com/Bluefix/Prodosia/GUI/";
    private static final String MAIN_MENU = "Application/ApplicationWindow.fxml";
    private static final String API_KEYS = "ApiKeysWindow.fxml";
    private static final String TRACKER_EDIT = "Tracker/EditTrackerWindow.fxml";
    private static final String TAGLIST_EDIT = "Taglist/EditTaglistWindow.fxml";
    private static final String USER_EDIT = "User/EditUserWindow.fxml";
    private static final String ARCHIVE_EDIT = "Archive/EditArchiveWindow.fxml";
    private static final String PREFIX_VIEW = "PrefixWindow.fxml";
    private static final String IMPORT_EXPORT = "ImportExportWindow.fxml";

    /* --- --- */


    /* --- Vista Controller reference --- */

    private static VistaHolder vistaHolder;

    public static void setVistaHolder(VistaHolder vistaHolder)
    {
        VistaNavigator.vistaHolder = vistaHolder;
    }

    /* --- --- */

    /* --- Vista Control --- */

    /**
     * The current stage of the application. Determines the fxml
     * content that will be displayed.
     */
    public enum AppStage
    {
        MAIN_MENU,
        API_KEYS,
        PREFIX,
        IMPORT_EXPORT,

        TRACKER_EDIT,
        TAGLIST_EDIT,
        USER_EDIT,
        ARCHIVE_EDIT
    }

    private static GuiNode[] applicationWindows = null;


    /**
     * retrieve the fxml content for the specified stage.
     * @param appStage
     */
    private static <T> GuiNode<T> GetVista(AppStage appStage) throws IOException
    {
        // if the array has not been initialized, do so.
        if (applicationWindows == null)
        {
            applicationWindows = new GuiNode[AppStage.values().length];
        }

        // if the specific node has not been initialized, do so.
        int index = appStage.ordinal();

        if (applicationWindows[index] == null)
        {
            // build the node
            String url;

            switch (appStage)
            {

                case MAIN_MENU:
                    url = urlBase + MAIN_MENU;
                    break;
                case API_KEYS:
                    url = urlBase + API_KEYS;
                    break;
                case PREFIX:
                    url = urlBase + PREFIX_VIEW;
                    break;
                case IMPORT_EXPORT:
                    url = urlBase + IMPORT_EXPORT;
                    break;
                case TRACKER_EDIT:
                    url = urlBase + TRACKER_EDIT;
                    break;
                case TAGLIST_EDIT:
                    url = urlBase + TAGLIST_EDIT;
                    break;
                case USER_EDIT:
                    url = urlBase + USER_EDIT;
                    break;
                case ARCHIVE_EDIT:
                    url = urlBase + ARCHIVE_EDIT;
                    break;

                default:
                    throw new IllegalArgumentException("This appstage does not exist!");
            }

            applicationWindows[index] = app.loadWindow(url);

        }

        return applicationWindows[index];
    }


    public static <T> T loadVista(AppStage appStage)
    {
        T controller = null;

        try
        {
            GuiNode<T> guiNode = GetVista(appStage);

            vistaHolder.setVista(guiNode.getParent());
            controller = guiNode.getController();



        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return controller;
    }

    /* --- --- */

    private static GuiApplication app;


    public static void initialize()
    {
        app = new GuiApplication();
        app.launch(GuiApplication.class);
    }


    /**
     * Bring to foreground
     */
    public static void show()
    {
        if (VistaNavigator.vistaHolder == null)
            return;

        ((Stage)VistaNavigator.vistaHolder.vistaHolder.getScene().getWindow()).show();
    }
}
