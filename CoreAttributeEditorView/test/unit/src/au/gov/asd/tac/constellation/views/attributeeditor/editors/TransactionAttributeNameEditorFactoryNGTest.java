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
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.graph.utilities.AttributeUtilities;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.AbstractEditor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.TransactionAttributeNameEditorFactory.TransactionAttributeNameEditor;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class TransactionAttributeNameEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(TransactionAttributeNameEditorFactoryNGTest.class.getName());
    
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
     * Test of createEditor method, of class TransactionAttributeNameEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");
        
        final TransactionAttributeNameEditorFactory instance = new TransactionAttributeNameEditorFactory();
        final AbstractEditor<String> result = instance.createEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        // could be different abstract editors for the string type but we want to make sure it's the right one
        assertTrue(result instanceof TransactionAttributeNameEditor);
    }

    /**
     * Test of updateControlsWithValue method, of class TransactionAttributeNameEditor.
     * @throws au.gov.asd.tac.constellation.views.attributeeditor.editors.AbstractEditorFactory.ControlsInvalidException
     */
    @Test
    public void testUpdateControlsWithValue() throws ControlsInvalidException {
        System.out.println("updateControlsWithValue");
        
        final TransactionAttributeNameEditorFactory instance = new TransactionAttributeNameEditorFactory();
        final TransactionAttributeNameEditor editor = instance.new TransactionAttributeNameEditor("Test", null, ValueValidator.getAlwaysSucceedValidator(), null, null);
        
        try (final MockedStatic<AttributeUtilities> attributeUtilitiesMockedStatic = Mockito.mockStatic(AttributeUtilities.class)) {
            final List<String> mockAttributes = Arrays.asList("attribute1", "attribute2", "attribute3", "attribute4");
            attributeUtilitiesMockedStatic.when(() -> AttributeUtilities.getAttributeNames(GraphElementType.TRANSACTION)).thenReturn(mockAttributes);
            
            // need to run in order for editor controls to be instantiated
            editor.createEditorControls();
            
            // default values from instantiation
            try {
                assertNull(editor.getValueFromControls());
                fail();
            } catch (final ControlsInvalidException ex) {
                // continue on, this is expected for the default since the selection should be null
            }

            editor.updateControlsWithValue("attribute2");

            assertEquals(editor.getValueFromControls(), "attribute2");
        }
    }
}
