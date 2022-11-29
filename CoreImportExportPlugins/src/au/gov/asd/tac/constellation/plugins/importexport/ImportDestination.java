/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.graph.Graph;

/**
 * An ImportDestination represents a place to which imported graph elements can
 * be added.
 *
 * @author sirius
 * @param <D>
 */
public abstract class ImportDestination<D> {

    private final D destination;
    protected String label;

    protected ImportDestination(final D destination) {
        this.destination = destination;
        this.label = String.valueOf(destination);
    }

    public D getDestination() {
        return destination;
    }

    public abstract Graph getGraph();

    @Override
    public String toString() {
        return label;
    }
}
