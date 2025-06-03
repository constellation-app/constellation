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

import au.gov.asd.tac.constellation.graph.operations.GraphOperation;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;

/**
 * GraphWriteMethods extends GraphReadMethods and adds methods that allow
 * modification of a graph.
 *
 * @see GraphReadMethods
 * @author sirius
 */
public interface GraphWriteMethods extends GraphReadMethods {

    /**
     * Sets the attributes that make up the primary key for the specified
     * element type. If an empty list of attributes is provided then no primary
     * key is defined and no automatic merging will occur for this element type.
     * However, if a new primary key is specified that causes primary key
     * clashes in the graph then this call could cause automatic merging of
     * elements in the graph.
     * <p>
     * From this moment on, automatic merging will occur based on these key
     * attributes.
     *
     * @param elementType the element type.
     * @param attributes the ids of the attributes that will make up the primary
     * key for this element type.
     * @see GraphReadMethods#getPrimaryKey(GraphElementType)
     * @see GraphWriteMethods#validateKey(GraphElementType, boolean)
     * @see GraphWriteMethods#validateKey(GraphElementType, int, boolean)
     */
    public void setPrimaryKey(final GraphElementType elementType, final int... attributes);

    /**
     * Validates all elements of the specified key for uniqueness with regard to
     * the primary key for that element type.
     *
     * If no key clashes are detected then the method will return without error.
     * If a key clash occurs then the result will depend on whether merging has
     * been allowed. If merging is allowed, then the graph's merger will be
     * given a chance to merge the two elements with the same key. If this
     * happens successfully then the method will return with out error. If
     * merging is not allowed or the graph's merger is unable to merge the two
     * elements, a DuplicateKeyException is thrown containing information about
     * the two elements that need to be merged.
     *
     * @param elementType the element type to be validated.
     * @param allowMerging whether the graph's merger should be given an
     * opportunity to resolve primary key clashes.
     * @throws DuplicateKeyException if a primary key clash occurred and either
     * merging was not allowed or the graph's merger was unable to merge the two
     * elements.
     * @see GraphReadMethods#getPrimaryKey(GraphElementType)
     * @see GraphWriteMethods#setPrimaryKey(GraphElementType, int...)
     */
    public void validateKey(final GraphElementType elementType, final boolean allowMerging) throws DuplicateKeyException;

    /**
     * Validates a specific element for uniqueness with regard to the primary
     * key for that element type.
     *
     * If no key clashes are detected then the method will return without error.
     * If a key clash occurs then the result will depend on whether merging has
     * been allowed. If merging is allowed, then the graph's merger will be
     * given a chance to merge the two elements with the same key. If this
     * happens successfully then the method will return with out error. If
     * merging is not allowed or the graph's merger is unable to merge the two
     * elements, a DuplicateKeyException is thrown containing information about
     * the two elements that need to be merged.
     *
     * @param elementType the element type to be validated.
     * @param element the element to validate.
     * @param allowMerging whether the graph's merger should be given an
     * opportunity to resolve primary key clashes.
     * @throws DuplicateKeyException if a primary key clash occurred and either
     * merging was not allowed or the graph's merger was unable to merge the two
     * elements.
     * @see GraphReadMethods#getPrimaryKey(GraphElementType)
     * @see GraphWriteMethods#setPrimaryKey(GraphElementType, int...)
     */
    public void validateKey(final GraphElementType elementType, final int element, final boolean allowMerging) throws DuplicateKeyException;

    /**
     * Adds a new vertex to the graph.
     * <p>
     * The vertex will be given a new id that is both currently unused and also
     * less than the graph's vertex capacity. If the graph's vertex capacity is
     * exhausted then the capacity will be increased so that the new vertex can
     * be accommodated.
     * <p>
     * If a primary key has been set on vertices in the graph, it is important
     * that this new vertex be allocated unique primary key attribute values
     * before the changes are committed to the graph or automatic vertex merging
     * may occur.
     *
     * @return the id of the new vertex.
     * @see GraphReadMethods#getVertexCapacity()
     */
    int addVertex();
    
