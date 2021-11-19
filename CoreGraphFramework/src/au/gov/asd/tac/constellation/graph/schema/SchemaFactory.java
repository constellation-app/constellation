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
package au.gov.asd.tac.constellation.graph.schema;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphConstants;
import au.gov.asd.tac.constellation.graph.GraphElementMerger;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConceptUtilities;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.visual.GraphLabels;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.icon.AnalyticIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.ImageIconData;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 * A factory class for the creation of schemas.
 *
 * @author sirius
 */
public abstract class SchemaFactory {

    private static final ConstellationColor ICON_COLOR = ConstellationColor.AZURE;
    private static final ConstellationIcon ICON_BACKGROUND = DefaultIconProvider.FLAT_SQUARE;
    private static final ConstellationIcon ICON_BACKGROUND_MODIFIED = DefaultIconProvider.EDGE_SQUARE;
    private static final ConstellationIcon ICON_SYMBOL = AnalyticIconProvider.STAR;

    private final Map<Class<? extends SchemaConcept>, Set<SchemaConcept>> allRegisteredConcepts;
    private final EnumMap<GraphElementType, Map<String, SchemaAttribute>> allRegisteredAttributes;
    private final List<SchemaVertexType> allRegisteredVertexTypes;
    private final List<SchemaTransactionType> allRegisteredTransactionTypes;

    /**
     * Constructor for SchemaFactory. This is where registered SchemaConcepts
     * will be processed so that this SchemaFactory knows which
     * {@link SchemaAttribute}, {@link SchemaVertexType} and
     * {@link SchemaTransactionType} objects to register to {@link Schema}
     * objects it creates.
     */
    protected SchemaFactory() {
        allRegisteredConcepts = new HashMap<>();

        // collect registered concepts in local cache
        this.getRegisteredConcepts().forEach(conceptClass -> {
            // add concept to local cache
            if (!allRegisteredConcepts.containsKey(conceptClass)) {
                allRegisteredConcepts.put(conceptClass, new HashSet<>());
            }
        });

        // lookup child concepts, and add results to local cache
        SchemaConceptUtilities.getChildConcepts(allRegisteredConcepts.keySet()).forEach(childConcept -> {
            if (!allRegisteredConcepts.containsKey(childConcept.getClass())) {
                allRegisteredConcepts.put(childConcept.getClass(), new HashSet<>());
            }

            childConcept.getParents().forEach(parentConceptClass -> {
                if (allRegisteredConcepts.containsKey(parentConceptClass)) {
                    allRegisteredConcepts.get(parentConceptClass).add(childConcept);
                }
            });
        });

        // collect all attributes and types defined by registered concepts
        final EnumMap<GraphElementType, Map<String, SchemaAttribute>> registeredAttributes = new EnumMap<>(GraphElementType.class);
        final List<SchemaVertexType> registeredVertexTypes = new ArrayList<>();
        final List<SchemaTransactionType> registeredTransactionTypes = new ArrayList<>();
        allRegisteredConcepts.forEach((parentConceptClass, childConcepts) -> {
            // instantiate concept
            SchemaConcept concept;
            try {
                concept = parentConceptClass.getDeclaredConstructor().newInstance();
            } catch (final IllegalAccessException | IllegalArgumentException
                    | InstantiationException | NoSuchMethodException
                    | SecurityException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }

            // register concept attributes to this schema factory
            if (concept.getSchemaAttributes() != null) {
                concept.getSchemaAttributes().forEach(schemaAttribute -> {
                    if (!registeredAttributes.containsKey(schemaAttribute.getElementType())) {
                        registeredAttributes.put(schemaAttribute.getElementType(), new HashMap<>());
                    }
                    registeredAttributes.get(schemaAttribute.getElementType()).put(schemaAttribute.getName(), schemaAttribute);
                });
            }

            // register concept vertex types to this schema factory
            if (concept.getSchemaVertexTypes() != null) {
                registeredVertexTypes.addAll(concept.getSchemaVertexTypes());
            }
            registeredVertexTypes.forEach(vertexType -> {
                vertexType.buildHierarchy();
            });

            // register concept transaction types to this schema factory
            if (concept.getSchemaTransactionTypes() != null) {
                registeredTransactionTypes.addAll(concept.getSchemaTransactionTypes());
            }
            registeredTransactionTypes.forEach(transactonType -> {
                transactonType.buildHierarchy();
            });
        });

        allRegisteredAttributes = new EnumMap<>(GraphElementType.class);
        registeredAttributes.keySet().forEach(elementType -> {
            allRegisteredAttributes.put(elementType, Collections.unmodifiableMap(registeredAttributes.get(elementType)));
        });
        allRegisteredVertexTypes = Collections.unmodifiableList(registeredVertexTypes);
        allRegisteredTransactionTypes = Collections.unmodifiableList(registeredTransactionTypes);
    }

