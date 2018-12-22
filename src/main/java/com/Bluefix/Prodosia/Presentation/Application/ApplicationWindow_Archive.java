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

package com.Bluefix.Prodosia.Presentation.Application;

import com.Bluefix.Prodosia.Data.DataHandler.ArchiveHandler;
import com.Bluefix.Prodosia.Data.DataType.Archive.Archive;
import com.Bluefix.Prodosia.Data.DataType.Taglist.Taglist;
import com.Bluefix.Prodosia.Business.Exception.ExceptionHelper;
import com.Bluefix.Prodosia.Presentation.Archive.EditArchiveWindow;
import com.Bluefix.Prodosia.Presentation.Managers.ButtonListManager.ButtonListManager;
import com.Bluefix.Prodosia.Presentation.Managers.ButtonListManager.TaglistListManager;
import com.Bluefix.Prodosia.Presentation.Navigation.VistaNavigator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class ApplicationWindow_Archive
{
    private ArchiveTaglistManager archiveSelector;


    public ApplicationWindow_Archive(VBox taglistBox, VBox archiveBox, Label selectionLabel, Button addNewArchiveButton) throws Exception
    {
        this.archiveSelector = new ArchiveTaglistManager(taglistBox, archiveBox, selectionLabel, addNewArchiveButton);
    }

    public void update() throws Exception
    {
        archiveSelector.update();
    }


    /**
     * All archives are linked to respective taglists first, to increase
     * overview. This way not all archives are shown at once, but only those that
     * adhere to the specific taglist.
     */
    private static class ArchiveTaglistManager extends TaglistListManager
    {
        private Pane selectionPane;
        private Label selectionLabel;
        private Button addNewArchive;

        private Taglist selectedTaglist;
        private ArchiveSelectionManager asm;

        /**
         * Create a new GuiListManager object that is linked to the root pane.
         * This list-manager will instantiate itself by filling the items
         * from `listItems()`
         *
         * @param root The root in which the items will be displayed.
         * @param selectionPane the panel in which the archive-selection will be visible.
         * @param selectionLabel the label that is used to indicate the currently selected taglist.
         */
        public ArchiveTaglistManager(
                Pane root,
                Pane selectionPane,
                Label selectionLabel,
                Button addNewArchive) throws Exception
        {
            super(root);
            this.selectionPane = selectionPane;
            this.selectionLabel = selectionLabel;
            this.addNewArchive = addNewArchive;

            if (this.addNewArchive != null)
                this.addNewArchive.setOnAction(event -> addNewArchive());

            this.selectedTaglist = null;

            updateGui();
        }

        //region GUI update

        @Override
        public void update() throws Exception
        {
            super.update();

            // de-select any selected taglist
            selectedTaglist = null;

            if (asm != null)
            {
                asm.close();
                asm = null;
            }

            updateGui();
        }

        private void updateGui()
        {
            // disable the "add new archive" button if no taglist was selected.
            if (addNewArchive != null)
                addNewArchive.setDisable(selectedTaglist == null);

            if (selectionPane != null)
                selectionPane.setDisable(selectedTaglist == null);

            // if a taglist was selected, show its label
            if (selectedTaglist == null)
                selectionLabel.setText("");
            else
                selectionLabel.setText(selectedTaglist.getAbbreviation());
        }

        //endregion

        private void addNewArchive()
        {
            if (selectedTaglist == null)
                return;

            // open an empty Edit Archive Window
            EditArchiveWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.ARCHIVE_EDIT);
            controller.init(selectedTaglist);
        }


        @Override
        protected EventHandler<ActionEvent> getEventHandlerForButton(Taglist taglist)
        {


            return event ->
            {
                if (asm != null)
                {
                    asm.close();
                    asm = null;
                }

                if (Objects.equals(selectedTaglist, taglist))
                {
                    selectedTaglist = null;
                    updateGui();
                    return;
                }

                try
                {
                    asm = new ArchiveSelectionManager(selectionPane, taglist);

                    selectedTaglist = taglist;

                    updateGui();

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            };
        }
    }




    /**
     * Allow for a selection of archives that correspond to the selected taglist.
     */
    private static class ArchiveSelectionManager extends ButtonListManager
    {
        private Taglist taglist;

        public ArchiveSelectionManager(Pane pane, Taglist tl) throws Exception
        {
            super(pane);
            this.taglist = tl;
            super.update();
        }


        @Override
        protected Iterable<String> listButtonItems() throws Exception
        {
            // find all archives that correspond with the taglist.
            ArrayList<Archive> archives = ArchiveHandler.getArchivesForTaglist(taglist);

            // init a map with all the archives and a list with the button entries.
            LinkedList<String> entries = new LinkedList<>();
            this.archiveCollection = new Archive[archives.size()];

            int counter = 0;

            for (Archive a : archives)
            {
                entries.addLast(a.getDescription());
                this.archiveCollection[counter++] = a;
            }

            return entries;
        }


        private Archive[] archiveCollection;

        @Override
        protected EventHandler<ActionEvent> getEventHandlerForButton(int entry)
        {
            return event ->
            {
                EditArchiveWindow controller = VistaNavigator.loadVista(VistaNavigator.AppStage.ARCHIVE_EDIT);
                try
                {
                    controller.init(archiveCollection[entry]);
                } catch (SQLException e)
                {
                    e.printStackTrace();
                    ExceptionHelper.showWarning(e);
                }
            };
        }
    }



}
