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
package au.gov.asd.tac.constellation.views.dataaccess.plugins;

import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeNodesPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.MergeTransactionsPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveNodesPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.RemoveUnusedAttributesPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.clean.SplitNodesPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractTypesFromTextPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ExtractWordsFromTextPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ImportGraphFilePlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.utility.SelectAllPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.utility.SelectTopNPlugin;

/**
 *
 * @author cygnus_x-1
 */
public class DataAccessPluginRegistry {

    private DataAccessPluginRegistry() {
    }

    public static final String EXTRACT_TYPES_FROM_TEXT = ExtractTypesFromTextPlugin.class.getName();
    public static final String EXTRACT_WORDS_FROM_TEXT = ExtractWordsFromTextPlugin.class.getName();
    public static final String IMPORT_GRAPH_FILE = ImportGraphFilePlugin.class.getName();
    public static final String MERGE_NODES = MergeNodesPlugin.class.getName();
    public static final String MERGE_TRANSACTIONS = MergeTransactionsPlugin.class.getName();
    public static final String REMOVE_NODES = RemoveNodesPlugin.class.getName();
    public static final String REMOVE_UNUSED_ATTRIBUTES = RemoveUnusedAttributesPlugin.class.getName();
    public static final String SELECT_ALL = SelectAllPlugin.class.getName();
    public static final String SELECT_TOP_N = SelectTopNPlugin.class.getName();
    public static final String SPLIT_NODES = SplitNodesPlugin.class.getName();
}
