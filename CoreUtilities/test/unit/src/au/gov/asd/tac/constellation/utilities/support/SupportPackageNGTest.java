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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Support Package Test.
 *
 * @author arcturus
 */
public class SupportPackageNGTest {

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
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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

        Assert.assertTrue(destination.exists());
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

        Assert.assertTrue(list.size() > 0);
    }

}