    /**
     * Adds a new vertex to the graph.
     * <p>
     * The new vertex will be given the id supplied from the parameters. It
     * will fail if there already exists a transaction with that id. If the
     * graph's vertex capacity is exhausted then the capacity will be
     * increased so that the new vertex can be accommodated.
     * <p>
     * If a primary key has been set on vertices in the graph, it is important
     * that this new vertex be allocated unique primary key attribute values
     * before the changes are committed to the graph or automatic vertex merging
     * may occur.
     *
     * @param vertex the id to be given to the vertex
     * @return the id of the new vertex.
     * @see GraphReadMethods#getVertexCapacity()
     */
    int addVertex(final int vertex);

    /**
     * Removes the specified vertex from the graph. This will also cause the
     * removal of all the adjacent transactions, edges and links of that vertex.
     *
     * @param vertex the id of the vertex to remove.
     * @throws IllegalArgumentException if the specified vertex does not exist
     * in the graph.
     */
    void removeVertex(final int vertex);

    /**
     * Adds a new transaction to the graph.
     * <p>
     * The new transaction will be given a new id that is both currently unused
     * and also less than the graph's transaction capacity. If the graph's
     * transaction capacity is exhausted then the capacity will be increased so
     * that the new transaction can be accommodated.
     * <p>
     * If this is the first transaction to be added between these two vertices
     * then a new link will also be created to hold the transaction. If the
     * transaction is the first in its link with this direction then a new edge
     * will also be created to represent the transaction.
     * <p>
     * If the new transaction is a loop (source and destination vertices the
     * same), then a directed transaction will have an uphill edge but not a
     * downhill edge.
     * <p>
     * If the transaction is undirected, the source and destination vertices may
     * be swapped to ensure that the source vertex id is not greater than the
     * destination vertex id. This ensures that all undirected transactions are
     * represented by the uphill edge.
     * <p>
     * If a primary key has been set on transaction in the graph, it is
     * important that this new transaction be allocated unique primary key
     * attribute values before the changes are committed to the graph or
     * automatic transaction merging may occur.
     *
     * @param sourceVertex the vertex from which the transaction originates.
     * @param destinationVertex the vertex at which the transaction terminates.
     * @param directed specifies whether or not the transaction is directed or
     * undirected.
     * @return the id of the new transaction.
     */
    int addTransaction(final int sourceVertex, final int destinationVertex, final boolean directed);

    /**
     * Adds a new transaction to the graph.
     * <p>
     * The new transaction will be given the id supplied from the parameters. It
     * will fail if there already exists a transaction with that id. If the
     * graph's transaction capacity is exhausted then the capacity will be
     * increased so that the new transaction can be accommodated.
     * <p>
     * If this is the first transaction to be added between these two vertices
     * then a new link will also be created to hold the transaction. If the
     * transaction is the first in its link with this direction then a new edge
     * will also be created to represent the transaction.
     * <p>
     * If the new transaction is a loop (source and destination vertices the
     * same), then a directed transaction will have an uphill edge but not a
     * downhill edge.
     * <p>
     * If the transaction is undirected, the source and destination vertices may
     * be swapped to ensure that the source vertex id is not greater than the
     * destination vertex id. This ensures that all undirected transactions are
     * represented by the uphill edge.
     * <p>
     * If a primary key has been set on transaction in the graph, it is
     * important that this new transaction be allocated unique primary key
     * attribute values before the changes are committed to the graph or
     * automatic transaction merging may occur.
     *
     * @param transaction the id to be given to the transaction
     * @param sourceVertex the vertex from which the transaction originates.
     * @param destinationVertex the vertex at which the transaction terminates.
     * @param directed specifies whether or not the transaction is directed or
     * undirected.
     * @return the id of the new transaction.
     */
    int addTransaction(final int transaction, final int sourceVertex, final int destinationVertex, final boolean directed);

