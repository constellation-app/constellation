/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.undo.GraphEdit;

/**
 * A NativeAttributeType represents the fundamental Java type that is used to
 * store the values of an attribute. The possibilities include each primitive
 * type as well as {@link java.lang.Object}. There are many operations, such as
 * copying and testing for equality that depend only on this native type, rather
 * than on the specific Object type stored. Each attribute specifies its
 * NativeAttributeType allowing these types of operations to be performed
 * efficiently on any attribute without the need to know the underlying
 * implementation.
 *
 * @author sirius
 */
public enum NativeAttributeType {

    BYTE("byte") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setByteValue(attribute, destinationElement, graph.getByteValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.b = graph.getByteValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setByteValue(attribute, element, value.b);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setByteValue(attribute, element, oldValue.b, newValue.b);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.b == a.b;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.b = ((Number) value).byteValue();
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final byte currentValue = graph.getByteValue(attribute, element);
            if (currentValue != oldValue.b) {
                edit.setByteValue(attribute, element, oldValue.b, currentValue);
                return true;
            }
            return false;
        }
    },
    SHORT("short") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setShortValue(attribute, destinationElement, graph.getShortValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.s = graph.getShortValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setShortValue(attribute, element, value.s);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setShortValue(attribute, element, oldValue.s, newValue.s);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.s == a.s;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.s = ((Number) value).shortValue();
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final short currentValue = graph.getShortValue(attribute, element);
            if (currentValue != oldValue.s) {
                edit.setShortValue(attribute, element, oldValue.s, currentValue);
                return true;
            }
            return false;
        }
    },
    INT("int") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setIntValue(attribute, destinationElement, graph.getIntValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.i = graph.getIntValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setIntValue(attribute, element, value.i);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setIntValue(attribute, element, oldValue.i, newValue.i);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.i == a.i;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.i = ((Number) value).intValue();
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final int currentValue = graph.getIntValue(attribute, element);
            if (currentValue != oldValue.i) {
                edit.setIntValue(attribute, element, oldValue.i, currentValue);
                return true;
            }
            return false;
        }
    },
    LONG("long") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setLongValue(attribute, destinationElement, graph.getLongValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.l = graph.getLongValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setLongValue(attribute, element, value.l);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setLongValue(attribute, element, oldValue.l, newValue.l);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.l == a.l;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.l = ((Number) value).longValue();
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final long currentValue = graph.getLongValue(attribute, element);
            if (currentValue != oldValue.l) {
                edit.setLongValue(attribute, element, oldValue.l, currentValue);
                return true;
            }
            return false;
        }
    },
    FLOAT("float") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setFloatValue(attribute, destinationElement, graph.getFloatValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.f = graph.getFloatValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setFloatValue(attribute, element, value.f);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setFloatValue(attribute, element, oldValue.f, newValue.f);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.f == a.f;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.f = ((Number) value).floatValue();
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final float currentValue = graph.getFloatValue(attribute, element);
            if (currentValue != oldValue.f) {
                edit.setFloatValue(attribute, element, oldValue.f, currentValue);
                return true;
            }
            return false;
        }
    },
    DOUBLE("double") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setDoubleValue(attribute, destinationElement, graph.getDoubleValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.d = graph.getDoubleValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setDoubleValue(attribute, element, value.d);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setDoubleValue(attribute, element, oldValue.d, newValue.d);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.d == a.d;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.d = ((Number) value).doubleValue();
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final double currentValue = graph.getDoubleValue(attribute, element);
            if (currentValue != oldValue.d) {
                edit.setDoubleValue(attribute, element, oldValue.d, currentValue);
                return true;
            }
            return false;
        }
    },
    BOOLEAN("boolean") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setBooleanValue(attribute, destinationElement, graph.getBooleanValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.z = graph.getBooleanValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setBooleanValue(attribute, element, value.z);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setBooleanValue(attribute, element, oldValue.z, newValue.z);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.z == a.z;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.z = (Boolean) value;
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final boolean currentValue = graph.getBooleanValue(attribute, element);
            if (currentValue != oldValue.z) {
                edit.setBooleanValue(attribute, element, oldValue.z, currentValue);
                return true;
            }
            return false;
        }
    },
    CHAR("char") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setCharValue(attribute, destinationElement, graph.getCharValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.c = graph.getCharValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setCharValue(attribute, element, value.c);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setCharValue(attribute, element, oldValue.c, newValue.c);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return b.c == a.c;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.c = (Character) value;
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final char currentValue = graph.getCharValue(attribute, element);
            if (currentValue != oldValue.c) {
                edit.setCharValue(attribute, element, oldValue.c, currentValue);
                return true;
            }
            return false;
        }
    },
    OBJECT("Object") {
        @Override
        public void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement) {
            graph.setObjectValue(attribute, destinationElement, graph.getObjectValue(attribute, sourceElement));
        }

        @Override
        public void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value) {
            value.o = graph.getObjectValue(attribute, element);
        }

        @Override
        public void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value) {
            graph.setObjectValue(attribute, element, value.o);
        }

        @Override
        public void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue) {
            edit.setObjectValue(attribute, element, oldValue.o, newValue.o);
        }

        @Override
        public boolean equalValue(final NativeValue a, final NativeValue b) {
            return a.o == b.o;
        }

        @Override
        public NativeValue create(final Object value) {
            final NativeValue nativeValue = new NativeValue();
            nativeValue.o = value;
            return nativeValue;
        }

        @Override
        public boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue) {
            final Object currentValue = graph.getObjectValue(attribute, element);
            if (currentValue != oldValue.o) {
                edit.setObjectValue(attribute, element, oldValue.o, currentValue);
                return true;
            }
            return false;
        }
    };

    private final String label;

    private NativeAttributeType(final String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    /**
     * Efficiently copies the attribute value from a source element to a
     * destination element.
     *
     * @param graph the graph holding the elements.
     * @param attribute the attribute to copy.
     * @param sourceElement the source element id.
     * @param destinationElement the destination element id.
     */
    public abstract void copyAttributeValue(final GraphWriteMethods graph, final int attribute, final int sourceElement, final int destinationElement);

    /**
     * Efficiently copies the attribute value from an element into a NativeValue
     * object.
     *
     * @param graph the graph holding the element.
     * @param attribute the attribute to copy.
     * @param element the element id.
     * @param value the NativeValue that will hold the attribute value.
     */
    public abstract void get(final GraphReadMethods graph, final int attribute, final int element, final NativeValue value);

    /**
     * Efficiently sets the attribute value of a specified element to the value
     * held by a specified NativeValue.
     *
     * @param graph the graph holding the element.
     * @param attribute the attribute.
     * @param element the id of the element to set.
     * @param value the NativeValue holding the attribute value.
     */
    public abstract void set(final GraphWriteMethods graph, final int attribute, final int element, final NativeValue value);

    /**
     * Updates the specified GraphEdit to include modifying the attribute value
     * of the specified attribute for the specified element from a specified old
     * value to a specified new value.
     *
     * @param edit the edit to be updated.
     * @param attribute the attribute that was modified.
     * @param element the element that was modified.
     * @param oldValue the old value for the attribute.
     * @param newValue the new value for the attribute.
     */
    public abstract void addEdit(final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue, final NativeValue newValue);

    /**
     * Efficiently test for equality between 2 NativeValue objects. This test
     * cannot be implemented directly on NativeValue as the NativeValue does not
     * know exactly which type of information it is holding and the type of
     * value it is holding determines how the equality test should be performed.
     *
     * @param a the first NativeValue object.
     * @param b the second NativeValue object.
     * @return true if the 2 NativeValue objects are equalValue for this
     * NativeValueType.
     */
    public abstract boolean equalValue(final NativeValue a, final NativeValue b);

    /**
     * Creates a new NativeValue object for the specified value, converting as
     * required.
     *
     * @param value an Object representation of the NativeValue required.
     * @return a new NativeValue object for the specified value, converting as
     * required.
     */
    public abstract NativeValue create(final Object value);

    /**
     * Updates the specified GraphEdit to include modifying the attribute value
     * of the specified attribute for the specified element from the specified
     * oldValue to its current value. This method is used in cases where an
     * attribute value has been modified and the undo/redo edit needs to be
     * updated to incorporate this change.
     *
     * @param graph the graph holding the element.
     * @param edit the edit to be updated.
     * @param attribute the attribute that was modified.
     * @param element the element that was modified.
     * @param oldValue the previous value.
     * @return true if the edit needed to be updated. In cases where the current
     * value is equalValue to the old value the edit can remain unchanged and
     * false is returned.
     */
    public abstract boolean addEdit(final GraphReadMethods graph, final GraphEdit edit, final int attribute, final int element, final NativeValue oldValue);

    /**
     * A NativeValue object holds an attribute value in an efficient form,
     * regardless of the underlying native data structures used to hold the
     * attribute values. It does this by providing an instance field for each
     * primitive type as well as Object, and relies on the NativeAttributeType
     * for a particular attribute to store attribute values in the most
     * efficient field.
     */
    public static class NativeValue {

        byte b;
        short s;
        int i;
        long l;
        float f;
        double d;
        boolean z;
        char c;
        Object o;
    }
}
