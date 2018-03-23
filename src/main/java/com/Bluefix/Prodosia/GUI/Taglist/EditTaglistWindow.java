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

package com.Bluefix.Prodosia.GUI.Taglist;

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.GUI.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.GUI.Helpers.EditableWindowPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;

public class EditTaglistWindow extends EditableWindowPane
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
    @FXML public TextField tf_abbreviation;
    @FXML public CheckBox chk_ratings;
    @FXML public TextArea ta_description;


    public void btn_CancelBack(ActionEvent actionEvent) throws Exception
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

    public void btn_confirmDelete(ActionEvent actionEvent) throws Exception
    {
        super.button_ConfirmDelete();
    }


    //endregion

    //region State handling

    @Override
    protected void applyState(EditableWindowPane.WindowState state)
    {
        switch (state)
        {

            case VIEW:
                lbl_navigation.setText("Inspect Taglist");

                lbl_deleteConfirmation.setVisible(false);
                button_confirmDelete.setVisible(false);
                button_delete.setDisable(false);
                button_delete.setText("Delete");
                button_back.setText("Back");
                button_edit.setText("Edit");
                button_back.setDisable(false);
                button_edit.setDisable(false);

                tf_abbreviation.setDisable(true);
                ta_description.setDisable(true);
                chk_ratings.setDisable(true);
                break;

            case EDIT:
                lbl_navigation.setText("Edit Taglist");

                lbl_deleteConfirmation.setVisible(false);
                button_confirmDelete.setVisible(false);
                button_delete.setDisable(true);
                button_back.setText("Cancel");
                button_edit.setText("Save");
                button_back.setDisable(false);
                button_edit.setDisable(false);

                tf_abbreviation.setDisable(false);
                ta_description.setDisable(false);
                chk_ratings.setDisable(false);
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

    private Taglist curTaglist;

    private void clearData()
    {
        tf_abbreviation.setText("");
        ta_description.setText("");
        chk_ratings.setSelected(false);
    }

    /**
     * Initialize an empty window
     */
    public void initialize()
    {
        curTaglist = null;

        clearData();

        setState(WindowState.EDIT);
    }

    /**
     * Initialize a window with the taglist information.
     * @param taglist the taglist to be initialized on.
     */
    public void initialize(Taglist taglist)
    {
        curTaglist = taglist;

        clearData();

        tf_abbreviation.setText(taglist.getAbbreviation());
        ta_description.setText(taglist.getDescription());
        chk_ratings.setSelected(taglist.hasRatings());


        setState(WindowState.VIEW);
    }

    //endregion

    //region Datafield recovery

    @Override
    protected DataFieldStorage storeFields()
    {
        return DataFieldStorage.store(
            tf_abbreviation.getText(),
            ta_description.getText(),
                (chk_ratings.isSelected() ? "1" : "0")
        );
    }

    @Override
    protected void restoreFields(DataFieldStorage storage)
    {
        ArrayList<String> fields = storage.retrieve();

        tf_abbreviation.setText(fields.get(0));
        ta_description.setText(fields.get(1));
        chk_ratings.setSelected("1".equals(fields.get(2)));
    }

    //endregion

    //region inherited methods

    /**
     * @return true iff we are still in the process of creating the tracker.
     */
    protected boolean isCreating()
    {
        return curTaglist == null;
    }

    @Override
    protected void deleteItem() throws Exception
    {
        TaglistHandler.handler().remove(curTaglist);
    }

    @Override
    protected boolean saveItem() throws Exception
    {
        Taglist tl = parseTaglist();

        // if the taglist was null, indicate failure to save.
        if (tl == null)
            return false;

        TaglistHandler.handler().update(curTaglist, tl);
        curTaglist = tl;

        return true;
    }

    //endregion

    //region Taglist Handling

    /**
     * Returns a valid taglist if applicable, or null otherwise.
     * @return
     */
    private Taglist parseTaglist() throws Exception
    {
        if (tf_abbreviation.getText().isEmpty())
        {
            // alert the user that the abbreviation cannot be empty.
            alertTaglistAbbreviationEmpty();
            tf_abbreviation.setId("tlwInvalidAbbreviation");
            return null;
        }
        else
            tf_abbreviation.setId("");

        // if this isn't a new taglist, use the id of the old taglist.
        if (curTaglist == null)
            return new Taglist(
                tf_abbreviation.getText(),
                ta_description.getText(),
                chk_ratings.isSelected());
        else
            return new Taglist(
                    curTaglist.getId(),
                    tf_abbreviation.getText(),
                    ta_description.getText(),
                    chk_ratings.isSelected());
    }

    private static void alertTaglistAbbreviationEmpty()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error! Abbreviation cannot be empty.");
        //alert.setContentText("Ooops, there was an error!");

        alert.showAndWait();
    }

    //endregion


}
