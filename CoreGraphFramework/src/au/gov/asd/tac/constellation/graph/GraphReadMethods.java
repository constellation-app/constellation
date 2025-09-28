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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;
import java.util.stream.IntStream;

/**
 * The GraphReadMethods provides an API that allows read-only access to the
 * graph. {@link GraphWriteMethods} extends GraphReadMethods and provides
 * methods that allow modification of the graph.
 *
 * <h3>Graph ID</h3>
 * Each graph is allocated an ID when it is created. The default behavior is to
 * assign each graph an ID that is unique with in the application so that it can
 * be used to identify the graph through its lifetime. However, there are
 * methods to copy a graph which preserves its ID and therefore creates the
 * situation where multiple graphs share a single ID. It is advised that these
 * graphs be used exclusively for private purposes and not exposed globally to
 * the application to preserve the uniqueness of the IDs.
 *
 * <h3>Graph Schema</h3>
 * In its basic form, the graph is a very generic structure providing attribute
 * graph elements with no context or functionality specific to any domain. While
 * this provides great flexibility, it makes working in specific domains harder
 * and more manual. Each graph has an associated schema that has the
 * responsibility of adding a layer of domain specific context over the top of
 * the generic graph structure. It is provided with an opportunity at various
 * stages in the graph's life cycle, such as when the graph is created, or when
 * graph elements are added or removed, to make domain specific changes to the
 * graph. Every graph must have an assigned schema at all times. However, the
 * default schema does nothing and is effectively equivalent to having no
 * schema.
 *
 * <h3>Graph Elements</h3>
 * A graph is comprised of sets of 5 different elements:
 * <ol>
 * <li>Vertices: vertices (or nodes) of the graph are connected together by
 * transactions. Where this occurs, the two vertices are considered to be
 * neighbors.</li>
 * <li>Transactions: transactions connect two vertices and can be either
 * directed or undirected. There can be many transactions between a single pair
 * of vertices.</li>
 * <li>Edges: an edge also connects a pair of vertices and represents all
 * transactions between those two vertices oriented in a single direction (or
 * undirected). Edges are not created explicity but are created and removed
 * automatically by the graph as transactions are created and deleted. By
 * definition, there can be at most 3 edges between any pair of vertices, one
 * representing transactions in the forward direction, one representing
 * transactions in the reverse direction, and one representing undirected
 * transactions.</li>
 * <li>Links: a link also connects a pair of vertices and represents all
 * transactions between those two vertices, regardless of direction. Links are
 * also not created explicitly but are created and deleted as transactions are
 * added and removed from the graph. By definition, there can be at most one
 * link between any pair of vertices. This link will exist only if there is at
 * least one transaction between the pair of vertices.</li>
 * <li>Attributes: attributes are associated with one of the other elements
 * types and allow values to be stored against each element of that type. Each
 * attribute has a type specifying the type of data that can be stored.</li>
 * </ol>
 * The graph contains methods that allow traversal of their relationships
 * between elements in the graph, for example:
 * <ol>
 * <li>Find all transactions adjacent to a vertex and find the
 * source/destination vertex for a transaction</li>
 * <li>Find all edges represented by a link and find the link representing an
 * edge.</li>
 * <li>Find all attributes associated with a vertex and find the element type
 * that an attribute is associated with.</li>
 * </ol>
 *
 * <h3>Element IDs</h3>
 * Each element is referenced by a unique integer ID that is allocated to the
 * element on creation and guaranteed not to change while the element exists. At
 * any point in time, the graph will have an capacity for a given element type
 * specifying the number of those elements the graph can hold. The IDs of those
 * elements will be allocated from the range of 0 (inclusive) to capacity
 * (exclusive). The capacity for a given element type is automatically expanded
 * as new elements are created. It is important to note that the ID of a deleted
 * element my be allocated to a new element when it is created.
 *
 * <h3>Element Positions</h3>
 * Each element also has a position number specifying the position that element
 * holds in the list of all elements of that type. While no elements of that
 * type are added or deleted, this position number will remain constant.
 * However, if a new element is added or removed, the graph is free to rearrange
 * those elements as needed by the implementation. If the graph holds N elements
 * of a given type, it is guaranteed that each element will hold a unique
 * position number between 0 (inclusive) and N (exclusive). This means that it
 * is possible to iterate through all elements of a given type by looking up the
 * element holding each positions between 0 and the count of those elements,
 * assuming that no elements are added or removed during the iterations.
 *
 * <h3>Element UIDs</h3>
 * Each element also has a UID which, unlike its ID, is guaranteed never to be
 * used for another element of that type for the life of the graph. This can be
 * useful to detect when an element has been deleted and another element created
 * using the same ID.
 *
 * <h3>Modification Counters</h3>
 * Changes on the graph are recorded through a series of modification counters:
 * <ol>
 * <li>Global Modification Counter: this counter is incremented each time any
 * change is made to the graph. If this modification counter has not changed
 * then it can be guaranteed that the graph has not changed in any way.</li>
 * <li>Structure Modification Counter: this counter is incremented each time a
 * vertex or transaction is added or removed from the graph. </li>
 * <li>Attribute Modification Counter: this counter is incremented each time an
 * attribute is added or removed from the graph.</li>
 * <li>Attribute Value Modification Counters: there is one attribute
 * modification counter for each attribute on the graph. These modification
 * counters get incremented each time a change is made to the value of that
 * attribute for any element.</li>
 * </ol>
 * When undo operations occur, the counters are decremented to be identical to
 * the values that existed before the original operation occurred. Therefore, if
 * attempting to detect changes in the graph, it is important that a test for
 * inequality is performed, rather than testing for a modification counter that
 * is greater than previously seen.
 *
 * <h3>Undo/Redo</h3>
 * The graph automatically records all operations performed on it to allow for
 * undo/redo functionality. In some cases, the default storage of the undo/redo
 * information is not memory efficient so the API allows for the execution of a
 * GraphOperation that is responsible for both the initial modification, as well
 * as any subsequent undo/redo operations. This should be treated as an advanced
 * functionality with much risk involved and should be only considered in cases
 * where a substantial memory saving can be achieved. It is critical that the
 * GraphOperation return the graph to the exact previous state when an undo (or
 * redo) operation is requested to prevent the graph becoming corrupted.
 *
 * <h3>Primary Keys</h3>
 * Each element type, particularly vertices and transactions, can have one or
 * more of their attributes specified as their primary key attributes. If no
 * primary key is specified for a given element type then attributes of that
 * type can be modified as required with the graph taking no action. If a
 * primary key is specified then the graph ensures that no to elements of that
 * type exist at the same time with the same combination of primary key
 * attribute values. It does this by keeping a list of elements that have had
 * primary key attribute changes since the last graph commit. Before a commit
 * can occur, each of these elements is validated to make sure its primary key
 * attribute values do not clash with other elements of its type. If a clash is
 * discovered, the graph's merger is given an opportunity to resolve the clash
 * by merging the two elements. If this is not possible, a DuplicateKeyException
 * is thrown and any uncommitted changes are rolled back. Only performing this
 * validation step on commit allows elements to temporarily share key attribute
 * values during the process of an edit, as long as those clashes are resolved
 * before the commit. There are also methods to manually enforce a primary key
 * validation if required.
 *
 * <h3>Immutable Attribute Values</h3>
 * It is important that all attribute values are considered immutable. This
 * happens by default when the underlying native attribute value is either a
 * primitive or a String. However, when the value is an Object, it is important
 * that values sourced from the graph are not simply edited and then set again
 * on the graph.
 * <p>
 * This is incorrect and will cause the graph to become corrupt:
 * <pre><code>
 *      Object value = graph.getObjectValue(attributeId, elementId);
 *      value.setSomething(...);
 *      graph.setObjectValue(attributeId, elementId, value);
 * </code></pre>
 *
 * This is correct:
 * <pre><code>
 *      Object value = graph.getObjectValue(attributeId, elementId);
 *      Object newValue = new Value(value);
 *      newValue.setSomething(...);
 *      graph.setObjectValue(attributeId, elementId, newValue);
 * </code></pre>
 *
 * @see GraphWriteMethods
 *
 * @author sirius
 */
