/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.io;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.util.NodeQueryUtils.hasText;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class QueryListDialogNGTest {
    private static final Logger LOGGER = Logger.getLogger(QueryListDialogNGTest.class.getName());
    
    private final FxRobot robot = new FxRobot();

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
    
    @Test
    public void getQueryName_ok_pressed() throws InterruptedException, ExecutionException {
        final List<String> names = List.of("query name 1", "query name 2");

        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> QueryListDialog.getQueryName(names));

        final Stage dialog = getDialog(robot);
        WaitForAsyncUtils.asyncFx(() -> dialog.requestFocus()).get();

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".list-cell")
                .lookup(hasText("query name 2"))
                .queryAs(ListCell.class));

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("OK"))
                .queryAs(Button.class));

        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertTrue(result.isPresent());
        assertEquals(result.get(), "query name 2");
    }
    
    @Test
    public void getQueryName_cancel_pressed() throws InterruptedException, ExecutionException {
        final List<String> names = List.of("query name 1", "query name 2");

        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> QueryListDialog.getQueryName(names));

        final Stage dialog = getDialog(robot);
        WaitForAsyncUtils.asyncFx(() -> dialog.requestFocus()).get();

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".list-cell")
                .lookup(hasText("query name 2"))
                .queryAs(ListCell.class));

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("Cancel"))
                .queryAs(Button.class));

        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertFalse(result.isPresent());
    }
    
    @Test
    public void getQueryName_drag_and_drop() throws InterruptedException, ExecutionException {
        final List<String> names = List.of("query name 1", "query name 2");
        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(
                () -> QueryListDialog.getQueryName(names));

        final Stage dialog = getDialog(robot);
        WaitForAsyncUtils.asyncFx(() -> dialog.requestFocus()).get();
        
        robot.drag(robot.from(dialog.getScene().getRoot())
                .lookup(".list-cell")
                .lookup(hasText("query name 2"))
                .queryAs(ListCell.class)
        )
                .dropTo(robot.from(dialog.getScene().getRoot())
                .lookup(".list-cell")
                .lookup(hasText("query name 1"))
                .queryAs(ListCell.class));
                
        // Pull out the list view and verify its current list state.
        final ListView listView = robot.from(dialog.getScene().getRoot())
                .lookup(".list-view")
                .queryAs(ListView.class);
        
        // The items should have switch order
        assertEquals(
                listView.getItems(),
                FXCollections.observableArrayList("query name 2", "query name 1")
        );
        
        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("Cancel"))
                .queryAs(Button.class));
        
        WaitForAsyncUtils.waitFor(future);       
    }
    
    /**
     * Get a dialog that has been displayed to the user. This will iterate
     * through all open windows and identify one that is modal. The assumption
     * is that there will only ever be one dialog open.
     * <p/>
     * If a dialog is not found then it will wait for the JavaFX thread queue to
     * empty and try again.
     *
     * @param robot the FX robot for these tests
     * @return the found dialog
     */
    private Stage getDialog(final FxRobot robot) {
        Stage dialog = null;
        while (dialog == null) {
            dialog = robot.robotContext().getWindowFinder().listWindows().stream()
                    .filter(window -> window instanceof javafx.stage.Stage)
                    .map(window -> (javafx.stage.Stage) window)
                    .filter(stage -> stage.getModality() == Modality.APPLICATION_MODAL)
                    .findFirst()
                    .orElse(null);

            if (dialog == null) {
                WaitForAsyncUtils.waitForFxEvents();
            }
        }
        return dialog;
    }
}
