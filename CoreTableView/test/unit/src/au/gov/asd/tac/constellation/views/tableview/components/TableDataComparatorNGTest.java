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
package au.gov.asd.tac.constellation.views.tableview.components;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author OrionsGuardian
 */
public class TableDataComparatorNGTest {
    
    @Test
    public void testNumberStrings() {
        TableDataComparator tdc = new TableDataComparator();
        int result = tdc.compare("1", "7");
        assertEquals(-1, result);
        result = tdc.compare("7", "1");
        assertEquals(1, result);
        result = tdc.compare("3", "3");
        assertEquals(0, result);
        result = tdc.compare("111", "44");
        assertEquals(1, result);
    }

    @Test
    public void testAlphaNumericStrings() {
        TableDataComparator tdc = new TableDataComparator();
        int result = tdc.compare("A1", "A5");
        assertEquals(-1, result);
        result = tdc.compare("A5", "A1");
        assertEquals(1, result);
        result = tdc.compare("A3", "A3");
        assertEquals(0, result);
        result = tdc.compare("A111", "A44");
        assertEquals(1, result);
    }

    @Test
    public void testAlphaStrings() {
        TableDataComparator tdc = new TableDataComparator();
        int result = tdc.compare("AB", "ABD");
        assertTrue(result < 0);
        result = tdc.compare("ABDE", "ABC");
        assertTrue(result > 0);
        result = tdc.compare("ABCD", "AZ");
        assertTrue(result < 0);
        result = tdc.compare("A", "A");
        assertEquals(0, result);
        result = tdc.compare("ABC", "abc");
        assertTrue(result < 0);
        result = tdc.compare("ABD", "123");
        assertTrue(result > 0);
    }
}
