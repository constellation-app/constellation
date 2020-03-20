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
package au.gov.asd.tac.constellation.graph.schema.visual.utilities;

import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.BitSet;
import javafx.util.Pair;

/**
 * A utilities class to hold constants and utility methods dealing with blazes.
 *
 * @author sirius
 */
public class BlazeUtilities {

    public static final Blaze DEFAULT_BLAZE = new Blaze(45, ConstellationColor.LIGHT_BLUE);

    public static final String VERTEX_ID_PARAMETER_ID = PluginParameter.buildId(BlazeUtilities.class, "vertex_id");
    public static final String VERTEX_IDS_PARAMETER_ID = PluginParameter.buildId(BlazeUtilities.class, "vertex_ids");
    public static final String COLOR_PARAMETER_ID = PluginParameter.buildId(BlazeUtilities.class, "color");
    public static final String BLAZE_COLOR_PARAMETER_ID = PluginParameter.buildId(BlazeUtilities.class, "blaze_color");

    /**
     * Selected vertices, and the color of the blaze of the first selected
     * vertex with a blaze.
     *
     * @param graph the graph on which to get selected blazes.
     * @param blazeColor if null then the color of the first blaze detected will
     * be returned.
     *
     * @return the specified blaze color, or the color of the first blaze found.
     */
    public static Pair<BitSet, ConstellationColor> getSelection(final Graph graph, ConstellationColor blazeColor) {
        final BitSet vertices = new BitSet();
        final ReadableGraph readableGraph = graph.getReadableGraph();
        try {
            final int vertexBlazeAttributeId = VisualConcept.VertexAttribute.BLAZE.get(readableGraph);
            final int vertexSelectedAttributeId = VisualConcept.VertexAttribute.SELECTED.get(readableGraph);
            final int vertexCount = readableGraph.getVertexCount();
            for (int vertexPosition = 0; vertexPosition < vertexCount; vertexPosition++) {
                final int vertexId = readableGraph.getVertex(vertexPosition);
                final boolean selected = readableGraph.getBooleanValue(vertexSelectedAttributeId, vertexId);
                if (selected) {
                    vertices.set(vertexId);
                    if (blazeColor == null && vertexBlazeAttributeId != Graph.NOT_FOUND) {
                        final Blaze blaze = (Blaze) readableGraph.getObjectValue(vertexBlazeAttributeId, vertexId);
                        if (blaze != null) {
                            blazeColor = blaze.getColor();
                        }
                    }
                }
            }

            if (blazeColor == null) {
                blazeColor = BlazeUtilities.DEFAULT_BLAZE.getColor();
            }
        } finally {
            readableGraph.release();
        }

        return new Pair<>(vertices, blazeColor);
    }

    /**
     * Display a dialog box to select a color.
     *
     * @param blazeColor The initial value of the color.
     *
     * @return a pair containing 1) if the user pressed OK and 2) the selected
     * blaze color.
     */
    public static Pair<Boolean, ConstellationColor> colorDialog(ConstellationColor blazeColor) {
        final PluginParameters dlgParams = new PluginParameters();
        final PluginParameter<ColorParameterType.ColorParameterValue> colorParam = ColorParameterType.build(COLOR_PARAMETER_ID);
        colorParam.setName("Color");
        colorParam.setDescription(BLAZE_COLOR_PARAMETER_ID);
        if (blazeColor != null) {
            colorParam.setColorValue(blazeColor);
        }
        dlgParams.addParameter(colorParam);

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(BLAZE_COLOR_PARAMETER_ID, dlgParams);
        dialog.showAndWait();
        final boolean isOk = PluginParametersDialog.OK.equals(dialog.getResult());
        if (isOk) {
            blazeColor = dlgParams.getColorValue(COLOR_PARAMETER_ID);
        }

        return new Pair<>(isOk, blazeColor);
    }
}
