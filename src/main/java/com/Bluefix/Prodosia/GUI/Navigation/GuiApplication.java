package com.Bluefix.Prodosia.GUI.Navigation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintStream;

public class GuiApplication extends Application
{
    //region Load FXML window.

    public GuiNode loadWindow(String url) throws IOException
    {

        FXMLLoader loader = new FXMLLoader();

        Parent node = loader.load(
                getClass().getResourceAsStream(
                        url
                )
        );

        node.getStylesheets().clear();
        Object controller = loader.getController();

        return new GuiNode(node, controller);
    }

    //endregion

    /**
     * The primary scene that persists throughout the application.
     */
    private final static String mainScene = "/com/Bluefix/Prodosia/GUI/Navigation/VistaHolder.fxml";


    /**
     * Start the application by setting the initial stage.
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // setup the application singleton.

        // load the application window
        FXMLLoader loader = new FXMLLoader();

        Parent root = loader.load(
                getClass().getResourceAsStream(
                        mainScene
                )
        );

        primaryStage.setTitle("Prodosía - by ReGeX");
        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add("/com/Bluefix/Prodosia/GUI/stylesheet.css");
        primaryStage.setScene(scene);

        VistaNavigator.setVistaHolder(loader.getController());

        VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);

        primaryStage.show();
    }




}
