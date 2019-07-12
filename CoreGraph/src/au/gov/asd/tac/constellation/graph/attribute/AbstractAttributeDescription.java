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
package au.gov.asd.tac.constellation.graph.attribute;

import au.gov.asd.tac.constellation.graph.GraphIndex;
import au.gov.asd.tac.constellation.graph.GraphIndexType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.NativeAttributeType;
import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import java.io.Serializable;

/**
 * abstract class for an attribute description
 *
 * @author algol
 */
public abstract class AbstractAttributeDescription implements AttributeDescription, Serializable {

    public static final int DEFAULT_VERSION = 0;
    protected GraphReadMethods graph = null;

    @Override
    public void setGraph(final GraphReadMethods graph) {
        if (this.graph != null) {
            throw new IllegalArgumentException("Can't reset to a new graph");
        }

        this.graph = graph;
    }

    @Override
    public int getVersion() {
        return DEFAULT_VERSION;
    }

    @Override
    public NativeAttributeType getNativeType() {
        return NativeAttributeType.OBJECT;
    }

    @Override
    public byte getByte(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to byte", getName()));
    }

    @Override
    public void setByte(int id, byte value) {
        throw new IllegalArgumentException(String.format("Error converting byte to %s", getName()));
    }

    @Override
    public short getShort(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to short", getName()));
    }

    @Override
    public void setShort(int id, short value) {
        throw new IllegalArgumentException(String.format("Error converting short to %s", getName()));
    }

    @Override
    public int getInt(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to int", getName()));
    }

    @Override
    public void setInt(int id, int value) {
        throw new IllegalArgumentException(String.format("Error converting int to %s", getName()));
    }

    @Override
    public long getLong(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to long", getName()));
    }

    @Override
    public void setLong(int id, long value) {
        throw new IllegalArgumentException(String.format("Error converting long to %s", getName()));
    }

    @Override
    public float getFloat(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to float", getName()));
    }

    @Override
    public void setFloat(int id, float value) {
        throw new IllegalArgumentException(String.format("Error converting float to %s", getName()));
    }

    @Override
    public double getDouble(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to double", getName()));
    }

    @Override
    public void setDouble(int id, double value) {
        throw new IllegalArgumentException(String.format("Error converting double to %s", getName()));
    }

    @Override
    public boolean getBoolean(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to boolean", getName()));
    }

    @Override
    public void setBoolean(int id, boolean value) {
        throw new IllegalArgumentException(String.format("Error converting boolean to %s", getName()));
    }

    @Override
    public char getChar(int id) {
        throw new IllegalArgumentException(String.format("Error converting %s to char", getName()));
    }

    @Override
    public void setChar(int id, char value) {
        throw new IllegalArgumentException(String.format("Error converting char to %s", getName()));
    }

    @Override
    public String getSearchString(int id) {
        return getString(id);
    }

    @Override
    public String acceptsString(String value) {
        return null;
    }

    @Override
    public Object convertToNativeValue(Object objectValue) {
        return objectValue;
    }

    @Override
    public boolean canBeImported() {
        return true;
    }

    @Override
    public int ordering() {
        return Integer.MAX_VALUE;
    }

    protected static boolean equals(final Object a, final Object b) {
        return a == null ? b == null : a.equals(b);
    }

    @Override
    public void save(final int id, final ParameterWriteAccess access) {
        access.setObject(getObject(id));
    }

    @Override
    public void restore(final int id, final ParameterReadAccess access) {
        setObject(id, access.getUndoObject());
    }

    @Override
    public boolean supportsIndexType(GraphIndexType indexType) {
        return indexType == GraphIndexType.NONE;
    }

    @Override
    public GraphIndex createIndex(GraphIndexType indexType) {
        return AttributeDescription.NULL_GRAPH_INDEX;
    }
}
