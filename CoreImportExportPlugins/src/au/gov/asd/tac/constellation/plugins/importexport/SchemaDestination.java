/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.WritableGraph;
import au.gov.asd.tac.constellation.graph.locking.DualGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;

/**
 * A SchemaDestination represents a new graph that can be created with a
 * specified schema and can act as the destination for imported graph elements.
 * In general, there will be a SchemaDestination created for each registered
 * schema in the application allowing the user to put imported data into a new
 * graph with their choice of schema.
 *
 * @author sirius
 */
public class SchemaDestination extends ImportDestination<SchemaFactory> {

    public SchemaDestination(final SchemaFactory destination) {
        super(destination);

        this.label = "New " + destination.getLabel();
    }

    @Override
    public Graph getGraph() {
        final SchemaFactory schemaFactory = getDestination();

        final Graph graph = new DualGraph(schemaFactory.createSchema());

        final WritableGraph wg = graph.getWritableGraphNow("New Graph", true);
        try {
            graph.getSchema().newGraph(wg);
        } finally {
            wg.commit();
        }

        return graph;
    }
}
