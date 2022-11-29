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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphIndex;
import au.gov.asd.tac.constellation.graph.GraphIndexResult;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.NativeAttributeType.NativeValue;
import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import au.gov.asd.tac.constellation.graph.value.readables.IntReadable;

/**
 * An AttributeDescription defines an attribute type that can hold a new type of
 * value, such as integers, booleans, colors etc. This interface hides the
 * actual data stored by the attribute behind a collection of getter and setter
 * methods meaning that the attribute designer has a lot of freedom to store the
 * data in an efficient manner.
 * <p>
 * Every attribute must work with #getObject and #getString. Everything else is
 * optional. This means that comparison of values, #getObject and #getString are
 * the safest choice.
 * <p>
 * Access to the attribute values held by this AttributeDescription are accessed
 * by a collection of getter/setter pairs covering all the primitive types as
 * well as Objects and Strings. The general principle is that these
 * getter/setter pairs should operate in a consistent way.
 * <p>
 * A getter method must either provide a version of its underlying value in the
 * requested form or throw an {@link IllegalArgumentException}. However, if it
 * chooses to return a value, this value must be accepted by the corresponding
 * setter method and calling the setter method must return the underlying native
 * value to the same state it was in when the getter was called.
 * <p>
 *
 * @author sirius
 */
public interface AttributeDescription {

    /**
     * Sets the graph that this attribute belongs to. The framework calls this
     * method to inform the attribute which graph they belong to in case the
     * attribute needs to access other information from the graph. The attribute
     * description should keep hold onto this reference if such information is
     * required.
     *
     * @param graph the graph that this AttributeDescription belongs to.
     */
    public void setGraph(final GraphReadMethods graph);

    /**
     * Returns the name of this attribute.
     * <p>
     * This should be a unique name for all AttributeDescriptions in the
     * application and is required when a new attribute is created on a graph to
     * specify which attribute type is to be created. This name also becomes
     * part of the attribute's specification and is visible to the user through
     * the application.
     *
     * @return the name of this attribute.
     */
    public String getName();

    /**
     * Get the version of this Attribute Description.
     *
     * @return The version as an integer
     */
    public int getVersion();

    /**
     * The underlying Java type of the Attribute.
     * <p>
     * Useful for property editors, for instance.
     *
     * @return The underlying type of the Attribute.
     */
    public Class<?> getNativeClass();

    /**
     * Returns the underlying native type for this attribute.
     *
     * @return the underlying native type for this attribute.
     */
    public NativeAttributeType getNativeType();

    /**
     * Return the default value for this attribute as an Object. If the
     * underlying native type of this AttributeDescription is not an Object,
     * then it is understood that the returned value will be consistent with
     * values returned by {@link AttributeDescription#getObject(int) } and
     * accepted by
     * {@link AttributeDescription#setObject(int, java.lang.Object) }.
     *
     * @return The default value for this attribute as an Object.
     */
    public Object getDefault();

    /**
     * Set the default value for this attribute.
     *
     * @param value The value to set as the default. It is understood that this
     * method will accept the same range of object values as
     * {@link AttributeDescription#setObject(int, java.lang.Object) }.
     */
    void setDefault(final Object value);

    /**
     * Returns the current element capacity of this attribute. The attribute is
     * responsible for storing attribute values for all elements with id from 0
     * (inclusive) to its capacity (exclusive).
     *
     * @return the current element capacity of this attribute.
     * @see AttributeDescription#setCapacity(int)
     */
    public int getCapacity();

    /**
     * Sets the capacity of this attribute which is the number of elements the
     * attribute should be able to hold values for. The AttributeDescription
     * should immediately expand its storage to enable storage of values for all
     * elements with ids from 0 (inclusive) to the new capacity (exclusive).
     *
     * @param capacity the capacity of this attribute.
     * @see AttributeDescription#getCapacity()
     */
    public void setCapacity(final int capacity);

    /**
     * Returns the value for the specified element as a byte. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * byte on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return a byte value that is accepted by the
     * {@link AttributeDescription#setByte(int, byte) } method and cause the
     * underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a byte.
     */
    public byte getByte(final int id);

