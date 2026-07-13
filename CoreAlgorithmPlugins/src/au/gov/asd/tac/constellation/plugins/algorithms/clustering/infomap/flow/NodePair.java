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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow;

/**
 *
 * @author algol
 */
public record NodePair(int end1, int end2) implements Comparable<NodePair> {

    @Override
    public int compareTo(final NodePair other) {
        if (end1 < other.end1) {
            return -1;
        }
        if (end1 > other.end1) {
            return 1;
        }
        if (end2 < other.end2) {
            return -1;
        }
        if (end2 > other.end2) {
            return 1;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "NodePair{" + "end1=" + end1 + ", end2=" + end2 + '}';
    }
}
