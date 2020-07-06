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
package au.gov.asd.tac.constellation.views.notes.utilities;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.NotesConcept;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.view.notes.state.NotesViewState;

/**
 * Write a given bitmask to the active graph.
 *
 * @author sol695510
 */
public final class UpdateNotesViewStatePlugin extends SimpleEditPlugin {

    private final NotesViewState state;

    public UpdateNotesViewStatePlugin(final NotesViewState state) {
        this.state = state;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) {
        // TODO Change attribute name
        final int stateAttributeId = NotesConcept.GraphAttribute.LAYER_MASK_SELECTED.ensure(graph);
        graph.setObjectValue(stateAttributeId, 0, state);
    }

    @Override
    protected boolean isSignificant() {
        return true;
    }

    @Override
    public String getName() {
         // TODO Change attribute name
        return "Layers View: Update Graph Bitmask";
    }
}
