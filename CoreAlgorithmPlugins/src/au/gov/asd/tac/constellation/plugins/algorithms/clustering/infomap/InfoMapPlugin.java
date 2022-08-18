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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.tree.TreeData;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Info Map Plugin
 *
 * @author algol
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(tags = {PluginTags.ANALYTIC})
@NbBundle.Messages("InfoMapPlugin=Info Map")
public class InfoMapPlugin extends SimpleEditPlugin {

    private static final Logger LOGGER = Logger.getLogger(InfoMapPlugin.class.getName());

    public static final String CONFIG_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "config");

    @Override
    protected void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        if (wg.getVertexCount() <= 0) {
            interaction.notify(PluginNotificationLevel.ERROR, "The graph must have at least one vertex to run clustering on");
            LOGGER.log(Level.WARNING, "{0} run on Empty Graph", Bundle.InfoMapPlugin());
            return;
        }
        final Config config = (Config) parameters.getParameters().get(CONFIG_PARAMETER_ID).getObjectValue();
        final InfoMapContext context = new InfoMapContext(config, wg);
        context.getInfoMap().run();

        final int clusterAttrId = ClusteringConcept.VertexAttribute.INFOMAP_CLUSTER.ensure(wg);
        final InfomapBase infomap = context.getInfoMap();
        final TreeData treeData = infomap.getTreeData();
        LOGGER.log(Level.INFO, "Vertices {0}", treeData.getNumLeafNodes());

        for (final NodeBase node : treeData.getLeaves()) {
            final int index = node.getParent().getIndex();
            wg.setIntValue(clusterAttrId, wg.getVertex(node.getOriginalIndex()), index + 1);
        }
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<ObjectParameterValue> configParam = ObjectParameterType.build(CONFIG_PARAMETER_ID);
        configParam.setName("Config");
        configParam.setDescription("A Config object which defines the Info Map");
        configParam.setObjectValue(new Config());
        parameters.addParameter(configParam);

        return parameters;
    }
}
