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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.interaction.plugins.io.SaveAsAction;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import static au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode.LINK;
import au.gov.asd.tac.constellation.graph.visual.framework.VisualGraphDefaults;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import java.net.URI;
import java.nio.file.Files;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class VisualGraphTopComponentNGTest {

    private static final Logger LOGGER = Logger.getLogger(VisualGraphTopComponentNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            if (!FxToolkit.isFXApplicationThreadRunning()) {
                FxToolkit.registerPrimaryStage();
            }
        } catch (TimeoutException e) {
            System.out.println("\n**** SETUP ERROR: " + e);
            throw e;
        }

        System.setProperty("java.awt.headless", "true");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        } catch (Exception e) {
            if (e.toString().contains("HeadlessException")) {
                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
            } else {
                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
                throw e;
            }
        }

        System.clearProperty("java.awt.headless");
    }

    @Test
    public void testConstructor() throws Exception {
        System.out.println("testConstructor");

        // Mock variables
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);

        when(mockGDO.isInMemory()).thenReturn(false);
        when(mockGDO.isValid()).thenReturn(true);
        when(mockGDO.getPrimaryFile()).thenReturn(mockFileObject);

        when(mockGDO.createFromTemplate(any(), anyString())).thenReturn(mockGDO);

        when(mockFileObject.getPath()).thenReturn("");

        // Assert constructed correctely
        final VisualGraphTopComponent instance = new VisualGraphTopComponent();
        instance.getGraphNode().setDataObject(mockGDO);

        assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
        assertNotNull(instance);
        assertNotNull(instance.getUndoRedo());
    }

    @Test
    public void testConstructorWithParams() throws Exception {
        System.out.println("testConstructorWithParams");

        // Mock variables
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);
        final File mockFile = mock(File.class);
        final DualGraph dgSpy = spy(new DualGraph(null));
        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);
        final String path = "mocked path";
        final Long lastModified = 123L;

        when(mockGDO.isInMemory()).thenReturn(false);
        when(mockGDO.isValid()).thenReturn(true);
        when(mockGDO.getPrimaryFile()).thenReturn(mockFileObject);

        when(mockGDO.createFromTemplate(any(), anyString())).thenReturn(mockGDO);

        when(mockFileObject.getPath()).thenReturn("");

        when(mockFile.getPath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        final int connectionMode = 1;
        // Hard coded to match actual values
        final int graphDecorators = 16;
        final int graphTopLabels = 7;
        System.out.println("graphDecorators: " + graphDecorators + " graphTopLabels: " + graphTopLabels);
        final int graphNotFound = -1107;

        when(mockReadableGraph.getObjectValue(connectionMode, 0)).thenReturn(null);
        when(mockReadableGraph.getObjectValue(graphDecorators, 0)).thenReturn(VisualGraphDefaults.DEFAULT_DECORATORS);
        when(mockReadableGraph.getObjectValue(graphTopLabels, 0)).thenReturn(VisualGraphDefaults.DEFAULT_TOP_LABELS);

        when(mockReadableGraph.getAttribute(GraphElementType.GRAPH, "connection_mode")).thenReturn(connectionMode);
        when(mockReadableGraph.getAttribute(GraphElementType.GRAPH, "draw_flags")).thenReturn(graphNotFound);

        // Assert constructed correctely
        final VisualGraphTopComponent instance = new VisualGraphTopComponent(mockGDO, dgSpy);
        instance.getGraphNode().setDataObject(mockGDO);
        assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
        assertNotNull(instance);
        assertNotNull(instance.getUndoRedo());
    }

    /**
     * Test of saveGraph method, of class VisualGraphTopComponent.
     *
     * @throws Exception
     */
    @Test
    public void testSaveGraphNotInMemory() throws Exception {
        System.out.println("saveGraph not in memory and valid");

        // Mock variables
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);
        final File mockFile = mock(File.class);
        final DualGraph dgSpy = spy(new DualGraph(null));
        final String path = "mocked path";
        final Long lastModified = 123L;

        final DataFolder mockFolder = mock(DataFolder.class);
        final FileObject mockFolderFileObject = mock(FileObject.class);
        final URI mockURI = mock(URI.class);
        final Path mockFolderPath = mock(Path.class);
        final String mockFolderPathString = "mockFolderPathString";

        when(mockGDO.isInMemory()).thenReturn(false);
        when(mockGDO.isValid()).thenReturn(true);
        when(mockGDO.getPrimaryFile()).thenReturn(mockFileObject);
        when(mockGDO.createFromTemplate(any(), anyString())).thenReturn(mockGDO);

        when(mockFileObject.getPath()).thenReturn("");
        when(mockFile.getPath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        when(mockFolder.getPrimaryFile()).thenReturn(mockFolderFileObject);
        when(mockFolderFileObject.toURI()).thenReturn(mockURI);
        when(mockGDO.getFolder()).thenReturn(mockFolder);
        when(mockFolderPath.toString()).thenReturn(mockFolderPathString);

        try (final MockedStatic<Paths> mockPaths = Mockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            // Set up paths mock
            mockPaths.when(() -> Paths.get(mockURI)).thenReturn(mockFolderPath);
            mockPaths.when(() -> Paths.get(anyString(), anyString())).thenReturn(mockFolderPath);
            // Set up Files mock
            mockFiles.when(() -> Files.exists(mockFolderPath)).thenReturn(false);

            final VisualGraphTopComponent instance = new VisualGraphTopComponent(mockGDO, dgSpy);
            instance.getGraphNode().setDataObject(mockGDO);
            instance.saveGraph();

            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);

            verify(mockGDO).isValid();
            verify(mockGDO).isInMemory();
            verify(mockGDO, times(2)).getName();
            verify(mockGDO, times(4)).getPrimaryFile();
        }
    }

    /**
     * Test of saveGraph method, of class VisualGraphTopComponent.
     *
     * @throws Exception
     */
    @Test
    public void testSaveGraphInvalid() throws Exception {
        System.out.println("saveGraph invalid");

        // Mock variables
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);

        final DualGraph dgSpy = spy(new DualGraph(null));

        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);

        final File mockFile = mock(File.class);
        final String path = "mocked path";
        final Long lastModified = 123L;

        when(mockGDO.isValid()).thenReturn(false);
        when(mockGDO.getPrimaryFile()).thenReturn(mockFileObject);

        when(mockFileObject.getPath()).thenReturn("");
        when(mockFile.getPath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        final int connectionMode = 1;
        final int graphNotFound = -1107;
        when(mockReadableGraph.getObjectValue(connectionMode, 0)).thenReturn(LINK);
        when(mockReadableGraph.getAttribute(GraphElementType.GRAPH, "connection_mode")).thenReturn(connectionMode);
        when(mockReadableGraph.getAttribute(GraphElementType.GRAPH, "draw_flags")).thenReturn(graphNotFound);

        when(mockGDO.createFromTemplate(any(), anyString())).thenReturn(mockGDO);

        // Mock contruct save as action, GraphNode
        try (MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class)) {

            VisualGraphTopComponent instance = new VisualGraphTopComponent(mockGDO, dgSpy);
            instance.getGraphNode().setDataObject(mockGDO);
            instance.saveGraph();

            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
            assertEquals(mockSaveAsAction.constructed().size(), 1);
            verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
            verify(mockSaveAsAction.constructed().get(0)).isSaved();

            verify(mockGDO).isValid();
            verify(mockGDO).getName();
        }
    }

    /**
     * Test of saveGraph method, of class VisualGraphTopComponent.
     *
     * @throws Exception
     */
    @Test
    public void testSaveGraphInMemory() throws Exception {
        System.out.println("testSaveGraphInMemory");

        // Mock variables
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);

        final DualGraph dgSpy = spy(new DualGraph(null));

        final ReadableGraph mockReadableGraph = mock(ReadableGraph.class);

        final File mockFile = mock(File.class);
        final String path = "mocked path";
        final Long lastModified = 123L;

        when(mockGDO.isInMemory()).thenReturn(true);
        when(mockGDO.isValid()).thenReturn(true);
        when(mockGDO.getPrimaryFile()).thenReturn(mockFileObject);
        when(mockGDO.createFromTemplate(any(), anyString())).thenReturn(mockGDO);

        when(mockFileObject.getPath()).thenReturn("");
        when(mockFile.getPath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        final int connectionMode = 1;
        final int graphNotFound = -1107;
        when(mockReadableGraph.getObjectValue(connectionMode, 0)).thenReturn(ConnectionMode.TRANSACTION);
        when(mockReadableGraph.getAttribute(GraphElementType.GRAPH, "connection_mode")).thenReturn(connectionMode);
        when(mockReadableGraph.getAttribute(GraphElementType.GRAPH, "draw_flags")).thenReturn(graphNotFound);

        // Mock contruct save as action, GraphNode
        try (final MockedConstruction<SaveAsAction> mockSaveAsAction = Mockito.mockConstruction(SaveAsAction.class)) {

            final VisualGraphTopComponent instance = new VisualGraphTopComponent(mockGDO, dgSpy);

            instance.getGraphNode().setDataObject(mockGDO);
            instance.saveGraph();

            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);
            assertEquals(mockSaveAsAction.constructed().size(), 1);
            verify(mockSaveAsAction.constructed().get(0)).actionPerformed(null);
            verify(mockSaveAsAction.constructed().get(0)).isSaved();

            verify(mockGDO).isValid();
            verify(mockGDO).getName();
        }
    }

    @Test
    public void testRequestActiveWithLatch() throws Exception {
        System.out.println("testRequestActiveWithLatch");

        // Mock variables
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);
        final DualGraph dgSpy = spy(new DualGraph(null));
        final File mockFile = mock(File.class);
        final String path = "mocked path";
        final Long lastModified = 123L;

        when(mockGDO.isInMemory()).thenReturn(false);
        when(mockGDO.isValid()).thenReturn(true);
        when(mockGDO.getPrimaryFile()).thenReturn(mockFileObject);
        when(mockGDO.createFromTemplate(any(), anyString())).thenReturn(mockGDO);

        when(mockFileObject.getPath()).thenReturn("");
        when(mockFile.getPath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        final VisualGraphTopComponent instance = new VisualGraphTopComponent(mockGDO, dgSpy);

        instance.requestActiveWithLatch(null);
    }

    /**
     * Test of saveGraph method when temp file already exists, of class VisualGraphTopComponent.
     *
     * @throws Exception
     */
    @Test
    public void testSaveGraphFileExists() throws Exception {
        System.out.println("SaveGraph file already exists");

        // Mock variables
        final GraphDataObject mockGDO = mock(GraphDataObject.class);
        final FileObject mockFileObject = mock(FileObject.class);
        final File mockFile = mock(File.class);
        final DualGraph dgSpy = spy(new DualGraph(null));
        final String path = "mocked path";
        final Long lastModified = 123L;

        final DataFolder mockFolder = mock(DataFolder.class);
        final FileObject mockFolderFileObject = mock(FileObject.class);
        final URI mockURI = mock(URI.class);
        final Path mockFolderPath = mock(Path.class);
        final String mockFolderPathString = "mockFolderPathString";

        when(mockGDO.isInMemory()).thenReturn(false);
        when(mockGDO.isValid()).thenReturn(true);
        when(mockGDO.getPrimaryFile()).thenReturn(mockFileObject);
        when(mockGDO.createFromTemplate(any(), anyString())).thenReturn(mockGDO);

        when(mockFileObject.getPath()).thenReturn("");
        when(mockFile.getPath()).thenReturn(path);
        when(mockFile.lastModified()).thenReturn(lastModified);

        when(mockFolder.getPrimaryFile()).thenReturn(mockFolderFileObject);
        when(mockFolderFileObject.toURI()).thenReturn(mockURI);
        when(mockGDO.getFolder()).thenReturn(mockFolder);
        when(mockFolderPath.toString()).thenReturn(mockFolderPathString);

        try (final MockedStatic<Paths> mockPaths = Mockito.mockStatic(Paths.class, Mockito.CALLS_REAL_METHODS); final MockedStatic<Files> mockFiles = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
            // Set up paths mock
            mockPaths.when(() -> Paths.get(mockURI)).thenReturn(mockFolderPath);
            mockPaths.when(() -> Paths.get(anyString(), anyString())).thenReturn(mockFolderPath);
            // Set up Files mock
            mockFiles.when(() -> Files.exists(mockFolderPath)).thenReturn(true);

            final VisualGraphTopComponent instance = new VisualGraphTopComponent(mockGDO, dgSpy);
            instance.getGraphNode().setDataObject(mockGDO);
            instance.saveGraph();

            assertEquals(instance.getGraphNode().getDataObject(), mockGDO);

            verify(mockGDO).isValid();
            verify(mockGDO).isInMemory();
            verify(mockGDO, times(2)).getName();
        }
    }
}
