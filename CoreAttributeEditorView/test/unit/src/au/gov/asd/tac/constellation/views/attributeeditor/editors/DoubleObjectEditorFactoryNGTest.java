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
import au.gov.asd.tac.constellation.views.attributeeditor.editors.DoubleObjectEditorFactory.DoubleObjectEditor;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class DoubleObjectEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(DoubleObjectEditorFactoryNGTest.class.getName());
    
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
     * Test of createEditor method, of class DoubleObjectEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final DoubleObjectEditorFactory instance = new DoubleObjectEditorFactory();
        final AbstractEditor<Double> result = instance.createEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        // could be different abstract editors for the double type but we want to make sure it's the right one
        assertTrue(result instanceof DoubleObjectEditor);
    }
    
    /**
     * Test of canSet method, of class DoubleObjectEditor (AbstractEditor).
     */
    @Test
    public void testCanSet() {
        System.out.println("canSet");
        
        final DoubleObjectEditorFactory instance = new DoubleObjectEditorFactory();
        final DoubleObjectEditor editor = instance.new DoubleObjectEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        assertTrue(editor.canSet(2D));
        assertTrue(editor.canSet(null));
    }
    
    /**
     * Test of updateControlsWithValue method, of class DoubleObjectEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final DoubleObjectEditorFactory instance = new DoubleObjectEditorFactory();
        final DoubleObjectEditor editor = instance.new DoubleObjectEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        // default values from instantiation
        assertEquals(editor.getNumberText(), "");
        
        editor.updateControlsWithValue(2D);
        
        assertEquals(editor.getNumberText(), "2");
    }
    
    /**
     * Test of getValueFromControls method, of class DoubleObjectEditor.
     * 
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testGetValueFromControls() throws ControlsInvalidException {
        System.out.println("getValueFromControls");
        
        final DoubleObjectEditorFactory instance = new DoubleObjectEditorFactory();
        final DoubleObjectEditor editor = instance.new DoubleObjectEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        final double d = 2D;
        editor.updateControlsWithValue(d);
        
        assertEquals(editor.getValueFromControls(), d);
    }
}
