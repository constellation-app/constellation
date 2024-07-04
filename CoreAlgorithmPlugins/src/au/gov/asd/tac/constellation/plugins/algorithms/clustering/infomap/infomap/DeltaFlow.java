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
package au.gov.asd.tac.constellation.plugins.algorithms.clustering.infomap.infomap;

/**
 *
 * @author algol
 */
public class DeltaFlow {

    private int module;
    private double deltaExit;
    private double deltaEnter;

    public DeltaFlow() {
        module = 0;
        deltaExit = 0;
        deltaEnter = 0;
    }

    public DeltaFlow(final int module, final double deltaExit, final double deltaEnter) {
        this.module = module;
        this.deltaExit = deltaExit;
        this.deltaEnter = deltaEnter;
    }

    public DeltaFlow(final DeltaFlow other) {
        module = other.module;
        deltaExit = other.deltaExit;
        deltaEnter = other.deltaEnter;
    }

    public int getModule() {
        return module;
    }

    public void setModule(final int module) {
        this.module = module;
    }

    public double getDeltaExit() {
        return deltaExit;
    }

    public void setDeltaExit(final double deltaExit) {
        this.deltaExit = deltaExit;
    }

    public double getDeltaEnter() {
        return deltaEnter;
    }

    public void setDeltaEnter(final double deltaEnter) {
        this.deltaEnter = deltaEnter;
    }
}
