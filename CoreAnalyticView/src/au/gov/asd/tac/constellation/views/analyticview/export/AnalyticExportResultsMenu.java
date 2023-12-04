/*
 * Copyright 2010-2023 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.views.analyticview.export;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.plugins.ThreadConstraints;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.icon.UserInterfaceIconProvider;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.lang3.StringUtils;
import static org.geotools.referencing.factory.ReferencingFactory.LOGGER;
import org.openide.filesystems.FileChooserBuilder;

/**
 * Handles exporting the results from the Analytic View table.
 *
 * @author Delphinus8821
 */
public class AnalyticExportResultsMenu {

    private static final String EXPORT_CSV = "Export to CSV";
    private static final String EXPORT_XLSX = "Export to Excel";
    private static final String EXPORT_CSV_FILE_CHOOSER_TITLE = "Export To CSV";
    private static final String EXPORT_XLSX_FILE_CHOOSER_TITLE = "Export To XLSX";
    private static final String EXPORT_CSV_FILE_CHOOSER_DESCRIPTION = "CSV files (*" + FileExtensionConstants.COMMA_SEPARATED_VALUE + ")";
    private static final String EXPORT_XLSX_FILE_CHOOSER_DESCRIPTION = "Excel files (*" + FileExtensionConstants.XLSX + ")";

    private static final ImageView EXPORT_ICON = new ImageView(UserInterfaceIconProvider.UPLOAD.buildImage(16));
    private static final int WIDTH = 200;

    private MenuButton exportButton;
    private MenuItem exportCsvMenu;
    private MenuItem exportExcelMenu;

    private final TableView table;

    public AnalyticExportResultsMenu(final TableView table) {
        this.table = table;
    }

    public void init() {
        exportButton = createMenuButton(EXPORT_ICON);

        exportCsvMenu = createExportMenu(
                EXPORT_CSV,
                EXPORT_CSV_FILE_CHOOSER_TITLE,
                FileExtensionConstants.COMMA_SEPARATED_VALUE,
                EXPORT_CSV_FILE_CHOOSER_DESCRIPTION,
                file -> new AnalyticExportToCsvFilePlugin(file, table)
        );

        exportExcelMenu = createExportMenu(
                EXPORT_XLSX,
                EXPORT_XLSX_FILE_CHOOSER_TITLE,
                FileExtensionConstants.XLSX,
                EXPORT_XLSX_FILE_CHOOSER_DESCRIPTION,
                file -> new AnalyticExportToExcelFilePlugin(file, table, "Export")
        );

        exportButton.getItems().addAll(exportCsvMenu, exportExcelMenu);
    }

    /**
     * Get the export button that the menu items have been added to.
     *
     * @return the export table data button
     */
    public MenuButton getExportButton() {
        return exportButton;
    }

    /**
     * Get the menu item that will export all the table data in CSV format.
     *
     * @return the export all rows to CSV menu item
     */
    public MenuItem getExportCsvMenu() {
        return exportCsvMenu;
    }

    /**
     * Get the menu item that will export all the table data in excel format.
     *
     * @return the export all rows to excel menu item
     */
    public MenuItem getExportExcelMenu() {
        return exportExcelMenu;
    }


    /**
     * Creates a menu button to store the export menu items under. Sets the icon and max width.
     *
     * @param icon the icon to display on the button
     * @return the created menu button
     */
    private MenuButton createMenuButton(final ImageView icon) {
        final MenuButton button = new MenuButton();
        button.setText("Export");
        button.setGraphic(icon);
        button.setMaxWidth(WIDTH);
        button.setPopupSide(Side.RIGHT);

        return button;
    }

