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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.flow;

/**
 *
 * @author algol
 */
public class NodePair implements Comparable<NodePair> {

    public final int end1;
    public final int end2;

    public NodePair(final int end1, final int end2) {
        this.end1 = end1;
        this.end2 = end2;
    }

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
    public int hashCode() {
        return end1 ^ end2;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final NodePair other = (NodePair) obj;

        return this.end1 == other.end1 && this.end2 == other.end2;
    }

    @Override
    public String toString() {
        return "NodePair{" + "end1=" + end1 + ", end2=" + end2 + '}';
    }
}
