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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.clipboard.ConstellationClipboardOwner;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.BitSet;
import static org.testng.Assert.assertEquals;
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
public class GraphCopyUtilitiesNGTest {
    
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
     * Test of copySelectedGraphElementsToClipboard method, of class GraphCopyUtilities.
     * 
     * @throws java.lang.InterruptedException
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    @Test
    public void testCopySelectedGraphElementsToClipboard() throws InterruptedException, UnsupportedFlavorException, IOException {
        System.out.println("copySelectedGraphElementsToClipboard");
        
        final Schema schema = SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID).createSchema();
        final StoreGraph graph = new StoreGraph(schema);
        
        final int vxId1 = graph.addVertex();
        final int vxId2 = graph.addVertex();
        final int vxId3 = graph.addVertex();
        
        final int tId1 = graph.addTransaction(vxId1, vxId2, true);
        final int tId2 = graph.addTransaction(vxId1, vxId3, true);
        graph.addTransaction(vxId2, vxId3, true);
        
        final int selectedVertexAttribute = VisualConcept.VertexAttribute.SELECTED.ensure(graph);
        final int selectedTransactionAttribute = VisualConcept.TransactionAttribute.SELECTED.ensure(graph);
        
        graph.setBooleanValue(selectedVertexAttribute, vxId1, true);
        graph.setBooleanValue(selectedVertexAttribute, vxId2, true);
        
        graph.setBooleanValue(selectedTransactionAttribute, tId1, true);
        graph.setBooleanValue(selectedTransactionAttribute, tId2, true);
        
        final Clipboard cb = ConstellationClipboardOwner.getConstellationClipboard();
        Transferable content = cb.getContents(null);
        assertNull(content);
        
        // this is the expected value for both the node bit set and transaction one
        // as in both instances, the first 2 are selected (and the 3rd is not)
        final BitSet expected = new BitSet(3);
        expected.set(0, true);
        expected.set(1, true);
        
        final BitSet[] result = GraphCopyUtilities.copySelectedGraphElementsToClipboard(graph);
        
        assertEquals(result, new BitSet[]{expected, expected});
        
        content = cb.getContents(null);
        assertTrue(content instanceof RecordStoreTransferable);
        
        // cleanup the clipboard at the end of the test
        cb.setContents(null, null);
    }
}
