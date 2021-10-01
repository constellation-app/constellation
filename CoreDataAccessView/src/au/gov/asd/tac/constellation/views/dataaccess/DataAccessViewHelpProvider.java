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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.help.HelpPageProvider;
import java.io.File;
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

    @Override
    public Map<String, String> getHelpMap() {
        final Map<String, String> map = new HashMap<>();
        final String sep = File.separator;
        final String dataModulePath = ".." + sep + "constellation" + sep + "CoreDataAccessView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd"
                + sep + "tac" + sep + "constellation" + sep + "views" + sep + "dataaccess" + sep + "docs" + sep;

        map.put("au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters", dataModulePath + "datetime-range.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.io.ParameterIO", dataModulePath + "data-access-options.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.panes.DataAccessViewTopComponent", dataModulePath + "data-access-view.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.SplitNodesPlugin", dataModulePath + "split-nodes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveUnusedAttributesPlugin", dataModulePath + "remove-unused-attributes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractTypesFromTextPlugin", dataModulePath + "extract-types-from-text.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractWordsFromTextPlugin", dataModulePath + "extract-words-from-text.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin", dataModulePath + "merge-nodes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin", dataModulePath + "merge-transactions.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveNodesPlugin", dataModulePath + "remove-nodes.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.experimental.TestParametersPlugin", dataModulePath + "test-parameters.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.utility.SelectAllPlugin", dataModulePath + "select-all.md");
        map.put("au.gov.asd.tac.constellation.views.dataaccess.plugins.utility.SelectTopNPlugin", dataModulePath + "select-top-n.md");
        return map;
    }

    @Override
    public String getHelpTOC() {
        final String sep = File.separator;
        final String dataViewPath;
        dataViewPath = "constellation" + sep + "CoreDataAccessView" + sep + "src" + sep + "au" + sep + "gov" + sep + "asd" + sep + "tac" + sep
                + "constellation" + sep + "views" + sep + "dataaccess" + sep + "docs" + sep + "dataaccess-toc.xml";
        return dataViewPath;
    }
}