public interface GraphReadMethods extends GraphConstants {

    /**
     * Returns a unique identifier for this graph. The default behavior is that
     * this ID is unique for all graphs in the application and can therefore be
     * used to uniquely identify any graph. However, it is possible to make
     * copies of a graph that have the same ID as the original meaning that this
     * uniqueness may not always hold. There is a convention that graphs with
     * duplicate IDs are used only for private uses and not exposed globally in
     * the application.
     *
     * @return a unique identifier for this graph.
     */
    public String getId();

    /**
     * Returns the schema for the graph.
     *
     * @return the schema.
     */
    public Schema getSchema();

    public boolean isRecordingEdit();

    /**
     * Returns the global modification counter. This counter is incremented
     * every time the graph changes in any way. When a change is undone, this
     * counter is reverted to the value it had before the change was performed.
     * This means that it is important to test for inequality when attempting to
     * detect changes to the graph, rather than testing for increases in this
     * counter, which would miss changes that result from an undo operation.
     *
     * @return the global modification counter.
     */
    long getGlobalModificationCounter();

    /**
     * Returns the global attribute modification counter. This counter is
     * increased every time an attribute is added or removed. When the addition
     * or removal of an attribute is undone, this counter is reverted to the
     * value it had before the element was added. This means that it is
     * important to test for inequality when attempting to detect changes
     * attribute changes in the graph, rather than testing for increases in this
     * counter, which would miss changes that result from an undo operation.
     *
     * @return the global attribute modification counter.
     */
    long getAttributeModificationCounter();

    /**
     * Returns the global structure modification counter. This counter is
     * increased every time a vertex or transaction is added or removed. When
     * the addition or removal of a vertex or transaction is undone, this
     * counter is reverted to the value it had before the element was added.
     * This means that it is important to test for inequality when attempting to
     * detect changes in the structure of the graph, rather than testing for
     * increases in this counter, which would miss changes that result from an
     * undo operation.
     *
     * @return the global structure modification counter.
     */
    long getStructureModificationCounter();

    /**
     * Returns the modification counter for the specified attribute. This
     * counter is increased every time the value of the attribute is set for any
     * element. When the value change is undone, this counter is reverted to the
     * value it had before the change. This means that it is important to test
     * for inequality when attempting to detect attribute value changes in the
     * graph, rather than testing for increases in this counter, which would
     * miss changes that result from an undo operation.
     *
     * @param attribute the attribute.
     * @return the modification counter for the specified attribute.
     */
    long getValueModificationCounter(final int attribute);

    /**
     * Creates a complete deep copy of the graph that has the same id as this
     * graph. The copy is represented by a GraphReadMethods object meaning that
     * is cannot be modified. Being a deep copy, the returned object is
     * completely independent of this graph meaning that future changes to this
     * graph will not propagate to the copy.
     *
     * @return a complete copy of the graph.
     */
    public GraphReadMethods copy();

    /**
     * Creates a complete deep copy of the graph with a new specified id. The
     * copy is represented by a GraphReadMethods object meaning that is cannot
     * be modified. Being a deep copy, the returned object is completely
     * independent of this graph meaning that future changes to this graph will
     * not propagate to the copy.
     *
     * @param id the id of the copy.
     *
     * @return a complete copy of the graph.
     */
    public GraphReadMethods copy(final String id);

    /**
     * Returns the number of edges that this graph can hold at this time. In
     * general, the edge capacity of the graph will increase as new edges are
     * created in the graph. Importantly, the ids of the edges in the graph will
     * always be in the range: 0 &lt;= id &lt; edge capacity.
     *
     * @return the edge capacity of the graph.
     */
    int getEdgeCapacity();

    /**
     * Returns a count of the number of edges in the graph. This will always be
     * less than or equal to the edge capacity. The position numbers of each
     * edge in the graph are unique and fill the range of 0 &lt;= position &lt;
     * edge count.
     *
     * <br> <br>This can be used to iterate through all edges in the graph:
     * <br>
     * <pre><code>
     *     final int edgeCount = graph.getEdgeCount();
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getEdge(position);
     *         ....
     *     }
     * </code></pre>
     *
     * @return a count of the number of edges in the graph.
     */
    int getEdgeCount();

    /**
     * Returns the ID of the edge at the specified position in the graph.
     *
     * <br>
     * <br>This can be used to iterate through all edges in the graph:
     * <br>
     * <pre><code>
     *     final int edgeCount = graph.getEdgeCount();
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getEdge(position);
     *         ....
     *     }
     * </code></pre>
     *
     * @param position the position of the edge that will be returned.
     *
     * @return the edge at the specified position in the graph.
     */
    int getEdge(final int position);

