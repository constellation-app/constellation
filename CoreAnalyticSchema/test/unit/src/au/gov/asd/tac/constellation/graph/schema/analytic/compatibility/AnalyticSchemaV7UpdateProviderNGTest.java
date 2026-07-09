/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

/**
 *
 * @author andromeda-224
 */
public class AnalyticSchemaV7UpdateProviderNGTest {
    
    /**
     * Test of schemaUpdate method, of class AnalyticSchemaV7UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("schemaUpdate");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        // old Chinese Whispers attributes
        int oldChineseWhispersColorVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers.Color", null, null, null);
        int oldChineseWhispersVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers", null, null, null);        
        int oldChineseWhispersColorTransactionAttribute = graph.addAttribute(GraphElementType.TRANSACTION, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers.Color", null, null, null);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        
        final int tId = graph.addTransaction(vxId1, vxId2, true);
        final int oldId1 = 101;
        final int oldId2 = 102;
        
        // set attributes for vxId1
        graph.setObjectValue(oldChineseWhispersColorVertexAttribute, vxId1, ConstellationColor.BANANA);
        graph.setObjectValue(oldChineseWhispersVertexAttribute, vxId1, oldId1);
        graph.setObjectValue(oldChineseWhispersColorTransactionAttribute, tId, ConstellationColor.BUTTERMILK);
        
        // set attributes for vxId2
        graph.setObjectValue(oldChineseWhispersColorVertexAttribute, vxId2, ConstellationColor.BLUEBERRY);
        graph.setObjectValue(oldChineseWhispersVertexAttribute, vxId2, oldId2);        
                
        int labelPropagationColorVertexAttribute = ClusteringConcept.VertexAttribute.LABEL_PROPAGATION_COLOR.get(graph);
        int labelPropagationVertexAttribute = ClusteringConcept.VertexAttribute.LABEL_PROPAGATION_CLUSTER.get(graph);
        int labelPropagationColorTransactionAttribute = ClusteringConcept.TransactionAttribute.LABEL_PROPAGATION_COLOR.get(graph);
        
        // After initial setup, the old Chinese Whispers attributes are in graph,
        // the new ones do not exist
        assertNotEquals(oldChineseWhispersColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(oldChineseWhispersVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(oldChineseWhispersColorTransactionAttribute, Graph.NOT_FOUND);
        
        assertEquals(labelPropagationColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(labelPropagationColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(labelPropagationColorTransactionAttribute, Graph.NOT_FOUND);
        
        final AnalyticSchemaV7UpdateProvider instance = new AnalyticSchemaV7UpdateProvider();
        instance.schemaUpdate(graph);
        
        // After update, old Chinese Whispers attributes should not exist on the
        // graph, but the new ones will
        oldChineseWhispersColorVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Cluster.ChineseWhispers.Color");
        oldChineseWhispersVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Cluster.ChineseWhispers");        
        oldChineseWhispersColorTransactionAttribute = graph.getAttribute(GraphElementType.TRANSACTION, "Cluster.ChineseWhispers.Color");
        
        labelPropagationColorVertexAttribute = ClusteringConcept.VertexAttribute.LABEL_PROPAGATION_COLOR.get(graph);
        labelPropagationVertexAttribute = ClusteringConcept.VertexAttribute.LABEL_PROPAGATION_CLUSTER.get(graph);        
        labelPropagationColorTransactionAttribute = ClusteringConcept.TransactionAttribute.LABEL_PROPAGATION_COLOR.get(graph);
        
        assertEquals(oldChineseWhispersColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(oldChineseWhispersVertexAttribute, Graph.NOT_FOUND);
        assertEquals(oldChineseWhispersColorTransactionAttribute, Graph.NOT_FOUND);
                
        assertNotEquals(labelPropagationColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(labelPropagationVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(labelPropagationColorVertexAttribute, Graph.NOT_FOUND);
        
        // values for new label propagation attributes
        assertEquals(graph.getObjectValue(labelPropagationColorVertexAttribute, vxId1), ConstellationColor.BANANA);
        assertEquals(graph.getIntValue(labelPropagationVertexAttribute, vxId1), oldId1);
        
        assertEquals(graph.getObjectValue(labelPropagationColorVertexAttribute, vxId2), ConstellationColor.BLUEBERRY);
        assertEquals(graph.getIntValue(labelPropagationVertexAttribute, vxId2), oldId2);
        
        assertNotEquals(labelPropagationColorTransactionAttribute, Graph.NOT_FOUND);        
        assertEquals(graph.getObjectValue(labelPropagationColorTransactionAttribute, tId), ConstellationColor.BUTTERMILK);
        
    }   
}
