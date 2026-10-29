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
import au.gov.asd.tac.constellation.graph.visual.dragdrop.GraphDropper;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDropEvent;
import java.io.IOException;
import java.util.function.BiConsumer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author andromeda
 */
public class RecordStoreJsonDropperNGTest {

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

}
