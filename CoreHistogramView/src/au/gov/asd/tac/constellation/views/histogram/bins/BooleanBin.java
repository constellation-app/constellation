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
 * A bin that holds boolean values.
 *
 * @author sirius
 */
public class BooleanBin extends Bin {

    protected boolean key;

    @Override
    public int compareTo(Bin o) {
        BooleanBin bin = (BooleanBin) o;
        if (key) {
            return bin.key ? 0 : 1;
        } else {
            return bin.key ? -1 : 0;
        }
    }

    @Override
    public int hashCode() {
        return key ? 1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() == o.getClass()) {
            BooleanBin bin = (BooleanBin) o;
            return key == bin.key;
        }
        return false;
    }

    @Override
    public void prepareForPresentation() {
        label = String.valueOf(key);
    }

    @Override
    public void setKey(GraphReadMethods graph, int attribute, int element) {
        key = graph.getBooleanValue(attribute, element);
    }

    @Override
    public Bin create() {
        return new BooleanBin();
    }

    @Override
    public Object getKeyAsObject() {
        return key;
    }
}
