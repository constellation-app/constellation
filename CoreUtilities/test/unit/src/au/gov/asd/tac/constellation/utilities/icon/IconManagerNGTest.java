/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.icon;

import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.util.Set;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Icon Manager Test.
 *
 * @author arcturus
 */
public class IconManagerNGTest {

    public IconManagerNGTest() {
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

    @Test
    public void testGetIcon_StringPerformancetest() {
        final IconManager im = new IconManager();

        int total = 0;

        final long start = System.currentTimeMillis();
        for (int i = 0; i <= 10000; i++) {
            total += im.getIcon("Server").getExtendedName().length();
        }
        final long end = System.currentTimeMillis();

        System.out.println("Total = " + total);
        System.out.println("Diff is " + (end - start));

        // TODO: check why the performance improvement is not showing as being quicker than with the previous implementaion.
        if ((end - start) > 3000) {
            fail("IconManager.getIcon() took longer than 3 seconds to load 10,000 icons");
        }
    }

    /**
     * Test of getIconObjects method, of class IconManager.
     */
    @Test
    public void testGetIconObjects() {
        Set firstTime = IconManager.getIcons();
        Set secondTime = IconManager.getIcons();
        assertEquals(firstTime, secondTime);
    }

    @Test
    public void testGetIconObjectsPerformanceTest() {
        final IconManager im = new IconManager();

        int total = 0;

        final long start = System.currentTimeMillis();
        for (int i = 0; i <= 1000; i++) {
            total += im.getIcons().size();
        }
        final long end = System.currentTimeMillis();

        System.out.println("Total = " + total);
        System.out.println("Diff is " + (end - start));
    }
}
