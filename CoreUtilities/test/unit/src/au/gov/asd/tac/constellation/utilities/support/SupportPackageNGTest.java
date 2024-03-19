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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.modules.Places;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class of SupportPackage.
 *
 * @author arcturus
 * @author sol695510
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class SupportPackageNGTest extends ConstellationTest {

    private static MockedStatic<Places> placesStaticMock;

    public SupportPackageNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        placesStaticMock = mockStatic(Places.class);
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

        verify(instance, times(1)).generateFileList(eq(sourceDirectory), any(List.class), eq(sourceDirectory.getPath()));
        verify(instance, times(1)).zipFolder(eq(sourceDirectory.getPath()), any(List.class), eq(destinationDirectory.getPath()));

        Files.deleteIfExists(sourceDirectory.toPath());
        Files.deleteIfExists(destinationDirectory.toPath());
    }

    /**
     * Test of zipFolder method, of class SupportPackage.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testZipFolder() throws IOException {
        final Date now = new Date();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        final String username = StringUtilities.removeSpecialCharacters(System.getProperty("user.name"));

        final File file = new File(this.getClass().getResource("..").getFile());
        final List<String> list = new ArrayList<>();
        final File destination = File.createTempFile(String.format("%s-%s-%s-", "SupportPackage", username, simpleDateFormat.format(now)), ".zip");
        final SupportPackage instance = new SupportPackage();
        instance.generateFileList(file, list, file.getPath());
        instance.zipFolder(file.getPath(), list, destination.getPath());

        assertTrue(destination.exists());
    }

    /**
     * Test of generateFileList method, of class SupportPackage.
     */
    @Test
    public void testGenerateFileList() {
        final File node = new File(this.getClass().getResource("..").getFile());
        final List<String> list = new ArrayList<>();
        final SupportPackage instance = new SupportPackage();
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

        Files.deleteIfExists(file.toPath());
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
