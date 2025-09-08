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

import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.TimeEditorFactory.TimeEditor;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
public class TimeEditorFactoryNGTest {
    
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
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of createEditor method, of class TimeEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final TimeEditorFactory instance = new TimeEditorFactory();
        final AbstractEditor<LocalTime> result = instance.createEditor("Test", null, null, null, null);
        // could be different abstract editors for the LocalTime type but we want to make sure it's the right one
        assertTrue(result instanceof TimeEditor);
    }
    
    /**
     * Test of updateControlsWithValue method, of class LocalTimeEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final TimeEditorFactory instance = new TimeEditorFactory();
        final TimeEditor editor = instance.new TimeEditor("Test", null, null, null, null);
        
        try (final MockedStatic<LocalTime> localTimeMockedStatic = Mockito.mockStatic(LocalTime.class, Mockito.CALLS_REAL_METHODS)) {
            localTimeMockedStatic.when(() -> LocalTime.now(ZoneOffset.UTC)).thenReturn(LocalTime.NOON);
            
            // need to run in order for editor controls to be instantiated
            editor.createEditorControls();
            
            // default values from instantiation
            assertEquals(editor.getHourValue(), (Integer) 12);
            assertEquals(editor.getMinValue(), (Integer) 0);
            assertEquals(editor.getSecValue(), (Integer) 0);
            assertEquals(editor.getMilliValue(), (Integer) 0);

            editor.updateControlsWithValue(LocalTime.of(5, 50, 50, 5000000));
            
            assertEquals(editor.getHourValue(), (Integer) 5);
            assertEquals(editor.getMinValue(), (Integer) 50);
            assertEquals(editor.getSecValue(), (Integer) 50);
            assertEquals(editor.getMilliValue(), (Integer) 5);
        }     
    }
    
    /**
     * Test of getValueFromControls method, of class LocalTimeEditor.
     * 
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testGetValueFromControls() throws ControlsInvalidException {
        System.out.println("getValueFromControls");
        
        final TimeEditorFactory instance = new TimeEditorFactory();
        final TimeEditor editor = instance.new TimeEditor("Test", null, null, null, null);
            
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();

        final LocalTime localTime = LocalTime.of(5, 50, 50, 5000000);
        editor.updateControlsWithValue(localTime);

        assertEquals(editor.getValueFromControls(), localTime);
    }
}
