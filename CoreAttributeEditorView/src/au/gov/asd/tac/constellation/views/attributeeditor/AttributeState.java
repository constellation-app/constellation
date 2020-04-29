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
package au.gov.asd.tac.constellation.views.attributeeditor;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AttributeState is the data model for CONSTELLATION's attribute editor. It
 * contains information about the current graphs attributes and all their
 * values. AttributeState objects are primarily created and populated by an
 * {@link AttributeReader} when requested by the
 * {@link AttributeEditorTopComponent}.
 *
 * @author twinkle2_little
 */
public class AttributeState {
    //graph, nodes, trasaction etc.

    private final List<GraphElementType> graphElements;
    private final List<GraphElementType> activeGraphElements;
    //key: element type, value : list of attributedata
    private final HashMap<GraphElementType, ArrayList<AttributeData>> attributeNames;
    //key: element type+attribute name value: list of values
    private final HashMap<String, Object[]> attributeValues;
    private final Map<GraphElementType, Integer> attributeCounts;

    public AttributeState(List<GraphElementType> graphElements, List<GraphElementType> activeGraphElements, HashMap<GraphElementType, ArrayList<AttributeData>> attributeNames, HashMap<String, Object[]> attributeValues, Map<GraphElementType, Integer> attributeCounts) {
        this.graphElements = graphElements;
        this.activeGraphElements = activeGraphElements;
        this.attributeNames = attributeNames;
        this.attributeValues = attributeValues;
        this.attributeCounts = attributeCounts;
    }

    /**
     * @return the graphElements
     */
    public List<GraphElementType> getGraphElements() {
        return graphElements;
    }

    /**
     * Get the graph elements types that are 'active' in this state.
     * <br>
     * Note that if this list is empty then the state is saying nothing about
     * which graph element types are active, and hence the attribute editor
     * should not change the 'activeness' of any graph elements (by expanding or
     * contracting). Yes this is horrendously dodge, but for the moment,
     * whatever.
     *
     * @return the graphElements that are 'active' in this state
     */
    public List<GraphElementType> getActiveGraphElements() {
        return activeGraphElements;
    }

    /**
     * @return the attributeNames
     */
    public HashMap<GraphElementType, ArrayList<AttributeData>> getAttributeNames() {
        return attributeNames;
    }

    /**
     * @return the attributeValues
     */
    public HashMap<String, Object[]> getAttributeValues() {
        return attributeValues;
    }

    /**
     * @return the attributeCounts
     */
    public Map<GraphElementType, Integer> getAttributeCounts() {
        return attributeCounts;
    }
}
