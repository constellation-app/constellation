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
package au.gov.asd.tac.constellation.utilities.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.testfx.api.FxToolkit;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author capricornunicorn123
 */
public class MultiChoiceInputFieldNGTest {
    
    static final Logger LOGGER = Logger.getLogger(MultiChoiceInputFieldNGTest.class.getName());

    private MultiChoiceInputField<String> field;
    private List<String> data;
    private String OPTION1;
    private String OPTION2;
    private String OPTION3;
    private String OPTION4;
    private String OPTION5;
    
    public MultiChoiceInputFieldNGTest() {
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
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        OPTION1 = "Option 1";
        OPTION2 = "Option 2";
        OPTION3 = "Option 3";
        OPTION4 = "Option 4";
        OPTION5 = "Option 5"; 
        
        data = new ArrayList<String>();
        data.add(OPTION1);
        data.add(OPTION2);
        data.add(OPTION3);
        data.add(OPTION4);
        data.add(OPTION5);  
        
        field = new MultiChoiceInputField<>();      
        field.getItems().addAll(data);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setSelectionOption method, of class MultiChoiceInputField.
     */
    @Test
    public void testSetSelectionOption() {
        final EventHandler<ActionEvent> invertSelectionEvent = event -> {
            for (int i = 0 ; i < field.getCheckModel().getItemCount() ; i++){
                field.getCheckModel().toggleCheckState(i);
            }
        };
        
        //Test the number of BulkSelectionOtions increased by 1
        int optionsCountInitial = this.field.getBulkSelectionOptionsMenuButton().getItems().size();
        this.field.setSelectionOption("InvertSelection", invertSelectionEvent);
        int optionsCountFinal = this.field.getBulkSelectionOptionsMenuButton().getItems().size();
        Assert.assertTrue(optionsCountInitial + 1 == optionsCountFinal);
    }

    /**
     * Test of enablePopUp method, of class MultiChoiceInputField.
     */
    @Test
    public void testEnablePopUp() {
       this.field.disablePopUp();   
       Assert.assertNull(this.field.getOnContextMenuRequested()); 
       
        this.field.enablePopUp();        
        Assert.assertNotNull(this.field.getOnContextMenuRequested());
    }

    /**
     * Test of disablePopUp method, of class MultiChoiceInputField.
     */
    @Test
    public void testDisablePopUp() {
        Assert.assertNotNull(this.field.getOnContextMenuRequested());     
        this.field.disablePopUp();        
        Assert.assertNull(this.field.getOnContextMenuRequested());
    }

    /**
     * Test of getBulkSelectionOptionsMenuButton method, of class MultiChoiceInputField.
     */
    @Test
    public void testGetMenuButton() {
        MenuButton menuButton = this.field.getBulkSelectionOptionsMenuButton();
        
        //Make sure the the a MenuButton is returned
        MenuButton expectedClass = new MenuButton();
        Class menuButtonClass = menuButton.getClass();
        Assert.assertTrue(menuButtonClass.isInstance(expectedClass));
        
        //Assert that the menu button only has two initial Options, Select all         
        ObservableList<MenuItem> menuItems = menuButton.getItems();
        ArrayList<String> menuItemsNames = new ArrayList<String>();
        menuItems.stream().forEach(item -> menuItemsNames.add(item.getText()));
        Assert.assertTrue(menuItems.size() == 2);
        Assert.assertTrue(menuItemsNames.contains("Select All"));
        Assert.assertTrue(menuItemsNames.contains("Clear All"));
    }

    /**
     * Test of promptTextProperty method, of class MultiChoiceInputField.
     */
    @Test
    public void testPromptTextProperty() {
        final String prompt = "Test Prompt";
        field.setPromptText(prompt);
        Assert.assertNotNull(field.promptTextProperty());
    }

    /**
     * Test of getPromptText method, of class MultiChoiceInputField.
     */
    @Test
    public void testGetPromptText() {
        final String prompt = "Test Prompt";
        field.setPromptText(prompt);
        Assert.assertEquals(field.promptTextProperty().getValue(), prompt);
    }

    /**
     * Test of setPromptText method, of class MultiChoiceInputField.
     */
    @Test
    public void testSetPromptText() {
        final String prompt = "Test Prompt";
        field.setPromptText(prompt);
        Assert.assertEquals(field.promptTextProperty().getValue(), prompt);
    }
    
}
