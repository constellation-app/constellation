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
package au.gov.asd.tac.constellation.graph;

/**
 * Implementations of this interface are used to specify how the attributes of 2
 * elements are merged together to form a single element. The most common use of
 * this is when 2 elements in the graph have duplicate primary keys and the most
 * recently created element is merged into the original.
 *
 * @author sirius
 */
public interface GraphElementMerger {

    /**
     * This method is called by the graph when 2 elements need to be merged into
     * a single element. It is the responsibility of the merger to remove the
     * merged element before returning. If this merger can successfully merge
     * the two elements then it should update the attribute values of the
     * surviving element as appropriate, remove the merged element, and return
     * true. If the two elements cannot be successfully merged, the method
     * should return false and leave the graph unchanged.
     *
     * @param graph the graph holding the elements.
     * @param elementType the type of element being merged.
     * @param survivingElement the element that should survive the merge.
     * @param mergedElement the element that should be removed during the merge.
     * @return true if the 2 elements were able to be merged successfully.
     */
    public boolean mergeElement(final GraphWriteMethods graph, final GraphElementType elementType, final int survivingElement, final int mergedElement);
}
