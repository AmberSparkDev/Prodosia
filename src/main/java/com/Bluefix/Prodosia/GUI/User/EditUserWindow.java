/*
 * Copyright (c) 2018 Bas Boellaard
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

package com.Bluefix.Prodosia.GUI.User;

import com.Bluefix.Prodosia.DataType.User;
import com.Bluefix.Prodosia.GUI.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.GUI.Helpers.EditableWindowPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class EditUserWindow extends EditableWindowPane
{

    //region Constructor



    //endregion

    //region Textfields and labels

    @FXML public Label lbl_navigation;
    @FXML public Label lbl_deleteConfirmation;
    @FXML public Button button_confirmDelete;

    //endregion

    //region Button actions

    @FXML public Button button_back;
    @FXML public Button button_edit;
    @FXML public Button button_delete;



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
                lbl_navigation.setText("Inspect User");

                lbl_deleteConfirmation.setVisible(false);
                button_confirmDelete.setVisible(false);
                button_delete.setDisable(false);
                button_delete.setText("Delete");
                button_back.setText("Back");
                button_edit.setText("Edit");
                button_back.setDisable(false);
                button_edit.setDisable(false);
                break;

            case EDIT:
                lbl_navigation.setText("Edit User");

                lbl_deleteConfirmation.setVisible(false);
                button_confirmDelete.setVisible(false);
                button_delete.setDisable(true);
                button_back.setText("Cancel");
                button_edit.setText("Save");
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

    private User curUser;

    private void clearData()
    {

    }

    /**
     * Initialize an empty window
     */
    public void initialize()
    {
        curUser = null;

        clearData();

        setState(WindowState.EDIT);
    }

    /**
     * Initialize a window with the user-information.
     * @param user the user to initialize on.
     */
    public void initialize(User user)
    {
        curUser = user;

        clearData();


        setState(WindowState.VIEW);
    }

    //endregion

    //region Datafield recovery

    @Override
    protected DataFieldStorage storeFields()
    {
        return DataFieldStorage.store(
        );
    }

    @Override
    protected void restoreFields(DataFieldStorage storage)
    {

        String[] fields = storage.retrieve();
    }

    //endregion

    //region inherited methods

    /**
     * @return true iff we are still in the process of creating the tracker.
     */
    protected boolean isCreating()
    {
        return curUser == null;
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
