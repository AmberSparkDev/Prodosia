package com.Bluefix.Prodosia.GUI.Helpers;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.Taglist.EditTaglistWindow;
import com.Bluefix.Prodosia.GUI.Tracker.EditTrackerWindow;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Labeled;
import javafx.scene.layout.VBox;

/**
 * GUI Helper class for anything related to taglists.
 */
public class TaglistGuiHelper
{


    /**
     * Fill up a VBox with checkbox references to taglists.
     * @param vbox
     */
    public static void fillVBoxWithTaglistCheckboxes(VBox vbox)
    {
        vbox.getChildren().clear();
        Taglist[] data = TaglistHandler.getTaglistsSorted();

        for (Taglist tl : data)
        {
            boolean add = vbox.getChildren().add(new CheckBox(tl.getAbbreviation()));
        }
    }

    /**
     * Fill up a VBox with button references to taglists.
     * @param vbox
     */
    public static void fillVBoxWithTaglistButtons(VBox vbox)
    {
        vbox.getChildren().clear();
        Taglist[] data = TaglistHandler.getTaglistsSorted();

        for (Taglist tl : data)
        {
            Button button = new Button(tl.getAbbreviation());
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("trackerButton");

            // set button action
            button.setOnAction(event ->
            {
                EditTaglistWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TAGLIST_EDIT);
                //controller.initialize(tl);
            });

            vbox.getChildren().add(button);
        }
    }



}
