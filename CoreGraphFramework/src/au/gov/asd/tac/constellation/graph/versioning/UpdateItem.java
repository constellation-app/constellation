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
package au.gov.asd.tac.constellation.graph.versioning;

import au.gov.asd.tac.constellation.graph.StoreGraph;

/**
 * Update Item
 *
 * @author twilight_sparkle
 */
public abstract class UpdateItem implements Comparable<UpdateItem> {

    public abstract int getPriority();

    public abstract String getName();

    public abstract boolean appliesToGraph(final StoreGraph graph);

    @Override
    public boolean equals(final Object o) {
        return o != null && this.getClass() == o.getClass() 
                && getPriority() == ((UpdateItem) o).getPriority() && getName().equals(((UpdateItem) o).getName());
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (hash * 13) + Integer.hashCode(getPriority());
        hash = (hash * 13) + getName().hashCode();
        return hash;
    }

    @Override
    public int compareTo(final UpdateItem o) {
        return getPriority() == o.getPriority() ? getName().compareTo(o.getName()) : Integer.compare(getPriority(), o.getPriority());
    }
}
