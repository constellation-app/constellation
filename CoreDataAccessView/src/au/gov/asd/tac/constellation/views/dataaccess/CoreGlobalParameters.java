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
package au.gov.asd.tac.constellation.views.dataaccess;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;

/**
 * Global parameters available to all data access view plugins.
 *
 * @author algol
 */
@ServiceProvider(service = GlobalParameters.class)
public class CoreGlobalParameters extends GlobalParameters {

    private static List<PositionalPluginParameter> coreGlobalParametersIDS = null;
    protected static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z").withZone(ZoneId.systemDefault());

    /**
     * The name of the query.
     */
    private static final int QUERY_NAME_PARAMETER_ID_INDEX = 0;
    public static final String QUERY_NAME_PARAMETER_ID = PluginParameter.buildId(CoreGlobalParameters.class, "query_name");
    public static final PluginParameter QUERY_NAME_PARAMETER = StringParameterType.build(QUERY_NAME_PARAMETER_ID);

    /**
     * The datetime range that the query spans.
     */
    private static final int DATETIME_RANGE_PARAMETER_ID_INDEX = 1;
    public static final String DATETIME_RANGE_PARAMETER_ID = PluginParameter.buildId(CoreGlobalParameters.class, "datetime_range");
    public static final PluginParameter DATETIME_RANGE_PARAMETER = DateTimeRangeParameterType.build(DATETIME_RANGE_PARAMETER_ID);

    @Override
    public List<PositionalPluginParameter> getParameterList(final PluginParameters previous) {
        if (coreGlobalParametersIDS == null) {
            coreGlobalParametersIDS = buildParameterList(previous);
        }

        updateParameterList(previous);

        return coreGlobalParametersIDS;
    }

    protected List<PositionalPluginParameter> buildParameterList(final PluginParameters previous) {
        final PluginParameter<StringParameterValue> queryNameParameter = QUERY_NAME_PARAMETER;
        queryNameParameter.setName("Query Name");
        queryNameParameter.setDescription("A reference name for the query");

        final PluginParameter<DateTimeRangeParameterValue> datetimeRangeParameter = DATETIME_RANGE_PARAMETER;
        datetimeRangeParameter.setName("Range");
        datetimeRangeParameter.setDescription("The date and time range to query");
        datetimeRangeParameter.setHelpID(CoreGlobalParameters.class.getName());

        final List<PositionalPluginParameter> positionalPluginParametersList = new ArrayList<>();
        positionalPluginParametersList.add(QUERY_NAME_PARAMETER_ID_INDEX, new PositionalPluginParameter(queryNameParameter, 0));
        positionalPluginParametersList.add(DATETIME_RANGE_PARAMETER_ID_INDEX, new PositionalPluginParameter(datetimeRangeParameter, 100));

        return positionalPluginParametersList;
    }

    protected void updateParameterList(final PluginParameters previous) {
        @SuppressWarnings("unchecked") //QUERY_NAME_PARAMETER will always be of type StringParameter
        final PluginParameter<StringParameterValue> queryNameParameter = (PluginParameter<StringParameterValue>) coreGlobalParametersIDS.get(QUERY_NAME_PARAMETER_ID_INDEX).getParameter();
        queryNameParameter.setStringValue(String.format("%s at %s", System.getProperty("user.name"), TIMESTAMP_FORMAT.format(Instant.now())));

        if (previous != null) {
            @SuppressWarnings("unchecked") //DATETIME_RANGE_PARAMETER will always be of type DateTimeRangeParameter
            final PluginParameter<DateTimeRangeParameterValue> datetimeRangeParameter = (PluginParameter<DateTimeRangeParameterValue>) coreGlobalParametersIDS.get(DATETIME_RANGE_PARAMETER_ID_INDEX).getParameter();
            datetimeRangeParameter.setDateTimeRangeValue(previous.getParameters().get(DATETIME_RANGE_PARAMETER_ID).getDateTimeRangeValue());
        }
    }
}
