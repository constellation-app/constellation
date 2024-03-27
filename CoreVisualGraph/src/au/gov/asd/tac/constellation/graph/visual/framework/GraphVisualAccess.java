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
package au.gov.asd.tac.constellation.graph.visual.framework;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.LayersConcept;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.BooleanAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabel;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Blaze;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess.ConnectionDirection;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The standard implementation of {@link VisualAccess} for a single
 * CONSTELLATION {@link Graph}.
 * <p>
 * This matches attributes found in {@link VisualConcept} to their obvious
 * counterparts in the visual access interface. IDs are stored for these
 * attributes for efficiency purposes, and mod counts are stored to determine if
 * a property has changed when {@link #updateInternally updateInteranlly()} is
 * called. When {@link #getIndigenousChanges getIndigenousChanges()} is called,
 * the mod counts are updated and a list of visual changes corresponding to
 * those attributes with altered mod counts is returned.
 * <p>
 * The handling of certain properties like label text, are a little more tricky,
 * but the aforementioned basic principles still apply in theory.
 *
 * @author twilight_sparkle
 * @author antares
 */
public final class GraphVisualAccess implements VisualAccess {

    private int graphBackgroundColor = Graph.NOT_FOUND;
    private int graphHighlightColor = Graph.NOT_FOUND;
    private int graphBlazeOpacity = Graph.NOT_FOUND;
    private int graphBlazeSize = Graph.NOT_FOUND;
    private int graphDecorators = Graph.NOT_FOUND;
    private int graphTopLabels = Graph.NOT_FOUND;
    private int graphBottomLabels = Graph.NOT_FOUND;
    private int graphConnectionLabels = Graph.NOT_FOUND;
    private int graphConnectionOpacity = Graph.NOT_FOUND;
    private int graphConnectionMotion = Graph.NOT_FOUND;
    private int graphConnectionMode = Graph.NOT_FOUND;
    private int graphDrawFlags = Graph.NOT_FOUND;
    private int graphCamera = Graph.NOT_FOUND;
    private int graphVertexColorRef = Graph.NOT_FOUND;
    private int graphTransactionColorRef = Graph.NOT_FOUND;
    private int graphMixColor = Graph.NOT_FOUND;
    private int graphMaxTransactions = Graph.NOT_FOUND;
    private int graphVisibleAboveThreshold = Graph.NOT_FOUND;
    private int graphVisibilityThreshold = Graph.NOT_FOUND;
    private int vertexX = Graph.NOT_FOUND;
    private int vertexY = Graph.NOT_FOUND;
    private int vertexZ = Graph.NOT_FOUND;
    private int vertexX2 = Graph.NOT_FOUND;
    private int vertexY2 = Graph.NOT_FOUND;
    private int vertexZ2 = Graph.NOT_FOUND;
    private int vertexColor = Graph.NOT_FOUND;
    private int vertexBackgroundIcon = Graph.NOT_FOUND;
    private int vertexForegroundIcon = Graph.NOT_FOUND;
    private int vertexSelected = Graph.NOT_FOUND;
    private int vertexVisibility = Graph.NOT_FOUND;
    private int vertexLayerVisibility = Graph.NOT_FOUND;
    private int vertexDimmed = Graph.NOT_FOUND;
    private int vertexRadius = Graph.NOT_FOUND;
    private int vertexBlaze = Graph.NOT_FOUND;
    private int transactionColor = Graph.NOT_FOUND;
    private int transactionSelected = Graph.NOT_FOUND;
    private int transactionDirected = Graph.NOT_FOUND;
    private int transactionVisibility = Graph.NOT_FOUND;
    private int transactionLayerVisibility = Graph.NOT_FOUND;
    private int transactionDimmed = Graph.NOT_FOUND;
    private int transactionLineStyle = Graph.NOT_FOUND;
    private int transactionWidth = Graph.NOT_FOUND;

    private int nwDecorator = Graph.NOT_FOUND;
    private int neDecorator = Graph.NOT_FOUND;
    private int swDecorator = Graph.NOT_FOUND;
    private int seDecorator = Graph.NOT_FOUND;

    private int[] topLabelAttrs = new int[0];
    private float[] topLabelSizes = new float[0];
    private ConstellationColor[] topLabelColors = new ConstellationColor[0];
    private int[] bottomLabelAttrs = new int[0];
    private float[] bottomLabelSizes = new float[0];
    private ConstellationColor[] bottomLabelColors = new ConstellationColor[0];
    private int[] connectionLabelAttrs = new int[0];
    private float[] connectionLabelSizes = new float[0];
    private ConstellationColor[] connectionLabelColors = new ConstellationColor[0];

    private final Graph graph;
    private ReadableGraph accessGraph;
    private ConnectionMode connectionMode;

    private long globalModCount = -1;
    private long structureModCount = -1;
    private long attributeModCount = -1;
    private final Map<SchemaAttribute, Long> modCounts = new HashMap<>();
    private long[] topLabelModCounts = new long[0];
    private long[] bottomLabelModCounts = new long[0];
    private long[] connectionLabelModCounts = new long[0];
    private long nwDecoratorModCount = -1;
    private long neDecoratorModCount = -1;
    private long swDecoratorModCount = -1;
    private long seDecoratorModCount = -1;

    private GraphElementType[] connectionElementTypes = new GraphElementType[0];
    private int[] connectionElementIds = new int[0];
    private int[] linkStartingPositions = new int[0];

    public GraphVisualAccess(final Graph graph) {
        this.graph = graph;
    }

    @Override
    public List<VisualChange> getIndigenousChanges() {
        return update(true);
    }

    @Override
    public void beginUpdate() {
        accessGraph = graph.getReadableGraph();
    }

    @Override
    public void endUpdate() {
        accessGraph.release();
        accessGraph = null;
    }

    @Override
    public void updateInternally() {
        update(false);
    }

    private List<VisualChange> update(final boolean recordChanges) {
        final List<VisualChange> changes = new ArrayList<>();

        final long currentGlobalModCount = accessGraph.getGlobalModificationCounter();
        if (currentGlobalModCount != globalModCount) {
            if (recordChanges) {
                globalModCount = currentGlobalModCount;
            }
            long count;
            boolean attributesChanged = false;
            boolean verticesRebuilding = false;
            boolean connectionsRebuilding = false;

            // Handle structural changes
            count = accessGraph.getStructureModificationCounter();
            if (count != structureModCount) {
                connectionsRebuilding = true;
                verticesRebuilding = true;
                if (recordChanges) {
                    structureModCount = count;
                }
            }

            // Handle attribute changes (as distinct from changes to attribute values).
            count = accessGraph.getAttributeModificationCounter();
            if (count != attributeModCount) {
                attributesChanged = true;
                recalculateVisualAttributes(accessGraph);
                if (recordChanges) {
                    attributeModCount = count;
                }
            }

            // Handle changes to the connection mode
            count = graphConnectionMode == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphConnectionMode);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.CONNECTION_MODE))) {
                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.CONNECTION_MODE, count);
                }
                recalculateConnectionMode(accessGraph);
                connectionsRebuilding = true;
            }

            // Handle changes to max transactions
            count = graphMaxTransactions == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphMaxTransactions);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.MAX_TRANSACTIONS))) {
                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.MAX_TRANSACTIONS, count);
                }
                connectionsRebuilding = true;
            }

            // Handle changes to the graph's decorators attribute
            count = graphDecorators == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphDecorators);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.DECORATORS)) || attributesChanged) {
                final int oldNwDecorator = nwDecorator;
                final int oldNeDecorator = neDecorator;
                final int oldSeDecorator = seDecorator;
                final int oldSwDecorator = swDecorator;
                recalculateDecorators(accessGraph);
                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.DECORATORS, count);
                    if (oldNwDecorator != nwDecorator) {
                        changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_NW_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                    }
                    if (oldNeDecorator != neDecorator) {
                        changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_NE_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                    }
                    if (oldSeDecorator != seDecorator) {
                        changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_SE_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                    }
                    if (oldSwDecorator != swDecorator) {
                        changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_SW_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                    }
                }
            }

            // Handle changes to the graph's decorators referred attributes
            if (recordChanges) {
                count = nwDecorator == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(nwDecorator);
                if (count != nwDecoratorModCount) {
                    nwDecoratorModCount = count;
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_NW_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                }
                count = neDecorator == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(neDecorator);
                if (count != neDecoratorModCount) {
                    neDecoratorModCount = count;
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_NE_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                }
                count = seDecorator == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(seDecorator);
                if (count != seDecoratorModCount) {
                    seDecoratorModCount = count;
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_SE_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                }
                count = swDecorator == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(swDecorator);
                if (count != swDecoratorModCount) {
                    swDecoratorModCount = count;
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_SW_DECORATOR).forItems(accessGraph.getVertexCount()).build());
                }
            }

            // Handle changes to the graph's top label attribute
            count = graphTopLabels == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphTopLabels);
            final int[] oldTopLabels = Arrays.copyOf(topLabelAttrs, topLabelAttrs.length);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.TOP_LABELS)) || attributesChanged) {
                final ConstellationColor[] oldTopLabelColors = Arrays.copyOf(topLabelColors, topLabelColors.length);
                final float[] oldTopLabelSizes = Arrays.copyOf(topLabelSizes, topLabelSizes.length);
                recalculateTopLabels(accessGraph);

                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.TOP_LABELS, count);
                    if (!Arrays.equals(topLabelAttrs, oldTopLabels)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.TOP_LABELS_REBUILD).build());
                    }
                    if (!Arrays.equals(topLabelSizes, oldTopLabelSizes)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.TOP_LABEL_SIZE).forItems(topLabelAttrs.length).build());
                    }
                    if (!Arrays.equals(topLabelColors, oldTopLabelColors)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.TOP_LABEL_COLOR).forItems(topLabelAttrs.length).build());
                    }
                }
            }

            // Handle changes to the attributes referred to for the graph's top labels
            if (recordChanges) {
                boolean labelTextChanged = false;
                topLabelModCounts = Arrays.copyOf(topLabelModCounts, topLabelAttrs.length);
                for (int i = 0; i < topLabelAttrs.length; i++) {
                    final int attr = topLabelAttrs[i];
                    count = attr == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(attr);
                    if (i >= oldTopLabels.length || attr != oldTopLabels[i] || count != topLabelModCounts[i]) {
                        topLabelModCounts[i] = count;
                        labelTextChanged = true;
                    }
                }
                if (labelTextChanged) {
                    changes.add(new VisualChangeBuilder(VisualProperty.TOP_LABEL_TEXT).forItems(accessGraph.getVertexCount() * topLabelAttrs.length).build());
                }
            }

            // Handle changes to the graph's bottom label attribute
            count = graphBottomLabels == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphBottomLabels);
            final int[] oldBottomLabels = Arrays.copyOf(bottomLabelAttrs, bottomLabelAttrs.length);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.BOTTOM_LABELS)) || attributesChanged) {
                final ConstellationColor[] oldBottomLabelColors = Arrays.copyOf(bottomLabelColors, bottomLabelColors.length);
                final float[] oldBottomLabelSizes = Arrays.copyOf(bottomLabelSizes, bottomLabelSizes.length);
                recalculateBottomLabels(accessGraph);
                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.BOTTOM_LABELS, count);
                    if (!Arrays.equals(bottomLabelAttrs, oldBottomLabels)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.BOTTOM_LABELS_REBUILD).build());
                    }
                    if (!Arrays.equals(bottomLabelSizes, oldBottomLabelSizes)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.BOTTOM_LABEL_SIZE).forItems(bottomLabelAttrs.length).build());
                    }
                    if (!Arrays.equals(bottomLabelColors, oldBottomLabelColors)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.BOTTOM_LABEL_COLOR).forItems(bottomLabelAttrs.length).build());
                    }
                }
            }

            // Handle changes to the attributes referred to for the graph's bottom labels
            if (recordChanges) {
                boolean labelTextChanged = false;
                bottomLabelModCounts = Arrays.copyOf(bottomLabelModCounts, bottomLabelAttrs.length);
                for (int i = 0; i < bottomLabelAttrs.length; i++) {
                    final int attr = bottomLabelAttrs[i];
                    count = attr == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(attr);
                    if (i >= oldBottomLabels.length || attr != oldBottomLabels[i] || count != bottomLabelModCounts[i]) {
                        bottomLabelModCounts[i] = count;
                        labelTextChanged = true;
                    }
                }
                if (labelTextChanged) {
                    changes.add(new VisualChangeBuilder(VisualProperty.BOTTOM_LABEL_TEXT).forItems(accessGraph.getVertexCount() * bottomLabelAttrs.length).build());
                }
            }

            // Handle changes to the graph's connection label attribute
            count = graphConnectionLabels == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphConnectionLabels);
            final int[] oldConnectionLabels = Arrays.copyOf(connectionLabelAttrs, connectionLabelAttrs.length);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.TRANSACTION_LABELS)) || attributesChanged) {
                final ConstellationColor[] oldConnectionLabelColors = Arrays.copyOf(connectionLabelColors, connectionLabelColors.length);
                final float[] oldConnectionLabelSizes = Arrays.copyOf(connectionLabelSizes, connectionLabelSizes.length);
                recalculateConnectionLabels(accessGraph);
                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.TRANSACTION_LABELS, count);
                    if (!Arrays.equals(connectionLabelAttrs, oldConnectionLabels)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_LABELS_REBUILD).build());
                    }
                    if (!Arrays.equals(connectionLabelSizes, oldConnectionLabelSizes)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_LABEL_SIZE).forItems(connectionLabelAttrs.length).build());
                    }
                    if (!Arrays.equals(connectionLabelColors, oldConnectionLabelColors)) {
                        changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_LABEL_COLOR).forItems(connectionLabelAttrs.length).build());
                    }
                }
            }

            // Handle changes to the attributes referred to for the graph's connection labels
            if (recordChanges) {
                boolean labelTextChanged = false;
                connectionLabelModCounts = Arrays.copyOf(connectionLabelModCounts, connectionLabelAttrs.length);
                for (int i = 0; i < connectionLabelAttrs.length; i++) {
                    final int attr = connectionLabelAttrs[i];
                    count = attr == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(attr);
                    if (i >= oldConnectionLabels.length || attr != oldConnectionLabels[i] || count != connectionLabelModCounts[i]) {
                        connectionLabelModCounts[i] = count;
                        labelTextChanged = true;
                    }
                }
                if (labelTextChanged) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_LABEL_TEXT).forItems(connectionElementTypes.length * connectionLabelAttrs.length).build());
                }
            }

            // Handle changes to the graph's referred vertex color attribute
            boolean vertexColorChanged = false;
            count = graphVertexColorRef == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphVertexColorRef);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE)) || attributesChanged) {
                final int oldVertexColor = vertexColor;
                recalculateVertexColorAttribute(accessGraph);
                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE, count);
                    vertexColorChanged = oldVertexColor != vertexColor;
                }
            }

            // Handle changes to vertex colors
            if (recordChanges) {
                count = vertexColor == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexColor);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.COLOR, count)) || vertexColorChanged) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_COLOR).forItems(accessGraph.getVertexCount()).build());
                }
            }

            // Handle changes to the graph's referred transaction color attribute
            boolean transactionColorChanged = false;
            count = graphTransactionColorRef == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphTransactionColorRef);
            if (!Objects.equals(count, modCounts.get(VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE)) || attributesChanged) {
                final int oldTransactionColor = transactionColor;
                recalculateTransactionColorAttribute(accessGraph);
                if (recordChanges) {
                    modCounts.put(VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE, count);
                    transactionColorChanged = oldTransactionColor != transactionColor;
                }
            }

            // Handle changes to transaction colors
            if (recordChanges) {
                count = transactionColor == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionColor);
                if (!Objects.equals(count, modCounts.put(VisualConcept.TransactionAttribute.COLOR, count)) || transactionColorChanged) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_COLOR).forItems(connectionElementTypes.length).build());
                }
            }

            // Do all structural stuff
            if (verticesRebuilding || connectionsRebuilding) {
                recalculateStructure(accessGraph);
                if (recordChanges && verticesRebuilding) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTICES_REBUILD).build());
                }
                if (recordChanges && connectionsRebuilding) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTIONS_REBUILD).build());
                }
            }

            if (recordChanges) {
                // Handle changes to stand-alone graph visual attributes
                count = graphBackgroundColor == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphBackgroundColor);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.BACKGROUND_COLOR, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.BACKGROUND_COLOR).forItems(1).build());
                }
                count = graphHighlightColor == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphHighlightColor);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.HIGHLIGHT_COLOR, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.HIGHLIGHT_COLOR).forItems(1).build());
                }
                count = graphBlazeOpacity == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphBlazeOpacity);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.BLAZE_OPACITY, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.BLAZE_OPACITY).forItems(1).build());
                }
                count = graphBlazeSize == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphBlazeSize);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.BLAZE_SIZE, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.BLAZE_SIZE).forItems(1).build());
                }
                count = graphConnectionOpacity == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphConnectionOpacity);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.CONNECTION_OPACITY, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTIONS_OPACITY).forItems(1).build());
                }
                
                count = graphConnectionMotion == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphConnectionMotion);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.CONNECTION_MOTION, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTIONS_MOTION).forItems(1).build());
                }
                
                count = graphDrawFlags == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphDrawFlags);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.DRAW_FLAGS, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.DRAW_FLAGS).forItems(1).build());
                }
                count = graphCamera == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphCamera);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.CAMERA, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CAMERA).forItems(1).build());
                }
                count = graphMixColor == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphMixColor);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.MIX_COLOR, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_COLOR).forItems(connectionElementTypes.length).build());
                }
                count = graphVisibleAboveThreshold == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphVisibleAboveThreshold);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.VISIBLE_ABOVE_THRESHOLD, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VISIBLE_ABOVE_THRESHOLD).forItems(1).build());
                }
                count = graphVisibilityThreshold == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(graphVisibilityThreshold);
                if (!Objects.equals(count, modCounts.put(VisualConcept.GraphAttribute.VISIBILITY_THRESHOLD, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VISIBILITY_THRESHOLD).forItems(1).build());
                }

                // Handle stand-alone changes to vertex visual attributes
                count = vertexX == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexX);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.X, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_X).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexY == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexY);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.Y, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_Y).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexZ == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexZ);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.Z, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_Z).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexX2 == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexX2);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.X2, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_X2).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexY2 == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexY2);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.Y2, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_Y2).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexZ2 == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexZ2);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.Z2, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_Z2).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexBackgroundIcon == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexBackgroundIcon);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.BACKGROUND_ICON, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_BACKGROUND_ICON).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexForegroundIcon == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexForegroundIcon);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.FOREGROUND_ICON, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_FOREGROUND_ICON).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexSelected == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexSelected);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.SELECTED, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_SELECTED).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexVisibility == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexVisibility);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.VISIBILITY, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_VISIBILITY).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexLayerVisibility == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexLayerVisibility);
                if (!Objects.equals(count, modCounts.put(LayersConcept.VertexAttribute.LAYER_VISIBILITY, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_VISIBILITY).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexDimmed == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexDimmed);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.DIMMED, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_DIM).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexRadius == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexRadius);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.NODE_RADIUS, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_RADIUS).forItems(accessGraph.getVertexCount()).build());
                }
                count = vertexBlaze == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(vertexBlaze);
                if (!Objects.equals(count, modCounts.put(VisualConcept.VertexAttribute.BLAZE, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_BLAZED).forItems(accessGraph.getVertexCount()).build());
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_BLAZE_ANGLE).forItems(accessGraph.getVertexCount()).build());
                    changes.add(new VisualChangeBuilder(VisualProperty.VERTEX_BLAZE_COLOR).forItems(accessGraph.getVertexCount()).build());
                }

                // Handle stand-alone changes to transaction visual attributes
                count = transactionSelected == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionSelected);
                if (!Objects.equals(count, modCounts.put(VisualConcept.TransactionAttribute.SELECTED, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_SELECTED).forItems(connectionElementTypes.length).build());
                }
                count = transactionDirected == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionDirected);
                if (!Objects.equals(count, modCounts.put(VisualConcept.TransactionAttribute.DIRECTED, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_DIRECTED).forItems(connectionElementTypes.length).build());
                }
                count = transactionVisibility == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionVisibility);
                if (!Objects.equals(count, modCounts.put(VisualConcept.TransactionAttribute.VISIBILITY, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_VISIBILITY).forItems(connectionElementTypes.length).build());
                }
                count = transactionLayerVisibility == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionLayerVisibility);
                if (!Objects.equals(count, modCounts.put(LayersConcept.TransactionAttribute.LAYER_VISIBILITY, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_VISIBILITY).forItems(connectionElementTypes.length).build());
                }
                count = transactionDimmed == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionDimmed);
                if (!Objects.equals(count, modCounts.put(VisualConcept.TransactionAttribute.DIMMED, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_DIM).forItems(connectionElementTypes.length).build());
                }
                count = transactionLineStyle == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionLineStyle);
                if (!Objects.equals(count, modCounts.put(VisualConcept.TransactionAttribute.LINE_STYLE, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_LINESTYLE).forItems(connectionElementTypes.length).build());
                }
                count = transactionWidth == Graph.NOT_FOUND ? -1 : accessGraph.getValueModificationCounter(transactionWidth);
                if (!Objects.equals(count, modCounts.put(VisualConcept.TransactionAttribute.WIDTH, count))) {
                    changes.add(new VisualChangeBuilder(VisualProperty.CONNECTION_WIDTH).forItems(connectionElementTypes.length).build());
                }
            }
        }
        return changes;
    }

    private void recalculateVisualAttributes(final GraphReadMethods rg) {
        graphBackgroundColor = VisualConcept.GraphAttribute.BACKGROUND_COLOR.get(rg);
        graphHighlightColor = VisualConcept.GraphAttribute.HIGHLIGHT_COLOR.get(rg);
        graphBlazeOpacity = VisualConcept.GraphAttribute.BLAZE_OPACITY.get(rg);
        graphBlazeSize = VisualConcept.GraphAttribute.BLAZE_SIZE.get(rg);
        graphDecorators = VisualConcept.GraphAttribute.DECORATORS.get(rg);
        graphTopLabels = VisualConcept.GraphAttribute.TOP_LABELS.get(rg);
        graphBottomLabels = VisualConcept.GraphAttribute.BOTTOM_LABELS.get(rg);
        graphConnectionLabels = VisualConcept.GraphAttribute.TRANSACTION_LABELS.get(rg);
        graphConnectionOpacity = VisualConcept.GraphAttribute.CONNECTION_OPACITY.get(rg);
        graphConnectionMotion = VisualConcept.GraphAttribute.CONNECTION_MOTION.get(rg);
        graphConnectionMode = VisualConcept.GraphAttribute.CONNECTION_MODE.get(rg);
        graphDrawFlags = VisualConcept.GraphAttribute.DRAW_FLAGS.get(rg);
        graphCamera = VisualConcept.GraphAttribute.CAMERA.get(rg);
        graphVertexColorRef = VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE.get(rg);
        graphTransactionColorRef = VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE.get(rg);
        graphMixColor = VisualConcept.GraphAttribute.MIX_COLOR.get(rg);
        graphMaxTransactions = VisualConcept.GraphAttribute.MAX_TRANSACTIONS.get(rg);
        graphVisibleAboveThreshold = VisualConcept.GraphAttribute.VISIBLE_ABOVE_THRESHOLD.get(rg);
        graphVisibilityThreshold = VisualConcept.GraphAttribute.VISIBILITY_THRESHOLD.get(rg);

        vertexX = VisualConcept.VertexAttribute.X.get(rg);
        vertexY = VisualConcept.VertexAttribute.Y.get(rg);
        vertexZ = VisualConcept.VertexAttribute.Z.get(rg);
        vertexX2 = VisualConcept.VertexAttribute.X2.get(rg);
        vertexY2 = VisualConcept.VertexAttribute.Y2.get(rg);
        vertexZ2 = VisualConcept.VertexAttribute.Z2.get(rg);
        vertexBackgroundIcon = VisualConcept.VertexAttribute.BACKGROUND_ICON.get(rg);
        vertexForegroundIcon = VisualConcept.VertexAttribute.FOREGROUND_ICON.get(rg);
        vertexSelected = VisualConcept.VertexAttribute.SELECTED.get(rg);
        vertexVisibility = VisualConcept.VertexAttribute.VISIBILITY.get(rg);
        vertexLayerVisibility = LayersConcept.VertexAttribute.LAYER_VISIBILITY.get(rg);
        vertexDimmed = VisualConcept.VertexAttribute.DIMMED.get(rg);
        vertexRadius = VisualConcept.VertexAttribute.NODE_RADIUS.get(rg);
        vertexBlaze = VisualConcept.VertexAttribute.BLAZE.get(rg);

        transactionSelected = VisualConcept.TransactionAttribute.SELECTED.get(rg);
        transactionDirected = VisualConcept.TransactionAttribute.DIRECTED.get(rg);
        transactionVisibility = VisualConcept.TransactionAttribute.VISIBILITY.get(rg);
        transactionLayerVisibility = LayersConcept.TransactionAttribute.LAYER_VISIBILITY.get(rg);
        transactionDimmed = VisualConcept.TransactionAttribute.DIMMED.get(rg);
        transactionLineStyle = VisualConcept.TransactionAttribute.LINE_STYLE.get(rg);
        transactionWidth = VisualConcept.TransactionAttribute.WIDTH.get(rg);
    }

    private void recalculateVertexColorAttribute(final ReadableGraph readGraph) {
        int referredAttr = Graph.NOT_FOUND;
        if (graphVertexColorRef != Graph.NOT_FOUND) {
            final String colorAttrName = readGraph.getStringValue(graphVertexColorRef, 0);
            if (colorAttrName != null) {
                referredAttr = readGraph.getAttribute(GraphElementType.VERTEX, colorAttrName);
            }
        }
        vertexColor = referredAttr != Graph.NOT_FOUND && readGraph.getAttributeType(referredAttr).equals(ColorAttributeDescription.ATTRIBUTE_NAME) ? referredAttr : VisualConcept.VertexAttribute.COLOR.get(readGraph);
    }

    private void recalculateTransactionColorAttribute(final ReadableGraph readGraph) {
        int referredAttr = Graph.NOT_FOUND;
        if (graphTransactionColorRef != Graph.NOT_FOUND) {
            final String colorAttrName = readGraph.getStringValue(graphTransactionColorRef, 0);
            if (colorAttrName != null) {
                referredAttr = readGraph.getAttribute(GraphElementType.TRANSACTION, colorAttrName);
            }
        }
        transactionColor = referredAttr != Graph.NOT_FOUND && readGraph.getAttributeType(referredAttr).equals(ColorAttributeDescription.ATTRIBUTE_NAME) ? referredAttr : VisualConcept.TransactionAttribute.COLOR.get(readGraph);
    }

    private void recalculateConnectionMode(final ReadableGraph readGraph) {
        connectionMode = graphConnectionMode != Graph.NOT_FOUND ? readGraph.getObjectValue(graphConnectionMode, 0) : VisualGraphDefaults.DEFAULT_CONNECTION_MODE;
    }

    private void recalculateTopLabels(final ReadableGraph readGraph) {
        final GraphLabels topLabels = graphTopLabels != Graph.NOT_FOUND ? readGraph.getObjectValue(graphTopLabels, 0) : VisualGraphDefaults.DEFAULT_TOP_LABELS;
        final int numLabels = topLabels.getNumberOfLabels();
        topLabelAttrs = new int[numLabels];
        topLabelSizes = new float[numLabels];
        topLabelColors = new ConstellationColor[numLabels];
        int i = 0;
        for (final GraphLabel label : topLabels.getLabels()) {
            topLabelAttrs[i] = readGraph.getAttribute(GraphElementType.VERTEX, label.getAttributeName());
            topLabelSizes[i] = label.getSize();
            topLabelColors[i] = label.getColor() != null ? label.getColor() : VisualGraphDefaults.DEFAULT_LABEL_COLOR;
            i++;
        }
    }

    private void recalculateBottomLabels(final ReadableGraph readGraph) {
        final GraphLabels bottomLabels = graphBottomLabels != Graph.NOT_FOUND ? readGraph.getObjectValue(graphBottomLabels, 0) : VisualGraphDefaults.DEFAULT_BOTTOM_LABELS;
        final int numLabels = bottomLabels.getNumberOfLabels();
        bottomLabelAttrs = new int[numLabels];
        bottomLabelSizes = new float[numLabels];
        bottomLabelColors = new ConstellationColor[numLabels];
        int i = 0;
        for (final GraphLabel label : bottomLabels.getLabels()) {
            bottomLabelAttrs[i] = readGraph.getAttribute(GraphElementType.VERTEX, label.getAttributeName());
            bottomLabelSizes[i] = label.getSize();
            bottomLabelColors[i] = label.getColor() != null ? label.getColor() : VisualGraphDefaults.DEFAULT_LABEL_COLOR;
            i++;
        }
    }

    private void recalculateConnectionLabels(final ReadableGraph readGraph) {
        final GraphLabels connectionLabels = graphConnectionLabels != Graph.NOT_FOUND ? readGraph.getObjectValue(graphConnectionLabels, 0) : VisualGraphDefaults.DEFAULT_CONNECTION_LABELS;
        final int numLabels = connectionLabels.getNumberOfLabels();
        connectionLabelAttrs = new int[numLabels];
        connectionLabelSizes = new float[numLabels];
        connectionLabelColors = new ConstellationColor[numLabels];
        int i = 0;
        for (final GraphLabel label : connectionLabels.getLabels()) {
            connectionLabelAttrs[i] = readGraph.getAttribute(GraphElementType.TRANSACTION, label.getAttributeName());
            connectionLabelSizes[i] = label.getSize();
            connectionLabelColors[i] = label.getColor() != null ? label.getColor() : VisualGraphDefaults.DEFAULT_LABEL_COLOR;
            i++;
        }
    }

    private void recalculateDecorators(final ReadableGraph readGraph) {
        final VertexDecorators decorators = graphDecorators != Graph.NOT_FOUND ? readGraph.getObjectValue(graphDecorators, 0) : VisualGraphDefaults.DEFAULT_DECORATORS;
        nwDecorator = readGraph.getAttribute(GraphElementType.VERTEX, decorators.getNorthWestDecoratorAttribute());
        neDecorator = readGraph.getAttribute(GraphElementType.VERTEX, decorators.getNorthEastDecoratorAttribute());
        seDecorator = readGraph.getAttribute(GraphElementType.VERTEX, decorators.getSouthEastDecoratorAttribute());
        swDecorator = readGraph.getAttribute(GraphElementType.VERTEX, decorators.getSouthWestDecoratorAttribute());
    }

    private void recalculateStructure(final ReadableGraph readGraph) {
        rebuildConnections(readGraph);
    }

    @SuppressWarnings("fallthrough")
    private void rebuildConnections(final ReadableGraph readGraph) {
        final int linkCount = readGraph.getLinkCount();
        final int maxTransactions = graphMaxTransactions != Graph.NOT_FOUND ? readGraph.getIntValue(graphMaxTransactions, 0) : VisualGraphDefaults.DEFAULT_MAX_TRANSACTION_TO_DRAW;

        final int connectionUpperBound;
        if (connectionMode == ConnectionMode.LINK) {
            connectionUpperBound = linkCount;
        } else {
            connectionUpperBound = connectionMode == ConnectionMode.EDGE ? readGraph.getEdgeCount() : readGraph.getTransactionCount();
        }
        connectionElementTypes = new GraphElementType[connectionUpperBound];
        connectionElementIds = new int[connectionUpperBound];
        linkStartingPositions = new int[linkCount];
        int currentPos = 0;
        for (int i = 0; i < linkCount; i++) {
            final int linkId = readGraph.getLink(i);
            linkStartingPositions[i] = currentPos;
            switch (connectionMode) {
                case TRANSACTION:
                    if (readGraph.getLinkTransactionCount(linkId) <= maxTransactions) {
                        for (int j = 0; j < readGraph.getLinkTransactionCount(linkId); j++) {
                            connectionElementTypes[currentPos] = GraphElementType.TRANSACTION;
                            connectionElementIds[currentPos] = readGraph.getLinkTransaction(linkId, j);
                            currentPos++;
                        }
                        break;
                    }
                // fall through
                case EDGE:
                    for (int j = 0; j < readGraph.getLinkEdgeCount(linkId); j++) {
                        connectionElementTypes[currentPos] = GraphElementType.EDGE;
                        connectionElementIds[currentPos] = readGraph.getLinkEdge(linkId, j);
                        currentPos++;
                    }
                    break;
                case LINK:
                    connectionElementTypes[currentPos] = GraphElementType.LINK;
                    connectionElementIds[currentPos] = linkId;
                    currentPos++;
                    break;
                default:
                    break;
            }
        }
        connectionElementTypes = Arrays.copyOf(connectionElementTypes, currentPos);
        connectionElementIds = Arrays.copyOf(connectionElementIds, currentPos);
    }

    @Override
    public ConstellationColor getBackgroundColor() {
        return graphBackgroundColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphBackgroundColor, 0) : VisualGraphDefaults.DEFAULT_BACKGROUND_COLOR;
    }

    @Override
    public ConstellationColor getHighlightColor() {
        return graphHighlightColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphHighlightColor, 0) : VisualGraphDefaults.DEFAULT_HIGHLIGHT_COLOR;
    }

    @Override
    public DrawFlags getDrawFlags() {
        final boolean visibilityThresholdExceeded = graphVisibleAboveThreshold != Graph.NOT_FOUND && !accessGraph.getBooleanValue(graphVisibleAboveThreshold, 0)
                && graphVisibilityThreshold != Graph.NOT_FOUND && accessGraph.getVertexCount() > accessGraph.getIntValue(graphVisibilityThreshold, 0);
        if (graphDrawFlags != Graph.NOT_FOUND) {
            return visibilityThresholdExceeded ? DrawFlags.NONE : accessGraph.getObjectValue(graphDrawFlags, 0);
        }
        return VisualGraphDefaults.DEFAULT_DRAW_FLAGS;
    }

    @Override
    public Camera getCamera() {
        final Camera camera = graphCamera != Graph.NOT_FOUND
                ? accessGraph.getObjectValue(graphCamera, 0) : VisualGraphDefaults.DEFAULT_CAMERA;
        return camera != null ? camera : VisualGraphDefaults.DEFAULT_CAMERA;
    }

    @Override
    public int getVertexCount() {
        return accessGraph.getVertexCount();
    }

    @Override
    public int getConnectionCount() {
        return connectionElementTypes.length;
    }

    @Override
    public float getBlazeSize() {
        return graphBlazeSize != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphBlazeSize, 0) : VisualGraphDefaults.DEFAULT_BLAZE_SIZE;
    }

    @Override
    public float getBlazeOpacity() {
        return graphBlazeOpacity != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphBlazeOpacity, 0) : VisualGraphDefaults.DEFAULT_BLAZE_OPACITY;
    }

    @Override
    public float getConnectionOpacity() {
        return graphConnectionOpacity != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphConnectionOpacity, 0) : VisualGraphDefaults.DEFAULT_CONNECTION_OPACITY;
    }

    @Override
    public float getConnectionMotion() {
        return graphConnectionMotion != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphConnectionMotion, 0) : VisualGraphDefaults.DEFAULT_CONNECTION_MOTION;
    }
    @Override
    public int getTopLabelCount() {
        return topLabelAttrs.length;
    }

    @Override
    public int getBottomLabelCount() {
        return bottomLabelAttrs.length;
    }

    @Override
    public int getConnectionAttributeLabelCount() {
        return connectionLabelAttrs.length;
    }

    @Override
    public int getConnectionLabelCount(final int connection) {
        return connectionElementTypes[connection] == GraphElementType.TRANSACTION ? connectionLabelAttrs.length : 1;
    }

    @Override
    public boolean isLabelSummary(final int connection) {
        return connectionElementTypes[connection] != GraphElementType.TRANSACTION;
    }

    @Override
    public ConstellationColor getTopLabelColor(final int labelNum) {
        return topLabelColors[labelNum];
    }

    @Override
    public ConstellationColor getBottomLabelColor(final int labelNum) {
        return bottomLabelColors[labelNum];
    }

    @Override
    public ConstellationColor getConnectionLabelColor(final int labelNum) {
        return connectionLabelColors[labelNum];
    }

    @Override
    public float getTopLabelSize(final int labelNum) {
        return topLabelSizes[labelNum];
    }

    @Override
    public float getBottomLabelSize(final int labelNum) {
        return bottomLabelSizes[labelNum];
    }

    @Override
    public float getConnectionLabelSize(int labelNum) {
        return connectionLabelSizes[labelNum];
    }

    @Override
    public int getConnectionId(final int connection) {
        switch (connectionElementTypes[connection]) {
            case LINK:
                return accessGraph.getLinkTransaction(connectionElementIds[connection], 0);
            case EDGE:
                return accessGraph.getEdgeTransaction(connectionElementIds[connection], 0);
            case TRANSACTION:
            default:
                return connectionElementIds[connection];
        }
    }

    @Override
    public ConnectionDirection getConnectionDirection(final int connection) {
        switch (connectionElementTypes[connection]) {
            case LINK:
                final int uphillCount = accessGraph.getLinkTransactionCount(connectionElementIds[connection], Graph.UPHILL);
                final int downhillCount = accessGraph.getLinkTransactionCount(connectionElementIds[connection], Graph.DOWNHILL);
                if (uphillCount > 0 && downhillCount > 0) {
                    return ConnectionDirection.BIDIRECTED;
                } else if (uphillCount > 0) {
                    return ConnectionDirection.LOW_TO_HIGH;
                } else if (downhillCount > 0) {
                    return ConnectionDirection.HIGH_TO_LOW;
                } else {
                    return ConnectionDirection.UNDIRECTED;
                }
            case EDGE:
                return getEdgeConnectionDirection(connection);
            case TRANSACTION:
            default:
                return getTransactionConnectionDirection(connection);
        }
    }

    private ConnectionDirection getTransactionConnectionDirection(final int connection) {        
        final int transactionDirection = accessGraph.getTransactionDirection(connectionElementIds[connection]);
        switch (transactionDirection) {
            case Graph.UPHILL:
                return ConnectionDirection.LOW_TO_HIGH;
            case Graph.DOWNHILL:
                return ConnectionDirection.HIGH_TO_LOW;
            case Graph.UNDIRECTED:
            default:
                return ConnectionDirection.UNDIRECTED;
        }
    }

    private ConnectionDirection getEdgeConnectionDirection(final int connection) {        
        final int edgeDirection = accessGraph.getEdgeDirection(connectionElementIds[connection]);
        switch (edgeDirection) {
            case Graph.UPHILL:
                return ConnectionDirection.LOW_TO_HIGH;
            case Graph.DOWNHILL:
                return ConnectionDirection.HIGH_TO_LOW;
            case Graph.UNDIRECTED:
            default:
                return ConnectionDirection.UNDIRECTED;
        }
    }

    @Override
    public boolean isConnectionDirected(final int connection) {
        if (transactionDirected != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    boolean linkDirected = false;
                    final int linkId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                        final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                        linkDirected |= accessGraph.getBooleanValue(transactionDirected, transactionId);
                    }
                    return linkDirected;
                case EDGE:
                    boolean edgeDirected = false;
                    final int edgeId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                        final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                        edgeDirected |= accessGraph.getBooleanValue(transactionDirected, transactionId);
                    }
                    return edgeDirected;
                case TRANSACTION:
                default:
                    return accessGraph.getBooleanValue(transactionDirected, connectionElementIds[connection]);
            }
        }
        return false;
    }

    @Override
    public int getVertexId(final int vertex) {
        return accessGraph.getVertex(vertex);
    }

    @Override
    public float getX(final int vertex) {
        return vertexX != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexX, accessGraph.getVertex(vertex)) : VisualGraphDefaults.getDefaultX(accessGraph.getVertex(vertex));
    }

    @Override
    public float getY(final int vertex) {
        return vertexY != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexY, accessGraph.getVertex(vertex)) : VisualGraphDefaults.getDefaultY(accessGraph.getVertex(vertex));
    }

    @Override
    public float getZ(final int vertex) {
        return vertexZ != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexZ, accessGraph.getVertex(vertex)) : VisualGraphDefaults.getDefaultZ(accessGraph.getVertex(vertex));
    }

    @Override
    public float getX2(final int vertex) {
        return vertexX2 != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexX2, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_X2;
    }

    @Override
    public float getY2(final int vertex) {
        return vertexY2 != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexY2, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_Y2;
    }

    @Override
    public float getZ2(final int vertex) {
        return vertexZ2 != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexZ2, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_Z2;
    }

    @Override
    public ConstellationColor getVertexColor(final int vertex) {
        ConstellationColor color = null;
        if (vertexColor != Graph.NOT_FOUND) {
            color = accessGraph.getObjectValue(vertexColor, accessGraph.getVertex(vertex));
        }
        return color != null ? color : VisualGraphDefaults.DEFAULT_VERTEX_COLOR;
    }

    @Override
    public String getBackgroundIcon(final int vertex) {
        return vertexBackgroundIcon != Graph.NOT_FOUND ? accessGraph.getStringValue(vertexBackgroundIcon, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_BACKGROUND_ICON;
    }

    @Override
    public String getForegroundIcon(final int vertex) {
        return vertexForegroundIcon != Graph.NOT_FOUND ? accessGraph.getStringValue(vertexForegroundIcon, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_FOREGROUND_ICON;
    }

    @Override
    public boolean isVertexSelected(final int vertex) {
        return vertexSelected != Graph.NOT_FOUND ? accessGraph.getBooleanValue(vertexSelected, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_SELECTED;
    }

    @Override
    public float getVertexVisibility(final int vertex) {
        final float layerVisibility = vertexLayerVisibility != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexLayerVisibility, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_FILTER_VISIBILITY;
        return layerVisibility * (vertexVisibility != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexVisibility, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_VISIBILITY);
    }

    @Override
    public boolean isVertexDimmed(final int vertex) {
        return vertexDimmed != Graph.NOT_FOUND ? accessGraph.getBooleanValue(vertexDimmed, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_DIMMED;
    }

    @Override
    public float getRadius(final int vertex) {
        return vertexRadius != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexRadius, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_RADIUS;
    }

    @Override
    public boolean isBlazed(final int vertex) {
        final Blaze blaze = vertexBlaze != Graph.NOT_FOUND ? accessGraph.getObjectValue(vertexBlaze, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_BLAZE;
        return blaze != null;
    }

    @Override
    public int getBlazeAngle(final int vertex) {
        final Blaze blaze = vertexBlaze != Graph.NOT_FOUND ? accessGraph.getObjectValue(vertexBlaze, accessGraph.getVertex(vertex)) : VisualGraphDefaults.DEFAULT_VERTEX_BLAZE;
        return blaze == null ? VisualGraphDefaults.DEFAULT_BLAZE_ANGLE : blaze.getAngle();
    }

    @Override
    public ConstellationColor getBlazeColor(final int vertex) {
        final Blaze blaze = accessGraph.getObjectValue(vertexBlaze, accessGraph.getVertex(vertex));
        return blaze == null ? VisualGraphDefaults.DEFAULT_BLAZE_COLOR : blaze.getColor();
    }

    private String getDecoratorStr(final int decoratorAttrib, final int decoratorVertex) {
        // If a valid attribute ID has been supplied, return its value as a string. This will be mapped to an icon
        // for display as a decorator. Boolean attributes are special cases as it is possible to overload the icon
        // used for true and false values based on attribute name.
        // This is acheived in the following way:
        //   * The value string returned for boolean attributes is appended with the text .<attribute_name> to result
        //     in a value of the form <value>.<attribute_name>
        //   * In a class overriding ConstellationIcon ensure an icon is added with a name, or an alias matching
        //     the string <value>.<attribute_name>
        // The icon DefaultIconProvider.TRANSPARENT is able to be aliased to support NOT showing an icon. Ie to NOT
        // show an icon when a pinned attribute is set to false, add an alias to DefaultIconProvider.TRANSPARENT
        // with name "false.pinned"
        if (decoratorAttrib != Graph.NOT_FOUND) {
            final String value = accessGraph.getStringValue(decoratorAttrib, accessGraph.getVertex(decoratorVertex));
            // If the attribute is a boolean, construct a value string including the attribute name as well,
            // as per comments above.
            if (accessGraph.getAttributeType(decoratorAttrib).equals(BooleanAttributeDescription.ATTRIBUTE_NAME)) {
                // Booleans will either have a value of true_<attributeName> or false_<attributeName>
                // there are three cases to cater for:
                // 1. Both true_<attributeName> and false_<attributeName> are set as aliases for icons
                //    --> In this case we use the supplied icon
                // 2. Only one of true_<attributeName> or false_<attributeName> is set as an alias for an icon
                //    --> In this case the value tht doesnt have an icon set should display nothing
                // 3. Neither true_<attributeName> or false_<attributeName> is set as an alias
                //    --> In this case there is no override, use the default true/false icons
                final boolean valueAsBool = accessGraph.getBooleanValue(decoratorAttrib, accessGraph.getVertex(decoratorVertex));
                final String notValue = Boolean.toString(!valueAsBool);
                final String attributeName = accessGraph.getAttributeName(decoratorAttrib);
                final String valueStr = value.concat("_").concat(attributeName);
                final String notValueStr = notValue.concat("_").concat(attributeName);

                // If an icon or alias doesn't exist for either the true or false value, then there are no
                // overrides, use defaults
                if (!IconManager.iconExists(valueStr) && !IconManager.iconExists(notValueStr)) {
                    return value;
                }
                return valueStr;
            }
            return value;
        }
        return null;
    }

    @Override
    public String getNWDecorator(final int vertex) {
        return getDecoratorStr(nwDecorator, vertex);
    }

    @Override
    public String getNEDecorator(final int vertex) {
        return getDecoratorStr(neDecorator, vertex);
    }

    @Override
    public String getSEDecorator(final int vertex) {
        return getDecoratorStr(seDecorator, vertex);
    }

    @Override
    public String getSWDecorator(final int vertex) {
        return getDecoratorStr(swDecorator, vertex);
    }

    @Override
    public ConstellationColor getConnectionColor(final int connection) {
        ConstellationColor color = null;
        final ConstellationColor mixColor;
        if (transactionColor != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    final int linkId = connectionElementIds[connection];
                    mixColor = graphMixColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphMixColor, 0) : VisualGraphDefaults.DEFAULT_MIX_COLOR;
                    for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                        final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                        final ConstellationColor transColor = accessGraph.getObjectValue(transactionColor, transactionId);
                        if (color != null && !color.equals(transColor)) {
                            color = mixColor;
                        } else {
                            color = transColor;
                        }
                    }
                    break;
                case EDGE:
                    final int edgeId = connectionElementIds[connection];
                    mixColor = graphMixColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphMixColor, 0) : VisualGraphDefaults.DEFAULT_MIX_COLOR;
                    for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                        final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                        final ConstellationColor transColor = accessGraph.getObjectValue(transactionColor, transactionId);
                        if (color != null && !color.equals(transColor)) {
                            color = mixColor;
                        } else {
                            color = transColor;
                        }
                    }
                    break;
                case TRANSACTION:
                default:
                    color = accessGraph.getObjectValue(transactionColor, connectionElementIds[connection]);
                    break;
            }
        }
        return color != null ? color : VisualGraphDefaults.DEFAULT_TRANSACTION_COLOR;
    }

    @Override
    public boolean isConnectionSelected(final int connection) {
        if (transactionSelected != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    boolean linkSelected = false;
                    final int linkId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                        final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                        linkSelected |= accessGraph.getBooleanValue(transactionSelected, transactionId);
                    }
                    return linkSelected;
                case EDGE:
                    boolean edgeSelected = false;
                    final int edgeId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                        final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                        edgeSelected |= accessGraph.getBooleanValue(transactionSelected, transactionId);
                    }
                    return edgeSelected;
                case TRANSACTION:
                default:
                    return accessGraph.getBooleanValue(transactionSelected, connectionElementIds[connection]);
            }
        }
        return VisualGraphDefaults.DEFAULT_TRANSACTION_SELECTED;
    }

    @Override
    public float getConnectionVisibility(final int connection) {
        if (transactionVisibility != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    float linkVisibility = -1;
                    if (transactionLayerVisibility != Graph.NOT_FOUND) {
                        final int linkId = connectionElementIds[connection];
                        for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                            final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                            linkVisibility = Math.max(linkVisibility, accessGraph.getFloatValue(transactionLayerVisibility, transactionId)); // handle case of no layer vis - ie no layer
                        }
                        return linkVisibility;
                    } else {
                        return VisualGraphDefaults.DEFAULT_TRANSACTION_VISIBILITY;
                    }

                case EDGE:
                    float edgeVisibility = -1;
                    if (transactionLayerVisibility != Graph.NOT_FOUND) {
                        final int edgeId = connectionElementIds[connection];
                        for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                            final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                            edgeVisibility = Math.max(edgeVisibility, accessGraph.getFloatValue(transactionLayerVisibility, transactionId));
                        }
                        return edgeVisibility;
                    } else {
                        return VisualGraphDefaults.DEFAULT_TRANSACTION_VISIBILITY;
                    }

                case TRANSACTION:
                default:
                    float transLayerVisibility = transactionLayerVisibility != Graph.NOT_FOUND ? accessGraph.getFloatValue(transactionLayerVisibility, connectionElementIds[connection]) : VisualGraphDefaults.DEFAULT_TRANSACTION_FILTER_VISIBILITY;
                    return transLayerVisibility * accessGraph.getFloatValue(transactionVisibility, connectionElementIds[connection]);
            }
        }
        float defaultLayerVisibility = transactionLayerVisibility != Graph.NOT_FOUND ? accessGraph.getFloatValue(transactionLayerVisibility, accessGraph.getTransaction(connection)) : VisualGraphDefaults.DEFAULT_TRANSACTION_FILTER_VISIBILITY;

        return defaultLayerVisibility * VisualGraphDefaults.DEFAULT_TRANSACTION_VISIBILITY;
    }

    @Override
    public boolean isConnectionDimmed(final int connection) {
        if (transactionDimmed != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    boolean linkDimmed = true;
                    final int linkId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                        final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                        linkDimmed &= accessGraph.getBooleanValue(transactionDimmed, transactionId);
                    }
                    return linkDimmed;
                case EDGE:
                    boolean edgeDimmed = true;
                    final int edgeId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                        final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                        edgeDimmed &= accessGraph.getBooleanValue(transactionDimmed, transactionId);
                    }
                    return edgeDimmed;
                case TRANSACTION:
                default:
                    return accessGraph.getBooleanValue(transactionDimmed, connectionElementIds[connection]);
            }
        }
        return VisualGraphDefaults.DEFAULT_TRANSACTION_DIMMED;
    }

    @Override
    public LineStyle getConnectionLineStyle(final int connection) {
        LineStyle style = null;
        final LineStyle mixStyle = LineStyle.SOLID;
        if (transactionLineStyle != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    final int linkId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                        final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                        final LineStyle transStyle = accessGraph.getObjectValue(transactionLineStyle, transactionId);
                        if (style != null && !style.equals(transStyle)) {
                            style = mixStyle;
                        } else {
                            style = transStyle;
                        }
                    }
                    break;
                case EDGE:
                    final int edgeId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                        final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                        final LineStyle transStyle = accessGraph.getObjectValue(transactionLineStyle, transactionId);
                        if (style != null && !style.equals(transStyle)) {
                            style = mixStyle;
                        } else {
                            style = transStyle;
                        }
                    }
                    break;
                case TRANSACTION:
                default:
                    style = accessGraph.getObjectValue(transactionLineStyle, connectionElementIds[connection]);
                    break;
            }
        }
        return style != null ? style : VisualGraphDefaults.DEFAULT_TRANSACTION_LINE_STYLE;
    }

    @Override
    public float getConnectionWidth(final int connection) {
        if (transactionWidth != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    float linkWidth = 0;
                    final int linkId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                        final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                        linkWidth = Math.max(linkWidth, accessGraph.getFloatValue(transactionWidth, transactionId));
                    }
                    return linkWidth;
                case EDGE:
                    float edgeWidth = 0;
                    final int edgeId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                        final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                        edgeWidth = Math.max(edgeWidth, accessGraph.getFloatValue(transactionWidth, transactionId));
                    }
                    return edgeWidth;
                case TRANSACTION:
                default:
                    return accessGraph.getFloatValue(transactionWidth, connectionElementIds[connection]);
            }
        }
        return VisualGraphDefaults.DEFAULT_TRANSACTION_WIDTH;
    }

    @Override
    public int getConnectionLowVertex(final int connection) {
        final int linkId;
        switch (connectionElementTypes[connection]) {
            case LINK:
                linkId = connectionElementIds[connection];
                break;
            case EDGE:
                linkId = accessGraph.getEdgeLink(connectionElementIds[connection]);
                break;
            case TRANSACTION:
            default:
                linkId = accessGraph.getTransactionLink(connectionElementIds[connection]);
                break;
        }
        return accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(linkId));
    }

    @Override
    public int getConnectionHighVertex(final int connection) {
        final int linkId;
        switch (connectionElementTypes[connection]) {
            case LINK:
                linkId = connectionElementIds[connection];
                break;
            case EDGE:
                linkId = accessGraph.getEdgeLink(connectionElementIds[connection]);
                break;
            case TRANSACTION:
            default:
                linkId = accessGraph.getTransactionLink(connectionElementIds[connection]);
                break;
        }
        return accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(linkId));
    }

    @Override
    public int getLinkCount() {
        return accessGraph.getLinkCount();
    }

    @Override
    public int getLinkLowVertex(final int link) {
        return accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(accessGraph.getLink(link)));
    }

    @Override
    public int getLinkHighVertex(final int link) {
        return accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(accessGraph.getLink(link)));
    }

    @Override
    public int getLinkSource(final int link) {
        final int linkId = accessGraph.getLink(link);
        return accessGraph.getLinkTransactionCount(linkId, Graph.DOWNHILL) > 0 && accessGraph.getLinkTransactionCount(linkId, Graph.UPHILL) == 0 ? accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(linkId)) : accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(linkId));
    }

    @Override
    public int getLinkDestination(final int link) {
        final int linkId = accessGraph.getLink(link);
        return accessGraph.getLinkTransactionCount(linkId, Graph.DOWNHILL) > 0 && accessGraph.getLinkTransactionCount(linkId, Graph.UPHILL) == 0 ? accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(linkId)) : accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(linkId));
    }

    @Override
    public int getLinkConnectionCount(final int link) {
        switch (connectionElementTypes[linkStartingPositions[link]]) {
            case LINK:
                return 1;
            case EDGE:
                return accessGraph.getLinkEdgeCount(accessGraph.getLink(link));
            case TRANSACTION:
            default:
                return accessGraph.getLinkTransactionCount(accessGraph.getLink(link));
        }
    }

    @Override
    public int getLinkConnection(final int link, final int pos) {
        return linkStartingPositions[link] + pos;
    }

    @Override
    public String getVertexTopLabelText(final int vertex, final int labelNum) {
        return topLabelAttrs[labelNum] != Graph.NOT_FOUND ? accessGraph.getStringValue(topLabelAttrs[labelNum], accessGraph.getVertex(vertex)) : "";
    }

    @Override
    public String getVertexBottomLabelText(final int vertex, final int labelNum) {
        return bottomLabelAttrs[labelNum] != Graph.NOT_FOUND ? accessGraph.getStringValue(bottomLabelAttrs[labelNum], accessGraph.getVertex(vertex)) : "";
    }

    @Override
    public String getConnectionLabelText(final int connection, final int labelNum) {
        switch (connectionElementTypes[connection]) {
            case LINK:
                return String.valueOf(accessGraph.getLinkTransactionCount(connectionElementIds[connection]));
            case EDGE:
                return String.valueOf(accessGraph.getEdgeTransactionCount(connectionElementIds[connection]));
            case TRANSACTION:
            default:
                if (connectionLabelAttrs[labelNum] != Graph.NOT_FOUND) {
                    return accessGraph.getStringValue(connectionLabelAttrs[labelNum], connectionElementIds[connection]);
                }
                return "";
        }
    }

    public void updateModCounts(final ReadableGraph readGraph) {
        recalculateVisualAttributes(readGraph);
        globalModCount = readGraph.getGlobalModificationCounter();
        structureModCount = readGraph.getStructureModificationCounter();
        attributeModCount = readGraph.getAttributeModificationCounter();
        long count;

        count = graphConnectionMode == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphConnectionMode);
        modCounts.put(VisualConcept.GraphAttribute.CONNECTION_MODE, count);
        recalculateConnectionMode(readGraph);

        count = graphMaxTransactions == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphMaxTransactions);
        modCounts.put(VisualConcept.GraphAttribute.MAX_TRANSACTIONS, count);

        count = graphDecorators == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphDecorators);
        recalculateDecorators(readGraph);
        modCounts.put(VisualConcept.GraphAttribute.DECORATORS, count);

        count = nwDecorator == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(nwDecorator);
        nwDecoratorModCount = count;

        count = neDecorator == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(neDecorator);
        neDecoratorModCount = count;

        count = seDecorator == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(seDecorator);
        seDecoratorModCount = count;

        count = swDecorator == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(swDecorator);
        swDecoratorModCount = count;

        count = graphTopLabels == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphTopLabels);
        recalculateTopLabels(readGraph);
        modCounts.put(VisualConcept.GraphAttribute.TOP_LABELS, count);

        topLabelModCounts = Arrays.copyOf(topLabelModCounts, topLabelAttrs.length);
        for (int i = 0; i < topLabelAttrs.length; i++) {
            final int attr = topLabelAttrs[i];
            count = attr == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(attr);
            topLabelModCounts[i] = count;
        }

        count = graphBottomLabels == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphBottomLabels);
        recalculateBottomLabels(readGraph);
        modCounts.put(VisualConcept.GraphAttribute.BOTTOM_LABELS, count);

        bottomLabelModCounts = Arrays.copyOf(bottomLabelModCounts, bottomLabelAttrs.length);
        for (int i = 0; i < bottomLabelAttrs.length; i++) {
            final int attr = bottomLabelAttrs[i];
            count = attr == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(attr);
            bottomLabelModCounts[i] = count;
        }

        count = graphConnectionLabels == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphConnectionLabels);
        recalculateConnectionLabels(readGraph);
        modCounts.put(VisualConcept.GraphAttribute.TRANSACTION_LABELS, count);

        connectionLabelModCounts = Arrays.copyOf(connectionLabelModCounts, connectionLabelAttrs.length);
        for (int i = 0; i < connectionLabelAttrs.length; i++) {
            final int attr = connectionLabelAttrs[i];
            count = attr == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(attr);
            connectionLabelModCounts[i] = count;
        }

        count = graphVertexColorRef == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphVertexColorRef);
        recalculateVertexColorAttribute(readGraph);
        modCounts.put(VisualConcept.GraphAttribute.NODE_COLOR_REFERENCE, count);
        count = vertexColor == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexColor);
        modCounts.put(VisualConcept.VertexAttribute.COLOR, count);

        count = graphTransactionColorRef == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphTransactionColorRef);
        recalculateTransactionColorAttribute(readGraph);
        modCounts.put(VisualConcept.GraphAttribute.TRANSACTION_COLOR_REFERENCE, count);
        count = transactionColor == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionColor);
        modCounts.put(VisualConcept.TransactionAttribute.COLOR, count);

        recalculateStructure(readGraph);

        count = graphBackgroundColor == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphBackgroundColor);
        modCounts.put(VisualConcept.GraphAttribute.BACKGROUND_COLOR, count);

        count = graphHighlightColor == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphHighlightColor);
        modCounts.put(VisualConcept.GraphAttribute.HIGHLIGHT_COLOR, count);

        count = graphBlazeOpacity == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphBlazeOpacity);
        modCounts.put(VisualConcept.GraphAttribute.BLAZE_OPACITY, count);

        count = graphBlazeSize == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphBlazeSize);
        modCounts.put(VisualConcept.GraphAttribute.BLAZE_SIZE, count);

        count = graphConnectionOpacity == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphConnectionOpacity);
        modCounts.put(VisualConcept.GraphAttribute.CONNECTION_OPACITY, count);
        
        count = graphConnectionMotion == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphConnectionMotion);
        modCounts.put(VisualConcept.GraphAttribute.CONNECTION_MOTION, count);

        count = graphDrawFlags == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphDrawFlags);
        modCounts.put(VisualConcept.GraphAttribute.DRAW_FLAGS, count);

        count = graphCamera == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphCamera);
        modCounts.put(VisualConcept.GraphAttribute.CAMERA, count);

        count = graphMixColor == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphMixColor);
        modCounts.put(VisualConcept.GraphAttribute.MIX_COLOR, count);

        count = graphVisibleAboveThreshold == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphVisibleAboveThreshold);
        modCounts.put(VisualConcept.GraphAttribute.VISIBLE_ABOVE_THRESHOLD, count);

        count = graphVisibilityThreshold == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(graphVisibilityThreshold);
        modCounts.put(VisualConcept.GraphAttribute.VISIBILITY_THRESHOLD, count);

        count = vertexX == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexX);
        modCounts.put(VisualConcept.VertexAttribute.X, count);

        count = vertexY == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexY);
        modCounts.put(VisualConcept.VertexAttribute.Y, count);

        count = vertexZ == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexZ);
        modCounts.put(VisualConcept.VertexAttribute.Z, count);

        count = vertexX2 == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexX2);
        modCounts.put(VisualConcept.VertexAttribute.X2, count);

        count = vertexY2 == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexY2);
        modCounts.put(VisualConcept.VertexAttribute.Y2, count);

        count = vertexZ2 == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexZ2);
        modCounts.put(VisualConcept.VertexAttribute.Z2, count);

        count = vertexBackgroundIcon == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexBackgroundIcon);
        modCounts.put(VisualConcept.VertexAttribute.BACKGROUND_ICON, count);

        count = vertexForegroundIcon == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexForegroundIcon);
        modCounts.put(VisualConcept.VertexAttribute.FOREGROUND_ICON, count);

        count = vertexSelected == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexSelected);
        modCounts.put(VisualConcept.VertexAttribute.SELECTED, count);

        count = vertexVisibility == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexVisibility);
        modCounts.put(VisualConcept.VertexAttribute.VISIBILITY, count);

        count = vertexLayerVisibility == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexLayerVisibility);
        modCounts.put(LayersConcept.VertexAttribute.LAYER_VISIBILITY, count);

        count = vertexDimmed == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexDimmed);
        modCounts.put(VisualConcept.VertexAttribute.DIMMED, count);

        count = vertexRadius == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexRadius);
        modCounts.put(VisualConcept.VertexAttribute.NODE_RADIUS, count);

        count = vertexBlaze == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(vertexBlaze);
        modCounts.put(VisualConcept.VertexAttribute.BLAZE, count);

        count = transactionSelected == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionSelected);
        modCounts.put(VisualConcept.TransactionAttribute.SELECTED, count);

        count = transactionDirected == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionDirected);
        modCounts.put(VisualConcept.TransactionAttribute.DIRECTED, count);

        count = transactionVisibility == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionVisibility);
        modCounts.put(VisualConcept.TransactionAttribute.VISIBILITY, count);

        count = transactionLayerVisibility == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionLayerVisibility);
        modCounts.put(LayersConcept.TransactionAttribute.LAYER_VISIBILITY, count);

        count = transactionDimmed == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionDimmed);
        modCounts.put(VisualConcept.TransactionAttribute.DIMMED, count);

        count = transactionLineStyle == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionLineStyle);
        modCounts.put(VisualConcept.TransactionAttribute.LINE_STYLE, count);

        count = transactionWidth == Graph.NOT_FOUND ? -1 : readGraph.getValueModificationCounter(transactionWidth);
        modCounts.put(VisualConcept.TransactionAttribute.WIDTH, count);

    }
}
