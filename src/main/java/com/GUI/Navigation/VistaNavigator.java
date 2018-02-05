package com.GUI.Navigation;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

/**
 * Global GUI Controller
 */
public class VistaNavigator
{
    /* --- Vista urls --- */

    private static final String urlBase = "/com/GUI/";
    private static final String MAIN_MENU = "ApplicationWindow.fxml";
    private static final String API_KEYS = "ApiKeysWindow.fxml";

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
        API_KEYS
    }

    private static Node NodeMainMenu;
    private static Node NodeApiKeys;


    /**
     * Retrieve the fxml content for the specified stage.
     * @param appStage
     */
    private static Node GetVista(AppStage appStage) throws IOException
    {
        switch (appStage)
        {

            case MAIN_MENU:
                if (NodeMainMenu == null)
                {
                    URL url = VistaNavigator.class.getResource(urlBase + MAIN_MENU);
                    NodeMainMenu = FXMLLoader.load(url);
                }

                return NodeMainMenu;

            case API_KEYS:
                if (NodeApiKeys == null)
                {
                    URL url = VistaNavigator.class.getResource(urlBase + API_KEYS);
                    NodeApiKeys = FXMLLoader.load(url);
                }

                return NodeApiKeys;

            default:
                throw new IllegalArgumentException("This AppStage does not exist!");
        }


    }


    public static void loadVista(AppStage appStage)
    {
        try
        {
            vistaHolder.setVista(GetVista(appStage));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* --- --- */

    private static GuiApplication app;


    public static void initialize()
    {
        app = new GuiApplication();
        app.launch(GuiApplication.class);
    }
}
