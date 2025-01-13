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
package au.gov.asd.tac.constellation.help.utilities;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.Lookup;
import static org.testng.Assert.assertEquals;
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
     * Test of getHelpAddress method, of class HelpMapper.
     */
    @Test
    public void testGetHelpAddress() {
        testGetMappings();
        String className = "au.gov.asd.tac.constellation.help.preferences.HelpOptionsPanelController";
        final String sep = File.separator;
        String expResult = ".." + sep + "ext" + sep + "docs" + sep + "CoreHelp" + sep + "src" + sep + "au" + sep + "gov" + sep
                + "asd" + sep + "tac" + sep + "constellation" + sep + "help" + sep + "help-options.md";
        String result = HelpMapper.getHelpAddress(className);
        assertEquals(result, expResult);
    }

    /**
     * Test of getMappings method, of class HelpMapper.
     */
    public void testGetMappings() {
        Map<String, String> expResult = new HashMap<>();
        Lookup.getDefault().lookupAll(HelpPageProvider.class).forEach(provider -> {
            expResult.putAll(provider.getHelpMap());
        });

        Map<String, String> result = HelpMapper.getMappings();
        assertEquals(result, expResult);
    }
}