    /**
     * Removes the specified transaction from the graph. If this is the last
     * transaction represented by an edge then the edge will also be removed.
     * Likewise, if this is the last transaction represented by a link then the
     * link will also be removed.
     *
     * @param transaction the id of the transaction to be removed.
     * @throws IllegalArgumentException if the specified transaction does not
     * exist in the graph.
     */
    void removeTransaction(final int transaction);

    /**
     * Sets the source vertex of a transaction. If this transaction is
     * undirected and this modification causes the source vertex id to become
     * greater than the destination vertex id then the source and destination
     * vertices will be swapped to ensure that the transaction remains on the
     * uphill edge in its link.
     *
     * <pre><code>
     *      final int transactionId = graph.addTransaction(1, 2, false);
     *      graph.setTransactionSourceVertex(transactionId, 3);
     *      final int sourceId = graph.getTransactionSourceVertex(transactionId); // returns 2
     *      final int destniationId = graph.getTransactionDestinationVertex(transactionId); // returns 3
     * </code></pre>
     *
     * @param transaction the id of the transaction.
     * @param newSourceVertex the id of the new source vertex.
     */
    public void setTransactionSourceVertex(final int transaction, final int newSourceVertex);

    /**
     * Sets the destination vertex of a transaction. If this transaction is
     * undirected and this modification causes the destination vertex id to
     * become less than the source vertex id then the source and destination
     * vertices will be swapped to ensure that the transaction remains on the
     * uphill edge in its link.
     *
     * <pre><code>
     *      final int transactionId = graph.addTransaction(2, 3, false);
     *      graph.setTransactionDestinationVertex(transactionId, 1);
     *      final int sourceId = graph.getTransactionSourceVertex(transactionId); // returns 1
     *      final int destniationId = graph.getTransactionDestinationVertex(transactionId); // returns 2
     * </code></pre>
     *
     * @param transaction the id of the transaction.
     * @param newDestinationVertex the id of the new destination vertex.
     */
    public void setTransactionDestinationVertex(final int transaction, final int newDestinationVertex);

    /**
     * Add an attribute to the graph. The new attribute will be given an id that
     * is both unused and less than the graph's current attribute capacity. If
     * the graph's attribute capacity is exhausted then it will be increased
     * automatically to accommodate the new attribute.
     *
     * @param elementType The type of element.
     * @param attributeType The type of attribute to be added.
     * @param label The attribute label.
     * @param description The attribute description.
     * @param defaultValue The default value of this attribute. This is
     * typically assigned in the same way that setObjectValue() assigns values.
     * @param attributeMergerId the id of the (@link GraphAttributeMerger} to
     * use when merging elements with duplicate keys.
     *
     * @return the id of the new attribute.
     */
    int addAttribute(final GraphElementType elementType, final String attributeType, final String label,
            final String description, final Object defaultValue, final String attributeMergerId);

    /**
     * Removes the attribute with the specified id from the graph.
     *
     * @param attribute the id of the attribute to remove.
     * @throws IllegalArgumentException if the specified attribute does not
     * exist in the graph.
     */
    void removeAttribute(final int attribute);

    /**
     * Changes the name of an attribute in the graph.
     *
     * @param attribute the id of the attribute.
     * @param newName the new name of the attribute.
     * @throws IllegalArgumentException if a new name is specified that is
     * already in use for an attribute for the same element type.
     */
    void updateAttributeName(final int attribute, final String newName);

    /**
     * Changes the description of an attribute in the graph.
     *
     * @param attribute the id of the attribute.
     * @param newDescription the new description of the attribute.
     */
    void updateAttributeDescription(final int attribute, final String newDescription);

    /**
     * Changes the default value of an attribute in the graph. From this point
     * on, all new elements will be given this value when they are created.
     *
     * @param attribute the id of the attribute.
     * @param newObject an object representation of the default value. The
     * attribute is free to translate this object into its native type in a way
     * that makes sense. This should be done in a way compatible with
     * setObjectValue();
     * @see GraphWriteMethods#setObjectValue(int, int, java.lang.Object)
     */
    void updateAttributeDefaultValue(final int attribute, final Object newObject);

