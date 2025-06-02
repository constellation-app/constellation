/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.CharacterIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;

/**
 *
 * @author cygnus_x-1
 */

@PluginInfo(pluginType = PluginType.NONE, tags = {PluginTags.EXPERIMENTAL, PluginTags.CREATE})
public class SudokuGraphBuilderPlugin extends SimpleEditPlugin {

    private static final int SIZE = 9;

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        interaction.setProgress(0, 0, "Building...", true);

        final int graphBackgroundColor = VisualConcept.GraphAttribute.BACKGROUND_COLOR.get(graph);
        graph.setObjectValue(graphBackgroundColor, 0, ConstellationColor.NIGHT_SKY);

        final int vertexIdentifierAttributeId = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        final int vertexColorAttributeId = VisualConcept.VertexAttribute.COLOR.get(graph);
        final int vertexBackgroundIconAttributeId = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(graph);
        final int vertexForegroundIconAttributeId = VisualConcept.VertexAttribute.FOREGROUND_ICON.get(graph);
        final int vertexXAttributeId = VisualConcept.VertexAttribute.X.get(graph);
        final int vertexYAttributeId = VisualConcept.VertexAttribute.Y.get(graph);
        final int vertexZAttributeId = VisualConcept.VertexAttribute.Z.get(graph);

        // setup sudoku grid
        final int[] squares = new int[SIZE * SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                final int vertexId = graph.addVertex();
                graph.setIntValue(vertexIdentifierAttributeId, vertexId, vertexId);
                graph.setObjectValue(vertexColorAttributeId, vertexId, ConstellationColor.CLOUDS);
                graph.setObjectValue(vertexBackgroundIconAttributeId, vertexId, DefaultIconProvider.EDGE_SQUARE);
                graph.setFloatValue(vertexXAttributeId, vertexId, j * 2);
                graph.setFloatValue(vertexYAttributeId, vertexId, (SIZE - 1 - i) * 2);
                graph.setFloatValue(vertexZAttributeId, vertexId, 0);
                squares[i * SIZE + j] = vertexId;
            }
        }

        final String sudoku
                = "1...7...5"
                + ".2...34.."
                + "..3....2."
                + "...4...6."
                + "84..5..91"
                + ".3...6..."
                + ".9....7.."
                + "..51...8."
                + "4...8...9";

        for (int i = 0; i < SIZE * SIZE; i++) {
            final String value = String.valueOf(sudoku.charAt(i));
            if (value.equals(SeparatorConstants.PERIOD)) {
                graph.setObjectValue(vertexColorAttributeId, squares[i], ConstellationColor.GREY);
                graph.setObjectValue(vertexForegroundIconAttributeId, squares[i], CharacterIconProvider.CHAR_003F);
            } else {
                graph.setStringValue(vertexForegroundIconAttributeId, squares[i], value);
            }
        }

        PluginExecution.withPlugin(InteractiveGraphPluginRegistry.RESET_VIEW).executeNow(graph);
        interaction.setProgress(1, 0, "Completed successfully", true);
    }
}
