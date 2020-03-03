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
package au.gov.asd.tac.constellation.graph.visual.display;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.utilities.ConnectionMode;
import au.gov.asd.tac.constellation.graph.visual.color.ColorAttributeDescription;
import au.gov.asd.tac.constellation.graph.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.visual.blaze.Blaze;
import au.gov.asd.tac.constellation.visual.camera.Camera;
import au.gov.asd.tac.constellation.visual.color.ConstellationColor;
import au.gov.asd.tac.constellation.visual.decorators.Decorators;
import au.gov.asd.tac.constellation.visual.display.VisualAccess;
import au.gov.asd.tac.constellation.visual.display.VisualChange;
import au.gov.asd.tac.constellation.visual.display.VisualChangeBuilder;
import au.gov.asd.tac.constellation.visual.display.VisualDefaults;
import au.gov.asd.tac.constellation.visual.display.VisualProperty;
import au.gov.asd.tac.constellation.visual.drawflags.DrawFlags;
import au.gov.asd.tac.constellation.visual.labels.GraphLabel;
import au.gov.asd.tac.constellation.visual.labels.GraphLabels;
import au.gov.asd.tac.constellation.visual.linestyle.LineStyle;
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

    public int[] visualAttributes = new int[VisualProperty.values().length];

    private int graphBackgroundColor = Graph.NOT_FOUND;
    private int graphHighlightColor = Graph.NOT_FOUND;
    private int graphBlazeOpacity = Graph.NOT_FOUND;
    private int graphBlazeSize = Graph.NOT_FOUND;
    private int graphDecorators = Graph.NOT_FOUND;
    private int graphTopLabels = Graph.NOT_FOUND;
    private int graphBottomLabels = Graph.NOT_FOUND;
    private int graphConnectionLabels = Graph.NOT_FOUND;
    private int graphConnectionOpacity = Graph.NOT_FOUND;
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
    private int vertexDimmed = Graph.NOT_FOUND;
    private int vertexRadius = Graph.NOT_FOUND;
    private int vertexBlaze = Graph.NOT_FOUND;
    private int transactionColor = Graph.NOT_FOUND;
    private int transactionSelected = Graph.NOT_FOUND;
    private int transactionDirected = Graph.NOT_FOUND;
    private int transactionVisibility = Graph.NOT_FOUND;
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
    public ReadableGraph accessGraph;
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
        final List<VisualChange> internalChangeList = update(true);
        return internalChangeList;
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
            boolean attributesChanged = false, verticesRebuilding = false, connectionsRebuilding = false;

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
                recalculateConnectionMode();
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
                recalculateDecorators();
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
                recalculateTopLabels();

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
                recalculateBottomLabels();
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
                recalculateConnectionLabels();
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
                recalculateVertexColorAttribute();
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
                recalculateTransactionColorAttribute();
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
                recalculateStructure();
                if (recordChanges) {
                    if (verticesRebuilding) {
                        changes.add(new VisualChangeBuilder(VisualProperty.VERTICES_REBUILD).build());
                    }
                    if (connectionsRebuilding) {
                        changes.add(new VisualChangeBuilder(VisualProperty.CONNECTIONS_REBUILD).build());
                    }
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
                    changes.add(new VisualChangeBuilder(VisualProperty.HIGHLIGHT_COLOUR).forItems(1).build());
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
        vertexDimmed = VisualConcept.VertexAttribute.DIMMED.get(rg);
        vertexRadius = VisualConcept.VertexAttribute.NODE_RADIUS.get(rg);
        vertexBlaze = VisualConcept.VertexAttribute.BLAZE.get(rg);

        transactionSelected = VisualConcept.TransactionAttribute.SELECTED.get(rg);
        transactionDirected = VisualConcept.TransactionAttribute.DIRECTED.get(rg);
        transactionVisibility = VisualConcept.TransactionAttribute.VISIBILITY.get(rg);
        transactionDimmed = VisualConcept.TransactionAttribute.DIMMED.get(rg);
        transactionLineStyle = VisualConcept.TransactionAttribute.LINE_STYLE.get(rg);
        transactionWidth = VisualConcept.TransactionAttribute.WIDTH.get(rg);
    }

    private void recalculateVertexColorAttribute() {
        int referredAttr = Graph.NOT_FOUND;
        if (graphVertexColorRef != Graph.NOT_FOUND) {
            final String colorAttrName = accessGraph.getStringValue(graphVertexColorRef, 0);
            if (colorAttrName != null) {
                referredAttr = accessGraph.getAttribute(GraphElementType.VERTEX, colorAttrName);
            }
        }
        vertexColor = referredAttr != Graph.NOT_FOUND && accessGraph.getAttributeType(referredAttr).equals(ColorAttributeDescription.ATTRIBUTE_NAME) ? referredAttr : VisualConcept.VertexAttribute.COLOR.get(accessGraph);
    }

    private void recalculateTransactionColorAttribute() {
        int referredAttr = Graph.NOT_FOUND;
        if (graphTransactionColorRef != Graph.NOT_FOUND) {
            final String colorAttrName = accessGraph.getStringValue(graphTransactionColorRef, 0);
            if (colorAttrName != null) {
                referredAttr = accessGraph.getAttribute(GraphElementType.TRANSACTION, colorAttrName);
            }
        }
        transactionColor = referredAttr != Graph.NOT_FOUND && accessGraph.getAttributeType(referredAttr).equals(ColorAttributeDescription.ATTRIBUTE_NAME) ? referredAttr : VisualConcept.TransactionAttribute.COLOR.get(accessGraph);
    }

    private void recalculateConnectionMode() {
        connectionMode = graphConnectionMode != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphConnectionMode, 0) : VisualGraphDefaults.DEFAULT_CONNECTION_MODE;
    }

    private void recalculateTopLabels() {
        final GraphLabels topLabels = graphTopLabels != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphTopLabels, 0) : VisualDefaults.DEFAULT_TOP_LABELS;
        final int numLabels = topLabels.getNumberOfLabels();
        topLabelAttrs = new int[numLabels];
        topLabelSizes = new float[numLabels];
        topLabelColors = new ConstellationColor[numLabels];
        int i = 0;
        for (GraphLabel label : topLabels.getLabels()) {
            topLabelAttrs[i] = accessGraph.getAttribute(GraphElementType.VERTEX, label.getAttributeName());
            topLabelSizes[i] = label.getSize();
            topLabelColors[i] = label.getColor() != null ? label.getColor() : VisualDefaults.DEFAULT_LABEL_COLOR;
            i++;
        }
    }

    private void recalculateBottomLabels() {
        final GraphLabels bottomLabels = graphBottomLabels != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphBottomLabels, 0) : VisualDefaults.DEFAULT_BOTTOM_LABELS;
        final int numLabels = bottomLabels.getNumberOfLabels();
        bottomLabelAttrs = new int[numLabels];
        bottomLabelSizes = new float[numLabels];
        bottomLabelColors = new ConstellationColor[numLabels];
        int i = 0;
        for (GraphLabel label : bottomLabels.getLabels()) {
            bottomLabelAttrs[i] = accessGraph.getAttribute(GraphElementType.VERTEX, label.getAttributeName());
            bottomLabelSizes[i] = label.getSize();
            bottomLabelColors[i] = label.getColor() != null ? label.getColor() : VisualDefaults.DEFAULT_LABEL_COLOR;
            i++;
        }
    }

    private void recalculateConnectionLabels() {
        final GraphLabels connectionLabels = graphConnectionLabels != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphConnectionLabels, 0) : VisualDefaults.DEFAULT_CONNECTION_LABELS;
        final int numLabels = connectionLabels.getNumberOfLabels();
        connectionLabelAttrs = new int[numLabels];
        connectionLabelSizes = new float[numLabels];
        connectionLabelColors = new ConstellationColor[numLabels];
        int i = 0;
        for (GraphLabel label : connectionLabels.getLabels()) {
            connectionLabelAttrs[i] = accessGraph.getAttribute(GraphElementType.TRANSACTION, label.getAttributeName());
            connectionLabelSizes[i] = label.getSize();
            connectionLabelColors[i] = label.getColor() != null ? label.getColor() : VisualDefaults.DEFAULT_LABEL_COLOR;
            i++;
        }
    }

    private void recalculateDecorators() {
        final Decorators decorators = graphDecorators != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphDecorators, 0) : VisualDefaults.DEFAULT_DECORATORS;
        nwDecorator = accessGraph.getAttribute(GraphElementType.VERTEX, decorators.getNorthWestDecoratorAttribute());
        neDecorator = accessGraph.getAttribute(GraphElementType.VERTEX, decorators.getNorthEastDecoratorAttribute());
        seDecorator = accessGraph.getAttribute(GraphElementType.VERTEX, decorators.getSouthEastDecoratorAttribute());
        swDecorator = accessGraph.getAttribute(GraphElementType.VERTEX, decorators.getSouthWestDecoratorAttribute());
    }

    private void recalculateStructure() {
        rebuildConnections();
    }

    private void rebuildConnections() {
        final int linkCount = accessGraph.getLinkCount();
        final int maxTranaxtions = graphMaxTransactions != Graph.NOT_FOUND ? accessGraph.getIntValue(graphMaxTransactions, 0) : VisualDefaults.DEFAULT_MAX_TRANSACTION_TO_DRAW;
        final int connectionUpperBound = connectionMode == ConnectionMode.LINK ? linkCount : connectionMode == ConnectionMode.EDGE ? accessGraph.getEdgeCount() : accessGraph.getTransactionCount();
        connectionElementTypes = new GraphElementType[connectionUpperBound];
        connectionElementIds = new int[connectionUpperBound];
        linkStartingPositions = new int[linkCount];
        int currentPos = 0;
        for (int i = 0; i < linkCount; i++) {
            final int linkId = accessGraph.getLink(i);
            linkStartingPositions[i] = currentPos;
            switch (connectionMode) {
                case TRANSACTION:
                    if (accessGraph.getLinkTransactionCount(linkId) <= maxTranaxtions) {
                        for (int j = 0; j < accessGraph.getLinkTransactionCount(linkId); j++) {
                            connectionElementTypes[currentPos] = GraphElementType.TRANSACTION;
                            connectionElementIds[currentPos] = accessGraph.getLinkTransaction(linkId, j);
                            currentPos++;
                        }
                        break;
                    }
                // fall through
                case EDGE:
                    for (int j = 0; j < accessGraph.getLinkEdgeCount(linkId); j++) {
                        connectionElementTypes[currentPos] = GraphElementType.EDGE;
                        connectionElementIds[currentPos] = accessGraph.getLinkEdge(linkId, j);
                        currentPos++;
                    }
                    break;
                case LINK:
                    connectionElementTypes[currentPos] = GraphElementType.LINK;
                    connectionElementIds[currentPos] = linkId;
                    currentPos++;
            }
        }
        connectionElementTypes = Arrays.copyOf(connectionElementTypes, currentPos);
        connectionElementIds = Arrays.copyOf(connectionElementIds, currentPos);
    }

    @Override
    public ConstellationColor getBackgroundColor() {
        return graphBackgroundColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphBackgroundColor, 0) : VisualDefaults.DEFAULT_BACKGROUND_COLOR;
    }

    @Override
    public ConstellationColor getHighlightColor() {
        return graphHighlightColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphHighlightColor, 0) : VisualDefaults.DEFAULT_HIGHLIGHT_COLOR;
    }

    @Override
    public DrawFlags getDrawFlags() {
        final boolean visibilityThresholdExceeded = graphVisibleAboveThreshold != Graph.NOT_FOUND && !accessGraph.getBooleanValue(graphVisibleAboveThreshold, 0)
                && graphVisibilityThreshold != Graph.NOT_FOUND && accessGraph.getVertexCount() > accessGraph.getIntValue(graphVisibilityThreshold, 0);
        return graphDrawFlags != Graph.NOT_FOUND
                ? visibilityThresholdExceeded ? DrawFlags.NONE
                        : accessGraph.getObjectValue(graphDrawFlags, 0) : VisualDefaults.DEFAULT_DRAW_FLAGS;
    }

    @Override
    public Camera getCamera() {
        final Camera camera = graphCamera != Graph.NOT_FOUND
                ? accessGraph.getObjectValue(graphCamera, 0) : VisualDefaults.DEFAULT_CAMERA;
        return camera != null ? camera : VisualDefaults.DEFAULT_CAMERA;
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
        return graphBlazeSize != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphBlazeSize, 0) : VisualDefaults.DEFAULT_BLAZE_SIZE;
    }

    @Override
    public float getBlazeOpacity() {
        return graphBlazeOpacity != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphBlazeOpacity, 0) : VisualDefaults.DEFAULT_BLAZE_OPACITY;
    }

    @Override
    public float getConnectionOpacity() {
        return graphConnectionOpacity != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphConnectionOpacity, 0) : VisualDefaults.DEFAULT_CONNECTION_OPACITY;
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
    public int getConnectionLabelCount(int connection) {
        return connectionElementTypes[connection] == GraphElementType.TRANSACTION ? connectionLabelAttrs.length : 1;
    }

    @Override
    public boolean getIsLabelSummary(int connection) {
        return connectionElementTypes[connection] != GraphElementType.TRANSACTION;
    }

    @Override
    public ConstellationColor getTopLabelColor(int labelNum) {
        return topLabelColors[labelNum];
    }

    @Override
    public ConstellationColor getBottomLabelColor(int labelNum) {
        return bottomLabelColors[labelNum];
    }

    @Override
    public ConstellationColor getConnectionLabelColor(int labelNum) {
        return connectionLabelColors[labelNum];
    }

    @Override
    public float getTopLabelSize(int labelNum) {
        return topLabelSizes[labelNum];
    }

    @Override
    public float getBottomLabelSize(int labelNum) {
        return bottomLabelSizes[labelNum];
    }

    @Override
    public float getConnectionLabelSize(int labelNum) {
        return connectionLabelSizes[labelNum];
    }

    @Override
    public int getConnectionId(int connection) {
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
    public ConnectionDirection getConnectionDirection(int connection) {
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
            case TRANSACTION:
            default:
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
    }

    @Override
    public boolean getConnectionDirected(int connection) {
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
    public int getVertexId(int vertex) {
        return accessGraph.getVertex(vertex);
    }

    @Override
    public float getX(int vertex) {
        return vertexX != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexX, accessGraph.getVertex(vertex)) : VisualDefaults.getDefaultX(accessGraph.getVertex(vertex));
    }

    @Override
    public float getY(int vertex) {
        return vertexY != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexY, accessGraph.getVertex(vertex)) : VisualDefaults.getDefaultY(accessGraph.getVertex(vertex));
    }

    @Override
    public float getZ(int vertex) {
        return vertexZ != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexZ, accessGraph.getVertex(vertex)) : VisualDefaults.getDefaultZ(accessGraph.getVertex(vertex));
    }

    @Override
    public float getX2(int vertex) {
        return vertexX2 != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexX2, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_X2;
    }

    @Override
    public float getY2(int vertex) {
        return vertexY2 != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexY2, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_Y2;
    }

    @Override
    public float getZ2(int vertex) {
        return vertexZ2 != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexZ2, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_Z2;
    }

    @Override
    public ConstellationColor getVertexColor(int vertex) {
        ConstellationColor color = null;
        if (vertexColor != Graph.NOT_FOUND) {
            color = accessGraph.getObjectValue(vertexColor, accessGraph.getVertex(vertex));
        }
        return color != null ? color : VisualDefaults.DEFAULT_VERTEX_COLOR;
    }

    @Override
    public String getBackgroundIcon(int vertex) {
        return vertexBackgroundIcon != Graph.NOT_FOUND ? accessGraph.getStringValue(vertexBackgroundIcon, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_BACKGROUND_ICON;
    }

    @Override
    public String getForegroundIcon(int vertex) {
        return vertexForegroundIcon != Graph.NOT_FOUND ? accessGraph.getStringValue(vertexForegroundIcon, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_FOREGROUND_ICON;
    }

    @Override
    public boolean getVertexSelected(int vertex) {
        return vertexSelected != Graph.NOT_FOUND ? accessGraph.getBooleanValue(vertexSelected, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_SELECTED;
    }

    @Override
    public float getVertexVisibility(int vertex) {
        return vertexVisibility != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexVisibility, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_VISIBILITY;
    }

    @Override
    public boolean getVertexDimmed(int vertex) {
        return vertexDimmed != Graph.NOT_FOUND ? accessGraph.getBooleanValue(vertexDimmed, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_DIMMED;
    }

    @Override
    public float getRadius(int vertex) {
        return vertexRadius != Graph.NOT_FOUND ? accessGraph.getFloatValue(vertexRadius, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_RADIUS;
    }

    @Override
    public boolean getBlazed(int vertex) {
        final Blaze blaze = vertexBlaze != Graph.NOT_FOUND ? accessGraph.getObjectValue(vertexBlaze, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_BLAZE;
        return blaze != null;
    }

    @Override
    public int getBlazeAngle(int vertex) {
        final Blaze blaze = vertexBlaze != Graph.NOT_FOUND ? accessGraph.getObjectValue(vertexBlaze, accessGraph.getVertex(vertex)) : VisualDefaults.DEFAULT_VERTEX_BLAZE;
        return blaze == null ? VisualDefaults.DEFAULT_BLAZE_ANGLE : blaze.getAngle();
    }

    @Override
    public ConstellationColor getBlazeColor(int vertex) {
        final Blaze blaze = accessGraph.getObjectValue(vertexBlaze, accessGraph.getVertex(vertex));
        return blaze == null ? VisualDefaults.DEFAULT_BLAZE_COLOR : blaze.getColor();
    }

    @Override
    public String getNWDecorator(int vertex) {
        return nwDecorator != Graph.NOT_FOUND ? accessGraph.getStringValue(nwDecorator, accessGraph.getVertex(vertex)) : null;
    }

    @Override
    public String getNEDecorator(int vertex) {
        return neDecorator != Graph.NOT_FOUND ? accessGraph.getStringValue(neDecorator, accessGraph.getVertex(vertex)) : null;
    }

    @Override
    public String getSEDecorator(int vertex) {
        return seDecorator != Graph.NOT_FOUND ? accessGraph.getStringValue(seDecorator, accessGraph.getVertex(vertex)) : null;
    }

    @Override
    public String getSWDecorator(int vertex) {
        return swDecorator != Graph.NOT_FOUND ? accessGraph.getStringValue(swDecorator, accessGraph.getVertex(vertex)) : null;
    }

    @Override
    public ConstellationColor getConnectionColor(int connection) {
        ConstellationColor color = null;
        ConstellationColor mixColor;
        if (transactionColor != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    final int linkId = connectionElementIds[connection];
                    mixColor = graphMixColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphMixColor, 0) : VisualDefaults.DEFAULT_MIX_COLOR;
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
                    mixColor = graphMixColor != Graph.NOT_FOUND ? accessGraph.getObjectValue(graphMixColor, 0) : VisualDefaults.DEFAULT_MIX_COLOR;
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
            }
        }
        return color != null ? color : VisualDefaults.DEFAULT_TRANSACTION_COLOR;
    }

    @Override
    public boolean getConnectionSelected(int connection) {
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
        return VisualDefaults.DEFAULT_TRANSACTION_SELECTED;
    }

    @Override
    public float getConnectionVisibility(int connection) {
        if (transactionVisibility != Graph.NOT_FOUND) {
            switch (connectionElementTypes[connection]) {
                case LINK:
                    float linkVisibility = -1;
                    final int linkId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getLinkTransactionCount(linkId); i++) {
                        final int transactionId = accessGraph.getLinkTransaction(linkId, i);
                        linkVisibility = Math.max(linkVisibility, accessGraph.getFloatValue(transactionVisibility, transactionId));
                    }
                    return linkVisibility;
                case EDGE:
                    float edgeVisbility = -1;
                    final int edgeId = connectionElementIds[connection];
                    for (int i = 0; i < accessGraph.getEdgeTransactionCount(edgeId); i++) {
                        final int transactionId = accessGraph.getEdgeTransaction(edgeId, i);
                        edgeVisbility = Math.max(edgeVisbility, accessGraph.getFloatValue(transactionVisibility, transactionId));
                    }
                    return edgeVisbility;
                case TRANSACTION:
                default:
                    return accessGraph.getFloatValue(transactionVisibility, connectionElementIds[connection]);
            }
        }
        return VisualDefaults.DEFAULT_TRANSACTION_VISIBILITY;
    }

    @Override
    public boolean getConnectionDimmed(int connection) {
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
        return VisualDefaults.DEFAULT_TRANSACTION_DIMMED;
    }

    @Override
    public LineStyle getConnectionLineStyle(int connection) {
        LineStyle style = null;
        LineStyle mixStyle = LineStyle.SOLID;
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
            }
        }
        return style != null ? style : VisualDefaults.DEFAULT_TRANSACTION_LINE_STYLE;
    }

    @Override
    public float getConnectionWidth(int connection) {
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
        return VisualDefaults.DEFAULT_TRANSACTION_WIDTH;
    }

    @Override
    public int getConnectionLowVertex(int connection) {
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
        }
        return accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(linkId));
    }

    @Override
    public int getConnectionHighVertex(int connection) {
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
        }
        return accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(linkId));
    }

    @Override
    public int getLinkCount() {
        return accessGraph.getLinkCount();
    }

    @Override
    public int getLinkLowVertex(int link) {
        return accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(accessGraph.getLink(link)));
    }

    @Override
    public int getLinkHighVertex(int link) {
        return accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(accessGraph.getLink(link)));
    }

    @Override
    public int getLinkSource(int link) {
        final int linkId = accessGraph.getLink(link);
        return accessGraph.getLinkTransactionCount(linkId, Graph.DOWNHILL) > 0 && accessGraph.getLinkTransactionCount(linkId, Graph.UPHILL) == 0 ? accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(linkId)) : accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(linkId));
    }

    @Override
    public int getLinkDestination(int link) {
        final int linkId = accessGraph.getLink(link);
        return accessGraph.getLinkTransactionCount(linkId, Graph.DOWNHILL) > 0 && accessGraph.getLinkTransactionCount(linkId, Graph.UPHILL) == 0 ? accessGraph.getVertexPosition(accessGraph.getLinkLowVertex(linkId)) : accessGraph.getVertexPosition(accessGraph.getLinkHighVertex(linkId));
    }

    @Override
    public int getLinkConnectionCount(int link) {
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
    public int getLinkConnection(int link, int pos) {
        return linkStartingPositions[link] + pos;
    }

    @Override
    public String getVertexTopLabelText(int vertex, int labelNum) {
        return topLabelAttrs[labelNum] != Graph.NOT_FOUND ? accessGraph.getStringValue(topLabelAttrs[labelNum], accessGraph.getVertex(vertex)) : "";
    }

    @Override
    public String getVertexBottomLabelText(int vertex, int labelNum) {
        return bottomLabelAttrs[labelNum] != Graph.NOT_FOUND ? accessGraph.getStringValue(bottomLabelAttrs[labelNum], accessGraph.getVertex(vertex)) : "";
    }

    @Override
    public String getConnectionLabelText(int connection, int labelNum) {
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
}
