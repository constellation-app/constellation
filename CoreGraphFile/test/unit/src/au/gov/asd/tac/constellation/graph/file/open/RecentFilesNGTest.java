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
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.graph.file.open.RecentFiles.HistoryItem;
import java.util.Arrays;
import java.util.List;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileObject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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
public class RecentFilesNGTest {
    
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
     * Test of getUniqueRecentFiles method, of class RecentFiles.
     */
    @Test
    public void testGetUniqueRecentFiles() {
        System.out.println("getUniqueRecentFiles");
        
        final HistoryItem h1 = mock(HistoryItem.class);
        when(h1.getPath()).thenReturn("test/path");
        final HistoryItem h2 = mock(HistoryItem.class);
        when(h2.getPath()).thenReturn("test/fail/path");
        final HistoryItem h3 = mock(HistoryItem.class);
        when(h3.getPath()).thenReturn("another/test/path");
        
        final List<HistoryItem> mockRecentFiles = Arrays.asList(h1, h2, h3, h1);
        try (final MockedStatic<RecentFiles> recentFilesMockedStatic = 
                Mockito.mockStatic(RecentFiles.class)) {
            recentFilesMockedStatic.when(RecentFiles::getRecentFiles).thenReturn(mockRecentFiles);
                   
            final FileObject fo1 = mock(FileObject.class);
            final FileObject fo2 = mock(FileObject.class);

            recentFilesMockedStatic.when(() -> RecentFiles.convertPath2File("test/path"))
                    .thenReturn(fo1);
            recentFilesMockedStatic.when(() -> RecentFiles.convertPath2File("test/fail/path"))
                    .thenReturn(null);
            recentFilesMockedStatic.when(() -> RecentFiles.convertPath2File("another/test/path"))
                    .thenReturn(fo2);
            
            recentFilesMockedStatic.when(RecentFiles::getUniqueRecentFiles).thenCallRealMethod();
            recentFilesMockedStatic.when(RecentFiles::decrementHistoryReadyLatch).thenCallRealMethod();
            RecentFiles.decrementHistoryReadyLatch();

            final List<HistoryItem> result = RecentFiles.getUniqueRecentFiles();
            assertTrue(result.contains(h1));
            assertFalse(result.contains(h2));
            assertTrue(result.contains(h3));
            assertEquals(result.size(), 2);
        }
    }
}
