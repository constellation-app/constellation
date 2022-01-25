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
package au.gov.asd.tac.constellation.views.attributeeditor.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author sol695510
 */
public class IconEditorUtilitiesNGTest {

    private static MockedStatic<IconEditorUtilities> iconEditorUtilitiesStaticMock;
    private static File pathMock;
    private static File folderMock;

    public IconEditorUtilitiesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        iconEditorUtilitiesStaticMock = Mockito.mockStatic(IconEditorUtilities.class);
        pathMock = Mockito.mock(File.class);
        folderMock = Mockito.mock(File.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        iconEditorUtilitiesStaticMock.close();
    }

    /**
     * Test of pngWalk method, of class IconEditorUtilities.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testPngWalk_File() throws Exception {
        System.out.println("testPngWalk_File");

        iconEditorUtilitiesStaticMock.when(() -> IconEditorUtilities.pngWalk(Mockito.any(File.class))).thenCallRealMethod();
        iconEditorUtilitiesStaticMock.when(() -> IconEditorUtilities.pngWalk(Mockito.any(File.class), Mockito.eq(new ArrayList<>()))).thenCallRealMethod();

        // When path is null.
        final File path1 = null;

        final List<File> expResult1 = new ArrayList<>();
        final List<File> result1 = IconEditorUtilities.pngWalk(path1);

        assertEquals(result1, expResult1);

        // When path is valid.
        final File path2 = pathMock;

        final File file1 = File.createTempFile("file1", ".png");
        final File file2 = File.createTempFile("file2", ".png");

        final File[] listFiles = new File[]{file1, file2};

        doReturn(listFiles).when(pathMock).listFiles();

        final List<File> expResult2 = Arrays.stream(new File[]{file1, file2}).collect(Collectors.toList());
        final List<File> result2 = IconEditorUtilities.pngWalk(path2);

        assertEquals(result2, expResult2);
    }

    /**
     * Test of pngWalk method, of class IconEditorUtilities.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testPngWalk_File_List() throws Exception {
        System.out.println("testPngWalk_File_List");

        iconEditorUtilitiesStaticMock.when(() -> IconEditorUtilities.pngWalk(Mockito.any(File.class), Mockito.eq(new ArrayList<>()))).thenCallRealMethod();

        // When path is null;
        final File path1 = null;
        final List<File> files1 = new ArrayList<>();

        final List<File> expResult1 = new ArrayList<>();
        final List<File> result1 = IconEditorUtilities.pngWalk(path1, files1);

        assertEquals(result1, expResult1);

        // When path is valid.
        final File path2 = pathMock;
        final List<File> files2 = new ArrayList<>();

        final File file1 = File.createTempFile("file1", ".png");
        final File file2 = File.createTempFile("file2", ".png");

        final File[] listFiles1 = new File[]{file1, file2};

        doReturn(listFiles1).when(path2).listFiles();

        final List<File> expResult2 = Arrays.stream(new File[]{file1, file2}).collect(Collectors.toList());
        final List<File> result2 = IconEditorUtilities.pngWalk(path2, files2);

        assertEquals(result2, expResult2);

        // When path is valid and contains a nested folder to test recursion.
        final File path3 = pathMock;
        final List<File> files3 = new ArrayList<>();

        final File folder = folderMock;
        final File file3 = File.createTempFile("file3", ".png");
        final File file4 = File.createTempFile("file4", ".png");
        final File file5 = File.createTempFile("file5", ".png");
        final File file6 = File.createTempFile("file6", ".png");

        final File[] listFiles2 = new File[]{folder, file3, file4};
        final File[] listFiles3 = new File[]{file5, file6};

        doReturn(listFiles2).when(path3).listFiles();
        doReturn(listFiles3).when(folder).listFiles();
        doReturn(Boolean.TRUE).when(folder).isDirectory();

        final List<File> expResult3 = Arrays.stream(new File[]{file5, file6, file3, file4}).collect(Collectors.toList());
        final List<File> result3 = IconEditorUtilities.pngWalk(path3, files3);

        assertEquals(result3, expResult3);
    }
}
