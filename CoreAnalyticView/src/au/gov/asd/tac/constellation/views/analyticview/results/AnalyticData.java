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
package au.gov.asd.tac.constellation.views.analyticview.results;

import au.gov.asd.tac.constellation.graph.GraphElementType;

/**
 *
 * @author cygnus_x-1
 */
public abstract class AnalyticData {

    protected final IdentificationData id;
    protected final boolean isNull;

    protected AnalyticData(final GraphElementType elementType, final int elementId, final String identifier, final boolean isNull) {
        this.id = new IdentificationData(elementType, elementId, identifier);
        this.isNull = isNull;
    }

    public IdentificationData getIdentificationData() {
        return id;
    }

    /**
     * Get the element type of the graph element this AnalyticResult corresponds
     * to.
     *
     * @return
     */
    public GraphElementType getElementType() {
        return id.getElementType();
    }

    /**
     * Get the id of the graph element this AnalyticResult corresponds to.
     *
     * @return
     */
    public int getElementId() {
        return id.getElementId();
    }

    /**
     * Get an identifier value for the graph element this AnalyticResult
     * corresponds to.
     *
     * @return
     */
    public String getIdentifier() {
        return id.getIdentifier();
    }

    /**
     * Get a flag indicating if this data represents null, or no result.
     *
     * @return
     */
    public boolean isNull() {
        return isNull;
    }

}