    /**
     * Returns the position of the specified edge in the graph.
     *
     * @param edge the id of the edge.
     * @return the position of the specified edge in the graph.
     */
    int getEdgePosition(final int edge);

    /**
     * Returns a UID for this edge that will be unique in this graph for all
     * edges with this edge id. This allows you to distinguish between edges
     * that happen to use the same edge id due to edges being added and removed.
     *
     * @param edge the ID of the edge.
     *
     * @return the UID of the edge.
     */
    long getEdgeUID(final int edge);

    /**
     * Returns the direction of the specified edge. This will always be either
     * UPHILL, DOWNHILL or FLAT.
     * <p>
     * In the case of edges, UPHILL or DOWNHILL (apart from indicating that this
     * is a directed edge) means that the source vertex id is less than or
     * greater than the destination vertex id respectively. Therefore, an edge
     * is directed if getTransactionDirection()!=Graph.FLAT.
     * <p>
     * (If the edge is a loop, then it could be UPHILL or DOWNHILL, it doesn't
     * matter.)
     *
     * @param edge the id of the edge.
     * @return the direction of the specified edge.
     */
    int getEdgeDirection(final int edge);

    /**
     * Returns the vertex from which the specified edge originates. For
     * undirected edges, the vertex with the lower id will always be returned.
     *
     * @param edge the ID of the edge.
     * @return the ID of the vertex from which the specified edge originates.
     */
    int getEdgeSourceVertex(final int edge);

    /**
     * Returns the vertex at which the specified edge terminates. For undirected
     * edges, the vertex with the higher id will always be returned.
     *
     * @param edge the ID of the edge.
     * @return the ID of the vertex at which the specified edge terminates.
     */
    int getEdgeDestinationVertex(final int edge);

    /**
     * Returns the ID of the link that represents the specified edge.
     *
     * @param edge the ID of the edge.
     * @return the ID of the link that holds the specified edge.
     */
    int getEdgeLink(final int edge);

    /**
     * Returns the number of transactions that are represented by the specified
     * edge.
     *
     * <br>
     * <br>This can be used to iterate though all transactions represented by a
     * single edge:
     * <br>
     * <pre><code>
     *     final int edgeId = ....
     *     final int transactionCount = graph.getEdgeTransactionCount(edgeId);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getEdgeTransaction(edgeId, position);
     *         ....
     *     }
     * </code></pre>
     *
     * @param edgeId the id of the edge.
     * @return the number of transactions that are represented by the specified
     * edge.
     */
    int getEdgeTransactionCount(final int edgeId);

    /**
     * Returns the transaction represented by the specified edge that occupies
     * the specified position in the list of transactions represented by the
     * edge.
     *
     * <br>
     * <br>This can be used to iterate though all transactions represented by a
     * single edge:
     * <br>
     * <pre><code>
     *     final int edgeId = ....
     *     final int transactionCount = graph.getEdgeTransactionCount(edgeId);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getEdgeTransaction(edgeId, position);
     *         ....
     *     }
     * </code></pre>
     *
     * @param edgeId the id of the edge.
     * @param position the position of the transaction.
     * @return the transaction represented by the specified edge that occupies
     * the specified position in the list of transactions represented by the
     * edge.
     */
    int getEdgeTransaction(final int edgeId, final int position);

    /**
     * Tests if the specified edge id is currently in use to reference an edge
     * in the graph. If this returns true then other graph methods that require
     * an edge id should not fail.
     *
     * @param edgeId the id of the edge.
     * @return true if the specified edge id is currently in use.
     */
    boolean edgeExists(final int edgeId);

    /**
     * Returns the id of the link in the graph that occupies the specified
     * position in the global list of graph links.
     *
     * <br>
     * <br>This can be used to iterate though all links in the graph:
     * <br>
     * <pre><code>
     *     final int linkCount = graph.getLinkCount();
     *     for (int position = 0; position &lt; linkCount; position++) {
     *         final int linkId = graph.getLink(position);
     *         ....
     *     }
     * </code></pre>
     *
     * @param position the position of the link in the graph.
     * @return the id of the link in the graph that occupies the specified
     * position in the graph.
     */
    int getLink(final int position);

    /**
     * Returns the position of the specified link in the global list of links in
     * the graph. This position is guaranteed to remain unchanged unless a link
     * is added or removed from the graph. If this happens, the graph is free to
     * re-order the links as necessary meaning that the position of any link in
     * the graph may change. If an unchanging reference to a link is required
     * then the id should be used.
     *
     * @param link the id of the link.
     * @return the position of the specified link in the global list of links in
     * the graph.
     */
    int getLinkPosition(final int link);

    /**
     * Returns a UID for this link that will be unique in this graph for all
     * links ever existing in this graph. This allows you to distinguish between
     * links that happen to use the same link id due to links being added and
     * removed.
     *
     * @param link the link id.
     * @return the link UID.
     */
    long getLinkUID(final int link);

    /**
     * Returns the number of links that the graph is capable of holding at this
     * time. In general, the link capacity will be automatically expanded as new
     * links are added to the graph. Importantly, all link ids will be
     * non-negative and less than the link capacity of the graph. The link
     * capacity will never be less than
     *
     * @return the number of links that the graph is capable of holding at this
     * time.
     */
    int getLinkCapacity();

    /**
     * Returns a count of the number of links currently in the graph. This will
     * never be greater than the link capacity.
     *
     * <br>
     * <br>This can be used to iterate though all links in the graph:
     * <br>
     * <pre><code>
     *     final int linkCount = graph.getLinkCount();
     *     for (int position = 0; position &lt; linkCount; position++) {
     *         final int linkId = graph.getLink(position);
     *         ....
     *     }
     * </code></pre>
     *
     * @return a count of the number of links in the graph.
     */
    int getLinkCount();

    /**
     * Returns the id of the vertex that is attached to this link with the lower
     * id. Because links do not have direction, it does not make sense to refer
     * to a source or destination vertex. Therefore, the vertices connected by a
     * link are referred to as the low and high vertex, depending on their ids.
     * When a link represents a loop, the low and high vertex are the same
     * vertex.
     *
     * @param link the id of the link.
     * @return the id of the vertex that is attached to this link with the lower
     * id.
     */
    int getLinkLowVertex(final int link);

    /**
     * Returns the id of the vertex that is attached to this link with the
     * higher id. Because links do not have direction, it does not make sense to
     * refer to a source or destination vertex. Therefore, the vertices
     * connected by a link are referred to as the low and high vertex, depending
     * on their ids. When a link represents a loop, the low and high vertex are
     * the same vertex.
     *
     * @param link the id of the link.
     * @return the id of the vertex that is attached to this link with the
     * higher id.
     */
    int getLinkHighVertex(final int link);

