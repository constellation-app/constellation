/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.views.analyticview.results.ClusterResult.ClusterData;

/**
 * Stores {@link CLusterData} objects mapping graph elements to the id of the
 * cluster they belong to.
 *
 * @author cygnus_x-1
 */
public class ClusterResult extends AnalyticResult<ClusterData> {

    public static class ClusterData extends AnalyticData implements Comparable<ClusterData> {

        private final int clusterNumber;

        public ClusterData(final GraphElementType elementType, final int elementId,
                final String identifier, final boolean isNull, final int clusterNumber) {
            super(elementType, elementId, identifier, isNull);
            this.clusterNumber = clusterNumber;
        }

        /**
         * Get the cluster number for the graph element this ClusterData
         * corresponds to.
         *
         * @return
         */
        public int getClusterNumber() {
            return clusterNumber;
        }

        @Override
        public String toString() {
            return String.format("{%s;%s;%s}", getClass().getSimpleName(), id.identifier, clusterNumber);
        }

        @Override
        public int compareTo(final ClusterData other) {
            return Integer.compare(other.clusterNumber, this.clusterNumber);
        }
    }

    public boolean getIgnoreTransactions() {
        return true;
    }
}