    Object createWriteAttributeObject(final int attribute, final IntReadable indexReadable);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the default value for that attribute.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @see GraphWriteMethods#updateAttributeDefaultValue(int, java.lang.Object)
     */
    abstract void clearValue(final int attribute, final int id);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified byte value. In general, the underlying native type of the
     * attribute is not a byte, meaning that the attribute must convert, if
     * possible, the specified byte value into its native format. In some cases,
     * this conversion may be an expensive operation meaning that this call
     * should be used in care where the native type of the attribute is not a
     * byte and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified byte value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setByteValue(final int attribute, final int id, final byte value);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified short value. In general, the underlying native type of the
     * attribute is not a short, meaning that the attribute must convert, if
     * possible, the specified short value into its native format. In some
     * cases, this conversion may be an expensive operation meaning that this
     * call should be used in care where the native type of the attribute is not
     * a short and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified short value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setShortValue(final int attribute, final int id, final short value);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified int value. In general, the underlying native type of the
     * attribute is not an int, meaning that the attribute must convert, if
     * possible, the specified int value into its native format. In some cases,
     * this conversion may be an expensive operation meaning that this call
     * should be used in care where the native type of the attribute is not an
     * int and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified int value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setIntValue(final int attribute, final int id, final int value);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified long value. In general, the underlying native type of the
     * attribute is not a long, meaning that the attribute must convert, if
     * possible, the specified long value into its native format. In some cases,
     * this conversion may be an expensive operation meaning that this call
     * should be used in care where the native type of the attribute is not a
     * long and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified long value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setLongValue(final int attribute, final int id, final long value);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified float value. In general, the underlying native type of the
     * attribute is not a float, meaning that the attribute must convert, if
     * possible, the specified float value into its native format. In some
     * cases, this conversion may be an expensive operation meaning that this
     * call should be used in care where the native type of the attribute is not
     * a float and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified float value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setFloatValue(final int attribute, final int id, final float value);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified double value. In general, the underlying native type of the
     * attribute is not a double, meaning that the attribute must convert, if
     * possible, the specified double value into its native format. In some
     * cases, this conversion may be an expensive operation meaning that this
     * call should be used in care where the native type of the attribute is not
     * a double and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified double value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setDoubleValue(final int attribute, final int id, final double value);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified boolean value. In general, the underlying native type of
     * the attribute is not a boolean, meaning that the attribute must convert,
     * if possible, the specified boolean value into its native format. In some
     * cases, this conversion may be an expensive operation meaning that this
     * call should be used in care where the native type of the attribute is not
     * a boolean and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified boolean value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setBooleanValue(final int attribute, final int id, final boolean value);

    /**
     * Sets the value of the specified attribute for the specified element to
     * the specified char value. In general, the underlying native type of the
     * attribute is not a char, meaning that the attribute must convert, if
     * possible, the specified char value into its native format. In some cases,
     * this conversion may be an expensive operation meaning that this call
     * should be used in care where the native type of the attribute is not a
     * char and performance is a concern.
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified char value cannot be
     * converted into the native type of the attribute.
     */
    abstract void setCharValue(final int attribute, final int id, final char value);

    /**
     * Set the value of the specified attribute for the specified element to the
     * specified String value.
     * <p>
     * As well as being the setter for string attributes, this is a generic
     * value setter for all types.
     * <p>
     * This method is guaranteed to be compatible with all values returned from
     * {@link GraphReadMethods#getStringValue(int, int) } meaning that any value
     * returned from that method must be a valid input for this method and
     * produce the same underlying native attribute value. For example, if a
     * Color type outputs a "red,green,blue" tuple, it must be able to parse
     * that tuple back to a Color type. This code example should always work
     * without error and cause both elements to have the same underlying
     * attribute values.
     *
     * <pre><code>
     *      final int vertexId1 = ...
     *      final int vertexId2 = ...
     *      final int attributeId = ...
     *      final String stringValue = graph.getStringValue(attributeId, vertexId1);
     *      graph.setStringValue(attributeId, vertexId2, stringValue);
     * </code></pre>
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new attribute value.
     * @throws IllegalArgumentException if the supplied string value is not
     * valid for this attribute.
     * @see GraphReadMethods#getStringValue(int, int)
     */
    abstract void setStringValue(final int attribute, final int id, final String value);