    /**
     * Get the unique name for this SchemaFactory.
     * <p>
     * This value should not be changed, as it is stored in saved CONSTELLATION
     * graphs. If a CONSTELLATION graph file is opened and the schema name is
     * not recognised, the resulting graph may not behave as intended.
     * <p>
     * This value is only used internally, and as such is not visible to the
     * user.
     *
     * @return The unique name of this SchemaFactory.
     * @see #getLabel()
     */
    public abstract String getName();

    /**
     * Get the label for this SchemaFactory which will be used in CONSTELLATIONs
     * GUIs.
     * <p>
     * This value is for visual purposes only and as such can be changed without
     * effecting the operation of the application.
     *
     * @return The label of this SchemaFactory.
     */
    public abstract String getLabel();

    /**
     * Get the description for this SchemaFactory which will be used in
     * CONSTELLATIONs GUIs.
     * <p>
     * This value is for visual purposes only and as such can be changed without
     * effecting the operation of the application.
     *
     * @return The description of this SchemaFactory.
     */
    public abstract String getDescription();

    /**
     * Get an icon symbol that will be used to represent this SchemaFactory.
     *
     * @return A {@link ConstellationIcon} containing an icon representing this
     * SchemaFactory.
     */
    public ConstellationIcon getIconSymbol() {
        return ICON_SYMBOL;
    }

    /**
     * Get the background color of the icon that will be used to represent this
     * SchemaFactory.
     *
     * @return A {@link ConstellationColor} representing the color of the icon
     * representing this SchemaFactory (or null to retain the default icon
     * color).
     */
    public ConstellationColor getIconColor() {
        return ICON_COLOR;
    }

    /**
     * Get the overall icon used to represent this SchemaFactory.
     *
     * @return A {@link ConstellationColor} made up of an icon symbol over a
     * colored square background.
     */
    public final ConstellationIcon getIcon() {
        return new ConstellationIcon.Builder(getName(),
                new ImageIconData((BufferedImage) ImageUtilities.mergeImages(
                        ICON_BACKGROUND.buildBufferedImage(16, getIconColor().getJavaColor()),
                        getIconSymbol().buildBufferedImage(16), 0, 0)))
                .build();
    }

    /**
     * Get the variant icon used to represent this SchemaFactory.
     * <p>
     * This icon is currently used to indicate a graph built on a {@link Schema}
     * created using this SchemaFactory which has been modified since the last
     * save.
     *
     * @return A {@link ConstellationColor} made up of an icon symbol over a
     * colored, raised square background.
     */
    public final ConstellationIcon getModifiedIcon() {
        return new ConstellationIcon.Builder(getName() + "_modified",
                new ImageIconData((BufferedImage) ImageUtilities.mergeImages(getIcon().buildBufferedImage(16),
                        ICON_BACKGROUND_MODIFIED.buildBufferedImage(16, ConstellationColor.BLACK.getJavaColor()), 0, 0)))
                .build();
    }

    /**
     * Get the {@link Set} of parent {@link SchemaConcept} objects to register
     * to this SchemaFactory. These registered concepts and their child concepts
     * will be used to determine which
     * {@link SchemaAttribute}, {@link SchemaVertexType} and
     * {@link SchemaTransactionType} objects to register to {@link Schema}
     * objects created by this factory.
     *
     * @return A {@link Set} of {@link SchemaConcept} objects to register to
     * this SchemaFactory.
     */
    public abstract Set<Class<? extends SchemaConcept>> getRegisteredConcepts();

