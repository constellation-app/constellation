/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.algorithms.sna.global.GlobalClusteringCoefficientPlugin;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * The global clustering coefficient analytic for the Analytic View.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = AnalyticPlugin.class),
    @ServiceProvider(service = Plugin.class)
})
@PluginInfo(tags = {PluginTags.ANALYTIC})
@AnalyticInfo(analyticCategory = "Global")
@NbBundle.Messages("GlobalClusteringCoefficientAnalytic=Global Clustering Coefficient Analytic")
public class GlobalClusteringCoefficientAnalytic extends GraphAnalyticPlugin {

    @Override
    public String getDocumentationUrl() {
        return getHelpPath() + "analytic-global-clustering-coefficient.md";
    }

    @Override
    public Set<SchemaAttribute> getAnalyticAttributes(final PluginParameters parameters) {
        final Set<SchemaAttribute> attributes = new HashSet<>();
        attributes.add(SnaConcept.GraphAttribute.CLUSTERING_COEFFICIENT);
        return Collections.unmodifiableSet(attributes);
    }

    @Override
    public Class<? extends Plugin> getAnalyticPlugin() {
        return GlobalClusteringCoefficientPlugin.class;
    }
}
