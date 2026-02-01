/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.gui.field;

import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputConstants.ChoiceType;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.LeftButtonSupport;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.RightButtonSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.MenuItem;
import static org.assertj.core.api.Assertions.assertThatCode;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for SingleChoiceInput class
 * 
 * @author Delphinus8821
 */
public class SingleChoiceInputNGTest {

    private static final Logger LOGGER = Logger.getLogger(SingleChoiceInputNGTest.class.getName());

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
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    /**
     * Test of getChoice method, of class SingleChoiceInput.
     */
    @Test
    public void testGetSetAndRemoveChoice() {
        System.out.println("getChoice");
        final SingleChoiceInput instance = new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN);
        
        // Get null choice
        final Object nullResult = instance.getChoice();
        assertNull(nullResult);
        
        // Set an option
        final Object choice = "choice";
        final List options = new ArrayList<>();
        options.add(choice);
        instance.setOptions(options);
        instance.setChoice(choice);
        final Object result = instance.getChoice();
        assertEquals(result, choice);
        
        // Remove choice
        instance.removeChoice(choice);
        final Object emptyResult = instance.getChoice();
        assertNull(emptyResult);
    }

    /**
     * Test of getValue method, of class SingleChoiceInput.
     */
    @Test
    public void testGetAndSetValue() {
        System.out.println("getValue");
        final SingleChoiceInput instance = new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN);
        final Object choice = "choice";
        final List options = new ArrayList<>();
        options.add(choice);
        instance.setOptions(options);
        instance.setValue(choice);
        final Object result = instance.getValue();
        assertEquals(result, choice);
    }

    /**
     * Test of isValidContent method, of class SingleChoiceInput.
     */
    @Test
    public void testIsValidContent() {
        System.out.println("isValidContent");
        final SingleChoiceInput instance = new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN);
        
        // Test empty value
        boolean result = instance.isValidContent();
        assertTrue(result);
        
        // Test with a value
        final Object choice = "choice";
        final List<Object> options = new ArrayList<>();
        options.add(choice);
        instance.setOptions(options);
        instance.setChoice(choice);
        result = instance.isValidContent();
        assertTrue(result);
    }

    /**
     * Test of getLocalMenuItems method, of class SingleChoiceInput.
     */
    @Test
    public void testGetLocalMenuItems() {
        System.out.println("getLocalMenuItems");
        
        // Test single dropdown
        final SingleChoiceInput instance = new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN);
        final List<MenuItem> expResult = new ArrayList<>();
        final MenuItem choose = new MenuItem("Select Choice");
        expResult.add(choose);
        
        List<MenuItem> result = instance.getLocalMenuItems();
        assertEquals(result.size(), expResult.size());
        
        // Test single spinner
        final SingleChoiceInput spinnerInstance = new SingleChoiceInput(ChoiceType.SINGLE_SPINNER);
        final List<MenuItem> biggerResult = new ArrayList<>();
        final MenuItem next = new MenuItem("Increment");
        final MenuItem prev = new MenuItem("Decrement");
        biggerResult.add(next);
        biggerResult.add(prev);
        biggerResult.add(choose);
        
        result = spinnerInstance.getLocalMenuItems();
        assertEquals(result.size(), biggerResult.size());
    }

    /**
     * Test of getLeftButton method, of class SingleChoiceInput.
     */
    @Test
    public void testGetLeftButton() {
        System.out.println("getLeftButton");
        
        // Test single dropdown 
        final SingleChoiceInput instance = new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN);
        LeftButtonSupport.LeftButton expResult = null;
        LeftButtonSupport.LeftButton result = instance.getLeftButton();
        assertEquals(result, expResult);
        
        // Test single spinner
        final SingleChoiceInput spinnerInstance = new SingleChoiceInput(ChoiceType.SINGLE_SPINNER);
        final String spinnerName = ConstellationInputConstants.PREVIOUS_BUTTON_LABEL;
        LeftButtonSupport.LeftButton spinnerResult = spinnerInstance.getLeftButton();
        assertEquals(spinnerName, spinnerResult.getValue());
    }

    /**
     * Test of getRightButton method, of class SingleChoiceInput.
     */
    @Test
    public void testGetRightButton() {
        System.out.println("getRightButton");
        final SingleChoiceInput instance = new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN);
        final String dropDownResult = ConstellationInputConstants.SELECT_BUTTON_LABEL;
        final RightButtonSupport.RightButton result = instance.getRightButton();
        assertEquals(result.getValue(), dropDownResult);
        
        final SingleChoiceInput spinnerInstance = new SingleChoiceInput(ChoiceType.SINGLE_SPINNER);
        final String spinnerResult = ConstellationInputConstants.NEXT_BUTTON_LABEL;
        final RightButtonSupport.RightButton newResult = spinnerInstance.getRightButton();
        assertEquals(newResult.getValue(), spinnerResult);
    }
    
    /**
     * Test of executeRightButtonAction method, of class SingleChoiceInput.
     */
    @Test
    public void testExecuteRightButtonAction() {
        System.out.println("executeRightButtonAction");
        final SingleChoiceInput singleChoiceInput = spy(new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN));
        doNothing().when(singleChoiceInput).showDropDown(Mockito.any());
        assertThatCode(() -> singleChoiceInput.executeRightButtonAction()).doesNotThrowAnyException();
        
        doReturn(false).when(singleChoiceInput).isMenuShown();
        doNothing().when(singleChoiceInput).showDropDown(Mockito.any());       

        final RightButtonSupport.RightButton rightButton = singleChoiceInput.getRightButton();
        rightButton.show();
        verify(singleChoiceInput, times(2)).executeRightButtonAction();
        
        doReturn(true).when(singleChoiceInput).isMenuShown();
        // second consecutive time it is called, setMenuShown(false) is called
        rightButton.show();
        verify(singleChoiceInput, times(3)).executeRightButtonAction();
    }

    /**
     * Test of getAutoCompleteSuggestions method, of class SingleChoiceInput.
     */
    @Test
    public void testGetAutoCompleteSuggestions() {
        System.out.println("getAutoCompleteSuggestions");
        final SingleChoiceInput instance = new SingleChoiceInput(ChoiceType.SINGLE_DROPDOWN);
        final Object choice = "choice";
        final List<Object> options = new ArrayList<>();
        options.add(choice);
        instance.setOptions(options);
        instance.setChoice(choice);
        instance.setText("choice");
        final List<Object> result = instance.getAutoCompleteSuggestions();
        assertEquals(result.size(), options.size());
    }
    
}
