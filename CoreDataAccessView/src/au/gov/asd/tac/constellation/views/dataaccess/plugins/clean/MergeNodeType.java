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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author arcturus
 */
public interface MergeNodeType {

    public class MergeException extends Exception {

        public MergeException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String getName();

    public void updateParameters(final Map<String, PluginParameter<?>> parameters);

    public Map<Integer, Set<Integer>> getNodesToMerge(final GraphWriteMethods graph, final Comparator<String> leadVertexChooser, final int threshold, final boolean selectedOnly) throws MergeException;
}