    /**
     * Sets the value for the specified element to the specified byte value. In
     * general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a byte value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same byte value when queried with the
     * {@link AttributeDescription#getByte(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getByte(int)
     */
    public void setByte(final int id, final byte value);

    /**
     * Returns the value for the specified element as a short. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * short on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return a short value that is accepted by the
     * {@link AttributeDescription#setShort(int, short) } method and cause the
     * underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a short.
     */
    public short getShort(final int id);

    /**
     * Sets the value for the specified element to the specified short value. In
     * general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a short value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same short value when queried with the
     * {@link AttributeDescription#getShort(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getShort(int)
     */
    public void setShort(final int id, final short value);

    /**
     * Returns the value for the specified element as an int. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to an
     * int on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return an int value that is accepted by the
     * {@link AttributeDescription#setInt(int, int) } method and cause the
     * underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as an int.
     */
    public int getInt(final int id);

    /**
     * Sets the value for the specified element to the specified int value. In
     * general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts an int value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same int value when queried with the
     * {@link AttributeDescription#getInt(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getInt(int)
     */
    public void setInt(final int id, final int value);

    /**
     * Returns the value for the specified element as a long. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * long on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return a long value that is accepted by the
     * {@link AttributeDescription#setLong(int, long) } method and cause the
     * underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a long.
     */
    public long getLong(final int id);

    /**
     * Sets the value for the specified element to the specified long value. In
     * general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a long value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same long value when queried with the
     * {@link AttributeDescription#getLong(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getLong(int)
     */
    public void setLong(final int id, final long value);

    /**
     * Returns the value for the specified element as a float. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * float on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return a float value that is accepted by the
     * {@link AttributeDescription#setByte(int, byte) } method and cause the
     * underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a float.
     */
    public float getFloat(final int id);

    /**
     * Sets the value for the specified element to the specified float value. In
     * general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a float value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same float value when queried with the
     * {@link AttributeDescription#getFloat(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getFloat(int)
     */
    public void setFloat(final int id, final float value);

    /**
     * Returns the value for the specified element as a double. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * double on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return a double value that is accepted by the
     * {@link AttributeDescription#setDouble(int, double) } method and cause the
     * underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a double.
     */
    public double getDouble(final int id);

    /**
     * Sets the value for the specified element to the specified double value.
     * In general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a double value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same double value when queried with the
     * {@link AttributeDescription#getDouble(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getDouble(int)
     */
    public void setDouble(final int id, final double value);

    /**
     * Returns the value for the specified element as a boolean. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * boolean on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return a boolean value that is accepted by the
     * {@link AttributeDescription#setBoolean(int, boolean) } method and cause
     * the underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a boolean.
     */
    public boolean getBoolean(final int id);

    /**
     * Sets the value for the specified element to the specified boolean value.
     * In general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a boolean value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same boolean value when queried with the
     * {@link AttributeDescription#getBoolean(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getBoolean(int)
     */
    public void setBoolean(final int id, final boolean value);

    /**
     * Returns the value for the specified element as a char. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * char on the fly. In cases where this conversion is not possible the
     * attribute should throw an IllegalArgumentException. In general, this
     * method should return a char value that is accepted by the
     * {@link AttributeDescription#setChar(int, char) } method and cause the
     * underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a char.
     */
    public char getChar(final int id);

    /**
     * Sets the value for the specified element to the specified char value. In
     * general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a char value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same char value when queried with the
     * {@link AttributeDescription#getChar(int) } method.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getChar(int)
     */
    public void setChar(final int id, final char value);

    /**
     * Returns the value for the specified element as a String. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to a
     * String on the fly. The String value returned by this method should be
     * accepted by the
     * {@link AttributeDescription#setString(int, java.lang.String) } method and
     * cause the underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as a String.
     */
    public String getString(final int id);

