/*
* Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.icon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author capricornunicorn123
 */
public class ByteIconDataNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of createRasterInputStream method, of class ByteIconData.
     * @throws java.io.IOException
     */
    @Test
    public void testCreateRasterInputStream() throws IOException {
        final byte[] b = new byte[] {(byte)0xe0};
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ByteIconData bid = new ByteIconData(b);
        InputStream is = bid.createRasterInputStream();
        assertEquals(bais.readAllBytes(),is.readAllBytes());
    }

    /**
     * Test of createVectorInputStream method, of class ByteIconData.
     * @throws java.io.IOException
     */
    @Test(expectedExceptions=UnsupportedOperationException.class)
    public void testCreateVectorInputStream() throws IOException {
        final byte[] b = new byte[] {(byte)0xe0};
        ByteIconData bid = new ByteIconData(b);
        bid.createVectorInputStream();
    }
}
