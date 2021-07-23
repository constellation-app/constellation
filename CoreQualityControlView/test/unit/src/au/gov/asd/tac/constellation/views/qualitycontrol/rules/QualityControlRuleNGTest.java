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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * QualityControlRule Test.
 *
 * @author aldebaran30701
 */
public class QualityControlRuleNGTest {

    public QualityControlRuleNGTest() {
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
     * Test of testPriority method, of class QualityControlRule.
     */
    @Test
    public void testPriority() {
        final int cat1Higher = -1;
        final int catEqual = 0;
        final int cat2Higher = 1;

        // Test categories are equal
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.CRITICAL,
                QualityControlEvent.QualityCategory.CRITICAL), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.SEVERE), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MAJOR,
                QualityControlEvent.QualityCategory.MAJOR), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MEDIUM,
                QualityControlEvent.QualityCategory.MEDIUM), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MINOR,
                QualityControlEvent.QualityCategory.MINOR), catEqual);

        // Test category 1 is higher
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.CRITICAL,
                QualityControlEvent.QualityCategory.SEVERE), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.MAJOR), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MAJOR,
                QualityControlEvent.QualityCategory.MEDIUM), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MEDIUM,
                QualityControlEvent.QualityCategory.MINOR), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.CRITICAL,
                QualityControlEvent.QualityCategory.MAJOR), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.MEDIUM), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MAJOR,
                QualityControlEvent.QualityCategory.MINOR), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.CRITICAL,
                QualityControlEvent.QualityCategory.MEDIUM), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.MINOR), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.CRITICAL,
                QualityControlEvent.QualityCategory.MINOR), cat1Higher);

        // Test category 2 is higher
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.CRITICAL), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MAJOR,
                QualityControlEvent.QualityCategory.SEVERE), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MEDIUM,
                QualityControlEvent.QualityCategory.MAJOR), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MINOR,
                QualityControlEvent.QualityCategory.MEDIUM), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MAJOR,
                QualityControlEvent.QualityCategory.CRITICAL), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MEDIUM,
                QualityControlEvent.QualityCategory.SEVERE), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MINOR,
                QualityControlEvent.QualityCategory.MAJOR), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MEDIUM,
                QualityControlEvent.QualityCategory.CRITICAL), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MINOR,
                QualityControlEvent.QualityCategory.SEVERE), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.MINOR,
                QualityControlEvent.QualityCategory.CRITICAL), cat2Higher);
    }

    /**
     * Test of getCategoryByScore method, of class QualityControlRule.
     *
     */
    @Test
    public void testGetCategoryByScore() {
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MINOR_VALUE - 1),
                QualityControlEvent.QualityCategory.OK); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MINOR_VALUE),
                QualityControlEvent.QualityCategory.MINOR); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MINOR_VALUE + 1),
                QualityControlEvent.QualityCategory.MINOR); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MEDIUM_VALUE - 1),
                QualityControlEvent.QualityCategory.MINOR); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MEDIUM_VALUE),
                QualityControlEvent.QualityCategory.MEDIUM); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MEDIUM_VALUE + 1),
                QualityControlEvent.QualityCategory.MEDIUM); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MAJOR_VALUE - 1),
                QualityControlEvent.QualityCategory.MEDIUM); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MAJOR_VALUE),
                QualityControlEvent.QualityCategory.MAJOR); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.MAJOR_VALUE + 1),
                QualityControlEvent.QualityCategory.MAJOR); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.SEVERE_VALUE - 1),
                QualityControlEvent.QualityCategory.MAJOR); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.SEVERE_VALUE),
                QualityControlEvent.QualityCategory.SEVERE); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.SEVERE_VALUE + 1),
                QualityControlEvent.QualityCategory.SEVERE); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.CRITICAL_VALUE - 1),
                QualityControlEvent.QualityCategory.SEVERE); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.CRITICAL_VALUE),
                QualityControlEvent.QualityCategory.CRITICAL); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.CRITICAL_VALUE + 1),
                QualityControlEvent.QualityCategory.CRITICAL); // ONE ABOVE
    }
}
