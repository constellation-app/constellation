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
package au.gov.asd.tac.constellation.utilities.gui.field.framework;

import au.gov.asd.tac.constellation.utilities.gui.field.MultiChoiceInput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import static org.assertj.core.api.Java6Assertions.assertThatCode;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class MultiChoiceInputNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(MultiChoiceInputNGTest.class.getName());
    List<String> fruitList;
    
    public MultiChoiceInputNGTest() {
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
                
        final String[] fruits = {"apple", "banana", "orange"};
        fruitList = Arrays.asList(fruits);
        
    }
    
    
    @Test
    public void testMultiChoiceInput_options() {               
        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput<String>());
        multiChoiceInput.setOptions(fruitList);
        final List fruitOptions = multiChoiceInput.getOptions();

        assertEquals(fruitOptions.size(), fruitList.size());
        assertEquals(fruitOptions.getFirst(), "apple");
        assertTrue(fruitOptions.contains("banana"));
        assertEquals(fruitOptions.getLast(), "orange");               
    }
     
    @Test(expectedExceptions = InvalidOperationException.class)
    public void testMultiChoiceInputField_nullOptions() {  
        final ChoiceInputField multiChoiceInputFieldMock = spy(new MultiChoiceInput());
        assertEquals(multiChoiceInputFieldMock.getOptions().size(), 0);
        multiChoiceInputFieldMock.setOptions(null);        
    }
    
    @Test
    public void testMultiChoiceInputField_choices() {  
        final ObservableList<String> observableList = FXCollections.observableArrayList();
        observableList.addAll(fruitList);
        observableList.add("None");
        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput(observableList));
        
        assertEquals(multiChoiceInput.getText(), "");
        multiChoiceInput.setChoice(fruitList.getFirst());
        assertEquals(multiChoiceInput.getText(), fruitList.getFirst());
        
        final List<String> myChoices = new ArrayList<>();
        myChoices.add(fruitList.getLast());
        myChoices.add("None");
        multiChoiceInput.setChoices(myChoices);
        assertEquals(multiChoiceInput.getText(), fruitList.getLast() + ", None");

        multiChoiceInput.clearChoices();
        assertEquals(multiChoiceInput.getText(), "");
        assertEquals(multiChoiceInput.getValue().size(), 0);
        multiChoiceInput.setChoice(fruitList.getFirst());
        assertEquals(multiChoiceInput.getText(), fruitList.getFirst());
        assertEquals(multiChoiceInput.getValue().size(), 1);
        assertEquals(multiChoiceInput.getValue().getFirst(), fruitList.getFirst());
        
        multiChoiceInput.setValue(myChoices);
        assertEquals(multiChoiceInput.getText(), fruitList.getLast() + ", None");
    }
    
    @Test
    public void testMultiChoiceInputField_getLocalMenuItems() {  
        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());      
        assertEquals(multiChoiceInput.getLocalMenuItems().size(), 1);
        final MenuItem menuItem = (MenuItem) multiChoiceInput.getLocalMenuItems().getFirst();
        assertEquals(menuItem.getText(), "Show Choices...");
    }   

    @Test
    public void testMultiChoiceInputField_isValidContent() {  
        final ObservableList<String> observableList = FXCollections.observableArrayList();
        observableList.addAll(fruitList);
        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput(observableList));
        
        multiChoiceInput.setChoice(fruitList.getFirst());
        assertTrue(multiChoiceInput.isValidContent());
        multiChoiceInput.setText("Nonsense");
        assertFalse(multiChoiceInput.isValidContent());
    }  
    
    @Test
    public void testMultiChoiceInputField_getAutocompleteSuggestions() {  
        final ObservableList<String> observableList = FXCollections.observableArrayList();
        observableList.addAll(fruitList);
        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput(observableList));
        
        multiChoiceInput.setText("o");
        final List<MenuItem> autoCompleteSuggestions = multiChoiceInput.getAutoCompleteSuggestions();
        assertEquals(autoCompleteSuggestions.size(), 1);
        final MenuItem suggestion = autoCompleteSuggestions.get(0);
        assertEquals(suggestion.getText(), "orange");
        
        multiChoiceInput.setText("a");
        final MenuItem nextSuggestion = (MenuItem) multiChoiceInput.getAutoCompleteSuggestions().getFirst();
        assertEquals(nextSuggestion.getText(), "apple");
    }
    
    @Test
    public void testMultiChoiceInputField_ChoiceInputDropDown() {  
        final MultiChoiceInput multiChoiceInput = spy(new MultiChoiceInput());
        multiChoiceInput.setOptions(fruitList);
        doNothing().when(multiChoiceInput).showDropDown(Mockito.any());
        assertThatCode(() -> multiChoiceInput.executeRightButtonAction()).doesNotThrowAnyException();
    }
}
