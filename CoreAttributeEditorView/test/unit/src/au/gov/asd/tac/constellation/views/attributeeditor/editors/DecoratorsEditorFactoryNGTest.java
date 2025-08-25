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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.DecoratorsEditorFactory.DecoratorsEditor;
import java.util.Arrays;
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
public class DecoratorsEditorFactoryNGTest {
    
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
     * Test of createEditor method, of class DecoratorsEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final DecoratorsEditorFactory instance = new DecoratorsEditorFactory();
        final AbstractEditor<VertexDecorators> result = instance.createEditor("Test", null, null, null, null);
        // could be different abstract editors for the VertexDecorators type but we want to make sure it's the right one
        assertTrue(result instanceof DecoratorsEditor);
    }
    
    /**
     * Test of canSet method, of class DecoratorsEditor.
     */
    @Test
    public void testCanSet() {
        System.out.println("canSet");
        
        final DecoratorsEditorFactory instance = new DecoratorsEditorFactory();
        final DecoratorsEditor editor = instance.new DecoratorsEditor("Test", null, null, null, null);
        
        assertTrue(editor.canSet(new VertexDecorators("attribute1", "attribute2", "attribute3", "attribute4")));
        assertFalse(editor.canSet(null));
    }
    
    /**
     * Test of updateControlsWithValue method, of class DecoratorsEditor.
     */
    @Test
    public void testUpdateControlsWithValue() {
        System.out.println("updateControlsWithValue");
        
        final DecoratorsEditorFactory instance = new DecoratorsEditorFactory();
        final DecoratorsEditor editor = instance.new DecoratorsEditor("Test", null, null, null, null);
        
        try (final MockedStatic<AttributeUtilities> attributeUtilitiesMockedStatic = Mockito.mockStatic(AttributeUtilities.class)) {
            final List<String> mockAttributes = Arrays.asList("attribute1", "attribute2", "attribute3", "attribute4");
            attributeUtilitiesMockedStatic.when(() -> AttributeUtilities.getAttributeNames(GraphElementType.VERTEX)).thenReturn(mockAttributes);
            
            // need to run in order for editor controls to be instantiated
            editor.createEditorControls();

            // default values from instantiation
            assertEquals(editor.getNWValue(), "");
            assertEquals(editor.getNEValue(), "");
            assertEquals(editor.getSEValue(), "");
            assertEquals(editor.getSWValue(), "");

            editor.updateControlsWithValue(new VertexDecorators("attribute1", "attribute2", "attribute3", "attribute4"));

            assertEquals(editor.getNWValue(), "attribute1");
            assertEquals(editor.getNEValue(), "attribute2");
            assertEquals(editor.getSEValue(), "attribute3");
            assertEquals(editor.getSWValue(), "attribute4");
        }
        
    }
    
    /**
     * Test of getValueFromControls method, of class DecoratorsEditor.
     * 
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testGetValueFromControls() throws ControlsInvalidException {
        System.out.println("getValueFromControls");
        
        final DecoratorsEditorFactory instance = new DecoratorsEditorFactory();
        final DecoratorsEditor editor = instance.new DecoratorsEditor("Test", null, null, null, null);
        
        try (final MockedStatic<AttributeUtilities> attributeUtilitiesMockedStatic = Mockito.mockStatic(AttributeUtilities.class)) {
            final List<String> mockAttributes = Arrays.asList("attribute1", "attribute2", "attribute3", "attribute4");
            attributeUtilitiesMockedStatic.when(() -> AttributeUtilities.getAttributeNames(GraphElementType.VERTEX)).thenReturn(mockAttributes);
            
            // need to run in order for editor controls to be instantiated
            editor.createEditorControls();

            final VertexDecorators decorators = new VertexDecorators("attribute1", "attribute2", "attribute3", "attribute4");
            editor.updateControlsWithValue(decorators);

            assertEquals(editor.getValueFromControls(), decorators);
        }
    }
}
