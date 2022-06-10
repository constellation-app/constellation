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
package au.gov.asd.tac.constellation.utilities.support;

import au.gov.asd.tac.constellation.utilities.text.StringUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.modules.Places;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Support Package Test.
 *
 * @author arcturus
 * @author sol695510
 */
public class SupportPackageNGTest {

    private static final Logger LOGGER = Logger.getLogger(SupportPackageNGTest.class.getName());

    private static MockedStatic<Places> placesStaticMock;

    public SupportPackageNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timedout trying to cleanup stages", ex);
        }
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        placesStaticMock = Mockito.mockStatic(Places.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        placesStaticMock.close();
    }

    /**
     * Test of createSupportPackage method, of class SupportPackage.
     *
     * @throws IOException
     */
    @Test
    public void testCreateSupportPackage() throws IOException {
        System.out.println("testCreateSupportPackage");

        final SupportPackage instance = spy(new SupportPackage());

        final File sourceDirectory = new File(SupportPackage.getUserLogDirectory());
        final File destinationDirectory = File.createTempFile("file", ".file");

        instance.createSupportPackage(sourceDirectory, destinationDirectory);

        verify(instance, times(1)).generateFileList(Mockito.eq(sourceDirectory), Mockito.any(List.class), Mockito.eq(sourceDirectory.getPath()));
        verify(instance, times(1)).zipFolder(Mockito.eq(sourceDirectory.getPath()), Mockito.any(List.class), Mockito.eq(destinationDirectory.getPath()));

        Files.deleteIfExists(sourceDirectory.toPath());
        Files.deleteIfExists(destinationDirectory.toPath());
    }

    /**
     * Test of zipFolder method, of class SupportPackage.
     *
     * @throws IOException
     */
    @Test
    public void testZipFolder() throws IOException {
        System.out.println("testZipFolder");

        final SupportPackage instance = new SupportPackage();

        final Date now = new Date();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        final String username = StringUtilities.removeSpecialCharacters(System.getProperty("user.name"));
        final File destination = File.createTempFile(String.format("%s-%s-%s-", "SupportPackage", username, simpleDateFormat.format(now)), ".zip");

        assertTrue(destination.exists());

        final File file1 = new File("heapdump.hprof");
        final List<String> list1 = new ArrayList<>();

        instance.generateFileList(file1, list1, file1.getPath());
        instance.zipFolder(file1.getPath(), list1, destination.getPath());

        assertEquals(list1.size(), 0);

        final File file2 = new File(this.getClass().getResource("..").getFile());
        final List<String> list2 = new ArrayList<>();

        instance.generateFileList(file2, list2, file2.getPath());
        instance.zipFolder(file2.getPath(), list2, destination.getPath());

        assertTrue(list2.size() > 0);
    }

    /**
     * Test of generateFileList method, of class SupportPackage.
     */
    @Test
    public void testGenerateFileList() {
        System.out.println("testGenerateFileList");

        final SupportPackage instance = new SupportPackage();

        final File node = new File(this.getClass().getResource("..").getFile());
        final List<String> list = new ArrayList<>();

        instance.generateFileList(node, list, node.getPath());

        assertTrue(list.size() > 0);
    }

    /**
     * Test of getUserLogDirectory method, of class SupportPackage.
     *
     * @throws IOException
     */
    @Test
    public void testGetUserLogDirectory() throws IOException {
        System.out.println("testGetUserLogDirectory");

        final File file = File.createTempFile("file", ".file");

        placesStaticMock.when(()
                -> Places.getUserDirectory())
                .thenReturn(file);

        final String expResult1 = String.format("%s%svar%slog", file.getPath(), File.separator, File.separator);
        final String result1 = SupportPackage.getUserLogDirectory();

        assertEquals(result1, expResult1);

        placesStaticMock.when(()
                -> Places.getUserDirectory())
                .thenReturn(null);

        final String expResult2 = String.format("%s%svar%slog", new File(System.getProperty("user.home")).getPath(), File.separator, File.separator);
        final String result2 = SupportPackage.getUserLogDirectory();

        assertEquals(result2, expResult2);
    }

    /**
     * Test of filesToIgnore method, of class SupportPackage.
     */
    @Test
    public void testFilesToIgnore() {
        System.out.println("testFilesToIgnore");

        final SupportPackage instance = new SupportPackage();

        final File file1 = new File("heapdump.hprof");
        final File file2 = new File("heapdump.hprof.old");
        final File file3 = new File("file");

        assertTrue(instance.filesToIgnore(file1.getName()));
        assertTrue(instance.filesToIgnore(file2.getName()));
        assertFalse(instance.filesToIgnore(file3.getName()));
    }
}
