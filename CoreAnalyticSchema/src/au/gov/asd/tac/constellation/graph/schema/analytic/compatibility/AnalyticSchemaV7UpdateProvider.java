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
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.TemporalConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Update provider for correcting temporal transaction attributes that were misclassified as vertex
 *
 * @author antares
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV7UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 7;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV6UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        final int startTimeAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, "StartTime");       
        if (startTimeAttributeId != Graph.NOT_FOUND) {
            final int newStartTimeAttributeId = TemporalConcept.TransactionAttribute.START_TIME.ensure(graph);
            // we only want to proceed with changes if the old attribute comes up as a different attribute
            // else we may accidentally delete the attribute
            if (startTimeAttributeId != newStartTimeAttributeId) {
                for (int transaction = 0; transaction < graph.getTransactionCount(); transaction++) {
                    final int transactionId = graph.getTransaction(transaction);
                    
                    final String startTime = graph.getStringValue(startTimeAttributeId, transactionId);
                    if (startTime != null) {
                        graph.setStringValue(newStartTimeAttributeId, transactionId, startTime);
                    }
                }
                graph.removeAttribute(startTimeAttributeId);
            }
        }
        
        final int endTimeAttributeId = graph.getAttribute(GraphElementType.TRANSACTION, "EndTime");
        if (endTimeAttributeId != Graph.NOT_FOUND) {
            final int newEndTimeAttributeId = TemporalConcept.TransactionAttribute.END_TIME.ensure(graph);
            // we only want to proceed with changes if the old attribute comes up as a different attribute
            // else we may accidentally delete the attribute
            if (endTimeAttributeId != newEndTimeAttributeId) {
                for (int transaction = 0; transaction < graph.getTransactionCount(); transaction++) {
                    final int transactionId = graph.getTransaction(transaction);
                    
                    final String endTimeTime = graph.getStringValue(endTimeAttributeId, transactionId);
                    if (endTimeTime != null) {
                        graph.setStringValue(newEndTimeAttributeId, transactionId, endTimeTime);
                    }
                }
                graph.removeAttribute(endTimeAttributeId);
            }
        }
    }
}
