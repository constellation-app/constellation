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
package au.gov.asd.tac.constellation.utilities.visual;

import java.util.ArrayList;
import java.util.List;

/**
 * A VisualOperation describes an operation to be performed by a
 * {@link VisualProcessor}. They are added to a {@link VisualManager} which will
 * process and forward all received operations in each iteration of its
 * life-cycle.
 * <p>
 * Typically visual operations are created by components trying to inform a
 * {@link VisualProcessor} of visual updates, usually by returning one more
 * {@link VisualChange} objects from their
 * {@link #getVisualChanges getVisualChanges()} method.
 * <p>
 * A {@link VisualProcessor} may also create a number of special purpose
 * operations such as exporting to image or requesting a full refresh refresh.
 * These types of operations typically perform the bulk of their work inside the
 * {@link #apply apply()} method which is called inside the
 * {@link VisualManager} life-cycle immediately before any {@link VisualChange}
 * objects are sent to the processor.
 * <p>
 * Note that if an operation doesn't change any of the data but still needs the
 * processor to enter its update cycle, then
 * {@link #getVisualChanges getVisualChanges()} should return a list containing
 * a single change with the {@link VisualProperty#EXTERNAL_CHANGE} property.
 *
 * @author twilight_sparkle
 */
public interface VisualOperation extends Comparable<VisualOperation> {

    public enum VisualPriority {
        REFRESH_PRIORITY(0),
        SIGNIFY_IDLE_PRIORITY(10),
        ELEVATED_VISUAL_PRIORITY(20),
        DEFAULT_VISUAL_PRIORITY(30);

        private final int value;

        private VisualPriority(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Allows this VisualOperation to perform any context specific work, such as
     * communicate with given components of the VisualProcessor that owns this
     * operation.
     */
    public default void apply() {
    }

    /**
     * @return The list of Visual Changes that need to be made as a result of
     * this operation having been applied.
     */
    public List<VisualChange> getVisualChanges();

    /**
     * Retrieve the priority of this operation.
     * <p>
     * Operations with lower priority have their {@link #apply apply()} method
     * called earlier in each iteration of the manager's processing life-cycle.
     *
     * @return The priority of this operation.
     */
    default int getPriority() {
        return VisualPriority.DEFAULT_VISUAL_PRIORITY.getValue();
    }

    /**
     * Create a visual operation which applies this operation, followed by the
     * supplied operation.
     * <p>
     * The new operation will have the lower priority and return the union of
     * visual changes with respect to its two constituent operations.
     *
     * @param other The operation to execute after this operation in the new
     * operation.
     * @return The new operation comprised of this and the supplied operation.
     */
    default VisualOperation join(VisualOperation other) {
        return new VisualOperation() {

            @Override
            public int getPriority() {
                return Math.min(VisualOperation.this.getPriority(), other.getPriority());
            }

            @Override
            public void apply() {
                VisualOperation.this.apply();
                other.apply();
            }

            @Override
            public List<VisualChange> getVisualChanges() {
                final List<VisualChange> changes = new ArrayList<>();
                changes.addAll(VisualOperation.this.getVisualChanges());
                changes.addAll(other.getVisualChanges());
                return changes;
            }
        };
    }

    @Override
    default int compareTo(VisualOperation o) {
        return Integer.compare(getPriority(), o.getPriority());
    }
}
