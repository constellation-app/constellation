/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.hashmod;

import au.gov.asd.tac.constellation.utilities.file.FileExtensionConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for HashmodInputSource.
 *
 * @author sol695510
 */
public class HashmodInputSourceNGTest {

    private static InputStream inputStreamMock;

    public HashmodInputSourceNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        inputStreamMock = mock(InputStream.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of getFile method, of class HashmodInputSource.
     *
     * @throws IOException
     */
    @Test
    public void testGetFile() throws IOException {
        System.out.println("testGetFile");

        final File file1 = File.createTempFile("fileCsv", FileExtensionConstants.COMMA_SEPARATED_VALUE);

        // If file is not null and inputStream is null.
        final HashmodInputSource instance1 = new HashmodInputSource(file1);
        assertEquals(instance1.getFile(), file1);

        // If file is null and inputStream is not null.
        final HashmodInputSource instance2 = new HashmodInputSource(inputStreamMock);
        assertEquals(instance2.getFile(), null);

        Files.deleteIfExists(file1.toPath());
    }

    /**
     * Test of getInputStream method, of class HashmodInputSource.
     *
     * @throws IOException
     */
    @Test
    public void testGetInputStream() throws IOException {
        System.out.println("testGetInputStream");

        final File file1 = File.createTempFile("fileCsv", FileExtensionConstants.COMMA_SEPARATED_VALUE);
        final File file2 = new File("/invalidPath/fileCsv" + FileExtensionConstants.COMMA_SEPARATED_VALUE);

        // If inputStream is null and file exists.
        final HashmodInputSource instance1 = new HashmodInputSource(file1);
        try ( InputStream inputStream = instance1.getInputStream()) {
            assertEquals(file1.exists(), true);
            assertEquals(inputStream.getClass(), FileInputStream.class);
        }

        // If inputStream is null and file does not exist.
        final HashmodInputSource instance2 = new HashmodInputSource(file2);
        assertEquals(file2.exists(), false);
        assertEquals(instance2.getInputStream(), null);

        // if inputStream is not null and file is null.
        final HashmodInputSource instance3 = new HashmodInputSource(inputStreamMock);
        assertEquals(instance3.getInputStream(), inputStreamMock);

        // If inputStream is null and file is null.
        final HashmodInputSource instance4 = new HashmodInputSource(mock(File.class));
        assertEquals(instance4.getInputStream(), null);

        Files.deleteIfExists(file1.toPath());
    }
}
