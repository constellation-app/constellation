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
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class VisualGraphOpenerNGTest {

    private static final String FILE_NAME = "dummy.star";

    @BeforeClass
    public static void setUpClass() throws Exception {
        System.setProperty("java.awt.headless", "true");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        System.clearProperty("java.awt.headless");
    }

    /**
     * Test of openGraph method, of class VisualGraphOpener.
     */
    @Test
    public void testOpenGraph() {
        System.out.println("openGraph");

        // Set up mocks
        final GraphDataObject mockGdo = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);
        final File mockFile = mock(File.class);
        final String path = "mocked path";
        final Path mockPath = mock(Path.class);
        final Long lastModified = 123L;

        when(mockGdo.getPrimaryFile()).thenReturn(mockFileObject);
        when(mockFileObject.getPath()).thenReturn(path);
        when(mockFile.getAbsolutePath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        // Check mocks work
        assertEquals(mockGdo.getPrimaryFile(), mockFileObject);
        assertEquals(mockFile.getAbsolutePath(), path);
        assertEquals((Long) mockFile.lastModified(), lastModified);

        try (final MockedStatic<FileUtil> mockFileUtil = Mockito.mockStatic(FileUtil.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Paths> mockPaths = Mockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS)) {
            // Set up FileUtil mock
            mockFileUtil.when(() -> FileUtil.toFile(mockFileObject)).thenReturn(mockFile);
            assertEquals(FileUtil.toFile(mockFileObject), mockFile);
            // Set up Paths mock
            mockPaths.when(() -> Paths.get(path)).thenReturn(mockPath);
            assertEquals(Paths.get(path), mockPath);

            final VisualGraphOpener instance = new VisualGraphOpener();
            instance.openGraph(mockGdo);

            // Assert that the path was added to the list, 
            // The list shouldn't be empty at the moment
            assertFalse(VisualGraphOpener.getOpeningGraphs().isEmpty());
            assertTrue(VisualGraphOpener.getOpeningGraphs().contains(mockPath));

            // Open again
            instance.openGraph(mockGdo);
            // Assert path still in list
            assertFalse(VisualGraphOpener.getOpeningGraphs().isEmpty());
            assertTrue(VisualGraphOpener.getOpeningGraphs().contains(mockPath));
        }
    }

    /**
     * Test of openGraph method, of class VisualGraphOpener.
     */
    @Test
    public void testOpenGraphDummyFile() {
        System.out.println("openGraphDummyFile");

        // Set up mocks
        final GraphDataObject mockGdo = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);

        // Dummy file
        final File dummyfile = new File(FILE_NAME);
        final Path expectedPath = Paths.get(dummyfile.getAbsolutePath());

        when(mockGdo.getPrimaryFile()).thenReturn(mockFileObject);
        assertEquals(mockGdo.getPrimaryFile(), mockFileObject);

        try (final MockedStatic<FileUtil> mockFileUtil = Mockito.mockStatic(FileUtil.class, Mockito.CALLS_REAL_METHODS)) {
            // Set up FileUtil mock
            mockFileUtil.when(() -> FileUtil.toFile(mockFileObject)).thenReturn(dummyfile);
            assertEquals(FileUtil.toFile(mockFileObject), dummyfile);

            final VisualGraphOpener instance = new VisualGraphOpener();
            instance.openGraph(mockGdo);

            // Assert that the path was added to the list, 
            // The list shouldn't be empty at the moment
            assertFalse(VisualGraphOpener.getOpeningGraphs().isEmpty());
            assertTrue(VisualGraphOpener.getOpeningGraphs().contains(expectedPath));

            // Open again
            instance.openGraph(mockGdo);
            // Assert path still in list
            assertFalse(VisualGraphOpener.getOpeningGraphs().isEmpty());
            assertTrue(VisualGraphOpener.getOpeningGraphs().contains(expectedPath));
        }
    }
}
