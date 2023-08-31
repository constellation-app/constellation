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
package au.gov.asd.tac.constellation.views.find.state;

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
 * This class is used to create a MetaAttribute for the find view. It is used to
 * contain a FindResultsList that contains a list of all results that are found
 * from the basicFindPlugin
 *
 * @author Atlas139mkm
 */
@ServiceProvider(service = SchemaConcept.class)
public class FindViewConcept extends SchemaConcept {

    public static final String NAME = "findview_state";

    @Override
    public String getName() {
        return ("Find View");
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(ConstellationViewsConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class MetaAttribute {
        public static final SchemaAttribute FINDVIEW_STATE = new SchemaAttribute.Builder(GraphElementType.META, NAME, NAME)
                .setDescription("The current list of found graph elements in realation to this graph")
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> graphAttributes = new ArrayList<>();
        graphAttributes.add(MetaAttribute.FINDVIEW_STATE);
        return Collections.unmodifiableCollection(graphAttributes);
    }

}
