/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.layers.state;

import au.gov.asd.tac.constellation.graph.GraphElementType;
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
 * Attribute provider for attributes specific to the Layers View.
 *
 * @author aldebaran30701
 */
@ServiceProvider(service = SchemaConcept.class)
public class LayersViewConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Layers View";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(SchemaConcept.ConstellationViewsConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class MetaAttribute {

        private MetaAttribute() {
            throw new IllegalStateException("Concept class");
        }

        public static final SchemaAttribute LAYERS_VIEW_STATE = new SchemaAttribute.Builder(GraphElementType.META, LayersViewStateAttributeDescription.ATTRIBUTE_NAME, "layers_view_state")
                .setDescription("The current state of the layers view with relation to the active graph")
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(MetaAttribute.LAYERS_VIEW_STATE);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}
