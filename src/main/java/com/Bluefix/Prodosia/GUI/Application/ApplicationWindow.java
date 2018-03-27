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

package com.Bluefix.Prodosia.GUI.Application;

import com.Bluefix.Prodosia.DataHandler.CommandPrefixStorage;
import com.Bluefix.Prodosia.DataHandler.TaglistHandler;
import com.Bluefix.Prodosia.DataHandler.TrackerHandler;
import com.Bluefix.Prodosia.DataHandler.UserHandler;
import com.Bluefix.Prodosia.DataType.Command.CommandPrefix;
import com.Bluefix.Prodosia.DataType.Data;
import com.Bluefix.Prodosia.DataType.Taglist.Rating;
import com.Bluefix.Prodosia.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.DataType.Tracker.Tracker;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerBuilder;
import com.Bluefix.Prodosia.DataType.Tracker.TrackerPermissions;
import com.Bluefix.Prodosia.DataType.User.User;
import com.Bluefix.Prodosia.DataType.User.UserSubscription;
import com.Bluefix.Prodosia.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.GUI.ApiKeysWindow;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.GuiCheckboxListManager;
import com.Bluefix.Prodosia.GUI.Managers.CheckboxListManager.TaglistClManager;
import com.Bluefix.Prodosia.GUI.Managers.ListManager.GuiListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.TaglistListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.TrackerListManager;
import com.Bluefix.Prodosia.GUI.Managers.ButtonListManager.UserListManager;
import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.GUI.Taglist.EditTaglistWindow;
import com.Bluefix.Prodosia.GUI.Tracker.EditTrackerWindow;
import com.Bluefix.Prodosia.GUI.User.EditUserWindow;
import com.Bluefix.Prodosia.Logger.Logger;
import com.Bluefix.Prodosia.Module.TestModule;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.HashSet;


public class ApplicationWindow
{
    //region Archives

    @FXML private VBox arch_taglistOverview;
    @FXML private Button arch_addArchiveButton;
    @FXML private Label arch_selectedTaglist;
    @FXML private VBox arch_selectionBox;

    /**
     * The ApplicationWindow_Archive class manages all logic necessary for the Archives to function.
     */
    private ApplicationWindow_Archive archiveGui;

    private void initializeArchive() throws Exception
    {
        if (archiveGui != null)
            return;

        archiveGui = new ApplicationWindow_Archive(arch_taglistOverview, arch_selectionBox, arch_selectedTaglist, arch_addArchiveButton);
    }

    //endregion

    //region Status

    @FXML private TextArea statusConsole;

    @FXML
    private void gotoApiKeys()
    {
        // set `ApiKeysWindow` as active window.
        ApiKeysWindow akw = VistaNavigator.loadVista(VistaNavigator.AppStage.API_KEYS);
        akw.initialize();
    }

    private void initializeStatusWindow()
    {
        // set the output of the logger to the textarea
        Logger.setupOutput(statusConsole);
    }



    //endregion

    //region Tag a Post

    @FXML private TextField tap_url;

    @FXML private TextArea tap_parentComment;

    @FXML private MenuButton tap_ratingButton;

    @FXML private VBox tap_taglistSelector;

    @FXML private void tap_ratingA(ActionEvent actionEvent)
    {
        selectRating(Rating.ALL);
    }

    @FXML private void tap_ratingS(ActionEvent actionEvent)
    {
        selectRating(Rating.SAFE);
    }

    @FXML private void tap_ratingQ(ActionEvent actionEvent)
    {
        selectRating(Rating.QUESTIONABLE);
    }

    @FXML private void tap_ratingE(ActionEvent actionEvent)
    {
        selectRating(Rating.EXPLICIT);
    }

    private Rating tap_rating;

    private void selectRating(Rating rating)
    {
        tap_rating = rating;

        tap_ratingButton.setText(Data.ToString(rating));
    }


    @FXML
    private void tap_execute(ActionEvent actionEvent)
    {
        String[] items = tap_taglist_cl.getSelectedItems();

        System.out.println("Selected items:");

        for (String i : items)
        {
            System.out.println(i);
        }

        System.out.println();
    }

    //endregion


    //region Trackers

    @FXML private VBox trackers_overview;