    /**
     * Get the collection of {@link SchemaAttribute} objects which will be
     * registered to {@link Schema} objects created by this SchemaFactory.
     *
     * @return An {@link EnumMap} of {@link GraphElementType} to {@link Map}
     * objects of {@link SchemaAttribute} objects which will be registered to
     * {@link Schema} objects created by this SchemaFactory.
     */
    public final EnumMap<GraphElementType, Map<String, SchemaAttribute>> getRegisteredAttributes() {
        return allRegisteredAttributes;
    }

    /**
     * Get a {@link Map} of all {@link SchemaAttribute} of the specified
     * {@link GraphElementType} which are registered to this SchemaFactory
     * mapped to their names.
     *
     * @param elementType The {@link GraphElementType} of the registered
     * attributes you wish to retrieve.
     *
     * @return A {@link Map} of {@link SchemaAttribute} instances mapped to
     * their names.
     */
    public final Map<String, SchemaAttribute> getRegisteredAttributes(final GraphElementType elementType) {
        return allRegisteredAttributes.get(elementType) != null ? allRegisteredAttributes.get(elementType) : Collections.emptyMap();
    }

    /**
     * Ensure that an {@link Attribute} is created on a graph. If the specified
     * attribute is registered to this SchemaFactory, the attribute will be
     * created on the specified graph and returned, otherwise the error value
     * {@link Graph#NOT_FOUND} will be returned.
     *
     * @param graph the {@link GraphWriteMethods} to add the attribute to.
     * @param elementType the {@link GraphElementType} the attribute applies to.
     * @param attributeName the name of the attribute you wish to add.
     *
     * @return an int representing the ensured attribute.
     */
    public final int ensureAttribute(final GraphWriteMethods graph, final GraphElementType elementType, final String attributeName) {
        int attribute = graph.getAttribute(elementType, attributeName);
        if (attribute == Graph.NOT_FOUND) {
            final SchemaAttribute registeredAttribute = getRegisteredAttributes(elementType).get(attributeName);
            if (registeredAttribute == null) {
                return Graph.NOT_FOUND;
            }
            attribute = graph.addAttribute(elementType, registeredAttribute.getAttributeType(),
                    registeredAttribute.getName(), registeredAttribute.getDescription(),
                    registeredAttribute.getDefault(), registeredAttribute.getAttributeMergerId());
            graph.setAttributeIndexType(attribute, registeredAttribute.getIndexType());
        }
        return attribute;
    }

    /**
     * Ensure that an {@link Attribute} is created on a graph. If the specified
     * attribute is registered to this SchemaFactory, the attribute will be
     * created on the specified graph and returned, otherwise the error value
     * {@link Graph#NOT_FOUND} will be returned.
     *
     * @param graph the {@link GraphWriteMethods} to add the attribute to.
     * @param schemaAttribute the {@link SchemaAttribute} to ensure
     * @param boundBySchema if true, and the specified attribute is not
     * registered to this {@link SchemaFactory}, then this method will return
     * {@link GraphConstants#NOT_FOUND}, otherwise the attribute will be created
     * regardless
     *
     * @return an int representing the ensured attribute.
     */
    public final int ensureAttribute(final GraphWriteMethods graph, final SchemaAttribute schemaAttribute, final boolean boundBySchema) {
        int attribute = schemaAttribute.get(graph);
        if (attribute == Graph.NOT_FOUND) {
            final SchemaAttribute registeredAttribute = getRegisteredAttributes(schemaAttribute.getElementType()).get(schemaAttribute.getName());
            if (boundBySchema && registeredAttribute == null) {
                return Graph.NOT_FOUND;
            }
            attribute = graph.addAttribute(schemaAttribute.getElementType(), schemaAttribute.getAttributeType(),
                    schemaAttribute.getName(), schemaAttribute.getDescription(),
                    schemaAttribute.getDefault(), schemaAttribute.getAttributeMergerId());
            graph.setAttributeIndexType(attribute, schemaAttribute.getIndexType());
        }
        return attribute;
    }

