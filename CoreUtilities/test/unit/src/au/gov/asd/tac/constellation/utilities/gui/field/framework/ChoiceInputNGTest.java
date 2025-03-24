/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class ChoiceInputNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(ChoiceInputNGTest.class.getName());
    
    List<String> fruitList;
    
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

    @BeforeMethod
    public void setUpMethod() {                
        final String[] fruits = {"apple", "banana", "orange"};
        fruitList = Arrays.asList(fruits);
    }
    

    @Test(expectedExceptions = InvalidOperationException.class)
    public void testChoiceInputField_nullOptions() {  
        final ChoiceInputField choiceInputFieldMock = spy(createEmptyChoiceInputField());
        assertEquals(choiceInputFieldMock.getOptions().size(), 0);
        choiceInputFieldMock.setOptions(null);
        
        // test that null observableList throws exception
        assertThrows(InvalidOperationException.class,
            ()-> createChoiceInputField(null));
    }
    
    @Test
    public void testChoiceInputField_constructorWithOptions() {  
        final ObservableList<String> observableList = FXCollections.observableArrayList();
        observableList.addAll(fruitList);
        final ChoiceInputField choiceInputFieldMock = spy(createChoiceInputField(observableList));
        
        final List fruitOptions = choiceInputFieldMock.getOptions();
        assertEquals(fruitOptions.size(), fruitList.size());
        assertEquals(fruitOptions.getFirst(), "apple");
        assertTrue(fruitOptions.contains("banana"));
        assertEquals(fruitOptions.getLast(), "orange");    
        
        assertThrows(InvalidOperationException.class,
            ()-> choiceInputFieldMock.setOptions(null));
        assertTrue(choiceInputFieldMock.getOptions().isEmpty());

    }
    
    @Test
    public void testChoiceInputField_clearChoices() {  
        final ObservableList<String> observableList = FXCollections.observableArrayList();
        observableList.addAll(fruitList);
        final ChoiceInputField choiceInputFieldMock = spy(createChoiceInputField(observableList));
        
        // test setText and getText
        choiceInputFieldMock.setText(fruitList.getFirst());
        assertEquals(choiceInputFieldMock.getText(), fruitList.getFirst());
        
        // test clear choices
        choiceInputFieldMock.clearChoices();
        assertTrue(choiceInputFieldMock.getText().isEmpty());               
    }
    
    @Test
    public void testChoiceInputField_icons() {  
        final ChoiceInputField choiceInputFieldMock = spy(createEmptyChoiceInputField());
        
        final List<ImageView> iconsList = new ArrayList<>();
        final ImageView mockIcon = mock(ImageView.class);
        iconsList.add(mockIcon);
        choiceInputFieldMock.setIcons(iconsList);
        assertEquals(choiceInputFieldMock.getIcons().size(), iconsList.size());
        assertEquals(choiceInputFieldMock.getIcons().getFirst(), mockIcon);
    }
    
    public final ChoiceInputField createEmptyChoiceInputField() {
      return new ChoiceInputFieldImpl();
    }
    
    public ChoiceInputField createChoiceInputField(ObservableList options) {
      return new ChoiceInputFieldImpl(options);
    }

    private static class ChoiceInputFieldImpl extends ChoiceInputField {

        private List<MenuItem> items = new ArrayList<>();

        public ChoiceInputFieldImpl(final ObservableList options) {
            super(options);
            items = options;
        }
        
         public ChoiceInputFieldImpl() {
            super();
        }

        @Override
        public Object getValue() {
            return "";
        }

        @Override
        public void setValue(final Object value) {
            return;
        }

        @Override
        public boolean isValidContent() {
            return true;
        }

        @Override
        public List<MenuItem> getLocalMenuItems() {
            if (items != null) {
                return items;
            } else {
                return new ArrayList();
            }
        }
    }
}
