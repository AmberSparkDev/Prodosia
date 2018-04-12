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

import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerBuilder;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import com.Bluefix.Prodosia.Discord.DiscordManager;
import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.GUI.Helpers.EditableWindowPane;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.TaglistClManager;
import com.Bluefix.Prodosia.GUI.Navigation.GuiApplication;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.util.BaringoApiException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;

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
    @FXML public Button button_checkDiscordTag;


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

                button_checkImgurName.setDisable(true);
                button_checkDiscordTag.setDisable(true);

                // permissions
                perm_scrollpane.setDisable(true);
                perm_filter.setDisable(true);
                perm_chkAdmin.setDisable(true);


                break;
            case EDIT:
                lbl_navigation.setText("Edit Tracker");

                txt_imgurName.setDisable(false);
                txt_discordName.setDisable(true);
                txt_discordTag.setDisable(true);
                txt_discordId.setDisable(false);

                lbl_deleteConfirmation.setVisible(false);

                button_checkImgurName.setDisable(false);
                button_checkDiscordTag.setDisable(false);

                // permissions
                perm_chkAdmin.setDisable(false);
                adminCheckboxSelection();

                break;
            case DELETE:
                lbl_deleteConfirmation.setVisible(true);
                break;
        }
    }

    //endregion

    //region initialization

    @FXML private void initialize()
    {
        super.initialize(button_back, button_edit, button_delete, button_confirmDelete);
    }



    private Tracker curTracker;

    /**
     * Clear all the data from the textfields.
     */
    private void clearData()
    {
        txt_imgurName.setText("");
        lbl_imgurId.setText("");
        txt_discordName.setText("");
        txt_discordTag.setText("");
        txt_discordId.setText("");

        initializePermissions();

        previousImgurName = "";
        previousDiscordId = "";

        if (curTracker != null)
        {

            // if imgur-credentials exist, set them up.
            if (curTracker.getImgurName() != null && !curTracker.getImgurName().isEmpty())
            {
                txt_imgurName.setText(curTracker.getImgurName());
                lbl_imgurId.setText("" + curTracker.getImgurId());
            }

            // if discord-credentials exist, set them up.
            if (curTracker.getDiscordId() != null && !curTracker.getDiscordId().isEmpty())
            {
                txt_discordName.setText(curTracker.getDiscordName());
                txt_discordTag.setText(curTracker.getDiscordTag());
                txt_discordId.setText(curTracker.getDiscordId());
            }

            // setup the permissions
            displayPermissions();
        }
    }



    /**
     * Initialize a window with the tracker-information.
     * @param tracker The tracker to initialize on. Can be null.
     */
    public void init(Tracker tracker)
    {
        curTracker = tracker;

        clearData();

        if (tracker != null)
        {
            // clear the update data to prevent the window from updating itself.
            clearUpdate();

            setState(WindowState.VIEW);
        }
        else
            setState(WindowState.EDIT);
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

        ArrayList<String> fields = storage.retrieve();

        txt_imgurName.setText(fields.get(0));
        lbl_imgurId.setText(fields.get(1));
        txt_discordName.setText(fields.get(2));
        txt_discordTag.setText(fields.get(3));
        txt_discordId.setText(fields.get(4));
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
        TrackerHandler.handler().remove(curTracker);
    }

    @Override
    protected boolean saveItem() throws Exception
    {
        // parse the tracker from the fields.
        Tracker newTracker = parseTracker();

        // if the parsed tracker was null, it was invalid and saving failed.
        if (newTracker == null)
            return false;


        TrackerHandler.handler().update(curTracker, newTracker);
        curTracker = newTracker;

        return true;
    }

    //endregion

    //region Api update

    /**
     * Indicate to the system that an update of the data is not required. This is
     * the case when a user has just been loaded in.
     */
    private void clearUpdate()
    {
        previousImgurName = txt_imgurName.getText().trim();
        previousDiscordId = txt_discordId.getText().trim();
    }

    /**
     * Reset the previous stored value and prepare for data update.
     */
    private void clearImgurUpdate()
    {
        previousImgurName = txt_imgurName.getText().trim();
        txt_imgurName.setId("");
        lbl_imgurId.setText("");
    }

    /**
     * Reset the previous stored value and prepare for data update.
     */
    private void clearDiscordUpdate()
    {
        previousDiscordId = txt_discordId.getText().trim();
        txt_discordId.setId("");
        txt_discordName.setText("");
        txt_discordTag.setText("");
    }

    private String previousImgurName = null;
    private String previousDiscordId = null;

    @FXML
    private synchronized void updateImgurData(ActionEvent event)
    {
        if (txt_imgurName.getText().equals(previousImgurName))
            return;

        clearImgurUpdate();

        // if there was an imgur username, refresh it.
        if (!txt_imgurName.getText().trim().isEmpty())
            setImgurId();
    }

    /**
     * Check the imgur name and see if it existed.
     */
    public void setImgurId()
    {
        try
        {

            Account acc = ImgurManager.client().accountService().getAccount(previousImgurName);

            if (acc == null)
            {
                invalidImgurName();
            }
            else
            {
                lbl_imgurId.setText(String.valueOf(acc.getId()));
                txt_imgurName.setId("");
            }

        } catch (BaringoApiException e)
        {
            invalidImgurName();
        } catch (IOException e)
        {
            exceptionAlert(e);
        } catch (URISyntaxException e)
        {
            exceptionAlert(e);
        }
    }

    /**
     * Indicate to the user that the imgur name is invalid.
     */
    private void invalidImgurName()
    {
        lbl_imgurId.setText("");
        txt_imgurName.setId("twInvalidImgurId");
    }


    /**
     * Update the discord data fields. Will only update if one of the fields was invalidated.
     * Will update according to the fields that were changed, giving priority to the id.
     */
    @FXML
    private synchronized void updateDiscordData(ActionEvent event)
    {
        // if none of the items were changed, return.
        if (txt_discordId.getText().equals(previousDiscordId))
            return;

        // update the data.
        clearDiscordUpdate();

        // if there was a discord-id at all, refresh it.
        if (!txt_discordId.getText().trim().isEmpty())
            setDiscordData();
    }

    private void setDiscordData()
    {
        try
        {
            User u = DiscordManager.manager().getUserById(previousDiscordId);

            if (u == null)
            {
                invalidDiscordId();
            }
            else
            {
                txt_discordName.setText(u.getName());
                txt_discordTag.setText(u.getDiscriminator());
                txt_discordId.setId("");
            }

        } catch (Exception e)
        {
            invalidDiscordId();
        }
    }

    private void invalidDiscordId()
    {
        txt_discordName.setText("");
        txt_discordTag.setText("");
        txt_discordId.setId("twInvalidDiscordId");
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
        updateImgurData(null);
        updateDiscordData(null);

        DataValidation valImg = validateImgurData();
        DataValidation valDis = validateDiscordData();

        // if the data was empty for both, issue a warning.
        if (valImg == DataValidation.MISSING && valDis == DataValidation.MISSING)
        {
            alertUser("There was no Tracker data. Please give a valid imgur id and/or discord id to continue.");
            return null;
        }

        // if either of the data was erroneous, warn the user.
        else if (valImg == DataValidation.ERRONEOUS)
        {
            alertUser("The imgur name was detected to be invalid. Check whether the name is correct or complete it to continue.");
            return null;
        }
        else if (valDis == DataValidation.ERRONEOUS)
        {
            alertUser("The discord id was detected to be invalid. Check whether the id is correct or complete it to continue.");
            return null;
        }

        // parse the permissions, then retrieve all the discord / imgur data.
        TrackerPermissions perm = parsePermissions();

        String imgurName = "";
        long imgurId = -1;
        String discordName = "";
        String discordTag = "";
        String discordId = "";

        if (valImg == DataValidation.VALIDATED)
        {
            imgurName = txt_imgurName.getText();
            imgurId = Long.parseLong(lbl_imgurId.getText());
        }

        if (valDis == DataValidation.VALIDATED)
        {
            discordName = txt_discordName.getText();
            discordTag = txt_discordTag.getText();
            discordId = txt_discordId.getText();
        }


        return new Tracker(
                imgurName,
                imgurId,
                discordName,
                discordTag,
                discordId,
                perm);
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
        if (txt_discordName.getText().isEmpty() || txt_discordTag.getText().isEmpty())
        {
            // if no discord id was specified, this is normal behavior.
            if (txt_discordId.getText().trim().isEmpty())
            {
                return DataValidation.MISSING;
            }
            else
            {
                // since there was a discord id but no associated name / tag, it is erroneous.
                return DataValidation.ERRONEOUS;
            }
        }

        // TODO: determine whether the data is valid.
        return DataValidation.VALIDATED;
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

    private void exceptionAlert(Exception ex)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Something went wrong!");
        alert.setHeaderText("Exception!");
        alert.setContentText(ex.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    //endregion


    //region Permission Handling

    @FXML private CheckBox perm_chkAdmin;
    @FXML private VBox perm_taglists;
    @FXML private TextField perm_filter;
    @FXML private ScrollPane perm_scrollpane;

    private TaglistClManager taglistClManager;

    /**
     * Initialize the permissions with a clean array.
     */
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


    /**
     * Display the permissions that correspond to the currently selected tagger.
     */
    private void displayPermissions()
    {
        if (curTracker == null)
            return;

        TrackerPermissions tp = curTracker.getPermissions();

        // if the user is an admin, he gets permissions to everything by default.
        if (tp.getType() == TrackerPermissions.TrackerType.ADMIN)
        {
            perm_chkAdmin.setSelected(true);
            adminCheckboxSelection();
        }

        // if the user has individual permissions to taglists, select them.
        taglistClManager.applyTaglists(tp.getTaglists());
    }

    /**
     * Handle the checkbox selection when the user selects "admin".
     */
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


    /**
     * Parse the permissions that have been selected by the user.
     * @return
     */
    private TrackerPermissions parsePermissions()
    {
        HashSet<Taglist> tl = new HashSet<>();
        TrackerPermissions.TrackerType type;

        if (perm_chkAdmin.isSelected())
            type = TrackerPermissions.TrackerType.ADMIN;
        else
            type = TrackerPermissions.TrackerType.TRACKER;


        for (String s : taglistClManager.getSelectedItems())
        {
            try
            {
                Taglist t = TaglistHandler.getTaglistByAbbreviation(s);

                if (t != null)
                    tl.add(t);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return new TrackerPermissions(type, tl.toArray(new Taglist[0]));
    }




    //endregion






}
