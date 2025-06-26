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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.GraphObjectUtilities;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class CloseActionNGTest {
    
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
     * Test of actionPerformed method, of class CloseAction.
     */
    @Test
    public void testActionPerformed() {
//        System.out.println("actionPerformed");
//        
//        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
//        final DualGraph graph = new DualGraph(schema);
//        
//        final GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject("closeTestGraph", true);
//        final VisualGraphTopComponent tc = spy(new VisualGraphTopComponent(gdo, graph));
//        final GraphNode graphNode = new GraphNode(graph, gdo, tc, null);
//        
//        final CloseAction instance = new CloseAction(graphNode);
//        try {
//            instance.actionPerformed(null);
//            // ideally we check whether the top component was actually closed but since its a challenge to have it open in the first place
//            // this is the next best thing (since this is what we expect to be called in order to successfully close the graph)
//            verify(tc).close();
//        } finally {
//            graphNode.destroy();
//        }
    } 
}
