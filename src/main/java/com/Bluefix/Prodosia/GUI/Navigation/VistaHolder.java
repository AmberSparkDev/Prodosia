package com.Bluefix.Prodosia.GUI.Navigation;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class VistaHolder
{

    @FXML
    public StackPane vistaHolder;

    public void setVista(Parent node)
    {
        vistaHolder.getChildren().setAll(node);
    }
}
