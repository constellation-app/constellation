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

import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.NewLineModel;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionBoxModel;
import au.gov.asd.tac.constellation.graph.interaction.visual.renderables.SelectionFreeformModel;
import au.gov.asd.tac.constellation.utilities.visual.VisualOperation;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor;
import java.util.Queue;

/**
 * An interface to allow various visual annotations to be added on top of the
 * standard display of a graph by a {@link VisualProcessor}.
 * <p>
 * This interface should be implemented by a {@link VisualProcessor}, or some
 * object in a one-to-one relationship with a processor. Typically this
 * interface's client will be an {@link InteractionEventHandler}.
 *
 * @author twilight_sparkle
 */
public interface VisualAnnotator {

    /**
     * Sets the {@link SelectionBoxModel} that should be displayed and returns a
     * {@link VisualOperation} that can be scheduled to push the changes to the
     * {@link VisualProcessor} with which this annotator is associated.
     * <p>
     * The selection box model is typically used to indicate a currently in
     * progress rectangle-based selection.
     *
     * @param model The {@link SelectionBoxModel} that should be displayed.
     * @return A {@link VisualOperation} that can be scheduled to visually
     * reflect the updated selection box model.
     */
    public VisualOperation setSelectionBoxModel(final SelectionBoxModel model);

    /**
     * Sets the {@link SelectionFreeformModel} that should be displayed and
     * returns a {@link VisualOperation} that can be scheduled to push the
     * changes to the {@link VisualProcessor} with which this annotator is
     * associated.
     * <p>
     * The selection freeform model is typically used to indicate a currently in
     * progress freeform selection.
     *
     * @param model The {@link SelectionFreeformModel} that should be displayed.
     * @return A {@link VisualOperation} that can be scheduled to visually
     * reflect the updated selection freeform model.
     */
    public VisualOperation setSelectionFreeformModel(final SelectionFreeformModel model);

    /**
     * Sets the {@link NewLineModel} that should be displayed and returns a
     * {@link VisualOperation} that can be scheduled to push the changes to the
     * {@link VisualProcessor} with which this annotator is associated.
     * <p>
     * The new line model is typically used to indicate that a user currently in
     * the process of creating or in some way editing an edge on the graph.
     *
     * @param model The {@link NewLineModel} that should be displayed.
     * @return A {@link VisualOperation} that can be scheduled to visually
     * reflect the updated new line model.
     */
    public VisualOperation setNewLineModel(final NewLineModel model);

    /**
     * Create a {@link VisualOperation} to either set or unset a visual cue that
     * the graph being interacted with is busy in some manner.
     *
     * @param isBusy Whether or not the graph is busy.
     * @return A {@link VisualOperation} to flag or unflag the graph as busy.
     */
    public VisualOperation flagBusy(final boolean isBusy);

    /**
     * Create a {@link VisualOperation} to perform a hit test at the given
     * coordinates, which are assumed to be the coordinates of the user's mouse
     * cursor.
     *
     * @param x The x-coordinate (in screen coordinates) to hit test
     * @param y The y-coordinate (in screen coordinates) to hit test
     * @param hitState The {@link HitState} which will have the results of the
     * hit test set on it.
     * @param notificationQueue A queue that will have the {@link HitState} with
     * the hit test results on it. The caller can wait on this queue for the hit
     * test to complete.
     * @return A {@link VisualOperation} to hit test the specified cursor
     * location.
     */
    public VisualOperation hitTestCursor(final int x, final int y, final HitState hitState, final Queue<HitState> notificationQueue);

    /**
     * Create a {@link VisualOperation} to perform a hit test at the given
     * coordinates.
     *
     * @param x The x-coordinate (in screen coordinates) to hit test
     * @param y The y-coordinate (in screen coordinates) to hit test
     * @param notificationQueue A queue that will have a {@link Hittate} with
     * the hit test results on it. The caller can wait on this queue for the hit
     * test to complete.
     * @return A {@link VisualOperation} to hit test the specified location.
     */
    public VisualOperation hitTestPoint(final int x, final int y, final Queue<HitState> notificationQueue);

    /**
     * Create a {@link VisualOperation} to enable or disable hit testing on the
     * {@link VisualProcessor}.
     * <p>
     * If hit testing is disabled on a given processor, then requests to
     * {@link #hitTestCursor} or {@link #hitTestPoint} can no longer be made
     * (they will either fail or return inaccurate results depending on the
     * implementation) until hit testing is re-enabled. Disabling hit testing
     * may increase performance of visual processors.
     *
     * @param enabled Whether or not to enable hit testing
     * @return A {@link VisualOperation} to enable or disable hit testing.
     */
    public VisualOperation setHitTestingEnabled(final boolean enabled);
}
