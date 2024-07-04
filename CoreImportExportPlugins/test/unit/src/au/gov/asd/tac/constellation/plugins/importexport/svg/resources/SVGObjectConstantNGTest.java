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
package au.gov.asd.tac.constellation.plugins.importexport.svg.resources;

import au.gov.asd.tac.constellation.utilities.svg.SVGData;
import au.gov.asd.tac.constellation.utilities.svg.SVGObject;
import au.gov.asd.tac.constellation.utilities.svg.SVGTypeConstants;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link SVGobjectConstant}
 * 
 * @author capricornunicorn123
 */
public class SVGObjectConstantNGTest {
    
    public SVGObjectConstantNGTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @BeforeMethod
    public void setUp() {
    }
    
    @AfterMethod
    public void tearDown() {
    }

    /**
     * Test of findIn(), removeFrom(), of class SVGObjectConstants.
     */
    @Test
    public void testFindIn() {
        SVGTypeConstants typeSVG = SVGTypeConstants.SVG;
        final SVGObject obj1 = new SVGObject(new SVGData(typeSVG, null, null));
        final SVGObject obj2 = new SVGObject(new SVGData(typeSVG, null, null));
        obj1.setID("parent");
                
        for (SVGObjectConstants constant : SVGObjectConstants.values()){
            obj2.setID(constant.idValue);
            obj2.setParent(obj1);
            assertEquals(constant.findIn(obj1).toSVGData().toString(), obj2.toSVGData().toString());
            constant.removeFrom(obj1);
            assertNull(constant.findIn(obj1));   
        }
    }
}
