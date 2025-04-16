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
package au.gov.asd.tac.constellation.utilities.geospatial;

import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.Geometries;
import org.geotools.geometry.jts.GeometryBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geopkg.FeatureEntry;
import org.geotools.geopkg.GeoPackage;
import org.geotools.kml.v22.KML;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xsd.Encoder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.openide.util.Utilities;

/**
 * Shape Utilities.
 *
 * @author cygnus_x-1
 */
public class Shape {

    private static final Logger LOGGER = Logger.getLogger(Shape.class.getName());

    private static final String DEFAULT_GEOMETRY_ATTRIBUTE = "geometry";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String CENTROID_LATITUDE_ATTRIBUTE = "centreLat";
    private static final String CENTROID_LONGITUDE_ATTRIBUTE = "centreLon";
    private static final String RADIUS_ATTRIBUTE = "radius";
    
    private static final Pattern SOURCE_PATTERN = Pattern.compile("source");
    private static final Pattern DESTINATION_PATTERN = Pattern.compile("destination");
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("transaction");

    private static final List<Class<?>> GEOPACKAGE_ATTRIBUTE_TYPES = new ArrayList<>();

    static {
        GEOPACKAGE_ATTRIBUTE_TYPES.add(String.class);
        GEOPACKAGE_ATTRIBUTE_TYPES.add(Integer.class);
        GEOPACKAGE_ATTRIBUTE_TYPES.add(Long.class);
        GEOPACKAGE_ATTRIBUTE_TYPES.add(Float.class);
        GEOPACKAGE_ATTRIBUTE_TYPES.add(Double.class);
        GEOPACKAGE_ATTRIBUTE_TYPES.add(Date.class);
    }

    private static final String SHAPEFILE_GEOMETRY_ATTRIBUTE = "the_geom";
    private static final Map<Class<?>, Object> SHAPEFILE_ATTRIBUTE_TYPES = new HashMap<>();

    static {
        SHAPEFILE_ATTRIBUTE_TYPES.put(String.class, "");
        SHAPEFILE_ATTRIBUTE_TYPES.put(Integer.class, 0);
        SHAPEFILE_ATTRIBUTE_TYPES.put(Long.class, 0L);
        SHAPEFILE_ATTRIBUTE_TYPES.put(Float.class, 0F);
        SHAPEFILE_ATTRIBUTE_TYPES.put(Double.class, 0.0);
        SHAPEFILE_ATTRIBUTE_TYPES.put(Boolean.class, false);
        SHAPEFILE_ATTRIBUTE_TYPES.put(Date.class, Date.from(Instant.EPOCH));
    }

    private static final int GEOMETRY_PRECISION = 8;

    private static final String POLGYGON_SHAPE = "Polygon";

    public enum GeometryType {

        POINT("Point", Point.class),
        MULTI_POINT("MultiPoint", MultiPoint.class),
        LINE("LineString", LineString.class),
        MULTI_LINE("MultiLineString", MultiLineString.class),
        POLYGON(POLGYGON_SHAPE, Polygon.class),
        MULTI_POLYGON("MultiPolygon", MultiPolygon.class),
        BOX(POLGYGON_SHAPE, Polygon.class),
        CIRCLE(POLGYGON_SHAPE, Polygon.class),
        ELLIPSE(POLGYGON_SHAPE, Polygon.class);

        private final String geometryType;
        private final Class<? extends Geometry> geometryClass;

        private GeometryType(final String geometryType, final Class<? extends Geometry> geometryClass) {
            this.geometryType = geometryType;
            this.geometryClass = geometryClass;
        }

        public String getGeomertyType() {
            return geometryType;
        }

        public Class<? extends Geometry> getGeomertyClass() {
            return geometryClass;
        }
    }

    public enum SpatialReference {

        WGS84("WGS84", 4326),
        WGS84_WEB_MERCATOR("Web Mercator", 3857);

        private final String name;
        private final int srid;

        /**
         * {@code CRS.decode()} is known to have performance issues so we are
         * going to cache the output to reduce delays.
         */
        private static final Map<Integer, String> cache = new HashMap<>();

        private SpatialReference(final String name, final int srid) {
            this.name = name;
            this.srid = srid;
        }

        public String getName() {
            return name;
        }

