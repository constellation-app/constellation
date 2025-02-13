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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleReadPlugin;
import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author aldebaran30701
 */
@PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.LOW_LEVEL})
public class QualityControlStateUpdater extends SimpleReadPlugin {

    protected static final String PLUGIN_NAME = "Quality Control View: Update State";

    @Override
    public void read(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        final List<QualityControlRule> registeredRules = new ArrayList<>();
        final List<Integer> vertexList = new ArrayList<>();
        final List<String> identifierList = new ArrayList<>();
        final List<SchemaVertexType> typeList = new ArrayList<>();

        if (graph != null) {
            final int selectedAttribute = VisualConcept.VertexAttribute.SELECTED.get(graph);
            final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
            final int typeAttribute = AnalyticConcept.VertexAttribute.TYPE.get(graph);

            if (selectedAttribute != Graph.NOT_FOUND
                    && identifierAttribute != Graph.NOT_FOUND
                    && typeAttribute != Graph.NOT_FOUND) {
                final int vxCount = graph.getVertexCount();
                for (int position = 0; position < vxCount; position++) {
                    final int vertex = graph.getVertex(position);
                    final String identifier = graph.getStringValue(identifierAttribute, vertex);
                    final SchemaVertexType type = graph.getObjectValue(typeAttribute, vertex);
                    final boolean selected = graph.getBooleanValue(selectedAttribute, vertex);

                    if (selected) {
                        vertexList.add(vertex);
                        identifierList.add(identifier);
                        typeList.add(type);
                    }
                }
            }

            // Set up and run each rule.
            if (!vertexList.isEmpty()) {
                for (final QualityControlRule rule : QualityControlAutoVetter.getRules()) {
                    rule.clearResults();
                    if (rule.isEnabled()) {
                        rule.executeRule(graph, vertexList);
                        registeredRules.add(rule);
                    }
                }
            }

            final List<QualityControlRule> uRegisteredRules = Collections.unmodifiableList(registeredRules);

            // Build quality control events based on results of rules.
            // Sort by descending risk.
            final List<QualityControlEvent> qualityControlEvents = new ArrayList<>();
            for (int i = 0; i < vertexList.size(); i++) {
                final QualityControlEvent qualityControlEvent = new QualityControlEvent(
                        vertexList.get(i),
                        identifierList.get(i), typeList.get(i),
                        uRegisteredRules
                );
                qualityControlEvents.add(qualityControlEvent);
            }
            Collections.sort(qualityControlEvents, Collections.reverseOrder());

            QualityControlAutoVetter.getInstance().setQualityControlState(
                    new QualityControlState(graph.getId(), qualityControlEvents, registeredRules)
            );
        }
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }
}
