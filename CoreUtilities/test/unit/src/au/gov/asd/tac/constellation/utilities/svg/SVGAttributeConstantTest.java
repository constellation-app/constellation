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

import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author capricornunicorn123
 */
public class SVGAttributeConstantTest {
    
    public SVGAttributeConstantTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getName method, of class SVGAttributeConstant.
     */
    @Test
    public void testGetName() {
        assertEquals(SVGAttributeConstant.BASELINE.getName(),"dominant-baseline");
        assertEquals(SVGAttributeConstant.CLASS.getName(),"class");
        assertEquals(SVGAttributeConstant.CUSTOM_SORT_ORDER.getName(),"data-sort-order");
        assertEquals(SVGAttributeConstant.CX.getName(),"cx");
        assertEquals(SVGAttributeConstant.CY.getName(),"cy");
        assertEquals(SVGAttributeConstant.DESTINATION_X.getName(),"x2");
        assertEquals(SVGAttributeConstant.DESTINATION_Y.getName(),"y2");
        assertEquals(SVGAttributeConstant.DASH_ARRAY.getName(),"stroke-dasharray");
        assertEquals(SVGAttributeConstant.EXTERNAL_RESOURCE_REFERENCE.getName(),"xlink:href");
        assertEquals(SVGAttributeConstant.FILL_COLOR.getName(),"fill");
        assertEquals(SVGAttributeConstant.FILTER.getName(),"filter");
        assertEquals(SVGAttributeConstant.FONT_SIZE.getName(),"font-size");
        assertEquals(SVGAttributeConstant.HEIGHT.getName(),"height");
        assertEquals(SVGAttributeConstant.ID.getName(),"id");
        assertEquals(SVGAttributeConstant.NAME_SPACE.getName(),"xmlns");
        assertEquals(SVGAttributeConstant.POINTS.getName(),"points");
        assertEquals(SVGAttributeConstant.RADIUS.getName(),"r");
        assertEquals(SVGAttributeConstant.SOURCE_X.getName(),"x1");
        assertEquals(SVGAttributeConstant.SOURCE_Y.getName(),"y1");
        assertEquals(SVGAttributeConstant.STROKE_COLOR.getName(),"stroke");
        assertEquals(SVGAttributeConstant.TRANSFORM.getName(),"transform");
        assertEquals(SVGAttributeConstant.VIEW_BOX.getName(),"viewBox");
        assertEquals(SVGAttributeConstant.WIDTH.getName(),"width");
        assertEquals(SVGAttributeConstant.X.getName(),"x");
        assertEquals(SVGAttributeConstant.Y.getName(),"y");
    }

    /**
     * Test of initialiseBasicAttributes method, of class SVGAttributeConstant.
     */
    @Test
    public void testInitialiseBasicAttributes() {
        Map<String, String> attributes = SVGAttributeConstant.initialiseBasicAttributes();
        assertTrue(attributes.containsKey(SVGAttributeConstant.CLASS.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.CX.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.CY.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.DESTINATION_X.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.DESTINATION_Y.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.FILL_COLOR.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.FONT_SIZE.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.HEIGHT.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.ID.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.RADIUS.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.SOURCE_X.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.SOURCE_Y.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.STROKE_COLOR.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.WIDTH.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.X.getName()));
        assertTrue(attributes.containsKey(SVGAttributeConstant.Y.getName()));
 
        attributes.keySet().forEach(key -> {
            assertNull(attributes.get(key));
        });
       

    }
    
}