        public int getSrid() {
            return srid;
        }

        public String getSrs() throws FactoryException {
            if (!cache.containsKey(srid)) {
                cache.put(srid, CRS.toSRS(CRS.decode("EPSG:" + srid)));
            }

            return cache.get(srid);
        }
    }

    /**
     * Check if a geojson string is valid by ensuring it represents a feature
     * collection containing at least one feature.
     *
     * @param geoJson a geojson string
     * @return true if the geojson is considered valid, false otherwise.
     */
    public static boolean isValidGeoJson(final String geoJson) {
        return geoJson.contains("\"type\":\"FeatureCollection\"") && geoJson.contains("\"features\":[");
    }

    /**
     * Construct geojson to represent a single shape given a list of
     * coordinates, a desired shape type and a unique identifier.
     *
     * @param uuid a unique identifier for the shape
     * @param type a type of shape
     * @param coordinates a list of coordinate tuples of the form (longitude,
     * latitude) from which to build the shape
     * @return a geojson string representing a single shape
     * @throws IOException there was a problem writing the generated shape
     */
    public static String generateShape(final String uuid, final GeometryType type, final List<Tuple<Double, Double>> coordinates) throws IOException {

        // build precision formatter
        final StringBuilder precisionPattern = new StringBuilder("#.");
        for (int decimalPlace = 0; decimalPlace < GEOMETRY_PRECISION; decimalPlace++) {
            precisionPattern.append("#");
        }
        final DecimalFormat precisionFormat = new DecimalFormat(precisionPattern.toString());
        precisionFormat.setRoundingMode(RoundingMode.CEILING);

        // calculate geometry
        final double centroidLongitude = coordinates.stream().mapToDouble(coordinate -> coordinate.getFirst()).reduce((lon1, lon2) -> lon1 + lon2).getAsDouble() / coordinates.size();
        final double centroidLatitude = coordinates.stream().mapToDouble(coordinate -> coordinate.getSecond()).reduce((lat1, lat2) -> lat1 + lat2).getAsDouble() / coordinates.size();
        final double errorLongitude = coordinates.stream().mapToDouble(coordinate -> Math.abs(centroidLongitude - coordinate.getFirst())).max().getAsDouble();
        final double errorLatitude = coordinates.stream().mapToDouble(coordinate -> Math.abs(centroidLatitude - coordinate.getSecond())).max().getAsDouble();
        final double minLongitude = centroidLongitude - errorLongitude;
        final double minLatitude = centroidLatitude - errorLatitude;
        final double maxLongitude = centroidLongitude + errorLongitude;
        final double maxLatitude = centroidLatitude + errorLatitude;
        final double radius = Math.max(errorLatitude, errorLongitude);

        // build geometry
        final GeometryBuilder geometryBuilder = new GeometryBuilder();
        final Geometry geometry;
        switch (type) {
            case POINT -> geometry = geometryBuilder.point(centroidLongitude, centroidLatitude);
            case LINE -> geometry = geometryBuilder.lineString(coordinates.stream()
                        .flatMap(Tuple::stream)
                        .mapToDouble(Double.class::cast)
                        .toArray());
            case POLYGON -> geometry = geometryBuilder.polygon(coordinates.stream()
                        .flatMap(Tuple::stream)
                        .mapToDouble(Double.class::cast)
                        .toArray());
            case BOX -> {
                final List<Tuple<Double, Double>> boxCoordinates = new ArrayList<>();
                boxCoordinates.add(Tuple.create(minLongitude, minLatitude));
                boxCoordinates.add(Tuple.create(minLongitude, maxLatitude));
                boxCoordinates.add(Tuple.create(maxLongitude, maxLatitude));
                boxCoordinates.add(Tuple.create(maxLongitude, minLatitude));
                geometry = geometryBuilder.polygon(boxCoordinates.stream()
                        .flatMap(Tuple::stream)
                        .mapToDouble(Double.class::cast)
                        .toArray());
            }
            default -> throw new IllegalArgumentException(String.format("The specified shape type, %s, is not currently supported.", type));
        }

        // initialise json
        final String wgs84;
        try {
            wgs84 = SpatialReference.WGS84.getSrs();
        } catch (final FactoryException ex) {
            throw new IOException(ex);
        }
        final SimpleFeatureType featureType = generateFeatureType(uuid, wgs84, DEFAULT_GEOMETRY_ATTRIBUTE, type.getGeomertyClass(), null);
        final FeatureJSON featureJson = generateFeatureJson(featureType, false);

        // build feature
        final SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        featureBuilder.add(geometry);
        featureBuilder.set(NAME_ATTRIBUTE, uuid);
        featureBuilder.set(CENTROID_LATITUDE_ATTRIBUTE, precisionFormat.format(centroidLatitude));
        featureBuilder.set(CENTROID_LONGITUDE_ATTRIBUTE, precisionFormat.format(centroidLongitude));
        featureBuilder.set(RADIUS_ATTRIBUTE, precisionFormat.format(radius));
        final SimpleFeature feature = featureBuilder.buildFeature(uuid);
        final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(uuid, featureType);

        featureCollection.add(feature);

        // build json
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        featureJson.writeFeatureCollection(featureCollection, output);

        return output.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Construct geojson to represent a collection of shapes given a list of
     * shapes, and a unique identifier.
     *
     * @param uuid a unique identifier for the shape
     * @param shapes a map of shape ids to shapes
     * @param attributes a map of shape ids to shape attributes as a map of
     * attribute name to attribute value
     * @return a geojson string representing a collection of shapes
     * @throws IOException there was a problem writing the generated geojson
     */
    public static String generateShapeCollection(final String uuid, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes) throws IOException {
        // modify schema to handle any additional attributes
        final Map<String, Class<?>> schemaAttributes = new HashMap<>();
        if (attributes != null) {
            for (final Entry<String, Map<String, Object>> entry : attributes.entrySet()) {
                if (shapes.keySet().contains(entry.getKey()) && entry.getValue() != null) {
                    entry.getValue().forEach((attributeName, attributeValue) -> {
                        if (attributeValue != null) {
                            schemaAttributes.put(attributeName, attributeValue.getClass());
                        }
                    });
                }
            }
        }

        // initialise json
        final String wgs84;
        try {
            wgs84 = SpatialReference.WGS84.getSrs();
        } catch (final FactoryException ex) {
            throw new IOException(ex);
        }
        final SimpleFeatureType featureType = generateFeatureType(uuid, wgs84, DEFAULT_GEOMETRY_ATTRIBUTE, Geometry.class, schemaAttributes);
        final FeatureJSON featureJson = generateFeatureJson(featureType, false);

        // extract all features from shapes
        final List<SimpleFeature> features = new ArrayList<>();
        for (final Entry<String, String> entry : shapes.entrySet()) {
            final String shape = entry.getValue();
            final InputStream shapeStream = new ByteArrayInputStream(shape.getBytes(StandardCharsets.UTF_8));
            try {
                final FeatureIterator<SimpleFeature> featureIterator = featureJson.streamFeatureCollection(shapeStream);
                while (featureIterator.hasNext()) {
                    final SimpleFeature feature = featureIterator.next();
                    if (attributes != null
                            && attributes.containsKey(entry.getKey())
                            && attributes.get(entry.getKey()) != null) {
                        attributes.get(entry.getKey()).forEach((attributeName, attributeValue) -> {
                            if (attributeValue != null) {
                                feature.setAttribute(attributeName, attributeValue);
                            }
                        });
                    }
                    features.add(feature);
                }
            } catch (final IOException ex) {
                // this shape is not valid geojson, so move on to the next
                LOGGER.severe(ex.getLocalizedMessage());
            }
        }

        // generate feature collection containing all features
        final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(uuid, featureType);
        featureCollection.addAll(features);

        // write feature collection to json
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        featureJson.writeFeatureCollection(featureCollection, output);

        return output.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Construct kml to represent a collection of shapes given a list of shapes,
     * and a unique identifier.
     *
     * @param uuid a unique identifier for the shape
     * @param shapes a map of shape ids to shapes
     * @param attributes a map of shape ids to shape attributes as a map of
     * attribute name to attribute value
     * @return a kml string representing a collection of shapes
     * @throws IOException there was a problem writing the generated kml
     */
    public static String generateKml(final String uuid, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes) throws IOException {
        // modify schema to handle any additional attributes
        final Map<String, Class<?>> schemaAttributes = new HashMap<>();
        if (attributes != null) {
            for (final Entry<String, Map<String, Object>> entry : attributes.entrySet()) {
                if (shapes.keySet().contains(entry.getKey()) && entry.getValue() != null) {
                    entry.getValue().forEach((attributeName, attributeValue) -> {
                        if (attributeValue != null) {
                            schemaAttributes.put(attributeName, attributeValue.getClass());
                        }
                    });
                }
            }
        }

        // initialise json
        final String wgs84;
        try {
            wgs84 = SpatialReference.WGS84.getSrs();
        } catch (final FactoryException ex) {
            throw new IOException(ex);
        }
        final SimpleFeatureType featureType = generateFeatureType(uuid, wgs84, DEFAULT_GEOMETRY_ATTRIBUTE, Geometry.class, schemaAttributes);
        final FeatureJSON featureJson = generateFeatureJson(featureType, false);

        // extract all features from shapes
        final List<SimpleFeature> features = new ArrayList<>();
        for (final Entry<String, String> entry : shapes.entrySet()) {
            final String shape = entry.getValue();
            final InputStream shapeStream = new ByteArrayInputStream(shape.getBytes(StandardCharsets.UTF_8));
            try {
                final FeatureIterator<SimpleFeature> featureIterator = featureJson.streamFeatureCollection(shapeStream);
                while (featureIterator.hasNext()) {
                    final SimpleFeature feature = featureIterator.next();
                    if (attributes != null && attributes.containsKey(entry.getKey()) 
                            && attributes.get(entry.getKey()) != null) {
                        attributes.get(entry.getKey()).forEach((attributeName, attributeValue) -> {
                            if (attributeValue != null) {
                                feature.setAttribute(attributeName, attributeValue);
                            }
                        });
                    }
                    features.add(feature);
                }
            } catch (final IOException ex) {
                // this shape is not valid geojson, so move on to the next
                LOGGER.severe(ex.getLocalizedMessage());
            }
        }

        // generate feature collection containing all features
        final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(uuid, featureType);
        featureCollection.addAll(features);

        // write feature collection to kml
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final Encoder kmlEncoder = new Encoder(new KMLConfiguration());
        kmlEncoder.setIndenting(true);
        kmlEncoder.encode(featureCollection, KML.kml, output);

        return output.toString(StandardCharsets.UTF_8.name());
    }

    /**
     * Construct a geopackage given a list of shapes, a unique identifier, and
     * an output file.
     *
     * @param uuid a unique identifier for the shape
     * @param shapes a map of shape ids to shapes
     * @param attributes a map of shape ids to additional attributes as a map of
     * attribute name to attribute value
     * @param output the geopackage file to write to
     * @param spatialReference the spatial reference to use for the geopackage
     * @throws IOException there was a problem writing the generated geopackage
     */
    public static void generateGeoPackage(final String uuid, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes, final File output, final SpatialReference spatialReference) throws IOException {
        // modify schema to handle any additional attributes, including
        // conversion to string if they are of a type unsupported by shapefiles
        final Map<String, Class<?>> schemaAttributes = new HashMap<>();
        final Map<String, List<String>> attributesOfValidType = new HashMap<>();
        if (attributes != null) {
            for (final Entry<String, Map<String, Object>> entry : attributes.entrySet()) {
                if (shapes.keySet().contains(entry.getKey()) && entry.getValue() != null) {
                    entry.getValue().forEach((attributeName, attributeValue) -> {
                        if (attributeValue != null) {
                            boolean validType = false;
                            for (final Class<?> validClass : GEOPACKAGE_ATTRIBUTE_TYPES) {
                                if (attributeValue.getClass().isAssignableFrom(validClass)) {
                                    validType = true;
                                    break;
                                }
                            }

                            final String compatibleAttributeName = generateSqliteCompatibleHeader(attributeName);
                            if (validType && !attributesOfValidType.containsKey(entry.getKey())) {
                                attributesOfValidType.put(entry.getKey(), new ArrayList<>());
                                attributesOfValidType.get(entry.getKey()).add(compatibleAttributeName);
                                schemaAttributes.put(compatibleAttributeName, attributeValue.getClass());
                            } else if (validType) {
                                attributesOfValidType.get(entry.getKey()).add(compatibleAttributeName);
                                schemaAttributes.put(compatibleAttributeName, attributeValue.getClass());
                            } else {
                                schemaAttributes.put(compatibleAttributeName, String.class);
                            }
                        }
                    });
                }
            }
        }

        // initialise json
        final String wgs84;
        try {
            wgs84 = SpatialReference.WGS84.getSrs();
        } catch (final FactoryException ex) {
            throw new IOException(ex);
        }
        final SimpleFeatureType featureType = generateFeatureType(uuid, wgs84, DEFAULT_GEOMETRY_ATTRIBUTE, Geometry.class, schemaAttributes);
        final FeatureJSON featureJson = generateFeatureJson(featureType, false);

        // extract all features from shapes
        final List<SimpleFeature> features = new ArrayList<>();
        for (final Entry<String, String> entry : shapes.entrySet()) {
            final String shape = entry.getValue();
            final InputStream shapeStream = new ByteArrayInputStream(shape.getBytes(StandardCharsets.UTF_8));
            try {
                final FeatureIterator<SimpleFeature> featureIterator = featureJson.streamFeatureCollection(shapeStream);
                while (featureIterator.hasNext()) {
                    final SimpleFeature feature = featureIterator.next();
                    if (attributes != null && attributes.containsKey(entry.getKey()) 
                            && attributes.get(entry.getKey()) != null) {
                        attributes.get(entry.getKey()).forEach((attributeName, attributeValue) -> {
                            final String compatibleAttributeName = generateSqliteCompatibleHeader(attributeName);
                            if (schemaAttributes.containsKey(compatibleAttributeName)
                                    && attributesOfValidType.get(entry.getKey()) != null
                                    && attributesOfValidType.get(entry.getKey()).contains(compatibleAttributeName)) {
                                feature.setAttribute(compatibleAttributeName, attributeValue);
                            } else if (schemaAttributes.containsKey(compatibleAttributeName)) {
                                feature.setAttribute(compatibleAttributeName, attributeValue == null
                                        ? null : attributeValue.toString());
                            }
                        });
                    }
                    features.add(feature);
                }
            } catch (final IOException ex) {
                // this shape is not valid geojson, so move on to the next
                LOGGER.severe(ex.getLocalizedMessage());
            }
        }

        // generate feature collection containing all features
        final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection(uuid, featureType);
        featureCollection.addAll(features);

        // create feature entry
        final FeatureEntry featureEntry = new FeatureEntry();
        featureEntry.setGeometryColumn(DEFAULT_GEOMETRY_ATTRIBUTE);
        featureEntry.setGeometryType(Geometries.GEOMETRYCOLLECTION);
        featureEntry.setBounds(ReferencedEnvelope.EVERYTHING);
        featureEntry.setSrid(spatialReference.getSrid());

        // Remove geopackage file if exists as it stores existing tables/indexes
        if (output.isFile()) {
            output.delete();
        }
        // write feature collection to geopackage
        try (final GeoPackage geoPackage = new GeoPackage(output)) {
            geoPackage.add(featureEntry, featureCollection);
            geoPackage.createSpatialIndex(featureEntry);
        }
    }

    /**
     * Construct a shapefile given a list of shapes of the specified shape type,
     * a unique identifier, and an output file.
     *
     * @param uuid a unique identifier for the shape
     * @param type a type of shape
     * @param shapes a map of shape ids to shapes
     * @param attributes a map of shape ids to additional attributes as a map of
     * attribute name to attribute value
     * @param output the shapefile to write to
     * @param spatialReference the spatial reference to use for the shapefile
     * @throws IOException there was a problem writing the generated shapefile
     */
    public static void generateShapefile(final String uuid, final GeometryType type, final Map<String, String> shapes, final Map<String, Map<String, Object>> attributes, final File output, final SpatialReference spatialReference) throws IOException {
        // modify schema to handle any additional attributes, including
        // conversion to string if they are of a type unsupported by shapefiles
        final Map<String, Class<?>> schemaAttributes = new HashMap<>();
        final Map<String, List<String>> attributesOfValidType = new HashMap<>();
        if (attributes != null) {
            for (final Entry<String, Map<String, Object>> entry : attributes.entrySet()) {
                if (shapes.keySet().contains(entry.getKey()) && entry.getValue() != null) {
                    entry.getValue().forEach((attributeName, attributeValue) -> {
                        if (attributeValue != null) {
                            boolean validType = false;
                            for (final Class<?> validClass : SHAPEFILE_ATTRIBUTE_TYPES.keySet()) {
                                if (attributeValue.getClass().isAssignableFrom(validClass)) {
                                    validType = true;
                                    break;
                                }
                            }

                            final String compatibleAttributeName = generateShapefileCompatibleHeader(attributeName);
                            if (validType && !attributesOfValidType.containsKey(entry.getKey())) {
                                attributesOfValidType.put(entry.getKey(), new ArrayList<>());
                                attributesOfValidType.get(entry.getKey()).add(compatibleAttributeName);
                                schemaAttributes.put(compatibleAttributeName, attributeValue.getClass());
                            } else if (validType) {
                                attributesOfValidType.get(entry.getKey()).add(compatibleAttributeName);
                                schemaAttributes.put(compatibleAttributeName, attributeValue.getClass());
                            } else {
                                schemaAttributes.put(compatibleAttributeName, String.class);
                            }
                        }
                    });
                }
            }
        }

        // initialise json
        final String wgs84;
        try {
            wgs84 = SpatialReference.WGS84.getSrs();
        } catch (final FactoryException ex) {
            throw new IOException(ex);
        }
        final SimpleFeatureType featureType = generateFeatureType(uuid, wgs84, DEFAULT_GEOMETRY_ATTRIBUTE, type.getGeomertyClass(), schemaAttributes);
        final FeatureJSON featureJson = generateFeatureJson(featureType, false);

        // initialise shapefile
        final String srs;
        try {
            srs = spatialReference.getSrs();
        } catch (final FactoryException ex) {
            throw new IOException(ex);
        }
        final SimpleFeatureType schema = generateFeatureType(uuid, srs, SHAPEFILE_GEOMETRY_ATTRIBUTE, type.getGeomertyClass(), schemaAttributes);
        final ShapefileDataStoreFactory datastoreFactory = new ShapefileDataStoreFactory();
        final Map<String, Serializable> datastoreParameters = new HashMap<>();
        datastoreParameters.put("url", Utilities.toURI(output).toURL());
        datastoreParameters.put("create spatial index", Boolean.TRUE);
        final ShapefileDataStore datastore = (ShapefileDataStore) datastoreFactory.createNewDataStore(datastoreParameters);
        datastore.createSchema(schema);

        // copy features of the desired type from geojson to shapefile
        try (final FeatureWriter<SimpleFeatureType, SimpleFeature> writer = datastore.getFeatureWriterAppend(datastore.getTypeNames()[0], Transaction.AUTO_COMMIT)) {
            for (final Entry<String, String> entry : shapes.entrySet()) {
                final String shape = entry.getValue();
                final InputStream shapeStream = new ByteArrayInputStream(shape.getBytes(StandardCharsets.UTF_8));
                try {
                    final FeatureIterator<SimpleFeature> featureIterator = featureJson.streamFeatureCollection(shapeStream);
                    while (featureIterator.hasNext()) {
                        final SimpleFeature feature = featureIterator.next();
                        if (type.getGeomertyClass().isInstance(feature.getDefaultGeometry())) {
                            final SimpleFeature writableFeature = writer.next();

                            final Geometry featureGeometry;
                            if (!schema.getCoordinateReferenceSystem().equals(featureType.getCoordinateReferenceSystem())) {
                                final Geometry currentGeometry = (Geometry) feature.getAttribute(DEFAULT_GEOMETRY_ATTRIBUTE);
                                final MathTransform transform = CRS.findMathTransform(featureType.getCoordinateReferenceSystem(), schema.getCoordinateReferenceSystem(), true);
                                featureGeometry = JTS.transform(currentGeometry, transform);
                            } else {
                                featureGeometry = (Geometry) feature.getAttribute(DEFAULT_GEOMETRY_ATTRIBUTE);
                            }
                            writableFeature.setDefaultGeometry(featureGeometry);

                            for (int index = 0; index < writableFeature.getType().getAttributeCount(); index++) {
                                final String name = writableFeature.getType().getDescriptor(index).getLocalName();
                                if (name.equals(SHAPEFILE_GEOMETRY_ATTRIBUTE)) {
                                    continue;
                                }
                                writableFeature.setAttribute(name, feature.getAttribute(name));
                            }

                            if (!feature.getUserData().isEmpty()) {
                                writableFeature.getUserData().putAll(feature.getUserData());
                            }

                            if (attributes != null && attributes.containsKey(entry.getKey()) 
                                    && attributes.get(entry.getKey()) != null) {
                                attributes.get(entry.getKey()).forEach((attributeName, attributeValue) -> {
                                    final String compatibleAttributeName = generateShapefileCompatibleHeader(attributeName);
                                    if (schemaAttributes.containsKey(compatibleAttributeName) 
                                            && attributeValue != null
                                            && attributesOfValidType.get(entry.getKey()) != null
                                            && attributesOfValidType.get(entry.getKey()).contains(compatibleAttributeName)) {
                                        writableFeature.setAttribute(compatibleAttributeName, attributeValue);
                                    } else if (schemaAttributes.containsKey(compatibleAttributeName) && attributeValue != null) {
                                        writableFeature.setAttribute(compatibleAttributeName, attributeValue.toString());
                                    }
                                });
                            }

                            writer.write();
                        }
                    }
                } catch (final IOException | TransformException | FactoryException ex) {
                    // this shape is not valid geojson, so move on to the next
                    LOGGER.severe(ex.getLocalizedMessage());
                }
            }
        }
    }

    private static SimpleFeatureType generateFeatureType(final String uuid, final String srs,
            final String geometryName, final Class<? extends Geometry> geometryClass,
            final Map<String, Class<?>> attributes) {
        final SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
        featureTypeBuilder.setName(uuid);
        featureTypeBuilder.setSRS(srs);
        featureTypeBuilder.add(geometryName, geometryClass);
        featureTypeBuilder.add(NAME_ATTRIBUTE, String.class);
        featureTypeBuilder.add(CENTROID_LATITUDE_ATTRIBUTE, Double.class);
        featureTypeBuilder.add(CENTROID_LONGITUDE_ATTRIBUTE, Double.class);
        featureTypeBuilder.add(RADIUS_ATTRIBUTE, Double.class);
        if (attributes != null) {
            attributes.forEach(featureTypeBuilder::add);
        }
        return featureTypeBuilder.buildFeatureType();
    }

    private static FeatureJSON generateFeatureJson(final SimpleFeatureType featureType, final boolean includeCrs) {
        final GeometryJSON geometryJson = new GeometryJSON(GEOMETRY_PRECISION);
        final FeatureJSON featureJson = new FeatureJSON(geometryJson);
        featureJson.setFeatureType(featureType);
        featureJson.setEncodeFeatureBounds(true);
        featureJson.setEncodeFeatureCRS(includeCrs);
        featureJson.setEncodeFeatureCollectionBounds(true);
        featureJson.setEncodeFeatureCollectionCRS(includeCrs);
        return featureJson;
    }

    private static String generateSqliteCompatibleHeader(final String header) {
        return header.replace(SeparatorConstants.PERIOD, SeparatorConstants.UNDERSCORE);
    }

    private static String generateShapefileCompatibleHeader(final String header) {
        String compatibleHeader;

        // remove category components and illegal characters
        final String[] headerComponents = header.split("\\.");
        if (headerComponents.length > 1) {
            compatibleHeader = headerComponents[0]
                    .concat(headerComponents[headerComponents.length - 1]);
        } else {
            compatibleHeader = header.replace(SeparatorConstants.PERIOD, "");
        }

        // shorten element prefixes
        if (compatibleHeader.startsWith("source")) {
            compatibleHeader = SOURCE_PATTERN.matcher(compatibleHeader).replaceFirst("s");
        }
        if (compatibleHeader.startsWith("destination")) {
            compatibleHeader = DESTINATION_PATTERN.matcher(compatibleHeader).replaceFirst("d");
        }
        if (compatibleHeader.startsWith("transaction")) {
            compatibleHeader = TRANSACTION_PATTERN.matcher(compatibleHeader).replaceFirst("t");
        }

        // ensure header is no more than 10 characters
        if (compatibleHeader.length() > 10) {
            compatibleHeader = compatibleHeader.substring(0, 10);
        }

        return compatibleHeader;
    }
}
