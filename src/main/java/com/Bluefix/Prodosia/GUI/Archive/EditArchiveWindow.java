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

package com.Bluefix.Prodosia.GUI.Archive;

import com.Bluefix.Prodosia.DataType.Archive.Archive;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataHandler.ArchiveHandler;
import com.Bluefix.Prodosia.Discord.DiscordManager;
import com.Bluefix.Prodosia.GUI.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.GUI.Helpers.EditableWindowPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import net.dv8tion.jda.core.entities.TextChannel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class EditArchiveWindow extends EditableWindowPane
{
    @FXML public Button button_checkDiscord;

    @FXML private Label lbl_navigation;
    @FXML private Label lbl_deleteConfirmation;
    @FXML private Label lbl_taglist;

    @FXML private TextArea ta_filters;
    @FXML private TextField tf_description;


    //region Initialization

    @FXML
    private void initialize()
    {
        super.initialize(button_back, button_edit, button_delete, button_confirmDelete);
    }

    /**
     * The currently selected archive.
     */
    private Archive selectedArchive;

    private Taglist selectedTaglist;

    private void clearData()
    {
        lbl_taglist.setText(selectedTaglist.getAbbreviation());
        tf_discordChannel.setId("");
        tf_description.setId("");

        if (selectedArchive == null)
        {
            tf_discordChannel.setText("");
            lbl_channelName.setText("");
            chk_safe.setSelected(false);
            chk_questionable.setSelected(false);
            chk_explicit.setSelected(false);
            ta_filters.setText("");
            tf_description.setText("");
        }
        else
        {
            // set the discord id and attempt to retrieve the channel name.
            tf_discordChannel.setText(selectedArchive.getChannelId());
            try
            {
                lbl_channelName.setText(DiscordManager.manager().getTextChannelById(selectedArchive.getChannelId()).getName());
            } catch (Exception e)
            {
                tf_discordChannel.setId("awInvalidDiscordChannel");
                lbl_channelName.setText("");
            }

            ta_filters.setText(selectedArchive.getFilters());
            tf_description.setText(selectedArchive.getDescription());
        }

        layoutRatings();
    }


    /**
     * Initialize the Edit Archive Window with the specified archive.
     * @param archive
     */
    public void init(Archive archive) throws SQLException
    {
        if (archive == null)
            throw new IllegalArgumentException("Cannot instantiate an Edit Archive Window without a valid archive.");

        this.selectedTaglist = archive.getTaglist();
        this.selectedArchive = archive;
        this.previousDiscordChannelId = null;

        clearData();

        setState(WindowState.VIEW);
    }

    /**
     * Initialize an empty Edit Archive Window
     * @param taglist The taglist to init to. Cannot be null.
     */
    public void init(Taglist taglist)
    {
        if (taglist == null)
            throw new IllegalArgumentException("Cannot instantiate an Edit Archive Window without a valid taglist.");

        this.selectedTaglist = taglist;
        this.selectedArchive = null;
        this.previousDiscordChannelId = null;

        clearData();

        setState(WindowState.EDIT);
    }

    //endregion


    //region Editable Window Pane Implementation

    @FXML private Button button_back;
    @FXML private Button button_edit;
    @FXML private Button button_delete;
    @FXML private Button button_confirmDelete;

    /**
     * Setup the expected change in layout dependent on the state.
     *
     * @param state the current state of the window.
     */
    @Override
    protected void applyState(WindowState state)
    {
        switch (state)
        {

            case VIEW:
                lbl_navigation.setText("Inspect Archive");
                lbl_deleteConfirmation.setVisible(false);

                tf_discordChannel.setDisable(true);
                button_checkDiscord.setDisable(true);
                chk_safe.setDisable(true);
                chk_questionable.setDisable(true);
                chk_explicit.setDisable(true);
                ta_filters.setDisable(true);
                tf_description.setDisable(true);
                break;

            case EDIT:
                lbl_navigation.setText("Edit Archive");
                lbl_deleteConfirmation.setVisible(false);

                tf_discordChannel.setDisable(false);
                button_checkDiscord.setDisable(false);
                chk_safe.setDisable(false);
                chk_questionable.setDisable(false);
                chk_explicit.setDisable(false);
                ta_filters.setDisable(false);
                tf_description.setDisable(false);
                break;

            case DELETE:
                lbl_deleteConfirmation.setVisible(true);
                break;
        }
    }

    /**
     * @return The data from the fields in the window.
     */
    @Override
    protected DataFieldStorage storeFields()
    {
        return DataFieldStorage.store(
                tf_discordChannel.getText(),
                lbl_channelName.getText(),
                chk_safe.isSelected(),
                chk_questionable.isSelected(),
                chk_explicit.isSelected(),
                ta_filters.getText(),
                tf_description.getText()
        );
    }

    @Override
    protected void restoreFields(DataFieldStorage storage)
    {
        ArrayList<Object> fields = storage.retrieve();

        tf_discordChannel.setText((String)fields.get(0));
        lbl_channelName.setText((String)fields.get(1));
        chk_safe.setSelected((boolean) fields.get(2));
        chk_questionable.setSelected((boolean) fields.get(3));
        chk_explicit.setSelected((boolean) fields.get(4));
        ta_filters.setText((String)fields.get(5));
        tf_description.setText((String)fields.get(6));
    }

    /**
     * @return true iff the current window was created with the intent of creating a
     * new item. Should return false after an item has been initially created / saved or if
     * an existing item is being edited.
     */
    @Override
    protected boolean isCreating()
    {
        return selectedArchive == null;
    }

    /**
     * Deletion has been confirmed.
     */
    @Override
    protected boolean deleteItem() throws Exception
    {
        ArchiveHandler.handler().remove(selectedArchive);
        selectedArchive = null;
        return true;
    }

    /**
     * Store the current data.
     *
     * @return true if the item was successfully saved, false otherwise
     */
    @Override
    protected boolean saveItem() throws Exception
    {
        Archive a = parseArchive();

        // if the archive was null, indicate failure.
        if (a == null)
            return false;

        ArchiveHandler.handler().update(selectedArchive, a);
        selectedArchive = a;

        return true;
    }



    //endregion

    //region Storage logic

    /**
     * Attempt to parse an archive from the GUI elements. Will give an appropriate warning message
     * if it fails.
     * @return
     */
    private Archive parseArchive()
    {
        // update the discord channel if necessary.
        updateDiscordChannel(null);

        String discordId = tf_discordChannel.getText();

        // if the channel name is empty, the discord channel id was incorrect.
        if (lbl_channelName.getText().trim().isEmpty())
        {
            alertDiscordChannelIncorrect();
            return null;
        }

        // check the ratings, if applicable.
        HashSet<Rating> ratings = parseRatings();

        if (selectedTaglist.hasRatings() && ratings.isEmpty())
        {
            alertNoRatingsSpecified();
            return null;
        }

        // the description should be not-empty
        String description = tf_description.getText().trim();

        if (description.isEmpty())
        {
            tf_description.setId("awInvalidDescription");
            alertDescriptionEmpty();
            return null;
        }
        else
        {
            tf_description.setId("");
        }

        // everything passed, create a new Archive object.
        return new Archive(selectedTaglist, description, discordId, ratings, ta_filters.getText().trim());
    }

    private static void alertDiscordChannelIncorrect()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error! The discord channel id was invalid.");
        alert.showAndWait();
    }

    private static void alertNoRatingsSpecified()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error! There were no ratings specified. ");
        alert.showAndWait();
    }

    private static void alertDescriptionEmpty()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error! The description cannot be empty. ");
        alert.showAndWait();
    }



    //endregion

    //region GUI logic



    //endregion

    //region DiscordChannel

    @FXML private TextField tf_discordChannel;
    @FXML private Label lbl_channelName;

    private String previousDiscordChannelId;

    @FXML
    private void updateDiscordChannel(ActionEvent actionEvent)
    {
        String discordId = tf_discordChannel.getText();

        // if the query was the same, skip it.
        if (Objects.equals(discordId, previousDiscordChannelId))
            return;

        previousDiscordChannelId = discordId;

        try
        {
            TextChannel tc = DiscordManager.manager().getTextChannelById(discordId);
            tf_discordChannel.setId("");

            String channelName = tc.getName();
            if (channelName == null)
                channelName = "[no name]";


            lbl_channelName.setText(channelName);

        } catch (Exception e)
        {


            tf_discordChannel.setId("awInvalidDiscordChannel");
            lbl_channelName.setText("");
        }

    }

    //endregion

    //region Ratings

    @FXML private CheckBox chk_safe;
    @FXML private CheckBox chk_questionable;
    @FXML private CheckBox chk_explicit;

    @FXML private Label lbl_ratingsInfo;
    @FXML private Label lbl_ratingsMarker;

    /**
     * Perform layout on the ratings for this archive. Hides the ratings options
     * if the taglist does not incorporate ratings.
     */
    private void layoutRatings()
    {
        if (selectedTaglist.hasRatings())
        {
            chk_safe.setVisible(true);
            chk_questionable.setVisible(true);
            chk_explicit.setVisible(true);
            lbl_ratingsInfo.setVisible(true);
            lbl_ratingsMarker.setVisible(true);

            // layout the archive if it exists.
            if (selectedArchive == null)
            {
                chk_safe.setSelected(false);
                chk_questionable.setSelected(false);
                chk_explicit.setSelected(false);
            }
            else
            {
                chk_safe.setSelected(selectedArchive.getRatings().contains(Rating.SAFE));
                chk_questionable.setSelected(selectedArchive.getRatings().contains(Rating.QUESTIONABLE));
                chk_explicit.setSelected(selectedArchive.getRatings().contains(Rating.EXPLICIT));
            }
        }
        else
        {
            chk_safe.setVisible(false);
            chk_questionable.setVisible(false);
            chk_explicit.setVisible(false);
            lbl_ratingsInfo.setVisible(false);
            lbl_ratingsMarker.setVisible(false);
        }
    }

    private HashSet<Rating> parseRatings()
    {
        HashSet<Rating> output = new HashSet<>();

        if (chk_safe.isSelected())
            output.add(Rating.SAFE);

        if (chk_questionable.isSelected())
            output.add(Rating.QUESTIONABLE);

        if (chk_explicit.isSelected())
            output.add(Rating.EXPLICIT);

        return output;
    }



    //endregion
}
