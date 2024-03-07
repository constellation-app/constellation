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
package au.gov.asd.tac.constellation.graph.attribute;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author arcturus
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class HyperlinkAttributeDescriptionNGTest extends ConstellationTest {

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
     * @throws java.net.MalformedURLException
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testConvertFromObject() throws MalformedURLException, URISyntaxException {
        System.out.println("convertFromObject");
        
        assertNull(instance.convertFromObject(null));
        final URL goodUrl = new URL("https://my.good.url");
        assertEquals(instance.convertFromObject(goodUrl), goodUrl.toURI());
    }
    
    /**
     * Test of convertFromObject method, of class HyperlinkAttributeDescription. Bad URL
     * @throws java.net.MalformedURLException
     * @throws java.net.URISyntaxException
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Error converting Object \'class java.net.URL\' to hyperlink")
    public void testConvertFromObjectBadURL() throws MalformedURLException, URISyntaxException {
        System.out.println("convertFromObjectBadURL");
        
        final URL badUrl = new URL("https://mybadurl^");
        instance.convertFromObject(badUrl);
    }
    
    /**
     * Test of convertFromObject method, of class HyperlinkAttributeDescription. Not a URL
     * @throws java.net.MalformedURLException
     * @throws java.net.URISyntaxException
     */
    @Test(expectedExceptions = IllegalArgumentException.class, 
            expectedExceptionsMessageRegExp = "Error converting Object \'class java.time.LocalDate\' to class java.net.URI")
    public void testConvertFromObjectNotAURL() throws MalformedURLException, URISyntaxException {
        System.out.println("convertFromObjectNotAURL");
        
        instance.convertFromObject(LocalDate.of(1999, 12, 31));
    }

    /**
     * Test of convertFromString method, of class HyperlinkAttributeDescription.
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testConvertFromString() throws URISyntaxException {
        System.out.println("convertFromString");
        
        assertNull(instance.convertFromString(""));
        assertEquals(instance.convertFromString("https://my.good.url"), new URI("https://my.good.url"));
    }
    
    /**
     * Test of convertFromString method, of class HyperlinkAttributeDescription. Bad URI
     * @throws java.net.URISyntaxException
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testConvertFromStringBadURI() throws URISyntaxException {
        System.out.println("convertFromStringBadURI");
        
        instance.convertFromString("https://mybadurl^");
    }
}
