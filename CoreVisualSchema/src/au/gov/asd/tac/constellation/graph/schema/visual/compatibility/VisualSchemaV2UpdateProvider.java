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
package au.gov.asd.tac.constellation.graph.schema.visual.compatibility;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import org.openide.util.lookup.ServiceProvider;

/**
 * This update ensures that icons from the updated icon set (deployed in
 * November 2016) are used.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = UpdateProvider.class)
public class VisualSchemaV2UpdateProvider extends SchemaUpdateProvider {

    static final int SCHEMA_VERSION_THIS_UPDATE = 2;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return VisualSchemaV1UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(StoreGraph graph) {
        final int backgroundIconAttribute = VisualConcept.VertexAttribute.BACKGROUND_ICON.ensure(graph);

        graph.updateAttributeDefaultValue(backgroundIconAttribute, "Background.Flat Square");

        for (int vertexPosition = 0; vertexPosition < graph.getVertexCount(); vertexPosition++) {
            final int vertexId = graph.getVertex(vertexPosition);
            final ConstellationIcon vertexBackgroundIcon = graph.getObjectValue(backgroundIconAttribute, vertexId);
            switch (vertexBackgroundIcon.getExtendedName()) {
                case "Sphere":
                case "Background.Sphere":
                    graph.setObjectValue(backgroundIconAttribute, vertexId, IconManager.getIcon("Background.Round Circle"));
                    break;
                case "Square":
                case "Background.Square":
                    graph.setObjectValue(backgroundIconAttribute, vertexId, IconManager.getIcon("Background.Round Square"));
                    break;
                case "Circle":
                case "Background.Circle":
                    graph.setObjectValue(backgroundIconAttribute, vertexId, IconManager.getIcon("Background.Flat Circle"));
                    break;
                case "SoftSquare":
                case "Background.SoftSquare":
                case "Background.Soft Square":
                    graph.setObjectValue(backgroundIconAttribute, vertexId, IconManager.getIcon("Background.Flat Square"));
                    break;
                default:
                    break;
            }
        }
    }
}
