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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.ImageIconData;
import java.awt.image.BufferedImage;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author serpens24
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class VisualSchemaV2UpdateProviderNGTest extends ConstellationTest {
    
    StoreGraph mockStoreGraph;
    SchemaFactoryUtilities mockSchemaFactory;
    VisualSchemaV2UpdateProvider instance;
    
    public VisualSchemaV2UpdateProviderNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new VisualSchemaV2UpdateProvider();
        mockStoreGraph = mock(StoreGraph.class);
        mockSchemaFactory = mock(SchemaFactoryUtilities.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getSchema method, of class VisualSchemaV2UpdateProvider.
     */
    @Test
    public void testGetSchema() {
        System.out.println("VisualSchemaV2UpdateProviderNGTest.getSchema");
        SchemaFactory factory = instance.getSchema();
        assertEquals(factory.getName(), VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    /**
     * Test of getFromVersionNumber method, of class VisualSchemaV2UpdateProvider.
     */
    @Test
    public void testGetFromVersionNumber() {
        System.out.println("VisualSchemaV2UpdateProviderNGTest.getFromVersionNumber");
        assertEquals(instance.getFromVersionNumber(), VisualSchemaV1UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of getToVersionNumber method, of class VisualSchemaV2UpdateProvider.
     */
    @Test
    public void testGetToVersionNumber() {
        System.out.println("VisualSchemaV2UpdateProviderNGTest.getToVersionNumber");
        assertEquals(instance.getToVersionNumber(), VisualSchemaV2UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV2UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("VisualSchemaV2UpdateProviderNGTest.testSchemaUpdate");
        
        ArgumentCaptor<Integer> attributeCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Object> iconCaptor = ArgumentCaptor.forClass(Object.class);
        
        when(mockStoreGraph.getVertexCount()).thenReturn(10);
        when(mockStoreGraph.getVertex(anyInt())).thenReturn(0,1,2,3,4,5,6,7,8,9);
        when(mockStoreGraph.getObjectValue(anyInt(), anyInt())).thenReturn(
                new ConstellationIcon.Builder("Sphere", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("Background.Sphere", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("Square", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("Background.Square", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("Circle", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("Background.Circle", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("SoftSquare", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("Background.SoftSquare", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("Background.Soft Square", new ImageIconData((BufferedImage)null)).build(),
                new ConstellationIcon.Builder("default", new ImageIconData((BufferedImage)null)).build());
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).updateAttributeDefaultValue(0, "Background.Flat Square");
        Mockito.verify(mockStoreGraph, times(10)).getObjectValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(9)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), iconCaptor.capture());
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(0)).getName(), "Round Circle");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(1)).getName(), "Round Circle");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(2)).getName(), "Round Square");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(3)).getName(), "Round Square");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(4)).getName(), "Flat Circle");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(5)).getName(), "Flat Circle");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(6)).getName(), "Flat Square");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(7)).getName(), "Flat Square");
        assertEquals(((ConstellationIcon)iconCaptor.getAllValues().get(8)).getName(), "Flat Square");
    } 
}
