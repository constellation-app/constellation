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
package au.gov.asd.tac.constellation.utilities.temporal;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class TimeZoneUtilitiesNGTest {
    
    public TimeZoneUtilitiesNGTest() {
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
     * Test of ZONE_ID_COMPARATOR, of class TimeZoneUtilties.
     */
    @Test
    public void testComparator() {
        // offset smaller than UTC
        final int compare1 = TimeZoneUtilities.ZONE_ID_COMPARATOR.compare(ZoneId.of("-10:00"), TimeZoneUtilities.UTC);
        assertTrue(compare1 < 0);
        
        // offset larger than UTC
        final int compare2 = TimeZoneUtilities.ZONE_ID_COMPARATOR.compare(ZoneId.of("+10:00"), TimeZoneUtilities.UTC);
        assertTrue(compare2 > 0);
        
        // offset equal to UTC (therefore comes down to id)
        final int compare3 = TimeZoneUtilities.ZONE_ID_COMPARATOR.compare(ZoneId.of("+00:00"), TimeZoneUtilities.UTC);
        assertTrue(compare3 > 0);
    }

    /**
     * Test of getTimeZoneAsString method, of class TimeZoneUtilities. One parameter version
     */
    @Test
    public void testGetTimeZoneAsStringOneParameter() {
        System.out.println("getTimeZoneAsStringOneParameter");

        final String result1 = TimeZoneUtilities.getTimeZoneAsString(null);
        assertEquals(result1, null);
        
        try (final MockedStatic<LocalDateTime> localDateTimeMockedStatic = Mockito.mockStatic(LocalDateTime.class);) {
            localDateTimeMockedStatic.when(() -> LocalDateTime.of(anyInt(), any(Month.class), anyInt(), anyInt(), anyInt(), anyInt()))
                    .thenCallRealMethod();
            final LocalDateTime localDateTime  = LocalDateTime.of(2000, Month.JANUARY, 1, 2, 34, 45);
            
            localDateTimeMockedStatic.when(() -> LocalDateTime.now()).thenReturn(localDateTime);
            
            final String result2 = TimeZoneUtilities.getTimeZoneAsString(ZoneId.of("+10:00"));
            assertEquals(result2, "+10:00");
            
            final String result3 = TimeZoneUtilities.getTimeZoneAsString(TimeZoneUtilities.UTC);
            assertEquals(result3, "+00:00 [UTC]");
        }
    }

    /**
     * Test of getTimeZoneAsString method, of class TimeZoneUtilities. Two parameter version
     */
    @Test
    public void testGetTimeZoneAsStringTwoParameters() {
        System.out.println("getTimeZoneAsStringTwoParameters");
        
        final LocalDateTime localDateTime  = LocalDateTime.of(1999, Month.JULY, 5, 4, 32, 10);
        final String result1 = TimeZoneUtilities.getTimeZoneAsString(localDateTime, ZoneId.of("+10:00"));
        assertEquals(result1, "+10:00");
        
        final String result2 = TimeZoneUtilities.getTimeZoneAsString(localDateTime, TimeZoneUtilities.UTC);
        assertEquals(result2, "+00:00 [UTC]");
    }
    
}
