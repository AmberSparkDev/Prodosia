package com.Bluefix.Prodosia.GUI.Navigation;


import java.io.IOException;

/**
 * Global GUI Controller
 */
public class VistaNavigator
{

    /* --- Vista urls --- */

    private static final String urlBase = "/com/Bluefix/Prodosia/GUI/";
    private static final String MAIN_MENU = "ApplicationWindow.fxml";
    private static final String API_KEYS = "ApiKeysWindow.fxml";
    private static final String TRACKER_EDIT = "Tracker/EditTrackerWindow.fxml";
    private static final String TAGLIST_EDIT = "Taglist/EditTaglistWindow.fxml";
    private static final String USER_EDIT = "User/EditUserWindow.fxml";

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

        TRACKER_EDIT,
        TAGLIST_EDIT,
        USER_EDIT
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
                case TRACKER_EDIT:
                    url = urlBase + TRACKER_EDIT;
                    break;
                case TAGLIST_EDIT:
                    url = urlBase + TAGLIST_EDIT;
                    break;
                case USER_EDIT:
                    url = urlBase + USER_EDIT;
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
}