    /**
     * Sets the value for the specified element to the specified String value.
     * In general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts a String value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same String value when queried with the
     * {@link AttributeDescription#getString(int) } method. Likewise, this
     * method should accept all values returned by the {@link AttributeDescription#getString(int)
     * } for this attribute.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getString(int)
     */
    public void setString(final int id, final String value);

    /**
     * Validates a potential string value for this attribute value.
     *
     * If the value is valid then this method will return null and the attribute
     * description is obliged to accept the value in future calls to
     * setStringValue() calls.
     *
     * If the string is invalid then the method will return a message explaining
     * why.
     *
     * @param value the candidate string value
     * @return true if this {@link AttributeDescription} will accept the
     * specified {@link String} value through its
     * {@link AttributeDescription#setString(int, java.lang.String)} method.
     */
    public String acceptsString(final String value);

    /**
     * Returns the value for the specified element as an Object. In general, the
     * native type of this attribute will be of a different type meaning that it
     * is the responsibility of the attribute to convert the native type to an
     * Object on the fly. The Object value returned by this method should be
     * accepted by the
     * {@link AttributeDescription#setObject(int, java.lang.Object) } method and
     * cause the underlying native value to return to the original value.
     *
     * @param id the id of the element.
     * @return the value for the specified element as an Object.
     */
    public Object getObject(final int id);

    /**
     * Sets the value for the specified element to the specified Object value.
     * In general the native type of this attribute will be of a different type
     * meaning that it is the responsibility of the attribute to convert the
     * specified value on the fly. In cases where this conversion is not
     * possible the attribute should throw an IllegalArgumentException. In
     * general, if the attribute accepts an Object value, it should cause the
     * native attribute value for the specified element to reach a state that
     * will return the same Object value when queried with the
     * {@link AttributeDescription#getObject(int) } method. Likewise, this
     * method should accept all values returned by the {@link AttributeDescription#getObject(int)
     * }.
     *
     * @param id the id of the element.
     * @param value the new value for the attribute.
     * @throws IllegalArgumentException if the specified value cannot be
     * converted to the attribute's native type.
     * @see AttributeDescription#getObject(int)
     */
    public void setObject(final int id, final Object value);

    public Object createReadObject(IntReadable indexReadable);

    public Object createWriteObject(GraphWriteMethods graph, int attribute, IntReadable indexReadable);

    /**
     * Convert an attribute value from its object representation to its native
     * representation. The native representation is that which the attribute
     * description uses to physically store values of the attribute.
     * <br>
     * This should return an object {@code obj} such that
     * {@code getNativeType().create(obj)} will return a {@link NativeValue}
     * that can be used to set values of this attribute directly through calls
     * to {@link NativeAttributeType#set}.
     *
     * @param object an attribute value represented as an Object, ie. a value
     * obtained from a call to {@link #getObject}
     * @return An attribute value represented natively.
     */
    public Object convertToNativeValue(final Object object);

    /**
     * Returns true if the value for the specified element is equal to the
     * default value for this attribute.
     *
     * @param id the id of the element.
     * @return true if the value for the specified element is equal to the
     * default value for this attribute.
     * @see AttributeDescription#clear(int)
     */
    public boolean isClear(final int id);

    /**
     * Sets the value for the specified element to the default value for this
     * attribute.
     *
     * @param id the id of the element.
     */
    public void clear(final int id);

    /**
     * Returns a deep copy of this AttributeDescription. It is important that
     * this copy not rely on this instance in any way as the caller is free to
     * call mutating methods on the copy at any time. As all attribute values in
     * Constellation are defined to be immutable, simply copying the values is
     * acceptable.
     *
     * @param graph the graph for that the copy will be attached to.
     * @return a deep copy of this AttributeDescription.
     */
    public AttributeDescription copy(final GraphReadMethods graph);

    /**
     * Returns a hash code for the attribute value associated with the specified
     * element id.
     *
     * @param id the element id.
     * @return a hash code for the attribute value associated with the specified
     * element id.
     */
    public int hashCode(final int id);

