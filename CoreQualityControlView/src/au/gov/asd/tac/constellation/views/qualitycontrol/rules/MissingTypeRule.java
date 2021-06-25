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
package au.gov.asd.tac.constellation.views.qualitycontrol.rules;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * A Rule object identifying null or empty types.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = QualityControlRule.class)
public class MissingTypeRule extends QualityControlRule {

    private static final String NAME = "Missing type";
    private static final String DESCRIPTION
            = "This rule is used to identify nodes which have no type "
            + "specified. Nodes without types might represent valid data, but "
            + "they could also indicate errors in the data.";
    private static final int RISK = 90;

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

    @Override
    protected boolean executeRule(final GraphReadMethods graph, final int vertexId) {
        final int typeAttr = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        if (typeAttr != Graph.NOT_FOUND) {
            final String type = graph.getStringValue(typeAttr, vertexId);
            return StringUtils.isBlank(type);
        }
        return typeAttr == Graph.NOT_FOUND || graph.getObjectValue(typeAttr, vertexId) == null;
    }
}
