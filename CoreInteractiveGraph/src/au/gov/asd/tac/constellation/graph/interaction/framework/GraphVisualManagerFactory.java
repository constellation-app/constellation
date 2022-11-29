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
package au.gov.asd.tac.constellation.graph.interaction.framework;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;

/**
 * A factory for creating {@link VisualManager} objects, along with all of their
 * related components (such as a {@link VisualProcessor}), for given graphs.
 * <p>
 * CONSTELLATION's windowing environment will use lookup to construct a
 * {@link VisualManager} for each graph, and place
 * {@link VisualManager#getVisualComponent()} as the immediate child of the top
 * component for that graph.
 *
 * @author twilight_sparkle
 */
public abstract class GraphVisualManagerFactory {

    /**
     * Construct a {@link VisualManager}, along with all related components, for
     * the specified graph.
     * <p>
     * As well as constructing components such as a {@link VisualProcessor},
     * this method should also initiate any processing life-cycles, such as
     * {@link VisualProcessor#startVisualising}. Note that
     * {@link VisualManager#startProcessing()} need not be called here, as that
     * should usually be done by the customer of this factory.
     *
     * @param graph The graph to be visually managed.
     * @return A {@link VisualManager} for the graph.
     */
    public abstract VisualManager constructVisualManager(final Graph graph);

}
