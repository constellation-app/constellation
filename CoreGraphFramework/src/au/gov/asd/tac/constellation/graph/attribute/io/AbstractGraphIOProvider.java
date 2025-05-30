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
package au.gov.asd.tac.constellation.graph.attribute.io;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.utilities.datastructure.ImmutableObjectCache;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.openide.util.Lookup;

/**
 * A template for providing the read/write capability for graph attributes.
 * <p>
 * Graphs are serialised to a JSON Object. Each vertex, node, and the graph
 * itself has a {@link JsonNode} corresponding to it. Graph attributes need to
 * read and write their values for these elements as child-nodes of the nodes
 * corresponding to the elements.
 * <p>
 * Implementations of this class correspond to a type of attribute, ie. there
 * should be a one-to-one correspondence between
 * {@link au.gov.asd.tac.constellation.graph.attribute.AttributeDescription} and
 * AbstractGraphIOProvider.
 * <p>
 * Note that all necessary graph locking when reading and writing is handled by
 * the framework that utilises these providers.
 *
 * @author algol
 */
public abstract class AbstractGraphIOProvider {

    public static final int DEFAULT_VERSION = -1;

    private static Collection<? extends AbstractGraphIOProvider> registeredProviders = null;

    /**
     * Get a string representing the type of data that this provider handles.
     * <p>
     * For providers corresponding to graph attributes, this should be the same
     * as
     * {@link au.gov.asd.tac.constellation.graph.attribute.AttributeDescription#getName()}
     * for the corresponding description class.
     *
     * @return A unique name indicating the type of data handled by this
     * provider.
     */
    public abstract String getName();

    /**
     * Returns all handlers that registered as graph I/O provider
     *
     * @return All handlers that registered as graph I/O provider
     */
    public static Collection<? extends AbstractGraphIOProvider> getProviders() {
        if (registeredProviders == null) {
            registeredProviders = Lookup.getDefault().lookupAll(AbstractGraphIOProvider.class);
        }
        return registeredProviders;
    }

    /**
     * Deserialise an object from a JsonNode.
     * <p>
     * When the graph is read from a JSON document, the vertex and transaction
     * JSON ids have the same values as the graph they were saved from. Because
     * of the way that Graph works, these JSON ids may not have the same values
     * as the vertices and transactions that are added in the new graph. The
     * vertexMap parameter provides a mapping from a JSON id to a graph id.
     * Implementations that need to have references to graph elements can use
     * <tt>vertexMap.get(jsonId)</tt> to get the graph id.
     * <p>
     * The transaction map does the same thing. However, for a long time the
     * transaction id wasn't recorded in the file, so graphs that were saved
     * before this capability was added will have an empty transaction map.
     *
     * @param attributeId The id of the attribute being read.
     * @param elementId The id of the element being read.
     * @param jnode The JsonNode to read from.
     * @param writableGraph The graph that the resulting object will be placed
     * in. Provided in case the object requires some graph data.
     * @param vertexMap A mapping from a vertex id in the file to the vertex id
     * in th graph.
     * @param transactionMap A mapping from a transaction id in the file to the
     * transaction id in the graph.
     * @param byteReader The byte reader containing ancillary data (e.g. images)
     * that doesn't easily fit into a JSON document.
     * @param cache a cache that can be used to dedup identical instances of the
     * same immutable objects.
     *
     * @throws java.io.IOException If there's a problem reading the document.
     */
    public abstract void readObject(final int attributeId, final int elementId, final JsonNode jnode,
            final GraphWriteMethods writableGraph, final Map<Integer, Integer> vertexMap, final Map<Integer, Integer> transactionMap,
            final GraphByteReader byteReader, final ImmutableObjectCache cache) throws IOException;

    /**
     * Write this object to the JSON generator.
     * <p>
     * When the graph is saved to a JSON document, the graph vertex and
     * transaction ids are used as id values in the JSON nodes. This means that
     * implementations that need to save references to graph elements can just
     * use the ids as they are.
     * <p>
     * If verbose is true, the value is always written to JSON. Otherwise, a
     * value is only written if it is not the same as the default value of the
     * attribute. This can save bytes when writing, and save time when reading
     * (since the graph does not have to update default values).
     *
     * @param attribute The attribute being written.
     * @param elementId The id of the element being written.
     * @param jsonGenerator The JsonGenerator used to write to the JSON
     * document.
     * @param readableGraph The graph that the object belongs to. Provided in
     * case the object requires some graph data.
     * @param byteWriter For ancillary data (e.g. images) that doesn't easily
     * fit into a JSON document.
     * @param verbose Determines whether to write default values of attributes
     * or not.
     *
     * @throws IOException If there's a problem writing.
     */
    public abstract void writeObject(final Attribute attribute, final int elementId, final JsonGenerator jsonGenerator,
            final GraphReadMethods readableGraph, final GraphByteWriter byteWriter, final boolean verbose) throws IOException;

    /**
     * Get the version of this IOProvider
     *
     * @return The version as an integer
     */
    public int getVersion() {
        return DEFAULT_VERSION;
    }
}
