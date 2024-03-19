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
package au.gov.asd.tac.constellation.utilities.csv;

import java.io.IOException;
import java.io.StringWriter;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class SmartCSVWriterNGTest extends ConstellationTest {

    public SmartCSVWriterNGTest() {
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

    @Test
    public void writeNext() throws IOException {
        final StringWriter writer = new StringWriter();

        try (SmartCSVWriter smartCSVWriter = new SmartCSVWriter(writer)) {
            String[] row = new String[5];
            row[0] = "test";
            row[1] = "some \"wierd\" text";
            row[2] = "Then what about the ,";
            row[3] = "Or a line \r feed";
            row[4] = "Or a new \n line";

            smartCSVWriter.writeNext(row);
        }

        assertEquals(writer.toString(),
                "test,\"some \"\"wierd\"\" text\",\"Then what about the ,"
                + "\",\"Or a line \r feed\",\"Or a new \n line\"\n");
    }
}
