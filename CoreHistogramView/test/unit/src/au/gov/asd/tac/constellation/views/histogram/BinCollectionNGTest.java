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
package au.gov.asd.tac.constellation.views.histogram;

import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class BinCollectionNGTest {

    public BinCollectionNGTest() {
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
     * Test of getSelectedBins method, of class BinCollection.
     */
    @Test
    public void testGetSelectedBins() {
        System.out.println("getSelectedBins");

        final Bin bin0 = mock(Bin.class);
        final Bin bin1 = mock(Bin.class);
        final Bin bin2 = mock(Bin.class);
        bin0.selectedCount = 5;

        final Bin[] bins = new Bin[3];
        bins[0] = bin0;
        bins[1] = bin1;
        bins[2] = bin2;

        final BinCollection instance = mock(BinCollection.class, Mockito.CALLS_REAL_METHODS);
        when(instance.getBins()).thenReturn(bins);

        final Bin[] expResult = new Bin[1];
        expResult[0] = bin0;
        final Bin[] result = instance.getSelectedBins();

        // Check that one bin was selected, and that was returned.
        assertEquals(expResult[0],  result[0]);

        bin0.selectedCount = 0;

        final Bin[] bins2 = new Bin[3];
        bins2[0] = bin0;
        bins2[1] = bin1;
        bins2[2] = bin2;

        when(instance.getBins()).thenReturn(bins2);

        final Bin[] expResult2 = new Bin[0];
        final Bin[] result2 = instance.getSelectedBins();

        // Check that no bins are selected
        assertEquals(expResult2.length,  result2.length);
    }
}