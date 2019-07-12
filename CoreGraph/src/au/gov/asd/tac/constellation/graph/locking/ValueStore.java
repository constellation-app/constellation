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
package au.gov.asd.tac.constellation.graph.locking;

import java.io.Serializable;

/**
 * data model for storing the attributes of a graph
 *
 * @author sirius
 */
public final class ValueStore implements ParameterWriteAccess, Serializable {

    private final ParameterWriteAccess access;
    private int i;
    private long l;
    private float f;
    private double d;
    private Object o;
    private Saver converter;

    public ValueStore(final ParameterWriteAccess access) {
        this.access = access;
    }

    @Override
    public void setInt(final int value) {
        i = value;
        converter = INT_CONVERTER;
    }

    @Override
    public void setLong(final long value) {
        l = value;
        converter = LONG_CONVERTER;
    }

    @Override
    public void setFloat(final float value) {
        f = value;
        converter = FLOAT_CONVERTER;
    }

    @Override
    public void setDouble(final double value) {
        d = value;
        converter = DOUBLE_CONVERTER;
    }

    @Override
    public void setObject(final Object value) {
        o = value;
        converter = OBJECT_CONVERTER;
    }

    public final void save() {
        converter.save();
    }

    private static interface Saver extends Serializable {

        public void save();
    }

    private final Saver INT_CONVERTER = new Saver() {
        @Override
        public void save() {
            access.setInt(i);
        }
    };
    private final Saver LONG_CONVERTER = new Saver() {
        @Override
        public void save() {
            access.setLong(l);
        }
    };
    private final Saver FLOAT_CONVERTER = new Saver() {
        @Override
        public void save() {
            access.setFloat(f);
        }
    };
    private final Saver DOUBLE_CONVERTER = new Saver() {
        @Override
        public void save() {
            access.setDouble(d);
        }
    };
    private final Saver OBJECT_CONVERTER = new Saver() {
        @Override
        public void save() {
            access.setObject(o);
        }
    };
}
