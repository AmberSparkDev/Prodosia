package com.Bluefix.Prodosia.GUI;

import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
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
