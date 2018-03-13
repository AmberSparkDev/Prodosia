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

package com.Bluefix.Prodosia.GUI.Tracker;

import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.DataBuilder.TrackerBuilder;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.GUI.Helpers.EditableWindowPane;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.TaglistClManager;
import com.Bluefix.Prodosia.GUI.Navigation.GuiApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

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

                // permissions
                perm_scrollpane.setDisable(true);
                perm_filter.setDisable(true);
                perm_chkAdmin.setDisable(true);


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

                // permissions
                perm_chkAdmin.setDisable(false);
                adminCheckboxSelection();

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

        initializePermissions();
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

        // clear the update data to prevent the window from updating itself.
        clearUpdate();

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
    protected void deleteItem() throws Exception
    {
        TrackerHandler.removeTracker(curTracker);
    }

    @Override
    protected void saveItem() throws Exception
    {
        // parse the tracker from the fields.
        Tracker newTracker = parseTracker();

        if (newTracker != null)
        {
            TrackerHandler.updateTracker(curTracker, newTracker);
            curTracker = newTracker;
        }
    }

    //endregion

    //region Api update

    /**
     * Indicate to the system that an update of the data is not required. This is
     * the case when a user has just been loaded in.
     */
    private void clearUpdate()
    {
        clearImgurUpdate();
        clearDiscordUpdate();
    }

    private void clearImgurUpdate()
    {
        previousImgurName = txt_imgurName.getText();
    }

    private void clearDiscordUpdate()
    {
        previousDiscordName = txt_discordName.getText();
        previousDiscordTag = txt_discordTag.getText();
        previousDiscordId = txt_discordId.getText();
    }

    private String previousImgurName = null;
    private String previousDiscordName = null;
    private String previousDiscordTag = null;
    private String previousDiscordId = null;

    private synchronized void updateImgurData()
    {
        if (txt_imgurName.getText().equals(previousImgurName))
            return;

        // TODO: set the imgur-id dependent on the imgur name.


    }

    /**
     * Update the discord data fields. Will only update if one of the fields was invalidated.
     * Will update according to the fields that were changed, giving priority to the id.
     */
    private synchronized void updateDiscordData()
    {
        // if none of the items were changed, return.
        boolean compId = txt_discordId.getText().equals(previousDiscordId);
        boolean compName = txt_discordName.getText().equals(previousDiscordName);
        boolean compTag = txt_discordTag.getText().equals(previousDiscordTag);

        if (compId && compName && compTag)
            return;

        // update the data.
        clearDiscordUpdate();

        // TODO: set the discord-data based on the last item that was edited.
        if (!compId)
        {
            updateDiscordDataById();
        }
        else if (!compName || !compTag)
        {
            updateDiscordDataByName();
        }
    }

    private void updateDiscordDataById()
    {
        // TODO: Implement update based on id.
    }

    private void updateDiscordDataByName()
    {
        // TODO: implement update based on name & tag.
    }


    //endregion


    //region Data extraction and validation

    /**
     * Parse a tracker from the data in the fields. Will return null iff at least one field is invalid.
     * @return the tracker as indicated in the fields, or null otherwise.
     */
    private Tracker parseTracker()
    {
        // TODO: update the api data if necessary.
        updateImgurData();
        updateDiscordData();

        DataValidation valImg = validateImgurData();
        DataValidation valDis = validateDiscordData();

        // if either of the data was erroneous, warn the user.






        TrackerBuilder builder = TrackerBuilder.getBuilder();



        return null;
    }

    private DataValidation validateImgurData()
    {
        if (lbl_imgurId.getText().isEmpty())
        {
            // if no imgur name was specified, this is normal behavior.
            if (txt_imgurName.getText().isEmpty())
            {
                return DataValidation.MISSING;
            }
            else
            {
                // there was an imgur name, but no proper id.
                return DataValidation.ERRONEOUS;
            }
        }

        return DataValidation.VALIDATED;
    }

    private DataValidation validateDiscordData()
    {
        // TODO: determine whether the data is valid.
        return DataValidation.MISSING;
    }



    /**
     * The validation of the specified data.
     */
    private enum DataValidation
    {
        /**
         * The data was found to be correct.
         */
        VALIDATED,

        /**
         * The data was found to be at least partially incorrect.
         */
        ERRONEOUS,

        /**
         * The data was missing.
         */
        MISSING
    }

    private void alertUser(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add(GuiApplication.cssSheet);
        alert.setTitle("Something went wrong!");
        alert.setHeaderText("Error!");
        alert.setContentText("Something went wrong :S");
        alert.showAndWait();
    }

    //endregion


    //region Permission Handling

    @FXML private CheckBox perm_chkAdmin;
    @FXML private VBox perm_taglists;
    @FXML private TextField perm_filter;
    @FXML private ScrollPane perm_scrollpane;

    private TaglistClManager taglistClManager;

    private void initializePermissions()
    {
        perm_filter.setText("");
        perm_chkAdmin.setSelected(false);
        try
        {
            taglistClManager = new TaglistClManager(perm_taglists);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        perm_filter.textProperty().addListener((observable, oldValue, newValue) ->
                taglistClManager.filter(newValue));

        perm_chkAdmin.selectedProperty().addListener((observable, oldValue, newValue) ->
                adminCheckboxSelection());
    }

    private void adminCheckboxSelection()
    {
        if (perm_chkAdmin.isSelected())
        {
            perm_filter.setDisable(true);
            perm_scrollpane.setDisable(true);
        }
        else
        {
            perm_filter.setDisable(false);
            perm_scrollpane.setDisable(false);
        }
    }




    //endregion






}
