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
package au.gov.asd.tac.constellation.plugins.arrangements;

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
public class ArrangementPluginsHelpProviderNGTest {

    private static final String CODEBASE_NAME = "constellation";
    private static final String SEP = File.separator;

    /**
     * Test of getHelpMap method, of class ArrangementPluginsHelpProvider.
     */
    @Test
    public void testGetHelpMap() {
        System.out.println("getHelpMap");

        final Map<String, String> expResult = new HashMap<>();
        final String arrangementModulePath = ".." + SEP + "ext" + SEP + "docs" + SEP + "CoreArrangementPlugins" + SEP + "src" + SEP + "au" + SEP + "gov"
                + SEP + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "plugins" + SEP + "arrangements" + SEP;

        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.grid", arrangementModulePath + "grid-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.line", arrangementModulePath + "line-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.hierarchy", arrangementModulePath + "hierarchy-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.tree", arrangementModulePath + "tree-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.circle", arrangementModulePath + "circle-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.scatter3d", arrangementModulePath + "scatter3d-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.sphere", arrangementModulePath + "sphere-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.flattenZField", arrangementModulePath + "flatten-z-field.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.contractGraph", arrangementModulePath + "contract-graph.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.expandGraph", arrangementModulePath + "expand-graph.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.layerByTime", arrangementModulePath + "layer-by-time.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.nodeAttribute", arrangementModulePath + "node-attribute-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.bubbleTree3d", arrangementModulePath + "bubble-tree-3d-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.proximity", arrangementModulePath + "proximity-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.spectral", arrangementModulePath + "spectral-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.hde", arrangementModulePath + "hde-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.uncollide", arrangementModulePath + "uncollide-arrangement.md");
        expResult.put("au.gov.asd.tac.constellation.plugins.arrangements.pinUnpin", arrangementModulePath + "pin-unpin-nodes.md");

        ArrangementPluginsHelpProvider instance = new ArrangementPluginsHelpProvider();

        Map<String, String> result = instance.getHelpMap();
        assertTrue(expResult.equals(result));

    }

    /**
     * Test of getHelpTOC method, of class ArrangementPluginsHelpProvider.
     */
    @Test
    public void testGetHelpTOC() {
        System.out.println("getHelpTOC");

        ArrangementPluginsHelpProvider instance = new ArrangementPluginsHelpProvider();
        final String expResult = "ext" + SEP + "docs" + SEP + "CoreArrangementPlugins" + SEP + "src" + SEP + "au" + SEP
                + "gov" + SEP + "asd" + SEP + "tac" + SEP + CODEBASE_NAME + SEP + "plugins" + SEP + "arrangements" + SEP
                + "arrangements-toc.xml";

        assertEquals(instance.getHelpTOC(), expResult);

    }
}
