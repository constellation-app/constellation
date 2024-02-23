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
package au.gov.asd.tac.constellation.graph.schema.analytic;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.SpatialConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept.ConstellationViewsConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.geospatial.Country;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * A factory for the Analytic Schema.
 *
 * @author cygnus_x-1
 * @author antares
 */
@ServiceProvider(service = SchemaFactory.class, position = Integer.MAX_VALUE - 2)
public class AnalyticSchemaFactory extends VisualSchemaFactory {

    // Note: changing this value will break backwards compatibility!
    public static final String ANALYTIC_SCHEMA_ID = "au.gov.asd.tac.constellation.graph.schema.AnalyticSchemaFactory";

    private static final ConstellationIcon ICON_SYMBOL = AnalyticIconProvider.GRAPH;
    private static final ConstellationColor ICON_COLOR = ConstellationColor.CARROT;

    @Override
    public String getName() {
        return ANALYTIC_SCHEMA_ID;
    }

    @Override
    public String getLabel() {
        return "Analytic Graph";
    }

    @Override
    public String getDescription() {
        return "This schema provides support for analysis";
    }

    @Override
    public ConstellationIcon getIconSymbol() {
        return ICON_SYMBOL;
    }

    @Override
    public ConstellationColor getIconColor() {
        return ICON_COLOR;
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getRegisteredConcepts() {
        final Set<Class<? extends SchemaConcept>> registeredConcepts = new HashSet<>();
        registeredConcepts.add(ConstellationViewsConcept.class);
        registeredConcepts.add(VisualConcept.class);
        registeredConcepts.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(registeredConcepts);
    }

    @Override
    public List<SchemaAttribute> getKeyAttributes(final GraphElementType elementType) {
        final List<SchemaAttribute> keys;
        switch (elementType) {
            case VERTEX:
                keys = Arrays.asList(
                        VisualConcept.VertexAttribute.IDENTIFIER,
                        AnalyticConcept.VertexAttribute.TYPE);
                break;
            case TRANSACTION:
                keys = Arrays.asList(VisualConcept.TransactionAttribute.IDENTIFIER,
                        AnalyticConcept.TransactionAttribute.TYPE,
                        TemporalConcept.TransactionAttribute.DATETIME);
                break;
            default:
                keys = Collections.emptyList();
                break;
        }

        return Collections.unmodifiableList(keys);
    }

    @Override
    public Schema createSchema() {
        return new AnalyticSchema(this);
    }

    protected class AnalyticSchema extends VisualSchema {

        public AnalyticSchema(final SchemaFactory factory) {
            super(factory);
        }

        @Override
        public void newGraph(final GraphWriteMethods graph) {
            super.newGraph(graph);
            ensureKeyAttributes(graph);
        }

        @Override
        public void newVertex(final GraphWriteMethods graph, final int vertexId) {
            super.newVertex(graph, vertexId);

            final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
            final int vertexSourceAttribute = AnalyticConcept.VertexAttribute.SOURCE.ensure(graph);

            graph.setObjectValue(vertexTypeAttribute, vertexId, SchemaConceptUtilities.getDefaultVertexType());
            graph.setStringValue(vertexSourceAttribute, vertexId, "Manually Created");

            graph.validateKey(GraphElementType.VERTEX, vertexId, false);
            completeVertex(graph, vertexId);
        }

        @Override
        public void completeVertex(final GraphWriteMethods graph, final int vertexId) {
            final int vertexIdentifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.ensure(graph);
            final int vertexTypeAttribute = AnalyticConcept.VertexAttribute.TYPE.ensure(graph);
            final int vertexRawAttribute = AnalyticConcept.VertexAttribute.RAW.ensure(graph);
            final int vertexLabelAttribute = VisualConcept.VertexAttribute.LABEL.ensure(graph);
            final int vertexColorAttribute = VisualConcept.VertexAttribute.COLOR.ensure(graph);
            final int vertexBackgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);
            final int vertexForegroundIconAttribute = VisualConcept.VertexAttribute.FOREGROUND_ICON.ensure(graph);

            String identifier = graph.getStringValue(vertexIdentifierAttribute, vertexId);
            SchemaVertexType type = graph.getObjectValue(vertexTypeAttribute, vertexId);
            RawData raw = graph.getObjectValue(vertexRawAttribute, vertexId);
            String label = graph.getStringValue(vertexLabelAttribute, vertexId);

            // set the raw
            if (raw == null || raw.isEmpty()) {
                raw = new RawData(identifier, type == null ? null : type.getName());
            }

            // set the type - preference is type > raw > label > unknown
            if (type == null) {
                type = SchemaVertexTypeUtilities.getDefaultType();
                if (StringUtils.isNotBlank(raw.getRawType())) {
                    type = graph.getSchema().resolveVertexType(raw.getRawType());
                } else if (StringUtils.isNotBlank(label)) {
                    final RawData rawLabel = new RawData(label);
                    if (StringUtils.isNotBlank(rawLabel.getRawType())) {
                        type = graph.getSchema().resolveVertexType(rawLabel.getRawType());
                    }
                }
            } else if (type.isIncomplete()) {
                type = graph.getSchema().resolveVertexType(type.toString());
            }

            // set the identifier - preference is identifier > raw > label > unknown
            if (StringUtils.isBlank(identifier)) {
                identifier = "unknown";
                if (StringUtils.isNotBlank(raw.getRawIdentifier())) {
                    identifier = raw.getRawIdentifier();
                } else if (StringUtils.isNotBlank(label)) {
                    final RawData rawLabel = new RawData(label);
                    if (StringUtils.isNotBlank(rawLabel.getRawIdentifier())) {
                        identifier = rawLabel.getRawIdentifier();
                    }
                }
            }

            // set the raw and label from the resolved identifier and type
            final RawData resolved = new RawData(identifier, type == null ? null : type.getName());
            raw = RawData.merge(resolved, raw);
            label = resolved.toString();

            // write changes to graph
            if (!StringUtils.equals(identifier, graph.getStringValue(vertexIdentifierAttribute, vertexId))) {
                graph.setStringValue(vertexIdentifierAttribute, vertexId, identifier);
            }

            if (type != null && type != SchemaVertexTypeUtilities.getDefaultType() && !type.equals(graph.getObjectValue(vertexTypeAttribute, vertexId))) {
                graph.setObjectValue(vertexTypeAttribute, vertexId, type);
            }

            if (raw != null && !raw.equals(graph.getObjectValue(vertexRawAttribute, vertexId))) {
                graph.setObjectValue(vertexRawAttribute, vertexId, raw);
            }

            if (!StringUtils.equals(label, graph.getStringValue(vertexLabelAttribute, vertexId))) {
                graph.setStringValue(vertexLabelAttribute, vertexId, label);
            }

            if (type != null && (type != SchemaVertexTypeUtilities.getDefaultType() || graph.isDefaultValue(vertexColorAttribute, vertexId))
                    && !Objects.equals(type.getColor(), graph.getObjectValue(vertexColorAttribute, vertexId))) {
                graph.setObjectValue(vertexColorAttribute, vertexId, type.getColor());
            }

            if (type != null && (type != SchemaVertexTypeUtilities.getDefaultType() || graph.isDefaultValue(vertexBackgroundIconAttribute, vertexId))
                    && !Objects.equals(type.getBackgroundIcon(), graph.getObjectValue(vertexBackgroundIconAttribute, vertexId))) {
                graph.setObjectValue(vertexBackgroundIconAttribute, vertexId, type.getBackgroundIcon().getExtendedName());
            }

            if (type != null && (type != SchemaVertexTypeUtilities.getDefaultType() || graph.isDefaultValue(vertexForegroundIconAttribute, vertexId))) {
                if (IconManager.iconExists(type.toString())) {
                    if (!Objects.equals(type.toString(), graph.getObjectValue(vertexForegroundIconAttribute, vertexId))) {
                        graph.setObjectValue(vertexForegroundIconAttribute, vertexId, type.toString());
                    }
                } else if (!Objects.equals(type.getForegroundIcon(), graph.getObjectValue(vertexForegroundIconAttribute, vertexId))) {
                    graph.setObjectValue(vertexForegroundIconAttribute, vertexId, type.getForegroundIcon().getExtendedName());
                }
            }

            // analytic attribute cleanup
            final int vertexCountryAttribute = SpatialConcept.VertexAttribute.COUNTRY.get(graph);
            if (vertexCountryAttribute != GraphConstants.NOT_FOUND) {
                final String countryValue = graph.getStringValue(vertexCountryAttribute, vertexId);
                final Country country = Country.lookupCountryDigraph(countryValue);
                if (country != null) {
                    graph.setStringValue(vertexCountryAttribute, vertexId, country.getDisplayName());
                }
            }
            applyColorblindVertex(graph, vertexId);
        }

        @Override
        public SchemaVertexType resolveVertexType(final String type) {
            SchemaVertexType resolvedType = SchemaVertexTypeUtilities.getType(type);
            if (SchemaConceptUtilities.getDefaultVertexType().equals(resolvedType)
                    && !SchemaConceptUtilities.getDefaultVertexType().getName().equals(type)) {
                resolvedType = SchemaConceptUtilities.getDefaultVertexType().rename(type);
            }

            return resolvedType;
        }

        @Override
        public void newTransaction(final GraphWriteMethods graph, final int transactionId) {
            super.newTransaction(graph, transactionId);

            final int transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
            final int transactionDatetimeAttribuute = TemporalConcept.TransactionAttribute.DATETIME.ensure(graph);
            final int transactionSourceAttribute = AnalyticConcept.TransactionAttribute.SOURCE.ensure(graph);
            final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);

            final boolean transactionDirected = graph.getTransactionDirection(transactionId) != Graph.UNDIRECTED;

            graph.setObjectValue(transactionTypeAttribute, transactionId, SchemaTransactionTypeUtilities.getDefaultType());
            graph.setStringValue(transactionDatetimeAttribuute, transactionId, null);
            graph.setStringValue(transactionSourceAttribute, transactionId, "Manually Created");
            graph.setBooleanValue(transactionDirectedAttribute, transactionId, transactionDirected);

            graph.validateKey(GraphElementType.TRANSACTION, transactionId, false);
            completeTransaction(graph, transactionId);
        }

