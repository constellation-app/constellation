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
package au.gov.asd.tac.constellation.help.utilities.toc;

import static junit.framework.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author aldebaran30701
 */
public class TOCItemNGTest {

    public TOCItemNGTest() {
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
     * Test of toString method, of class TOCItem.
     */
    @Test
    public void testToString() {
        System.out.println("toString");

        final String text = "text";
        final String target = "target";
        final TOCItem instance = new TOCItem(text, target);
        final String expResult = text + " -> " + target;
        final String result = instance.toString();
        assertEquals(result, expResult);

        final String text1 = null;
        final String target1 = "target";
        final TOCItem instance1 = new TOCItem(text1, target1);
        final String expResult1 = text1 + " -> " + target1;
        final String result1 = instance1.toString();
        assertEquals(result1, expResult1);

        final String text2 = null;
        final String target2 = null;
        final TOCItem instance2 = new TOCItem(text2, target2);
        final String expResult2 = text2 + " -> " + target2;
        final String result2 = instance2.toString();
        assertEquals(result2, expResult2);
    }

    /**
     * Test of equals method, of class TOCItem.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");

        final String text = "text";
        final String target = "target";
        final TOCItem instance = new TOCItem(text, target);
        final TOCItem comparedInstance = new TOCItem(text, target);
        final boolean expResult = true;
        final boolean result = instance.equals(comparedInstance);
        assertEquals(result, expResult);

        final String text1 = null;
        final String target1 = "target";
        final TOCItem instance1 = new TOCItem(text1, target1);
        final TOCItem comparedInstance1 = new TOCItem(text1, target1);
        final boolean expResult1 = true;
        final boolean result1 = instance1.equals(comparedInstance1);
        assertEquals(result1, expResult1);

        final String text2 = "text";
        final String target2 = null;
        final TOCItem instance2 = new TOCItem(text2, target2);
        final TOCItem comparedInstance2 = new TOCItem(text2, target2);
        final boolean expResult2 = true;
        final boolean result2 = instance2.equals(comparedInstance2);
        assertEquals(result2, expResult2);

        final String text3 = null;
        final String target3 = null;
        final TOCItem instance3 = new TOCItem(text3, target3);
        final TOCItem comparedInstance3 = new TOCItem(text3, target3);
        final boolean expResult3 = true;
        final boolean result3 = instance3.equals(comparedInstance3);
        assertEquals(result3, expResult3);

        final String text4 = null;
        final String target4 = null;
        final TOCItem comparedInstance4 = null;
        final TOCItem instance4 = new TOCItem(text4, target4);
        final boolean expResult4 = false;
        final boolean result4 = instance4.equals(comparedInstance4);
        assertEquals(result4, expResult4);

        final String text5 = "text";
        final String target5 = "target";
        final Object comparedInstance5 = new Integer("555");
        final TOCItem instance5 = new TOCItem(text5, target5);
        final boolean expResult5 = false;
        final boolean result5 = instance5.equals(comparedInstance5);
        assertEquals(result5, expResult5);

        final String text6 = "text";
        final String target6 = "target";
        final TOCItem instance6 = new TOCItem(text6, target6);
        final TOCItem comparedInstance6 = new TOCItem("not the same", target6);
        final boolean expResult6 = false;
        final boolean result6 = instance6.equals(comparedInstance6);
        assertEquals(result6, expResult6);

        final String text7 = "text";
        final String target7 = "target";
        final TOCItem instance7 = new TOCItem(text7, target7);
        final TOCItem comparedInstance7 = new TOCItem(text7, "not the same");
        final boolean expResult7 = false;
        final boolean result7 = instance7.equals(comparedInstance7);
        assertEquals(result7, expResult7);
    }
}
