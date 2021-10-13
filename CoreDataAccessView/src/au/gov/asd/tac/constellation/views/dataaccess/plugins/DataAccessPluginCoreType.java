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
package au.gov.asd.tac.constellation.views.dataaccess.plugins;

import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author algol
 */
@ServiceProvider(service = DataAccessPluginType.class)
public class DataAccessPluginCoreType implements DataAccessPluginType {

    public static final String FAVOURITES = "Favourites";
    public static final String IMPORT = "Import";
    public static final String EXTEND = "Extend";
    public static final String CLEAN = "Clean";
    public static final String WORKFLOW = "Workflow";
    public static final String UTILITY = "Utility";
    public static final String EXPERIMENTAL = "Experimental";
    public static final String DEVELOPER = "Developer";
    public static final String ENRICHMENT = "Enrichment";

    @Override
    public List<PositionalDataAccessPluginType> getPluginTypeList() {
        final List<PositionalDataAccessPluginType> pluginTypeList = new ArrayList<>();
        pluginTypeList.add(new PositionalDataAccessPluginType(FAVOURITES, Integer.MIN_VALUE));
        pluginTypeList.add(new PositionalDataAccessPluginType(IMPORT, 1000));
        pluginTypeList.add(new PositionalDataAccessPluginType(EXTEND, 2000));
        pluginTypeList.add(new PositionalDataAccessPluginType(ENRICHMENT, 2300));
        pluginTypeList.add(new PositionalDataAccessPluginType(CLEAN, 3000));
        pluginTypeList.add(new PositionalDataAccessPluginType(WORKFLOW, 4000));
        pluginTypeList.add(new PositionalDataAccessPluginType(UTILITY, 5000));
        pluginTypeList.add(new PositionalDataAccessPluginType(EXPERIMENTAL, Integer.MAX_VALUE - 1));
        pluginTypeList.add(new PositionalDataAccessPluginType(DEVELOPER, Integer.MAX_VALUE));

        return pluginTypeList;
    }
}
