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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexType;
import au.gov.asd.tac.constellation.graph.schema.type.SchemaVertexTypeUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * A Rule for identifying nodes that are not known by the Schema of the Graph.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = QualityControlRule.class)
public class UnknownTypeRule extends QualityControlRule {

    private static final String NAME = "Unknown type";
    private static final String DESCRIPTION
            = """
              This rule is used to identify nodes which have the 'Unknown' type.
              Nodes without specific types might represent valid entities, but they could also indicate errors in the data.""";
    private static final int RISK = 20;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public int getQuality(final int vertexId) {
        return RISK;
    }

    /**
     * ExecuteRule will return true when the vertex type exists and the type is 'Unknown'. False will be returned when
     * the type is null or when the type is not 'Unknown'
     *
     * @param graph the readable graph
     * @param vertexId the vertex to check against the rule
     * @return boolean true if it matches the rule, false otherwise.
     */
    @Override
    protected boolean executeRule(final GraphReadMethods graph, final int vertexId) {
        final int typeAttribute = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        if (typeAttribute != Graph.NOT_FOUND) {
            final SchemaVertexType type = graph.getObjectValue(typeAttribute, vertexId);
            return type != null && SchemaVertexTypeUtilities.getDefaultType().equals(type);
        }
        return false;
    }
}
