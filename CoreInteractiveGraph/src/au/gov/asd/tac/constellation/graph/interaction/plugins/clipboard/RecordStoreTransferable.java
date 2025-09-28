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
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author twinkle2_little
 */
public class RecordStoreTransferable implements Transferable {

    public static final DataFlavor RECORDSTORE_FLAVOR = new DataFlavor(Graph.class, "RecordStore");
    private static final DataFlavor[] SUPPORTED_FLAVORS = {
        RECORDSTORE_FLAVOR
    };
    private final RecordStore copy;

    public RecordStoreTransferable(final RecordStore copy) {
        this.copy = copy;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return Arrays.copyOf(SUPPORTED_FLAVORS, SUPPORTED_FLAVORS.length);
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return RECORDSTORE_FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (!RECORDSTORE_FLAVOR.equals(flavor)) {
            throw new UnsupportedFlavorException(flavor);
        }

        return copy;
    }
}
