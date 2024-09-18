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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.open.RecentFiles;
import au.gov.asd.tac.constellation.graph.file.open.RecentFiles.HistoryItem;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javax.xml.bind.DatatypeConverter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol695510
 */
public class RecentGraphScreenshotUtilitiesNGTest {

    private static MockedStatic<RecentGraphScreenshotUtilities> recentGraphScreenshotUtilitiesMock;
    private static MockedStatic<RecentFiles> recentFilesMock;
    private static MockedStatic<DatatypeConverter> dataTypeConverter;

    public RecentGraphScreenshotUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        recentGraphScreenshotUtilitiesMock = Mockito.mockStatic(RecentGraphScreenshotUtilities.class);
        recentFilesMock = Mockito.mockStatic(RecentFiles.class);
        dataTypeConverter = Mockito.mockStatic(DatatypeConverter.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        recentGraphScreenshotUtilitiesMock.close();
        recentFilesMock.close();
        dataTypeConverter.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        recentGraphScreenshotUtilitiesMock.reset();
        recentFilesMock.reset();
    }

    /**
     * Test of takeScreenshot method, of class RecentGraphScreenshotUtilities.
     */
    @Test
    public void testTakeScreenshot() {
        System.out.println("testTakeScreenshot");
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.takeScreenshot(anyString())).thenCallRealMethod();
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.takeScreenshot(anyString(), any())).thenCallRealMethod();

        final String filePath = "";

        // Mocks
        final Graph mockGraph = mock(Graph.class);
        final GraphNode mockGraphNode = mock(GraphNode.class);
        final GraphManager gm = mock(GraphManager.class);
        final VisualManager vm = mock(VisualManager.class);

        when(mockGraph.getId()).thenReturn("");
        when(gm.getActiveGraph()).thenReturn(mockGraph);
        when(mockGraphNode.getVisualManager()).thenReturn(vm);

        // Assert mocks work
        assertEquals(gm.getActiveGraph(), mockGraph);
        assertEquals(mockGraphNode.getVisualManager(), vm);

        //GraphNode.getGraphNode
        try (MockedStatic<GraphManager> mockedGraphManager = Mockito.mockStatic(GraphManager.class); MockedStatic<GraphNode> mockedGraphNode = Mockito.mockStatic(GraphNode.class); MockedConstruction<Semaphore> mockSemaphoreConstructor = Mockito.mockConstruction(Semaphore.class)) {
            mockedGraphManager.when(GraphManager::getDefault).thenReturn(gm);
            // Assert mocks work
            assertEquals(GraphManager.getDefault(), gm);

            // Test graphnode is null
            mockedGraphNode.when(() -> GraphNode.getGraphNode(any(Graph.class))).thenReturn(null);
            assertEquals(GraphNode.getGraphNode(mockGraph), null);

            RecentGraphScreenshotUtilities.takeScreenshot(filePath);

            // Test graphnode not null, but visual manager is null
            mockedGraphNode.when(() -> GraphNode.getGraphNode(any(Graph.class))).thenReturn(mockGraphNode);
            when(mockGraphNode.getVisualManager()).thenReturn(null);
            assertEquals(GraphNode.getGraphNode(mockGraph), mockGraphNode);
            assertEquals(mockGraphNode.getVisualManager(), null);

            RecentGraphScreenshotUtilities.takeScreenshot(filePath);

            // Test with graphnode and visual manager NOT null
            mockedGraphNode.when(() -> GraphNode.getGraphNode(any(Graph.class))).thenReturn(mockGraphNode);
            when(mockGraphNode.getVisualManager()).thenReturn(vm);
            assertEquals(GraphNode.getGraphNode(mockGraph), mockGraphNode);
            assertEquals(mockGraphNode.getVisualManager(), vm);

            RecentGraphScreenshotUtilities.takeScreenshot(filePath);
        }

    }

    /**
     * Test of resizeAndSave method, of class RecentGraphScreenshotUtilities.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testResizeAndSave() throws Exception {
    }

    /**
     * Test of refreshScreenshotDir method, of class RecentGraphScreenshotUtilities, where getScreenShotsDir returns
     * null value.
     */
    @Test
    public void testRefreshScreenshotsDirNull() {
        System.out.println("refreshScreenshotDirNull");

        try (final MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            // getScreenshotsDir() will return null therefore there will be no files in filesInDirectory to iterate through.
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(null);
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.refreshScreenshotsDir()).thenCallRealMethod();

            // Return a HistoryItem from getUniqueRecentFiles() to add to filesInHistory.
            recentFilesMock.when(() -> RecentFiles.getUniqueRecentFiles()).thenReturn(new ArrayList<>(Arrays.asList(new HistoryItem(1, "file1"))));
            filesMock.when(() -> Files.delete(Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);

            RecentGraphScreenshotUtilities.refreshScreenshotsDir();

            recentGraphScreenshotUtilitiesMock.verify(() -> RecentGraphScreenshotUtilities.getScreenshotsDir(), times(1));
            recentFilesMock.verify(() -> RecentFiles.getUniqueRecentFiles(), times(1));

            // Files.delete() will never be called since filesInDirectory is empty.
            filesMock.verifyNoInteractions();
        }
    }

    /**
     * Test of refreshScreenshotDir method, of class RecentGraphScreenshotUtilities, where getScreenShotsDir returns non
     * null value.
     */
    @Test
    public void testRefreshScreenshotsDirNotNull() {
        System.out.println("refreshScreenshotDirNotNull");
        try (final MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            final File file1 = mock(File.class);
            when(file1.getName()).thenReturn("file1.star.png");
            when(file1.toPath()).thenReturn(Paths.get("path\'file1.star.png"));

            final File file2 = mock(File.class);
            when(file2.getName()).thenReturn("file2.star.png");
            when(file2.toPath()).thenReturn(Paths.get("path\'file2.star.png"));

            final File screenShotsDir = mock(File.class);
            when(screenShotsDir.listFiles()).thenReturn(new File[]{file1, file2});

            // getScreenshotsDir() will return a file structure with files therefore there will be files in filesInDirectory to iterate through.
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(screenShotsDir);
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.refreshScreenshotsDir()).thenCallRealMethod();
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.findScreenshot(anyString(), anyString())).thenReturn(Optional.of(file1));

            // Return a HistoryItem from getUniqueRecentFiles() to add to filesInHistory.
            recentFilesMock.when(() -> RecentFiles.getUniqueRecentFiles()).thenReturn(new ArrayList<>(Arrays.asList(new HistoryItem(1, "file1.star"))));
            filesMock.when(() -> Files.delete(Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);

            RecentGraphScreenshotUtilities.refreshScreenshotsDir();

            recentGraphScreenshotUtilitiesMock.verify(() -> RecentGraphScreenshotUtilities.getScreenshotsDir(), times(1));
            recentFilesMock.verify(() -> RecentFiles.getUniqueRecentFiles(), times(1));

            // Files.delete() will be called only on file2 since it is not in filesInHistory.
            filesMock.verify(() -> Files.delete(Mockito.any()), times(1));
            filesMock.verify(() -> Files.delete(Mockito.eq(Paths.get("path\'file2.star.png"))), times(1));
        }
    }

    /**
     * Test of refreshScreenshotHashed method, of class RecentGraphScreenshotUtilities, where a path should be hashed
     * path should be found in the directory
     */
    @Test
    public void testRefreshScreenshotsHashed() {
        System.out.println("refreshScreenshotDirNotNull");
        try (final MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            final File file1 = mock(File.class);
            when(file1.getName()).thenReturn("1901de09374733aff5b72e9400d18482.png");
            when(file1.toPath()).thenReturn(Paths.get("1901de09374733aff5b72e9400d18482.png"));

            final File screenShotsDir = mock(File.class);
            when(screenShotsDir.listFiles()).thenReturn(new File[]{file1});

            // getScreenshotsDir() will return a file structure with files therefore there will be files in filesInDirectory to iterate through.
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(screenShotsDir);
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.refreshScreenshotsDir()).thenCallRealMethod();
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.findScreenshot(anyString(), anyString())).thenReturn(Optional.of(file1));

            // Return a HistoryItem from getUniqueRecentFiles() to add to filesInHistory.
            recentFilesMock.when(() -> RecentFiles.getUniqueRecentFiles()).thenReturn(new ArrayList<>(Arrays.asList(new HistoryItem(1, "/path/to/helloworld"))));
            filesMock.when(() -> Files.delete(Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);

            RecentGraphScreenshotUtilities.refreshScreenshotsDir();

            recentGraphScreenshotUtilitiesMock.verify(() -> RecentGraphScreenshotUtilities.getScreenshotsDir(), times(1));
            recentFilesMock.verify(() -> RecentFiles.getUniqueRecentFiles(), times(1));

            // Files.delete() will be called only on file2 since it is not in filesInHistory.
            filesMock.verify(() -> Files.delete(Mockito.any()), times(1));
        }
    }

    /**
     * Test of refreshScreenshotHashed method, of class RecentGraphScreenshotUtilities, where a path should be hashed
     * path should be found in the directory
     */
    @Test
    public void testRefreshScreenshotsLegacy() {
        System.out.println("refreshScreenshotDirNotNull");

        try (final MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            final File file1 = mock(File.class);
            when(file1.getName()).thenReturn("test1.star.png");
            when(file1.toPath()).thenReturn(Paths.get("path\\to\\userdir\\test1.star.png"));

            final File screenShotsDir = mock(File.class);
            when(screenShotsDir.listFiles()).thenReturn(new File[]{file1});

            // getScreenshotsDir() will return a file structure with files therefore there will be files in filesInDirectory to iterate through.
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(screenShotsDir);
            recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.refreshScreenshotsDir()).thenCallRealMethod();

            // Return a HistoryItem from getUniqueRecentFiles() to add to filesInHistory.
            recentFilesMock.when(() -> RecentFiles.getUniqueRecentFiles()).thenReturn(new ArrayList<>(Arrays.asList(new HistoryItem(1, "path\\to\\userdir\\test1.star"))));
            filesMock.when(() -> Files.delete(Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);

            RecentGraphScreenshotUtilities.refreshScreenshotsDir();

            recentGraphScreenshotUtilitiesMock.verify(() -> RecentGraphScreenshotUtilities.getScreenshotsDir(), times(1));
            recentFilesMock.verify(() -> RecentFiles.getUniqueRecentFiles(), times(1));

            // Files.delete() will be called only on file2 since it is not in filesInHistory.
            filesMock.verifyNoInteractions();
        }
    }

    @Test
    public void testHashFilePath() {
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.hashFilePath(anyString())).thenCallRealMethod();
        dataTypeConverter.when(() -> DatatypeConverter.printHexBinary(Mockito.any())).thenReturn("0c695c8bff7af91d321c237bdf969addbfb859be8095d880f1d034737fbc35d2");
        final String actual = RecentGraphScreenshotUtilities.hashFilePath("/test/path");
        final String expected = "0C695C8BFF7AF91D321C237BDF969ADDBFB859BE8095D880F1D034737FBC35D2";

        assertEquals(actual, expected);
    }

    @Test
    public void testFindScreenshotWithValidLegacyFile() throws IOException {
        File testFile = File.createTempFile("file", ".png");

        final File screenShotsDir = mock(File.class);
        when(screenShotsDir.toString()).thenReturn(testFile.getParent());

        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.hashFilePath(anyString())).thenReturn("hash123");
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(screenShotsDir);
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.findScreenshot(anyString(), anyString())).thenCallRealMethod();

        final Optional<File> actual = RecentGraphScreenshotUtilities.findScreenshot(testFile.getParent(), testFile.getName().replace(".png", ""));
        final Optional<File> expected = Optional.of(new File(testFile.getParent() + File.separator + testFile.getName()));

        testFile.delete();
        assertEquals(actual, expected);
    }

    @Test
    public void testFindScreenshotButNotFound() throws IOException {
        final File screenShotsDir = mock(File.class);
        when(screenShotsDir.toString()).thenReturn("/screenshots");

        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.hashFilePath(anyString())).thenReturn("hash123");
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(screenShotsDir);
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.findScreenshot(anyString(), anyString())).thenCallRealMethod();

        final Optional<File> actual = RecentGraphScreenshotUtilities.findScreenshot("/not/found", "file");
        final Optional<File> expected = Optional.empty();

        assertEquals(actual, expected);
    }

    @Test
    public void testRequestGraphActive() {
        System.out.println("testRequestGraphActive");

        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.requestGraphActive(any(), any())).thenCallRealMethod();

        // Set up mocks
        final Graph mockGraph = mock(Graph.class);
        when(mockGraph.getId()).thenReturn("");

        final WindowManager wm = mock(WindowManager.class);
        final Registry reg = mock(Registry.class);
        when(wm.getRegistry()).thenReturn(reg);

        final GraphNode gn = mock(GraphNode.class);
        when(gn.getGraph()).thenReturn(mockGraph);

        final Set<TopComponent> setTopC = new HashSet<>();
        final VisualGraphTopComponent tc = mock(VisualGraphTopComponent.class);
        when(tc.getGraphNode()).thenReturn(gn);
        setTopC.add(tc);
        when(reg.getOpened()).thenReturn(setTopC);

        // Assert mocks work
        assertEquals(wm.getRegistry(), reg);
        assertEquals(reg.getOpened(), setTopC);

        final Semaphore semaphore = new Semaphore(1);

        // test correct functionality
        testRequestGraphActiveHelper(mockGraph, wm, reg, setTopC, semaphore);
        // Verify functions were run
        verify(tc, times(1)).getGraphNode();
        verify(gn, times(1)).getGraph();
        verify(mockGraph, times(2)).getId();

        try (MockedStatic<EventQueue> mockedEventQueue = Mockito.mockStatic(EventQueue.class, Mockito.CALLS_REAL_METHODS)) {
            // test InvocationTargetException
            mockedEventQueue.when(() -> EventQueue.invokeAndWait(any())).thenThrow(new InvocationTargetException(new Throwable()));

            assertThrows(() -> EventQueue.invokeAndWait(() -> {
                ((VisualGraphTopComponent) tc).requestActive();
            }));

            testRequestGraphActiveHelper(mockGraph, wm, reg, setTopC, semaphore);

            // Verify functions were run (includes previous test)
            verify(tc, times(2)).getGraphNode();
            verify(gn, times(2)).getGraph();
            verify(mockGraph, times(4)).getId();
        }
    }

    @Test
    public void testRequestGraphActiveInterrupt() {
        System.out.println("testRequestGraphActiveInterrupt");

        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.requestGraphActive(any(), any())).thenCallRealMethod();

        // Set up mocks
        final Graph mockGraph = mock(Graph.class);
        when(mockGraph.getId()).thenReturn("");

        final WindowManager wm = mock(WindowManager.class);
        final Registry reg = mock(Registry.class);
        when(wm.getRegistry()).thenReturn(reg);

        final GraphNode gn = mock(GraphNode.class);
        when(gn.getGraph()).thenReturn(mockGraph);

        final Set<TopComponent> setTopC = new HashSet<>();
        final VisualGraphTopComponent tc = mock(VisualGraphTopComponent.class);
        when(tc.getGraphNode()).thenReturn(gn);
        setTopC.add(tc);
        when(reg.getOpened()).thenReturn(setTopC);

        // Assert mocks work
        assertEquals(wm.getRegistry(), reg);
        assertEquals(reg.getOpened(), setTopC);

        final Semaphore semaphore = new Semaphore(1);

        // test correct functionality
        testRequestGraphActiveHelper(mockGraph, wm, reg, setTopC, semaphore);
        // Verify functions were run
        verify(tc, times(1)).getGraphNode();
        verify(gn, times(1)).getGraph();
        verify(mockGraph, times(2)).getId();

        try (MockedStatic<EventQueue> mockedEventQueue = Mockito.mockStatic(EventQueue.class, Mockito.CALLS_REAL_METHODS)) {
            // test InterruptedException
            mockedEventQueue.when(() -> EventQueue.invokeAndWait(any())).thenThrow(new InterruptedException());

            assertThrows(() -> EventQueue.invokeAndWait(() -> {
                ((VisualGraphTopComponent) tc).requestActive();
            }));

            testRequestGraphActiveHelper(mockGraph, wm, reg, setTopC, semaphore);

            // Verify functions were run (includes previous test)
            verify(tc, times(2)).getGraphNode();
            verify(gn, times(2)).getGraph();
            verify(mockGraph, times(4)).getId();
        }
    }

    private void testRequestGraphActiveHelper(final Graph mockGraph, final WindowManager mockWindowManager, final Registry mockRegistry, final Set<TopComponent> topComponents, final Semaphore semaphore) {
        try (MockedStatic<WindowManager> mockedWindowManager = Mockito.mockStatic(WindowManager.class)) {
            mockedWindowManager.when(WindowManager::getDefault).thenReturn(mockWindowManager);
            // Assert mocks work
            assertEquals(WindowManager.getDefault(), mockWindowManager);

            // When top component is NOT null
            when(mockRegistry.getOpened()).thenReturn(topComponents);

            RecentGraphScreenshotUtilities.requestGraphActive(null, semaphore);
            RecentGraphScreenshotUtilities.requestGraphActive(mockGraph, semaphore);

            // When top component is null
            when(mockRegistry.getOpened()).thenReturn(null);

            RecentGraphScreenshotUtilities.requestGraphActive(null, semaphore);
            RecentGraphScreenshotUtilities.requestGraphActive(mockGraph, semaphore);
        }
    }

    // Couldn't find a way to mock LOGGER.log() to assert whether it was ever invoked.
