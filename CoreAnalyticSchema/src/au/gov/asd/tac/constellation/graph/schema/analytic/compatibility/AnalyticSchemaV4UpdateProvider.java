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

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import java.util.HashMap;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 * Upgrade Vertex Types that have changed.
 * <p>
 * The detection regex and validation regex was reviewed and improved.
 *
 * @author arcturus
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV4UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 4;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV3UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        final int typeAttribute = AnalyticConcept.VertexAttribute.TYPE.get(graph);

        final Map<String, SchemaVertexType> typesToUpgrade = new HashMap<>();
        typesToUpgrade.put(AnalyticConcept.VertexType.MD5.getName(), AnalyticConcept.VertexType.MD5);
        typesToUpgrade.put(AnalyticConcept.VertexType.SHA1.getName(), AnalyticConcept.VertexType.SHA1);
        typesToUpgrade.put(AnalyticConcept.VertexType.SHA256.getName(), AnalyticConcept.VertexType.SHA256);
        typesToUpgrade.put(AnalyticConcept.VertexType.COUNTRY.getName(), AnalyticConcept.VertexType.COUNTRY);
        typesToUpgrade.put(AnalyticConcept.VertexType.GEOHASH.getName(), AnalyticConcept.VertexType.GEOHASH);
        typesToUpgrade.put(AnalyticConcept.VertexType.MGRS.getName(), AnalyticConcept.VertexType.MGRS);
        typesToUpgrade.put(AnalyticConcept.VertexType.IPV4.getName(), AnalyticConcept.VertexType.IPV4);
        typesToUpgrade.put(AnalyticConcept.VertexType.IPV6.getName(), AnalyticConcept.VertexType.IPV6);
        typesToUpgrade.put(AnalyticConcept.VertexType.EMAIL_ADDRESS.getName(), AnalyticConcept.VertexType.EMAIL_ADDRESS);
        typesToUpgrade.put(AnalyticConcept.VertexType.HOST_NAME.getName(), AnalyticConcept.VertexType.HOST_NAME);
        typesToUpgrade.put(AnalyticConcept.VertexType.URL.getName(), AnalyticConcept.VertexType.URL);
        typesToUpgrade.put(AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER.getName(), AnalyticConcept.VertexType.TELEPHONE_IDENTIFIER);

        for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
            final int vertexId = graph.getVertex(vertex);
            final SchemaVertexType oldType = graph.getObjectValue(typeAttribute, vertexId);

            if (oldType != null && typesToUpgrade.containsKey(oldType.getName())) {
                graph.setObjectValue(typeAttribute, vertexId, typesToUpgrade.get(oldType.getName()));
            }
        }
    }
}
