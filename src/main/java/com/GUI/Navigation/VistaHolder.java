package com.GUI.Navigation;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class VistaHolder
{

    @FXML
    public StackPane vistaHolder;

    public void setVista(Node node)
    {
        vistaHolder.getChildren().setAll(node);
    }
}
