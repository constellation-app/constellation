/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;

/**
 * A Rule is a class defining a test to be run by the Quality Control View.
 *
 * @author cygnus_x-1
 */
public abstract class QualityControlRule {

    protected Set<Integer> results;

    /**
     * Construct a Rule.
     */
    public QualityControlRule() {
        results = new HashSet<>();
    }

    /**
     * Get the results of the Rule, consisting of a set of vertex id's for any
     * vertices which matched this Rule.
     *
     * @return A {@link Set} or {@link Integer} objects representing the id's of
     * any vertices who match this Rule.
     */
    public Set<Integer> getResults() {
        return Collections.unmodifiableSet(results);
    }

    /**
     * Clear the results of this Rule.
     * <p>
     * Rules are found using {@link Lookup}, which caches instances of
     * registered classes, meaning the same rules will be reused. Therefore, the
     * results of any previous runs must be cleared before re-running this Rule.
     */
    public void clearResults() {
        results.clear();
    }

    /**
     * Check all specified nodes against this Rule.
     *
     * @param graph The graph to execute the rule on.
     * @param vertexIds The list of vertex ids to execute the rule on.
     */
    public void executeRule(final GraphReadMethods graph, final List<Integer> vertexIds) {
        vertexIds.forEach(vxId -> {
            if (this.executeRule(graph, vxId)) {
                results.add(vxId);
            }
        });
    }

    /**
     * Get the name of this Rule.
     *
     * @return A {@link String} representing the name of this Rule.
     */
    public abstract String getName();

    /**
     * Get a description of this rule for presentation to the user.
     *
     * @return A {@link String} description of this Rule.
     */
    public abstract String getDescription();

    /**
     * Get the quality of the given vertex as identified by this Rule, where
     * higher scores indicate lesser quality.
     *
     * @param vertexId An {@link Integer} object representing the id of a
     * vertex.
     * @return An integer value representing the quality of the specified
     * vertex.
     */
    public abstract int getQuality(final int vertexId);

    /**
     * Execute the logic of this Rule against a single vertex and return a
     * {@link Boolean} object representing whether the vertex passed or not.
     *
     * @param graph A {@link GraphReadMethods} on which to execute this Rule.
     * @param vertexId The id of the vertex to on which to execute this Rule.
     * @return True if the vertex matched this QualityControlRule, false
     * otherwise.
     */
    protected abstract boolean executeRule(final GraphReadMethods graph, final int vertexId);
}
