/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.GUI.Navigation;

import javafx.scene.Node;
import javafx.scene.Parent;

public class GuiNode<T>
{
    public GuiNode(Parent parent, T controller)
    {
        this.parent = parent;
        this.controller = controller;
    }

    public Parent getParent()
    {
        return parent;
    }

    public T getController()
    {
        return controller;
    }

    private Parent parent;
    private T controller;
}
