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
package au.gov.asd.tac.constellation.graph.visual.framework;

import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;

/**
 * A list of hard-coded visual values that are required by CONSTELLATION's
 * renderer to display graphs. These are used by the renderer as a fallback if
 * the relevant attributes are not available on a given graph.
 *
 * @author algol
 */
public final class VisualGraphDefaults {
    
    private static final boolean DARK_MODE = JavafxStyleManager.isDarkTheme();

    private VisualGraphDefaults() {
    }

    /*
     * Graph defaults
     */
    public static final ConstellationColor DEFAULT_BACKGROUND_COLOR = DARK_MODE ? ConstellationColor.NIGHT_SKY : ConstellationColor.LIGHT_SKY;
    public static final ConstellationColor DEFAULT_HIGHLIGHT_COLOR = ConstellationColor.CHERRY;
    public static final ConstellationColor DEFAULT_MIX_COLOR = DARK_MODE ? ConstellationColor.CLOUDS : ConstellationColor.AZURE;
    public static final ConnectionMode DEFAULT_CONNECTION_MODE = ConnectionMode.EDGE;
    public static final float DEFAULT_CONNECTION_OPACITY = 1;
    public static final DrawFlags DEFAULT_DRAW_FLAGS = DrawFlags.ALL;
    public static final Camera DEFAULT_CAMERA = new Camera();
    public static final boolean DEFAULT_DISPLAY_MODE_3D = true;
    public static final boolean DEFAULT_DRAWING_MODE = false;
    public static final boolean DEFAULT_DRAWING_DIRECTED_TRANSACTIONS = true;
    public static final int DEFAULT_MAX_TRANSACTION_TO_DRAW = 8;
    public static final boolean DEFAULT_GRAPH_VISIBILITY = true;
    public static final int DEFAULT_GRAPH_VISIBILITY_VERTEX_THRESHOLD = 15000;

    /*
     * Vertex defaults
     */
    public static float getDefaultX(final int vertexId) {
        return vertexId;
    }

    public static float getDefaultY(final int vertexId) {
        return vertexId;
    }

    public static float getDefaultZ(final int vertexId) {
        return vertexId;
    }
    public static final float DEFAULT_VERTEX_X2 = 0;
    public static final float DEFAULT_VERTEX_Y2 = 0;
    public static final float DEFAULT_VERTEX_Z2 = 0;
    public static final ConstellationColor DEFAULT_VERTEX_COLOR = ConstellationColor.GREY;
    public static final String DEFAULT_VERTEX_BACKGROUND_ICON = "Background.Sphere";
    public static final String DEFAULT_VERTEX_FOREGROUND_ICON = "Unknown";
    public static final boolean DEFAULT_VERTEX_SELECTED = false;
    public static final float DEFAULT_VERTEX_VISIBILITY = 1;
    public static final float DEFAULT_VERTEX_FILTER_VISIBILITY = 1;
    public static final boolean DEFAULT_VERTEX_DIMMED = false;
    public static final float DEFAULT_VERTEX_RADIUS = 1;
    public static final Blaze DEFAULT_VERTEX_BLAZE = null;

    /*
     * Transaction defaults
     */
    public static final ConstellationColor DEFAULT_TRANSACTION_COLOR = ConstellationColor.GREY;
    public static final LineStyle DEFAULT_TRANSACTION_LINE_STYLE = LineStyle.SOLID;
    public static final float DEFAULT_TRANSACTION_WIDTH = 1;
    public static final boolean DEFAULT_TRANSACTION_SELECTED = false;
    public static final boolean DEFAULT_TRANSACTION_DIMMED = false;
    public static final float DEFAULT_TRANSACTION_VISIBILITY = 1;
    public static final float DEFAULT_TRANSACTION_FILTER_VISIBILITY = 1;

    /*
     * Label/decorator defaults
     */
    public static final VertexDecorators DEFAULT_DECORATORS = VertexDecorators.NO_DECORATORS;
    public static final GraphLabels DEFAULT_TOP_LABELS = GraphLabels.NO_LABELS;
    public static final GraphLabels DEFAULT_BOTTOM_LABELS = GraphLabels.NO_LABELS;
    public static final GraphLabels DEFAULT_CONNECTION_LABELS = GraphLabels.NO_LABELS;
    public static final float DEFAULT_LABEL_SIZE = 1;
    public static final ConstellationColor DEFAULT_LABEL_COLOR = ConstellationColor.YELLOW;

    /*
     * Blaze defaults
     */
    public static final float DEFAULT_BLAZE_OPACITY = 1.0F;
    public static final float DEFAULT_BLAZE_SIZE = 0.3F;
    public static final int DEFAULT_BLAZE_ANGLE = 45;
    public static final ConstellationColor DEFAULT_BLAZE_COLOR = ConstellationColor.LIGHT_BLUE;
}
