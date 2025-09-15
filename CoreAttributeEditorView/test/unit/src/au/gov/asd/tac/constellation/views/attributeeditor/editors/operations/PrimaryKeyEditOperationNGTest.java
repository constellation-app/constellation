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
package au.gov.asd.tac.constellation.views.attributeeditor.editors.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class PrimaryKeyEditOperationNGTest {

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
     * Test of performEdit method, of class PrimaryKeyEditOperation.
     */
    @Test
    public void testPerformEdit() {
        System.out.println("performEdit");
        
//        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
//        final StoreGraph graph = new StoreGraph(schema);
//        
//        final int identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
//        final int identifierColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
//        final int identifierDimmedAttribute = VisualConcept.VertexAttribute.DIMMED.ensure(graph);
//        
//        schema.newGraph(graph);
//        
//        final int[] originalKeys = graph.getPrimaryKey(GraphElementType.VERTEX);
//        assertEquals(originalKeys.length, 1);
//        // since there is only one, we can be confident as to what is in the first (and only) entry
//        assertEquals(originalKeys[0], identifierVertexAttribute);
//        
//        final PrimaryKeyEditOperation instance = new PrimaryKeyEditOperation(GraphElementType.VERTEX);
//        final List<String> keys = Arrays.asList(VisualConcept.VertexAttribute.COLOR.getName(), VisualConcept.VertexAttribute.DIMMED.getName());
//        final DualGraph dualGraph = new DualGraph(schema, graph);
//        
//        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class)) {
//            final GraphManager mockManager = mock(GraphManager.class);
//            when(mockManager.getActiveGraph()).thenReturn(dualGraph);
//            
//            graphManagerMockedStatic.when(() -> GraphManager.getDefault()).thenReturn(mockManager);
//            
//            instance.performEdit(keys);
//        }
//        
//        final int[] newKeys = graph.getPrimaryKey(GraphElementType.VERTEX);
//        assertEquals(newKeys.length, 2);
//        
//        final IntStream newKeysStream = IntStream.of(newKeys);
//        assertFalse(newKeysStream.anyMatch(x -> x == identifierVertexAttribute));
//        assertTrue(newKeysStream.anyMatch(x -> x == identifierColorAttribute));
//        assertTrue(newKeysStream.anyMatch(x -> x == identifierDimmedAttribute));
    }
}
