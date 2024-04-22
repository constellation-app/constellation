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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap;

import au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.util.Resizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author algol
 */
public class PartitionQueue {

    private int level;
    private int numNonTrivialModules;
    private double flow;
    private double nonTrivialFlow;
    private boolean skip;
    private double indexCodelength; // Consolidated
    private double leafCodelength; // Consolidated
    private double moduleCodelength; // Left to improve on next level

    private List<NodeBase> queue;

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

    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public int getNumNonTrivialModules() {
        return numNonTrivialModules;
    }

    public void setNumNonTrivialModules(final int numNonTrivialModules) {
        this.numNonTrivialModules = numNonTrivialModules;
    }

    public double getFlow() {
        return flow;
    }

    public void setFlow(final double flow) {
        this.flow = flow;
    }

    public double getNonTrivialFlow() {
        return nonTrivialFlow;
    }

    public void setNonTrivialFlow(final double nonTrivialFlow) {
        this.nonTrivialFlow = nonTrivialFlow;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(final boolean skip) {
        this.skip = skip;
    }

    public double getIndexCodelength() {
        return indexCodelength;
    }

    public void setIndexCodelength(final double indexCodelength) {
        this.indexCodelength = indexCodelength;
    }

    public double getLeafCodelength() {
        return leafCodelength;
    }

    public void setLeafCodelength(final double leafCodelength) {
        this.leafCodelength = leafCodelength;
    }

    public double getModuleCodelength() {
        return moduleCodelength;
    }

    public void setModuleCodelength(final double moduleCodelength) {
        this.moduleCodelength = moduleCodelength;
    }

    public List<NodeBase> getQueue() {
        return Collections.unmodifiableList(queue);
    }

    public void setQueue(final List<NodeBase> queue) {
        this.queue = queue;
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

        final List<NodeBase> tmp = queue;
        queue = other.queue;
        other.queue = tmp;
    }

    public int size() {
        return queue.size();
    }

    public void resize(final int size) {
        Resizer.resize(queue, size, null);
    }

    public void set(final int ix, final NodeBase node) {
        queue.set(ix, node);
    }

    public NodeBase get(final int ix) {
        return queue.get(ix);
    }
}
