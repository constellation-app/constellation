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
package au.gov.asd.tac.constellation.views.analyticview.analytics;

import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.centrality.ClosenessCentralityPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * The Closeness Centrality analytic for the Analytic View.
 *
 * @author cygnus_x-1
 */
@ServiceProviders({
    @ServiceProvider(service = AnalyticPlugin.class),
    @ServiceProvider(service = Plugin.class)
})
@PluginInfo(tags = {PluginTags.ANALYTIC})
@AnalyticInfo(analyticCategory = "Centrality")
@NbBundle.Messages("ClosenessCentralityAnalytic=Closeness Centrality Analytic")
public class ClosenessCentralityAnalytic extends ScoreAnalyticPlugin {

    @Override
    public String getDocumentationUrl() {
        return getHelpPath() + "analytic-closeness-centrality.md";
    }

    @Override
    public Set<SchemaAttribute> getPrerequisiteAttributes() {
        final Set<SchemaAttribute> analyticAttributes = super.getPrerequisiteAttributes();
        analyticAttributes.add(VisualConcept.TransactionAttribute.SELECTED);
        return Collections.unmodifiableSet(analyticAttributes);
    }

    @Override
    public Set<SchemaAttribute> getAnalyticAttributes(final PluginParameters parameters) {
        final boolean harmonic = parameters.getBooleanValue(ClosenessCentralityPlugin.HARMONIC_PARAMETER_ID);
        final boolean includeConnectionsIn = parameters.getBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_IN_PARAMETER_ID);
        final boolean includeConnectionsOut = parameters.getBooleanValue(ClosenessCentralityPlugin.INCLUDE_CONNECTIONS_OUT_PARAMETER_ID);
        final Set<SchemaAttribute> analyticAttributes = new HashSet<>();
        if (harmonic) {
            analyticAttributes.add(includeConnectionsIn && !includeConnectionsOut ? SnaConcept.VertexAttribute.IN_HARMONIC_CLOSENESS_CENTRALITY
                    : !includeConnectionsIn && includeConnectionsOut ? SnaConcept.VertexAttribute.OUT_HARMONIC_CLOSENESS_CENTRALITY
                            : SnaConcept.VertexAttribute.HARMONIC_CLOSENESS_CENTRALITY);
        } else {
            analyticAttributes.add(includeConnectionsIn && !includeConnectionsOut ? SnaConcept.VertexAttribute.IN_CLOSENESS_CENTRALITY
                    : !includeConnectionsIn && includeConnectionsOut ? SnaConcept.VertexAttribute.OUT_CLOSENESS_CENTRALITY
                            : SnaConcept.VertexAttribute.CLOSENESS_CENTRALITY);
        }
        return Collections.unmodifiableSet(analyticAttributes);
    }

    @Override
    public Class<? extends Plugin> getAnalyticPlugin() {
        return ClosenessCentralityPlugin.class;
    }
}
