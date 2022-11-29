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
package au.gov.asd.tac.constellation.graph.utilities.wrapper;

/**
 *
 * @author capella
 */
public class GraphStep {

    private final GraphTransaction transaction;
    private final GraphVertex source;
    private final GraphVertex destination;
    private final GraphDirection direction;

    public GraphStep(GraphTransaction transaction, GraphVertex source, GraphVertex destination, GraphDirection direction) {
        this.transaction = transaction;
        this.source = source;
        this.destination = destination;
        this.direction = this.destination.equals(this.source) ? GraphDirection.LOOPBACK : direction;
    }

    public GraphTransaction getTransaction() {
        return transaction;
    }

    public GraphVertex getSourceVertex() {
        return source;
    }

    public GraphVertex getDestinationVertex() {
        return destination;
    }

    public GraphDirection getDirection() {
        return direction;
    }
}
