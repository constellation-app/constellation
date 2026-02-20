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
package au.gov.asd.tac.constellation.views.scatterplot;

import javafx.scene.chart.XYChart;

/**
 * An object representing the scatter plot data for a single graph element.
 *
 * @author cygnus_x-1
 */
public class ScatterData {

    private final int elementId;
    private final String elementLabel;
    private final XYChart.Data<? extends Object, ? extends Object> data;

    public ScatterData(int elementId, String elementLabel, XYChart.Data<? extends Object, ? extends Object> data) {
        this.elementId = elementId;
        this.elementLabel = elementLabel;
        this.data = data;
    }

    /**
     * Get the element id of the element represented by this ScatterData.
     *
     * @return the element id of the element represented by this ScatterData.
     */
    public int getElementId() {
        return elementId;
    }

    /**
     * Get the label of the element represented by this ScatterData.
     *
     * @return the label of the element represented by this ScatterData.
     */
    public String getElementLabel() {
        return elementLabel;
    }

    /**
     * Get the scatter plot data associated with the element represented by this
     * ScatterData.
     *
     * @return the scatter plot data associated with the element represented by
     * this ScatterData.
     */
    public XYChart.Data<? extends Object, ? extends Object> getData() {
        return data;
    }
}
