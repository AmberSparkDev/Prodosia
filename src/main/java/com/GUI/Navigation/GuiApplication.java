package com.GUI.Navigation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GuiApplication extends Application
{
    /**
     * The primary scene that persists throughout the application.
     */
    private final static String mainScene = "/com/GUI/VistaHolder.fxml";


    /**
     * Start the application by setting the initial stage.
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // load the application window
        FXMLLoader loader = new FXMLLoader();

        Parent root = loader.load(
                getClass().getResourceAsStream(
                        mainScene
                )
        );

        primaryStage.setTitle("Prodosía - by ReGeX");
        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add("/com/GUI/stylesheet.css");
        primaryStage.setScene(scene);

        VistaNavigator.setVistaHolder(loader.getController());

        VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);

        primaryStage.show();
    }




}
