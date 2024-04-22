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
package au.gov.asd.tac.constellation.plugins.arrangements.planar;

import au.gov.asd.tac.constellation.plugins.arrangements.planar.PQNodeList.PQNodeListTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * PQ Node List Test.
 *
 * @author twilight_sparkle
 */
public class PQNodeListNGTest {

    PQNodeListTest tester = new PQNodeListTest();

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
    public void testAdd() {
        tester.testAdd();
    }

    @Test
    public void testRemove() {
        tester.testRemove();
    }

    @Test
    public void testReplace() {
        tester.testReplace();
    }

    @Test
    public void testReverse() {
        tester.testReverse();
    }

    @Test
    public void testConcatenate() {
        tester.testConcatenate();
    }

    @Test
    public void testFlatten() {
        tester.testFlatten();
    }

}
