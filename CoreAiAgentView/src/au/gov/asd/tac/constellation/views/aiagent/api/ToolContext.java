/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.aiagent.api;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 * What a tool can see of the environment it is running in.
 *
 * @author constellation
 */
public interface ToolContext {

    /**
     * The active graph at the time the tool runs (not when it was proposed).
     *
     * @return the active graph, or null if there is none.
     */
    Graph getActiveGraph();

    /**
     * @return true if the user has stopped the run; long running tools should
     * check this and give up early.
     */
    boolean isCancelled();

    /**
     * Report progress to the user interface.
     *
     * @param message a short progress message.
     */
    void progress(String message);

    /**
     * @return the maximum number of characters a tool result should contain.
     */
    int getMaxResultChars();
}
