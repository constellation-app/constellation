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
import au.gov.asd.tac.constellation.views.attributeeditor.editors.BooleanObjectEditorFactory.BooleanObjectEditor;
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
public class BooleanObjectEditorFactoryNGTest {
    
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
     * Test of createEditor method, of class BooleanObjectEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final BooleanObjectEditorFactory instance = new BooleanObjectEditorFactory();
        final AbstractEditor<Boolean> result = instance.createEditor("Test", null, null, null, null);
        // could be different abstract editors for the boolean type but we want to make sure it's the right one
        assertTrue(result instanceof BooleanObjectEditor);
    }
    
    /**
     * Test of canSet method, of class BooleanObjectEditor (AbstractEditor).
     */
    @Test
    public void testCanSet() {
        System.out.println("canSet");
        
        final BooleanObjectEditorFactory instance = new BooleanObjectEditorFactory();
        final BooleanObjectEditor editor = instance.new BooleanObjectEditor("Test", null, null, null, null);
        
        assertTrue(editor.canSet(true));
        assertTrue(editor.canSet(false));
        assertTrue(editor.canSet(null));
    }
    
    /**
     * Test of updateControlsWithValue method, of class BooleanObjectEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final BooleanObjectEditorFactory instance = new BooleanObjectEditorFactory();
        final BooleanObjectEditor editor = instance.new BooleanObjectEditor("Test", null, null, null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        // default values from instantiation
        assertFalse(editor.getValueFromControls());
        assertTrue(editor.isCheckBoxDisabled());
        
        editor.updateControlsWithValue(true);
        assertTrue(editor.getValueFromControls());
        assertTrue(editor.isCheckBoxDisabled());
                
        editor.updateControlsWithValue(null);
        assertFalse(editor.getValueFromControls());
        assertFalse(editor.isCheckBoxDisabled());
    }
}
