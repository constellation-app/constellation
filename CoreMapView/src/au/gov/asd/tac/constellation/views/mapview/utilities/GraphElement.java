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
package au.gov.asd.tac.constellation.views.mapview.utilities;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.Objects;

/**
 * An object representing an element on a graph.
 *
 * @author cygnus_x-1
 */
public class GraphElement {

    public static final GraphElement NON_ELEMENT = new GraphElement(-1, GraphElementType.META);

    private final int id;
    private final GraphElementType type;

    public GraphElement(final int id, final GraphElementType type) {
        this.id = id;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public GraphElementType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.id;
        hash = 89 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GraphElement other = (GraphElement) obj;
        if (this.id != other.id) {
            return false;
        }
        return this.type == other.type;
    }

    @Override
    public String toString() {
        return String.format("%s %d", type.toString(), id);
    }
}
