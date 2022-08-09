/*
 * Copyright 2010-2022 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.analytic.AnalyticSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Update provider to ensure that previous attributes using the spelling "colour" are updated
 * to the new spelling "color" to be consistent throughout the application
 *
 * @author Delphinus8821
 */
@ServiceProvider(service = UpdateProvider.class)
public class AnalyticSchemaV5UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 5;

    private static final String K_TRUSS_COLOUR = "Cluster.KTruss.Colour";
    private static final String HIERARCHICAL_COLOUR = "Cluster.Hierarchical.Colour";
    private static final String INFOMAP_COLOUR = "Cluster.Infomap.Colour";

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(AnalyticSchemaFactory.ANALYTIC_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return AnalyticSchemaV4UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {

        // Retrieve the attributes IDs of the previous attributes
        final int oldKTrussColourAttributeId = graph.getAttribute(GraphElementType.VERTEX, K_TRUSS_COLOUR);
        final int oldHierarchicalColourAttributeId = graph.getAttribute(GraphElementType.VERTEX, HIERARCHICAL_COLOUR);
        final int oldInformapColourAttributeId = graph.getAttribute(GraphElementType.VERTEX, INFOMAP_COLOUR);

        // retrieve the new values for the graph_labels_bottom and set it to the new values
        final int newKTrussColorAttributeId = ClusteringConcept.VertexAttribute.K_TRUSS_COLOR.ensure(graph);
        final String kTrussColorValue = graph.getStringValue(oldKTrussColourAttributeId, 0);
        graph.setStringValue(newKTrussColorAttributeId, 0, kTrussColorValue);

        // retrieve the new values for the graph_labels_top and set it to the new values
        final int newHierarchicalColorAttributeId = ClusteringConcept.VertexAttribute.HIERARCHICAL_COLOR.ensure(graph);
        final String hierarchicalColorValue = graph.getStringValue(oldHierarchicalColourAttributeId, 0);
        graph.setStringValue(newHierarchicalColorAttributeId, 0, hierarchicalColorValue);

        // retrieve the new values for the transaction_labels and set it to the new values
        final int newInfomapColorAttributeId = ClusteringConcept.VertexAttribute.INFOMAP_COLOR.ensure(graph);
        final String infomapColorValue = graph.getStringValue(oldInformapColourAttributeId, 0);
        graph.setStringValue(newInfomapColorAttributeId, 0, infomapColorValue);

        // remove the old attributes
        graph.removeAttribute(oldKTrussColourAttributeId);
        graph.removeAttribute(oldHierarchicalColourAttributeId);
        graph.removeAttribute(oldInformapColourAttributeId);
    }

}
