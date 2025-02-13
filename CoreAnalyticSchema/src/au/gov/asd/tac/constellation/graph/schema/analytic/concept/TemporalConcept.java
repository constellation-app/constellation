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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.IntegerObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.StringAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.attribute.SchemaAttribute;
import au.gov.asd.tac.constellation.graph.schema.concept.SchemaConcept;
import au.gov.asd.tac.constellation.utilities.temporal.TemporalConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Temporal Concept
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class)
public class TemporalConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Temporal";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class VertexAttribute {

        public static final SchemaAttribute DATETIME = new SchemaAttribute.Builder(GraphElementType.VERTEX, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "DateTime")
                .setDescription("The datetime at which this node occurred")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute CREATED = new SchemaAttribute.Builder(GraphElementType.VERTEX, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "Created")
                .setDescription("The datetime at which this node was created")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute MODIFIED = new SchemaAttribute.Builder(GraphElementType.VERTEX, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "Modified")
                .setDescription("The datetime at which this node was last modified")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute FIRST_SEEN = new SchemaAttribute.Builder(GraphElementType.VERTEX, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "FirstSeen")
                .setDescription("The first time this node was seen")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute LAST_SEEN = new SchemaAttribute.Builder(GraphElementType.VERTEX, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "LastSeen")
                .setDescription("The last time this node was seen")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute START_TIME = new SchemaAttribute.Builder(GraphElementType.VERTEX, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "StartTime")
                .setDescription("The time at which this node becomes active or valid")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute END_TIME = new SchemaAttribute.Builder(GraphElementType.VERTEX, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "EndTime")
                .setDescription("The time at which this node ceases to be active or valid")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute DATETIME = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "DateTime")
                .setDescription("The datetime at which this transaction occurred")
                .build();
        public static final SchemaAttribute CREATED = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "Created")
                .setDescription("The datetime at which this transaction was created")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute MODIFIED = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "Modified")
                .setDescription("The datetime at which this transaction was last modified")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute FIRST_SEEN = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "FirstSeen")
                .setDescription("The first time this transaction was seen")
                .build();
        public static final SchemaAttribute LAST_SEEN = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "LastSeen")
                .setDescription("The last time this transaction was seen")
                .build();
        public static final SchemaAttribute DURATION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerObjectAttributeDescription.ATTRIBUTE_NAME, "Duration")
                .setDescription("The duration of the event (in seconds) represented by the transaction")
                .build();
        public static final SchemaAttribute DAY_BITMAP = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "DayBitMap")
                .setDescription("A bitmap of days this transaction represents")
                .build();
        public static final SchemaAttribute START_TIME = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "StartTime")
                .setDescription("The time at which this transaction becomes active or valid")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
        public static final SchemaAttribute END_TIME = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, ZonedDateTimeAttributeDescription.ATTRIBUTE_NAME, "EndTime")
                .setDescription("The time at which this transaction ceases to be active or valid")
                .setFormat(TemporalConstants.DATE_TIME_FULL_FORMAT)
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(VertexAttribute.DATETIME);
        schemaAttributes.add(VertexAttribute.CREATED);
        schemaAttributes.add(VertexAttribute.MODIFIED);
        schemaAttributes.add(VertexAttribute.FIRST_SEEN);
        schemaAttributes.add(VertexAttribute.LAST_SEEN);
        schemaAttributes.add(VertexAttribute.START_TIME);
        schemaAttributes.add(VertexAttribute.END_TIME);
        schemaAttributes.add(TransactionAttribute.DATETIME);
        schemaAttributes.add(TransactionAttribute.CREATED);
        schemaAttributes.add(TransactionAttribute.MODIFIED);
        schemaAttributes.add(TransactionAttribute.FIRST_SEEN);
        schemaAttributes.add(TransactionAttribute.LAST_SEEN);
        schemaAttributes.add(TransactionAttribute.DURATION);
        schemaAttributes.add(TransactionAttribute.DAY_BITMAP);
        schemaAttributes.add(TransactionAttribute.START_TIME);
        schemaAttributes.add(TransactionAttribute.END_TIME);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}
