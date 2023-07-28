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
package au.gov.asd.tac.constellation.plugins.parameters;

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
import org.controlsfx.control.CheckComboBox;
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
public class SelectOptionsExtensionNGTest {

    static final Logger LOGGER = Logger.getLogger(SelectOptionsExtensionNGTest.class.getName());

    private SelectOptionsExtension BulkSelectionOptions;
    private List<String> data;
    CheckComboBox<String> field;
    private String OPTION1;
    private String OPTION2;
    private String OPTION3;
    private String OPTION4;
    private String OPTION5;
        
    public SelectOptionsExtensionNGTest() {    
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
        
        field = new CheckComboBox<>();      
        field.getItems().addAll(data);
        BulkSelectionOptions = new SelectOptionsExtension(field);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of setSelectionOption method, of class SelectOptionsExtension.
     */
    @Test
    public void testSetSelectionOption() {
        final EventHandler<ActionEvent> invertSelectionEvent = event -> {
            for (int i = 0 ; i < field.getCheckModel().getItemCount() ; i++){
                field.getCheckModel().toggleCheckState(i);
            }
        };
        
        //Test the number of BulkSelectionOtions increased by 1
        int optionsCountInitial = this.BulkSelectionOptions.getMenuButton().getItems().size();
        this.BulkSelectionOptions.setSelectionOption("InvertSelection", invertSelectionEvent);
        int optionsCountFinal = this.BulkSelectionOptions.getMenuButton().getItems().size();
        Assert.assertTrue(optionsCountInitial + 1 == optionsCountFinal);
    }

    /**
     * Test of enablePopUp method, of class SelectOptionsExtension.
     */
    @Test
    public void testEnablePopUp() {
        Assert.assertNull(this.BulkSelectionOptions.getField().getOnContextMenuRequested());       
        this.BulkSelectionOptions.enablePopUp();        
        Assert.assertNotNull(this.BulkSelectionOptions.getField().getOnContextMenuRequested());
    }

    /**
     * Test of disablePopUp method, of class SelectOptionsExtension.
     */
    @Test
    public void testDisablePopUp() {
        this.BulkSelectionOptions.enablePopUp();  
        Assert.assertNotNull(this.BulkSelectionOptions.getField().getOnContextMenuRequested());     
        this.BulkSelectionOptions.disablePopUp();        
        Assert.assertNull(this.BulkSelectionOptions.getField().getOnContextMenuRequested());
    }

    /**
     * Test of getMenuButton method, of class SelectOptionsExtension.
     */
    @Test
    public void testGetMenuButton() {    
        MenuButton menuButton = this.BulkSelectionOptions.getMenuButton();
        
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
}
