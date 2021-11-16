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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.interaction.InteractiveGraphPluginRegistry;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.awt.Color;
import java.util.Date;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * A data access plugin that builds a complete graph.
 *
 * @author canis_majoris
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@Messages("DnaGraphBuilderPlugin=DNA Graph Builder")
@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.EXPERIMENTAL, PluginTags.CREATE})
public class DnaGraphBuilderPlugin extends SimpleEditPlugin {

    @Override
    public String getDescription() {
        return "Builds a complete graph.";
    }

    @Override
    public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Building...", true);

        final float N = 1;
        final float NPAIRS = 360 * N;
        final float RADIUS_BIG = 100;
        final float RADIUS_STRAND = 10;
        final float RADIUS_SMALL = 0.5f;

        final float maxColor = 255f;

        final int vxNameAttr = VisualConcept.VertexAttribute.LABEL.ensure(graph);
        final int vxIdentifierAttr = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
        final int vxXAttr = VisualConcept.VertexAttribute.X.ensure(graph);
        final int vxYAttr = VisualConcept.VertexAttribute.Y.ensure(graph);
        final int vxZAttr = VisualConcept.VertexAttribute.Z.ensure(graph);
        final int vxRadiusAttr = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
        final int vxColorAttr = VisualConcept.VertexAttribute.COLOR.ensure(graph);
        final int vxBackgroundIconAttr = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
        final int txColorAttr = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
        final int txDateTimeAttr = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);

        final ConstellationColor edgeColor = ConstellationColor.getColorValue(0, 0, 1, 1);
        long dt = new Date().getTime();
        for (int i = 0; i < NPAIRS; i++) {
            final double theta = i / N;

            final float ry = (float) (RADIUS_BIG * Math.sin(Math.toRadians(theta)));
            final float rz = (float) (RADIUS_BIG * Math.cos(Math.toRadians(theta)));
            final Color vc = new Color(Color.HSBtoRGB((float) i / NPAIRS, 0.5f, 1));

            final int n0 = graph.addVertex();
            graph.setStringValue(vxNameAttr, n0, String.valueOf(i * 2));
            graph.setStringValue(vxIdentifierAttr, n0, String.valueOf(i * 2));

            graph.setStringValue(vxBackgroundIconAttr, n0, "Background.Round Circle" + ((i % 2 == 0) ? "" : "_64"));
            graph.setFloatValue(vxRadiusAttr, n0, RADIUS_SMALL);
            graph.setObjectValue(vxColorAttr, n0, ConstellationColor.getColorValue(vc.getRed() / maxColor, vc.getGreen() / maxColor, vc.getBlue() / maxColor, 1f));
            final float x0 = RADIUS_STRAND * (float) Math.sin(Math.toRadians(i * 4.0));
            final float y0 = RADIUS_STRAND * (float) Math.cos(Math.toRadians(i * 4.0));
            ConstructionUtilities.setxyz(graph, n0, vxXAttr, vxYAttr, vxZAttr, x0, ry + y0, rz);

            final int n1 = graph.addVertex();
            graph.setStringValue(vxNameAttr, n1, String.valueOf(i * 2 + 1));
            graph.setStringValue(vxIdentifierAttr, n0, String.valueOf(i * 2 + 1));

            graph.setStringValue(vxBackgroundIconAttr, n1, "Background.Round Circle" + ((i % 2 == 1) ? "" : "_64"));
            graph.setFloatValue(vxRadiusAttr, n1, RADIUS_SMALL);
            graph.setObjectValue(vxColorAttr, n1, ConstellationColor.getColorValue(1 - vc.getRed() / maxColor, 1 - vc.getGreen() / maxColor, 1 - vc.getBlue() / maxColor, 1f));
            final float x1 = -x0;
            final float y1 = -y0;
            ConstructionUtilities.setxyz(graph, n1, vxXAttr, vxYAttr, vxZAttr, x1, ry + y1, rz);

            final int tx = graph.addTransaction(n0, n1, false);
            graph.setLongValue(txDateTimeAttr, tx, dt++);
            graph.setObjectValue(txColorAttr, tx, edgeColor);
        }

        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        interaction.setProgress(1, 0, "Completed successfully", true);
    }
}