    /**
     * Returns a count of the number of edges represented by the specified link.
     * Because all transactions in the same direction between a pair of vertices
     * are represented by a single link, there can never be more than 3 edges
     * represented by a single link: UPHILL, DOWNHILL and UNDIRECTED. In
     * addition, the link would not exist unless at least 1 transaction exists
     * between the pair of vertices meaning that the link edge count will never
     * be zero.
     *
     * <br>
     * <br>This can be used to iterate though all edges represented by a link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int edgeCount = graph.getLinkEdgeCount(linkId);
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getLinkEdge(position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @return a count of the number of edges represented by the specified link.
     */
    int getLinkEdgeCount(final int link);

    /**
     * Returns the edge that occupies the specified position in the list of
     * edges represented by this link.
     *
     * <br>
     * <br>This can be used to iterate though all edges represented by a link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int edgeCount = graph.getLinkEdgeCount(linkId);
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getLinkEdge(position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @param position the position of the edge.
     * @return the edge that occupies the specified position in the list of
     * edges represented by this link.
     */
    int getLinkEdge(final int link, final int position);

    /**
     * Returns a count of the number of edges attached to the specified edge in
     * the specified direction. In reality, there can only ever be 1 edge under
     * a specified link in any given direction meaning that this method can only
     * ever return 0 or 1. The method is included in this form for consistency
     * with other methods that give element counts.
     *
     * <br>
     * <br>This can be used to iterate though all edges in a specified direction
     * represented by a link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int direction = ...
     *     final int edgeCount = graph.getLinkEdgeCount(linkId, direction);
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getLinkEdge(linkId, direction, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @param direction the direction of the edge.
     * @return a count of the number of edges attached to the specified edge.
     */
    int getLinkEdgeCount(final int link, final int direction);

    /**
     * Returns the edge attached to the specified link that occupies the
     * specified position in the list of edges of a particular direction. In
     * reality, there can only every be 1 edge of a particular direction under a
     * given link meaning that the only valid position is 0. The method is
     * included in this form to provide consistency with other methods that
     * return elements based on their positions.
     *
     * <br>
     * <br>This can be used to iterate though all edges in a specified direction
     * represented by a link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int direction = ...
     *     final int edgeCount = graph.getLinkEdgeCount(linkId, direction);
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getLinkEdge(linkId, direction, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @param direction the direction of the edge.
     * @param position the position of the edge.
     * @return the edge attached to the specified link that occupies the
     * specified position in the list of edges of a particular direction.
     */
    int getLinkEdge(final int link, final int direction, final int position);

    /**
     * Returns the transaction that occupies the specified position in the list
     * of transactions represented by the specified link.
     *
     * <br>
     * <br>This can be used to iterate though all transactions represented by a
     * link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int transactionCount = graph.getLinkTransactionCount(linkId);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getLinkTransaction(linkId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @param position the position of the transaction.
     * @return the transaction that occupies the specified position in the list
     * of transactions represented by the specified link.
     */
    int getLinkTransaction(final int link, final int position);

    /**
     * Returns the transaction that occupies the specified position in the list
     * of transactions of the specified direction attached to the specified
     * link. The valid directions are UPHILL, DOWNHILL and FLAT.
     *
     * <br>
     * <br>This can be used to iterate though all transactions in a specified
     * direction represented by a link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int direction = ...
     *     final int transactionCount = graph.getLinkTransactionCount(linkId, direction);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getLinkTransaction(linkId, direction, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @param direction the direction of the transaction.
     * @param position the position of the transaction.
     * @return the transaction that occupies the specified position in the list
     * of transactions of the specified direction attached to the specified
     * link.
     */
    int getLinkTransaction(final int link, final int direction, final int position);

    /**
     * Returns a count of the number of transactions attached to the specified
     * link.
     *
     * <br>
     * <br>This can be used to iterate though all transactions represented by a
     * link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int transactionCount = graph.getLinkTransactionCount(linkId);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getLinkTransaction(linkId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @return a count of the number of transactions attached to the specified
     * link.
     */
    int getLinkTransactionCount(final int link);

    /**
     * Returns a count of the number of transactions of the specified direction
     * that are represented by the specified link.
     *
     * <br>
     * <br>This can be used to iterate though all transactions in a specified
     * direction represented by a link:
     * <br>
     * <pre><code>
     *     final int linkId = ...
     *     final int direction = ...
     *     final int transactionCount = graph.getLinkTransactionCount(linkId, direction);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getLinkTransaction(linkId, direction, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param link the id of the link.
     * @param direction the direction of the transaction.
     * @return a count of the number of transactions of the specified direction
     * that are represented by the specified link.
     */
    int getLinkTransactionCount(final int link, final int direction);

    /**
     * Returns the transaction that occupies the specified position in the
     * global list of transactions in the graph.
     *
     * <br>
     * <br>This can be used to iterate though all transactions in the graph:
     * <br>
     * <pre><code>
     *     final int transactionCount = graph.getTransactionCount();
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getTransaction(position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param position the position of the transaction.
     * @return the transaction that occupies the specified position in the
     * global list of transactions in the graph.
     */
    int getTransaction(final int position);

    /**
     * Returns the position of the specified transaction in the global list of
     * transactions in the graph. This position is guaranteed to remain
     * unchanged unless a transaction is added or removed from the graph. If
     * this happens, the graph is free to re-order the transactions as necessary
     * meaning that the position of any transaction in the graph may change. If
     * an unchanging reference to a transaction is required then the id should
     * be used.
     *
     * @param transaction the id of the transaction.
     * @return the position of the specified transaction in the global list of
     * transactions in the graph.
     */
    int getTransactionPosition(final int transaction);

    /**
     * Returns a UID for this transaction that will be unique in this graph for
     * all transactions ever created in the graph. This allows you to
     * distinguish between transactions that happen to use the same transaction
     * id due to transactions being added and removed.
     *
     * @param transaction the transaction id.
     * @return the UID for this transaction.
     */
    long getTransactionUID(final int transaction);

    /**
     * Returns the number of transactions that the graph is capable of holding.
     * In general, the transaction capacity will be increased automatically as
     * more transactions are added to the graph. Importantly, all transaction
     * ids will be non-negative and less than the transaction capacity.
     *
     * @return the number of transactions that the graph is capable of holding.
     */
    int getTransactionCapacity();

