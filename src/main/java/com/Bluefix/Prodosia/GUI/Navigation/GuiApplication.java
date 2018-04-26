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

package com.Bluefix.Prodosia.GUI.Navigation;

import com.Bluefix.Prodosia.Discord.DiscordManager;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.Bluefix.Prodosia.Module.ModuleManager;
import com.Bluefix.Prodosia.Storage.KeyStorage;
import com.github.kskelm.baringo.BaringoClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.PrintStream;

public class GuiApplication extends Application
{
    private static boolean initialized = false;

    public static boolean isInitialized()
    {
        return GuiApplication.initialized;
    }

    public static String cssSheet;

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
        // set the application variable to initialized.
        GuiApplication.initialized = true;

        // load the application window
        FXMLLoader loader = new FXMLLoader();

        Parent root = loader.load(
                getClass().getResourceAsStream(
                        mainScene
                )
        );

        primaryStage.setTitle("Prodosía - by ReGeX");
        primaryStage.setResizable(false);

        // ensure that the entire application is shut down on closing this window.
        primaryStage.setOnCloseRequest(t ->
        {
            Platform.exit();
            System.exit(0);
        });

        //primaryStage.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(root, 640, 480);


        if (GuiApplication.cssSheet == null)
        {
            GuiApplication.cssSheet = getClass().getResource("/com/Bluefix/Prodosia/GUI/stylesheet.css").toExternalForm();
        }
        scene.getStylesheets().add(cssSheet);
        primaryStage.setScene(scene);
        VistaNavigator.setVistaHolder(loader.getController());

        // finish the necessary initialization now that the GUI is ready.
        initializationFinished();
        primaryStage.show();
    }


    private void initializationFinished() throws IOException, LoginException
    {
        // start the discord manager.
        DiscordManager.manager();

        // initialize the Imgur dependencies if applicable.
        if (KeyStorage.getImgurKey() == null)
        {
            VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
        }
        else
        {

            try
            {
                BaringoClient client = ImgurManager.client();

                if (client != null)
                {
                    ModuleManager.startImgurDependencies();
                    VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
                } else
                {
                    VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
                }

            } catch (Exception e)
            {
                // on any exception, we will direct to the API keys
                // to ensure they are valid.
                VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
            }
        }
    }




}
