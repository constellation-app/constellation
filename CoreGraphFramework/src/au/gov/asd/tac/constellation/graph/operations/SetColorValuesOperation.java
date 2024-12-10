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
package au.gov.asd.tac.constellation.graph.operations;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;

/**
 * The SetColorValuesOperation is used when the need arises to change a large
 * number of ConstellationColor attribute values on an attribute of
 * nodes/transactions on the graph, such as for animation. By implementing the
 * color changes as a GraphOperation, significant space can be saved on the
 * undo/redo stack.
 *
 * @author andromeda-224
 */
public class SetColorValuesOperation extends GraphOperation {

    private final int attribute;
    private final int capacity;
    private int id;
    protected ConstellationColor originalColor;
    protected ConstellationColor newColor;
    private final GraphWriteMethods graph;

    /**
     * Set up value initialisation.
     * @param graph Graph to update
     * @param elementType Element type to update
     * @param attribute Attribute to update
*/
    public SetColorValuesOperation(final GraphWriteMethods graph, final GraphElementType elementType, final int attribute) {
        this.graph = graph;
        this.attribute = attribute;
        this.capacity = elementType.getElementCapacity(graph);
    }

    /**
     * Set the new value to update if it has changed.
     * @param elementId Id of element to update
     * @param newValue  New color to set attribute to
     */
    public void setValue(final int elementId, final ConstellationColor newValue) {
        this.id = elementId;
        this.originalColor = graph.getObjectValue(attribute, id);
        if (newValue != null
                && (newValue.getRGBString() == null ? originalColor.getRGBString() != null : !newValue.getRGBString().equals(originalColor.getRGBString()))) {
            newColor = newValue;
        }
    }

    @Override
    public void execute(final GraphWriteMethods graph) {
        if (newColor != null) {
            graph.setObjectValue(attribute, id, newColor);
        } 
    }

    @Override
    public void undo(final GraphWriteMethods graph) {
        graph.setObjectValue(attribute, id, originalColor);
        newColor = null;
    }

    @Override
    public boolean isMoreEfficient() {
        return true;
    }

    @Override
    public int size() {
        return capacity;
    }   
}
