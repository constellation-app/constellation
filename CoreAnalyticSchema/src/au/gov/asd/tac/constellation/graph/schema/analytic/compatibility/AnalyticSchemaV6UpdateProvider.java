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
package au.gov.asd.tac.constellation.graph.schema.analytic.compatibility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Update provider for select types with updated detection and validation regexes
 *
 * @author antares
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV6UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 6;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV5UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        final int typeAttributeId = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        
        if (typeAttributeId != Graph.NOT_FOUND) {
            for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
                final int vertexId = graph.getVertex(vertex);
                
                final SchemaVertexType vertexType = graph.getObjectValue(typeAttributeId, vertexId);
                if (vertexType != null) {
                    switch (vertexType.getName()) {
                        case "IPv4 Address" -> graph.setObjectValue(typeAttributeId, vertexId, AnalyticConcept.VertexType.IPV4);
                        case "IPv6 Address" -> graph.setObjectValue(typeAttributeId, vertexId, AnalyticConcept.VertexType.IPV6);
                        case "Email" -> graph.setObjectValue(typeAttributeId, vertexId, AnalyticConcept.VertexType.EMAIL_ADDRESS);
                        case "MGRS" -> graph.setObjectValue(typeAttributeId, vertexId, AnalyticConcept.VertexType.MGRS);
                        default -> {
                            // Do nothing 
                        }
                    }
                }
            }
        }
    }
}
