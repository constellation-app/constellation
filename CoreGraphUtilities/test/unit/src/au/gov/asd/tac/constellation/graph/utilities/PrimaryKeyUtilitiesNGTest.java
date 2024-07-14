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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import java.util.HashSet;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Primary Key Utilities Test.
 *
 * @author arcturus
 */
public class PrimaryKeyUtilitiesNGTest {

    public PrimaryKeyUtilitiesNGTest() {
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
     * Test of getPrimaryKeyNames method, of class PrimaryKeyUtilities.
     */
    @Test
    public void testGetPrimaryKeyNamesForVertices() {
        final StoreGraph graph = new StoreGraph();
        final int attribute = graph.addAttribute(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Name", "", "", null);
        graph.setPrimaryKey(GraphElementType.VERTEX, attribute);

        final Set<String> expResult = new HashSet<>();
        expResult.add("Name");

        final Set<String> result = PrimaryKeyUtilities.getPrimaryKeyNames(graph, GraphElementType.VERTEX);
        assertEquals(result, expResult);
    }

    @Test
    public void testGetPrimaryKeyNamesForTransactions() {
        final StoreGraph graph = new StoreGraph();
        final int attribute = graph.addAttribute(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Name", "", "", null);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, attribute);

        final Set<String> expResult = new HashSet<>();
        expResult.add("Name");

        final Set<String> result = PrimaryKeyUtilities.getPrimaryKeyNames(graph, GraphElementType.TRANSACTION);
        assertEquals(result, expResult);
    }

}
