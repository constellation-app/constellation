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
package au.gov.asd.tac.constellation.views.analyticview.results;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.analyticview.results.GraphResult.GraphScore;
import java.util.List;

/**
 * Stores {@link GraphScore} objects which represent a float score which applies
 * to the graph as a whole.
 *
 * @author cygnus_x-1
 */
public class GraphResult extends AnalyticResult<GraphScore> {

    @Override
    public void add(final GraphScore result) {
        this.result.put(result.id, result);
        this.metadata.put(result.getType(), String.valueOf(result.getScore()));
    }

    @Override
    public void setSelectionOnVisualisation(final GraphElementType elementType, final List<Integer> elementIds) {
        // Overridden method intentionally left blank
    }

    public static class GraphScore extends AnalyticData implements Comparable<GraphScore> {

        private final String type;
        private final float score;

        public GraphScore(final String identifier, final boolean isNull, final String type, final float score) {
            super(GraphElementType.GRAPH, 0, identifier, isNull);
            this.type = type;
            this.score = score;
        }

        /**
         * Get the type of score this GraphResult corresponds to.
         *
         * @return
         */
        public String getType() {
            return type;
        }

        /**
         * Get the score for the graph this GraphResult corresponds to.
         *
         * @return
         */
        public float getScore() {
            return score;
        }

        @Override
        public String toString() {
            return String.format("{%s;%s;%s}", getClass().getSimpleName(), id.identifier, score);
        }

        @Override
        public int compareTo(final GraphScore other) {
            return Float.compare(other.score, this.score);
        }
    }
}
