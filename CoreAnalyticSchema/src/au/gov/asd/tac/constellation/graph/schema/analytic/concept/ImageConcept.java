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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Image Concept
 *
 * @author arcturus
 */
@ServiceProvider(service = SchemaConcept.class)
public class ImageConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class VertexAttribute {

        public static final SchemaAttribute CAMERA_MODEL = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Image.CameraModel")
                .setDescription("Camera Model")
                .build();
        public static final SchemaAttribute CAMERA_MAKE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Image.CameraMake")
                .setDescription("Camera Make")
                .build();

    }

    public static class TransactionAttribute {

    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(VertexAttribute.CAMERA_MAKE);
        schemaAttributes.add(VertexAttribute.CAMERA_MODEL);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}
