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
package au.gov.asd.tac.constellation.graph.schema.analytic.concept;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.attribute.FloatObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerObjectAttributeDescription;
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
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = SchemaConcept.class)
public class SpatialConcept extends SchemaConcept {

    @Override
    public String getName() {
        return "Spatial";
    }

    @Override
    public Set<Class<? extends SchemaConcept>> getParents() {
        final Set<Class<? extends SchemaConcept>> parentSet = new HashSet<>();
        parentSet.add(AnalyticConcept.class);
        return Collections.unmodifiableSet(parentSet);
    }

    public static class VertexAttribute {

        public static final SchemaAttribute LATITUDE = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Latitude")
                .setDescription("The latitude of this node")
                .build();
        public static final SchemaAttribute LONGITUDE = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Longitude")
                .setDescription("The longitude of this node")
                .build();
        public static final SchemaAttribute ALTITUDE = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Altitude")
                .setDescription("The altitude of this node")
                .build();
        public static final SchemaAttribute SHAPE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Shape")
                .setDescription("A shape representing the location of this node")
                .build();
        public static final SchemaAttribute GEOHASH = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Geohash")
                .setDescription("A geohash representing the location of this node")
                .build();
        public static final SchemaAttribute CITY = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.City")
                .setDescription("The city in which this node is located")
                .build();
        public static final SchemaAttribute COUNTRY = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Country")
                .setDescription("The country in which this node is located")
                .setDecorator(true)
                .build();
        public static final SchemaAttribute LOCATION = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Location")
                .setDescription("The location of this node")
                .build();
        public static final SchemaAttribute PRECISION = new SchemaAttribute.Builder(GraphElementType.VERTEX, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Precision")
                .setDescription("The precision or exactness of the geolocation of this node (in km)")
                .build();
        public static final SchemaAttribute ACCURACY = new SchemaAttribute.Builder(GraphElementType.VERTEX, IntegerObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Accuracy")
                .setDescription("The accuracy or correctness of the geolocation of this node")
                .build();
        public static final SchemaAttribute TYPE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Type")
                .setDescription("The type of geospatial information on this node")
                .build();
        public static final SchemaAttribute SOURCE = new SchemaAttribute.Builder(GraphElementType.VERTEX, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Source")
                .setDescription("The source of geospatial information on this node")
                .build();
    }

    public static class TransactionAttribute {

        public static final SchemaAttribute LATITUDE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Latitude")
                .setDescription("The latitude of this transaction")
                .build();
        public static final SchemaAttribute LONGITUDE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Longitude")
                .setDescription("The longitude of this transaction")
                .build();
        public static final SchemaAttribute ALTITUDE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Altitude")
                .setDescription("The altitude of this transaction")
                .build();
        public static final SchemaAttribute SHAPE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Shape")
                .setDescription("A shape representing the location of this transaction")
                .build();
        public static final SchemaAttribute GEOHASH = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Geohash")
                .setDescription("A geohash representing the location of this transaction")
                .build();
        public static final SchemaAttribute CITY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.City")
                .setDescription("The city in which this transaction is located")
                .build();
        public static final SchemaAttribute COUNTRY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Country")
                .setDescription("The country in which this transaction is located")
                .build();
        public static final SchemaAttribute LOCATION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Location")
                .setDescription("The location of this transaction")
                .build();
        public static final SchemaAttribute PRECISION = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, FloatObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Precision")
                .setDescription("The precision or exactness of the geolocation of this transaction (in km)")
                .build();
        public static final SchemaAttribute ACCURACY = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, IntegerObjectAttributeDescription.ATTRIBUTE_NAME, "Geo.Accuracy")
                .setDescription("The accuracy or correctness of the geolocation of this transaction")
                .build();
        public static final SchemaAttribute TYPE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Type")
                .setDescription("The type of geospatial information on this transaction")
                .build();
        public static final SchemaAttribute SOURCE = new SchemaAttribute.Builder(GraphElementType.TRANSACTION, StringAttributeDescription.ATTRIBUTE_NAME, "Geo.Source")
                .setDescription("The source of geospatial information on this transaction")
                .build();
    }

    @Override
    public Collection<SchemaAttribute> getSchemaAttributes() {
        final List<SchemaAttribute> schemaAttributes = new ArrayList<>();
        schemaAttributes.add(VertexAttribute.LATITUDE);
        schemaAttributes.add(VertexAttribute.LONGITUDE);
        schemaAttributes.add(VertexAttribute.ALTITUDE);
        schemaAttributes.add(VertexAttribute.SHAPE);
        schemaAttributes.add(VertexAttribute.GEOHASH);
        schemaAttributes.add(VertexAttribute.CITY);
        schemaAttributes.add(VertexAttribute.COUNTRY);
        schemaAttributes.add(VertexAttribute.LOCATION);
        schemaAttributes.add(VertexAttribute.PRECISION);
        schemaAttributes.add(VertexAttribute.ACCURACY);
        schemaAttributes.add(VertexAttribute.TYPE);
        schemaAttributes.add(VertexAttribute.SOURCE);
        schemaAttributes.add(TransactionAttribute.LATITUDE);
        schemaAttributes.add(TransactionAttribute.LONGITUDE);
        schemaAttributes.add(TransactionAttribute.ALTITUDE);
        schemaAttributes.add(TransactionAttribute.SHAPE);
        schemaAttributes.add(TransactionAttribute.GEOHASH);
        schemaAttributes.add(TransactionAttribute.CITY);
        schemaAttributes.add(TransactionAttribute.COUNTRY);
        schemaAttributes.add(TransactionAttribute.LOCATION);
        schemaAttributes.add(TransactionAttribute.PRECISION);
        schemaAttributes.add(TransactionAttribute.ACCURACY);
        schemaAttributes.add(TransactionAttribute.TYPE);
        schemaAttributes.add(TransactionAttribute.SOURCE);
        return Collections.unmodifiableCollection(schemaAttributes);
    }
}
