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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * This update ensures that the directed transaction attribute is added to the
 * graph. 
 *
 * @author antares
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV3UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 3;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV2UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        // update transaction directed attribute
        final int transactionDirectedAttribute = VisualConcept.TransactionAttribute.DIRECTED.ensure(graph);
        for (int transactionPosition = 0; transactionPosition < graph.getTransactionCount(); transactionPosition++) {
            final int transactionId = graph.getTransaction(transactionPosition);
            final boolean transactionDirected = graph.getTransactionDirection(transactionId) != Graph.UNDIRECTED;
            graph.setBooleanValue(transactionDirectedAttribute, transactionId, transactionDirected);
        }

    }
}
