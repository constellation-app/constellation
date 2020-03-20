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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects;

/**
 * A representation of a layer, or three dimensional slice of a graph.
 *
 * @author procyon
 */
public class LayerName implements Comparable<LayerName> {

    private final String name;
    private final int layer;

    public LayerName(final int layer, final String name) {
        this.name = name;
        this.layer = layer;
    }

    public int getLayer() {
        return layer;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(final LayerName o) {
        if (o.layer > this.layer) {
            return -1;
        } else if (o.layer == this.layer) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.name.hashCode();
        hash = 59 * hash + this.layer;
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LayerName other = (LayerName) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.layer != other.layer) {
            return false;
        }
        return true;
    }
}
