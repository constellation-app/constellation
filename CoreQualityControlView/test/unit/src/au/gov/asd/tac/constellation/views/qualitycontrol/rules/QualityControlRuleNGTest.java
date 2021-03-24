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
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.FATAL,
                QualityControlEvent.QualityCategory.FATAL), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.SEVERE), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.WARNING,
                QualityControlEvent.QualityCategory.WARNING), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.INFO,
                QualityControlEvent.QualityCategory.INFO), catEqual);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.DEFAULT,
                QualityControlEvent.QualityCategory.DEFAULT), catEqual);

        // Test category 1 is higher
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.FATAL,
                QualityControlEvent.QualityCategory.SEVERE), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.WARNING), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.WARNING,
                QualityControlEvent.QualityCategory.INFO), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.INFO,
                QualityControlEvent.QualityCategory.DEFAULT), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.FATAL,
                QualityControlEvent.QualityCategory.WARNING), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.INFO), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.WARNING,
                QualityControlEvent.QualityCategory.DEFAULT), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.FATAL,
                QualityControlEvent.QualityCategory.INFO), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.DEFAULT), cat1Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.FATAL,
                QualityControlEvent.QualityCategory.DEFAULT), cat1Higher);

        // Test category 2 is higher
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.SEVERE,
                QualityControlEvent.QualityCategory.FATAL), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.WARNING,
                QualityControlEvent.QualityCategory.SEVERE), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.INFO,
                QualityControlEvent.QualityCategory.WARNING), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.DEFAULT,
                QualityControlEvent.QualityCategory.INFO), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.WARNING,
                QualityControlEvent.QualityCategory.FATAL), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.INFO,
                QualityControlEvent.QualityCategory.SEVERE), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.DEFAULT,
                QualityControlEvent.QualityCategory.WARNING), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.INFO,
                QualityControlEvent.QualityCategory.FATAL), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.DEFAULT,
                QualityControlEvent.QualityCategory.SEVERE), cat2Higher);
        assertEquals(QualityControlRule.testPriority(QualityControlEvent.QualityCategory.DEFAULT,
                QualityControlEvent.QualityCategory.FATAL), cat2Higher);
    }

    /**
     * Test of getCategoryByScore method, of class QualityControlRule.
     *
     */
    @Test
    public void testGetCategoryByScore() {
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.DEFAULT_VALUE - 1),
                QualityControlEvent.QualityCategory.DEFAULT); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.DEFAULT_VALUE),
                QualityControlEvent.QualityCategory.DEFAULT); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.DEFAULT_VALUE + 1),
                QualityControlEvent.QualityCategory.DEFAULT); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.INFO_VALUE - 1),
                QualityControlEvent.QualityCategory.DEFAULT); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.INFO_VALUE),
                QualityControlEvent.QualityCategory.INFO); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.INFO_VALUE + 1),
                QualityControlEvent.QualityCategory.INFO); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.WARNING_VALUE - 1),
                QualityControlEvent.QualityCategory.INFO); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.WARNING_VALUE),
                QualityControlEvent.QualityCategory.WARNING); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.WARNING_VALUE + 1),
                QualityControlEvent.QualityCategory.WARNING); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.SEVERE_VALUE - 1),
                QualityControlEvent.QualityCategory.WARNING); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.SEVERE_VALUE),
                QualityControlEvent.QualityCategory.SEVERE); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.SEVERE_VALUE + 1),
                QualityControlEvent.QualityCategory.SEVERE); // ONE ABOVE

        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.FATAL_VALUE - 1),
                QualityControlEvent.QualityCategory.SEVERE); // ONE BELOW
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.FATAL_VALUE),
                QualityControlEvent.QualityCategory.FATAL); // ON VALUE
        assertEquals(QualityControlRule.getCategoryByScore(QualityControlEvent.FATAL_VALUE + 1),
                QualityControlEvent.QualityCategory.FATAL); // ONE ABOVE
    }
}