    /**
     * Returns true if the attribute values associated with the two specified
     * element ids are considered equal. This test should be consistent with the
     * results obtained by calling {@link AttributeDescription#getObject(int) }
     * for each element id and calling {@link Object#equals(java.lang.Object)}
     * on the result. This method provides a performant and constistent way to
     * test for equality regardless of the underlying native type.
     *
     * @param id1 the first element id.
     * @param id2 the second element id.
     * @return true if the attribute values associated with the two specified
     * element ids are considered equal.
     */
    public boolean equals(final int id1, final int id2);

    /**
     * Causes the attribute value associated with specified element id to be
     * saved to the specified ParameterWriteAccess object. This operation should
     * be compatible with
     * {@link AttributeDescription#restore(int, au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess)}
     * and put the access object in a state that will return the underlying
     * native attribute value to the original state when the restore method is
     * called.
     *
     * @param id the element id.
     * @param access the ParameterWriteAccess object.
     */
    public void save(final int id, final ParameterWriteAccess access);

    /**
     * Causes the attribute value associated with the specified element id to be
     * restored from the specified ParameterReadAccess object. This operation
     * should be compatible with
     * {@link AttributeDescription#save(int, au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess)}
     * and restore the underlying native value to an identical state.
     *
     * @param id the element id.
     * @param access the ParameterReadAccess object.
     */
    public void restore(final int id, final ParameterReadAccess access);

    /**
     * When this method is called, the AttributeDescription should return an
     * object that stores all its element data. This same object will be passed
     * back to the restoreData() method so that the AttributeDescription can
     * restore itself.
     *
     * @return an object that will cause all attribute values held by this
     * AttributeDescription to be returned to their current state when passed to
     * {@link AttributeDescription#restoreData(java.lang.Object) }.
     */
    public Object saveData();

    /**
     * This method will be called when the AttributeDescription is required to
     * restore its data. The only objects passed to this method will be those
     * returned from {@link AttributeDescription#saveData() } method of the same
     * AttributeDescription instance.
     *
     * @param savedData the saved data object previously returned from a call to
     * {@link AttributeDescription#saveData() } on this object.
     */
    public void restoreData(final Object savedData);

    /**
     * Returns true if this AttributeDescription supports the specified index
     * type. The only index type an AttributeDescription must support is
     * {@link GraphIndexType#NONE} as this is the default state. If true is
     * returned then the caller can be guaranteed that a call to
     * {@link AttributeDescription#createIndex(au.gov.asd.tac.constellation.graph.GraphIndexType)}
     * with the same index type will not fail.
     *
     * @param indexType the candidate index type.
     * @return true if this AttributeDescription supports the specified index
     * type.
     */
    public boolean supportsIndexType(final GraphIndexType indexType);

    /**
     * Causes this AttributeDescription to create an index of the specified
     * type. Each AttributeDescription can only have a single index at any point
     * in time meaning that if an index current exists it will be deleted before
     * the new index is created. Before calling this method,
     * {@link AttributeDescription#supportsIndexType(au.gov.asd.tac.constellation.graph.GraphIndexType)}
     * should be called to test whether or not this AttributeDescription
     * supports the required index type. An exception to this is
     * {@link GraphIndexType#NONE} which represents the absence of an index and
     * is supported by all AttributeDescriptions.
     *
     * @param indexType the required index type.
     * @return the created GraphIndex object.
     */
    public GraphIndex createIndex(final GraphIndexType indexType);

    public static final GraphIndex NULL_GRAPH_INDEX = new GraphIndex() {

        @Override
        public void addElement(final int element) {
            // Override required for implementation of GraphIndex
        }

        @Override
        public void removeElement(final int element) {
            // Override required for implementation of GraphIndex
        }

        @Override
        public void updateElement(final int element) {
            // Override required for implementation of GraphIndex
        }

        @Override
        public GraphIndexResult getElementsWithAttributeValue(final Object value) {
            return null;
        }

        @Override
        public GraphIndexResult getElementsWithAttributeValueRange(final Object start, final Object end) {
            return null;
        }

        @Override
        public void expandCapacity(final int newCapacity) {
            // Override required for implementation of GraphIndex
        }
    };
}
