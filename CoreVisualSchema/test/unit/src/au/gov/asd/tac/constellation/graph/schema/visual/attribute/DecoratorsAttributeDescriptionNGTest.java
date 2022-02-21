/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class DecoratorsAttributeDescriptionNGTest {

    public DecoratorsAttributeDescriptionNGTest() {
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
     * Test of convertFromString method, of class DecoratorsAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        System.out.println("convertFromString");

        final DecoratorsAttributeDescription instance = new DecoratorsAttributeDescription();

        final VertexDecorators nullResult = instance.convertFromString(null);
        // should be the default here
        assertEquals(nullResult, VertexDecorators.NO_DECORATORS);

        final VertexDecorators blankResult = instance.convertFromString("   ");
        // should be the default here as well
        assertEquals(blankResult, VertexDecorators.NO_DECORATORS);

        final VertexDecorators validResult = instance.convertFromString("\"decorator1\";\"decorator2\";\"decorator3\";\"decorator4\";");
        final VertexDecorators expResult = new VertexDecorators("decorator1", "decorator2", "decorator3", "decorator4");
        assertEquals(validResult.getNorthEastDecoratorAttribute(), expResult.getNorthEastDecoratorAttribute());
        assertEquals(validResult.getNorthWestDecoratorAttribute(), expResult.getNorthWestDecoratorAttribute());
        assertEquals(validResult.getSouthEastDecoratorAttribute(), expResult.getSouthEastDecoratorAttribute());
        assertEquals(validResult.getSouthWestDecoratorAttribute(), expResult.getSouthWestDecoratorAttribute());
    }

}