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

package com.Bluefix.Prodosia.GUI.User;

import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.Helpers.DataFieldStorage;
import com.Bluefix.Prodosia.GUI.Helpers.EditableWindowPane;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.UserSubscriptionListManager;
import com.Bluefix.Prodosia.Imgur.ImgurApi.ImgurManager;
import com.github.kskelm.baringo.model.Account;
import com.github.kskelm.baringo.util.BaringoApiException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

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
    @FXML public TextField tf_imgurName;
    @FXML public Label lbl_imgurId;

    @FXML public VBox vbox_userSubscriptions;
    @FXML public TextField tf_usFilter;
    @FXML public Label lbl_selectedTaglist;
    @FXML public CheckBox chk_safe;
    @FXML public CheckBox chk_questionable;
    @FXML public CheckBox chk_explicit;
    @FXML public TextArea ta_filters;
    @FXML public Button button_clearUserSubscription;
    @FXML public Label lbl_ratingsIndicator;
    @FXML public Label inf_ratings;
    @FXML public Button button_checkImgurName;





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

                tf_imgurName.setDisable(true);
                button_checkImgurName.setDisable(true);

                tf_usFilter.setDisable(true);
                vbox_userSubscriptions.setDisable(true);
                selectedTaglist = null;
                clearSelectedUserSubscription();

                break;

            case EDIT:
                lbl_navigation.setText("Edit User");

                lbl_deleteConfirmation.setVisible(false);

                tf_imgurName.setDisable(false);
                button_checkImgurName.setDisable(false);

                tf_usFilter.setDisable(false);
                vbox_userSubscriptions.setDisable(false);
                selectedTaglist = null;
                clearSelectedUserSubscription();
                break;

            case DELETE:
                lbl_deleteConfirmation.setVisible(true);
                break;
        }
    }

    //endregion

    //region initialization

    private User curUser;
    private MyUserSubscriptionListManager uslm;

    private void clearData()
    {
        tf_imgurName.setText("");
        lbl_imgurId.setText("");

        tf_usFilter.setText("");
        lbl_selectedTaglist.setText("");
        chk_safe.setSelected(false);
        chk_questionable.setSelected(false);
        chk_explicit.setSelected(false);
        ta_filters.setText("");
    }

    @FXML
    private void initialize()
    {
        super.initialize(button_back, button_edit, button_delete, button_confirmDelete);

        // setup a listener for the filter.
        tf_usFilter.textProperty().addListener((o, oldVal, newVal) ->
        {
            uslm.filter(newVal);
        });

        ta_filters.textProperty().addListener((o, oldVal, newVal) -> storeSubscription());

        chk_safe.setOnAction((e) -> storeSubscription());
        chk_questionable.setOnAction((e) -> storeSubscription());
        chk_explicit.setOnAction((e) -> storeSubscription());
    }


    /**
     * Initialize a window with the user-information.
     * @param user the user to init on. Can be null for an empty window.
     */
    public void init(User user) throws Exception
    {
        curUser = user;
        previousImgurName = null;

        clearData();

        // intialize the list manager for the user subscriptions.
        uslm = new MyUserSubscriptionListManager(vbox_userSubscriptions, this);
        uslm.initializeUser(curUser);


        if (curUser == null)
        {
            tf_imgurName.setText("");
            lbl_imgurId.setText("");
            setState(WindowState.EDIT);
        }
        else
        {
            tf_imgurName.setText(curUser.getImgurName());
            lbl_imgurId.setText("" + curUser.getImgurId());
            setState(WindowState.VIEW);
        }
    }

    //endregion

    //region Datafield recovery

    @Override
    protected DataFieldStorage storeFields()
    {
        return DataFieldStorage.store(
                tf_imgurName.getText(),
                lbl_imgurId.getText(),
                new HashSet<>(uslm.getSubscriptions()));
    }

    @Override
    protected void restoreFields(DataFieldStorage storage)
    {
        ArrayList<Object> fields = storage.retrieve();

        tf_imgurName.setText((String)fields.get(0));
        lbl_imgurId.setText((String)fields.get(1));
        uslm.setSubscriptions((HashSet<UserSubscription>)fields.get(2));
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
    protected void deleteItem() throws Exception
    {
        UserHandler.handler().remove(curUser);
    }

    /**
     * Store the item if applicable and replace the active user with this data.
     * @return
     */
    @Override
    protected boolean saveItem() throws Exception
    {
        User newUser = parseUser();

        if (newUser == null)
            return false;

        UserHandler.handler().update(curUser, newUser);
        curUser = newUser;
        return true;
    }


    private String previousImgurName;

    /**
     * Retrieve the imgur id if the imgur name was changed from what it was before.
     * @param actionEvent
     */
    public void checkImgurname(ActionEvent actionEvent)
    {
        // skip if the name hasn't changed.
        if (tf_imgurName.getText().trim().equals(previousImgurName))
            return;

        try
        {
            Account acc = ImgurManager.client().accountService().getAccount(tf_imgurName.getText().trim());

            if (acc == null)
            {
                tf_imgurName.setId("uwInvalidImgurName");
                lbl_imgurId.setText("");
            }
            else
            {
                tf_imgurName.setId("");
                lbl_imgurId.setText("" + acc.getId());
            }

        } catch (IOException e)
        {
            ExceptionHelper.showWarning(e);
        } catch (BaringoApiException e)
        {
            tf_imgurName.setId("uwInvalidImgurName");
            lbl_imgurId.setText("");
        } catch (URISyntaxException e)
        {
            ExceptionHelper.showWarning(e);
        }
    }




    //endregion

    //region Tracker Handling

    /**
     * Parse a user from the data. If no valid user could be parsed, return null.
     * @return
     */
    private User parseUser() throws Exception
    {
        // if necessary, update the imgur name.
        checkImgurname(null);

        String imgurName = tf_imgurName.getText().trim();
        String imgurId = lbl_imgurId.getText().trim();

        // if either the imgur name or id is missing, return.
        if (imgurName.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error!");
            alert.setContentText("Please provide an Imgur name.");

            alert.showAndWait();

            return null;
        }

        if (imgurId.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error!");
            alert.setContentText("The Imgur name was erroneous. Please check the name.");

            alert.showAndWait();

            return null;
        }

        // retrieve the User Subscriptions
        HashSet<UserSubscription> uss = uslm.getSubscriptions();

        if (uss.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error!");
            alert.setContentText("There was no subscription data detected. Please subscribe the user to at least one taglist.");

            alert.showAndWait();

            return null;
        }

        return new User(imgurName, Long.parseLong(imgurId), uss);
    }

    //endregion


    //region Subscriptions

    private Taglist selectedTaglist;

    /**
     * Clear the selected taglist from the subscription list manager.
     * @param actionEvent
     */
    @FXML
    private void clearSelectedTaglist(ActionEvent actionEvent)
    {
        this.uslm.clearUserSubscription(selectedTaglist);

        selectedTaglist = null;
        clearSelectedUserSubscription();
    }


    private synchronized void setSelectedTaglist(Taglist tl, UserSubscription us)
    {
        // if the selected taglist was the same as the already selected one,
        // deselect it.
        if (tl == null || tl.equals(selectedTaglist))
        {
            selectedTaglist = null;
            clearSelectedUserSubscription();
            return;
        }

        selectedTaglist = null;

        if (us == null)
            prepareEmptyUserSubscription(tl);
        else
            // setup the user subscription data
            setUserSubscriptionData(us);

        // dependent on the taglist, display either ratings or an option to subscribe.
        if (tl.hasRatings())
            setRatingUserSubscription(us);
        else
            setNoRatingUserSubscription(us);

        selectedTaglist = tl;
    }


    private synchronized void clearSelectedUserSubscription()
    {
        lbl_selectedTaglist.setText("");
        lbl_ratingsIndicator.setVisible(true);
        inf_ratings.setVisible(true);

        chk_safe.setSelected(false);
        chk_questionable.setSelected(false);
        chk_explicit.setSelected(false);

        chk_safe.setText("Safe");
        chk_questionable.setVisible(true);
        chk_explicit.setVisible(true);

        chk_safe.setDisable(true);
        chk_questionable.setDisable(true);
        chk_explicit.setDisable(true);

        ta_filters.setText("");
        ta_filters.setDisable(true);

        button_clearUserSubscription.setDisable(true);
    }

    private synchronized void prepareEmptyUserSubscription(Taglist tl)
    {
        lbl_selectedTaglist.setText(tl.getAbbreviation());

        ta_filters.setDisable(false);
        ta_filters.setText("");
    }

    private synchronized void setUserSubscriptionData(UserSubscription us)
    {
        lbl_selectedTaglist.setText(us.getTaglist().getAbbreviation());

        ta_filters.setText(us.getFilters());
        ta_filters.setDisable(false);

        button_clearUserSubscription.setDisable(false);
    }


    private void setRatingUserSubscription(UserSubscription us)
    {
        // prepare the GUI elements for ratings
        chk_safe.setText("Safe");
        chk_safe.setDisable(false);
        chk_questionable.setVisible(true);
        chk_questionable.setDisable(false);
        chk_explicit.setVisible(true);
        chk_explicit.setDisable(false);

        lbl_ratingsIndicator.setVisible(true);
        inf_ratings.setVisible(true);

        if (us == null)
        {
            chk_safe.setSelected(false);
            chk_questionable.setSelected(false);
            chk_explicit.setSelected(false);
        }
        else
        {
            chk_safe.setSelected(us.hasRating(Rating.SAFE));
            chk_questionable.setSelected(us.hasRating(Rating.QUESTIONABLE));
            chk_explicit.setSelected(us.hasRating(Rating.EXPLICIT));
        }
    }

    private void setNoRatingUserSubscription(UserSubscription us)
    {
        // prepare the GUI elements for no ratings.
        chk_safe.setText("Subscribe");
        chk_safe.setDisable(false);
        chk_questionable.setVisible(false);
        chk_explicit.setVisible(false);

        lbl_ratingsIndicator.setVisible(false);
        inf_ratings.setVisible(false);

        chk_safe.setSelected(us != null);
    }


    /**
     * Store the subscription to the list manager.
     */
    private void storeSubscription()
    {
        if (selectedTaglist == null)
            return;

        UserSubscription us = parseUserSubscription();

        System.out.println("#### us == null ? " + (us == null));

        uslm.updateUserSubscription(selectedTaglist, us);

        if (us != null)
            button_clearUserSubscription.setDisable(false);
        else
            button_clearUserSubscription.setDisable(true);
    }

    /**
     * Parse a user-subscription from the UI.
     * @return A user-subscription if applicable, or null if the data was completely empty.
     */
    private UserSubscription parseUserSubscription()
    {
        // return null if no taglist was selected.
        if (selectedTaglist == null)
            return null;

        // if the taglist uses ratings, check if at least one rating was selected.
        if (selectedTaglist.hasRatings())
        {
            if (    !chk_safe.isSelected() &&
                    !chk_questionable.isSelected() &&
                    !chk_explicit.isSelected())
                return null;
        }
        else
        {
            // if no filter was selected and the subscription checkbox was unselected, return null.
            if (ta_filters.getText().trim().isEmpty() &&
                    !chk_safe.isSelected())
                return null;

            // if there was a filter message, automatically enable the subscription check.
            if (!ta_filters.getText().trim().isEmpty())
                chk_safe.setSelected(true);
        }

        // parse the User Subscription
        HashSet<Rating> ratings = new HashSet<>();

        if (selectedTaglist.hasRatings())
        {
            if (chk_safe.isSelected())
                ratings.add(Rating.SAFE);

            if (chk_questionable.isSelected())
                ratings.add(Rating.QUESTIONABLE);

            if (chk_explicit.isSelected())
                ratings.add(Rating.EXPLICIT);
        }

        return new UserSubscription(selectedTaglist, ratings, ta_filters.getText().trim());
    }





    private static class MyUserSubscriptionListManager extends UserSubscriptionListManager
    {
        private EditUserWindow euw;


        public MyUserSubscriptionListManager(VBox vbox, EditUserWindow euw) throws Exception
        {
            super(vbox);
            this.euw = euw;
        }

        /**
         * This method indicates that the specified taglist has been selected.
         * Will also give a user-subscription object if it was already set.
         *
         * @param tl The taglist that has been selected.
         * @param us The UserSubscription if it existed, or null otherwise.
         */
        @Override
        public void taglistSelected(Taglist tl, UserSubscription us)
        {
            euw.setSelectedTaglist(tl, us);
        }
    }

    //endregion



}
