/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport.dragdrop;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.arrangements.ArrangementPluginRegistry;
import au.gov.asd.tac.constellation.plugins.importexport.dragdrop.RecordStoreJsonDropper.DragAndDropRecordStoreJSONPlugin;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.openide.awt.NotificationDisplayer;
import org.testng.Assert;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author andromeda
 */
public class RecordStoreJsonDropperNGTest {

    private static MockedStatic<PluginExecution> pluginExecutionStaticMock;
    private static PluginExecution pluginExecutionMock;

    @BeforeMethod
    public void setUpMethod() {
        pluginExecutionStaticMock = mockStatic(PluginExecution.class);
        pluginExecutionMock = mock(PluginExecution.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        pluginExecutionStaticMock.close();
    }

    @Test
    public void dropTest() throws UnsupportedFlavorException, IOException {
        final String jsonString = "JSON=[{\"source.Identifier\": \"Thing 1\",\"source.Type\": \"Person\"},{\"source.Identifier\": \"Thing 2\",\"source.Type\": \"Person\"},{\"source.Identifier\": \"Thing 1\",\"source.Type\": \"Person\",\"destination.Identifier\": \"Thing 2\",\"destination.Type\": \"Person\",\"transaction.Identifier\": \"Unique ID 1\",\"transaction.Type\": \"Communication\"},{\"source.Identifier\": \"Thing 2\",\"source.Type\": \"Person\",\"destination.Identifier\": \"Thing 1\",\"destination.Type\": \"Person\",\"transaction.Identifier\": \"Unique ID 2\",\"transaction.Type\": \"Communication\"}]";

        final DropTargetDropEvent dtdeMock = mock(DropTargetDropEvent.class);
        final Transferable transferableMock = mock(Transferable.class);
        when(transferableMock.isDataFlavorSupported(DataFlavor.stringFlavor)).thenReturn(Boolean.TRUE);
        when(transferableMock.getTransferData(DataFlavor.stringFlavor)).thenReturn(jsonString);
        when(dtdeMock.getTransferable()).thenReturn(transferableMock);

        final RecordStoreJsonDropper dropper = new RecordStoreJsonDropper();
        final BiConsumer<Graph, GraphDropper.DropInfo> drop = dropper.drop(dtdeMock);
        Assert.assertNotNull(drop);
    }

    @Test
    public void pluginEditTest() throws InterruptedException, PluginException {
        final String jsonString = "JSON=[{\"source.Identifier\": \"Thing 1\",\"source.Type\": \"Person\"},{\"source.Identifier\": \"Thing 2\",\"source.Type\": \"Person\"},{\"source.Identifier\": \"Thing 1\",\"source.Type\": \"Person\",\"destination.Identifier\": \"Thing 2\",\"destination.Type\": \"Person\",\"transaction.Identifier\": \"Unique ID 1\",\"transaction.Type\": \"Communication\"},{\"source.Identifier\": \"Thing 2\",\"source.Type\": \"Person\",\"destination.Identifier\": \"Thing 1\",\"destination.Type\": \"Person\",\"transaction.Identifier\": \"Unique ID 2\",\"transaction.Type\": \"Communication\"}]";

        final DragAndDropRecordStoreJSONPlugin plugin = new DragAndDropRecordStoreJSONPlugin(jsonString);
        assertTrue(plugin.getName().equals("Drag and Drop: JSON (RecordStore) to Graph"));
        final GraphWriteMethods gwmMock = mock(GraphWriteMethods.class);
        final GraphRecordStore rsMock = mock(GraphRecordStore.class);
        final PluginInteraction interactionMock = mock(PluginInteraction.class);
        final PluginParameters parameterssMock = mock(PluginParameters.class);
        final List<String> listMock = mock(List.class);

        try (final MockedStatic<GraphRecordStoreUtilities> graphRecordStoreUtilitiesMockedStatic = Mockito.mockStatic(GraphRecordStoreUtilities.class);
                final MockedStatic<ConstellationLoggerHelper> constellationLoggerHelperMockedStatic = Mockito.mockStatic(ConstellationLoggerHelper.class)) {

            pluginExecutionStaticMock.when(()
                    -> PluginExecution.withPlugin(ArrangementPluginRegistry.GRID_COMPOSITE)).thenReturn(pluginExecutionMock);
            graphRecordStoreUtilitiesMockedStatic.when(() -> GraphRecordStoreUtilities.fromRecordStoreJson(jsonString)).thenReturn(rsMock);
            final List<Integer> mockVertices = mock(List.class);
            when(rsMock.getAll(Mockito.any())).thenReturn(listMock);
            graphRecordStoreUtilitiesMockedStatic.when(() -> GraphRecordStoreUtilities.addRecordStoreToGraph(gwmMock, rsMock, true, true, null)).thenReturn(mockVertices);
            when(mockVertices.isEmpty()).thenReturn(Boolean.FALSE);

            plugin.edit(gwmMock, interactionMock, parameterssMock);
            constellationLoggerHelperMockedStatic.verify(() -> ConstellationLoggerHelper.importPropertyBuilder(plugin, listMock, null, ConstellationLoggerHelper.SUCCESS));
        }
    }

    @Test
    public void pluginEditExceptionTest() throws InterruptedException, PluginException {
        final String jsonString = "[{\"source.Identifier\": \"Thing 1\",\"source.Type\": \"Person\"},{\"source.Identifier\": \"Thing 2\",\"source.Type\": \"Person\"},{\"source.Identifier\": \"Thing 1\",\"source.Type\": \"Person\",\"destination.Identifier\": \"Thing 2\",\"destination.Type\": \"Person\",\"transaction.Identifier\": \"Unique ID 1\",\"transaction.Type\": \"Communication\"},{\"source.Identifier\": \"Thing 2\",\"source.Type\": \"Person\",\"destination.Identifier\": \"Thing 1\",\"destination.Type\": \"Person\",\"transaction.Identifier\": \"Unique ID 2\",\"transaction.Type\": \"Communication\"}]";

        final DragAndDropRecordStoreJSONPlugin plugin = new DragAndDropRecordStoreJSONPlugin(jsonString);
        final GraphWriteMethods gwmMock = mock(GraphWriteMethods.class);
        final PluginInteraction interactionMock = mock(PluginInteraction.class);
        final PluginParameters parameterssMock = mock(PluginParameters.class);
        try (final MockedStatic<GraphRecordStoreUtilities> graphRecordStoreUtilitiesMockedStatic = Mockito.mockStatic(GraphRecordStoreUtilities.class); final MockedStatic<NotificationDisplayer> notificationDisplayerMockedStatic = Mockito.mockStatic(NotificationDisplayer.class);) {
            graphRecordStoreUtilitiesMockedStatic.when(() -> GraphRecordStoreUtilities.fromRecordStoreJson(jsonString)).thenThrow(new ClassCastException());
            notificationDisplayerMockedStatic.when(() -> NotificationDisplayer.getDefault()).thenReturn(mock(NotificationDisplayer.class));
            plugin.edit(gwmMock, interactionMock, parameterssMock);
            notificationDisplayerMockedStatic.verify(() -> NotificationDisplayer.getDefault());            
        }
    }
}
