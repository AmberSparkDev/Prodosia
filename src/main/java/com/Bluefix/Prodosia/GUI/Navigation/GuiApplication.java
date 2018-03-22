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

import com.Bluefix.Prodosia.Storage.KeyStorage;
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


        if (GuiApplication.cssSheet == null)
        {
            GuiApplication.cssSheet = getClass().getResource("/com/Bluefix/Prodosia/GUI/stylesheet.css").toExternalForm();
        }
        scene.getStylesheets().add(cssSheet);
        primaryStage.setScene(scene);
        VistaNavigator.setVistaHolder(loader.getController());

        // if the imgur key was not setup, first go to the API keys window.
        if (KeyStorage.getImgurKey() == null)
        {
            VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
        }
        else
        {
            VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
        }



        primaryStage.show();
    }




}
