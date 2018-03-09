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

package com.Bluefix.Prodosia.GUI.Helpers;

import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import java.sql.SQLException;

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
    protected void button_Cancel_Back()
    {
        switch (curState)
        {

            case VIEW:
                VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
                break;

            case EDIT:
                if (isCreating())
                {
                    // if there was no active tracker and there was no valid information, go back to main menu.
                    VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
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
                    saveItem();
                } catch (Exception e)
                {
                    ExceptionHelper.showWarning(e);
                }
                setState(WindowState.VIEW);
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

    protected void button_ConfirmDelete()
    {
        try
        {
            deleteItem();
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }
        VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
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
     * @return The data from the fields in the window.
     */
    protected abstract DataFieldStorage storeFields();

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
     * Deletion has been confirmed.
     */
    protected abstract void deleteItem() throws Exception;

    /**
     * Store the current data.
     */
    protected abstract void saveItem() throws Exception;

    //endregion







}
