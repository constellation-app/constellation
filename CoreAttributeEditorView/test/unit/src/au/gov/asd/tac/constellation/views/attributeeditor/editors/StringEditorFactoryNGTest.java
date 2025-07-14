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

import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.interaction.ValueValidator;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author spica
 */
public class StringEditorFactoryNGTest {

    private static final Logger LOGGER = Logger.getLogger(StringEditorFactoryNGTest.class.getName());

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
     * Test of createEditor method, of class IconEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("testCreateEditor");

        final AbstractEditorFactory.AbstractEditor<String> instance = new StringEditorFactory().createEditor(
                mock(EditOperation.class),
                mock(DefaultGetter.class),
                mock(ValueValidator.class),
                "",
                "test");

        assertEquals(instance.getClass(), StringEditorFactory.StringEditor.class);
    }
    
    /**
     * Test of getAttributeType method, of class StringEditorFactory.
     */
    @Test
    public void testGetAttributeType() {
        System.out.println("getAttributeType");
        final StringEditorFactory instance = new StringEditorFactory();        
        assertTrue(instance.getAttributeType().equals(StringAttributeDescription.ATTRIBUTE_NAME));
    }
    
    /**
     * Test StringEditor
     */
    @Test
    public void testStringEditor() throws Exception{
        System.out.println("testStringEditor");

        final AbstractEditorFactory.AbstractEditor<String> instance = new StringEditorFactory().createEditor(
                mock(EditOperation.class),
                mock(DefaultGetter.class),
                mock(ValueValidator.class),
                "",
                null);

        instance.createEditorControls();
        
        assertEquals(instance.getClass(), StringEditorFactory.StringEditor.class);
        assertTrue(instance.noValueCheckBoxAvailable());
        assertTrue(Objects.isNull(instance.getValueFromControls()));
        
        instance.updateControlsWithValue("test");
        assertEquals(instance.getCurrentValue(), "test");
        assertEquals(instance.getValueFromControls(), "test");
        
        instance.updateControlsWithValue(null);
        assertTrue(Objects.isNull(instance.getValueFromControls()));
    }
}