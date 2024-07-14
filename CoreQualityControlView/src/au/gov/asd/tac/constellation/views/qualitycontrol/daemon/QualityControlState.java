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
package au.gov.asd.tac.constellation.views.qualitycontrol.daemon;

import au.gov.asd.tac.constellation.views.qualitycontrol.QualityControlEvent;
import au.gov.asd.tac.constellation.views.qualitycontrol.rules.QualityControlRule;
import java.util.Collections;
import java.util.List;

/**
 * An object representing the state of the Quality Control View.
 *
 * @author algol
 */
public class QualityControlState {

    private final String graphId;
    private final List<QualityControlEvent> qualityControlEvents;
    private final List<QualityControlRule> registeredRules;

    public QualityControlState(final String graphId, final List<QualityControlEvent> qualityControlEvents, final List<QualityControlRule> registeredRules) {
        this.graphId = graphId;
        this.qualityControlEvents = Collections.unmodifiableList(qualityControlEvents);
        this.registeredRules = Collections.unmodifiableList(registeredRules);
    }

    public String getGraphId() {
        return graphId;
    }

    /**
     * A List of QualityControlEvent instances.
     * <p>
     * Each element of the list corresponds to a selected node in the current graph.
     *
     * @return A List of QualityControlEvent instances.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<QualityControlEvent> getQualityControlEvents() {
        return qualityControlEvents;
    }

    /**
     * The current quality control rules.
     *
     * @return The current quality control rules.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<QualityControlRule> getRegisteredRules() {
        return registeredRules;
    }

    /**
     * The highest quality control score (ie. the score of the lowest quality node) in the current list of quality
     * control events.
     *
     * @return The highest quality control score in the current list of quality control events, or null if there are no
     * quality control events.
     */
    public QualityControlEvent getHighestScoringEvent() {
        return qualityControlEvents.isEmpty() ? null : qualityControlEvents.get(0);
    }
}
