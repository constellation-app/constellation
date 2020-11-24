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
package au.gov.asd.tac.constellation.views.qualitycontrol.event;

import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.*;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * QualityControlEvent Test.
 *
 * @author aldebaran30701
 */
public class QualityControlEventNGTest {

    private final static QualityControlEvent.QualityCategory DEFAULT = QualityControlEvent.QualityCategory.DEFAULT;
    private final static QualityControlEvent.QualityCategory INFO = QualityControlEvent.QualityCategory.INFO;
    private final static QualityControlEvent.QualityCategory WARNING = QualityControlEvent.QualityCategory.WARNING;
    private final static QualityControlEvent.QualityCategory SEVERE = QualityControlEvent.QualityCategory.SEVERE;
    private final static QualityControlEvent.QualityCategory FATAL = QualityControlEvent.QualityCategory.FATAL;

    public QualityControlEventNGTest() {
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
     * Test of getCategoryFromString method, of class QualityControlEvent.
     */
    @Test
    public void testGetCategoryFromString() {
        String categoryString = "default";

        // DEFAULT test
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
        categoryString = "DEFAULT";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
        categoryString = "DEFAULTA";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
        categoryString = "ADEFAULT";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);

        // INFO test
        categoryString = "info";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), INFO);
        categoryString = "INFO";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), INFO);
        categoryString = "INFOA";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
        categoryString = "AINFO";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);

        // WARNING test
        categoryString = "warning";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), WARNING);
        categoryString = "WARNING";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), WARNING);
        categoryString = "WARNINGA";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
        categoryString = "AWARNING";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);

        // SEVERE test
        categoryString = "severe";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), SEVERE);
        categoryString = "SEVERE";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), SEVERE);
        categoryString = "SEVEREA";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
        categoryString = "ASEVERE";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);

        // FATAL test
        categoryString = "fatal";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), FATAL);
        categoryString = "FATAL";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), FATAL);
        categoryString = "FATALA";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
        categoryString = "AFATAL";
        assertEquals(QualityControlEvent.getCategoryFromString(categoryString), DEFAULT);
    }

    /**
     * Test of getRuleByString method, of class QualityControlRule.
     *
     */
    @Test
    public void testGetRuleByString() {

        // Test missing type rule
        MissingTypeRule missingRule = new MissingTypeRule();
        String ruleName = missingRule.getName();
        assertEquals(QualityControlEvent.getRuleByString(ruleName).getClass(), missingRule.getClass());

        // Test unknown type rule
        UnknownTypeRule unknownRule = new UnknownTypeRule();
        ruleName = unknownRule.getName();
        assertEquals(QualityControlEvent.getRuleByString(ruleName).getClass(), unknownRule.getClass());

        // Test IdentifierInconsistentWithTypeRule
        IdentifierInconsistentWithTypeRule inconsistentRule = new IdentifierInconsistentWithTypeRule();
        ruleName = inconsistentRule.getName();
        assertEquals(QualityControlEvent.getRuleByString(ruleName).getClass(), inconsistentRule.getClass());

        // Test empty string
        Assert.assertNull(QualityControlEvent.getRuleByString(""));

        // Test blank string
        Assert.assertNull(QualityControlEvent.getRuleByString(" "));
    }
}