    /**
     * Creates a export {@link MenuItem}. Sets the title text and the action handler to a new {@link ExportMenuItemActionHandler}.
     *
     * @param menuTitle the title to put on the menu item
     * @param fileChooserTitle the title that will be on the export file chooser dialog
     * @param expectedFileExtension the file extension the file chooser will save
     * @param fileChooserDescription the description that will be on the export file chooser dialog
     * @param exportPluginCreator a function that creates an export plugin that will write to the passed file
     * @return the created menu item
     */
    private MenuItem createExportMenu(final String menuTitle,
            final String fileChooserTitle,
            final String expectedFileExtension,
            final String fileChooserDescription,
            final Function<File, Plugin> exportPluginCreator) {
        final MenuItem menuItem = new MenuItem(menuTitle);

        menuItem.setOnAction(new ExportMenuItemActionHandler(
                fileChooserTitle, expectedFileExtension, fileChooserDescription, exportPluginCreator));

        return menuItem;
    }

    /**
     * Action handler for menu items that will export table rows to either CSV or Excel.
     */
    class ExportMenuItemActionHandler implements EventHandler<ActionEvent> {

        private final String fileChooserTitle;
        private final String expectedFileExtension;
        private final String fileChooserDescription;
        private final Function<File, Plugin> exportPluginCreator;

        private CompletableFuture<Void> lastExport;

        /**
         * Creates a new export menu item action handler.
         *
         * @param fileChooserTitle the title that will be on the export file chooser dialog
         * @param expectedFileExtension the file extension the file chooser will save
         * @param fileChooserDescription the description that will be on the export file chooser dialog
         * @param exportPluginCreator a function that creates an export plugin that will write to the passed file
         */
        public ExportMenuItemActionHandler(final String fileChooserTitle,
                final String expectedFileExtension,
                final String fileChooserDescription,
                final Function<File, Plugin> exportPluginCreator) {
            this.fileChooserTitle = fileChooserTitle;
            this.expectedFileExtension = expectedFileExtension;
            this.fileChooserDescription = fileChooserDescription;
            this.exportPluginCreator = exportPluginCreator;
        }

        /**
         * Opens a save dialog and requests user input for where the export should be saved. With the selected file a new plugin is instantiated which
         * will perform the actual export. The export will only be executed if the current graph is not null.
         *
         * @param event the event that triggered this action
         * @see EventHandler#handle(javafx.event.Event)
         */
        @Override
        public void handle(final ActionEvent event) {
            if (GraphManager.getDefault().getActiveGraph() != null) {
                final FileChooserBuilder exportFileChooser = getExportFileChooser();
                final ThreadConstraints callingConstraints = ThreadConstraints.getConstraints();

                // Open the file chooser and get the user to select a file
                // Use the function to create the required export plugin and
                // then execute it
                lastExport = FileChooser.openSaveDialog(exportFileChooser)
                        .thenAccept(optionalFile
                                -> optionalFile.ifPresent(file -> {
                            final String filePath = file.getAbsolutePath().toLowerCase().endsWith(expectedFileExtension)
                                    ? file.getAbsolutePath() : file.getAbsolutePath() + expectedFileExtension;
                            final File fileName = new File(filePath);
                            Platform.runLater(() -> {
                                final ThreadConstraints workerConstraints = ThreadConstraints.getConstraints();
                                workerConstraints.setCurrentReport(callingConstraints.getCurrentReport());
                                try {
                                    PluginExecution.withPlugin(
                                            exportPluginCreator.apply(fileName)
                                    ).executeNow((Graph) null);
                                } catch (final InterruptedException ex) {
                                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                                    Thread.currentThread().interrupt();
                                } catch (final PluginException ex) {
                                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                                }
                            });
                        }));
            }
            event.consume();
        }

        /**
         * Gets a future that will complete when the last export has completed.
         *
         * @return the completable future of the last export
         */
        public CompletableFuture<Void> getLastExport() {
            return lastExport;
        }

        /**
         * Creates a new file chooser.
         *
         * @return the created file chooser
         */
        public FileChooserBuilder getExportFileChooser() {
            return new FileChooserBuilder(fileChooserTitle)
                    .setTitle(fileChooserTitle)
                    .setFilesOnly(true)
                    .setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(final File file) {
                            final String name = file.getName();
                            // if it is an actual file and it ends with the expected extension

                            return (file.isFile() && StringUtils.endsWithIgnoreCase(name, expectedFileExtension)) || file.isDirectory();
                        }

                        @Override
                        public String getDescription() {
                            return fileChooserDescription;
                        }
                    });
        }
    }
}
