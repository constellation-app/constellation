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

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.io.Config;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class InfoMapPluginNGTest {

    // Connection Type
    public static final String CONNECTION_TYPE_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "connection_type");
    private static final String CONNECTION_TYPE_PARAMETER_ID_NAME = "Connection Type";

    private static final String CONNECTION_TYPE_LINKS = "Links";
    private static final String CONNECTION_TYPE_EDGES = "Edges";
    private static final String CONNECTION_TYPE_TRANSACTIONS = "Transactions";

    private static final List<String> CONNECTION_TYPE_PARAM_VALUES = Arrays.asList(
            CONNECTION_TYPE_LINKS,
            CONNECTION_TYPE_EDGES,
            CONNECTION_TYPE_TRANSACTIONS
    );

    // Dynamics
    public static final String DYNAMICS_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "dynamics");
    private static final String DYNAMICS_PARAMETER_ID_NAME = "Dynamics";
    private static final String DYNAMICS_PARAMETER_UNDIRECTED = "Undirected";
    private static final String DYNAMICS_PARAMETER_DIRECTED = "Directed";
    private static final String DYNAMICS_PARAMETER_UNDIRECTED_FLOW = "Undirected flow, directed codelength";
    private static final String DYNAMICS_PARAMETER_INCLOMING_FLOW = "Incoming flow, all codelength";
    private static final String DYNAMICS_PARAMETER_DIRECTED_WEIGHT = "Directed, weight as flow";

    private static final List<String> DYNAMICS_PARAM_VALUES = Arrays.asList(
            DYNAMICS_PARAMETER_UNDIRECTED,
            DYNAMICS_PARAMETER_DIRECTED,
            DYNAMICS_PARAMETER_UNDIRECTED_FLOW,
            DYNAMICS_PARAMETER_INCLOMING_FLOW,
            DYNAMICS_PARAMETER_DIRECTED_WEIGHT
    );

    // Optimisation Level
    public static final String OPTIMISATION_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "optimisation_level");
    private static final String OPTIMISATION_PARAMETER_ID_NAME = "Optimisation Level";
    private static final String OPTIMISATION_PARAMETER_ID_INTERVAL_DEFAULT = "Full coarse-tune";
    private static final String OPTIMISATION_PARAMETER_ID_DEFAULT = OPTIMISATION_PARAMETER_ID_INTERVAL_DEFAULT;

    private static final Map<String, Integer> OPTIMISATION_LEVELS = new HashMap<>();

    static {
        OPTIMISATION_LEVELS.put("Full coarse-tune", 0);
        OPTIMISATION_LEVELS.put("Fast coarse-tune", 1);
        OPTIMISATION_LEVELS.put("No tuning", 2);
        OPTIMISATION_LEVELS.put("No aggregation or tuning", 3);
    }

    // Fast Hierarchical
    public static final String FAST_HIERARCHICAL_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "fast_hierarchical");
    private static final String FAST_HIERARCHICAL_PARAMETER_ID_NAME = "Fast Hierarchical";
    private static final String FAST_HIERARCHICAL_PARAMETER_ID_INTERVAL_DEFAULT = "Normal";
    private static final String FAST_HIERARCHICAL_PARAMETER_ID_DEFAULT = FAST_HIERARCHICAL_PARAMETER_ID_INTERVAL_DEFAULT;

    private static final Map<String, Integer> FAST_HIERARCHICAL_LEVELS = new HashMap<>();

    static {
        FAST_HIERARCHICAL_LEVELS.put("Normal", 0);
        FAST_HIERARCHICAL_LEVELS.put("Top modules fast", 1);
        FAST_HIERARCHICAL_LEVELS.put("All fast levels", 2);
        FAST_HIERARCHICAL_LEVELS.put("Skip recursive", 3);
    }

    // Number of Trials
    public static final String NUM_TRIALS_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "num_trials");
    private static final String NUM_TRIALS_PARAMETER_ID_NAME = "Number of Trials";
    private static final int NUM_TRIALS_PARAMETER_ID_DEFAULT = 1;

    /**
     * Test of createParameters method, of class InfoMapPlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");

        final String[] expResults = {
            PluginParameter.buildId(InfoMapPlugin.class, "connection_type"),
            PluginParameter.buildId(InfoMapPlugin.class, "dynamics"),
            PluginParameter.buildId(InfoMapPlugin.class, "optimisation_level"),
            PluginParameter.buildId(InfoMapPlugin.class, "fast_hierarchical"),
            PluginParameter.buildId(InfoMapPlugin.class, "num_trials")
        };

        final InfoMapPlugin instance = new InfoMapPlugin();
        final PluginParameters result = instance.createParameters();

        // Assert all plugin parameters were created
        for (final String param : expResults) {
            assertTrue(result.getParameters().containsKey(param));
        }
    }

    /**
     * Test of createConfig method, of class InfoMapPlugin.
     */
    @Test
    public void testCreateConfig() {
        System.out.println("createConfig");

        final String[] connectionTypes = {
            CONNECTION_TYPE_TRANSACTIONS,
            CONNECTION_TYPE_EDGES,
            CONNECTION_TYPE_LINKS
        };

        final String[] dynamicTypes = {
            DYNAMICS_PARAMETER_UNDIRECTED,
            DYNAMICS_PARAMETER_DIRECTED,
            DYNAMICS_PARAMETER_UNDIRECTED_FLOW,
            DYNAMICS_PARAMETER_INCLOMING_FLOW,
            DYNAMICS_PARAMETER_DIRECTED_WEIGHT
        };

        final InfoMapPlugin instance = new InfoMapPlugin();
        for (final String connection : connectionTypes) {
            for (final String dynamic : dynamicTypes) {
                final PluginParameters params = createParametersHelper(connection, dynamic);
                final Config config = instance.createConfig(params);

                // Assert connection and dynamic was set in config
                configParameterHelper(connection, dynamic, config);
            }
        }

    }

    private void configParameterHelper(final String connectionType, final String dynamicType, final Config config) {
        // Connection type.
        final Config.ConnectionType connectionTypeEnum = connectionTypeHelper(connectionType);
        assertEquals(connectionTypeEnum, config.getConnectionType());

        // Dynamic type
        // Expected true if given dynamic type matches this dynamic type
        final boolean expResultUndirected = dynamicType.equals(DYNAMICS_PARAMETER_UNDIRECTED);
        final boolean expResultDirected = dynamicType.equals(DYNAMICS_PARAMETER_DIRECTED);
        final boolean expResultUndirectedFlow = dynamicType.equals(DYNAMICS_PARAMETER_UNDIRECTED_FLOW);
        final boolean expResultIncomingFlow = dynamicType.equals(DYNAMICS_PARAMETER_INCLOMING_FLOW);
        final boolean expResultDirectectWeigth = dynamicType.equals(DYNAMICS_PARAMETER_DIRECTED_WEIGHT);

        assertEquals(expResultUndirected, config.isUndirected());
        assertEquals(expResultDirected, config.isDirected());
        assertEquals(expResultUndirectedFlow, config.isUndirdir());
        assertEquals(expResultIncomingFlow, config.isOutdirdir());
        assertEquals(expResultDirectectWeigth, config.isRawdir());
    }

    private Config.ConnectionType connectionTypeHelper(final String connectionType) {
        return switch (connectionType) {
            case CONNECTION_TYPE_TRANSACTIONS ->
                Config.ConnectionType.TRANSACTIONS;
            case CONNECTION_TYPE_EDGES ->
                Config.ConnectionType.EDGES;
            case CONNECTION_TYPE_LINKS ->
                Config.ConnectionType.LINKS;
            default ->
                Config.ConnectionType.TRANSACTIONS;
        };
    }

    private PluginParameters createParametersHelper(final String connectionType, final String dynamicType) {
        final PluginParameters parameters = new PluginParameters();

        // Connection type
        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> connectionParam = SingleChoiceParameterType.build(CONNECTION_TYPE_PARAMETER_ID);
        connectionParam.setName(CONNECTION_TYPE_PARAMETER_ID_NAME);
        SingleChoiceParameterType.setOptions(connectionParam, CONNECTION_TYPE_PARAM_VALUES);
        SingleChoiceParameterType.setChoice(connectionParam, connectionType);
        parameters.addParameter(connectionParam);

        // Dynamics
        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> dynamicsParam = SingleChoiceParameterType.build(DYNAMICS_PARAMETER_ID);
        dynamicsParam.setName(DYNAMICS_PARAMETER_ID_NAME);
        SingleChoiceParameterType.setOptions(dynamicsParam, DYNAMICS_PARAM_VALUES);
        SingleChoiceParameterType.setChoice(dynamicsParam, dynamicType);
        parameters.addParameter(dynamicsParam);

        // Optimisation Level
        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> optimisationParam = SingleChoiceParameterType.build(OPTIMISATION_PARAMETER_ID);
        optimisationParam.setName(OPTIMISATION_PARAMETER_ID_NAME);
        SingleChoiceParameterType.setOptions(optimisationParam, new ArrayList<>(OPTIMISATION_LEVELS.keySet()));
        SingleChoiceParameterType.setChoice(optimisationParam, OPTIMISATION_PARAMETER_ID_DEFAULT);
        parameters.addParameter(optimisationParam);

        // Fast Hierarchical
        final PluginParameter<SingleChoiceParameterType.SingleChoiceParameterValue> hierarchicalParam = SingleChoiceParameterType.build(FAST_HIERARCHICAL_PARAMETER_ID);
        hierarchicalParam.setName(FAST_HIERARCHICAL_PARAMETER_ID_NAME);
        SingleChoiceParameterType.setOptions(hierarchicalParam, new ArrayList<>(FAST_HIERARCHICAL_LEVELS.keySet()));
        SingleChoiceParameterType.setChoice(hierarchicalParam, FAST_HIERARCHICAL_PARAMETER_ID_DEFAULT);
        parameters.addParameter(hierarchicalParam);

        // Number of trials
        final PluginParameter<IntegerParameterType.IntegerParameterValue> amountParam = IntegerParameterType.build(NUM_TRIALS_PARAMETER_ID);
        amountParam.setName(NUM_TRIALS_PARAMETER_ID_NAME);
        amountParam.setIntegerValue(NUM_TRIALS_PARAMETER_ID_DEFAULT);
        parameters.addParameter(amountParam);

        return parameters;
    }
}
