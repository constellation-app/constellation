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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.views.attributeeditor.AttributePrototype;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
public class CreateAttributeEditOperationNGTest {

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
     * Test of performEdit method, of class CreateAttributeEditOperation.
     */
    @Test
    public void testPerformEdit() {
        System.out.println("performEdit");
        
//        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
//        final StoreGraph graph = new StoreGraph(schema);
//        
//        assertEquals(graph.getAttribute(GraphElementType.VERTEX, "NewIdentifier"), Graph.NOT_FOUND);
//        
//        final CreateAttributeEditOperation instance = new CreateAttributeEditOperation();
//        final AttributePrototype newAttributeValues = new AttributePrototype("NewIdentifier", "new description", GraphElementType.VERTEX, "string", "test");
//        final DualGraph dualGraph = new DualGraph(schema, graph);
//        
//        try (final MockedStatic<GraphManager> graphManagerMockedStatic = Mockito.mockStatic(GraphManager.class)) {
//            final GraphManager mockManager = mock(GraphManager.class);
//            when(mockManager.getActiveGraph()).thenReturn(dualGraph);
//            
//            graphManagerMockedStatic.when(() -> GraphManager.getDefault()).thenReturn(mockManager);
//            
//            instance.performEdit(newAttributeValues);
//        }
//        
//        assertNotEquals(graph.getAttribute(GraphElementType.VERTEX, "NewIdentifier"), Graph.NOT_FOUND);
    }
}
