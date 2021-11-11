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
package au.gov.asd.tac.constellation.utilities.clipboard;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class ConstellationClipboardOwnerNGTest {
    
    public ConstellationClipboardOwnerNGTest() {
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
     * Test of lostOwnership method, of class ConstellationClipboardOwner.
     */
    @Test
    public void testLostOwnership() {
        System.out.println("lostOwnership");

        final Clipboard clipboard = ConstellationClipboardOwner.getConstellationClipboard();
        final ClipboardOwner instance = ConstellationClipboardOwner.getOwner();
        
        final Transferable mockContent = mock(Transferable.class);
        
        clipboard.setContents(mockContent, instance);
        assertEquals(clipboard.getContents(null), mockContent);
        
        instance.lostOwnership(null, null);
        
        assertNull(clipboard.getContents(null));   
    }
    
}
