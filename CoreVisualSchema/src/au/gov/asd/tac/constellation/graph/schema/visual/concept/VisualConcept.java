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
package au.gov.asd.tac.constellation.graph.schema.visual.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.TransactionAttributeNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.VertexAttributeNameAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.BlazeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.CameraAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ConnectionModeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DecoratorsAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.DrawFlagsAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.IconAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.LineStyleAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.TransactionGraphLabelsAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.VertexGraphLabelsAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.preferences.GraphPreferenceKeys;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * A SchemaConcept for elements which support visualisation of a graph.
 *
 * @author cygnus_x-1
 * @author antares
 */
@ServiceProvider(service = SchemaConcept.class)
public class VisualConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Visual";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(SchemaConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class GraphAttribute {

        static final Preferences PREFERENCES = NbPreferences.forModule(GraphPreferenceKeys.class);

        public static final SchemaAttribute BACKGROUND_COLOR = new SchemaAttribute.Builder(GraphElementType.GRAPH, ColorAttributeDescription.ATTRIBUTE_NAME, "background_color")
                .setDescription("The background color of the graph")
                .setDefaultValue(ConstellationColor.NIGHT_SKY)
                .create()
                .build();
        public static final SchemaAttribute BLAZE_OPACITY = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "blaze_opacity")
                .setDescription("The opacity of blazes on the graph")
                .setDefaultValue(PREFERENCES.getInt(GraphPreferenceKeys.BLAZE_OPACITY, GraphPreferenceKeys.BLAZE_OPACITY_DEFAULT) / 100F)
                .create()
                .build();
        public static final SchemaAttribute BLAZE_SIZE = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "blaze_size")
                .setDescription("The size of blazes on the graph")
                .setDefaultValue(PREFERENCES.getInt(GraphPreferenceKeys.BLAZE_SIZE, GraphPreferenceKeys.BLAZE_SIZE_DEFAULT) / 100F)
                .create()
                .build();
        public static final SchemaAttribute BOTTOM_LABELS = new SchemaAttribute.Builder(GraphElementType.GRAPH, VertexGraphLabelsAttributeDescription.ATTRIBUTE_NAME, "node_bottom_labels")
                .setDescription("The labels beneath nodes")
                .setDefaultValue(GraphLabels.NO_LABELS)
                .create()
                .build();
        public static final SchemaAttribute CAMERA = new SchemaAttribute.Builder(GraphElementType.GRAPH, CameraAttributeDescription.ATTRIBUTE_NAME, Camera.ATTRIBUTE_NAME)
                .setDescription("The camera used to view and navigate around the graph")
                .setDefaultValue(new Camera())
                .create()
                .build();
        public static final SchemaAttribute CONNECTION_MODE = new SchemaAttribute.Builder(GraphElementType.GRAPH, ConnectionModeAttributeDescription.ATTRIBUTE_NAME, "connection_mode")
                .setDescription("The mode in which to display connections on the graph (transaction, edge or link).")
                .setDefaultValue(ConnectionMode.EDGE)
                .create()
                .build();
        public static final SchemaAttribute CONNECTION_OPACITY = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "connection_opacity")
                .setDescription("The opacity of connections on the graph")
                .setDefaultValue(1.0)
                .create()
                .build();
        public static final SchemaAttribute DECORATORS = new SchemaAttribute.Builder(GraphElementType.GRAPH, DecoratorsAttributeDescription.ATTRIBUTE_NAME, "decorators")
                .setDescription("The decorators on nodes")
                .setDefaultValue(VertexDecorators.NO_DECORATORS)
                .create()
                .build();
        public static final SchemaAttribute DISPLAY_MODE_3D = new SchemaAttribute.Builder(GraphElementType.GRAPH, BooleanAttributeDescription.ATTRIBUTE_NAME, "3d_display")
                .setDescription("Whether or not the graph is being displayed in 3D")
                .setDefaultValue(true)
                .create()
                .build();
        public static final SchemaAttribute DRAW_DIRECTED_TRANSACTIONS = new SchemaAttribute.Builder(GraphElementType.GRAPH, BooleanAttributeDescription.ATTRIBUTE_NAME, "draw_directed_transactions")
                .setDescription("Whether or not the transactions being added in draw mode are directed")
                .setDefaultValue(true)
                .create()
                .build();
        public static final SchemaAttribute DRAW_FLAGS = new SchemaAttribute.Builder(GraphElementType.GRAPH, DrawFlagsAttributeDescription.ATTRIBUTE_NAME, "draw_flags")
                .setDescription("The graph elements to draw.")
                .setDefaultValue(DrawFlags.ALL)
                .create()
                .build();
        public static final SchemaAttribute DRAWING_MODE = new SchemaAttribute.Builder(GraphElementType.GRAPH, BooleanAttributeDescription.ATTRIBUTE_NAME, "drawing_mode")
                .setDescription("Whether or not the graph is in drawing mode (as opposed to the normal selection mode)")
                .setDefaultValue(false)
                .create()
                .build();
        public static final SchemaAttribute HIGHLIGHT_COLOR = new SchemaAttribute.Builder(GraphElementType.GRAPH, ColorAttributeDescription.ATTRIBUTE_NAME, "highlight_color")
                .setDescription("The highlight color of the graph")
                .setDefaultValue(ConstellationColor.CHERRY)
                .create()
                .build();
        public static final SchemaAttribute MAX_TRANSACTIONS = new SchemaAttribute.Builder(GraphElementType.GRAPH, IntegerAttributeDescription.ATTRIBUTE_NAME, "max_transactions")
                .setDescription("The maximum number of transactions to draw")
                .setDefaultValue(8)
                .create()
                .build();
        public static final SchemaAttribute MIX_COLOR = new SchemaAttribute.Builder(GraphElementType.GRAPH, ColorAttributeDescription.ATTRIBUTE_NAME, "mix_color")
                .setDescription("The color to use for edges and links that contain transactions with a variety of colors.")
                .setDefaultValue(ConstellationColor.CLOUDS)
                .create()
                .build();
        public static final SchemaAttribute NODE_COLOR_REFERENCE = new SchemaAttribute.Builder(GraphElementType.GRAPH, VertexAttributeNameAttributeDescription.ATTRIBUTE_NAME, "node_color_reference")
                .setDescription("The name of the node attribute that will determine node colors")
                .create()
                .build();
        public static final SchemaAttribute TOP_LABELS = new SchemaAttribute.Builder(GraphElementType.GRAPH, VertexGraphLabelsAttributeDescription.ATTRIBUTE_NAME, "node_top_labels")
                .setDescription("The labels above nodes")
                .setDefaultValue(GraphLabels.NO_LABELS)
                .create()
                .build();
        public static final SchemaAttribute TRANSACTION_COLOR_REFERENCE = new SchemaAttribute.Builder(GraphElementType.GRAPH, TransactionAttributeNameAttributeDescription.ATTRIBUTE_NAME, "transaction_color_reference")
                .setDescription("The name of the transaction attribute that will determine transaction colors")
                .create()
                .build();
        public static final SchemaAttribute TRANSACTION_LABELS = new SchemaAttribute.Builder(GraphElementType.GRAPH, TransactionGraphLabelsAttributeDescription.ATTRIBUTE_NAME, "transaction_labels")
                .setDescription("The labels on transactions")
                .setDefaultValue(GraphLabels.NO_LABELS)
                .create()
                .build();
        public static final SchemaAttribute VISIBLE_ABOVE_THRESHOLD = new SchemaAttribute.Builder(GraphElementType.GRAPH, BooleanAttributeDescription.ATTRIBUTE_NAME, "visible_above_threshold")
                .setDescription("Whether or not the graph is visible even when the maximum number of nodes is exceeded.")
                .setDefaultValue(true)
                .create()
                .build();
        public static final SchemaAttribute VISIBILITY_THRESHOLD = new SchemaAttribute.Builder(GraphElementType.GRAPH, IntegerAttributeDescription.ATTRIBUTE_NAME, "visibility_threshold")
                .setDescription("The maximum number of nodes to display")
                .setDefaultValue(50000)
                .create()
                .build();
        public static final SchemaAttribute MOTION = new SchemaAttribute.Builder(GraphElementType.GRAPH, FloatAttributeDescription.ATTRIBUTE_NAME, "motion")
                .setDescription("the current motion of the direction indicatior annimation")
                .setDefaultValue(-1.0F)
                .create()
                .build();
    }

    public static class VertexAttribute {

        public static final SchemaAttribute BACKGROUND_ICON = new SchemaAttribute.Builder(GraphElementType.VERTEX, IconAttributeDescription.ATTRIBUTE_NAME, "background_icon")
                .setDescription("The background icon of the vertex")
                .setDefaultValue(DefaultIconProvider.FLAT_SQUARE.getExtendedName())
                .create()
                .build();
        public static final SchemaAttribute BLAZE = new SchemaAttribute.Builder(GraphElementType.VERTEX, BlazeAttributeDescription.ATTRIBUTE_NAME, "blaze")
                .setDescription("The blaze on this vertex")
                .build();
        public static final SchemaAttribute COLOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "color")
                .setDescription("The color of the vertex")
                .create()
                .build();
        public static final SchemaAttribute DIMMED = new SchemaAttribute.Builder(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "dim")
                .setDescription("Specifies whether the vertex is displayed in a dimmed state")
                .setDefaultValue(false)
                .setIndexType(GraphIndexType.UNORDERED)
                .create()
                .build();
        public static final SchemaAttribute FOREGROUND_ICON = new SchemaAttribute.Builder(GraphElementType.VERTEX, IconAttributeDescription.ATTRIBUTE_NAME, "icon")
                .setDescription("The icon of the vertex")
                .create()
                .build();
        public static final SchemaAttribute IDENTIFIER = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Identifier")
                .setDescription("The identifier of the node")
                .create()
                .build();
        public static final SchemaAttribute LABEL = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Label")
                .setDescription("The label of the vertex")
                .create()
                .build();
        public static final SchemaAttribute LABEL_RADIUS = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "lradius")
                .setDescription("The radius of the label")
                .setDefaultValue(1.0F)
                .build();
        public static final SchemaAttribute NODE_RADIUS = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "nradius")
                .setDescription("The radius of the vertex")
                .setDefaultValue(1.0F)
                .create()
                .build();
        public static final SchemaAttribute OVERLAY_COLOR = new SchemaAttribute.Builder(GraphElementType.VERTEX, ColorAttributeDescription.ATTRIBUTE_NAME, "overlay_color")
                .setDescription("The overlay color of the vertex")
                .build();
        public static final SchemaAttribute SELECTED = new SchemaAttribute.Builder(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected")
                .setDescription("Is the vertex selected?")
                .setDefaultValue(false)
                .setIndexType(GraphIndexType.UNORDERED)
                .create()
                .build();
        public static final SchemaAttribute VISIBILITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "visibility")
                .setDescription("The visibility of the vertex")
                .setDefaultValue(1.0F)
                .create()
                .build();
        public static final SchemaAttribute X = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x")
                .setDescription("The x coordinate of the vertex")
                .setDefaultValue(0.0F)
                .create()
                .build();
        public static final SchemaAttribute X2 = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "x2")
                .setDescription("The alternative x coordinate of the vertex")
                .setDefaultValue(0.0F)
                .create()
                .build();
        public static final SchemaAttribute Y = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y")
                .setDescription("The y coordinate of the vertex")
                .setDefaultValue(0.0F)
                .create()
                .build();
        public static final SchemaAttribute Y2 = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "y2")
                .setDescription("The alternative y coordinate of the vertex")
                .setDefaultValue(0.0F)
                .create()
                .build();
        public static final SchemaAttribute Z = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z")
                .setDescription("The z coordinate of the vertex")
                .setDefaultValue(0.0F)
                .create()
                .build();
        public static final SchemaAttribute Z2 = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatAttributeDescription.ATTRIBUTE_NAME, "z2")
                .setDescription("The alternative z coordinate of the vertex")
                .setDefaultValue(0.0F)
                .create()
                .build();
        public static final SchemaAttribute PINNED = new SchemaAttribute.Builder(GraphElementType.VERTEX, BooleanAttributeDescription.ATTRIBUTE_NAME, "pinned")
                .setDescription("Is the vertex position pinned?")
                .setDefaultValue(false)
                .setDecorator(true)
                .create()
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute COLOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ColorAttributeDescription.ATTRIBUTE_NAME, "color")
                .setDescription("The color of the transaction")
                .create()
                .build();
        public static final SchemaAttribute DIMMED = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "dim")
                .setDescription("Specified whether the transaction is displayed in a dimmed state")
                .setDefaultValue(false)
                .setIndexType(GraphIndexType.UNORDERED)
                .create()
                .build();
        public static final SchemaAttribute DIRECTED = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "directed")
                .setDescription("Is the transaction directed?")
                .create()
                .build();
        public static final SchemaAttribute IDENTIFIER = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Identifier")
                .setDescription("The identifier of the transaction")
                .create()
                .build();
        public static final SchemaAttribute LINE_STYLE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, LineStyleAttributeDescription.ATTRIBUTE_NAME, "line_style")
                .setDescription("The line style of the transaction")
                .setDefaultValue(LineStyle.SOLID)
                .create()
                .build();
        public static final SchemaAttribute LABEL = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Label")
                .setDescription("The label of the transaction")
                .create()
                .build();
        public static final SchemaAttribute OVERLAY_COLOR = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ColorAttributeDescription.ATTRIBUTE_NAME, "overlay_color")
                .setDescription("The overlay colore of the transaction")
                .build();
        public static final SchemaAttribute SELECTED = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, BooleanAttributeDescription.ATTRIBUTE_NAME, "selected")
                .setDescription("Is the transaction selected?")
                .setDefaultValue(false)
                .setIndexType(GraphIndexType.UNORDERED)
                .create()
                .build();
        public static final SchemaAttribute VISIBILITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "visibility")
                .setDescription("The visibility of the transaction")
                .setDefaultValue(1.0F)
                .create()
                .build();
        public static final SchemaAttribute WIDTH = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatAttributeDescription.ATTRIBUTE_NAME, "width")
                .setDescription("The width of the transaction")
                .setDefaultValue(1.0F)
                .create()
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(GraphAttribute.BACKGROUND_COLOR);
        schemaAttributes.add(GraphAttribute.BLAZE_OPACITY);
        schemaAttributes.add(GraphAttribute.BLAZE_SIZE);
        schemaAttributes.add(GraphAttribute.BOTTOM_LABELS);
        schemaAttributes.add(GraphAttribute.CAMERA);
        schemaAttributes.add(GraphAttribute.CONNECTION_MODE);
        schemaAttributes.add(GraphAttribute.CONNECTION_OPACITY);
        schemaAttributes.add(GraphAttribute.DECORATORS);
        schemaAttributes.add(GraphAttribute.DISPLAY_MODE_3D);
        schemaAttributes.add(GraphAttribute.DRAW_DIRECTED_TRANSACTIONS);
        schemaAttributes.add(GraphAttribute.DRAW_FLAGS);
        schemaAttributes.add(GraphAttribute.DRAWING_MODE);
        schemaAttributes.add(GraphAttribute.HIGHLIGHT_COLOR);
        schemaAttributes.add(GraphAttribute.MAX_TRANSACTIONS);
        schemaAttributes.add(GraphAttribute.MIX_COLOR);
        schemaAttributes.add(GraphAttribute.NODE_COLOR_REFERENCE);
        schemaAttributes.add(GraphAttribute.TOP_LABELS);
        schemaAttributes.add(GraphAttribute.TRANSACTION_COLOR_REFERENCE);
        schemaAttributes.add(GraphAttribute.TRANSACTION_LABELS);
        schemaAttributes.add(GraphAttribute.VISIBLE_ABOVE_THRESHOLD);
        schemaAttributes.add(GraphAttribute.VISIBILITY_THRESHOLD);
        schemaAttributes.add(GraphAttribute.MOTION);
        schemaAttributes.add(VertexAttribute.BACKGROUND_ICON);
        schemaAttributes.add(VertexAttribute.BLAZE);
        schemaAttributes.add(VertexAttribute.COLOR);
        schemaAttributes.add(VertexAttribute.DIMMED);
        schemaAttributes.add(VertexAttribute.FOREGROUND_ICON);
        schemaAttributes.add(VertexAttribute.IDENTIFIER);
        schemaAttributes.add(VertexAttribute.LABEL);
        schemaAttributes.add(VertexAttribute.LABEL_RADIUS);
        schemaAttributes.add(VertexAttribute.NODE_RADIUS);
        schemaAttributes.add(VertexAttribute.OVERLAY_COLOR);
        schemaAttributes.add(VertexAttribute.SELECTED);
        schemaAttributes.add(VertexAttribute.VISIBILITY);
        schemaAttributes.add(VertexAttribute.X);
        schemaAttributes.add(VertexAttribute.X2);
        schemaAttributes.add(VertexAttribute.Y);
        schemaAttributes.add(VertexAttribute.Y2);
        schemaAttributes.add(VertexAttribute.Z);
        schemaAttributes.add(VertexAttribute.Z2);
        schemaAttributes.add(VertexAttribute.PINNED);
        schemaAttributes.add(TransactionAttribute.COLOR);
        schemaAttributes.add(TransactionAttribute.DIMMED);
        schemaAttributes.add(TransactionAttribute.DIRECTED);
        schemaAttributes.add(TransactionAttribute.IDENTIFIER);
        schemaAttributes.add(TransactionAttribute.LINE_STYLE);
        schemaAttributes.add(TransactionAttribute.LABEL);
        schemaAttributes.add(TransactionAttribute.OVERLAY_COLOR);
        schemaAttributes.add(TransactionAttribute.SELECTED);
        schemaAttributes.add(TransactionAttribute.VISIBILITY);
        schemaAttributes.add(TransactionAttribute.WIDTH);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}
