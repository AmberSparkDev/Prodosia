/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.Bluefix.Prodosia.GUI.Tracker;

import com.Bluefix.Prodosia.DataType.Tracker;
import com.Bluefix.Prodosia.GUI.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.GUI.Helpers.EditableWindowPane;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EditTrackerWindow extends EditableWindowPane
{

    //region Constructor



    //endregion

    //region Textfields and labels

    @FXML public TextField txt_imgurName;
    @FXML public Label lbl_imgurId;
    @FXML public TextField txt_discordName;
    @FXML public TextField txt_discordTag;
    @FXML public TextField txt_discordId;
    @FXML public Label lbl_navigation;
    @FXML public Label lbl_deleteConfirmation;
    @FXML public Button button_confirmDelete;

    //endregion

    //region Button actions

    @FXML public Button button_back;
    @FXML public Button button_edit;
    @FXML public Button button_delete;
    @FXML public Button button_checkImgurName;



    public void btn_CancelBack(ActionEvent actionEvent)
    {
        super.button_Cancel_Back();
    }


    /**
     * Button that, depending on the current state, either states Edit or Save.
     *
     * The Edit button will store the fields that currently exist to allow for restoration.
     * The Save button will store the newly edited data.
     * @param actionEvent The action-event corresponding to the button press.
     */
    public void btn_EditSave(ActionEvent actionEvent)
    {
        super.button_Edit_Save();
    }

    /**
     * Button that, depending
     * @param actionEvent
     */
    public void btn_delete(ActionEvent actionEvent)
    {
        super.button_Delete();
    }

    public void btn_confirmDelete(ActionEvent actionEvent)
    {
        super.button_ConfirmDelete();
    }


    //endregion

    //region State handling

    @Override
    protected void applyState(WindowState state)
    {
        switch (state)
        {

            case VIEW:
                lbl_navigation.setText("Inspect Tracker");

                txt_imgurName.setDisable(true);
                txt_discordName.setDisable(true);
                txt_discordTag.setDisable(true);
                txt_discordId.setDisable(true);

                lbl_deleteConfirmation.setVisible(false);
                button_confirmDelete.setVisible(false);
                button_delete.setDisable(false);
                button_delete.setText("Delete");

                button_back.setText("Back");
                button_edit.setText("Edit");
                button_checkImgurName.setDisable(true);
                button_back.setDisable(false);
                button_edit.setDisable(false);


                break;
            case EDIT:
                lbl_navigation.setText("Edit Tracker");

                txt_imgurName.setDisable(false);
                txt_discordName.setDisable(false);
                txt_discordTag.setDisable(false);
                txt_discordId.setDisable(false);

                lbl_deleteConfirmation.setVisible(false);
                button_confirmDelete.setVisible(false);
                button_delete.setDisable(true);

                button_back.setText("Cancel");
                button_edit.setText("Save");
                button_checkImgurName.setDisable(false);
                button_back.setDisable(false);
                button_edit.setDisable(false);

                break;
            case DELETE:
                lbl_deleteConfirmation.setVisible(true);
                button_confirmDelete.setVisible(true);
                button_delete.setText("No");

                button_back.setDisable(true);
                button_edit.setDisable(true);


                break;
        }
    }

    //endregion

    //region initialization

    private Tracker curTracker;

    private void clearData()
    {
        txt_imgurName.setText("");
        lbl_imgurId.setText("");
        txt_discordName.setText("");
        txt_discordTag.setText("");
        txt_discordId.setText("");
    }

    /**
     * Initialize an empty window
     */
    public void initialize()
    {
        curTracker = null;

        clearData();

        setState(WindowState.EDIT);
    }

    /**
     * Initialize a window with the tracker-information.
     * @param tracker
     */
    public void initialize(Tracker tracker)
    {
        curTracker = tracker;

        clearData();

        // if imgur-credentials exist, set them up.
        if (tracker.getImgurName() != null)
        {
            txt_imgurName.setText(tracker.getImgurName());
            lbl_imgurId.setText("" + tracker.getImgurId());
        }

        // if discord-credentials exist, set them up.
        if (tracker.getDiscordName() != null)
        {
            txt_discordName.setText(tracker.getDiscordName());
            txt_discordTag.setText("" + tracker.getDiscordTag());
            txt_discordId.setText("" + tracker.getDiscordId());
        }

        setState(WindowState.VIEW);
    }

    //endregion

    //region Datafield recovery

    @Override
    protected DataFieldStorage storeFields()
    {
        return DataFieldStorage.store(
                txt_imgurName.getText(),
                lbl_imgurId.getText(),
                txt_discordName.getText(),
                txt_discordTag.getText(),
                txt_discordId.getText()
        );
    }

    @Override
    protected void restoreFields(DataFieldStorage storage)
    {

        String[] fields = storage.retrieve();

        txt_imgurName.setText(fields[0]);
        lbl_imgurId.setText(fields[1]);
        txt_discordName.setText(fields[2]);
        txt_discordTag.setText(fields[3]);
        txt_discordId.setText(fields[4]);
    }

    //endregion

    //region inherited methods

    /**
     * @return true iff we are still in the process of creating the tracker.
     */
    protected boolean isCreating()
    {
        return curTracker == null;
    }

    @Override
    protected void deleteItem()
    {

    }

    @Override
    protected void saveItem()
    {

    }

    //endregion

    //region Tracker Handling



    //endregion

    //region Deletion Handling



    //endregion



}
