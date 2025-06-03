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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.templates.TSVDropper.TSVDropperToGraphPlugin;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import org.testng.annotations.Test;

/**
 * TSV Dropper Test
 *
 * @author arcturus
 */
public class TSVDropperNGTest {

    /**
     * Test of drop method, of class TSVDropper.
     *
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    @Test
    public void dropWithValidTsv() throws UnsupportedFlavorException, IOException {
        System.out.println("dropWithValidTsv");

        final DropTargetDropEvent dtde = mock(DropTargetDropEvent.class);
        final Transferable transferable = mock(Transferable.class);

        when(dtde.getTransferable()).thenReturn(transferable);
        when(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)).thenReturn(true);

        final File file = new File(TSVDropperNGTest.class.getResource("./resources/sample.tsv").getFile());
        final List<File> data = new ArrayList<>();
        data.add(file);
        when(transferable.getTransferData(DataFlavor.javaFileListFlavor)).thenReturn(data);

        final BiConsumer expResult = null;
        final TSVDropper instance = new TSVDropper();
        final BiConsumer result = instance.drop(dtde);

        // TODO: would like to be able to test more than not null
        assertNotEquals(result, expResult);
    }

    /**
     * Test of drop method, of class TSVDropper.
     *
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    @Test
    public void dropWithValidTsvCompressed() throws UnsupportedFlavorException, IOException {
        System.out.println("dropWithValidTsvCompressed");

        final DropTargetDropEvent dtde = mock(DropTargetDropEvent.class);
        final Transferable transferable = mock(Transferable.class);

        when(dtde.getTransferable()).thenReturn(transferable);
        when(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)).thenReturn(true);

        final File file = new File(TSVDropperNGTest.class.getResource("./resources/sample.tsv.gz").getFile());
        final List<File> data = new ArrayList<>();
        data.add(file);
        when(transferable.getTransferData(DataFlavor.javaFileListFlavor)).thenReturn(data);

        final BiConsumer expResult = null;
        final TSVDropper instance = new TSVDropper();
        final BiConsumer result = instance.drop(dtde);

        // TODO: would like to be able to test more than not null
        assertNotEquals(result, expResult);
    }

    /**
     * Test of drop method, of class TSVDropper.
     *
     * @throws java.awt.datatransfer.UnsupportedFlavorException
     * @throws java.io.IOException
     */
    @Test
    public void dropWithInvalidDataFlavour() throws UnsupportedFlavorException, IOException {
        System.out.println("dropWithInvalidDataFlavour");

        final DropTargetDropEvent dtde = mock(DropTargetDropEvent.class);
        final Transferable transferable = mock(Transferable.class);

        when(dtde.getTransferable()).thenReturn(transferable);
        when(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)).thenReturn(false);

        final BiConsumer expResult = null;
        final TSVDropper instance = new TSVDropper();
        final BiConsumer result = instance.drop(dtde);

        assertEquals(result, expResult);
    }

    @Test
    public void testTSVDropperToGraphPlugin() throws InterruptedException, PluginException {
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

        final File file = new File(TSVDropperNGTest.class.getResource("./resources/sample.tsv").getFile());
        final List<File> files = new ArrayList<>();
        files.add(file);

        final TSVDropperToGraphPlugin plugin = new TSVDropperToGraphPlugin(recordStore, files);
        final RecordStore expResult = plugin.query(recordStore, interaction, parameters);

        assertEquals(recordStore, expResult);
    }
}
