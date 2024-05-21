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
package au.gov.asd.tac.constellation.graph.visual.graphics;

import java.util.Arrays;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * BBox Test.
 *
 * @author algol
 */
public class BBoxNGTest {

    public BBoxNGTest() {
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
     * Test of getCentre method, of class BBox.
     */
    @Test
    public void testGetCentre() {
        final BBoxf box = new BBoxf();
        box.add(-1, 0, 1);
        box.add(1, 0, 1);

        System.out.printf("centre %s\n", Arrays.toString(box.getCentre()));
        System.out.printf("min %s\n", Arrays.toString(box.getMin()));
        System.out.printf("max %s\n", Arrays.toString(box.getMax()));

        assertEquals(box.getCentre(), new float[]{0, 0, 1});
        assertEquals(box.getMin(), new float[]{-1, 0, 1});
        assertEquals(box.getMax(), new float[]{1, 0, 1});
    }

    @Test
    public void empty() {
        final BBoxf box = new BBoxf();

        assertTrue(box.isEmpty());
    }
}
