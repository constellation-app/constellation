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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider to get help pages for the data access view
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = HelpPageProvider.class, position = 1000)
@NbBundle.Messages("DataAccessViewHelpProvider=Data Access View Help Provider")
public class DataAccessViewHelpProvider extends HelpPageProvider {
    
    private static final String MODULE_PATH = "ext" + SEP + "docs" + SEP + "CoreDataAccessView" + SEP;

    /**
     * Provides a map of all the help files Maps the file name to the md file name
     *
     * @return Map of the file names vs md file names
     */
    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();

        map.put("au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters", MODULE_PATH + "datetime-range.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.io.ParameterIO", MODULE_PATH + "data-access-options.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.DataAccessViewTopComponent", MODULE_PATH + "data-access-view.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.SplitNodesPlugin", MODULE_PATH + "split-nodes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveUnusedAttributesPlugin", MODULE_PATH + "remove-unused-attributes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractTypesFromTextPlugin", MODULE_PATH + "extract-types-from-text.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractWordsFromTextPlugin", MODULE_PATH + "extract-words-from-text.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ImportGraphFilePlugin", MODULE_PATH + "import-graph-file.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin", MODULE_PATH + "merge-nodes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin", MODULE_PATH + "merge-transactions.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveNodesPlugin", MODULE_PATH + "remove-nodes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.experimental.TestParametersPlugin", MODULE_PATH + "test-parameters.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.experimental.TestParametersBatched", MODULE_PATH + "test-parameters.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.utility.SelectAllPlugin", MODULE_PATH + "select-all.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.utility.SelectTopNPlugin", MODULE_PATH + "select-top-n.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewCategoryPanelController", MODULE_PATH + "data-access-view-preferences.md");
        return map;
    }

    /**
     * Provides a location as a string of the TOC xml file in the module
     *
     * @return List of help resources
     */
    @Override
    public String getHelpTOC() {
        return MODULE_PATH + "dataaccess-toc.xml";
    }
}
