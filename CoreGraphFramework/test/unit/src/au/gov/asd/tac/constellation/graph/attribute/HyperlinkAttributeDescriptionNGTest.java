/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.attribute;

import java.net.URI;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author arcturus
 */
public class HyperlinkAttributeDescriptionNGTest {

    HyperlinkAttributeDescription instance;

    public HyperlinkAttributeDescriptionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new HyperlinkAttributeDescription();
        instance.setCapacity(1);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of convertFromObject method, of class HyperlinkAttributeDescription.
     */
    @Test
    public void testConvertFromObject() {
        Object object = null;
        URI expResult = null;
        URI result = instance.convertFromObject(object);
        assertEquals(result, expResult);
    }

    /**
     * Test of convertFromString method, of class HyperlinkAttributeDescription.
     */
    @Test
    public void testConvertFromString() {
        String string = "";
        URI expResult = null;
        URI result = instance.convertFromString(string);
        assertEquals(result, expResult);
    }

}
