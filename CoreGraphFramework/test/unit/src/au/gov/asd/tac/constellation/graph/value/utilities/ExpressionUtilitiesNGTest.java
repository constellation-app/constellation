/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.value.utilities;

import java.util.ArrayList;
import java.util.List;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class ExpressionUtilitiesNGTest extends ConstellationTest {
    
    public ExpressionUtilitiesNGTest() {
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
     * Test of testQueryValidity method, of class ExpressionUtilities.
     */
    @Test
    public void testTestQueryValidity() {
        System.out.println("testQueryValidity");
        
        final List<String> validQueryStrings = new ArrayList<>();
        validQueryStrings.add("Label == 'Vertex #1<Unknown>'");
        validQueryStrings.add("Source == 'Manually Created'");
        validQueryStrings.add("Type == 'Machine Identifier'");
        validQueryStrings.add("background_icon == 'Background.Flat Square'");
        validQueryStrings.add("color == '#a0f461'");
        validQueryStrings.add("x == '0.940897'");
        validQueryStrings.add("Label == 'Vertex #1<Unknown>' && dim == 'false'");
        validQueryStrings.add("x startswith 1"); // valid because parse will not detect issue until calculation
        validQueryStrings.add(""); // valid as no errors
        
        
        final List<String> invalidQueryStrings = new ArrayList<>();
        invalidQueryStrings.add("x = '1.15");
        invalidQueryStrings.add("x = 1.15");
        invalidQueryStrings.add("x = 1.15'");
        invalidQueryStrings.add("x > '0.940897");
        invalidQueryStrings.add(null);
        
        
        for (final String validQuery : validQueryStrings) {
            assertTrue(ExpressionUtilities.testQueryValidity(validQuery));
        }
        
        for (final String invalidQuery : invalidQueryStrings) {
            assertFalse(ExpressionUtilities.testQueryValidity(invalidQuery));
        }
    }
    
}
