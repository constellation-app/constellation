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
package au.gov.asd.tac.constellation.graph;

import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
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
 * @author sol695510
 */
@ServiceProvider(service = SchemaConcept.class)
public class NotesConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Layers";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(SchemaConcept.ConstellationViewsConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class GraphAttribute {

        private GraphAttribute() {
            throw new IllegalStateException("Concept class");
        }

        public static final SchemaAttribute LAYER_MASK_SELECTED = new SchemaAttribute.Builder(GraphElementType.GRAPH, IntegerAttributeDescription.ATTRIBUTE_NAME, "layer_bitmask_selected")
                .setDescription("The layers currently enabled for display")
                .setDefaultValue(1)
                .create()
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(GraphAttribute.LAYER_MASK_SELECTED);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}
