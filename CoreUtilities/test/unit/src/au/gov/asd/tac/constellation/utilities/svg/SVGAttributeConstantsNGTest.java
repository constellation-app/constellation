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
package au.gov.asd.tac.constellation.utilities.svg;

import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * Tests for {@link SVGAttibuteConstant}
 * 
 * @author capricornunicorn123
 */
public class SVGAttributeConstantsNGTest {
    
    @BeforeClass
    public static void setUpClass() {
        // Not currently required
    }
    
    @AfterClass
    public static void tearDownClass() {
        // Not currently required
    }
    
    @BeforeMethod
    public void setUpMethod() {
        // Not currently required
    }
    
    @AfterMethod
    public void tearDownMethod() {
        // Not currently required
    }

    /**
     * Test of getName method, of class SVGAttributeConstants.
     */
    @Test
    public void testGetName() {
        assertEquals(SVGAttributeConstants.BASELINE.getName(),"dominant-baseline");
        assertEquals(SVGAttributeConstants.CLASS.getName(),"class");
        assertEquals(SVGAttributeConstants.CUSTOM_SORT_ORDER.getName(),"data-sort-order");
        assertEquals(SVGAttributeConstants.CX.getName(),"cx");
        assertEquals(SVGAttributeConstants.CY.getName(),"cy");
        assertEquals(SVGAttributeConstants.DESTINATION_X.getName(),"x2");
        assertEquals(SVGAttributeConstants.DESTINATION_Y.getName(),"y2");
        assertEquals(SVGAttributeConstants.DASH_ARRAY.getName(),"stroke-dasharray");
        assertEquals(SVGAttributeConstants.EXTERNAL_RESOURCE_REFERENCE.getName(),"href");
        assertEquals(SVGAttributeConstants.FILL_COLOR.getName(),"fill");
        assertEquals(SVGAttributeConstants.FILTER.getName(),"filter");
        assertEquals(SVGAttributeConstants.FONT_SIZE.getName(),"font-size");
        assertEquals(SVGAttributeConstants.HEIGHT.getName(),"height");
        assertEquals(SVGAttributeConstants.ID.getName(),"id");
        assertEquals(SVGAttributeConstants.NAME_SPACE.getName(),"xmlns");
        assertEquals(SVGAttributeConstants.POINTS.getName(),"points");
        assertEquals(SVGAttributeConstants.RADIUS.getName(),"r");
        assertEquals(SVGAttributeConstants.SOURCE_X.getName(),"x1");
        assertEquals(SVGAttributeConstants.SOURCE_Y.getName(),"y1");
        assertEquals(SVGAttributeConstants.STROKE_COLOR.getName(),"stroke");
        assertEquals(SVGAttributeConstants.TRANSFORM.getName(),"transform");
        assertEquals(SVGAttributeConstants.VIEW_BOX.getName(),"viewBox");
        assertEquals(SVGAttributeConstants.WIDTH.getName(),"width");
        assertEquals(SVGAttributeConstants.X.getName(),"x");
        assertEquals(SVGAttributeConstants.Y.getName(),"y");
    }

    /**
     * Test of initialiseBasicAttributes method, of class SVGAttributeConstants.
     */
    @Test
    public void testInitialiseBasicAttributes() {
        Map<String, String> attributes = SVGAttributeConstants.initialiseBasicAttributes();
        assertTrue(attributes.containsKey(SVGAttributeConstants.CLASS.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.CX.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.CY.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.DESTINATION_X.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.DESTINATION_Y.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.FILL_COLOR.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.FONT_SIZE.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.HEIGHT.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.ID.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.RADIUS.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.SOURCE_X.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.SOURCE_Y.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.STROKE_COLOR.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.WIDTH.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.X.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstants.Y.getName()));
 
        attributes.keySet().forEach(key -> {
            assertNull(attributes.get(key));
        });
    } 
}
