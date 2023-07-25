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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.SelectOptionsExtension;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuButton;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author capricornunicorn123
 */
public class SelectOptionsExtensionNGTest {
    
    public SelectOptionsExtensionNGTest() {
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
     * Test of setSelectionOption method, of class SelectOptionsExtension.
     */
    @Test
    public void testSetSelectionOption() {
        System.out.println("setSelectionOption");
        String displayText = "";
        EventHandler<ActionEvent> event = null;
        SelectOptionsExtension instance = null;
        instance.setSelectionOption(displayText, event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of enablePopUp method, of class SelectOptionsExtension.
     */
    @Test
    public void testEnablePopUp() {
        System.out.println("enablePopUp");
        SelectOptionsExtension instance = null;
        instance.enablePopUp();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disablePopUp method, of class SelectOptionsExtension.
     */
    @Test
    public void testDisablePopUp() {
        System.out.println("disablePopUp");
        SelectOptionsExtension instance = null;
        instance.disablePopUp();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
