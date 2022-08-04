/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.stage.Window;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.util.NodeQueryUtils.hasText;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * NewAttributeDialog Test
 *
 * @author arcturus
 */
public class NewAttributeDialogNGTest {

    private static final Logger LOGGER = Logger.getLogger(NewAttributeDialogNGTest.class.getName());

    private static final String BEFORE_EVENT_RUN = "BEFORE_EVENT_RUN";
    private static final String AFTER_EVENT_RUN = "AFTER_EVENT_RUN";

    private final FxRobot robot = new FxRobot();

    public NewAttributeDialogNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    private class TestEvent implements EventHandler {

        private String value;

        public TestEvent(String value) {
            this.value = value;
        }

        @Override
        public void handle(Event t) {
            value = AFTER_EVENT_RUN;
        }

        public String getValue() {
            return value;
        }

    }

    /**
     * Test of setOkButtonAction method, of class NewAttributeDialog.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
    @Test
    public void testSetOkButtonAction() throws InterruptedException, ExecutionException {
        System.out.println("setOkButtonAction");

        final String test = BEFORE_EVENT_RUN;
        final EventHandler<ActionEvent> event = new TestEvent(test);

        final Future<Optional<String>> future = WaitForAsyncUtils.asyncFx(() -> {
            final NewAttributeDialog instance = new NewAttributeDialog();
            instance.setOkButtonAction(event);
            instance.showDialog();
            return Optional.of(((TestEvent) event).getValue());
        });

        final Window dialog = getDialog(robot);
        WaitForAsyncUtils.asyncFx(() -> dialog.requestFocus()).get();

        assertEquals(((TestEvent) event).getValue(), BEFORE_EVENT_RUN);

        robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("OK"))
                .queryAs(Button.class));
        final Optional<String> result = WaitForAsyncUtils.waitFor(future);

        assertTrue(result.isPresent());
        assertEquals(((TestEvent) event).getValue(), AFTER_EVENT_RUN);
    }

    /**
     * Test of getType method, of class NewAttributeDialog.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        Platform.runLater(() -> {
            final NewAttributeDialog instance = new NewAttributeDialog();
            final String expResult = "string";
            final String result = instance.getType();
            assertEquals(result, expResult);
        });
    }

    /**
     * Test of getLabel method, of class NewAttributeDialog.
     */
    @Test
    public void testGetLabel() {
        System.out.println("getLabel");
        Platform.runLater(() -> {
            final NewAttributeDialog instance = new NewAttributeDialog();
            final String expResult = "";
            final String result = instance.getLabel();
            assertEquals(result, expResult);
        });
    }

    /**
     * Test of getDescription method, of class NewAttributeDialog.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        Platform.runLater(() -> {
            final NewAttributeDialog instance = new NewAttributeDialog();
            final String expResult = "";
            final String result = instance.getDescription();
            assertEquals(result, expResult);
        });
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
    private Window getDialog(final FxRobot robot) {
        Window dialog = null;
        while (dialog == null) {
            if (robot.robotContext().getWindowFinder().listWindows().size() > 0) {
                dialog = robot
                        .robotContext()
                        .getWindowFinder()
                        .listWindows()
                        .get(robot.robotContext().getWindowFinder().listWindows().size() - 1)
                        .getScene()
                        .getWindow();
            }

            if (dialog == null) {
                WaitForAsyncUtils.waitForFxEvents();
            }
        }
        return dialog;
    }

}
