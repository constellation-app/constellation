/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.svg;

import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author capricornunicorn123
 */
public class SVGTypeConstantsNGTest {
    
    public SVGTypeConstantsNGTest() {
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
     * Test of getTypeString(), getType(), of class SVGTypeConstants.
     */
    @Test
    public void testGetTypeString() {
        for (SVGTypeConstants constant : SVGTypeConstants.values()){
            assertEquals(SVGTypeConstants.getType(constant.getTypeString()), constant);
        }
        assertNull(SVGTypeConstants.getType(null));
        
        //The below test is failing in CI but passes localy. Test is required to Have 100% code coverage for this class.
        //CI ir reporting "Error" with no logs so may be un unrelated falure.
        assertNull(SVGTypeConstants.getType("Not A SVG Type"));
    }    
}
