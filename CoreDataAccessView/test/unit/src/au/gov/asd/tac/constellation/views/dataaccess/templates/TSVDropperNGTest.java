/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * TSV Dropper Test
 *
 * @author arcturus
 */
public class TSVDropperNGTest {

    public TSVDropperNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of drop method, of class TSVDropper.
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

}
