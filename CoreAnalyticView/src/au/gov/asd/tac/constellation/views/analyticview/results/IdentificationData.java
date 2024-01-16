/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.results;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import java.util.Objects;

/**
 * A class designed for combining all of the data needed to identify unique
 * graph elements.
 *
 * @author Nova
 */
public class IdentificationData {

    protected final GraphElementType elementType;
    protected final int elementId;
    protected final String identifier;

    public IdentificationData(final GraphElementType elementType, final int elementId, final String identifier) {
        this.elementType = elementType;
        this.elementId = elementId;
        this.identifier = identifier;
    }

    /**
     * Get the element type of the graph element this AnalyticResult corresponds
     * to.
     *
     * @return
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    /**
     * Get the id of the graph element this AnalyticResult corresponds to.
     *
     * @return
     */
    public int getElementId() {
        return elementId;
    }

    /**
     * Get an identifier value for the graph element this AnalyticResult
     * corresponds to.
     *
     * @return
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.elementType);
        hash = 71 * hash + this.elementId;
        hash = 71 * hash + Objects.hashCode(this.identifier);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IdentificationData other = (IdentificationData) obj;
        if (this.elementId != other.elementId) {
            return false;
        }
        if (!Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        return this.elementType == other.elementType;
    }

}
