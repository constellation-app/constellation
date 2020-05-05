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
package au.gov.asd.tac.constellation.graph.schema.visual;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttributeUtilities;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept.ConstellationViewsConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.visual.LineStyle;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * A factory for the Visual Schema.
 *
 * @author sirius
 */
@ServiceProvider(service = SchemaFactory.class, position = Integer.MAX_VALUE - 1)
public class VisualSchemaFactory extends SchemaFactory {

    // Note: changing this value will break backwards compatibility!
    public static final String VISUAL_SCHEMA_ID = "au.gov.asd.tac.constellation.graph.schema.VisualSchemaFactory";

    @Override
    public String getName() {
        return VISUAL_SCHEMA_ID;
    }

    @Override
    public String getLabel() {
        return "Visual Graph";
    }

    @Override
    public String getDescription() {
        return "This schema provides support for visualisation and interaction";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getRegisteredConcepts() {
        final Set<Class<? extends SchemaConcept>> registeredConcepts = new HashSet<>();
        registeredConcepts.add(ConstellationViewsConcept.class);
        registeredConcepts.add(VisualConcept.class);
        return Collections.unmodifiableSet(registeredConcepts);
    }

    @Override
    public List<SchemaAttribute> getKeyAttributes(final GraphElementType elementType) {
        final List<SchemaAttribute> keys;
        switch (elementType) {
            case VERTEX:
                keys = Arrays.asList(VisualConcept.VertexAttribute.IDENTIFIER);
                break;
            case TRANSACTION:
                keys = Arrays.asList(VisualConcept.TransactionAttribute.IDENTIFIER);
                break;
            default:
                keys = Collections.emptyList();
                break;
        }

        return Collections.unmodifiableList(keys);
    }

    @Override
    public GraphLabels getBottomLabels() {
        final GraphLabel nameLabel = new GraphLabel(VisualConcept.VertexAttribute.LABEL.getName(), getVertexLabelColor());
        return new GraphLabels(Arrays.asList(nameLabel));
    }

    @Override
    public GraphLabels getVertexTopLabels() {
        final List<GraphLabel> labels = new ArrayList<>();
        SchemaAttributeUtilities.getAttributes(GraphElementType.VERTEX).forEach(attribute -> {
            if (attribute.isLabel()) {
                labels.add(new GraphLabel(attribute.getName(), getVertexLabelColor()));
            }
        });
        Collections.sort(labels);
        return new GraphLabels(labels);
    }

    @Override
    public GraphLabels getTransactionLabels() {
        final List<GraphLabel> labels = new ArrayList<>();
        SchemaAttributeUtilities.getAttributes(GraphElementType.TRANSACTION).forEach(attribute -> {
            if (attribute.isLabel()) {
                labels.add(new GraphLabel(attribute.getName(), getVertexLabelColor()));
            }
        });
        Collections.sort(labels);
        return new GraphLabels(labels);
    }

    @Override
    public VertexDecorators getDecorators() {
        final List<String> decorators = new ArrayList<>();
        SchemaAttributeUtilities.getAttributes(GraphElementType.VERTEX).forEach(attribute -> {
            if (decorators.size() < 4 && attribute.isDecorator()) {
                decorators.add(attribute.getName());
            }
        });

        // sort the decorators list to have some consistency
        Collections.sort(decorators);

        return new VertexDecorators(
                !decorators.isEmpty() ? decorators.get(0) : null,
                decorators.size() > 1 ? decorators.get(1) : null,
                decorators.size() > 2 ? decorators.get(2) : null,
                decorators.size() > 3 ? decorators.get(3) : null
        );
    }

    @Override
    public Schema createSchema() {
        return new VisualSchema(this);
    }

    protected class VisualSchema extends Schema {

        private final SecureRandom random = new SecureRandom();

        public VisualSchema(final SchemaFactory factory) {
            super(factory);
        }

        @Override
        public void newGraph(final GraphWriteMethods graph) {
            super.newGraph(graph);
            ensureKeyAttributes(graph);

            final int topLabelsAttribute = VisualConcept.GraphAttribute.TOP_LABELS.ensure(graph);
            final int bottomLabelsAttribute = VisualConcept.GraphAttribute.BOTTOM_LABELS.ensure(graph);
            final int transactionLabelsAttribute = VisualConcept.GraphAttribute.TRANSACTION_LABELS.ensure(graph);
            final int decoratorsAttribute = VisualConcept.GraphAttribute.DECORATORS.ensure(graph);

            graph.setObjectValue(topLabelsAttribute, 0, getVertexTopLabels());
            graph.setObjectValue(bottomLabelsAttribute, 0, getBottomLabels());
            graph.setObjectValue(transactionLabelsAttribute, 0, getTransactionLabels());
            graph.setObjectValue(decoratorsAttribute, 0, getDecorators());
        }

        @Override
        public void newVertex(final GraphWriteMethods graph, final int vertexId) {
            super.newVertex(graph, vertexId);

            final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
            final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
            final int vertexRadiusAttribute = VisualConcept.VertexAttribute.NODE_RADIUS.ensure(graph);
            final int vertexVisibilityAttribute = VisualConcept.VertexAttribute.VISIBILITY.ensure(graph);
            final int vertexDimAttribute = VisualConcept.VertexAttribute.DIMMED.ensure(graph);
            final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
            final int vertexBackgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
            final int vertexForegroundIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);

            int uniqueId = vertexId;
            boolean validKeys = false;
            while (!validKeys) {
                try {
                    validKeys = true;
                    graph.setStringValue(vertexIdentifierAttribute, vertexId, "Vertex #" + uniqueId);
                    graph.setStringValue(vertexLabelAttribute, vertexId, "Vertex #" + uniqueId);
                    graph.validateKey(GraphElementType.VERTEX, vertexId, false);
                } catch (DuplicateKeyException ex) {
                    validKeys = false;
                    uniqueId++;
                }
            }

            final float radius = (Float) (graph.getAttributeDefaultValue(vertexRadiusAttribute));
            final float nradius = radius <= 0f ? 1f : radius;
            graph.setFloatValue(vertexRadiusAttribute, vertexId, nradius);
            graph.setFloatValue(vertexVisibilityAttribute, vertexId, 1f);
            graph.setBooleanValue(vertexDimAttribute, vertexId, false);

            final Object colorDefaultValue = graph.getAttributeDefaultValue(vertexColorAttribute);
            if (colorDefaultValue == null) {
                graph.setObjectValue(vertexColorAttribute, vertexId, randomColor());
            } else {
                graph.setObjectValue(vertexColorAttribute, vertexId, colorDefaultValue);
            }

            final Object backgroundIconDefaultValue = graph.getAttributeDefaultValue(vertexBackgroundIconAttribute);
            if ((backgroundIconDefaultValue == null) || (backgroundIconDefaultValue.toString().isEmpty())) {
                graph.setObjectValue(vertexBackgroundIconAttribute, vertexId, DefaultIconProvider.FLAT_SQUARE);
            } else {
                graph.setObjectValue(vertexBackgroundIconAttribute, vertexId, backgroundIconDefaultValue);
            }

            final Object foregroundIconDefaultValue = graph.getAttributeDefaultValue(vertexForegroundIconAttribute);
            if ((foregroundIconDefaultValue == null) || (foregroundIconDefaultValue.toString().isEmpty())) {
                graph.setStringValue(vertexForegroundIconAttribute, vertexId, "");
            } else {
                graph.setStringValue(vertexForegroundIconAttribute, vertexId, foregroundIconDefaultValue.toString());
            }

            graph.validateKey(GraphElementType.VERTEX, vertexId, false);
        }

        @Override
        public void completeVertex(final GraphWriteMethods graph, final int vertexId) {
            super.completeVertex(graph, vertexId);

            final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
            final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);

            final String identifier = graph.getStringValue(vertexIdentifierAttribute, vertexId);
            final String label = graph.getStringValue(vertexLabelAttribute, vertexId);
            if (identifier != null && label == null) {
                graph.setStringValue(vertexLabelAttribute, vertexId, identifier);
            }
        }

