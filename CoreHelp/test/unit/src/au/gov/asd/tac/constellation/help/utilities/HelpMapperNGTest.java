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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for HelpMapper
 *
 * @author Delphinus8821
 */
public class HelpMapperNGTest {

    public HelpMapperNGTest() {
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
     * Test of getHelpAddress method, of class HelpMapper.
     */
    @Test
    public void testGetHelpAddress() {
        testGetMappings();
        String className = "au.gov.asd.tac.constellation.help.preferences.HelpOptionsPanelController";
        final String sep = File.separator;
        String expResult = ".." + sep + "constellation" + sep + "CoreHelp" + sep + "src" + sep + "au" + sep + "gov" + sep
                + "asd" + sep + "tac" + sep + "constellation" + sep + "help" + sep + "docs" + sep + "help-options.md";
        String result = HelpMapper.getHelpAddress(className);
        assertEquals(result, expResult);
    }

    /**
     * Test of getMappings method, of class HelpMapper.
     */
    public void testGetMappings() {
        Map expResult = new HashMap();
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> {
            expResult.putAll(provider.getHelpMap());
        });

        Map result = HelpMapper.getMappings();
        assertEquals(result, expResult);
    }
}
