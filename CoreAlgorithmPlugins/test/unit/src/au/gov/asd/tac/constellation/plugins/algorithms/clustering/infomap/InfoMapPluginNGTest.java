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
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
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
    private static final String CONNECTION_TYPE_LINKS = "Links";
    private static final String CONNECTION_TYPE_EDGES = "Edges";
    private static final String CONNECTION_TYPE_TRANSACTIONS = "Transactions";

    // Dynamics
    public static final String DYNAMICS_PARAMETER_ID = PluginParameter.buildId(InfoMapPlugin.class, "dynamics");
    private static final String DYNAMICS_PARAMETER_UNDIRECTED = "Undirected";
    private static final String DYNAMICS_PARAMETER_DIRECTED = "Directed";
    private static final String DYNAMICS_PARAMETER_UNDIRECTED_FLOW = "Undirected flow, directed codelength";
    private static final String DYNAMICS_PARAMETER_INCLOMING_FLOW = "Incoming flow, all codelength";
    private static final String DYNAMICS_PARAMETER_DIRECTED_WEIGHT = "Directed, weight as flow";

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
                final PluginParameters params = instance.createParameters();
                // Connection type
                SingleChoiceParameterType.setChoice((PluginParameter) params.getParameters().get(CONNECTION_TYPE_PARAMETER_ID), connection);
                // Dynamics
                SingleChoiceParameterType.setChoice((PluginParameter) params.getParameters().get(DYNAMICS_PARAMETER_ID), dynamic);

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
}
