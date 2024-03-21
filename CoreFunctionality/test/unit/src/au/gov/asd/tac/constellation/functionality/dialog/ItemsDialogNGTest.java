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
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Window;
import static org.mockito.Mockito.mock;
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
public class ItemsDialogNGTest {

    public ItemsDialogNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setOkButtonAction method, of class ItemsDialog.
     */
    @Test
    public void testSetGetOkButtonAction() {
        System.out.println("setGetOkButtonAction");
        EventHandler<ActionEvent> event = null;
        ItemsDialog instance = new ItemsDialog(
                mock(Window.class),
                "",
                "",
                "",
                "",
                mock(ObservableList.class));

        instance.setOkButtonAction(event);
        assertEquals(instance.getOkButtonAction(), event);
    }

    /**
     * Test of setCancelButtonAction method, of class ItemsDialog.
     */
    @Test
    public void testSetGetCancelButtonAction() {
        System.out.println("setGetCancelButtonAction");
        EventHandler<ActionEvent> event = null;
        ItemsDialog instance = new ItemsDialog(
                mock(Window.class),
                "",
                "",
                "",
                "",
                mock(ObservableList.class));
        instance.setCancelButtonAction(event);
        assertEquals(instance.getCancelButtonAction(), event);
    }

}
