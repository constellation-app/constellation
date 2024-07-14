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
package au.gov.asd.tac.constellation.plugins.importexport.jdbc;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
public class JDBCImportControllerNGTest {

    public JDBCImportControllerNGTest() {
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
     * Tests if data clears after clearSampleData is called
     */
    @Test
    public void testClearData() {
        System.out.println("clearData");
        JDBCImportController instance = new JDBCImportController();

        String[] sample = {"test", "test", "test"};
        List<String[]> sampleData = new ArrayList<String[]>();

        sampleData.add(sample);
        sampleData.add(sample);
        sampleData.add(sample);

        instance.setColumns(sample);


        instance.clearSampleData();

        assertEquals(0, instance.getColumns().length);
        assertEquals(0, instance.getData().size());
    }


}
