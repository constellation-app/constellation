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
package au.gov.asd.tac.constellation.graph.locking;

import java.io.Serializable;

/**
 * The data model for storing the attributes of a graph.
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
        converter = intConverter;
    }

    @Override
    public void setLong(final long value) {
        l = value;
        converter = longConverter;
    }

    @Override
    public void setFloat(final float value) {
        f = value;
        converter = floatConverter;
    }

    @Override
    public void setDouble(final double value) {
        d = value;
        converter = doubleConverter;
    }

    @Override
    public void setObject(final Object value) {
        o = value;
        converter = objectConverter;
    }

    public final void save() {
        converter.save(access);
    }

    private interface Saver extends Serializable {

        public void save(final ParameterWriteAccess pwa);
    }

    private final Saver intConverter = new Saver() {
        @Override
        public void save(final ParameterWriteAccess access) {
            access.setInt(i);
        }
    };
    private final Saver longConverter = new Saver() {
        @Override
        public void save(final ParameterWriteAccess access) {
            access.setLong(l);
        }
    };
    private final Saver floatConverter = new Saver() {
        @Override
        public void save(final ParameterWriteAccess access) {
            access.setFloat(f);
        }
    };
    private final Saver doubleConverter = new Saver() {
        @Override
        public void save(final ParameterWriteAccess access) {
            access.setDouble(d);
        }
    };
    private final Saver objectConverter = new Saver() {
        @Override
        public void save(final ParameterWriteAccess access) {
            access.setObject(o);
        }
    };
}
