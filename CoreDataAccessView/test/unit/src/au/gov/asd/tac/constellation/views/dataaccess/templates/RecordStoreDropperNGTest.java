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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.processing.RecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import static au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreDropper.RECORD_STORE_FLAVOR;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreDropper.RecordStoreDropperToGraphPlugin;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

/**
 * RecordStoreDropper Test
 *
 * @author arcturus
 */
public class RecordStoreDropperNGTest {

    /**
     * Test of drop method, of class RecordStoreDropper.
     *
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    @Test
    public void testDrop() throws UnsupportedFlavorException, IOException {
        System.out.println("drop");

        final DropTargetDropEvent dtde = mock(DropTargetDropEvent.class);
        final Transferable transferable = mock(Transferable.class);

        when(dtde.getTransferable()).thenReturn(transferable);
        when(transferable.isDataFlavorSupported(RECORD_STORE_FLAVOR)).thenReturn(true);
        when(transferable.getTransferData(RECORD_STORE_FLAVOR)).thenReturn(getRecordStoreAsStream());

        final RecordStoreDropper instance = new RecordStoreDropper();
        final BiConsumer expResult = null;
        final BiConsumer result = instance.drop(dtde);

        // TODO: would like to be able to test more than not null
        assertNotEquals(result, expResult);
    }

    @Test
    public void testRecordStoreDropperToGraphPlugin() throws InterruptedException, PluginException {
        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);

        final StoreGraph graph = new StoreGraph(new AnalyticSchemaFactory().createSchema());
        VisualConcept.VertexAttribute.X.ensure(graph);
        VisualConcept.VertexAttribute.Y.ensure(graph);
        VisualConcept.VertexAttribute.Z.ensure(graph);

        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "foo");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "bar");

        final RecordStoreDropperToGraphPlugin plugin = new RecordStoreDropperToGraphPlugin(recordStore);
        final RecordStore expResult = plugin.query(recordStore, interaction, parameters);

        assertEquals(recordStore, expResult);
    }

    private InputStream getRecordStoreAsStream() throws IOException {
        final RecordStore recordStore = new GraphRecordStore();
        recordStore.add();
        recordStore.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, "foo");
        recordStore.set(GraphRecordStoreUtilities.DESTINATION + VisualConcept.VertexAttribute.IDENTIFIER, "bar");

        final String RecordStoreAsJson = RecordStoreUtilities.toJson(recordStore);

        return new ByteArrayInputStream(("RecordStore=" + RecordStoreAsJson).getBytes());
    }
}
