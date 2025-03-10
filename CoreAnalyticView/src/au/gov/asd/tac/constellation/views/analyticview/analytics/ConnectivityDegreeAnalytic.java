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
package au.gov.asd.tac.constellation.views.analyticview.analytics;

import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.SnaConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.sna.metrics.ConnectivityDegreePlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * The connected components analytic for the Analytic View.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = AnalyticPlugin.class),
    @ServiceProvider(service = Plugin.class)
})
@PluginInfo(tags = {PluginTags.ANALYTIC})
@AnalyticInfo(analyticCategory = "Metrics")
@NbBundle.Messages("ConnectivityDegreeAnalytic=Connectivity Degree Analytic")
public class ConnectivityDegreeAnalytic extends ScoreAnalyticPlugin {

    @Override
    public String getDocumentationUrl() {
        return AnalyticUtilities.getHelpPath() + "analytic-connectivity-degree.md";
    }

    @Override
    public Set<SchemaAttribute> getAnalyticAttributes(final PluginParameters parameters) {
        final Set<SchemaAttribute> analyticAttributes = new HashSet<>();
        analyticAttributes.add(SnaConcept.VertexAttribute.CONNECTIVITY_DEGREE);
        return Collections.unmodifiableSet(analyticAttributes);
    }

    @Override
    public Class<? extends Plugin> getAnalyticPlugin() {
        return ConnectivityDegreePlugin.class;
    }
}