    @FXML
    private void addTracker(ActionEvent actionEvent)
    {
        EditTrackerWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TRACKER_EDIT);
        controller.init(null);
    }

    //endregion

    //region Taglists

    @FXML private VBox taglists_overview;

    @FXML
    private void addTaglist(ActionEvent actionEvent)
    {
        EditTaglistWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.TAGLIST_EDIT);
        controller.init(null);
    }

    //endregion

    //region Users

    @FXML private VBox users_overview;

    @FXML
    private void addUser(ActionEvent actionEvent) throws Exception
    {
        EditUserWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.USER_EDIT);
        controller.init(null);
    }

    //endregion

    //region Constructor

    public ApplicationWindow()
    {

    }

    //endregion

    //region Initialization

    @FXML
    private void initialize() throws Exception
    {
        /* Status */
        initializeStatusWindow();



        /* Tag a Post */
        initializeTap();

        // init list managers
        setupListManagers();

        // init Archive
        initializeArchive();

    }

    private void initializeTap()
    {
        tap_url.setText("tap_url");
        tap_parentComment.setText("tap_parentComment");
        tap_parentComment.setDisable(true);
        selectRating(Rating.ALL);
    }

    //endregion

    //region List Managers

    @FXML private TextField tracker_filter;
    @FXML private TextField taglist_filter;
    @FXML private TextField user_filter;

    private GuiListManager[] listManagers;

    private GuiCheckboxListManager tap_taglist_cl;


    private void setupListManagers()
    {
        listManagers = new GuiListManager[3];

        // tracker list
        try
        {
            listManagers[0] = new TrackerListManager(trackers_overview);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        try
        {
            listManagers[1] = new TaglistListManager(taglists_overview);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        try
        {
            listManagers[2] = new UserListManager(users_overview);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

        try
        {
            // setup checkbox gui list managers.
            tap_taglist_cl = new TaglistClManager(tap_taglistSelector);
        } catch (Exception e)
        {
            ExceptionHelper.showWarning(e);
        }

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

    /**
     * Update the components now that we changed data somewhere else.
     */
    public void update() throws Exception
    {
        for (GuiListManager glm : listManagers)
        {
            glm.update();
        }

        // update the archiving functionality.
        archiveGui.update();
    }




    //endregion

    TestModule tm;
    boolean isOn = false;

    /**
     * Temporary test method.
     * @param actionEvent
     */
    public void test(ActionEvent actionEvent) throws Exception
    {
        addStewTracker();
        addPrefix();
        addSimpleUserbase();
    }

    private static void addStewTracker() throws Exception
    {
        Tracker myTracker = TrackerBuilder.builder()
                .setImgurName("mashedstew")
                .setPermissions(new TrackerPermissions(TrackerPermissions.TrackerType.ADMIN))
                .build();

        TrackerHandler.handler().set(myTracker);

        Logger.logMessage("mashedstew successfully added as tracker.");
    }

    private static void addPrefix() throws Exception
    {
        String regex = CommandPrefix.parsePatternForItems("@mashedstew ");

        CommandPrefix prefix = new CommandPrefix(CommandPrefix.Type.IMGUR,
                regex);

        CommandPrefixStorage.handler().set(prefix);

        Logger.logMessage("`@mashedstew ` prefix successfully added.");
    }

    private static void addSimpleUserbase() throws Exception
    {
        Taglist newTl0 = new Taglist("test0", "test0 taglist", false);
        Taglist newTl1 = new Taglist("test1", "test1 taglist", true);

        TaglistHandler.handler().set(newTl0);
        TaglistHandler.handler().set(newTl1);

        HashSet<UserSubscription> sub0 = new HashSet<>();
        HashSet<UserSubscription> sub1 = new HashSet<>();
        HashSet<UserSubscription> sub2 = new HashSet<>();

        HashSet<Rating> ratings = new HashSet<>();
        ratings.add(Rating.SAFE);

        Taglist tl0 = TaglistHandler.getTaglistByAbbreviation("test0");
        Taglist tl1 = TaglistHandler.getTaglistByAbbreviation("test1");

        UserSubscription us0 = new UserSubscription(tl0, null, null);
        UserSubscription us1 = new UserSubscription(tl0, null, "cows");
        UserSubscription us2 = new UserSubscription(tl0, ratings, null);
        UserSubscription us3 = new UserSubscription(tl1, ratings, null);
        UserSubscription us4 = new UserSubscription(tl1, null, null);
        UserSubscription us5 = new UserSubscription(tl1, ratings, "cows");

        sub0.add(us0);
        sub0.add(us3);
        sub1.add(us1);
        sub1.add(us4);
        sub2.add(us2);
        sub2.add(us5);

        String uName0 = "mashedstew";
        String uName1 = "BloomingRose";
        String uName2 = "MisterThree";

        long u0Id = 33641050;
        long u1Id = 58590281;
        long u2Id = 13920225;

        User u0 = new User(uName0, u0Id, sub0);
        User u1 = new User(uName1, u1Id, sub1);
        User u2 = new User(uName2, u2Id, sub2);

        UserHandler.handler().set(u0);
        UserHandler.handler().set(u1);
        UserHandler.handler().set(u2);

        Logger.logMessage("simple user base successfully added.");

        // test1 s -> u0, u2
        // test1 e -> nobody
        // test1 s cows -> u0
        // test0 -> u0, u1, u2
        // test0 s -> u0, u1, u2
        // test0 cows -> u0, u2
    }

}














































