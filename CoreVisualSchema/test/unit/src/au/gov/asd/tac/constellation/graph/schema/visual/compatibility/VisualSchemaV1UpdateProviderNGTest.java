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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility.GraphLabelV0;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility.GraphLabelsAndDecoratorsV0;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility.GraphLabelsAndDecoratorsV0.Decorator;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
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
public class VisualSchemaV1UpdateProviderNGTest {
    
    StoreGraph mockStoreGraph;
    VisualSchemaV1UpdateProvider instance;
     
    // Captors
    ArgumentCaptor<Integer> attributeCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
    ArgumentCaptor<Object> listCaptor = ArgumentCaptor.forClass(Object.class);
    
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
        instance = new VisualSchemaV1UpdateProvider();
        mockStoreGraph = mock(StoreGraph.class);
        attributeCaptor = ArgumentCaptor.forClass(Integer.class);
        idCaptor = ArgumentCaptor.forClass(Integer.class);
        listCaptor = ArgumentCaptor.forClass(Object.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getSchema method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testGetSchema() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testGetSchema");
        SchemaFactory factory = instance.getSchema();
        assertEquals(factory.getName(), VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    /**
     * Test of getFromVersionNumber method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testGetFromVersionNumber() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testGetFromVersionNumber");
        assertEquals(instance.getFromVersionNumber(), UpdateProvider.DEFAULT_VERSION);
    }

    /**
     * Test of getToVersionNumber method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testGetToVersionNumber() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testGetToVersionNumber");
        assertEquals(instance.getToVersionNumber(), VisualSchemaV1UpdateProvider.SCHEMA_VERSION_THIS_UPDATE);
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labels_getAttributeNotFound() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labels_getAttributeNotFound");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(Graph.NOT_FOUND);
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(0)).getObjectValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(0)).removeAttribute(anyInt());
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labels_getAttributeNull() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labels_getAttributeNull");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(23);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getObjectValue(23, 0)).thenReturn(null);
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).getObjectValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(anyInt());
        Mockito.verify(mockStoreGraph, times(0)).setObjectValue(anyInt(), anyInt(), any());
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labels_getAttributeValid() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labels_getAttributeValid");
        GraphLabelsAndDecoratorsV0 labelsAndDecorators = new GraphLabelsAndDecoratorsV0();
        labelsAndDecorators.addBottomLabel(new GraphLabelV0("bottom;red;1"));
        labelsAndDecorators.addTopLabel(new GraphLabelV0("top;red;1"));
        labelsAndDecorators.addTopLabel(new GraphLabelV0("top;red;2"));;
        labelsAndDecorators.addConnectionLabel(new GraphLabelV0("connection;red;1"));
        labelsAndDecorators.addConnectionLabel(new GraphLabelV0("connection;red;2"));
        labelsAndDecorators.addConnectionLabel(new GraphLabelV0("connection;red;3"));
        labelsAndDecorators.setDecoratorLabel(Decorator.NW, "NW");
        labelsAndDecorators.setDecoratorLabel(Decorator.SW, "SW");
        labelsAndDecorators.setDecoratorLabel(Decorator.NE, "");
        labelsAndDecorators.setDecoratorLabel(Decorator.SE, "null");

        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(23);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getObjectValue(23, 0)).thenReturn(labelsAndDecorators);
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(4)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), listCaptor.capture());
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(anyInt());
        assertEquals((int)attributeCaptor.getAllValues().get(0), 0);
        assertEquals((int)attributeCaptor.getAllValues().get(1), 0);
        assertEquals((int)attributeCaptor.getAllValues().get(2), 0);
        assertEquals((int)attributeCaptor.getAllValues().get(3), 0);
        assertEquals((int)idCaptor.getAllValues().get(0), 0);
        assertEquals((int)idCaptor.getAllValues().get(1), 0);
        assertEquals((int)idCaptor.getAllValues().get(2), 0);
        assertEquals((int)idCaptor.getAllValues().get(3), 0);
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(0)).toString(), "bottom;Red;1.0");
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(1)).toString(), "top;Red;1.0|top;Red;2.0");
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(2)).toString(), "connection;Red;1.0|connection;Red;2.0|connection;Red;3.0");
        assertEquals(((VertexDecorators)listCaptor.getAllValues().get(3)).toString(), "\"NW\";\"SW\";;;");        
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labelstop_getAttributeNotFound() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labelstop_getAttributeNotFound");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(23);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("a,b,c");
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).getStringValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(anyInt());
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), listCaptor.capture());
        assertEquals((int)attributeCaptor.getAllValues().get(0), 0);
        assertEquals((int)idCaptor.getAllValues().get(0), 0);
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(0)).toString(), "a;LightBlue;1.0|b;LightBlue;1.0|c;LightBlue;1.0");
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labelstop_getAttributeNull() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labelstop_getAttributeNull");    
        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(23);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("a,b,c");
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).getStringValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(anyInt());
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), listCaptor.capture());
        assertEquals((int)attributeCaptor.getAllValues().get(0), 0);
        assertEquals((int)idCaptor.getAllValues().get(0), 0);
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(0)).toString(), "a;LightBlue;1.0|b;LightBlue;1.0|c;LightBlue;1.0");
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labelstop_getAttributeValid() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labelstop_getAttributeValid");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(23);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("a,b,c");
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).getStringValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(anyInt());
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), listCaptor.capture());
        assertEquals((int)attributeCaptor.getAllValues().get(0), 0);
        assertEquals((int)idCaptor.getAllValues().get(0), 0);
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(0)).toString(), "a;LightBlue;1.0|b;LightBlue;1.0|c;LightBlue;1.0");
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labelsbottom_getAttributeNotFound() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labelsbottom_getAttributeNotFound");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(23);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("a,b,c");
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).getStringValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(anyInt());
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), listCaptor.capture());
        assertEquals((int)attributeCaptor.getAllValues().get(0), 0);
        assertEquals((int)idCaptor.getAllValues().get(0), 0);
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(0)).toString(), "a;LightBlue;1.0|b;LightBlue;1.0|c;LightBlue;1.0");
    }

    /**
     * Test of schemaUpdate method, of class VisualSchemaV1UpdateProvider.
     */
    @Test
    public void testSchemaUpdate_labelsbottom_getAttributeValid() {
        System.out.println("VisualSchemaV1UpdateProviderNGTest.testSchemaUpdate_labelsbottom_getAttributeValid");
        when(mockStoreGraph.getAttribute(GraphElementType.META, "labels")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_top")).thenReturn(Graph.NOT_FOUND);
        when(mockStoreGraph.getAttribute(GraphElementType.GRAPH, "labels_bottom")).thenReturn(23);
        when(mockStoreGraph.getStringValue(23, 0)).thenReturn("a,b,c");
        instance.schemaUpdate(mockStoreGraph);
        Mockito.verify(mockStoreGraph, times(1)).getStringValue(anyInt(), anyInt());
        Mockito.verify(mockStoreGraph, times(1)).removeAttribute(anyInt());
        Mockito.verify(mockStoreGraph, times(1)).setObjectValue(attributeCaptor.capture(), idCaptor.capture(), listCaptor.capture());
        assertEquals((int)attributeCaptor.getAllValues().get(0), 0);
        assertEquals((int)idCaptor.getAllValues().get(0), 0);
        assertEquals(((GraphLabels)listCaptor.getAllValues().get(0)).toString(), "a;LightBlue;1.0|b;LightBlue;1.0|c;LightBlue;1.0");
    }
}
