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
package au.gov.asd.tac.constellation.graph.attribute.interaction;

import java.net.URI;
import java.net.URISyntaxException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class HyperlinkAttributeInteractionNGTest {
    
    public HyperlinkAttributeInteractionNGTest() {
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
     * Test of getDisplayText method, of class HyperlinkAttributeInteraction.
     * @throws java.net.URISyntaxException
     */
    @Test
    public void testGetDisplayText() throws URISyntaxException {
        System.out.println("getDisplayText");
        
        final HyperlinkAttributeInteraction instance = new HyperlinkAttributeInteraction();
        
        assertNull(instance.getDisplayText(null));
        assertEquals(instance.getDisplayText(new URI("not.real.uri")), "not.real.uri");
    }
}
