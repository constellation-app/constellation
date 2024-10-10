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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap.InfomapBase;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.tree.TreeData;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Dynamics
    public static final String DYNAMICS_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "dynamics");
    private static final String DYNAMICS_PARAMETER_ID_NAME = "Dynamics";
    //private static final String DYNAMICS_PARAMETER_ID_DESCRIPTION = "Unit of time by which to layer graph";
    private static final String DYNAMICS_PARAMETER_UNDIRECTED = "Undirected";
    private static final String DYNAMICS_PARAMETER_DIRECTED = "Directed";
    private static final String DYNAMICS_PARAMETER_UNDIRECTED_FLOW = "Undirected flow, directed codelength";
    private static final String DYNAMICS_PARAMETER_INCLOMING_FLOW = "Incoming flow, all codelength";
    private static final String DYNAMICS_PARAMETER_DIRECTECT_WEIGHT = "Directed, weight as flow";
    private static final String DYNAMICS_PARAMETER_ID_DEFAULT = DYNAMICS_PARAMETER_UNDIRECTED;

    private static final List<String> DYNAMICS_PARAM_VALUES = Arrays.asList(
            DYNAMICS_PARAMETER_UNDIRECTED,
            DYNAMICS_PARAMETER_DIRECTED,
            DYNAMICS_PARAMETER_UNDIRECTED_FLOW,
            DYNAMICS_PARAMETER_INCLOMING_FLOW,
            DYNAMICS_PARAMETER_DIRECTECT_WEIGHT);

    // Optimisation Level
    public static final String OPTIMISATION_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "optimisation_level");
    private static final String OPTIMISATION_PARAMETER_ID_NAME = "optimisation Level";
    //private static final String OPTIMISATION_PARAMETER_ID_DESCRIPTION = "Unit of time by which to layer graph";
    private static final String OPTIMISATION_PARAMETER_ID_INTERVAL_DEFAULT = "Full coarse-tune";
    private static final String OPTIMISATION_PARAMETER_ID_DEFAULT = OPTIMISATION_PARAMETER_ID_INTERVAL_DEFAULT;

    private static final Map<String, Integer> OPTIMISATION_LEVELS = new HashMap<>();

    static {
        OPTIMISATION_LEVELS.put("Full coarse-tune", 0);
        OPTIMISATION_LEVELS.put("Fast coarse-tune", 1);
        OPTIMISATION_LEVELS.put("No tuning", 2);
        OPTIMISATION_LEVELS.put("No aggregation or tuning", 3);
    }

    // Number of Trials
    public static final String NUM_TRIALS_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "num_trials");
    private static final String NUM_TRIALS_PARAMETER_ID_NAME = "Number of Trials";
    //private static final String NUM_TRIALS_PARAMETER_ID_DESCRIPTION = "Number of Intervals to layer the graph into";
    private static final int NUM_TRIALS_PARAMETER_ID_DEFAULT = 1;

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
        //OLD
//        final PluginParameter<ObjectParameterValue> configParam = ObjectParameterType.build(CONFIG_PARAMETER_ID);
//        configParam.setName("Config");
//        configParam.setDescription("A Config object which defines the Info Map");
//        configParam.setObjectValue(new Config());
//        parameters.addParameter(configParam);

        // New
        // Connection type
        // Dynamics
        final PluginParameter<SingleChoiceParameterValue> dynamicsParam = SingleChoiceParameterType.build(DYNAMICS_PARAMETER_ID);
        dynamicsParam.setName(DYNAMICS_PARAMETER_ID_NAME);
        //layerByParam.setDescription(DYNAMICS_PARAMETER_ID_DESCRIPTION);
        SingleChoiceParameterType.setOptions(dynamicsParam, DYNAMICS_PARAM_VALUES);
        SingleChoiceParameterType.setChoice(dynamicsParam, DYNAMICS_PARAMETER_ID_DEFAULT);
        parameters.addParameter(dynamicsParam);

//        final PluginParameter<SingleChoiceParameterValue> dtAttrParam = SingleChoiceParameterType.build(DATETIME_ATTRIBUTE_PARAMETER_ID);
//        dtAttrParam.setName(DATETIME_PARAMETER_ID_NAME);
//        dtAttrParam.setDescription(DATETIME_ATTRIBUTE_PARAMETER_ID_DESCRIPTION);
//        dtAttrParam.setRequired(true);
//        parameters.addParameter(dtAttrParam);
// 
        //Optimisation Level
        final PluginParameter<SingleChoiceParameterValue> unitParam = SingleChoiceParameterType.build(OPTIMISATION_PARAMETER_ID);
        unitParam.setName(OPTIMISATION_PARAMETER_ID_NAME);
        //unitParam.setDescription(OPTIMISATION_PARAMETER_ID_DESCRIPTION);
        SingleChoiceParameterType.setOptions(unitParam, new ArrayList<>(OPTIMISATION_LEVELS.keySet()));
        SingleChoiceParameterType.setChoice(unitParam, OPTIMISATION_PARAMETER_ID_DEFAULT);
        parameters.addParameter(unitParam);

        // Fast Hierarchical
        // Number of trials
        final PluginParameter<IntegerParameterValue> amountParam = IntegerParameterType.build(NUM_TRIALS_PARAMETER_ID);
        amountParam.setName(NUM_TRIALS_PARAMETER_ID_NAME);
        //amountParam.setDescription(NUM_TRIALS_PARAMETER_ID_DESCRIPTION);
        amountParam.setIntegerValue(NUM_TRIALS_PARAMETER_ID_DEFAULT);
        parameters.addParameter(amountParam);

        return parameters;
    }
}
