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
package au.gov.asd.tac.constellation.views.histogram.bins;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.views.histogram.Bin;

/**
 * A bin that holds arbitrary object values.
 *
 * @author sirius
 */
public class ObjectBin extends Bin {

    public static final Object ERROR_OBJECT = new Object() {
        @Override
        public String toString() {
            return "ERROR";
        }
    };

    protected Object key;

    @Override
    public int compareTo(Bin o) {
        final ObjectBin bin = (ObjectBin) o;
        if (key == null) {
            return bin.key == null ? 0 : -1;
        } else if (bin.key == null) {
            return 1;
        } else if (key == ERROR_OBJECT) {
            return bin.key == ERROR_OBJECT ? 0 : 1;
        } else if (bin.key == ERROR_OBJECT) {
            return -1;
        } else {
            @SuppressWarnings("unchecked") //comparableKey will be comparable object
            final Comparable<Object> comparableKey = (Comparable<Object>) key;
            return comparableKey.compareTo(bin.key);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.key != null ? this.key.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() == o.getClass()) {
            final ObjectBin bin = (ObjectBin) o;
            return key == null ? bin.key == null : key.equals(bin.key);
        }
        return false;
    }

    @Override
    public boolean isNull() {
        return key == null;
    }

    @Override
    public void prepareForPresentation() {
        label = key == null ? null : String.valueOf(key);
    }

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        key = graph.getObjectValue(attribute, element);
    }

    @Override
    public Bin create() {
        return new ObjectBin();
    }

    @Override
    public Object getKeyAsObject() {
        return key;
    }
}
