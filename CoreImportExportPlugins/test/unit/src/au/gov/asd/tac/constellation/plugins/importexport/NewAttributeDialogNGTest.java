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

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.stage.Window;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;
import static org.testng.Assert.assertEquals;
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

    // TODO: commenting out as there is a Headless Exception related to Swing.
    // Will continue investigating this.
    /**
     * Test of setOkButtonAction method, of class NewAttributeDialog.
     *
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.ExecutionException
     */
//    @Test
//    public void testSetOkButtonAction() throws InterruptedException, ExecutionException {
//        System.out.println("setOkButtonAction");
//
//        final String[] value = new String[1];
//
//        final Future<String> future = WaitForAsyncUtils.asyncFx(() -> {
//            final NewAttributeDialog instance = new NewAttributeDialog();
//            instance.setOkButtonAction(event2 -> {
//                value[0] = instance.getLabel();
//            });
//            instance.showDialog();
//            return "done";
//        });
//
//        final Window dialog = getDialog(robot);
//
//        robot.clickOn(
//                robot.from(dialog.getScene().getRoot())
//                        .lookup(".text-field")
//                        .queryAs(TextField.class)
//        ).write("test");
//
//        robot.clickOn(robot.from(dialog.getScene().getRoot())
//                .lookup(".button")
//                .lookup(hasText("OK"))
//                .queryAs(Button.class));
//
//        WaitForAsyncUtils.waitFor(future);
//
//        assertEquals(value[0], "test");
//    }
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
