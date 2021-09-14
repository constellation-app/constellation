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
package au.gov.asd.tac.constellation.graph.interaction;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the interactive graph module
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class)
@NbBundle.Messages("InteractiveGraphHelpProvider=Interactive Graph Help Provider")
public class InteractiveGraphHelpProvider extends HelpPageProvider {

    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String interactiveModulePath = ".." + sep + "constellation" + sep + "CoreInteractiveGraph" + sep + "src" + sep + "au" + sep + "gov"
                + sep + "asd" + sep + "tac" + sep + "constellation" + sep + "graph" + sep + "interaction" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.graph.interaction.addselectmode", interactiveModulePath + "add-and-selection-modes.md");
        map.put("au.gov.asd.tac.constellation.graph.interaction.cutcopypaste", interactiveModulePath + "cut-copy-paste.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String interactiveGraphPath;
        interactiveGraphPath = "constellation" + sep + "CoreInteractiveGraph" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac"
                + sep + "constellation" + sep + "graph" + sep + "interaction" + sep + "docs" + sep + "interactivegraph-toc.xml";
        return interactiveGraphPath;
    }
}
