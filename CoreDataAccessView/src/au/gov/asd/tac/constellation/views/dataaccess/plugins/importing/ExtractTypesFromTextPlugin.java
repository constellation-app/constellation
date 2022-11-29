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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.GraphRecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities.ExtractedVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Extract entities matching schema types from text and add them to a graph.
 *
 * @author cygnus_x-1
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@Messages("ExtractTypesFromTextPlugin=Extract Types from Text")
@PluginInfo(pluginType = PluginType.IMPORT, tags = {PluginTags.IMPORT})
public class ExtractTypesFromTextPlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {

    // plugin parameters
    public static final String TEXT_PARAMETER_ID = PluginParameter.buildId(ExtractTypesFromTextPlugin.class, "text");

    @Override
    public String getType() {
        return DataAccessPluginCoreType.IMPORT;
    }

    @Override
    public int getPosition() {
        return 1000;
    }

    @Override
    public String getDescription() {
        return "Identify schema type values within text and add them to your graph";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();

        final PluginParameter<StringParameterValue> text = StringParameterType.build(TEXT_PARAMETER_ID);
        StringParameterType.setLines(text, 15);
        text.setName("Text");
        text.setDescription("Text to extract from");
        text.setRequired(true);
        params.addParameter(text);

        return params;
    }

    @Override
    protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final RecordStore result = new GraphRecordStore();

        interaction.setProgress(0, 0, "Importing...", true);

        final Map<String, PluginParameter<?>> extractEntityParameters = parameters.getParameters();
        final String text = extractEntityParameters.get(TEXT_PARAMETER_ID).getStringValue();

        if (text == null) {
            throw new PluginException(PluginNotificationLevel.ERROR, "No text provided from which to extract types.");
        }

        final List<ExtractedVertexType> extractedTypes = SchemaVertexTypeUtilities.extractVertexTypes(text);

        final Map<String, SchemaVertexType> identifiers = new HashMap<>();
        extractedTypes.forEach(extractedType -> identifiers.put(extractedType.getIdentifier(), extractedType.getType()));

        for (final String identifier : identifiers.keySet()) {
            result.add();
            result.set(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER, identifier);
            result.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.TYPE, identifiers.get(identifier));
            result.set(GraphRecordStoreUtilities.SOURCE + AnalyticConcept.VertexAttribute.SEED, "true");
        }

        ConstellationLoggerHelper.createPropertyBuilder(
                this,
                result.getAll(GraphRecordStoreUtilities.SOURCE + VisualConcept.VertexAttribute.IDENTIFIER),
                ConstellationLoggerHelper.SUCCESS
        );

        interaction.setProgress(1, 0, "Completed successfully - imported " + result.size() + " entities.", true);

        return result;
    }
}
