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
package au.gov.asd.tac.constellation.graph.file;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author GCHQDeveloper601
 */
public class GraphObjectUtilitiesNGTest {

    // This is the in-memory filesystem we use to store files for DataObjects.
    private static final FileSystem FILE_SYSTEM = FileUtil.createMemoryFileSystem();

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test(priority = 0)
    public void writeGraphNameNumberedFirst() throws IOException {
        GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject("exampleGraph", true);
        assertEquals(gdo.getName(), "exampleGraph1");
    }

    @Test(priority = 1)
    public void writeGraphNameNumberedSecond() throws IOException {
        GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject("exampleGraph", true);
        assertEquals(gdo.getName(), "exampleGraph2");
    }

    @Test(priority = 2)
    public void writeGraphNameNumberedThird() throws IOException {
        GraphDataObject gdo = GraphObjectUtilities.createMemoryDataObject("exampleGraph", true);
        assertEquals(gdo.getName(), "exampleGraph3");
    }

    @Test(priority = 3)
    public void writeGraphNameNumberedCopy() throws IOException {
        final String baseName = "exampleGraph1";
        ensureFileExistsFirst(baseName);
        GraphDataObject gdo1 = GraphObjectUtilities.createMemoryDataObject(baseName, true);
        assertEquals(gdo1.getName(), "exampleGraph1 - Copy");
    }

    @Test(priority = 4)
    public void writeGraphNameNumberedSecondCopy() throws IOException {
        final String baseName = "exampleGraph1 - Copy";
        ensureFileExistsFirst(baseName);
        GraphDataObject gdo1 = GraphObjectUtilities.createMemoryDataObject(baseName, true);
        assertEquals(gdo1.getName(), "exampleGraph1 - Copy (1)");
    }

    @Test(priority = 5)
    public void writeGraphNameNumberedThirdCopy() throws IOException {
        final String baseName = "exampleGraph1 - Copy";
        GraphDataObject gdo1 = GraphObjectUtilities.createMemoryDataObject(baseName, true);
        assertEquals(gdo1.getName(), "exampleGraph1 - Copy (2)");
    }

    private void ensureFileExistsFirst(final String filename) throws IOException {
        final FileObject root = FILE_SYSTEM.getRoot();
        FileUtil.createData(root, filename);
    }
}
