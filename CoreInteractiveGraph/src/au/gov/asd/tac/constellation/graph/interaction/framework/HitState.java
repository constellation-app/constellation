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
package au.gov.asd.tac.constellation.graph.interaction.framework;

import au.gov.asd.tac.constellation.graph.GraphElementType;

/**
 * An object to hold information about the results of a hit test.
 * <p>
 * Note that this object is independent of any particular implementation or
 * usage of hit testing.
 *
 *
 * @author twilight_sparkle
 */
public class HitState {

    /**
     * Constants denoting the type of element that was hit. These are matched to
     * corresponding {@link GraphElementType} constants.
     */
    public enum HitType {

        VERTEX(GraphElementType.VERTEX),
        TRANSACTION(GraphElementType.TRANSACTION),
        NO_ELEMENT(GraphElementType.GRAPH),;

        private HitType(final GraphElementType elementType) {
            this.elementType = elementType;
        }

        public final GraphElementType elementType;

    }

    private int currentHitId;
    private HitType currentHitType;

    /**
     * Create a new HitState.
     * <p>
     * The new state will be recording that no element has been hit.
     */
    public HitState() {
        currentHitId = -1;
        currentHitType = HitType.NO_ELEMENT;
    }

    /**
     * Create a new HitState with the same data as the specified hit state.
     *
     * @param other The HitState to take the information about which (if any)
     * element was hit.
     */
    public HitState(HitState other) {
        currentHitId = other.currentHitId;
        currentHitType = other.currentHitType;
    }

    /**
     * Get the ID of the currently hit element.
     *
     * @return The ID of the currently hit element, or -1 if no element has been
     * hit.
     */
    public int getCurrentHitId() {
        return currentHitId;
    }

    /**
     * Get the type of the currently hit element
     *
     * @return The {@link HitType} of the currently hit element, or
     * {@link HitType#NO_ELEMENT} if no element has been hit.
     */
    public HitType getCurrentHitType() {
        return currentHitType;
    }

    /**
     * Set the ID of the currently hit element.
     *
     * @param currentHitId The ID to set.
     */
    public void setCurrentHitId(final int currentHitId) {
        this.currentHitId = currentHitId;
    }

    /**
     * Set the type of the currently hit element.
     *
     * @param currentHitType The {@link HitType} to set.
     */
    public void setCurrentHitType(final HitType currentHitType) {
        this.currentHitType = currentHitType;
    }
}