    /**
     * Returns a count of the number of transactions in the graph. This will
     * never be greater than the transaction capacity.
     *
     * <br>
     * <br>This can be used to iterate though all transactions in the graph:
     * <br>
     * <pre><code>
     *     final int transactionCount = graph.getTransactionCount();
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getTransaction(position);
     *         ...
     *     }
     * </code></pre>
     *
     * @return a count of the number of transactions in the graph.
     */
    int getTransactionCount();

    /**
     * Returns the direction of the specified transaction. This will always be
     * either UPHILL, DOWNHILL or FLAT.
     * <p>
     * In the case of transactions, UPHILL or DOWNHILL (apart from indicating
     * that this is a directed transaction) means that the source vertex id is
     * less than or greater than the destination vertex id respectively.
     * Therefore, a transaction is directed if
     * getTransactionDirection()!=Graph.FLAT.
     * <p>
     * (If the transaction is a loop, then it could be UPHILL or DOWNHILL, it
     * doesn't matter.)
     *
     * @param transaction the id of the transaction.
     * @return the direction of the specified transaction.
     */
    int getTransactionDirection(final int transaction);

    /**
     * Returns the id of the link that holds the specified transaction.
     *
     * @param transaction the id of the transaction.
     * @return the id of the link that holds the specified transaction.
     */
    int getTransactionLink(final int transaction);

    /**
     * Returns the id of the edge that holds the specified transaction.
     *
     * @param transaction the id of the transaction.
     * @return the id of the edge that holds the specified transaction.
     */
    int getTransactionEdge(final int transaction);

    /**
     * Returns the id of the vertex that occupies the specified position in the
     * global list of graph vertices. This position is only guaranteed to remain
     * unchanged while no vertices are added or removed. When this happens, the
     * implementation is free to reorder the vertices as required. This is in
     * contrast to the vertex id which is guaranteed to remain constant for the
     * life of the vertex.
     *
     * <p>
     * This can be used to iterate through all vertices in the graph:
     *
     * <pre><code>
     *     final int vertexCount = graph.getVertexCount();
     *     for (int position = 0; position &lt; vertexCount; position++) {
     *         final int vertexId = graph.getVertex(position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param position the position of the vertex.
     * @return the id of the vertex that occupies the specified position in the
     * global list of graph vertices.
     */
    int getVertex(final int position);

    /**
     * Returns the number of vertices that the graph is capable of holding. In
     * general, the vertex capacity will increase automatically as more vertices
     * are added to the graph. Importantly, the ids of vertices in the graph
     * will always be non-negative and less than the vertex capacity.
     *
     * @return the number of vertices that the graph is capable of holding.
     */
    int getVertexCapacity();

    /**
     * Returns a count of the number of vertices in the graph.
     *
     * <p>
     * This can be used to iterate through all vertices in the graph:
     *
     * <pre><code>
     *     final int vertexCount = graph.getVertexCount();
     *     for (int position = 0; position &lt; vertexCount; position++) {
     *         final int vertexId = graph.getVertex(position);
     *         ...
     *     }
     * </code></pre>
     *
     * @return a count of the number of vertices in the graph.
     */
    int getVertexCount();

    /**
     * Returns the id of the link that occupies the specified position in the
     * list of links attached to the specified vertex.
     *
     * <p>
     * This can be used to iterate through all links adjacent to a vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int linkCount = graph.getVertexLinkCount(vertexId);
     *     for (int position = 0; position &lt; linkCount; position++) {
     *         final int linkId = graph.getVertexLink(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the id of the vertex.
     * @param position the position of the link.
     * @return the id of the link that occupies the specified position in the
     * list of links attached to the specified vertex.
     */
    int getVertexLink(final int vertex, final int position);

    /**
     * Returns a count of the number of links adjacent to the specified vertex.
     *
     * <p>
     * This can be used to iterate through all links adjacent to a vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int linkCount = graph.getVertexLinkCount(vertexId);
     *     for (int position = 0; position &lt; linkCount; position++) {
     *         final int linkId = graph.getVertexLink(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the id of the vertex.
     * @return a count of the number of links adjacent to the specified vertex.
     */
    int getVertexLinkCount(final int vertex);

    /**
     * Returns a count of the number of neighbors of this vertex. Each neighbor
     * is attached to the vertex by a link meaning that this method will always
     * return the same value as the link count.
     *
     * <p>
     * This can be used to iterate through all neighbours of a vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
     *     for (int position = 0; position &lt; neighbourCount; position++) {
     *         final int neighbourId = graph.getVertexNeighbour(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the id of the vertex.
     * @return a count of the number of neighbors of this vertex.
     */
    int getVertexNeighbourCount(final int vertex);

    /**
     * Returns a count of the number of edges attached to the specified vertex.
     *
     * <p>
     * This can be used to iterate through all edges adjacent to a vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int edgeCount = graph.getVertexEdgeCount(vertexId);
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getVertexEdge(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the id of the vertex.
     * @return a count of the number of edges adjacent to the specified vertex.
     */
    int getVertexEdgeCount(final int vertex);

    /**
     * Returns the edge that occupies the specified position in the list of
     * edges adjacent to the specified vertex.
     *
     * <p>
     * This can be used to iterate through all edges adjacent to a vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int edgeCount = graph.getVertexEdgeCount(vertexId);
     *     for (int position = 0; position &lt; edgeCount; position++) {
     *         final int edgeId = graph.getVertexEdge(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the id of the vertex.
     * @param position the position of the edge.
     * @return the edge that occupies the specified position in the list of
     * edges adjacent to the specified vertex.
     */
    int getVertexEdge(final int vertex, final int position);

    /**
     * Returns a count of the number of edges of the specified direction that
     * are attached to the specified vertex. The valid directions are OUTGOING,
     * INCOMING and UNDIRECTED.
     *
     * @param vertex the id of the vertex.
     * @param direction the direction of the edge.
     * @return a count of the number of edges of the specified direction that
     * are attached to the specified vertex.
     */
    int getVertexEdgeCount(final int vertex, final int direction);

    /**
     * Returns the edge that occupies the specified position in the list of
     * edges of the specified direction that are attached to the specified
     * vertex. The valid directions are OUTGOING, INCOMING and UNDIRECTED.
     *
     * @param vertex the id of the vertex.
     * @param direction the direction of the edge.
     * @param position the position of the edge.
     * @return the edge that occupies the specified position in the list of
     * edges of the specified direction that are attached to the specified
     * vertex.
     */
    int getVertexEdge(final int vertex, final int direction, final int position);

