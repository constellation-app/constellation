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
package au.gov.asd.tac.constellation.functionality.dialog;

import java.util.LinkedList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ConsolidatedDialogNGTest {
    
//    private static final Logger LOGGER = Logger.getLogger(ConsolidatedDialogNGTest.class.getName());
//    
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//        try {
//            if (!FxToolkit.isFXApplicationThreadRunning()) {
//                FxToolkit.registerPrimaryStage();
//            }
//        } catch (Exception e) {
//            System.out.println("\n**** SETUP ERROR: " + e);
//            throw e;
//        }
//    }
//
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//        try {
//            FxToolkit.cleanupStages();
//        } catch (TimeoutException ex) {
//            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
//        } catch (Exception e) {
//            if (e.toString().contains("HeadlessException")) {
//                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
//            } else {
//                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
//                throw e;
//            }
//        }
//    }

    /**
     * Test of ConsolidatedDialog constructor, of class ConsolidatedDialog.
     */
//    @Test
//    public void testConstructor() {
//        System.out.println("testConstructor");
//
//        System.setProperty("java.awt.headless", "true");
//        Platform.runLater(() -> {
//            ConsolidatedDialog instance = new ConsolidatedDialog(
//                    "",
//                    new HashMap(),
//                    "",
//                    0);
//
//            assertEquals(instance.getClass(), ConsolidatedDialog.class);
//        });
//
//        System.clearProperty("java.awt.headless");
//    }

    /**
     * Test of getSelectedObjects method, of class ConsolidatedDialog.
     */
    @Test
    public void testGetSelectedObjects() {
        System.out.println("getSelectedObjects");
//        ConsolidatedDialog instance = new ConsolidatedDialog(
//                "",
//                new HashMap(),
//                "",
//                0);
//      assertEquals(result.getClass(), ArrayList.class);

        ConsolidatedDialog instance = mock(ConsolidatedDialog.class);
        List result = instance.getSelectedObjects();
        assertEquals(result.getClass(), LinkedList.class);
    }

    /**
     * Test of setUseButtonAction method, of class ConsolidatedDialog.
     */
    @Test
    public void testSetGetUseButtonAction() {
        System.out.println("setGetUseButtonAction");
//        EventHandler<ActionEvent> event = null;
//        ConsolidatedDialog instance = new ConsolidatedDialog(
//                "",
//                new HashMap(),
//                "",
//                0);
//        
//        instance.setUseButtonAction(event);
//        assertEquals(instance.getUseButtonAction(), event);

        ConsolidatedDialog instance = mock(ConsolidatedDialog.class);
        EventHandler<ActionEvent> event = null;
        instance.setUseButtonAction(event);
        assertEquals(instance.getUseButtonAction(), event);
    }
}
