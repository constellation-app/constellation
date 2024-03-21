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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Window;
import static org.geotools.referencing.factory.ReferencingFactory.LOGGER;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ConsolidatedDialogNGTest {

    public ConsolidatedDialogNGTest() {
    }

//    @BeforeClass
//    public static void setUpClass() throws Exception {
//        if (!FxToolkit.isFXApplicationThreadRunning()) {
//            FxToolkit.registerPrimaryStage();
//        }
//    }
// 
//    @AfterClass
//    public static void tearDownClass() throws Exception {
//        try {
//            FxToolkit.cleanupStages();
//        } catch (TimeoutException ex) {
//            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
//        }
//    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getSelectedObjects method, of class ConsolidatedDialog.
     */
    @Test
    public void testGetSelectedObjects() {
        System.out.println("getSelectedObjects");
        ConsolidatedDialog instance = new ConsolidatedDialog(
                "",
                new HashMap(),
                "",
                0);

        List result = instance.getSelectedObjects();
        assertEquals(result.getClass(), ArrayList.class);
    }

    /**
     * Test of setUseButtonAction method, of class ConsolidatedDialog.
     */
//    @Test
//    public void testSetGetUseButtonAction() {
//        System.out.println("setGetUseButtonAction");
//        EventHandler<ActionEvent> event = null;
//        ConsolidatedDialog instance = new ConsolidatedDialog(
//                "",
//                new HashMap(),
//                "",
//                0);
//        
//        instance.setUseButtonAction(event);
//        assertEquals(instance.getUseButtonAction(), event);
//    }
}
