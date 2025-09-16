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
import au.gov.asd.tac.constellation.views.attributeeditor.editors.FloatEditorFactory.FloatEditor;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testfx.api.FxToolkit;
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
public class FloatEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(FloatEditorFactoryNGTest.class.getName());
    
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
     * Test of createEditor method, of class FloatEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final FloatEditorFactory instance = new FloatEditorFactory();
        final AbstractEditor<Float> result = instance.createEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        // could be different abstract editors for the float type but we want to make sure it's the right one
        assertTrue(result instanceof FloatEditor);
    }
    
    /**
     * Test of canSet method, of class FloatEditor.
     */
    @Test
    public void testCanSet() {
        System.out.println("canSet");
        
        final FloatEditorFactory instance = new FloatEditorFactory();
        final FloatEditor editor = instance.new FloatEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        assertTrue(editor.canSet(2F));
        assertFalse(editor.canSet(null));
    }
    
    /**
     * Test of updateControlsWithValue method, of class FloatEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final FloatEditorFactory instance = new FloatEditorFactory();
        final FloatEditor editor = instance.new FloatEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        // default values from instantiation
        assertEquals(editor.getNumberText(), "");
        
        editor.updateControlsWithValue(2F);
        
        assertEquals(editor.getNumberText(), "2.0");
    }
    
    /**
     * Test of getValueFromControls method, of class FloatEditor.
     * 
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testGetValueFromControls() throws ControlsInvalidException {
        System.out.println("getValueFromControls");
        
        final FloatEditorFactory instance = new FloatEditorFactory();
        final FloatEditor editor = instance.new FloatEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        final float f = 2F;
        editor.updateControlsWithValue(f);
        
        assertEquals(editor.getValueFromControls(), f);
    }
}
