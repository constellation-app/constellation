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
package au.gov.asd.tac.constellation.graph.schema.analytic.utilities;

import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import java.util.List;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class AnalyticDominanceCalculatorNGTest {
    
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
     * Test of getTypePriority method, of class AnalyticDominanceCalculator.
     */
    @Test
    public void testGetTypePriority() {
        System.out.println("getTypePriority");
        
        final AnalyticDominanceCalculator instance = new AnalyticDominanceCalculator();
        final List<SchemaVertexType> typePriority = instance.getTypePriority();
        
        // not a huge amount that can be tested here so testing that any types with super types are ahead of their super in the list
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.IPV4) < typePriority.indexOf(AnalyticConcept.VertexType.NETWORK_IDENTIFIER));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.IPV6) < typePriority.indexOf(AnalyticConcept.VertexType.NETWORK_IDENTIFIER));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.COUNTRY) < typePriority.indexOf(AnalyticConcept.VertexType.LOCATION));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.GEOHASH) < typePriority.indexOf(AnalyticConcept.VertexType.LOCATION));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.MGRS) < typePriority.indexOf(AnalyticConcept.VertexType.LOCATION));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.EMAIL_ADDRESS) < typePriority.indexOf(AnalyticConcept.VertexType.ONLINE_IDENTIFIER));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.USER_NAME) < typePriority.indexOf(AnalyticConcept.VertexType.ONLINE_IDENTIFIER));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.HOST_NAME) < typePriority.indexOf(AnalyticConcept.VertexType.ONLINE_LOCATION));
        assertTrue(typePriority.indexOf(AnalyticConcept.VertexType.URL) < typePriority.indexOf(AnalyticConcept.VertexType.ONLINE_LOCATION));
    }
}
