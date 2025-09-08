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

import au.gov.asd.tac.constellation.utilities.temporal.TimeZoneUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.TimeZoneEditorFactory.TimeZoneEditor;
import java.time.ZoneId;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class TimeZoneEditorFactoryNGTest {
    
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
     * Test of createEditor method, of class TimeZoneEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final TimeZoneEditorFactory instance = new TimeZoneEditorFactory();
        final AbstractEditor<ZoneId> result = instance.createEditor("Test", null, null, null, null);
        // could be different abstract editors for the ZonedId type but we want to make sure it's the right one
        assertTrue(result instanceof TimeZoneEditor);
    }
    
    /**
     * Test of canSet method, of class TimeZoneEditor.
     */
    @Test
    public void testCanSet() {
        System.out.println("canSet");
        
        final TimeZoneEditorFactory instance = new TimeZoneEditorFactory();
        final TimeZoneEditor editor = instance.new TimeZoneEditor("Test", null, null, null, null);
        
        assertTrue(editor.canSet(ZoneId.of("GMT+2")));
        assertFalse(editor.canSet(null));
    }
    
    /**
     * Test of updateControlsWithValue method, of class TimeZoneEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final TimeZoneEditorFactory instance = new TimeZoneEditorFactory();
        final TimeZoneEditor editor = instance.new TimeZoneEditor("Test", null, null, null, null);
                   
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();

        // default values from instantiation
        assertEquals(editor.getValueFromControls(), TimeZoneUtilities.UTC);

        editor.updateControlsWithValue(ZoneId.of("GMT+2"));

        assertEquals(editor.getValueFromControls(), ZoneId.of("GMT+2")); 
    }
}
