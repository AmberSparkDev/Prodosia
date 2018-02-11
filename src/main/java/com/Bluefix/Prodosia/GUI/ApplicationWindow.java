package com.Bluefix.Prodosia.GUI;

import com.Bluefix.Prodosia.DataType.Data;
import com.Bluefix.Prodosia.DataType.Rating;
import com.Bluefix.Prodosia.GUI.Helpers.*;
import com.Bluefix.Prodosia.GUI.Managers.GuiListManager;
import com.Bluefix.Prodosia.GUI.Managers.TaglistListManager;
import com.Bluefix.Prodosia.GUI.Managers.TrackerListManager;
import com.Bluefix.Prodosia.GUI.Managers.UserListManager;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.Taglist.EditTaglistWindow;
import com.Bluefix.Prodosia.GUI.Tracker.EditTrackerWindow;
import com.Bluefix.Prodosia.GUI.User.EditUserWindow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


public class ApplicationWindow
{

    //region Status

    @FXML private TextArea statusConsole;

    @FXML
    private void gotoApiKeys()
    {
        // set `ApiKeysWindow` as active window.
        VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
    }

    //endregion

    //region Tag a Post

    @FXML private TextField tap_url;

    @FXML private TextArea tap_parentComment;

    @FXML private MenuButton tap_ratingButton;

    @FXML private VBox tap_taglistSelector;

    @FXML public void tap_ratingA(ActionEvent actionEvent)
    {
        selectRating(Rating.ALL);
    }

    @FXML public void tap_ratingS(ActionEvent actionEvent)
    {
        selectRating(Rating.SAFE);
    }

    @FXML public void tap_ratingQ(ActionEvent actionEvent)
    {
        selectRating(Rating.QUESTIONABLE);
    }

    @FXML public void tap_ratingE(ActionEvent actionEvent)
    {
        selectRating(Rating.EXPLICIT);
    }

    private Rating tap_rating;

    private void selectRating(Rating rating)
    {
        tap_rating = rating;

        tap_ratingButton.setText(Data.ToString(rating));
    }




    //endregion


    //region Trackers

    @FXML private VBox trackers_overview;

    public void addTracker(ActionEvent actionEvent)
    {
        EditTrackerWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TRACKER_EDIT);
        controller.initialize();
    }

    //endregion

    //region Taglists

    @FXML public VBox taglists_overview;

    public void addTaglist(ActionEvent actionEvent)
    {
        EditTaglistWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TAGLIST_EDIT);
        controller.initialize();
    }

    //endregion

    //region Users

    @FXML public VBox users_overview;

    public void addUser(ActionEvent actionEvent)
    {
        EditUserWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.USER_EDIT);
        controller.initialize();
    }

    //endregion

    public ApplicationWindow()
    {

    }
    

    //region Initialization

    @FXML
    private void initialize()
    {
        /* Status */
        statusConsole.setText("This is the console");


        /* Tag a Post */
        initializeTap();

        // initialize list managers
        setupListManagers();



    }

    private void initializeTap()
    {
        tap_url.setText("tap_url");
        tap_parentComment.setText("tap_parentComment");
        tap_parentComment.setDisable(true);
        selectRating(Rating.ALL);
        TaglistGuiHelper.fillVBoxWithTaglistCheckboxes(tap_taglistSelector);
    }

    //endregion

    //region List Managers

    @FXML public TextField tracker_filter;
    @FXML public TextField taglist_filter;
    @FXML public TextField user_filter;

    private GuiListManager[] listManagers;


    private void setupListManagers()
    {
        listManagers = new GuiListManager[3];

        // tracker list
        listManagers[0] = new TrackerListManager(trackers_overview);
        listManagers[1] = new TaglistListManager(taglists_overview);
        listManagers[2] = new UserListManager(users_overview);

        // setup listeners for their respective filters.

        tracker_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            listManagers[0].filter(newVal);
        });

        taglist_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            listManagers[1].filter(newVal);
        });

        user_filter.textProperty().addListener((o, oldVal, newVal) ->
        {
            listManagers[2].filter(newVal);
        });
    }


    //endregion



}
