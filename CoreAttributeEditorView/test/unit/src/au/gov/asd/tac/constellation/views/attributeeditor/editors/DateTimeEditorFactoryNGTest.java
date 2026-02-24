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
import au.gov.asd.tac.constellation.utilities.temporal.TemporalUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.DateTimeEditorFactory.DateTimeEditor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
public class DateTimeEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(DateTimeEditorFactoryNGTest.class.getName());
    
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
     * Test of createEditor method, of class DateTimeEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final DateTimeEditorFactory instance = new DateTimeEditorFactory();
        final AbstractEditor<ZonedDateTime> result = instance.createEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        // could be different abstract editors for the ZonedDateTime type but we want to make sure it's the right one
        assertTrue(result instanceof DateTimeEditor);
    }
    
    /**
     * Test of updateControlsWithValue method, of class DateTimeEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final DateTimeEditorFactory instance = new DateTimeEditorFactory();
        final DateTimeEditor editor = instance.new DateTimeEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
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
            assertEquals(editor.getTZValue(), TemporalUtilities.UTC);

            editor.updateControlsWithValue(ZonedDateTime.of(2050, 5, 5, 5, 50, 50, 5000000, ZoneId.of("GMT+2")));
            
            assertEquals(editor.getDateValue(), LocalDate.of(2050, 5, 5));
            assertEquals(editor.getHourValue(), (Integer) 5);
            assertEquals(editor.getMinValue(), (Integer) 50);
            assertEquals(editor.getSecValue(), (Integer) 50);
            assertEquals(editor.getMilliValue(), (Integer) 5);
            assertEquals(editor.getTZValue(), ZoneId.of("GMT+2"));
        }     
    }
    
    /**
     * Test of getValueFromControls method, of class DateTimeEditor.
     * 
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testGetValueFromControls() throws ControlsInvalidException {
        System.out.println("getValueFromControls");
        
        final DateTimeEditorFactory instance = new DateTimeEditorFactory();
        final DateTimeEditor editor = instance.new DateTimeEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
            
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();

        final ZonedDateTime zonedDateTime = ZonedDateTime.of(2050, 5, 5, 5, 50, 50, 5000000, ZoneId.of("GMT+2"));
        editor.updateControlsWithValue(zonedDateTime);

        assertEquals(editor.getValueFromControls(), zonedDateTime);
    }
}
