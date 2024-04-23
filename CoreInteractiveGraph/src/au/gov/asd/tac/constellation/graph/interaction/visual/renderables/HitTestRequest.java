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
package au.gov.asd.tac.constellation.graph.interaction.visual.renderables;

import au.gov.asd.tac.constellation.graph.interaction.framework.HitState;
import java.util.Collections;
import java.util.Queue;
import java.util.function.Consumer;

/**
 *
 * @author twilight_sparkle
 */
public class HitTestRequest {

    private final int x;
    private final int y;
    private final Consumer<HitState> followUpOperation;
    private final HitState hitState;
    private final Queue<HitState> notificationQueue;

    public HitTestRequest(final int x, final int y, final HitState hitState, final Queue<HitState> notificationQueue, final Consumer<HitState> followUpOperation) {
        this.x = x;
        this.y = y;
        this.followUpOperation = followUpOperation;
        this.hitState = hitState;
        this.notificationQueue = notificationQueue;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public HitState getHitState() {
        return hitState;
    }

    public Consumer<HitState> getFollowUpOperation() {
        return followUpOperation;
    }

    public Queue<HitState> getNotificationQueue() {
        return notificationQueue;
    }
}