    /**
     * Returns the vertex id of the neighbouring vertex that occupies the
     * specified position in the list of all vertices that are neighbours of the
     * specified vertex.
     * <p>
     * This can be used to iterate through all neighbours of a vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int neighbourCount = graph.getVertexNeighbourCount(vertexId);
     *     for (int position = 0; position &lt; neighbourCount; position++) {
     *         final int neighbourId = graph.getVertexNeighbour(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the id of the vertex.
     * @param position the position in the list of neighbours.
     * @return the vertex id of the neighbouring vertex that occupies the
     * specified position in the list of all vertices that are neighbours of the
     * specified vertex.
     */
    int getVertexNeighbour(final int vertex, final int position);

    /**
     * Returns the id of the transaction that occupies the specified position in
     * the list of transactions adjacent to the specified vertex (in any
     * direction)
     *
     * This can be used to iterate through all transactions adjacent to a
     * specified vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int transactionCount = graph.getVertexTransactionCount(vertexId);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getVertexTransaction(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the vertex adjacent to the transactions.
     * @param position the position the transaction occupies in the list of
     * transactions.
     * @return the id of the transaction that occupies the specified position in
     * the list of transactions adjacent to the specified vertex (in any
     * direction)
     */
    int getVertexTransaction(final int vertex, final int position);

    /**
     * Returns the id of the transaction at occupies the specified position in
     * the list of transactions adjacent to the specified vertex in the
     * specified direction.
     *
     * The accepted directions are Graph.OUTGOING, Graph.INCOMING and
     * Graph.UNDIRECTED.
     *
     * @param vertex the vertex adjacent to the transactions.
     * @param direction the direction of the transactions relative to the
     * vertex.
     * @param position the position of the transaction in the list of
     * transactions.
     * @return the id of the transaction at occupies the specified position in
     * the list of transactions adjacent to the specified vertex in the
     * specified direction.
     */
    int getVertexTransaction(final int vertex, final int direction, final int position);

    /**
     * Return the count of all transactions adjacent to the specified vertex.
     *
     * This can be used to iterate through all transactions adjacent to a
     * specified vertex:
     *
     * <pre><code>
     *     final int vertexId = ...
     *     final int transactionCount = graph.getVertexTransactionCount(vertexId);
     *     for (int position = 0; position &lt; transactionCount; position++) {
     *         final int transactionId = graph.getVertexTransaction(vertexId, position);
     *         ...
     *     }
     * </code></pre>
     *
     * @param vertex the id of the vertex.
     *
     * @return the count of all transactions adjacent to the specified vertex.
     */
    int getVertexTransactionCount(final int vertex);

    /**
     * Returns the number of transactions adjacent to the specified vertex that
     * have the specified direction relative to that vertex.
     *
     * The accepted directions are Graph.OUTGOING, Graph.INCOMING and
     * Graph.UNDIRECTED.
     *
     * @param vertex the vertex.
     * @param direction the direction the transactions have relative to that
     * vertex.
     * @return the number of transactions adjacent to the specified vertex that
     * have the specified direction relative to that vertex.
     */
    int getVertexTransactionCount(final int vertex, final int direction);

    /**
     * Returns the position the specified vertex occupies in the list of all
     * vertices in the graph.
     *
     * @param vertex the vertex.
     * @return the position the specified vertex occupies in the list of all
     * vertices in the graph.
     */
    int getVertexPosition(final int vertex);

    /**
     * Returns a UID for this vertex that will be unique in this graph for all
     * vertices with this vertex id. This allows you to distinguish between
     * vertices that happen to use the same vertex id due to vertices being
     * added and removed.
     *
     * @param vertex the vertex id.
     * @return the vertex UID.
     */
    long getVertexUID(final int vertex);

    /**
     * Returns the link between 2 specified vertices or Graph.NOT_FOUND if no
     * such link exists in the graph. As a link has no direction, the order in
     * which the two vertices are provided does not matter.
     *
     * @param vertex1 the id of the first vertex adjacent to the link.
     * @param vertex2 the id of the second vertex adjacent to the link.
     * @return the link between 2 specified vertices or Graph.NOT_FOUND if no
     * such link exists in the graph.
     */
    int getLink(final int vertex1, final int vertex2);

    /**
     * Returns the source vertex of this transaction. If the transaction is
     * undirected, the source vertex will always be the vertex with the lower
     * id. If the transaction represents a loop, the source and destination
     * vertices will be the same.
     *
     * @param transaction the id of the transaction.
     * @return the source vertex of this transaction.
     */
    int getTransactionSourceVertex(final int transaction);

    /**
     * Returns the destination vertex of this transaction. If the transaction is
     * undirected, the destination vertex will always be the vertex with the
     * higher id. If the transaction represents a loop, the source and
     * destination vertices will be the same.
     *
     * @param transaction the id of the transaction.
     * @return the destination vertex of this transaction.
     */
    int getTransactionDestinationVertex(final int transaction);

    /**
     * Returns true if a link with the specified id exists in the graph. If this
     * method returns true, then all other methods that expect a link id as a
     * parameter are guaranteed to give well defined results. If this method
     * returns false, it is illegal to call any of these methods and the results
     * of doing so are undefined. In general, most link ids are gained from
     * querying the graph (such as getLink(...) etc) meaning that it is already
     * clear that a candidate link id exists. This means that this method is
     * hardly ever required.
     *
     * @param link the id of the link.
     * @return true if a link with the specified id exists in the graph.
     */
    boolean linkExists(final int link);

    /**
     * Returns true if a transaction with the specified id exists in the graph.
     * If this method returns true, then all other methods that expect a
     * transaction id as a parameter are guaranteed to give well defined
     * results. If this method returns false, it is illegal to call any of these
     * methods and the results of doing so are undefined. In general, most
     * transaction ids are gained from querying the graph (such as
     * getTransaction(...) etc) meaning that it is already clear that a
     * candidate transaction id exists. This means that this method is hardly
     * ever required.
     *
     * @param transaction the id of the transaction.
     * @return true if a transaction with the specified id exists in the graph.
     */
    boolean transactionExists(final int transaction);

