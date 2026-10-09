/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.agent;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 * What the agent needs from the application. Separated out so the agent loop
 * can be tested without a running application.
 *
 * @author doubtsec
 */
public interface AgentEnvironment {

    /**
     * @return the active graph, or null.
     */
    Graph getActiveGraph();

    /**
     * @return a short, system generated description of the active graph to
     * prepend to each user message, or null.
     */
    String describeContext();

    /**
     * @param graph a graph.
     * @return a display name for the graph.
     */
    String graphName(Graph graph);
}
