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
package au.gov.asd.tac.constellation.graph.attribute.interaction;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
public class ZonedDateTimeAttributeInteractionNGTest {
    
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
     * Test of getDisplayText method, of class ZonedDateTimeAttributeInteraction.
     */
    @Test
    public void testGetDisplayText() {
        System.out.println("getDisplayText");
        
        final ZonedDateTimeAttributeInteraction instance = new ZonedDateTimeAttributeInteraction();
        
        assertNull(instance.getDisplayText(null));
        assertEquals(instance.getDisplayText(ZonedDateTime.of(1999, 12, 31, 11, 59, 59, 0, ZoneId.of("UTC"))), "1999-12-31 11:59:59 +00:00 [UTC]");
        assertEquals(instance.getDisplayText(ZonedDateTime.of(1999, 12, 31, 11, 59, 59, 999999999, ZoneId.of("UTC"))), "1999-12-31 11:59:59.999 +00:00 [UTC]");
    }
}
