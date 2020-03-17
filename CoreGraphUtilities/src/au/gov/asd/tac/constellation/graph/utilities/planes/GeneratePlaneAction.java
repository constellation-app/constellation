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
package au.gov.asd.tac.constellation.graph.utilities.planes;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Plane;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.PlaneState;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * generate plane
 *
 * @author algol
 */
@ActionID(category = "Experimental", id = "au.gov.asd.tac.constellation.graph.visual.planes.GeneratePlaneAction")
@ActionRegistration(displayName = "#CTL_GeneratePlaneAction", surviveFocusChange = true)
@ActionReference(path = "Menu/Experimental/Tools", position = 0)
@Messages("CTL_GeneratePlaneAction=Generate Plane")
public final class GeneratePlaneAction extends AbstractAction {

    private final GraphNode context;

    public GeneratePlaneAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(new SimpleEditPlugin() {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                GeneratePlaneAction.run(graph);
            }

            @Override
            public String getName() {
                return Bundle.CTL_GeneratePlaneAction();
            }
        }).executeLater(context.getGraph());
    }

    private static void run(final GraphWriteMethods graph) {
        final int xAttr = VisualConcept.VertexAttribute.X.get(graph);
        final int yAttr = VisualConcept.VertexAttribute.Y.get(graph);
        final int zAttr = VisualConcept.VertexAttribute.Z.get(graph);

        float minx = Float.MAX_VALUE;
        float maxx = -Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;
        float minz = Float.MAX_VALUE;
        float maxz = -Float.MAX_VALUE;

        // Determine the boundaries of the graph.
        final int vxCount = graph.getVertexCount();
        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            final float x = graph.getFloatValue(xAttr, vxId);
            minx = Math.min(minx, x);
            maxx = Math.max(maxx, x);

            final float y = graph.getFloatValue(yAttr, vxId);
            miny = Math.min(miny, y);
            maxy = Math.max(maxy, y);

            final float z = graph.getFloatValue(zAttr, vxId);
            minz = Math.min(minz, z);
            maxz = Math.max(maxz, z);
        }

        final int width = (int) Math.ceil(maxx - minx + 1);
        final int height = (int) (maxy - miny + 1);

        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        final Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.drawRect(0, 0, width - 1, height - 1);
        g2d.setColor(Color.LIGHT_GRAY);

        for (int position = 0; position < vxCount; position++) {
            final int vxId = graph.getVertex(position);

            final int nNeighbours = graph.getVertexLinkCount(vxId);
            if (nNeighbours > 30) {
                final float x = graph.getFloatValue(xAttr, vxId);
                final float y = graph.getFloatValue(yAttr, vxId);
                g2d.drawLine((int) (x - minx), 0, (int) (x - minx), height - 1);
                g2d.drawLine(0, (int) (height - (y - miny) - 1), width - 1, (int) (height - (y - miny) - 1));
            }
        }

        g2d.dispose();

        final Plane plane = new Plane("Many neighbours", minx, miny, (maxz + minz) / 2, width, height, img, width, height);
        List<Plane> planes = Collections.singletonList(plane);

        final PlaneState state = new PlaneState();
        state.setPlanes(planes);
        final int planeAttr = graph.addAttribute(GraphElementType.META, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, PlaneState.ATTRIBUTE_NAME, null, null);
        graph.setObjectValue(planeAttr, 0, state);
    }
}
