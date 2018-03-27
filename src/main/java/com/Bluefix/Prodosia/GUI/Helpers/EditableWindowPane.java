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

package com.Bluefix.Prodosia.GUI.Helpers;

import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.Application.ApplicationWindow;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * This is a helper class aimed at an editable window.
 * This class can adopt several states, and depending on these
 * states the layout and visibility of its components will change.
 *
 * This class relies on a window having 4 buttons:
 * - Cancel / Back button
 * - Edit / Save button
 * - Delete / No button
 * - Confirmation of deletion button
 */
public abstract class EditableWindowPane
{
    //region Constructor

    private Button button_back;
    private Button button_edit;
    private Button button_delete;
    @FXML
    private Button button_confirmDelete;

    /**
     * Initialize buttons associated with this editable window pane.
     * Every single user-element is optional, but functionality won't be complete if they are omitted.
     * @param button_back The back / cancel button of the editable pane.
     * @param button_edit The edit / save button of the editable pane.
     * @param button_delete The delete button of the editable pane.
     * @param button_confirmDelete The button that confirms deletion.
     */
    protected void initialize(Button button_back, Button button_edit, Button button_delete, Button button_confirmDelete)
    {
        this.button_back = button_back;
        this.button_edit = button_edit;
        this.button_delete = button_delete;
        this.button_confirmDelete = button_confirmDelete;

        if (this.button_back != null)
            this.button_back.setOnAction(event ->
            {
                try
                {
                    button_Cancel_Back();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            });

        if (this.button_edit != null)
            this.button_edit.setOnAction(event -> button_Edit_Save());

        if (this.button_delete != null)
            this.button_delete.setOnAction(event -> button_Delete());

        if (this.button_confirmDelete != null)
        {
            this.button_confirmDelete.setOnAction(event ->
            {
                try
                {
                    button_ConfirmDelete();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            });

            // init default confirmation text.
            this.button_confirmDelete.setText("Yes");
        }
    }

    //endregion



    //region State handling

    private WindowState curState;

    protected enum WindowState
    {
        VIEW, EDIT, DELETE,
    }

    /**
     * Set the state of the window.
     * @param state the new state of the window.
     */
    protected void setState(WindowState state)
    {
        curState = state;

        // for whichever buttons is applicable to us, set the text and values.
        switch (curState)
        {

            case VIEW:
                if (button_confirmDelete != null)
                    button_confirmDelete.setVisible(false);

                if (button_delete != null)
                {
                    button_delete.setDisable(false);
                    button_delete.setText("Delete");
                }

                if (button_back != null)
                {
                    button_back.setText("Back");
                    button_back.setDisable(false);
                }

                if (button_edit != null)
                {
                    button_edit.setText("Edit");
                    button_edit.setDisable(false);
                }
                break;

            case EDIT:
                if (button_confirmDelete != null)
                    button_confirmDelete.setVisible(false);

                if (button_delete != null)
                    button_delete.setDisable(true);

                if (button_back != null)
                {
                    button_back.setText("Cancel");
                    button_back.setDisable(false);
                }

                if (button_edit != null)
                {
                    button_edit.setText("Save");
                    button_edit.setDisable(false);
                }
                break;

            case DELETE:
                if (button_confirmDelete != null)
                    button_confirmDelete.setVisible(true);

                if (button_delete != null)
                    button_delete.setText("No");

                if (button_back != null)
                    button_back.setDisable(true);

                if (button_edit != null)
                    button_edit.setDisable(true);
                break;
        }

        // allow the subclass to set its state information.
        applyState(state);
    }

    /**
     * Setup the expected change in layout dependent on the state.
     * @param state the current state of the window.
     */
    protected abstract void applyState(WindowState state);

    //endregion


    //region Button events

    /**
     * Depending on the state of the window, either go back to view mode or
     * go back to the main application.
     */
    protected void button_Cancel_Back() throws Exception
    {
        ApplicationWindow aw;

        switch (curState)
        {

            case VIEW:
                aw = VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
                aw.update();
                break;

            case EDIT:
                if (isCreating())
                {
                    // if there was no active tracker and there was no valid information, go back to main menu.
                    aw = VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
                    aw.update();
                }
                else
                {
                    // cancel the changes made by the edit.
                    myRestoreFields();
                    setState(WindowState.VIEW);
                }
                break;

            case DELETE:
                setState(WindowState.VIEW);
                break;
        }
    }

    protected void button_Edit_Save()
    {
        switch (curState)
        {
            case VIEW:
                myStoreFields();
                setState(WindowState.EDIT);
                break;
            case EDIT:
                try
                {
                    boolean success = saveItem();

                    // if saving the item was successful, change to view mode.
                    if (success)
                        setState(WindowState.VIEW);

                } catch (Exception e)
                {
                    ExceptionHelper.showWarning(e);
                }
                break;
        }
    }

    protected void button_Delete()
    {
        switch (curState)
        {
            case DELETE:
                setState(WindowState.VIEW);
                break;

            default:
                setState(WindowState.DELETE);
                break;
        }
    }

    protected void button_ConfirmDelete() throws Exception
    {
        try
        {
            deleteItem();
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }
        ApplicationWindow aw = VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
        aw.update();
    }

    //endregion

    //region Store fields

    private DataFieldStorage fieldStorage;

    private void myStoreFields()
    {
        fieldStorage = storeFields();
    }

    private void myRestoreFields()
    {
        if (fieldStorage == null)
            return;

        restoreFields(fieldStorage);
    }

    /**
     * Give a complete collection of data-items that can be used to reconstruct the
     * current values in the UI. When an edit is cancelled, `restoreFields` will be
     * called and the subclass will be prompted to restore these same values.
     * @return The data from the fields in the UI.
     */
    protected abstract DataFieldStorage storeFields();

    /**
     * Restore the fields as given according to the storage. The storage will have
     * the same items that were added in `storeFields` (and hence is its opposite).
     * @param storage The storage with items originally given in `storeFields`
     */
    protected abstract void restoreFields(DataFieldStorage storage);


    //endregion

    //region inherited data and methods

    /**
     * @return true iff the current window was created with the intent of creating a
     * new item. Should return false after an item has been initially created / saved or if
     * an existing item is being edited.
     */
    protected abstract boolean isCreating();

    /**
     * Deletion has been confirmed. Requests the subclass to remove the item from storage.
     */
    protected abstract void deleteItem() throws Exception;

    /**
     * Store the current data.
     * @return true if the item was successfully saved, false otherwise
     */
    protected abstract boolean saveItem() throws Exception;

    //endregion







}