        @Override
        public void completeTransaction(final GraphWriteMethods graph, final int transactionId) {
            final int transactionIdentifierAttribute = VisualConcept.TransactionAttribute.IDENTIFIER.ensure(graph);
            final int transactionTypeAttribute = AnalyticConcept.TransactionAttribute.TYPE.ensure(graph);
            final int transactionLabelAttribute = VisualConcept.TransactionAttribute.LABEL.ensure(graph);
            final int transactionColorAttribute = VisualConcept.TransactionAttribute.COLOR.ensure(graph);
            final int transactionStyleAttribute = VisualConcept.TransactionAttribute.LINE_STYLE.ensure(graph);
            final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);

            String identifier = graph.getStringValue(transactionIdentifierAttribute, transactionId);
            SchemaTransactionType type = graph.getObjectValue(transactionTypeAttribute, transactionId);
            String label = graph.getStringValue(transactionLabelAttribute, transactionId);

            // set the type - preference is type > label > unknown
            if (type == null) {
                if (StringUtils.isNotBlank(label)) {
                    final RawData rawLabel = new RawData(label);
                    if (StringUtils.isNotBlank(rawLabel.getRawType())) {
                        type = resolveTransactionType(rawLabel.getRawType());
                    }
                } else {
                    type = SchemaTransactionTypeUtilities.getDefaultType();
                }
            } else if (type.isIncomplete()) {
                type = resolveTransactionType(type.getName());
            }

