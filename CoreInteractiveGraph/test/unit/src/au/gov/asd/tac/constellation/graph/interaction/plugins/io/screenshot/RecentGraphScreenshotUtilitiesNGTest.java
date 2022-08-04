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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io.screenshot;

import au.gov.asd.tac.constellation.graph.file.open.RecentFiles;
import au.gov.asd.tac.constellation.graph.file.open.RecentFiles.HistoryItem;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
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
    private static MockedStatic<Files> filesMock;
    private static MockedStatic<Logger> loggerMock;

    public RecentGraphScreenshotUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        recentGraphScreenshotUtilitiesMock = Mockito.mockStatic(RecentGraphScreenshotUtilities.class);
        recentFilesMock = Mockito.mockStatic(RecentFiles.class);
        filesMock = Mockito.mockStatic(Files.class);
        loggerMock = Mockito.mockStatic(Logger.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        recentGraphScreenshotUtilitiesMock.close();
        recentFilesMock.close();
        filesMock.close();
        loggerMock.close();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        recentGraphScreenshotUtilitiesMock.reset();
        recentFilesMock.reset();
        filesMock.reset();
        loggerMock.reset();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getScreenshotsDir method, of class
     * RecentGraphScreenshotUtilities.
     */
    @Test
    public void testGetScreenshotsDir() {
    }

    /**
     * Test of takeScreenshot method, of class RecentGraphScreenshotUtilities.
     */
    @Test
    public void testTakeScreenshot() {
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
     * Test of refreshScreenshotDir method, of class
     * RecentGraphScreenshotUtilities, where getScreenShotsDir returns null
     * value.
     */
    @Test
    public void testRefreshScreenshotsDirNull() {
        System.out.println("refreshScreenshotDirNull");

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

    /**
     * Test of refreshScreenshotDir method, of class
     * RecentGraphScreenshotUtilities, where getScreenShotsDir returns non null
     * value.
     */
    @Test
    public void testRefreshScreenshotsDirNotNull() {
        System.out.println("refreshScreenshotDirNotNull");

        final File file1 = mock(File.class);
        when(file1.getName()).thenReturn("file1.png");
        when(file1.toPath()).thenReturn(Paths.get("path\'file1.png"));

        final File file2 = mock(File.class);
        when(file2.getName()).thenReturn("file2.png");
        when(file2.toPath()).thenReturn(Paths.get("path\'file2.png"));

        final File screenShotsDir = mock(File.class);
        when(screenShotsDir.listFiles()).thenReturn(new File[]{file1, file2});

        // getScreenshotsDir() will return a file structure with files therefore there will be files in filesInDirectory to iterate through.
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.getScreenshotsDir()).thenReturn(screenShotsDir);
        recentGraphScreenshotUtilitiesMock.when(() -> RecentGraphScreenshotUtilities.refreshScreenshotsDir()).thenCallRealMethod();

        // Return a HistoryItem from getUniqueRecentFiles() to add to filesInHistory.
        recentFilesMock.when(() -> RecentFiles.getUniqueRecentFiles()).thenReturn(new ArrayList<>(Arrays.asList(new HistoryItem(1, "file1"))));
        filesMock.when(() -> Files.delete(Mockito.any())).thenAnswer((Answer<Void>) invocation -> null);

        RecentGraphScreenshotUtilities.refreshScreenshotsDir();

        recentGraphScreenshotUtilitiesMock.verify(() -> RecentGraphScreenshotUtilities.getScreenshotsDir(), times(1));
        recentFilesMock.verify(() -> RecentFiles.getUniqueRecentFiles(), times(1));

        // Files.delete() will be called only on file2 since it is not in filesInHistory.
        filesMock.verify(() -> Files.delete(Mockito.any()), times(1));
        filesMock.verify(() -> Files.delete(Mockito.eq(Paths.get("path\'file2.png"))), times(1));
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
