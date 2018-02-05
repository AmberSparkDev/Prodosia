package com.GUI;

import com.GUI.Navigation.VistaNavigator;
import javafx.fxml.FXML;

import java.io.IOException;

public class ApiKeysWindow
{



    @FXML
    private void goBack() throws IOException
    {
        // go back
        VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
    }
}