            // set the identifier - preference is identifier > label > unknown
            if (StringUtils.isBlank(identifier)) {
                identifier = "unknown";
                if (StringUtils.isNotBlank(label)) {
                    final RawData rawLabel = new RawData(label);
                    if (StringUtils.isNotBlank(rawLabel.getRawIdentifier())) {
                        identifier = rawLabel.getRawIdentifier();
                    }
                }
            }

            // set label from the resolved type
            label = type == null ? null : type.getName();

            // set new values on the graph
            if (!StringUtils.equals(identifier, graph.getStringValue(transactionIdentifierAttribute, transactionId))) {
                graph.setStringValue(transactionIdentifierAttribute, transactionId, identifier);
            }

            if (type != null && type != SchemaTransactionTypeUtilities.getDefaultType() && type != graph.getObjectValue(transactionTypeAttribute, transactionId)) {
                graph.setObjectValue(transactionTypeAttribute, transactionId, type);
            }

            if (!StringUtils.equals(label, graph.getStringValue(transactionLabelAttribute, transactionId))) {
                graph.setStringValue(transactionLabelAttribute, transactionId, label);
            }

            if (type != null && (type != SchemaTransactionTypeUtilities.getDefaultType() || graph.isDefaultValue(transactionColorAttribute, transactionId))
                    && !Objects.equals(type.getColor(), graph.getObjectValue(transactionColorAttribute, transactionId))) {
                graph.setObjectValue(transactionColorAttribute, transactionId, type.getColor());
            }

