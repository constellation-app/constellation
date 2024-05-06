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
package au.gov.asd.tac.constellation.graph.utilities;

import au.gov.asd.tac.constellation.graph.locking.ParameterReadAccess;
import au.gov.asd.tac.constellation.graph.locking.ParameterWriteAccess;
import java.io.Serializable;

/**
 * data model for multi-value properties
 *
 * @author sirius
 */
public class MultiValueStore implements ParameterReadAccess, ParameterWriteAccess, Serializable {

    private int i;
    private long l;
    private float f;
    private double d;
    private Object o;
    
    private int getInt(){
        return i;
    }

    private long getLong() {
        return l;
    }

    private float getFloat() {
        return f;
    }

    private double getDouble() {
        return d;
    }
    
    private Object getObject(){
        return o;
    }

    @Override
    public int getExecuteInt() {
        return getInt();
    }

    @Override
    public long getExecuteLong() {
        return getLong();
    }

    @Override
    public float getExecuteFloat() {
        return getFloat();
    }

    @Override
    public double getExecuteDouble() {
        return getDouble();
    }

    @Override
    public Object getExecuteObject() {
        return getObject();
    }

    @Override
    public int getUndoInt() {
        return getInt();
    }

    @Override
    public long getUndoLong() {
        return getLong();
    }

    @Override
    public float getUndoFloat() {
        return getFloat();
    }

    @Override
    public double getUndoDouble() {
        return getDouble();
    }

    @Override
    public Object getUndoObject() {
        return getObject();
    }

    @Override
    public void setInt(final int value) {
        i = value;
    }

    @Override
    public void setLong(final long value) {
        l = value;
    }

    @Override
    public void setFloat(final float value) {
        f = value;
    }

    @Override
    public void setDouble(final double value) {
        d = value;
    }

    @Override
    public void setObject(final Object value) {
        o = value;
    }
}
