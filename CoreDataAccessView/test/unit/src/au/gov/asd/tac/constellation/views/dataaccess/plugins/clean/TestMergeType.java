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
import org.openide.util.lookup.ServiceProvider;

/**
 * This class is test implementation of {@link MergeNodeType}. It is used as a
 * stub for the tests in {@link MergeNodesPluginNGTest}.
 * <p/>
 * This is needed because these classes are injected using the {@link Lookup}
 * pattern which makes it basically impossible to inject a mock.
 *
 * @author formalhaunt
 */
@ServiceProvider(service = MergeNodeType.class)
public class TestMergeType implements MergeNodeType {

    public static final String NAME = "Test Name";

    public static final int MERGE_EXCEPTION_THRESHOLD = 128;
    public static final int MERGE_SUCCESS_THRESHOLD = 512;

    public static final Map<Integer, Set<Integer>> NODES_TO_MERGE = Map.of(1, Set.of(1, 2, 3));

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void updateParameters(final Map<String, PluginParameter<?>> parameters) {
        //Do nothing
    }

    @Override
    public Map<Integer, Set<Integer>> getNodesToMerge(GraphWriteMethods graph, Comparator<String> leadVertexChooser, int threshold, boolean selectedOnly) throws MergeNodeType.MergeException {
        // The threshold is used as a hack to pass a flag so that the code knows what to do
        // when the method is called. If a threshold is not supported then an exception
        // is thown.
        if (threshold == MERGE_EXCEPTION_THRESHOLD) {
            throw new MergeNodeType.MergeException("Some Error", new RuntimeException());
        } else if (threshold == MERGE_SUCCESS_THRESHOLD) {
            return NODES_TO_MERGE;
        }

        throw new RuntimeException("This is a special test class and you failed to call it correctly.");
    }
}