    /**
     * Returns true if a vertex with the specified id exists in the graph. If
     * this method returns true, then all other methods that expect a vertex id
     * as a parameter are guaranteed to give well defined results. If this
     * method returns false, it is illegal to call any of these methods and the
     * results of doing so are undefined. In general, most vertex ids are gained
     * from querying the graph (such as getVertex() etc) meaning that it is
     * already clear that a candidate vertex id exists. This means that this
     * method is hardly ever required.
     *
     * @param vertex the id of the vertex.
     * @return true if a vertex with the specified id exists in the graph.
     */
    boolean vertexExists(final int vertex);

    /**
     * Returns a count of the number of attributes that exist in the graph for
     * the specified element type.
     *
     * @param elementType the number of attributes for this element type will be
     * returned.
     *
     * @return a count of the number of attributes that exist in the graph for
     * the specified element type.
     */
    int getAttributeCount(final GraphElementType elementType);

    /**
     * Returns the number of attributes this graph is currently capable of
     * storing. The capacity automatically expands as new attributes are added
     * to the graph so there is no need to explicitly expand the capacity.
     *
     * @return the number of attributes this graph is currently capable of
     * storing. The capacity automatically expands as new attributes are added
     * to the graph so there is no need to explicitly expand the capacity.
     */
    int getAttributeCapacity();

    /**
     * Returns the id of the attribute that occupies the specified position in
     * the list of all attributes for the specified element type.
     *
     * @param elementType the element type.
     * @param position the position of the attribute in the element type's list
     * of attributes.
     * @return the id of the attribute that occupies the specified position in
     * the list of all attributes for the specified element type.
     */
    int getAttribute(final GraphElementType elementType, final int position);

    /**
     * Returns the id of the attribute of the specified element type with the
     * specified name.
     *
     * @param elementType the element type.
     * @param name the name of the attribute.
     * @return the id of the attribute of the specified element type with the
     * specified name.
     */
    int getAttribute(final GraphElementType elementType, final String name);

    /**
     * Returns the {@link NativeAttributeType} of the attribute with the
     * specified id.
     *
     * @param attribute the id of the attribute.
     *
     * @return the {@link NativeAttributeType} of the attribute with the
     * specified id.
     */
    NativeAttributeType getNativeAttributeType(final int attribute);

    /**
     * Return the name of the attribute. This name will be unique for all
     * attributes associated with the same element type in a graph. This is the
     * value that is presented to the user in the UI and the most common way in
     * which attributes are looked up in the graph.
     *
     * @param attribute the id of the attribute.
     *
     * @return the name of the attribute.
     */
    public String getAttributeName(final int attribute);

    /**
     * The type of this attribute.
     * <p>
     * This is a String as returned by
     * {@link au.gov.asd.tac.constellation.graph.attribute.AttributeDescription#getName()}
     * from one of the registered AttributeDescription instances.
     *
     * @param attribute the id of the attribute.
     *
     * @return The type of this attribute.
     */
    public String getAttributeType(final int attribute);

    /**
     * Returns the description of an attribute. The description provides more
     * detailed information about the attribute such as how it is being used or
     * and constraints that should be observed.
     *
     * @param attribute the id of the attribute.
     *
     * @return the description of an attribute.
     */
    public String getAttributeDescription(final int attribute);

    /**
     * Returns the element type that this attribute is associated with.
     *
     * @param attribute the id of the attribute.
     *
     * @return the element type that this attribute is associated with.
     */
    public GraphElementType getAttributeElementType(final int attribute);

    /**
     * Returns the class of the attribute description that defines this
     * attribute.
     *
     * @param attribute the id of the attribute.
     *
     * @return the class of the attribute description that defines this
     * attribute.
     */
    public Class<? extends AttributeDescription> getAttributeDataType(final int attribute);

    /**
     * Returns the attribute merger for the specified attribute.
     *
     * @param attribute the id of the attribute.
     *
     * @return the attribute merger for the specified attribute.
     */
    public GraphAttributeMerger getAttributeMerger(final int attribute);

    /**
     * Returns the current default value for this attribute. This is the value
     * that new elements will get when they are created.
     *
     * @param attribute the id of the attribute.
     *
     * @return the current default value for this attribute.
     */
    public Object getAttributeDefaultValue(final int attribute);

    /**
     * Returns a UID for this attribute that will be unique in this graph for
     * all attribute with this attribute id. This allows you to distinguish
     * between attributes that happen to use the same attribute id due to
     * attributes being added and removed.
     *
     * @param attribute the attribute id.
     * @return the attribute UID.
     */
    long getAttributeUID(final int attribute);

    Object createReadAttributeObject(final int attribute, IntReadable indexReadable);

    /**
     * Returns true if the value of this attribute for the specified element is
     * equal to the default value for that attribute. For attributes that hold
     * object values, this is often null, but not necessarily so.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return true if the value of this attribute for the specified element is
     * equal to the default value for that attribute.
     */
    abstract boolean isDefaultValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive byte value. In general, the native type of the attribute will
     * be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive byte value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a byte.
     */
    abstract byte getByteValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive short value. In general, the native type of the attribute
     * will be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive short value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a short.
     */
    abstract short getShortValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive int value. In general, the native type of the attribute will
     * be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive int value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a int.
     */
    abstract int getIntValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive long value. In general, the native type of the attribute will
     * be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive long value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a long.
     */
    abstract long getLongValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive float value. In general, the native type of the attribute
     * will be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive float value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a float.
     */
    abstract float getFloatValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive double value. In general, the native type of the attribute
     * will be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive double value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a double.
     */
    abstract double getDoubleValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive boolean value. In general, the native type of the attribute
     * will be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive boolean value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a boolean.
     */
    abstract boolean getBooleanValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a primitive char value. In general, the native type of the attribute will
     * be of a different type and the attribute will be converted by the
     * attribute each time it is requested. Therefore, value requests where the
     * conversion might be expensive should be avoided when possible.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive char value.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as a char.
     */
    abstract char getCharValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * a String value. In general, the native type of the attribute will be of a
     * different type and the attribute will be converted by the attribute each
     * time it is requested. Therefore, value requests where the conversion
     * might be expensive should be avoided when possible.
     *
     * All attributes must be able to represent their values as strings with the
     * added requirement that any value returned by this method must be a valid
     * input to the corresponding setString() call and produce the same
     * underlying value in the attribute. For instance:
     *
     * <pre><code>
     *      final int attributeId = ...
     *      final int elementId = ...
     *      final String stringValue = graph.getStringValue(attributeId, elementId);
     *      graph.setXXXValue(attributeId, elementId, ...);
     *      graph.setStringValue(attributeId, elementId, stringValue);
     * </code></pre>
     *
     * must result in the attribute value for the specified element returning to
     * the same value it had originally.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * a primitive byte value.
     *
     */
    abstract String getStringValue(final int attribute, final int id);