//    /**
//     * Test of refreshScreenshotDir method, of class
//     * RecentGraphScreenshotUtilities, when an IOException is thrown.
//     */
//    @Test
//    public void testRefreshScreenshotsDirThrowsException() {
//        System.out.println("refreshScreenshotDirWithoutNull");
//
//        final Logger logger = mock(Logger.class);
//        loggerMock.when(() -> Logger.getLogger(Mockito.any())).thenReturn(logger);
////        when(logger.log(Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);
//
//        final File file1 = mock(File.class);
//        when(file1.getName()).thenReturn("file1.png");
//        when(file1.toPath()).thenReturn(Paths.get("path\'file1.png"));
//
//        final File file2 = mock(File.class);
//        when(file2.getName()).thenReturn("file2.png");
//        when(file2.toPath()).thenReturn(Paths.get("path\'file2.png"));
//
//        final File screenShotsDir = mock(File.class);
//        when(screenShotsDir.listFiles()).thenReturn(new File[]{file1, file2});
//
//        // getScreenshotsDir() will return a file structure with files therefore there will be files in filesInDirectory to iterate through.
//        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(screenShotsDir);
//        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.refreshScreenshotsDir()).thenCallRealMethod();
//
//        // Return a HistoryItem from getUniqueRecentFiles() to add to filesInHistory.
//        recentFilesMock.when(() -> RecentFiles.getUniqueRecentFiles()).thenReturn(new ArrayList<>(Arrays.asList(new HistoryItem(1, "file1"))));
//        filesMock.when(() -> Files.delete(Mockito.any())).thenThrow(IOException.class);
//
//        RecentGraphScreenshotUtilities.refreshScreenshotsDir();
//
//        recentGraphScreenshotUtilitiesMock.verify(() -> RecentGraphScreenshotUtilities.getScreenshotsDir(), times(1));
//        recentFilesMock.verify(() -> RecentFiles.getUniqueRecentFiles(), times(1));
//
//        // Files.delete() will be called only on file2 since it is not in filesInHistory and it will throw an IOException because of its invalid path.
//        filesMock.verify(() -> Files.delete(Mockito.any()), times(1));
//        filesMock.verify(() -> Files.delete(Mockito.eq(Paths.get("path\'file2.png"))), times(1));
//        loggerMock.verifyNoInteractions();
//    }
}
