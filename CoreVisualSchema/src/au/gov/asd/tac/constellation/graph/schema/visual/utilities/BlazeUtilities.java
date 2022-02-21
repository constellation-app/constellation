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
package au.gov.asd.tac.constellation.graph.schema.visual.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersDialog;
import au.gov.asd.tac.constellation.plugins.gui.PluginParametersSwingDialog;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.prefs.Preferences;
import javafx.util.Pair;
import org.openide.util.NbPreferences;

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
    public static final String PRESET_PARAMETER_ID = PluginParameter.buildId(BlazeUtilities.class, "save_color_as_preset");
    public static final int MAXIMUM_CUSTOM_BLAZE_COLORS = 10;

    private BlazeUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
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
     * Display a dialog box to select a color. Option to save color as a preset
     * is represented by a checkbox. The preset will be saved in the first
     * instance of a free position, otherwise the last index of the presets will
     * be used and overridden.
     *
     * @param blazeColor The initial value of the color.
     *
     * @return a pair containing 1) if the user pressed OK and 2) the selected
     * blaze color.
     */
    public static Pair<Boolean, ConstellationColor> colorDialog(final ConstellationColor blazeColor) {
        final PluginParameters dlgParams = new PluginParameters();
        final PluginParameter<ColorParameterValue> colorParam = ColorParameterType.build(COLOR_PARAMETER_ID);
        colorParam.setName("Color");
        colorParam.setDescription(BLAZE_COLOR_PARAMETER_ID);
        dlgParams.addParameter(colorParam);

        final PluginParameter<BooleanParameterValue> presetParam = BooleanParameterType.build(PRESET_PARAMETER_ID);
        presetParam.setName("Preset");
        presetParam.setDescription("Save as Preset");
        presetParam.setBooleanValue(false);
        dlgParams.addParameter(presetParam);

        final PluginParametersSwingDialog dialog = new PluginParametersSwingDialog(BLAZE_COLOR_PARAMETER_ID, dlgParams);
        dialog.showAndWait();
        final boolean isOk = PluginParametersDialog.OK.equals(dialog.getResult());
        ConstellationColor colorResult = blazeColor;
        if (isOk) {
            colorResult = dlgParams.getColorValue(COLOR_PARAMETER_ID);
            if (dlgParams.getBooleanValue(PRESET_PARAMETER_ID)) {
                savePreset(colorResult.getJavaColor());
            }
        }
        return new Pair<>(isOk, colorResult);
    }

    /**
     * Saves a blaze color as a preset
     *
     * @param newColor the new selected color to add as a preset
     */
    public static void savePreset(final Color newColor) {
        final String colorString = getGraphPreferences().get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.BLAZE_PRESET_COLORS_DEFAULT);
        final List<String> colorsList = Arrays.asList(colorString.split(SeparatorConstants.SEMICOLON));
        final int freePosition;
        if (colorsList.indexOf("null") != -1) {
            freePosition = colorsList.indexOf("null");
        } else if (colorsList.size() < MAXIMUM_CUSTOM_BLAZE_COLORS) {
            freePosition = colorsList.size();
        } else {
            freePosition = MAXIMUM_CUSTOM_BLAZE_COLORS - 1;
        }
        savePreset(newColor, freePosition);
    }

    /**
     * Saves a blaze color as a preset
     *
     * @param newColor the new selected color to add as a preset
     * @param position
     */
    public static void savePreset(final Color newColor, final int position) {
        if (position >= 10 || position < 0) {
            return;
        }

        final String colorString = getGraphPreferences().get(GraphPreferenceKeys.BLAZE_PRESET_COLORS, GraphPreferenceKeys.BLAZE_PRESET_COLORS_DEFAULT);
        final List<String> colorsList = new ArrayList<>();

        colorsList.addAll(Arrays.asList(colorString.split(SeparatorConstants.SEMICOLON)));
        for (int i = colorsList.size(); i < MAXIMUM_CUSTOM_BLAZE_COLORS; i++) {
            colorsList.add(null);
        }
        colorsList.set(position, getHTMLColor(newColor));

        final StringBuilder preferencesBuilder = new StringBuilder();
        for (int i = 0; i < MAXIMUM_CUSTOM_BLAZE_COLORS; i++) {
            preferencesBuilder.append(colorsList.get(i));
            preferencesBuilder.append(SeparatorConstants.SEMICOLON);
        }
        getGraphPreferences().put(GraphPreferenceKeys.BLAZE_PRESET_COLORS, preferencesBuilder.toString());
    }

    /**
     * Get the HTML color from a java color
     *
     * @param color
     * @return the string representing the color in hex
     */
    public static String getHTMLColor(final Color color) {
        if (color == null) {
            return null;
        }

        final int r = color.getRed();
        final int g = color.getGreen();
        final int b = color.getBlue();
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    protected static Preferences getGraphPreferences() {
        return NbPreferences.forModule(GraphPreferenceKeys.class);
    }
}
