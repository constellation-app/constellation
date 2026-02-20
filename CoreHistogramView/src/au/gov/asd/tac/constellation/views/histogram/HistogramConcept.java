/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.histogram;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept.ConstellationViewsConcept;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * The HistogramAttributeProvider provides a collection of SchemaAttributes that
 * are needed for the histogram view to function.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class)
public class HistogramConcept extends SchemaConcept {

    public static final String HISTOGRAM_BIN_LABEL = "histogram_bin";

    @Override
    public String getName() {
        return "Histogram";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(ConstellationViewsConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class MetaAttribute {

        public static final SchemaAttribute HISTOGRAM_STATE = new SchemaAttribute.Builder(GraphElementType.META, "histogram_state", "histogram_state")
                .setDescription("The current state of the histogram with relation to this graph")
                .build();
    }

    public static class VertexAttribute {

        public static final SchemaAttribute HISTOGRAM_VERTEX_BIN = new SchemaAttribute.Builder(GraphElementType.VERTEX, ObjectAttributeDescription.ATTRIBUTE_NAME, HISTOGRAM_BIN_LABEL)
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute HISTOGRAM_TRANSACTION_BIN = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ObjectAttributeDescription.ATTRIBUTE_NAME, HISTOGRAM_BIN_LABEL)
                .build();
    }

    public static class EdgeAttribute {

        public static final SchemaAttribute HISTOGRAM_EDGE_BIN = new SchemaAttribute.Builder(GraphElementType.EDGE, ObjectAttributeDescription.ATTRIBUTE_NAME, HISTOGRAM_BIN_LABEL)
                .build();
    }

    public static class LinkAttribute {

        public static final SchemaAttribute HISTOGRAM_LINK_BIN = new SchemaAttribute.Builder(GraphElementType.LINK, ObjectAttributeDescription.ATTRIBUTE_NAME, HISTOGRAM_BIN_LABEL)
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> graphAttributes = new ArrayList<>();
        graphAttributes.add(MetaAttribute.HISTOGRAM_STATE);
        graphAttributes.add(VertexAttribute.HISTOGRAM_VERTEX_BIN);
        graphAttributes.add(TransactionAttribute.HISTOGRAM_TRANSACTION_BIN);
        graphAttributes.add(EdgeAttribute.HISTOGRAM_EDGE_BIN);
        graphAttributes.add(LinkAttribute.HISTOGRAM_LINK_BIN);
        return Collections.unmodifiableCollection(graphAttributes);
    }
}
