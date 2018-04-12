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

package com.Bluefix.Prodosia.GUI.ImportExport;

import com.Bluefix.Prodosia.GUI.Navigation.VistaNavigator;
import com.Bluefix.Prodosia.ImportExport.ImportExportHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class ImportExportWindow
{
    private ImportExportHandler.ImportPolicy importPolicy;
    private File importFile;

    @FXML private TextField tf_import;
    @FXML private TextField tf_export;
    @FXML private ComboBox cb_importpolicy;

    @FXML private TitledPane tp_import;
    @FXML private TitledPane tp_export;

    @FXML
    private void back(ActionEvent actionEvent)
    {
        VistaNavigator.loadVista(VistaNavigator.AppStage.MAIN_MENU);
    }

    //region Initialization

    @FXML
    private void initialize()
    {
        // titled panes and expansion
        tp_import.setExpanded(false);
        tp_export.setExpanded(false);

        titlePaneListener();

        // import policy
        this.importPolicy = ImportExportHandler.ImportPolicy.MERGE_THEIRS;
        initializeImportPolicies();
    }


    /**
     * Implement listeners so that only one titled pane can be expanded at a time.
     */
    private void titlePaneListener()
    {
        tp_import.expandedProperty().addListener(c ->
        {
            if (tp_import.isExpanded())
            {
                tp_export.setExpanded(false);
            }
        });

        tp_export.expandedProperty().addListener(c ->
        {
            if (tp_export.isExpanded())
            {
                tp_import.setExpanded(false);
            }
        });
    }

    private void initializeImportPolicies()
    {
        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Merge (theirs)",
                        "Merge (ours)",
                        "Overwrite"
                );

        cb_importpolicy.setItems(options);

        selectImportPolicy();

        cb_importpolicy.setOnAction(e -> updateImportPolicy());


    }

    private void selectImportPolicy()
    {
        switch (this.importPolicy)
        {

            case MERGE_THEIRS:
                cb_importpolicy.getSelectionModel().select(0);
                break;
            case MERGE_OURS:
                cb_importpolicy.getSelectionModel().select(1);
                break;
            case OVERWRITE:
                cb_importpolicy.getSelectionModel().select(2);
                break;
        }
    }

    private void updateImportPolicy()
    {
        switch (cb_importpolicy.getSelectionModel().getSelectedIndex())
        {
            case 0:
                this.importPolicy = ImportExportHandler.ImportPolicy.MERGE_THEIRS;
                break;
            case 1:
                this.importPolicy = ImportExportHandler.ImportPolicy.MERGE_OURS;
                break;
            case 2:
                this.importPolicy = ImportExportHandler.ImportPolicy.OVERWRITE;
                break;
        }
    }

    //endregion


    //region Button functionality

    public void selectImportFile(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please select the import file.");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Prodosía Files", "*.prod"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Node source = (Node)actionEvent.getSource();
        Window stage = source.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);

        this.importFile = file;

        tf_import.setText(file == null ? "" : file.getPath());
    }

    public void selectExportFile(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Please select the export location.");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Prodosía Files", "*.prod"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Node source = (Node)actionEvent.getSource();
        Window stage = source.getScene().getWindow();

        File file = fileChooser.showSaveDialog(stage);

        tf_export.setText(file == null ? "" : file.getPath());
    }

    public void doExport(ActionEvent actionEvent)
    {
        String location = tf_export.getText().trim();

        if (location.isEmpty())
        {
            showInformation("It is required to select an export location first.");
            return;
        }

        ImportExportHandler.ExportResult er;
        try
        {
            er = ImportExportHandler.exportToFile(location);
        } catch (Exception e)
        {
            er = ImportExportHandler.ExportResult.ERROR;
        }

        switch (er)
        {

            case OK:
                showInformation("Successfully created the export file.");
                break;
            case LOCATION_INCORRECT:
                showWarning("Could not write to this location.");
                break;
            case ERROR:
                showWarning("Something went wrong :<");
                break;
        }

        this.tf_export.setText("");

    }

    public void doImport(ActionEvent actionEvent) throws Exception
    {
        if (this.importFile == null)
        {
            showInformation("It is required to select an import file first.");
            return;
        }

        ImportExportHandler.ImportResult ir =
                ImportExportHandler.importFromFile(this.importFile, this.importPolicy);

        switch (ir)
        {

            case OK:
                showInformation("Successfully imported the file.");
                break;
            case FILE_MISSING:
                showWarning("This file could not be found!");
                break;
            case FILE_INCORRECT:
                showWarning("This file isn't a proper import file; syntax incorrect.");
                break;
        }

        this.importFile = null;
        this.tf_import.setText("");

    }

    private void showWarning(String message)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Warning!");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    private void showInformation(String message)
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }


    //endregion
}








































