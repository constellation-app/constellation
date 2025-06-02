/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.errorreport;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class ErrorReportHelpProviderNGTest {

    private static final String CODEBASE_NAME = "constellation";
    private static final String SEP = File.separator;

    /**
     * Test of getHelpMap method, of class ErrorReportHelpProvider.
     */
    @Test
    public void testGetHelpMap() {
        System.out.println("getHelpMap");
        
        final Map<String, String> expResult = new HashMap<>();
        final String dataModulePath = ".." + SEP + "ext" + SEP + "docs" + SEP + "CoreErrorReportView" + SEP + "src"
                + SEP + "au" + SEP + "gov" + SEP + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "views" + SEP + "errorreport" + SEP;

        expResult.put("au.gov.asd.tac.constellation.views.errorreport", dataModulePath + "error-report.md");

        final ErrorReportHelpProvider instance = new ErrorReportHelpProvider();

        final Map<String, String> result = instance.getHelpMap();
        assertTrue(expResult.equals(result));
    }

    /**
     * Test of getHelpTOC method, of class ErrorReportHelpProvider.
     */
    @Test
    public void testGetHelpTOC() {
        System.out.println("getHelpTOC");
        
        final ErrorReportHelpProvider instance = new ErrorReportHelpProvider();
        final String expResult = "ext" + SEP + "docs" + SEP + "CoreErrorReportView" + SEP + "src" + SEP + "au" + SEP
                + "gov" + SEP + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "views" + SEP + "errorreport" + SEP
                + "errorreport-toc.xml";

        assertEquals(instance.getHelpTOC(), expResult);
    }
}
