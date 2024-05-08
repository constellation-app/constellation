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
package au.gov.asd.tac.constellation.views.timeline.clustering;

import au.gov.asd.tac.constellation.views.timeline.components.Interaction;
import javafx.scene.chart.XYChart;

/**
 *
 * @author betelgeuse
 */
public abstract class TreeElement {

    private Interaction interaction;
    private XYChart.Data<Number, Number> item;

    public abstract long getLowerTimeExtent();

    public abstract long getUpperTimeExtent();

    public abstract int getLowerDisplayPos();

    public abstract int getUpperDisplayPos();

    public abstract int getCount();

    public abstract int getSelectedCount();

    public abstract boolean anyNodesSelected();

    public Interaction getInteraction() {
        return interaction;
    }

    public void setInteraction(final Interaction interaction) {
        this.interaction = interaction;
    }

    public void setNodeItem(final XYChart.Data<Number, Number> item) {
        this.item = item;
    }

    public XYChart.Data<Number, Number> getNodeItem() {
        return item;
    }

    @Override
    public String toString() {
        return getClass().getName() + " uuid: " + System.identityHashCode(this) + " LB: " + getLowerTimeExtent()
                + " UB: " + getUpperTimeExtent() + " Count: " + getCount();
    }
}
