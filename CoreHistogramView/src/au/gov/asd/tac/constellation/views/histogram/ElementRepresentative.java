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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;

/**
 * An ElementRepresentative provides a mapping between the element that is being
 * binned and the element that provides the values upon which the binning is
 * occurring. The common example of this is when a transaction is being binned
 * based on the value of an attribute of its source or destination vertex.
 *
 * @author sirius
 */
public abstract class ElementRepresentative {

    /**
     * Does this ElementRepresentative apply to this element type?.
     *
     * @param elementType the candidate element type.
     * @return true if this representative applies to the candidate element
     * type.
     */
    public abstract int appliesToElementType(GraphElementType elementType);

    /**
     * Get the ElementType that represents the specified ElementType.
     *
     * @param elementType the element type that needs a representative.
     * @return the element type that represents the specified element type.
     */
    public abstract GraphElementType getRepresentativeElementType(GraphElementType elementType);

    /**
     * Get the element that will represent this element.
     *
     * @param graph the graph that holds the elements.
     * @param elementType the element type of the element.
     * @param element the element that needs a representative.
     * @return the element that will represent this element.
     */
    public abstract int findRepresentative(GraphReadMethods graph, GraphElementType elementType, int element);

}
