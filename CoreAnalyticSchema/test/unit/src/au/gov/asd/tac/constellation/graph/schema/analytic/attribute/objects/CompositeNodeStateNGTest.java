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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class CompositeNodeStateNGTest {
    
    private CompositeNodeState nullInstance;
    private CompositeNodeState contractedInstance;
    private CompositeNodeState expandedInstance;
    
    private String contractedString;
    private String expandedString;
    
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
        final RecordStore constituentNodeStore = new GraphRecordStore();
        constituentNodeStore.add();
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0");
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 1.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 2.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 3.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0>Type<Unknown>");
        constituentNodeStore.add();
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #1");
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, -1.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, -2.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, -3.0);
        constituentNodeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #1>Type<Unknown>");
        
        final List<String> expandedIds = Arrays.asList("copy.Identifier<Vertex #0>Type<Unknown>", "copy.Identifier<Vertex #1>Type<Unknown>");
        
        final float[] mean = new float[]{1.5F ,1.0F, 0.5F};
        
        final ContractedCompositeNodeState contractedNodeState = new ContractedCompositeNodeState(constituentNodeStore, expandedIds, expandedIds, mean);
        
        final RecordStore compositeNodeStore = new GraphRecordStore();
        compositeNodeStore.add();
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0 + 1 more...");
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.X, 1.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Y, 2.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.Z, 3.0);
        compositeNodeStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>");
        
        final ExpandedCompositeNodeState expandedNodeState = new ExpandedCompositeNodeState(compositeNodeStore, "copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>", true, 2);
        
        nullInstance = new CompositeNodeState(0, (ContractedCompositeNodeState) null);
        contractedInstance = new CompositeNodeState(0, contractedNodeState);
        expandedInstance = new CompositeNodeState(0, expandedNodeState);
        
        contractedString = "{\"nodeId\":0,\"expandedState\":null,\"contractedState\":{\"constituentNodeStore\":\"[{\\\"source.Identifier\\\":\\\"Vertex #0\\\",\\\"source.x\\\":\\\"1.0\\\",\\\"source.y\\\":\\\"2.0\\\",\\\"source.z\\\":\\\"3.0\\\",\\\"source.[id]\\\":\\\"copy.Identifier<Vertex #0>Type<Unknown>\\\"},{\\\"source.Identifier\\\":\\\"Vertex #1\\\",\\\"source.x\\\":\\\"-1.0\\\",\\\"source.y\\\":\\\"-2.0\\\",\\\"source.z\\\":\\\"-3.0\\\",\\\"source.[id]\\\":\\\"copy.Identifier<Vertex #1>Type<Unknown>\\\"}]\",\"expandedIds\":[\"copy.Identifier<Vertex #0>Type<Unknown>\",\"copy.Identifier<Vertex #1>Type<Unknown>\"],\"affectedExpandedIds\":[\"copy.Identifier<Vertex #0>Type<Unknown>\",\"copy.Identifier<Vertex #1>Type<Unknown>\"],\"mean\":[1.5,1.0,0.5]}}";
        expandedString = "{\"nodeId\":0,\"expandedState\":{\"compositeNodeStore\":\"[{\\\"source.Identifier\\\":\\\"Vertex #0 + 1 more...\\\",\\\"source.x\\\":\\\"1.0\\\",\\\"source.y\\\":\\\"2.0\\\",\\\"source.z\\\":\\\"3.0\\\",\\\"source.[id]\\\":\\\"copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>\\\"}]\",\"compositeId\":\"copy.Identifier<Vertex #0 + 1 more...>Type<Unknown>\",\"isAffecting\":true,\"numberOfNodes\":2},\"contractedState\":null}";
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of getNumberOfNodes method, of class CompositeNodeState.
     */
    @Test
    public void testGetNumberOfNodes() {
        System.out.println("getNumberOfNodes");
        
        assertEquals(nullInstance.getNumberOfNodes(), 0);
        assertEquals(contractedInstance.getNumberOfNodes(), 2);
        assertEquals(expandedInstance.getNumberOfNodes(), 2);
    }

    /**
     * Test of getStatus method, of class CompositeNodeState.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        
        assertEquals(nullInstance.getStatus(), CompositeStatus.NOT_A_COMPOSITE);
        assertEquals(contractedInstance.getStatus(), CompositeStatus.IS_A_COMPOSITE);
        assertEquals(expandedInstance.getStatus(), CompositeStatus.LEADER_OF_A_COMPOSITE);
    }

    /**
     * Test of isComposite method, of class CompositeNodeState.
     */
    @Test
    public void testIsComposite() {
        System.out.println("isComposite");
        
        assertFalse(nullInstance.isComposite());
        assertTrue(contractedInstance.isComposite());
        assertFalse(expandedInstance.isComposite());
    }

    /**
     * Test of comprisesAComposite method, of class CompositeNodeState.
     */
    @Test
    public void testComprisesAComposite() {
        System.out.println("comprisesAComposite");
        
        assertFalse(nullInstance.comprisesAComposite());
        assertFalse(contractedInstance.comprisesAComposite());
        assertTrue(expandedInstance.comprisesAComposite());
    }

    /**
     * Test of convertToString method, of class CompositeNodeState.
     */
    @Test
    public void testConvertToString() {
        System.out.println("convertToString");
        
        final String contractedResult = contractedInstance.convertToString();
        final String expandedResult = expandedInstance.convertToString();
        
        assertEquals(contractedResult, contractedString);
        assertEquals(expandedResult, expandedString);
    }

    /**
     * Test of createFromString method, of class CompositeNodeState.
     */
    @Test
    public void testCreateFromString() {
        System.out.println("createFromString");
        
        final CompositeNodeState contractedResult = CompositeNodeState.createFromString(contractedString);
        
        assertNull(contractedResult.expandedState);
        assertEquals(contractedResult.contractedState.getAffectedExpandedIds(), contractedInstance.contractedState.getAffectedExpandedIds());
        assertEquals(contractedResult.contractedState.getExpandedIds(), contractedInstance.contractedState.getExpandedIds());
        assertEquals(contractedResult.contractedState.getMean(), contractedInstance.contractedState.getMean());
        assertEquals(contractedResult.contractedState.getConstituentNodeStore(), contractedInstance.contractedState.getConstituentNodeStore());
        
        final CompositeNodeState expandedResult = CompositeNodeState.createFromString(expandedString);
        
        assertNull(expandedResult.contractedState);
        assertEquals(expandedResult.expandedState.getCompositeId(), expandedInstance.expandedState.getCompositeId());
        assertEquals(expandedResult.expandedState.getNumberOfNodes(), expandedInstance.expandedState.getNumberOfNodes());
        assertEquals(expandedResult.expandedState.isAffectingNode(), expandedInstance.expandedState.isAffectingNode());
        assertEquals(expandedResult.expandedState.getCompositeNodeStore(), expandedInstance.expandedState.getCompositeNodeStore());
    }   
}