        @Override
        public void newTransaction(final GraphWriteMethods graph, final int transactionId) {
            super.newTransaction(graph, transactionId);

            final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
            final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
            final int transactionWidthAttribute = VisualConcept.TransactionAttribute.WIDTH.ensure(graph);
            final int transactionVisibilityAttribute = VisualConcept.TransactionAttribute.VISIBILITY.ensure(graph);
            final int transactionDimAttribute = VisualConcept.TransactionAttribute.DIMMED.ensure(graph);
            final int transactionColorAttribute = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
            final int transactionStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(graph);

            int uniqueId = transactionId;
            boolean validKeys = false;
            while (!validKeys) {
                try {
                    validKeys = true;
                    final String identifier = "Transaction #" + uniqueId;
                    graph.setStringValue(transactionIdentifierAttribute, transactionId, identifier);
                    graph.setStringValue(transactionLabelAttribute, transactionId, identifier);
                    graph.validateKey(GraphElementType.TRANSACTION, transactionId, false);
                } catch (final DuplicateKeyException ex) {
                    validKeys = false;
                    uniqueId++;
                }
            }

            graph.setFloatValue(transactionWidthAttribute, transactionId, 1f);
            graph.setFloatValue(transactionVisibilityAttribute, transactionId, 1f);
            graph.setBooleanValue(transactionDimAttribute, transactionId, false);

            final Object colorDefaultValue = graph.getAttributeDefaultValue(transactionColorAttribute);
            if (colorDefaultValue == null) {
                graph.setObjectValue(transactionColorAttribute, transactionId, randomColor());
            } else {
                graph.setObjectValue(transactionColorAttribute, transactionId, colorDefaultValue);
            }

            graph.setObjectValue(transactionStyleAttribute, transactionId, LineStyle.SOLID);

            graph.validateKey(GraphElementType.TRANSACTION, transactionId, false);
        }

        @Override
        public void completeTransaction(final GraphWriteMethods graph, final int transactionId) {
            super.completeVertex(graph, transactionId);

            final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
            final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);

            final String identifier = graph.getStringValue(transactionIdentifierAttribute, transactionId);
            final String label = graph.getStringValue(transactionLabelAttribute, transactionId);
            if (identifier != null && label == null) {
                graph.setStringValue(transactionLabelAttribute, transactionId, identifier);
            }
        }

        private ConstellationColor randomColor() {
            return ConstellationColor.getColorValue(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1.0f);
        }
    }
}
