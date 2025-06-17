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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;

/**
 * AttributeType represents the 3 different types of attributes that can be
 * specified in the importer.
 *
 * @author sirius
 */
public enum AttributeType {

    // Source vertex attributes
    SOURCE_VERTEX(GraphElementType.VERTEX, "Source Vertex Attributes", ConstellationColor.BLUEBERRY),
    // Destination vertex attributes
    DESTINATION_VERTEX(GraphElementType.VERTEX, "Destination Vertex Attributes", ConstellationColor.MELON),
    // Transaction attributes
    TRANSACTION(GraphElementType.TRANSACTION, "Transaction Attributes", ConstellationColor.MANILLA);

    private final GraphElementType elementType;
    private final String label;
    private final ConstellationColor color;

    private AttributeType(final GraphElementType elementType, final String label, final ConstellationColor color) {
        this.elementType = elementType;
        this.label = label;

        this.color = color;
    }

    /**
     * Returns the element type of this AttributeType.
     *
     * @return the element type of this AttributeType.
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    /**
     * Returns the label of this AttributeType. This is the identifier that will
     * be displayed in the UI.
     *
     * @return the label of this AttributeType.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the color that should be used in the UI to represent this
     * AttributeType.
     *
     * @return the color that should be used in the UI to represent this
     * AttributeType.
     */
    public ConstellationColor getColor() {
        return color;
    }
}