    /**
     * Set an Object value.
     * <p>
     * IMPORTANT: it is assumed that the Object is immutable, otherwise
     * undo/redo won't work properly, and the behaviour of the graph in general
     * will become undefined.
     * <p>
     * Assume the following (incorrect) code.
     * <pre><code>
     *   final MyObject value = wg.getObjectValue(attrId, id);
     *   value.setSomething(something);
     *   wg.setObjectValue(attrId, id, value);
     * </code></pre> If an undo is done at this point, the previous state of the
     * graph will be referencing the same updated MyObject, and the undo will
     * have no effect. Instead, do this.
     * <pre><code>
     *   final MyObject value = wg.getObjectValue(attrId, id);
     *   final MyObject newValue = new MyObject(value); // Create an independent copy.
     *   newValue.setSomething(something);
     *   wg.setObjectValue(attrId, id, newValue);
     * </code></pre>
     *
     * It is also guaranteed that this method will accept any value returned
     * from {@link GraphReadMethods#getObjectValue(int, int) } and produce the
     * same underlying native attribute value for the element it is set on. This
     * means that the following code should alway work with out error and
     * produce the same underlying attribute value for both vertices.
     *
     * <pre><code>
     *      final int vertexId1 = ...
     *      final int vertexId2 = ...
     *      final int attributeId = ...
     *      final Object objectValue = graph.getObjectValue(attributeId, vertexId1);
     *      graph.setObjectValue(attributeId, vertexId2, objectValue);
     * </code></pre>
     *
     * @param attribute the id of the attribute.
     * @param id the id of the element.
     * @param value the new value of the attribute.
     * @throws IllegalArgumentException if the specified Object value is not
     * valid for this attribute.
     * @see GraphReadMethods#getObjectValue(int, int)
     */
    abstract void setObjectValue(final int attribute, final int id, final Object value);

    /**
     * Executes the specified graph operation on the graph. In general, the
     * graph should be modified by using the standard API provided in this
     * interface. In these cases, the appropriate messages are sent ensuring
     * that undo/redo works consistently with not further work required by the
     * caller.
     * <p>
     * However, in some cases, this process of storing undo/redo information can
     * be extremely inefficient. For instance, in cases where all vertices in
     * the graph are selected, the default behavior would be to store a change
     * event for each vertex that needs to be selected, while a more efficient
     * approach would be to simply store a bitset holding all the vertices that
     * were previously unselected. In these cases, it is possible to provide a
     * custom {@link GraphOperation} object that handles the undo/redo
     * operations in a more memory efficient way.
     * <p>
     * IMPORTANT: This is an advanced operation and should only be used where
     * the benefits clearly outweigh the extra work and risk of taking over
     * responsibility for undo/redo. It is critical that the undo/redo
     * operations provided by the GraphOperation restore the graph to exactly
     * the right state or the graph may become corrupted.
     *
     * @param operation the GraphOperation to execute.
     * @see GraphOperation
     */
    public void executeGraphOperation(final GraphOperation operation);

    /**
     * Sets the type of index that operates on this attribute. This can be used
     * to add, remove or alter the index on an attribute.
     *
     * @param attribute the id of the attribute.
     * @param indexType the required index type.
     * @see GraphReadMethods#getAttributeIndexType(int)
     * @see GraphIndexType
     * @see GraphIndexType#NONE
     * @see GraphIndexType#UNORDERED
     * @see GraphIndexType#ORDERED
     */
    public void setAttributeIndexType(final int attribute, final GraphIndexType indexType);
}
