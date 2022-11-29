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
package au.gov.asd.tac.constellation.views.scatterplot.state;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;

/**
 * An object representing the state of the Scatter Plot.
 *
 * @author cygnus_x-1
 */
public class ScatterPlotState {

    private GraphElementType elementType;
    private Attribute xAttribute;
    private Attribute yAttribute;
    private boolean selectedOnly;

    public ScatterPlotState() {
        this.elementType = GraphElementType.VERTEX;
        this.xAttribute = null;
        this.yAttribute = null;
        this.selectedOnly = false;
    }

    public ScatterPlotState(ScatterPlotState scatterState) {
        this.elementType = scatterState == null ? null : scatterState.getElementType();
        this.xAttribute = scatterState == null ? null : scatterState.getXAttribute();
        this.yAttribute = scatterState == null ? null : scatterState.getYAttribute();
        this.selectedOnly = scatterState == null ? null : scatterState.isSelectedOnly();
    }

    /**
     * Get the element type whose attributes are currently being visualised in
     * the Scatter Plot.
     *
     * @return the element type whose attributes are currently being visualised
     * in the Scatter Plot.
     */
    public GraphElementType getElementType() {
        return elementType;
    }

    /**
     * Set the element type whose attributes are currently being visualised in
     * the Scatter Plot.
     *
     * @param elementType the new element type.
     */
    public void setElementType(GraphElementType elementType) {
        this.elementType = elementType;
    }

    /**
     * Get the attribute which is currently being visualised on the x-axis of
     * the Scatter Plot.
     *
     * @return the attribute which is currently being visualised on the x-axis
     * of the Scatter Plot.
     */
    public Attribute getXAttribute() {
        return xAttribute;
    }

    /**
     * Set the attribute which is currently being visualised on the x-axis of
     * the Scatter Plot.
     *
     * @param xAttribute the new attribute to visualise on the x-axis.
     */
    public void setXAttribute(Attribute xAttribute) {
        this.xAttribute = xAttribute;
    }

    /**
     * Get the attribute which is currently being visualised on the y-axis of
     * the Scatter Plot.
     *
     * @return the attribute which is currently being visualised on the y-axis
     * of the Scatter Plot.
     */
    public Attribute getYAttribute() {
        return yAttribute;
    }

    /**
     * Set the attribute which is currently being visualised on the y-axis of
     * the Scatter Plot.
     *
     * @param yAttribute the new attribute to visualise on the y-axis.
     */
    public void setYAttribute(Attribute yAttribute) {
        this.yAttribute = yAttribute;
    }

    /**
     * Get a boolean representing whether the Scatter Plot should visualise only
     * data currently selected on the graph or not.
     *
     * @return a boolean representing whether the Scatter Plot should visualise
     * only data currently selected on the graph or not.
     */
    public boolean isSelectedOnly() {
        return selectedOnly;
    }

    /**
     * Set a boolean representing whether the Scatter Plot should visualise only
     * data currently selected on the graph or not.
     *
     * @param selectedOnly the new selectedOnly value.
     */
    public void setSelectedOnly(boolean selectedOnly) {
        this.selectedOnly = selectedOnly;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("ScatterPlotState[elementType=");
        out.append(elementType);
        out.append(", xAttribute=");
        out.append(xAttribute);
        out.append(", yAttribute=");
        out.append(yAttribute);
        out.append(", selectedOnly=");
        out.append(selectedOnly);
        out.append("]");
        return out.toString();
    }
}
