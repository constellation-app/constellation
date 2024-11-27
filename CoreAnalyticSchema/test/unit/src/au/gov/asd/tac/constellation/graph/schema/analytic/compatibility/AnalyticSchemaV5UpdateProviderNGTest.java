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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class AnalyticSchemaV5UpdateProviderNGTest {
    
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
     * Test of schemaUpdate method, of class AnalyticSchemaV5UpdateProvider.
     */
    @Test
    public void testSchemaUpdate() {
        System.out.println("schemaUpdate");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        int oldKTrussColorVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.KTruss.Colour", null, null, null);
        int oldHierarchicalColorVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.Hierarchical.Colour", null, null, null);
        int oldChineseWhispersColorVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.ChineseWhispers.Colour", null, null, null);
        int oldInfomapColorVertexAttribute = graph.addAttribute(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "Cluster.Infomap.Colour", null, null, null);
        
        final int vxId = graph.addVertex();
        
        graph.setObjectValue(oldKTrussColorVertexAttribute, vxId, ConstellationColor.AMETHYST);
        graph.setObjectValue(oldHierarchicalColorVertexAttribute, vxId, ConstellationColor.AZURE);
        graph.setObjectValue(oldChineseWhispersColorVertexAttribute, vxId, ConstellationColor.BANANA);
        graph.setObjectValue(oldInfomapColorVertexAttribute, vxId, ConstellationColor.BLACK);
        
        int kTrussColorVertexAttribute = ClusteringConcept.VertexAttribute.K_TRUSS_COLOR.get(graph);
        int hierarchicalColorVertexAttribute = ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR.get(graph);
        int chineseWhispersColorVertexAttribute = ClusteringConcept.VertexAttribute.CHINESE_WHISPERS_COLOR.get(graph);
        int infomapColorVertexAttribute = ClusteringConcept.VertexAttribute.INFOMAP_COLOR.get(graph);
        
        assertNotEquals(oldKTrussColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(oldHierarchicalColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(oldChineseWhispersColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(oldInfomapColorVertexAttribute, Graph.NOT_FOUND);
        
        assertEquals(kTrussColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(hierarchicalColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(chineseWhispersColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(infomapColorVertexAttribute, Graph.NOT_FOUND);
        
        final AnalyticSchemaV5UpdateProvider instance = new AnalyticSchemaV5UpdateProvider();
        instance.schemaUpdate(graph);
        
        oldKTrussColorVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Cluster.KTruss.Colour");
        oldHierarchicalColorVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Cluster.Hierarchical.Colour");
        oldChineseWhispersColorVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Cluster.ChineseWhispers.Colour");
        oldInfomapColorVertexAttribute = graph.getAttribute(GraphElementType.VERTEX, "Cluster.Infomap.Colour");
        
        kTrussColorVertexAttribute = ClusteringConcept.VertexAttribute.K_TRUSS_COLOR.get(graph);
        hierarchicalColorVertexAttribute = ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR.get(graph);
        chineseWhispersColorVertexAttribute = ClusteringConcept.VertexAttribute.CHINESE_WHISPERS_COLOR.get(graph);
        infomapColorVertexAttribute = ClusteringConcept.VertexAttribute.INFOMAP_COLOR.get(graph);
        
        assertEquals(oldKTrussColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(oldHierarchicalColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(oldChineseWhispersColorVertexAttribute, Graph.NOT_FOUND);
        assertEquals(oldInfomapColorVertexAttribute, Graph.NOT_FOUND);
        
        assertNotEquals(kTrussColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(hierarchicalColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(chineseWhispersColorVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(infomapColorVertexAttribute, Graph.NOT_FOUND);
        
        assertEquals(graph.getObjectValue(kTrussColorVertexAttribute, vxId), ConstellationColor.AMETHYST);
        assertEquals(graph.getObjectValue(hierarchicalColorVertexAttribute, vxId), ConstellationColor.AZURE);
        assertEquals(graph.getObjectValue(chineseWhispersColorVertexAttribute, vxId), ConstellationColor.BANANA);
        assertEquals(graph.getObjectValue(infomapColorVertexAttribute, vxId), ConstellationColor.BLACK);
    }
    
}
