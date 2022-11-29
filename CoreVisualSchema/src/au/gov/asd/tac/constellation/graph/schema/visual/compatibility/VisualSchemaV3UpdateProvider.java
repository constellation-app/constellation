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

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.SchemaFactoryUtilities;
import au.gov.asd.tac.constellation.graph.schema.visual.VisualSchemaFactory;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.graph.versioning.SchemaUpdateProvider;
import au.gov.asd.tac.constellation.graph.versioning.UpdateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * This update ensures that display mode, drawing mode and drawing directed
 * transactions attributes are added to the graph. These attributes were
 * previously part of the 'Visual State' attribute, but now exist as independent
 * graph attributes.
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = UpdateProvider.class)
public class VisualSchemaV3UpdateProvider extends SchemaUpdateProvider {

    static final int SCHEMA_VERSION_THIS_UPDATE = 3;

    @Override
    protected SchemaFactory getSchema() {
        return SchemaFactoryUtilities.getSchemaFactory(VisualSchemaFactory.VISUAL_SCHEMA_ID);
    }

    @Override
    public int getFromVersionNumber() {
        return VisualSchemaV2UpdateProvider.SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    public int getToVersionNumber() {
        return SCHEMA_VERSION_THIS_UPDATE;
    }

    @Override
    protected void schemaUpdate(StoreGraph graph) {
        VisualConcept.GraphAttribute.DISPLAY_MODE_3D.ensure(graph);
        VisualConcept.GraphAttribute.DRAWING_MODE.ensure(graph);
        VisualConcept.GraphAttribute.DRAW_DIRECTED_TRANSACTIONS.ensure(graph);
        final int oldCameraAttribute = graph.getAttribute(GraphElementType.META, "visual_state");
        final int cameraAttribute = VisualConcept.GraphAttribute.CAMERA.ensure(graph);
        if (oldCameraAttribute != Graph.NOT_FOUND) {
            graph.setObjectValue(cameraAttribute, 0, graph.getObjectValue(oldCameraAttribute, 0));
            graph.removeAttribute(oldCameraAttribute);
        }
    }
}
