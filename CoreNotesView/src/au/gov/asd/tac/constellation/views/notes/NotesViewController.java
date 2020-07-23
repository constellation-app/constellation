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
package au.gov.asd.tac.constellation.views.notes;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NotesConcept;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;

/**
 *
 * @author sol695510
 */
public class NotesViewController {

    private final NotesViewTopComponent parent;

    public NotesViewController(final NotesViewTopComponent parent) {
        this.parent = parent;
    }

    /**
     * Add attributes required by the Notes View for it to function
     */
    public void addAttributes() {
        final Graph activeGraph = GraphManager.getDefault().getActiveGraph();
        if (activeGraph != null) {
            PluginExecution.withPlugin(new SimpleEditPlugin("Notes View: Add Required Attributes") {
                @Override
                public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                    NotesConcept.GraphAttribute.NOTES_VIEW_STATE.ensure(graph);
                }
            }).executeLater(activeGraph);
        }
    }
    // Here inside this file will be any control that does not relate to
    // visual changes such as reloading menus (will only be responsible for
    // changes like saving values to the graph state.)
}
