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
package au.gov.asd.tac.constellation.graph.interaction.gui;

import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import java.io.File;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
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
 * @author Quasar985
 */
public class VisualGraphOpenerNGTest {

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
     * Test of openGraph method, of class VisualGraphOpener.
     */
    @Test
    public void testOpenGraph_GraphDataObject() {
        System.out.println("openGraph");
        // Set up mocks
        final GraphDataObject mockGdo = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);
        final File mockFile = mock(File.class);
        final String path = "mocked path";
        final Long lastModified = 123L;

        when(mockGdo.getPrimaryFile()).thenReturn(mockFileObject);
        when(mockFile.getPath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        // Check mocks work
        assertEquals(mockGdo.getPrimaryFile(), mockFileObject);
        assertEquals(mockFile.getPath(), path);
        assertEquals((Long) mockFile.lastModified(), lastModified);

        try (MockedStatic<FileUtil> mockFileUtil = Mockito.mockStatic(FileUtil.class, Mockito.CALLS_REAL_METHODS)) {
            // Set up FileUtil mock
            mockFileUtil.when(() -> FileUtil.toFile(mockFileObject)).thenReturn(mockFile);
            assertEquals(FileUtil.toFile(mockFileObject), mockFile);

            final VisualGraphOpener instance = new VisualGraphOpener();
            instance.openGraph(mockGdo);

            // Assert that the path was added to the list, 
            // The list shouldn't be empty as the mockGdo wouldn't allow the GraphFileOpener to finsh and remove it
            assertFalse(VisualGraphOpener.getOpeningGraphs().isEmpty());
            assertTrue(VisualGraphOpener.getOpeningGraphs().contains(path));
        }
    }
}
