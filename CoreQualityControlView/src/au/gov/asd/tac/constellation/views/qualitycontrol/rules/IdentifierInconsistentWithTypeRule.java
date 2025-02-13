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
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import java.util.regex.Pattern;
import org.openide.util.lookup.ServiceProvider;

/**
 * A Rule for identifying node identifier values which do not comply with their specified type.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = QualityControlRule.class)
public class IdentifierInconsistentWithTypeRule extends QualityControlRule {

    private static final String NAME = "Identifier inconsistent with type";
    private static final String DESCRIPTION
            = """
              This rule is used to identify nodes whose identifier do not appear to match their type.
              This check is performed using the regular expressions specified for each node type.
              Examples of when this rule will match are phone numbers that are too short or email addresses without the @ symbol.""";
    private static final int RISK = 30;

    public IdentifierInconsistentWithTypeRule() {
        // Method intentionally left blank
    }

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
        final int identifierAttribute = VisualConcept.VertexAttribute.IDENTIFIER.get(graph);
        final int typeAttribute = AnalyticConcept.VertexAttribute.TYPE.get(graph);
        if (identifierAttribute != Graph.NOT_FOUND && typeAttribute != Graph.NOT_FOUND) {
            final String identifier = graph.getStringValue(identifierAttribute, vertexId);
            final SchemaVertexType type = graph.getObjectValue(typeAttribute, vertexId);
            if (identifier != null && type != null && !SchemaVertexTypeUtilities.getDefaultType().equals(type)) {
                final Pattern validationRegex = type.getValidationRegex();
                return validationRegex != null && !validationRegex.matcher(identifier).matches();
            }
        }
        return false;
    }
}