            if (type != null && (type != SchemaTransactionTypeUtilities.getDefaultType() || graph.isDefaultValue(transactionStyleAttribute, transactionId))
                    && !Objects.equals(type.getStyle(), graph.getObjectValue(transactionStyleAttribute, transactionId))) {
                graph.setObjectValue(transactionStyleAttribute, transactionId, type.getStyle());
            }
            // Previously, null and empty types were treated separately.
            // Since treating them the same (which makes sense), we were seeing
            // some unexpected behaviour with blank types.
            // see https://github.com/constellation-app/constellation/issues/723#issuecomment-662241467
            // see also https://github.com/constellation-app/constellation/pull/735
            if (type != null && type != SchemaTransactionTypeUtilities.getDefaultType() && !Objects.equals(type.isDirected(), graph.getBooleanValue(transactionDirectedAttribute, transactionId))) {
                graph.setBooleanValue(transactionDirectedAttribute, transactionId, type.isDirected());
            }

            final boolean directed = graph.getBooleanValue(transactionDirectedAttribute, transactionId);
            final boolean transactionIsDirected = graph.getTransactionDirection(transactionId) != Graph.FLAT;
            if (directed != transactionIsDirected) {
                // this next bit is done to ensure that transactions merge to the appropriate edge/link group when updated
                // (by changing the hidden direction of the transaction)
                final int sourceVertexId = graph.getTransactionSourceVertex(transactionId);
                final int destinationVertexId = graph.getTransactionDestinationVertex(transactionId);
                final int newTransactionId = graph.addTransaction(sourceVertexId, destinationVertexId, directed);

                for (int i = 0; i < graph.getAttributeCount(GraphElementType.TRANSACTION); i++) {
                    final int attributeId = graph.getAttribute(GraphElementType.TRANSACTION, i);
                    graph.setObjectValue(attributeId, newTransactionId, graph.getObjectValue(attributeId, transactionId));
                }

                graph.removeTransaction(transactionId);
            }
            applyColorblindTransaction(graph, transactionId);            
        }

        @Override
        public SchemaTransactionType resolveTransactionType(final String type) {
            SchemaTransactionType resolvedType = SchemaTransactionTypeUtilities.getType(type);
            if (SchemaTransactionTypeUtilities.getDefaultType().equals(resolvedType)
                    && !SchemaTransactionTypeUtilities.getDefaultType().getName().equals(type)) {
                resolvedType = SchemaTransactionTypeUtilities.getDefaultType().rename(type);
            }

            return resolvedType;
        }

        @Override
        public int getVertexAliasAttribute(final GraphReadMethods graph) {
            return VisualConcept.VertexAttribute.LABEL.get(graph);
        }
    }
}
