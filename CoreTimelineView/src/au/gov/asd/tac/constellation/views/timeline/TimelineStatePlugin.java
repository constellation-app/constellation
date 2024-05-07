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
package au.gov.asd.tac.constellation.views.timeline;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;

/**
 * This class provides read/write access to the <code>TimelineState</code>
 * object stored on the given graph.
 *
 * @see TimelineState
 *
 * @author betelgeuse
 */
@PluginInfo(tags = {PluginTags.LOW_LEVEL})
@NbBundle.Messages("TimelineStatePlugin=Timeline: Update State")
public class TimelineStatePlugin extends SimpleEditPlugin {

    private TimelineState state = null;

    /**
     * Creates a new <code>TimelineStatePlugin</code> to be used to set a given
     * <code>TimelineState</code> to the active graph.
     *
     * @see TimelineState
     *
     * @param state the time line state.
     */
    public TimelineStatePlugin(final TimelineState state) {
        this.state = state;
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction,
            final PluginParameters parameters) throws InterruptedException {
        final int attrID = TimelineConcept.MetaAttribute.TIMELINE_STATE.ensure(graph);
        graph.setObjectValue(attrID, 0, state);
    }
}
