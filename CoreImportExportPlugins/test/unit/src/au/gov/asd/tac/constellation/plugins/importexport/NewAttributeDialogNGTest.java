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
package au.gov.asd.tac.constellation.plugins.importexport;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
public class NewAttributeDialogNGTest {
    
    public NewAttributeDialogNGTest() {
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
     * Test of getType method, of class NewAttributeDialog.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        NewAttributeDialog instance = new NewAttributeDialog();
        String result = instance.getType();
        assertEquals(result.getClass(), String.class);
    }

    /**
     * Test of getLabel method, of class NewAttributeDialog.
     */
    @Test
    public void testGetLabel() {
        System.out.println("getLabel");
        NewAttributeDialog instance = new NewAttributeDialog();
        String result = instance.getLabel();
        assertEquals(result.getClass(), String.class);
    }

    /**
     * Test of getDescription method, of class NewAttributeDialog.
     */
    @Test
    public void testGetDescription() {
        System.out.println("getDescription");
        NewAttributeDialog instance = new NewAttributeDialog();
        String result = instance.getDescription();
        assertEquals(result.getClass(), String.class);
    }

    /**
     * Test of setOkButtonAction method, of class NewAttributeDialog.
     */
    @Test
    public void testSetGetOkButtonAction() {
        System.out.println("setOkButtonAction");
        EventHandler<ActionEvent> event = null;
        NewAttributeDialog instance = new NewAttributeDialog();
        
        instance.setOkButtonAction(event);
        assertEquals(instance.getOkButtonAction(), event);
    }
    
}
