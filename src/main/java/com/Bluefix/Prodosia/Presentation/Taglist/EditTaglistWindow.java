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

package com.Bluefix.Prodosia.Presentation.Taglist;

import com.Bluefix.Prodosia.Data.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.Data.DataHandler.UserHandler;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Presentation.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.Presentation.Helpers.EditableWindowPane;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.Optional;

public class EditTaglistWindow extends EditableWindowPane
{

    //region Textfields and labels

    @FXML public Label lbl_navigation;
    @FXML public Label lbl_deleteConfirmation;
    @FXML public TextField tf_abbreviation;
    @FXML public CheckBox chk_ratings;
    @FXML public TextArea ta_description;

    //endregion

    //region Editable Window Pane GUI components

    @FXML public Button button_back;
    @FXML public Button button_edit;
    @FXML public Button button_delete;
    @FXML public Button button_confirmDelete;

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

                tf_abbreviation.setDisable(true);
                ta_description.setDisable(true);
                chk_ratings.setDisable(true);
                break;

            case EDIT:
                lbl_navigation.setText("Edit Taglist");

                lbl_deleteConfirmation.setVisible(false);

                tf_abbreviation.setDisable(false);
                ta_description.setDisable(false);
                chk_ratings.setDisable(false);
                break;

            case DELETE:
                lbl_deleteConfirmation.setVisible(true);
                break;
        }
    }

    //endregion

    //region initialization

    /**
     * Initialize this item with the specified buttons.
     */
    @FXML
    private void initialize()
    {
        super.initialize(button_back, button_edit, button_delete, button_confirmDelete);
    }


    private Taglist curTaglist;

    private void clearData()
    {
        if (curTaglist == null)
        {
            tf_abbreviation.setText("");
            ta_description.setText("");
            chk_ratings.setSelected(false);
        }
        else
        {
            tf_abbreviation.setText(curTaglist.getAbbreviation());
            ta_description.setText(curTaglist.getDescription());
            chk_ratings.setSelected(curTaglist.hasRatings());
        }
    }

    /**
     * Initialize a window with the taglist information.
     * @param taglist the taglist to be initialized on.
     */
    public void init(Taglist taglist)
    {
        curTaglist = taglist;

        clearData();

        if (taglist == null)
            setState(WindowState.EDIT);
        else
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
     * @return true iff we are still in the process of creating the taglist.
     */
    protected boolean isCreating()
    {
        return curTaglist == null;
    }

    @Override
    protected boolean deleteItem() throws Exception
    {
        // check how many users will be removed because of this deletion.
        int amount = UserHandler.amountOfUserDependencies(curTaglist);

        boolean shouldDelete = amount <= 0;

        if (amount > 0)
        {
            // if there are no users that will be deleted by this, skip the confirmation phase.
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete " + curTaglist.getAbbreviation());
            alert.setHeaderText("You are about to delete " + curTaglist.getAbbreviation() + "!");
            alert.setContentText("This action will also remove " + amount + " users from the system " +
                    "since they will no longer have any taglists they are subscribed to. Do you wish " +
                    "to continue?");

            Optional<ButtonType> result = alert.showAndWait();

            shouldDelete = shouldDelete || result.get() == ButtonType.OK;
        }


        if (shouldDelete){
            // temporarily disable user updating
            UserHandler.handler().enableGuiUpdate(false);
            TaglistHandler.handler().clear(curTaglist);
            UserHandler.handler().enableGuiUpdate(true);
            curTaglist = null;
            return true;
        }

        return false;
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
