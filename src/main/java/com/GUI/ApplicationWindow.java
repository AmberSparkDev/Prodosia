package com.GUI;

import com.GUI.Navigation.VistaNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;


public class ApplicationWindow
{

    /* --- Tag a Post --- */

    @FXML
    private TextField tap_url;



    public ApplicationWindow()
    {

    }
    



    @FXML
    private void initialize()
    {
        tap_url.setText("blablabla");
    }

    @FXML
    private void gotoApiKeys() throws IOException
    {
        // set `ApiKeysWindow` as active window.
        VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
    }
}
