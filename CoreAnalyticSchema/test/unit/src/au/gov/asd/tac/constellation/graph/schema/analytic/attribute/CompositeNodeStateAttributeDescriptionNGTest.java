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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.ContractedCompositeNodeState;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class CompositeNodeStateAttributeDescriptionNGTest {
    
    private final String compositeNodeString = "{\"nodeId\":1,\"expandedState\":null,\"contractedState\":{\"constituentNodeStore\":\"[{\\\"source.Identifier\\\":\\\"Vertex #0\\\",\\\"source.[id]\\\":\\\"copy.Identifier<Vertex #0>Type<Unknown>\\\"},{\\\"source.Identifier\\\":\\\"Vertex #1\\\",\\\"source.[id]\\\":\\\"copy.Identifier<Vertex #1>Type<Unknown>\\\"}]\",\"expandedIds\":[\"copy.Identifier<Vertex #0>Type<Unknown>\",\"copy.Identifier<Vertex #1>Type<Unknown>\"],\"affectedExpandedIds\":[\"copy.Identifier<Vertex #0>Type<Unknown>\",\"copy.Identifier<Vertex #1>Type<Unknown>\"],\"mean\":[0.0,1.0,0.5]}}";
    
    private CompositeNodeState state;
    
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
        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #0");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #0>Type<Unknown>");
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "Vertex #1");
        recordStore.set(GraphRecordStoreUtilities.SOURCE + GraphRecordStoreUtilities.ID, "copy.Identifier<Vertex #1>Type<Unknown>");
        
        final List<String> expandedIds = Arrays.asList("copy.Identifier<Vertex #0>Type<Unknown>", "copy.Identifier<Vertex #1>Type<Unknown>");
        
        final ContractedCompositeNodeState contractedState = new ContractedCompositeNodeState(recordStore, expandedIds, expandedIds, new float[]{0.0F ,1.0F, 0.5F});
        state = new CompositeNodeState(1, contractedState);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of convertFromString method, of class CompositeNodeStateAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");
        
        final CompositeNodeStateAttributeDescription instance = new CompositeNodeStateAttributeDescription();
        assertNull(instance.convertFromString(""));
        
        final CompositeNodeState resultState = instance.convertFromString(compositeNodeString);
        assertNull(resultState.expandedState);
        assertEquals(resultState.contractedState.getAffectedExpandedIds(), state.contractedState.getAffectedExpandedIds());
        assertEquals(resultState.contractedState.getExpandedIds(), state.contractedState.getExpandedIds());
        assertEquals(resultState.contractedState.getMean(), state.contractedState.getMean());
        assertEquals(resultState.contractedState.getConstituentNodeStore(), state.contractedState.getConstituentNodeStore());
    }

    /**
     * Test of getString method, of class CompositeNodeStateAttributeDescription.
     */
    @Test
    public void testGetString() {
        System.out.println("getString");
        
        final CompositeNodeStateAttributeDescription instance = new CompositeNodeStateAttributeDescription();
        instance.setCapacity(1);
        instance.setObject(0, state);
        
        assertEquals(instance.getString(0), compositeNodeString);
    }   
}
