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
import au.gov.asd.tac.constellation.views.attributeeditor.editors.LocalDateTimeEditorFactory.LocalDateTimeEditor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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
public class LocalDateTimeEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(LocalDateTimeEditorFactoryNGTest.class.getName());
    
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
     * Test of createEditor method, of class LocalDateTimeEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final LocalDateTimeEditorFactory instance = new LocalDateTimeEditorFactory();
        final AbstractEditor<LocalDateTime> result = instance.createEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        // could be different abstract editors for the LocalDateTime type but we want to make sure it's the right one
        assertTrue(result instanceof LocalDateTimeEditor);
    }
    
    /**
     * Test of updateControlsWithValue method, of class LocalDateTimeEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final LocalDateTimeEditorFactory instance = new LocalDateTimeEditorFactory();
        final LocalDateTimeEditor editor = instance.new LocalDateTimeEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        try (final MockedStatic<LocalDate> localDateMockedStatic = Mockito.mockStatic(LocalDate.class, Mockito.CALLS_REAL_METHODS);
                final MockedStatic<LocalTime> localTimeMockedStatic = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            localDateMockedStatic.when(LocalDate::now).thenReturn(LocalDate.EPOCH);
            localTimeMockedStatic.when(() -> LocalTime.now(ZoneOffset.UTC)).thenReturn(LocalTime.NOON);
            
            // need to run in order for editor controls to be instantiated
            editor.createEditorControls();
            
            // default values from instantiation
            assertEquals(editor.getDateValue(), LocalDate.EPOCH);
            assertEquals(editor.getHourValue(), (Integer) 12);
            assertEquals(editor.getMinValue(), (Integer) 0);
            assertEquals(editor.getSecValue(), (Integer) 0);
            assertEquals(editor.getMilliValue(), (Integer) 0);

            editor.updateControlsWithValue(LocalDateTime.of(2050, 5, 5, 5, 50, 50, 5000000));
            
            assertEquals(editor.getDateValue(), LocalDate.of(2050, 5, 5));
            assertEquals(editor.getHourValue(), (Integer) 5);
            assertEquals(editor.getMinValue(), (Integer) 50);
            assertEquals(editor.getSecValue(), (Integer) 50);
            assertEquals(editor.getMilliValue(), (Integer) 5);
        }     
    }
    
    /**
     * Test of getValueFromControls method, of class LocalDateTimeEditor.
     * 
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testGetValueFromControls() throws ControlsInvalidException {
        System.out.println("getValueFromControls");
        
        final LocalDateTimeEditorFactory instance = new LocalDateTimeEditorFactory();
        final LocalDateTimeEditor editor = instance.new LocalDateTimeEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
            
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();

        final LocalDateTime localDateTime = LocalDateTime.of(2050, 5, 5, 5, 50, 50, 5000000);
        editor.updateControlsWithValue(localDateTime);

        assertEquals(editor.getValueFromControls(), localDateTime);
    }
}