    /**
     * Returns the value of the specified attribute for the specified element as
     * an Object. In general, the native type of the attribute will be of a
     * different type and the attribute will be converted by the attribute each
     * time it is requested. Therefore, value requests where the conversion
     * might be expensive should be avoided when possible.
     *
     * @param <T> the type of object value to return.
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @return the value of the specified attribute for the specified element as
     * an Object.
     *
     * @throws IllegalArgumentException if the attribute is unable to represent
     * its values as an Object.
     */
    abstract <T> T getObjectValue(final int attribute, final int id);

    /**
     * Returns true if the specified attribute is part of the primary key for
     * its element type.
     *
     * @param attribute the id of the attribute.
     *
     * @return true if the specified attribute is part of the primary key for
     * its element type.
     */
    abstract boolean isPrimaryKey(final int attribute);

    /**
     * Returns true if the specified attribute will accept the specified string
     * as an input value to the setString() method. If true is returned, it can
     * be guaranteed that setString() will not throw an error when passed this
     * value. For example:
     *
     * <pre><code>
     *      final int attributeId = ...
     *      final String stringValue = ...
     *      final int elementId = ...
     *      if (graph.acceptsStringValue(attributeId, stringValue) {
     *          graph.setString(attributeId, elementId, stringValue);
     *      } else {
     *          // Handle invalid values here.
     *      }
     * </code></pre>
     *
     * @param attribute the id of the attribute.
     * @param value a candidate string value to setString().
     * @return true if the specified attribute will accept the specified string
     * as an input value to the setString() method.
     */
    abstract String acceptsStringValue(final int attribute, final String value);

    /**
     * Returns an object that holds all the information necessary to restore the
     * attribute values for this attribute.
     *
     * @param attribute the id of the attribute.
     *
     * @return an object that holds all the information necessary to restore the
     * attribute values for this attribute.
     */
    public Object copyAttribute(final int attribute);

    /**
     * Returns a GraphKey object that represent the key value attributes for the
     * specified element. The returned GraphKey object.
     *
     * @param elementType the element type of the element.
     * @param id the id of the element.
     * @return a GraphKey object that represent the key value attributes for the
     * specified element. The returned GraphKey object.
     */
    public GraphKey getPrimaryKeyValue(final GraphElementType elementType, final int id);

    /**
     * Returns the attribute ids for the attributes in the primary key for the
     * specified element type.
     * <p>
     * For instance, the vertex key attributes can be accessed by:
     *
     * <pre><code>
     *      final int [] keyAttributeIds = graph.getPrimaryKey(GraphElementType.VERTEX);
     *      for (int keyAttributeId : keyAttributeIds) {
     *          final Attribute keyAttribute = graph.getAttribute(keyAttributeId);
     *          ...
     *      }
     * </code></pre>
     *
     * @param elementType the element type.
     * @return the attribute ids for the attributes in the primary key for the
     * specified element type.
     */
    public int[] getPrimaryKey(final GraphElementType elementType);

    /**
     * Returns true if the specified attribute supports the specified index
     * type.
     *
     * @param attribute the id of the attribute.
     * @param indexType the index type.
     * @return true if the specified attribute supports the specified index
     * type.
     */
    public boolean attributeSupportsIndexType(final int attribute, final GraphIndexType indexType);

    /**
     * Returns the type of index that is currently operating on the specified
     * attribute.
     *
     * The type of index determines which types of queries can be performed
     * efficiently on the attribute. The attribute must be able to perform both
     * exact match queries and range queries but unless an enabling index is
     * currently operating, these queries generally resort to scanning all the
     * data meaning that the query will be very inefficient.
     *
     * See GraphIndexType for a description of the different index types
     * available and the types of queries they enable.
     *
     * @param attribute the id of the attribute.
     * @return the type of index that is currently operating on the specified
     * attribute.
     * @see GraphIndexType
     */
    public GraphIndexType getAttributeIndexType(final int attribute);

    /**
     * Returns a GraphIndexResult containing all elements that have a value for
     * the specified attribute that is an exact match to the specified value.
     * While this query will always work, unless either an UNORDERED or ORDERED
     * index has been created on the attribute this operation will resort to a
     * full scan of all the element values and therefore be extremely
     * inefficient.
     * <p>
     * Unless performance is not a concern, callers should first call
     * getAttributeIndexType() before calling this method to determine what
     * index is in operation and therefore if this query will be efficient.
     *
     * @param attribute the id of the attribute.
     * @param value the value to match against.
     * @return a GraphIndexResult containing all elements that have a value for
     * the specified attribute that is an exact match to the specified value.
     */
    public GraphIndexResult getElementsWithAttributeValue(final int attribute, final Object value);

    /**
     * Returns a GraphIndexResult containing all elements that have a value for
     * the specified attribute that is within the range defined by the specified
     * start and end values, inclusively. While this query will always work,
     * unless an ORDERED index has been created on the attribute this operation
     * will resort to a full scan of all the element values and therefore be
     * extremely inefficient.
     * <p>
     * Unless performance is not a concern, callers should first call
     * getAttributeIndexType() before calling this method to determine what
     * index is in operation and therefore if this query will be efficient.
     *
     * @param attribute the id of the attribute.
     * @param start the start of the range (inclusive).
     * @param end the end of the range (inclusive).
     * @return a GraphIndexResult containing all elements that have a value for
     * the specified attribute that is within the range defined by the specified
     * start and end values, inclusively.
     */
    public GraphIndexResult getElementsWithAttributeValueRange(final int attribute, final Object start, final Object end);

    /**
     * Returns a stream containing all vertices in the graph.
     *
     * @return a stream containing all vertices in the graph.
     */
    public IntStream vertexStream();

    /**
     * Returns a stream containing all links in the graph.
     *
     * @return a stream containing all links in the graph.
     */
    public IntStream linkStream();

    /**
     * Returns a stream containing all edges in the graph.
     *
     * @return a stream containing all edges in the graph.
     */
    public IntStream edgeStream();

    /**
     * Returns a stream containing all transactions in the graph.
     *
     * @return a stream containing all transactions in the graph.
     */
    public IntStream transactionStream();
}
