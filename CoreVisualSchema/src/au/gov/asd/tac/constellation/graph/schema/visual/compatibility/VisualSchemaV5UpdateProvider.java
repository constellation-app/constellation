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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VertexDecorators;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * This update ensures that the new pinned vertex attribute is set as the NE
 * vertex decorator. This will override the existing NE decorator if one exists
 *
 * @author antares
 */
@ServiceProvider(service = UpdateProvider.class)
public class VisualSchemaV5UpdateProvider extends SchemaUpdateProvider {

    public static final int SCHEMA_VERSION_THIS_UPDATE = 5;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return VisualSchemaV4UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(final StoreGraph graph) {
        final int decoratorsAttribute = VisualConcept.GraphAttribute.DECORATORS.get(graph);
        final int pinnedAttribute = VisualConcept.VertexAttribute.PINNED.ensure(graph);

        final VertexDecorators oldDecorators = graph.getObjectValue(decoratorsAttribute, 0);
        // create the new set of decorators by adding the pinned attribute as the NE decorator and retain all others
        final VertexDecorators newDecorators = new VertexDecorators(oldDecorators.getNorthWestDecoratorAttribute(),
                graph.getAttributeName(pinnedAttribute),
                oldDecorators.getSouthEastDecoratorAttribute(),
                oldDecorators.getSouthWestDecoratorAttribute()
        );
        graph.setObjectValue(decoratorsAttribute, 0, newDecorators);
    }
}
