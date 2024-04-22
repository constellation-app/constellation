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
package au.gov.asd.tac.constellation.graph.monitor;

/**
 * A GraphChangeListener is registered against a particular graph and receives
 * GraphChangeEvents whenever that graph is changed in any way.
 *
 * @author sirius
 */
public interface GraphChangeListener {

    /**
     * This method is called whenever a graph that this listener is registered
     * against changes in any way.
     *
     * @param event the {@link GraphChangeEvent} describing the change on the
     * graph.
     */
    public void graphChanged(final GraphChangeEvent event);
}
