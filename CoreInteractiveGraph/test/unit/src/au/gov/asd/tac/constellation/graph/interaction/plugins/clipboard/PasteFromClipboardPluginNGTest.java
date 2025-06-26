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
package au.gov.asd.tac.constellation.graph.interaction.plugins.clipboard;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import java.awt.datatransfer.Clipboard;
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
public class PasteFromClipboardPluginNGTest {
    
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
     * Test of edit method, of class PasteFromClipboardPlugin.
     * 
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testEdit() throws InterruptedException, PluginException {
        System.out.println("edit");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final RecordStore rs = new GraphRecordStore();
        rs.add();
        rs.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "node1");
        rs.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "node2");
        rs.set(GraphRecordStoreUtilities.TRANSACTION + VisualConcept.TransactionAttribute.IDENTIFIER, "transaction12");
        
        final Clipboard cb = ConstellationClipboardOwner.getConstellationClipboard();
        cb.setContents(new RecordStoreTransferable(rs), null);
        
        assertEquals(graph.getVertexCount(), 0);
        assertEquals(graph.getTransactionCount(), 0);
        
        int identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        int identifierTransactionAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
        
        assertEquals(identifierVertexAttribute, Graph.NOT_FOUND);
        assertEquals(identifierTransactionAttribute, Graph.NOT_FOUND);
        
        final PasteFromClipboardPlugin instance = new PasteFromClipboardPlugin();
        instance.edit(graph, null, null);
        
        assertEquals(graph.getVertexCount(), 2);
        assertEquals(graph.getTransactionCount(), 1);
        
        identifierVertexAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        identifierTransactionAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.get(graph);
        
        assertNotEquals(identifierVertexAttribute, Graph.NOT_FOUND);
        assertNotEquals(identifierTransactionAttribute, Graph.NOT_FOUND);
        
        assertEquals(graph.getStringValue(identifierVertexAttribute, 0), "node1");
        assertEquals(graph.getStringValue(identifierVertexAttribute, 1), "node2");
        assertEquals(graph.getStringValue(identifierTransactionAttribute, 0), "transaction12");
        
        // cleanup the clipboard at the end of the test
        cb.setContents(null, null);
    }
}
