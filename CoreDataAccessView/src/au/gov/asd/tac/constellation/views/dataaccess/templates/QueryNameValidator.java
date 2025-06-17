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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import org.apache.commons.lang3.StringUtils;

/**
 * Ensure that the query name is non-empty.
 *
 * @author arcturus
 */
public class QueryNameValidator extends RecordStoreValidator {

    @Override
    public void validatePreQuery(final RecordStoreQueryPlugin plugin, final RecordStore rs, final PluginInteraction interaction, final PluginParameters parameters) throws PluginException {
        final String queryName = parameters.getStringValue(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID);
        if (StringUtils.isBlank(queryName)) {
            throw new PluginException(PluginNotificationLevel.ERROR, "Query name required");
        }
    }
}
