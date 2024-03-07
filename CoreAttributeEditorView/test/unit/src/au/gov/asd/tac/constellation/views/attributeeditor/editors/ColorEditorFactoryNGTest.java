/*
 * Copyright 2010-2024 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.DefaultGetter;
import au.gov.asd.tac.constellation.views.attributeeditor.editors.operations.EditOperation;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ColorEditorFactoryNGTest {

    public ColorEditorFactoryNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of createEditor method, of class ColorEditorFactory.
     */
    @Test
    public void testCreateEditor() {
        System.out.println("createEditor");

        final AbstractEditorFactory.AbstractEditor instance = new ColorEditorFactory().createEditor(
                mock(EditOperation.class),
                mock(DefaultGetter.class),
                mock(ValueValidator.class),
                "",
                mock(ConstellationColor.class));

        assertEquals(instance.getClass(), ColorEditorFactory.ColorEditor.class);
    }

    /**
     * Test of getAttributeType method, of class ColorEditorFactory.
     */
    @Test
    public void testGetAttributeType() {
        System.out.println("getAttributeType");
        ColorEditorFactory instance = new ColorEditorFactory();
        String result = instance.getAttributeType();
        assertEquals(result, ColorAttributeDescription.ATTRIBUTE_NAME);
    }

}
