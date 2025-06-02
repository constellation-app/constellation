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
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.TransactionGraphLabelsAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.VertexGraphLabelsAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class VisualSchemaV7UpdateProviderNGTest {

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
     * Test of schemaUpdate method, of class VisualSchemaV7UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("schemaUpdate");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int oldGraphBottomlabelsAttributeId = graph.addAttribute(GraphElementType.GRAPH, VertexGraphLabelsAttributeDescription.ATTRIBUTE_NAME, "node_bottom_labels_colours", "node_bottom_labels_colours", null, null);
        final int oldGraphToplabelsAttributeId = graph.addAttribute(GraphElementType.GRAPH, VertexGraphLabelsAttributeDescription.ATTRIBUTE_NAME, "node_top_labels_colours", "node_top_labels_colours", null, null);
        final int oldGraphTransactionlabelsAttributeId = graph.addAttribute(GraphElementType.GRAPH, TransactionGraphLabelsAttributeDescription.ATTRIBUTE_NAME, "transaction_labels_colours", "transaction_labels_colours", null, null);
        
        graph.setStringValue(oldGraphBottomlabelsAttributeId, 0, "Label1;Blue;1|Label2;Red;2|Label3;Yellow;3");
        graph.setStringValue(oldGraphToplabelsAttributeId, 0, "Label4;Black;4|Label5;Green;5");
        graph.setStringValue(oldGraphTransactionlabelsAttributeId, 0, "Label6;White;6");
        
        final VisualSchemaV7UpdateProvider instance = new VisualSchemaV7UpdateProvider();
        
        assertNotEquals(graph.getAttribute(GraphElementType.GRAPH, "node_bottom_labels_colours"), Graph.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.GRAPH, "node_top_labels_colours"), Graph.NOT_FOUND);
        assertNotEquals(graph.getAttribute(GraphElementType.GRAPH, "transaction_labels_colours"), Graph.NOT_FOUND);
        
        assertEquals(VisualConcept.GraphAttribute.BOTTOM_LABELS.get(graph), Graph.NOT_FOUND);
        assertEquals(VisualConcept.GraphAttribute.TOP_LABELS.get(graph), Graph.NOT_FOUND);
        assertEquals(VisualConcept.GraphAttribute.TRANSACTION_LABELS.get(graph), Graph.NOT_FOUND);
        
        assertEquals(graph.getStringValue(oldGraphBottomlabelsAttributeId, 0), "Label1;Blue;1.0|Label2;Red;2.0|Label3;Yellow;3.0");
        assertEquals(graph.getStringValue(oldGraphToplabelsAttributeId, 0), "Label4;Black;4.0|Label5;Green;5.0");
        assertEquals(graph.getStringValue(oldGraphTransactionlabelsAttributeId, 0), "Label6;White;6.0");
        
        instance.schemaUpdate(graph);
        
        assertEquals(graph.getAttribute(GraphElementType.GRAPH, "node_bottom_labels_colours"), Graph.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.GRAPH, "node_top_labels_colours"), Graph.NOT_FOUND);
        assertEquals(graph.getAttribute(GraphElementType.GRAPH, "transaction_labels_colours"), Graph.NOT_FOUND);
        
        assertNotEquals(VisualConcept.GraphAttribute.BOTTOM_LABELS.get(graph), Graph.NOT_FOUND);
        assertNotEquals(VisualConcept.GraphAttribute.TOP_LABELS.get(graph), Graph.NOT_FOUND);
        assertNotEquals(VisualConcept.GraphAttribute.TRANSACTION_LABELS.get(graph), Graph.NOT_FOUND);
        
        final int newGraphBottomlabelsAttributeId = VisualConcept.GraphAttribute.BOTTOM_LABELS.get(graph);
        final int newGraphToplabelsAttributeId = VisualConcept.GraphAttribute.TOP_LABELS.get(graph);
        final int newGraphTransactionlabelsAttributeId = VisualConcept.GraphAttribute.TRANSACTION_LABELS.get(graph);
        
        assertEquals(graph.getStringValue(newGraphBottomlabelsAttributeId, 0), "Label1;Blue;1.0|Label2;Red;2.0|Label3;Yellow;3.0");
        assertEquals(graph.getStringValue(newGraphToplabelsAttributeId, 0), "Label4;Black;4.0|Label5;Green;5.0");
        assertEquals(graph.getStringValue(newGraphTransactionlabelsAttributeId, 0), "Label6;White;6.0");
    }   
}
