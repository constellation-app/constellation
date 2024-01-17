/*
* Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.visual.plugins.colorblind;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecutor;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author centauri032001
 */
@ActionID(category = "Display", id = "au.gov.asd.tac.constellation.graph.visual.plugins.colorblind.ApplyColorblindAction")
@ActionRegistration(displayName = "#CTL_ColorblindAction",
        iconBase = "au/gov/asd/tac/constellation/graph/visual/plugins/colorblind/resources/colorblind.png",
        surviveFocusChange = true)
@ActionReferences({
    @ActionReference(path = "Menu/Display", position = 950, separatorBefore = 901)
})
@NbBundle.Messages({
    "CTL_ColorblindAction=Apply colorblind selection to graph"
})
public class ColorblindAction extends AbstractAction {

    private final GraphNode context;

    public ColorblindAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecutor.startWith(new ColorblindCleanupPlugin())
                .executeWriteLater(context.getGraph());
    }

    @PluginInfo(pluginType = PluginType.UPDATE, tags = {PluginTags.MODIFY})
    public static class ColorblindCleanupPlugin extends SimpleEditPlugin {

        @Override
        public String getName() {
            return "Colorblind plugin: Color nodes";
        }

        @Override
        public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final int vxColorblindAttr = VisualConcept.VertexAttribute.COLORBLIND_LAYER.ensure(graph);
            final int txColorblindAttr = VisualConcept.TransactionAttribute.COLORBLIND_LAYER.ensure(graph);
            ColorblindUtilities.colorNodes(graph, vxColorblindAttr, txColorblindAttr);
        }

    }

}
