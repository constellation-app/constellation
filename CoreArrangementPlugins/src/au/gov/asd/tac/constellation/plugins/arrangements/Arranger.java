/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;

/**
 * interface for all graph arrangements
 *
 * @author algol
 */
public interface Arranger {

    /**
     * Arrange a graph.
     *
     * @param wg The Graph to arrange.
     *
     * @throws InterruptedException If the user cancels the task.
     */
    void arrange(GraphWriteMethods wg) throws InterruptedException;

    /**
     * Specify whether the mean position of the nodes should be maintained.
     *
     * @param b If true, the mean position of the nodes will be maintained.
     */
    void setMaintainMean(boolean b);
}