    /**
     * Get the {@link List} of {@link SchemaAttribute} which are key attributes
     * for this SchemaFactory for the specified {@link GraphElementType}.
     * <p>
     * A key attribute is an attribute which should be unique for the graph
     * element it belongs to. Any two graph elements of the same
     * {@link GraphElementType} which have identical values for all key
     * attributes will be considered identical entities and merged by the graph.
     *
     * @param elementType The {@link GraphElementType} of the keys you wish to
     * retrieve.
     *
     * @return The {@link List} of {@link SchemaAttribute} that are keys.
     */
    public List<SchemaAttribute> getKeyAttributes(final GraphElementType elementType) {
        return Collections.emptyList();
    }

    /**
     * Set the vertex and transactions primary keys on a graph.
     *
     * @param graph The {@link GraphWriteMethods} that will have its primary
     * keys * set.
     */
    public final void ensureKeyAttributes(final GraphWriteMethods graph) {
        for (final GraphElementType elementType : new GraphElementType[]{GraphElementType.VERTEX, GraphElementType.TRANSACTION}) {
            final List<SchemaAttribute> keys = getKeyAttributes(elementType);
            final int[] attrIds = new int[keys.size()];
            for (int i = 0; i < keys.size(); i++) {
                attrIds[i] = keys.get(i).ensure(graph);
            }

            graph.setPrimaryKey(elementType, attrIds);
        }
    }

    /**
     * Get the collection of {@link SchemaVertexType} objects which will be
     * registered to {@link Schema} objects created by this SchemaFactory.
     *
     * @return A {@link List} of {@link SchemaVertexType} objects which will be
     * registered to {@link Schema} objects created by this SchemaFactory.
     */
    public final List<SchemaVertexType> getRegisteredVertexTypes() {
        return allRegisteredVertexTypes;
    }

    /**
     * Get the collection of {@link SchemaTransactionType} objects which will be
     * registered to {@link Schema} objects created by this SchemaFactory.
     *
     * @return A {@link List} of {@link SchemaTransactionType} objects which
     * will be registered to {@link Schema} objects created by this
     * SchemaFactory.
     */
    public final List<SchemaTransactionType> getRegisteredTransactionTypes() {
        return allRegisteredTransactionTypes;
    }

    /**
     * Get the {@link ConstellationColor} to apply to vertex labels under this
     * SchemaFactory.
     *
     * @return The default vertex label color.
     */
    public ConstellationColor getVertexLabelColor() {
        return ConstellationColor.WHITE;
    }

    /**
     * Get the {@link ConstellationColor} to apply to connection (transaction,
     * edge and link) labels under this SchemaFactory.
     *
     * @return The default connection label color.
     */
    public ConstellationColor getConnectionLabelColor() {
        return ConstellationColor.LIGHT_GREEN;
    }

    public GraphLabels getVertexTopLabels() {
        return GraphLabels.NO_LABELS;
    }

    public GraphLabels getBottomLabels() {
        return GraphLabels.NO_LABELS;
    }

    public GraphLabels getTransactionLabels() {
        return GraphLabels.NO_LABELS;
    }

    public VertexDecorators getDecorators() {
        return VertexDecorators.NO_DECORATORS;
    }

    /**
     * Get whether this schema is a primary schema. A primary schema is one
     * which a user would be expected to use, and so will be publicly available
     * within CONSTELLATION.
     *
     * @return True if this is a primary schema, false otherwise.
     */
    public boolean isPrimarySchema() {
        return true;
    }

    /**
     * Returns the {@link GraphElementMerger} that should be used to merge
     * elements on graphs using this schema. The default implementation looks up
     * the default {@link GraphElementMerger} in the application and returns
     * that. This method should never return null. The
     * {@link GraphElementMerger} returned should be stateless, as it may be
     * copied into more than one graph.
     *
     * @return the {@link GraphElementMerger} that should be used to merge
     * elements on graphs using this schema.
     */
    public GraphElementMerger getGraphElementMerger() {
        return Lookup.getDefault().lookup(GraphElementMerger.class);
    }

    /**
     * Create a new {@link Schema} that will provide context to a graph.
     *
     * @return A {@link Schema} object.
     */
    public abstract Schema createSchema();
}
