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
package au.gov.asd.tac.constellation.graph.interaction.visual;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.interaction.framework.GraphVisualManagerFactory;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.preferences.DeveloperPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * The default {@link GraphVisualManagerFactory} to construct
 * {@link VisualManager} objects for CONSTELLATION graphs.
 * <p>
 * This factory will use a {@link GraphVisualAccess} as the interface to a
 * graph, an {@link InteractiveGLVisualProcessor} to visualise the graph in 3D
 * using GL, a {@link DefaultInteractionEventHandler} to respond to mouse and
 * keyboard gestures on the graph, a {@link GraphRendererDropTarget} to handle
 * drag and dropping onto a graph, and a {@link GraphChangeListener} to respond
 * to general graph changes.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = GraphVisualManagerFactory.class)
public class GLInteractiveVisualManagerFactory extends GraphVisualManagerFactory {

    @Override
    public VisualManager constructVisualManager(Graph graph) {
        final GraphVisualAccess access = new GraphVisualAccess(graph);
        try (final ReadableGraph rg = graph.getReadableGraph()) {
            access.updateModCounts(rg);
        }
        final Preferences prefs = NbPreferences.forModule(DeveloperPreferenceKeys.class);
        final InteractiveGLVisualProcessor processor = new InteractiveGLVisualProcessor(prefs.getBoolean(DeveloperPreferenceKeys.DEBUG_GL, DeveloperPreferenceKeys.DEBUG_GL_DEFAULT), prefs.getBoolean(DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES, DeveloperPreferenceKeys.PRINT_GL_CAPABILITIES_DEFAULT));
        final VisualManager manager = new VisualManager(access, processor);
        final GraphChangeListener changeDetector = event -> manager.updateFromIndigenousChanges();
        final DefaultInteractionEventHandler eventHandler = new DefaultInteractionEventHandler(graph, manager, processor, processor);

        processor.addDropTargetToCanvas(new GraphRendererDropTarget(graph, manager, processor));
        graph.addGraphChangeListener(changeDetector);
        changeDetector.graphChanged(null);
        processor.startVisualising(manager);
        processor.setEventHandler(eventHandler);

        return manager;
    }
}
