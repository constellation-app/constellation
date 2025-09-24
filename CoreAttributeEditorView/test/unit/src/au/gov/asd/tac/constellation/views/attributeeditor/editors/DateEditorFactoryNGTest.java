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
package au.gov.asd.tac.constellation.views.attributeeditor.editors;

import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.DateEditorFactory.DateEditor;
import java.time.LocalDate;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class DateEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(DateEditorFactoryNGTest.class.getName());
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    /**
     * Test of createEditor method, of class DateEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final DateEditorFactory instance = new DateEditorFactory();
        final AbstractEditor<LocalDate> result = instance.createEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        // could be different abstract editors for the LocalDate type but we want to make sure it's the right one
        assertTrue(result instanceof DateEditor);
    }
    
    /**
     * Test of updateControlsWithValue method, of class DateEditor.
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testUpdateControlsWithValue() throws ControlsInvalidException {
        System.out.println("updateControlsWithValue");
        
        final DateEditorFactory instance = new DateEditorFactory();
        final DateEditor editor = instance.new DateEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        try (final MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            localDateMockedStatic.when(() -> LocalDate.now()).thenReturn(LocalDate.EPOCH);
            
            // need to run in order for editor controls to be instantiated
            editor.createEditorControls();
            
            // default values from instantiation
            assertEquals(editor.getValueFromControls(), LocalDate.EPOCH);

            editor.updateControlsWithValue(LocalDate.of(2050, 5, 5));
            assertEquals(editor.getValueFromControls(), LocalDate.of(2050, 5, 5));
        }     
    }
}
