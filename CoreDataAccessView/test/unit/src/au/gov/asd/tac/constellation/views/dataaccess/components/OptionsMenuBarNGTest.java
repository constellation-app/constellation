/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.views.dataaccess.io.DataAccessParametersIoProvider;
import au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessPane;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class OptionsMenuBarNGTest {

    private static final Logger LOGGER = Logger.getLogger(OptionsMenuBarNGTest.class.getName());

    private DataAccessPane dataAccessPane;

    private OptionsMenuBar optionsMenuBar;

    private static MockedStatic<FileChooser> fileChooserStaticMock;

    @BeforeClass
    public void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        dataAccessPane = mock(DataAccessPane.class);

        optionsMenuBar = spy(new OptionsMenuBar(dataAccessPane));

        fileChooserStaticMock = Mockito.mockStatic(FileChooser.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        fileChooserStaticMock.close();
    }

    @Test
    public void init() {
        try (final MockedStatic<DataAccessPreferenceUtilities> prefUtilsMockedStatic
                = Mockito.mockStatic(DataAccessPreferenceUtilities.class)) {
            prefUtilsMockedStatic.when(DataAccessPreferenceUtilities::getDataAccessResultsDir)
                    .thenReturn(new File("/"));
            prefUtilsMockedStatic.when(DataAccessPreferenceUtilities::isDeselectPluginsOnExecuteEnabled)
                    .thenReturn(true);

            optionsMenuBar.init();
        }

        final String iconSet = JavafxStyleManager.isDarkTheme() ? "Light" : "Dark";
        
        // Load Templates Menu Item
        verifyMenuItem(
                optionsMenuBar.getLoadMenuItem(),
                "Load Templates",
                new Image(OptionsMenuBar.class.getResourceAsStream(
                        "resources/DataAccessLoadTemplate" + iconSet + ".png"
                ))
        );

        try (final MockedStatic<DataAccessParametersIoProvider> prefProviderMockedStatic
                = Mockito.mockStatic(DataAccessParametersIoProvider.class)) {
            final ActionEvent actionEvent = mock(ActionEvent.class);

            optionsMenuBar.getLoadMenuItem().getOnAction().handle(actionEvent);

            prefProviderMockedStatic.verify(() -> DataAccessParametersIoProvider
                    .loadParameters(dataAccessPane));
            verify(actionEvent).consume();
        }

        // Save Templates Menu Item
        verifyMenuItem(
                optionsMenuBar.getSaveMenuItem(),
                "Save Templates",
                new Image(OptionsMenuBar.class.getResourceAsStream(
                        "resources/DataAccessSaveTemplate" + iconSet + ".png"
                ))
        );

        // Connection Logging Menu Item
        verifyMenuItem(
                optionsMenuBar.getConnectionLoggingMenuItem(),
                "Connection Logging",
                new Image(OptionsMenuBar.class.getResourceAsStream(
                        "resources/DataAccessConnectionLogging" + iconSet + ".png"
                ))
        );

        try (final MockedStatic<DataAccessParametersIoProvider> prefProviderMockedStatic
                = Mockito.mockStatic(DataAccessParametersIoProvider.class)) {
            final DataAccessTabPane dataAccessTabPane = mock(DataAccessTabPane.class);
            final TabPane tabPane = mock(TabPane.class);
            final ActionEvent actionEvent = mock(ActionEvent.class);

            when(dataAccessPane.getDataAccessTabPane()).thenReturn(dataAccessTabPane);
            when(dataAccessTabPane.getTabPane()).thenReturn(tabPane);

            optionsMenuBar.getSaveMenuItem().getOnAction().handle(actionEvent);

            prefProviderMockedStatic.verify(() -> DataAccessParametersIoProvider
                    .saveParameters(tabPane));
            verify(actionEvent).consume();
        }

        // Save Results Menu Item
        verifyMenuItem(
                optionsMenuBar.getSaveResultsItem(),
                "Save Results",
                new Image(OptionsMenuBar.class.getResourceAsStream(
                        "resources/DataAccessSaveResults" + iconSet + ".png"
                ))
        );
        assertTrue(optionsMenuBar.getSaveResultsItem().isSelected());

        // Can't extract added listeners. Testing of the listener itself
        // happens separately.
        // De-Select Plugins On Execution Menu Item
        verifyMenuItem(
                optionsMenuBar.getDeselectPluginsOnExecutionMenuItem(),
                "Deselect On Go",
                new Image(OptionsMenuBar.class.getResourceAsStream(
                        "resources/DataAccessUnchecked" + iconSet + ".png"
                ))
        );
        assertTrue(optionsMenuBar.getDeselectPluginsOnExecutionMenuItem().isSelected());

        try (final MockedStatic<DataAccessPreferenceUtilities> prefUtilsMockedStatic
                = Mockito.mockStatic(DataAccessPreferenceUtilities.class)) {
            final ActionEvent actionEvent = mock(ActionEvent.class);

            optionsMenuBar.getDeselectPluginsOnExecutionMenuItem().getOnAction().handle(actionEvent);

            prefUtilsMockedStatic.verify(() -> DataAccessPreferenceUtilities
                    .setDeselectPluginsOnExecute(true));
            verify(actionEvent).consume();
        }

        // Options Menu
        verifyMenuItem(
                optionsMenuBar.getOptionsMenu(),
                "Workflow Options",
                new Image(OptionsMenuBar.class.getResourceAsStream(
                        "resources/DataAccessSettings.png"
                ))
        );

        assertEquals(
                optionsMenuBar.getOptionsMenu().getItems(),
                FXCollections.observableArrayList(
                        optionsMenuBar.getLoadMenuItem(),
                        optionsMenuBar.getSaveMenuItem(),
                        optionsMenuBar.getSaveResultsItem(),
                        optionsMenuBar.getConnectionLoggingMenuItem(),
                        optionsMenuBar.getDeselectPluginsOnExecutionMenuItem()
                )
        );

        // Menu Bar
        assertEquals(
                optionsMenuBar.getMenuBar().getMenus(),
                FXCollections.observableArrayList(
                        optionsMenuBar.getOptionsMenu()
                )
        );
        assertEquals(optionsMenuBar.getMenuBar().getMinHeight(), 36.0);
        assertEquals(optionsMenuBar.getMenuBar().getPadding(), new Insets(4));
    }

    @Test
    public void saveResultsListener() throws InterruptedException, ExecutionException {
        final CheckMenuItem menuItem = mock(CheckMenuItem.class);

        when(optionsMenuBar.getSaveResultsItem()).thenReturn(menuItem);

        OptionsMenuBar.SaveResultsListener listener = optionsMenuBar.new SaveResultsListener();

        // Value Changed is FALSE
        try (
                final MockedStatic<DataAccessPreferenceUtilities> prefUtilsMockedStatic
                = Mockito.mockStatic(DataAccessPreferenceUtilities.class);) {
            listener.changed(null, null, false);

            // Wait for any work to complete
            listener.getLastChange().get();

            prefUtilsMockedStatic.verify(() -> DataAccessPreferenceUtilities.setDataAccessResultsDir(null));
        }

        // Value Changed is TRUE
        verifySaveResultsListenerValueChangedTrue(listener, menuItem, new File("/savedir/"), false);
        verifySaveResultsListenerValueChangedTrue(listener, menuItem, null, true);
    }

    /**
     * Verifies that when the new value in the listener is true the directory
     * chooser is opened. If the user selects a folder, then nothing happens. If
     * the user cancels, then save results menu item is set to un-selected.
     *
     * @param listener the listener to test
     * @param menuItem the mocked save results menu item
     * @param userSelection the user selection returned by the directory chooser
     * @param setSelectedExecuted true if the save results menu item should be
     * de-selected, false otherwise
     */
    private void verifySaveResultsListenerValueChangedTrue(final OptionsMenuBar.SaveResultsListener listener,
            final CheckMenuItem menuItem,
            final File userSelection,
            final boolean setSelectedExecuted) throws InterruptedException, ExecutionException {

        final Optional<File> optionalFile = Optional.ofNullable(userSelection);

        fileChooserStaticMock.when(()
                -> FileChooser.openOpenDialog(Mockito.any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile));

        listener.changed(null, null, true);

        // Wait for any work to complete
        listener.getLastChange().get();
        WaitForAsyncUtils.waitForFxEvents();

        if (setSelectedExecuted) {
            verify(menuItem).setSelected(false);
        } else {
            verifyNoInteractions(menuItem);
        }
    }

    /**
     * Verifies the passed menu item has the correct text, style, tool tip and
     * icon.
     *
     * @param menuItem the menu item to test
     * @param text the text that should be on the button
     * @param icon the icon that should be on the button or null if no icon
     * should be set
     */
    private void verifyMenuItem(final MenuItem menuItem,
            final String text,
            final Image icon) {
        assertEquals(menuItem.getText(), text);

        if (icon != null) {
            assertTrue(isImageEqual(
                    ((ImageView) menuItem.getGraphic()).getImage(), icon
            ));
        } else {
            assertNull(menuItem.getGraphic());
        }
    }

    /**
     * Verifies that two JavaFX images are equal. Unfortunately they don't
     * provide a nice way to do this so we check pixel by pixel.
     *
     * @param firstImage the first image to compare
     * @param secondImage the second image to compare
     * @return true if the images are the same, false otherwise
     */
    private static boolean isImageEqual(Image firstImage, Image secondImage) {
        // Prevent `NullPointerException`
        if (firstImage != null && secondImage == null) {
            return false;
        }

        if (firstImage == null) {
            return secondImage == null;
        }

        // Compare images size
        if (firstImage.getWidth() != secondImage.getWidth()) {
            return false;
        }

        if (firstImage.getHeight() != secondImage.getHeight()) {
            return false;
        }

        // Compare images color
        for (int x = 0; x < firstImage.getWidth(); x++) {
            for (int y = 0; y < firstImage.getHeight(); y++) {
                int firstArgb = firstImage.getPixelReader().getArgb(x, y);
                int secondArgb = secondImage.getPixelReader().getArgb(x, y);

                if (firstArgb != secondArgb) {
                    return false;
                }
            }
        }

        return true;
    }
}
