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
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.ColorEditorFactory.ColorEditor;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.paint.Color;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
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
public class ColorEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(ColorEditorFactoryNGTest.class.getName());
    
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
     * Test of createEditor method, of class ColorEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final ColorEditorFactory instance = new ColorEditorFactory();
        final AbstractEditor<ConstellationColor> result = instance.createEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        // could be different abstract editors for the ConstellationColor type but we want to make sure it's the right one
        assertTrue(result instanceof ColorEditor);
    }
    
    /**
     * Test of updateControlsWithValue method, of class ColorEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final ColorEditorFactory instance = new ColorEditorFactory();
        final ColorEditor editor = instance.new ColorEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        // default values from instantiation
        assertNull(editor.getComboBoxColor());
        assertNull(editor.getPickerColor());
        
        editor.updateControlsWithValue(ConstellationColor.CYAN);
        
        assertEquals(editor.getComboBoxColor(), ConstellationColor.CYAN);
        assertEquals(editor.getPickerColor(), Color.CYAN);
    }
    
    /**
     * Test of getValueFromControls method, of class ColorEditor.
     * 
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testGetValueFromControls() throws ControlsInvalidException {
        System.out.println("getValueFromControls");
        
        final ColorEditorFactory instance = new ColorEditorFactory();
        final ColorEditor editor = instance.new ColorEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        // need to run in order for editor controls to be instantiated
        editor.createEditorControls();
        
        final ConstellationColor color = ConstellationColor.CYAN;
        editor.updateControlsWithValue(color);
        
        assertEquals(editor.getValueFromControls(), color);
    }
}
