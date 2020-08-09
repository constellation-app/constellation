/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.interaction.framework.GraphVisualManagerFactory;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.graph.visual.framework.GraphVisualAccess;
import au.gov.asd.tac.constellation.preferences.DeveloperPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = GraphVisualManagerFactory.class, position = 1)
public class VKInteractiveVisualManagerFactory extends GraphVisualManagerFactory {

    @Override
    public VisualManager constructVisualManager(Graph graph) throws Throwable {
        final VisualAccess access = new GraphVisualAccess(graph);
        final Preferences prefs = NbPreferences.forModule(DeveloperPreferenceKeys.class);
        final InteractiveVKVisualProcessor processor = new InteractiveVKVisualProcessor(graph.getId());
        final VisualManager manager = new VisualManager(access, processor);
        final GraphChangeListener changeDetector = event -> manager.updateFromIndigenousChanges();
        final DefaultInteractionEventHandler eventHandler = new DefaultInteractionEventHandler(graph, manager, processor, processor);

        //TODO_TT:
//        processor.addDropTargetToCanvas(new GraphRendererDropTarget(graph, manager, processor));
        graph.addGraphChangeListener(changeDetector);
        changeDetector.graphChanged(null);
        processor.startVisualising(manager);
        processor.setEventHandler(eventHandler);

        return manager;
    }
}
