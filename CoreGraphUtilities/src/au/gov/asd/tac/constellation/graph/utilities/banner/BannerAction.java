/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.utilities.banner;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Banner;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/*
 * action to allow the user to set a banner for a graph window
 */
@ActionID(
        category = "Edit",
        id = "au.gov.asd.tac.constellation.visual.banner.BannerAction")
@ActionRegistration(displayName = "#CTL_BannerAction", surviveFocusChange = true)
@ActionReference(path = "Menu/Experimental/Tools", position = 0)
@Messages({
    "CTL_BannerAction=Add Banner",
    "MSG_Title=Edit a banner",
    "MSG_Text=Banner text"
})
public final class BannerAction implements ActionListener {

    private final GraphNode context;

    public BannerAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent ev) {
        final Graph graph = context.getGraph();
        final ReadableGraph rg = graph.getReadableGraph();
        final Banner banner;
        try {
            final int bannerAttr = rg.getAttribute(GraphElementType.META, Banner.ATTRIBUTE_NAME);
            banner = bannerAttr != Graph.NOT_FOUND ? (Banner) rg.getObjectValue(bannerAttr, 0) : new Banner(true, 0, Bundle.MSG_Text(), ConstellationColor.BLACK, ConstellationColor.GREEN, "");
        } finally {
            rg.release();
        }

        final BannerPanel bpanel = new BannerPanel(banner);
        final DialogDescriptor dialog = new DialogDescriptor(bpanel, Bundle.MSG_Title(), true, new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (e.getActionCommand().equals("OK")) {
                    final Banner banner = bpanel.getBanner();
                    PluginExecution.withPlugin(new SimpleEditPlugin(Bundle.CTL_BannerAction()) {
                        @Override
                        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                            if (banner != null) {
                                final int bannerAttr = wg.addAttribute(GraphElementType.META, Banner.ATTRIBUTE_NAME, Banner.ATTRIBUTE_NAME, Banner.ATTRIBUTE_NAME, null, null);
                                wg.setObjectValue(bannerAttr, 0, banner);
                            }
                        }
                    }).executeLater(graph);
                }
            }
        });
        DialogDisplayer.getDefault().notify(dialog);
    }
}
