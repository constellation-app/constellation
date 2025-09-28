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
package au.gov.asd.tac.constellation.graph.schema;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaTransactionType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;

/**
 * A Schema provides domain specific context to the otherwise data agnostic
 * graph.
 *
 * In general, the graph aims to be completely generic in terms of the data it
 * holds, providing only the ability to add attributes to graph elements with no
 * specific help to the user regarding their specific use case.
 *
 * Each graph has an attached schema that is given the opportunity to modify the
 * graph at specific times in the graph's life cycle in order to customise the
 * graph for a specific use case. These times include when the graph is created,
 * when a new element is created or removed, or when an element has finished
 * being configured.
 *
 * @author sirius
 */
public class Schema {

    private final SchemaFactory factory;

    /**
     * Create a new schema using the provided {@link SchemaFactory}.
     *
     * @param factory the {@link SchemaFactory} which will be used to build this
     * Schema.
     */
    public Schema(final SchemaFactory factory) {
        this.factory = factory;
    }

    /**
     * Get the {@link SchemaFactory} used to create this Schema.
     *
     * @return a {@link SchemaFactory}
     */
    public SchemaFactory getFactory() {
        return factory;
    }

    /**
     * Initialise the graph using this Schema.
     *
     * @param graph the {@link GraphWriteMethods} to initialise.
     */
    public void newGraph(final GraphWriteMethods graph) {
        for (final GraphElementType elementType : factory.getRegisteredAttributes().keySet()) {
            for (final SchemaAttribute attribute : factory.getRegisteredAttributes().get(elementType).values()) {
                if (attribute.isCreate()) {
                    attribute.ensure(graph);
                }
            }
        }
    }

    /**
     * Called by the framework a by another process. This gives the schema a
     * chance to further modify the attributes of the graph to provide domain
     * specific values automatically. A common use case would be the setting the
     * graph density attribute
     *
     * @param graph the graph holding the vertex.
     */
    public void completeGraph(final GraphWriteMethods graph) {
        //Unpopulated method, called in CompleteSchemaPlugin
    }

    /**
     * Called by the framework when a new vertex has been created. This allows
     * the schema to set up a default state for a new vertex before any other
     * processes are allowed to alter it. Following this, the process that
     * created the vertex often modifies the values of its attributes in a
     * domain specific way. The schema then gets another chance to modify the
     * vertex when the framework calls
     * {@link Schema#completeVertex(au.gov.asd.tac.constellation.graph.GraphWriteMethods, int)}.
     *
     * @param graph the {@link GraphWriteMethods} holding the vertex.
     * @param vertexId the id of the vertex that has just been created.
     */
    public void newVertex(final GraphWriteMethods graph, final int vertexId) {
    }

    /**
     * Called by the framework after a vertex has been created and initialized
     * by another process. This gives the schema a chance to further modify the
     * attributes of the vertex to provide domain specify values automatically.
     * A common use case would be the setting of an icon/color based on a type
     * attribute.
     *
     * @param graph the graph holding the vertex.
     * @param vertexId the id of the vertex that has been added.
     */
    public void completeVertex(final GraphWriteMethods graph, final int vertexId) {
    }

    /**
     * Given a prospective type name as a {@link String}, infer the matching
     * {@link SchemaVertexType}.
     *
     * @param type A {@link String} representing the name of a
     * {@link SchemaVertexType}.
     * @return The resolved {@link SchemaVertexType}.
     */
    public SchemaVertexType resolveVertexType(final String type) {
        return null;
    }

    /**
     * Called by the framework when a new transaction has been created. This
     * allows the schema to set up a default state for a new transaction before
     * any other processes are allowed to alter it. Following this, the process
     * that created the transaction often modifies the values of its attributes
     * in a domain specific way. The schema then gets another chance to modify
     * the transaction when the framework calls
     * {@link Schema#completeTransaction(au.gov.asd.tac.constellation.graph.GraphWriteMethods, int)}.
     *
     * @param graph the {@link GraphWriteMethods} holding the vertex.
     * @param transactionId the id of the transaction that has just been
     * created.
     */
    public void newTransaction(final GraphWriteMethods graph, final int transactionId) {
    }

    /**
     * Called by the framework after a transaction has been created and
     * initialized by another process. This gives the schema a chance to further
     * modify the attributes of the transaction to provide domain specify values
     * automatically. A common use case would be the setting of an
     * linestyle/color based on a type attribute.
     *
     * @param graph the {@link GraphWriteMethods} holding the vertex.
     * @param transactionId the id of the transaction that has been added.
     */
    public void completeTransaction(final GraphWriteMethods graph, final int transactionId) {
    }

    /**
     * Given a prospective type name as a {@link String}, infer the matching
     * {@link SchemaTransactionType}.
     *
     * @param type A {@link String} representing the name of a
     * {@link SchemaTransactionType}.
     * @return The resolved {@link SchemaTransactionType}.
     */
    public SchemaTransactionType resolveTransactionType(final String type) {
        return null;
    }

    /**
     * Get the vertex attribute which should be used to represent a vertex
     * textually to the user. This is often set to a primary key since they are
     * assured to be unique for each vertex.
     *
     * @param graph the {@link GraphWriteMethods} from which to retrieve the
     * vertex alias attribute.
     * @return the int representing the vertex alias attribute.
     */
    public int getVertexAliasAttribute(final GraphReadMethods graph) {
        return Graph.NOT_FOUND;
    }
}
