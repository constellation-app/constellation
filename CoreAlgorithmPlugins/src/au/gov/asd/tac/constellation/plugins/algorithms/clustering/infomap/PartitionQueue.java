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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Resizer;
import java.util.ArrayList;

/**
 *
 * @author algol
 */
public class PartitionQueue {

    public int level;
    public int numNonTrivialModules;
    public double flow;
    public double nonTrivialFlow;
    public boolean skip;
    public double indexCodelength; // Consolidated
    public double leafCodelength; // Consolidated
    public double moduleCodelength; // Left to improve on next level

    private ArrayList<NodeBase> queue;

    public PartitionQueue() {
        level = 1;
        numNonTrivialModules = 0;
        flow = 0;
        nonTrivialFlow = 0;
        skip = false;
        indexCodelength = 0;
        leafCodelength = 0;
        moduleCodelength = 0;

        queue = new ArrayList<>();
    }

    public void swap(final PartitionQueue other) {
        int ti;
        double td;
        boolean tb;

        ti = level;
        level = other.level;
        other.level = ti;

        ti = numNonTrivialModules;
        numNonTrivialModules = other.numNonTrivialModules;
        other.numNonTrivialModules = ti;

        td = flow;
        flow = other.flow;
        other.flow = td;

        td = nonTrivialFlow;
        nonTrivialFlow = other.nonTrivialFlow;
        other.nonTrivialFlow = td;

        tb = skip;
        skip = other.skip;
        other.skip = tb;

        td = indexCodelength;
        indexCodelength = other.indexCodelength;
        other.indexCodelength = td;

        td = leafCodelength;
        leafCodelength = other.leafCodelength;
        other.leafCodelength = td;

        td = moduleCodelength;
        moduleCodelength = other.moduleCodelength;
        other.moduleCodelength = td;

        final ArrayList<NodeBase> tmp = queue;
        queue = other.queue;
        other.queue = tmp;
    }

    public int size() {
        return queue.size();
    }

    public void resize(int size) {
        Resizer.resizeNodeBase(queue, size);
    }

    public void set(final int ix, final NodeBase node) {
        queue.set(ix, node);
    }

    public NodeBase get(final int ix) {
        return queue.get(ix);
    }
}
