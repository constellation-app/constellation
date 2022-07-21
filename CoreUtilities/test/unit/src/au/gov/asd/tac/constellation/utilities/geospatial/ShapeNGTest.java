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
package au.gov.asd.tac.constellation.utilities.geospatial;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.datastructure.Tuple;
import static au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType.BOX;
import static au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType.LINE;
import static au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType.MULTI_POINT;
import static au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType.MULTI_POLYGON;
import static au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType.POINT;
import static au.gov.asd.tac.constellation.utilities.geospatial.Shape.GeometryType.POLYGON;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geopkg.GeoPkgDataStoreFactory;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xsd.PullParser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPoint;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * @author groombridge34a
 */
public class ShapeNGTest {

    /**
     * An IOException is thrown if a FactoryException is thrown by CRS.
     *
     * This test will fail if the CRS cache in
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.Shape.SpatialReference}
     * is not empty. If you write a test that populates the CRS cache, use
     * TestNG's "dependsOnGroups" function to cause your test to run after this
     * test, which is in the "ShapeNGTest.emptyCrsCache" test group.
     *
     * @throws IOException if unable to generate shape GeoJson
     */
    @Test(groups = {"ShapeNGTest.emptyCrsCache"}, expectedExceptions = {IOException.class},
            expectedExceptionsMessageRegExp = ".*FactoryException.*")
    public void testGenerateShapeCrsError() throws IOException {
        try (final MockedStatic<CRS> crsMockedStatic = mockStatic(CRS.class)) {
            crsMockedStatic.when(() -> CRS.decode(anyString())).thenThrow(FactoryException.class);
            // must be run before any other test that populates the CRS cache
            // see test method comment
            Shape.generateShape(
                    "dummy",
                    POINT,
                    Arrays.asList(new Tuple<>(0D, 0D)));
        }
    }

    /**
     * An IOException is thrown when retrieving the spacial reference to use
     * while creating GeoJson from a collection of shapes.
     *
     * This test will fail if the CRS cache in
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.Shape.SpatialReference}
     * is not empty. If you write a test that populates the CRS cache, use
     * TestNG's "dependsOnGroups" function to cause your test to run after this
     * test, which is in the "ShapeNGTest.emptyCrsCache" test group.
     *
     * @throws IOException if unable to generate shape GeoJson
     */
    @Test(groups = {"ShapeNGTest.emptyCrsCache"}, expectedExceptions = {IOException.class},
            expectedExceptionsMessageRegExp = ".*FactoryException.*")
    public void testGenerateShapeCollectionCrsError() throws IOException {
        try (final MockedStatic<CRS> crsMockedStatic = mockStatic(CRS.class)) {
            crsMockedStatic.when(() -> CRS.decode(anyString())).thenThrow(FactoryException.class);
            // must be run before any other test that populates the CRS cache
            // see test method comment
            Shape.generateShapeCollection("dummy", Collections.emptyMap(),
                    Collections.emptyMap());
        }
    }

    /**
     * An IOException is thrown when retrieving the spacial reference to use
     * while creating KML from a collection of shapes.
     *
     * This test will fail if the CRS cache in
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.Shape.SpatialReference}
     * is not empty. If you write a test that populates the CRS cache, use
     * TestNG's "dependsOnGroups" function to cause your test to run after this
     * test, which is in the "ShapeNGTest.emptyCrsCache" test group.
     *
     * @throws IOException if unable to generate shape KML
     */
    @Test(groups = {"ShapeNGTest.emptyCrsCache"}, expectedExceptions = {IOException.class},
            expectedExceptionsMessageRegExp = ".*FactoryException.*")
    public void testGenerateKmlCrsError() throws IOException {
        try (final MockedStatic<CRS> crsMockedStatic = mockStatic(CRS.class)) {
            crsMockedStatic.when(() -> CRS.decode(anyString())).thenThrow(FactoryException.class);
            // must be run before any other test that populates the CRS cache
            // see test method comment
            Shape.generateKml("dummy", Collections.emptyMap(),
                    Collections.emptyMap());
        }
    }

    /**
     * An IOException is thrown when retrieving the spacial reference to use
     * while creating a GeoPackage from a collection of shapes.
     *
     * This test will fail if the CRS cache in
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.Shape.SpatialReference}
     * is not empty. If you write a test that populates the CRS cache, use
     * TestNG's "dependsOnGroups" function to cause your test to run after this
     * test, which is in the "ShapeNGTest.emptyCrsCache" test group.
     *
     * @throws IOException if unable to generate shape GeoPackage
     */
    @Test(groups = {"ShapeNGTest.emptyCrsCache"}, expectedExceptions = {IOException.class},
            expectedExceptionsMessageRegExp = ".*FactoryException.*")
    public void testGenerateGeoPackageCrsError() throws IOException {
        try (final MockedStatic<CRS> crsMockedStatic = mockStatic(CRS.class)) {
            crsMockedStatic.when(() -> CRS.decode(anyString())).thenThrow(FactoryException.class);
            // must be run before any other test that populates the CRS cache
            // see test method comment
            Shape.generateGeoPackage("dummy",
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    File.createTempFile("dummy", "dummy"),
                    Shape.SpatialReference.WGS84);
        }
    }

    /**
     * An IOException is thrown when retrieving the spacial reference to use
     * while creating a Shapefile from a collection of shapes.
     *
     * This test will fail if the CRS cache in
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.Shape.SpatialReference}
     * is not empty. If you write a test that populates the CRS cache, use
     * TestNG's "dependsOnGroups" function to cause your test to run after this
     * test, which is in the "ShapeNGTest.emptyCrsCache" test group.
     *
     * @throws IOException if unable to generate Shapefile
     */
    @Test(groups = {"ShapeNGTest.emptyCrsCache"}, expectedExceptions = {IOException.class},
            expectedExceptionsMessageRegExp = ".*FactoryException.*")
    public void testGenerateShapefileCrsError() throws IOException {
        try (final MockedStatic<CRS> crsMockedStatic = mockStatic(CRS.class)) {
            crsMockedStatic.when(() -> CRS.decode(anyString())).thenThrow(FactoryException.class);
            // must be run before any other test that populates the CRS cache
            // see test method comment
            Shape.generateShapefile("dummy", POINT,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    File.createTempFile("dummy", "dummy"),
                    Shape.SpatialReference.WGS84);
        }
    }

    /**
     * After running the tests that rely on an empty CRS cache we can manually
     * populate the cache to speed up test runs and avoid EPSG code from
     * reaching out further than necessary for simple unit tests.
     */
    @AfterGroups(groups = ("ShapeNGTest.emptyCrsCache"))
    public void populateCrsCache() {
        try (final MockedStatic<CRS> crsMockedStatic = mockStatic(CRS.class)) {
            crsMockedStatic.when(() -> CRS.toSRS(any()))
                    .thenReturn(EPSG_PREFIX + Shape.SpatialReference.WGS84.getSrid())
                    .thenReturn(EPSG_PREFIX + Shape.SpatialReference.WGS84_WEB_MERCATOR.getSrid());
            // run the code twice to populate the cache
            try {
                Shape.SpatialReference.WGS84.getSrs();
                Shape.SpatialReference.WGS84.getSrs();
            } catch (FactoryException e) {
                fail("Mocking error.");
            }
        }
    }

    /**
     * Can retrieve GeometryType values with getters.
     */
    @Test
    public void testGeometryTypeGetters() {
        assertEquals(POINT.getGeomertyType(), "Point");
        assertEquals(MULTI_POINT.getGeomertyClass(),
                MultiPoint.class);
    }

    /**
     * Can retrieve SpatialReference values with getters.
     */
    @Test
    public void testSpatialReferenceGetters() {
        assertEquals(Shape.SpatialReference.WGS84_WEB_MERCATOR.getName(),
                "Web Mercator");
        assertEquals(Shape.SpatialReference.WGS84.getSrid(),
                4326);
    }

    private static final String EPSG_PREFIX = "EPSG:";

    /**
     * Can retrieve and cache the decoded SRS of a spacial reference.
     *
     * @throws FactoryException if can't get SRS from CRS
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testSpatialReferenceGetSrs() throws FactoryException {
        try (final MockedStatic<CRS> crsMockedStatic = mockStatic(CRS.class)) {
            /* assert that when getSrs is called multiple times for a
               SpacialReference the same value is returned, and the value is
               only calculated by the CRS class once */
            // WGS84
            final String srs84 = Shape.SpatialReference.WGS84.getSrs();
            assertSame(Shape.SpatialReference.WGS84.getSrs(), srs84);
            crsMockedStatic.verify(
                    () -> CRS.decode(EPSG_PREFIX + Shape.SpatialReference.WGS84.getSrid()),
                    atMostOnce());

            // WGS84_WEB_MERCATOR
            final String srs84wm = Shape.SpatialReference.WGS84_WEB_MERCATOR.getSrs();
            assertSame(Shape.SpatialReference.WGS84_WEB_MERCATOR.getSrs(), srs84wm);
            crsMockedStatic.verify(
                    () -> CRS.decode(EPSG_PREFIX + Shape.SpatialReference.WGS84_WEB_MERCATOR.getSrid()),
                    atMostOnce());
        }
    }

    // string templates for the GeoJson output format
    private static final String GEOJSON_TEMPLATE_PREFIX
            = "{\"type\":\"FeatureCollection\",\"bbox\":[%s],\"features\":[";
    private static final String GEOJSON_TEMPLATE_FEATURE
            = "{\"type\":\"Feature\",\"bbox\":[%s],\"geometry\":"
            + "{\"type\":\"%s\",\"coordinates\":%s},\"properties\":{%s},"
            + "\"id\":\"%s\"}";
    private static final String GEOJSON_TEMPLATE_SUFFIX
            = "]}";

    private static final String GEOJSON_TEMPLATE_SIMPLE
            = GEOJSON_TEMPLATE_PREFIX
            + GEOJSON_TEMPLATE_FEATURE
            + GEOJSON_TEMPLATE_SUFFIX;

    /**
     * Can test if a String is valid GeoJson.
     */
    @Test
    public void testIsValidGeoJson() {
        // the template used for the JSON feature collection test is valid
        assertTrue(Shape.isValidGeoJson(GEOJSON_TEMPLATE_SIMPLE));
        // removing the FeatureCollection element makes this invalid GeoJson
        assertFalse(Shape.isValidGeoJson(
                GEOJSON_TEMPLATE_SIMPLE.replace("\"type\":\"FeatureCollection\"", "")));
        // removing the features array makes this invalid GeoJson
        assertFalse(Shape.isValidGeoJson(
                GEOJSON_TEMPLATE_SIMPLE.replace("\"features\":[", "")));
    }

    // common property names for all shapes
    private static final String NAME = "name";
    private static final String CENTRE_LAT = "centreLat";
    private static final String CENTRE_LON = "centreLon";
    private static final String RADIUS = "radius";

    // test points for each shape
    private static final double POINT_LAT = -35.29D;
    private static final double POINT_LON = 149.14D;
    private static final double LINE_LAT1 = -27.98D;
    private static final double LINE_LON1 = 153.40D;
    private static final double LINE_LAT2 = -31.89D;
    private static final double LINE_LON2 = 115.84D;
    private static final double POLY_LAT1 = 27.94D;
    private static final double POLY_LON1 = -96.97D;
    private static final double POLY_LAT2 = 18.96D;
    private static final double POLY_LON2 = -95.80D;
    private static final double POLY_LAT3 = 19.42D;
    private static final double POLY_LON3 = -90.98D;
    private static final double POLY_LAT4 = 21.61D;
    private static final double POLY_LON4 = -90.27D;
    private static final double POLY_LAT5 = 25.11D;
    private static final double POLY_LON5 = -81.41D;
    private static final double POLY_LAT6 = 29.69D;
    private static final double POLY_LON6 = -83.89D;
    private static final double BOX_LAT_MIN = 77.26D;
    private static final double BOX_LAT_MAX = 84.76D;
    private static final double BOX_LON_MIN = -130.51D;
    private static final double BOX_LON_MAX = 121.38D;

    // test coordinate tuples for shapes
    private static final List<Tuple<Double, Double>> POINT_COORDS
            = Arrays.asList(new Tuple<>(POINT_LAT, POINT_LON));
    private static final List<Tuple<Double, Double>> LINE_COORDS
            = Arrays.asList(
                    new Tuple<>(LINE_LAT1, LINE_LON1),
                    new Tuple<>(LINE_LAT2, LINE_LON2));
    private static final List<Tuple<Double, Double>> POLY_COORDS
            = Arrays.asList(
                    new Tuple<>(POLY_LAT1, POLY_LON1),
                    new Tuple<>(POLY_LAT2, POLY_LON2),
                    new Tuple<>(POLY_LAT3, POLY_LON3),
                    new Tuple<>(POLY_LAT4, POLY_LON4),
                    new Tuple<>(POLY_LAT5, POLY_LON5),
                    new Tuple<>(POLY_LAT6, POLY_LON6));
    private static final List<Tuple<Double, Double>> BOX_COORDS
            = Arrays.asList(
                    new Tuple<>(BOX_LAT_MIN, BOX_LON_MIN),
                    new Tuple<>(BOX_LAT_MIN, BOX_LON_MAX),
                    new Tuple<>(BOX_LAT_MAX, BOX_LON_MAX),
                    new Tuple<>(BOX_LAT_MAX, BOX_LON_MIN));

    // test common properties for shapes
    private static final String POINT_ID = "pointId";
    private static final double POINT_RADIUS = 0D;
    private static final String LINE_ID = "lineId";
    private static final double LINE_CENTRE_LAT = 134.62D;
    private static final double LINE_CENTRE_LON = -29.935D;
    private static final double LINE_RADIUS = 18.78D;
    private static final String POLY_ID = "polyId";
    private static final double POLY_CENTRE_LAT = -89.88666666D;
    private static final double POLY_CENTRE_LON = 23.78833334D;
    private static final double POLY_RADIUS = 8.47666667D;
    private static final String BOX_ID = "boxId";
    private static final double BOX_CENTRE_LAT = -4.56499999D;
    private static final double BOX_CENTRE_LON = 81.01D;
    private static final double BOX_RADIUS = 125.945D;

    // convenience method to get the bounding box part of GeoJson
    private String getGeoJsonBoundingBox(final List<Double> points) {
        return points.stream()
                .map(p -> String.valueOf(p))
                .collect(Collectors.joining(","));
    }

    // convenince method to get the coordinates part of GeoJson
    private String getGeoJsonCoords(final List<Tuple<Double, Double>> coords,
            final String type) {
        final List<String> c = new ArrayList<>();

        String first = null;
        for (Tuple t : coords) {
            // convert each coord to [123,456]
            final StringBuilder s = new StringBuilder();
            s.append("[");
            s.append(String.join(",",
                    String.valueOf((Double) t.getFirst()),
                    String.valueOf((Double) t.getSecond())));
            s.append("]");
            c.add(s.toString());
            // some shapes put the first coord in twice, beginning and end
            if (first == null) {
                first = s.toString();
            }
        }

        // polygons duplicate the first coordinate as the last coord
        if (type.equals(POLYGON.getGeomertyType())) {
            c.add(first);
        }

        String ret = String.join(",", c);
        // points only have a single coordinate, not an array of coords
        if (!type.equals(POINT.getGeomertyType())) {
            // polygons have arrays of arrays of coords
            if (type.equals(POLYGON.getGeomertyType())) {
                ret = "[" + ret + "]";
            }
            // all other shapes have an array of coords
            ret = "[" + ret + "]";
        }
        return ret;
    }

    // patterns to format quoted and unquoted property values
    private static final String QUOTED_PATTERN = "\"%s\":\"%s\"";
    private static final String UNQUOTED_PATTERN = "\"%s\":%s";

    // convenince method to get the properties part of GeoJson
    private String getGeoJsonProperties(final String name,
            final double centreLat, final double centreLon,
            final double radius) {
        final List<String> c = new ArrayList<>(Arrays.asList(
                String.format(QUOTED_PATTERN, NAME, name),
                String.format(UNQUOTED_PATTERN, CENTRE_LAT, centreLat),
                String.format(UNQUOTED_PATTERN, CENTRE_LON, centreLon),
                String.format(UNQUOTED_PATTERN, RADIUS, radius)));
        return String.join(",", c);
    }

    /**
     * Can construct GeoJson to represent a shape.
     *
     * Note that this test also covers rounding of centreLat, centreLon and
     * radius to 8 points of precision. If you update the test, make sure that
     * at least one value for each of centreLat, centreLon and radius ends up
     * being a recurring value.
     *
     * This test verifies JSON instead of marshalling to a geo Feature, as we
     * use several unsupported geotools libraries. Since the GeoJson schema is
     * so simple the GeoJson isn't marshalled to a JSON object, verification is
     * performed on the String, which is computationally faster. However working
     * with String representations of structured data models is generally A Bad
     * Idea because it increases complexity. Feel free to re-write if it is too
     * hard to understand.
     *
     * @throws IOException if unable to generate shape GeoJson
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShape() throws IOException {
        // point
        final String point = Shape.generateShape(POINT_ID, POINT, POINT_COORDS);

        final String bboxPoint = getGeoJsonBoundingBox(
                Arrays.asList(POINT_LAT, POINT_LON, POINT_LAT, POINT_LON));
        assertEquals(point, String.format(GEOJSON_TEMPLATE_SIMPLE,
                bboxPoint,
                bboxPoint,
                POINT.getGeomertyType(),
                getGeoJsonCoords(POINT_COORDS, POINT.getGeomertyType()),
                getGeoJsonProperties(
                        POINT_ID, POINT_LON, POINT_LAT, POINT_RADIUS), // not a typo!
                POINT_ID));

        // line
        final String line = Shape.generateShape(LINE_ID, LINE, LINE_COORDS);

        final String bboxLine = String.join(",",
                String.valueOf(LINE_LAT2), String.valueOf(LINE_LON2),
                String.valueOf(LINE_LAT1), String.valueOf(LINE_LON1));
        assertEquals(line, String.format(GEOJSON_TEMPLATE_SIMPLE,
                bboxLine,
                bboxLine,
                LINE.getGeomertyType(),
                getGeoJsonCoords(LINE_COORDS, LINE.getGeomertyType()),
                getGeoJsonProperties(
                        LINE_ID, LINE_CENTRE_LAT, LINE_CENTRE_LON, LINE_RADIUS),
                LINE_ID));

        // polygon
        final String poly = Shape.generateShape(POLY_ID, POLYGON, POLY_COORDS);

        final String bboxPoly = String.join(",",
                String.valueOf(POLY_LAT2), String.valueOf(POLY_LON1),
                String.valueOf(POLY_LAT6), String.valueOf(POLY_LON5));
        assertEquals(poly, String.format(GEOJSON_TEMPLATE_SIMPLE,
                bboxPoly,
                bboxPoly,
                POLYGON.getGeomertyType(),
                getGeoJsonCoords(POLY_COORDS, POLYGON.getGeomertyType()),
                getGeoJsonProperties(
                        POLY_ID, POLY_CENTRE_LAT, POLY_CENTRE_LON, POLY_RADIUS),
                POLY_ID));

        // box
        final String box = Shape.generateShape(BOX_ID, BOX, BOX_COORDS);

        final String bboxBox = String.join(",",
                String.valueOf(BOX_LAT_MIN), String.valueOf(BOX_LON_MIN),
                String.valueOf(BOX_LAT_MAX), String.valueOf(BOX_LON_MAX));
        assertEquals(box, String.format(GEOJSON_TEMPLATE_SIMPLE,
                bboxBox,
                bboxBox,
                BOX.getGeomertyType(),
                getGeoJsonCoords(BOX_COORDS, BOX.getGeomertyType()),
                getGeoJsonProperties(
                        BOX_ID, BOX_CENTRE_LAT, BOX_CENTRE_LON, BOX_RADIUS),
                BOX_ID));
    }

    /**
     * An IllegalArgumentException is thrown if the Shape requested is not
     * currently supported.
     *
     * @throws IOException if unable to generate shape GeoJson
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"},
            expectedExceptions = {IllegalArgumentException.class},
            expectedExceptionsMessageRegExp = ".*MULTI_POINT, is not currently supported.")
    public void testGenerateShapeNotSupported() throws IOException {
        Shape.generateShape(
                "dummy",
                MULTI_POINT,
                Arrays.asList(new Tuple<>(POINT_LAT, POINT_LON)));
    }

    // IDs of shapes that test edge cases
    private static final String NULL_ATTR_ID = "nullAttrId";
    private static final String EMPTY_ATTR_MAP_ID = "emptyAttrMapId";
    private static final String NULL_ATTR_VAL_ID = "nullAttrValueId";
    private static final String ROGUE_SHAPE_ID = "rogueShapeId";
    private static final String ROGUE_ATTR_ID = "rogueAttrId";

    // shapes used by many tests
    private static final Map<String, String> TEST_SHAPES = new HashMap<>();

    // returns a map of many test shapes
    private static Map<String, String> getTestShapes() throws IOException {
        // return TEST_SHAPES if populated
        if (!TEST_SHAPES.isEmpty()) {
            return TEST_SHAPES;
        }

        // if not, populate TEST_SHAPES then return
        // first add valid shapes with "normal" attributes
        TEST_SHAPES.put(POINT_ID, Shape.generateShape(POINT_ID, POINT, POINT_COORDS));
        TEST_SHAPES.put(LINE_ID, Shape.generateShape(LINE_ID, LINE, LINE_COORDS));
        TEST_SHAPES.put(POLY_ID, Shape.generateShape(POLY_ID, POLYGON, POLY_COORDS));
        TEST_SHAPES.put(BOX_ID, Shape.generateShape(BOX_ID, BOX, BOX_COORDS));

        // attributes map for this shape is null
        TEST_SHAPES.put(NULL_ATTR_ID,
                Shape.generateShape(NULL_ATTR_ID, BOX, BOX_COORDS));

        // attributes map for this shape is empty
        TEST_SHAPES.put(EMPTY_ATTR_MAP_ID,
                Shape.generateShape(EMPTY_ATTR_MAP_ID, LINE, LINE_COORDS));

        // attribute has a null value
        TEST_SHAPES.put(NULL_ATTR_VAL_ID,
                Shape.generateShape(NULL_ATTR_VAL_ID, POINT, POINT_COORDS));

        // id of this shape is not present in attributes
        TEST_SHAPES.put(ROGUE_SHAPE_ID,
                Shape.generateShape(ROGUE_SHAPE_ID, POLYGON, POLY_COORDS));

        return TEST_SHAPES;
    }

    // shapes used by many tests, one of each of Point, Line, Box and Polygon
    private static final Map<String, String> BASIC_TEST_SHAPES = new HashMap<>();

    /* returns a map of test shapes that includes one of each of Point, Line,
       Box and Polygon */
    private static Map<String, String> getBasicTestShapes() throws IOException {
        // return BASIC_TEST_SHAPES if populated
        if (!BASIC_TEST_SHAPES.isEmpty()) {
            return BASIC_TEST_SHAPES;
        }

        // if not, populate BASIC_TEST_SHAPES then return
        BASIC_TEST_SHAPES.putAll(getTestShapes().entrySet().stream()
                .filter(e -> e.getKey().equals(POINT_ID)
                || e.getKey().equals(LINE_ID)
                || e.getKey().equals(BOX_ID)
                || e.getKey().equals(POLY_ID))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return BASIC_TEST_SHAPES;
    }

    // additional test shape attributes
    // note that GeoPackage and Shapefile attributes have character and size requirements
    private static final String POINT_ATTR_ID1 = "pntAttrId1";
    private static final String POINT_ATTR_ID2 = "pntAttrId2";
    private static final String LINE_ATTR_ID = "lineAttrId";
    private static final String POLY_ATTR_ID1 = "plyAttrId1";
    private static final String POLY_ATTR_ID2 = "plyAttrId2";
    private static final String BOX_ATTR_ID = "boxAttrId";
    private static final String NULL_ATTR_VAL_ATTR_ID = "nullAtVlId";

    private static final String POINT_ATTR_VAL1 = "pointAttrVal1";
    private static final Tuple<String, String> POINT_ATTR_VAL2
            = new Tuple<>("pointAttrVal2l", "pointAttrVal2r");
    private static final Integer LINE_ATTR_VAL = 987;
    private static final Double POLY_ATTR_VAL1 = 654.321D;
    private static final ConstellationColor POLY_ATTR_VAL2
            = ConstellationColor.BLUE;
    private static final Vector3f BOX_ATTR_VAL
            = new Vector3f(0.123F, -0.456F, 0.00789F);

    // attributes used by several tests
    private static final Map<String, Map<String, Object>> TEST_ATTRIBUTES
            = new HashMap<>();

    // returns a map of many test attribute maps
    private static Map<String, Map<String, Object>> getTestAttributes() {
        // return TEST_ATTRIBUTES if populated
        if (!TEST_ATTRIBUTES.isEmpty()) {
            return TEST_ATTRIBUTES;
        }

        // if not, populate TEST_ATTRIBUTES then return
        // valid point
        final Map<String, Object> pointAttr = new HashMap<>();
        pointAttr.put(POINT_ATTR_ID1, POINT_ATTR_VAL1);
        pointAttr.put(POINT_ATTR_ID2, POINT_ATTR_VAL2);
        TEST_ATTRIBUTES.put(POINT_ID, pointAttr);

        // valid line
        final Map<String, Object> lineAttr = new HashMap<>();
        lineAttr.put(LINE_ATTR_ID, LINE_ATTR_VAL);
        TEST_ATTRIBUTES.put(LINE_ID, lineAttr);

        // valid polygon
        final Map<String, Object> polyAttr = new HashMap<>();
        polyAttr.put(POLY_ATTR_ID1, POLY_ATTR_VAL1);
        polyAttr.put(POLY_ATTR_ID2, POLY_ATTR_VAL2);
        TEST_ATTRIBUTES.put(POLY_ID, polyAttr);

        // valid box
        final Map<String, Object> boxAttr = new HashMap<>();
        boxAttr.put(BOX_ATTR_ID, BOX_ATTR_VAL);
        TEST_ATTRIBUTES.put(BOX_ID, boxAttr);

        // attributes map for this shape is null
        TEST_ATTRIBUTES.put(NULL_ATTR_ID, null);

        // attributes map for this shape is empty
        TEST_ATTRIBUTES.put(EMPTY_ATTR_MAP_ID, new HashMap<>());

        // attribute has a null value
        final Map<String, Object> nullValAttr = new HashMap<>();
        nullValAttr.put(NULL_ATTR_VAL_ATTR_ID, null);
        TEST_ATTRIBUTES.put(NULL_ATTR_VAL_ID, nullValAttr);

        // id of this attribute map is not present in shapes
        final Map<String, Object> rogueAttr = new HashMap<>();
        rogueAttr.put("dummy", "dummy");
        TEST_ATTRIBUTES.put(ROGUE_ATTR_ID, null);

        return TEST_ATTRIBUTES;
    }

    // property names for the Geometry attribute in different geo file types
    private static final String GEOMETRY = "geometry";
    private static final String GEOMETRY_KML = "Geometry";
    private static final String GEOMETRY_SHAPEFILE = "the_geom";

    // flags the type of geo file under test to common verification methods
    private enum EXPORT_TYPE {
        GEOJSON,
        KML,
        GEOPACKAGE,
        SHAPEFILE
    }

    /**
     * Verifies a collection of geo features generated from the test shapes and
     * attributes. See the method comments of
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestShapes}
     * and
     * {
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestAttributes}
     * for a list of different cases exercised.
     */
    private void assertFeatures(final SimpleFeatureIterator it,
            final EXPORT_TYPE exportType) {
        try (it) {
            while (it.hasNext()) {
                final SimpleFeature feature = it.next();
                if (feature.getAttribute(NAME) == null) {
                    /* KML import includes the FeatureType in the list of
                       Features, ignore it */
                    continue;
                }

                // get a Map of values from the Feature to assert later
                final Map<String, Object> props = new HashMap<>();
                props.put(NAME, feature.getAttribute(NAME));
                if (exportType == EXPORT_TYPE.KML) {
                    // capitalised key for the geometry attribute
                    props.put(GEOMETRY, feature.getAttribute(GEOMETRY_KML));
                    // KML extended props are nested further than other export types
                    final Map<Object, Object> userData
                            = (Map<Object, Object>) feature.getUserData().get("UntypedExtendedData");
                    props.put(CENTRE_LAT, Double.parseDouble((String) userData.get(CENTRE_LAT)));
                    props.put(CENTRE_LON, Double.parseDouble((String) userData.get(CENTRE_LON)));
                    props.put(RADIUS, Double.parseDouble((String) userData.get(RADIUS)));
                    props.put(POINT_ATTR_ID1, userData.get(POINT_ATTR_ID1));
                    props.put(POINT_ATTR_ID2, userData.get(POINT_ATTR_ID2));
                    props.put(LINE_ATTR_ID, userData.get(LINE_ATTR_ID));
                    props.put(POLY_ATTR_ID1, userData.get(POLY_ATTR_ID1) != null
                            ? Double.parseDouble((String) userData.get(POLY_ATTR_ID1))
                            : null);
                    props.put(POLY_ATTR_ID2, userData.get(POLY_ATTR_ID2));
                    props.put(BOX_ATTR_ID, userData.get(BOX_ATTR_ID));
                } else {
                    props.put(CENTRE_LAT, feature.getAttribute(CENTRE_LAT));
                    props.put(CENTRE_LON, feature.getAttribute(CENTRE_LON));
                    props.put(RADIUS, feature.getAttribute(RADIUS));
                    props.put(POINT_ATTR_ID1, feature.getAttribute(POINT_ATTR_ID1));
                    props.put(POINT_ATTR_ID2, feature.getAttribute(POINT_ATTR_ID2));
                    props.put(LINE_ATTR_ID, feature.getAttribute(LINE_ATTR_ID));
                    props.put(POLY_ATTR_ID1, feature.getAttribute(POLY_ATTR_ID1));
                    props.put(POLY_ATTR_ID2, feature.getAttribute(POLY_ATTR_ID2));
                    props.put(BOX_ATTR_ID, feature.getAttribute(BOX_ATTR_ID));
                    if (exportType == EXPORT_TYPE.SHAPEFILE) {
                        // different key for geometry in shapefiles
                        props.put(GEOMETRY, feature.getAttribute(GEOMETRY_SHAPEFILE));
                    } else {
                        props.put(GEOMETRY, feature.getAttribute(GEOMETRY));
                    }
                }

                // assert the geometry shape
                final String geometryLc = ((Geometry) props.get(GEOMETRY))
                        .toString().toLowerCase();
                if (exportType == EXPORT_TYPE.SHAPEFILE) {
                    // shapefiles assume every shape in the file is the same type
                    assertTrue(geometryLc.startsWith(
                            MULTI_POLYGON.getGeomertyType().replace("_", "").toLowerCase()));
                } else if (exportType == EXPORT_TYPE.GEOJSON) {
                    /* The GeoJson functionality in geotools is unsupported. One of the
                       current bugs is that all shapes are considered polygons. The
                       actual shape type must therefore be asserted in the raw JSON. */
                    assertTrue(geometryLc.startsWith(POLYGON.getGeomertyType().toLowerCase()));
                } else {
                    if (POINT_ID.equals(props.get(NAME))
                            || NULL_ATTR_VAL_ID.equals(props.get(NAME))) {
                        assertTrue(geometryLc.startsWith(POINT.getGeomertyType().toLowerCase()));
                    } else if (LINE_ID.equals(props.get(NAME))
                            || EMPTY_ATTR_MAP_ID.equals(props.get(NAME))) {
                        assertTrue(geometryLc.startsWith(LINE.getGeomertyType().toLowerCase()));
                    } else {
                        assertTrue(geometryLc.startsWith(POLYGON.getGeomertyType().toLowerCase()));
                    }
                }

                // assert other values
                if (POINT_ID.equals(props.get(NAME))) {
                    assertTrue(geometryLc.contains(getFeatureCoords(POINT_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), POINT_LON); // not a typo!
                    assertEquals(props.get(CENTRE_LON), POINT_LAT); // not a typo!
                    assertEquals(props.get(RADIUS), POINT_RADIUS);
                    assertEquals(props.get(POINT_ATTR_ID1), POINT_ATTR_VAL1);
                    assertEquals(props.get(POINT_ATTR_ID2), POINT_ATTR_VAL2.toString());
                } else if (LINE_ID.equals(props.get(NAME))) {
                    assertTrue(geometryLc.contains(getFeatureCoords(LINE_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), LINE_CENTRE_LAT);
                    assertEquals(props.get(CENTRE_LON), LINE_CENTRE_LON);
                    assertEquals(props.get(RADIUS), LINE_RADIUS);
                    assertEquals(props.get(LINE_ATTR_ID).toString(), LINE_ATTR_VAL.toString());
                } else if (POLY_ID.equals(props.get(NAME))) {
                    assertTrue(geometryLc.contains(getFeatureCoords(POLY_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), POLY_CENTRE_LAT);
                    assertEquals(props.get(CENTRE_LON), POLY_CENTRE_LON);
                    assertEquals(props.get(RADIUS), POLY_RADIUS);
                    assertEquals(props.get(POLY_ATTR_ID1), POLY_ATTR_VAL1);
                    assertEquals(props.get(POLY_ATTR_ID2), POLY_ATTR_VAL2.toString());
                } else if (BOX_ID.equals(props.get(NAME))) {
                    assertTrue(geometryLc.contains(getFeatureCoords(BOX_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), BOX_CENTRE_LAT);
                    assertEquals(props.get(CENTRE_LON), BOX_CENTRE_LON);
                    assertEquals(props.get(RADIUS), BOX_RADIUS);
                    assertEquals(props.get(BOX_ATTR_ID), BOX_ATTR_VAL.toString());
                } else if (NULL_ATTR_ID.equals(props.get(NAME))) {
                    assertTrue(geometryLc.contains(getFeatureCoords(BOX_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), BOX_CENTRE_LAT);
                    assertEquals(props.get(CENTRE_LON), BOX_CENTRE_LON);
                    assertEquals(props.get(RADIUS), BOX_RADIUS);
                } else if (EMPTY_ATTR_MAP_ID.equals(props.get(NAME))) {
                    assertTrue(((Geometry) props.get(GEOMETRY)).toString()
                            .contains(getFeatureCoords(LINE_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), LINE_CENTRE_LAT);
                    assertEquals(props.get(CENTRE_LON), LINE_CENTRE_LON);
                    assertEquals(props.get(RADIUS), LINE_RADIUS);
                } else if (NULL_ATTR_VAL_ID.equals(props.get(NAME))) {
                    assertTrue(geometryLc.contains(getFeatureCoords(POINT_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), POINT_LON); // not a typo!
                    assertEquals(props.get(CENTRE_LON), POINT_LAT); // not a typo!
                    assertEquals(props.get(RADIUS), POINT_RADIUS);
                } else if (ROGUE_SHAPE_ID.equals(props.get(NAME))) {
                    assertTrue(geometryLc.contains(getFeatureCoords(POLY_COORDS)));
                    assertEquals(props.get(CENTRE_LAT), POLY_CENTRE_LAT);
                    assertEquals(props.get(CENTRE_LON), POLY_CENTRE_LON);
                    assertEquals(props.get(RADIUS), POLY_RADIUS);
                } else {
                    fail("Unexpected feature " + feature.getName());
                }
            }
        }
    }

    // returns a list of coords in the format a geo Feature expects
    private String getFeatureCoords(final List<Tuple<Double, Double>> coords) {
        final List<String> fc = new ArrayList<>();
        for (Tuple<Double, Double> c : coords) {
            fc.add(c.getFirst() + " " + c.getSecond());
        }
        return String.join(", ", fc);
    }

    // part of GeoJson that indicates the presence of a Feature
    private static final String FEATURE_MARKER = "\"type\":\"Feature\"";

    /**
     * Can construct GeoJson to represent a collection of shapes with
     * attributes. The test collection includes a number of different shapes so
     * that a variety of different decision points can be verified. See
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestShapes}
     * and
     * {
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestAttributes}.
     *
     * @throws IOException if unable to generate shape collection GeoJson
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShapeCollection() throws IOException {
        // generate the shape collection
        final String s = Shape.generateShapeCollection(
                UUID.randomUUID().toString(), // the UUID is never used
                getTestShapes(), getTestAttributes());

        // convert GeoJson string to a list of Features
        final FeatureJSON featureJson = new FeatureJSON(new GeometryJSON());
        featureJson.setFeatureType(featureJson.readFeatureCollectionSchema(s, false));
        final FeatureCollection featureCollection;
        try (final Reader stringReader = new StringReader(s)) {
            featureCollection = featureJson.readFeatureCollection(stringReader);
        }

        /* FeatureJSON and GeometryJSON are unsupported and have several
           limitations, one being that they assume all shapes in the collection
           will be the same geometry type. So verify geometry types in the raw
           JSON. */
        final String[] features = s.split(FEATURE_MARKER);
        for (final String f : Arrays.copyOfRange(features, 1, features.length - 1)) {
            if (f.contains(POINT_ID) || f.contains(NULL_ATTR_VAL_ID)) {
                assertTrue(f.contains(POINT.getGeomertyType()));
            } else if (f.contains(LINE_ID) || f.contains(EMPTY_ATTR_MAP_ID)) {
                assertTrue(f.contains(LINE.getGeomertyType()));
            } else {
                assertTrue(f.contains(POLYGON.getGeomertyType()));
            }
        }

        // and now verify the rest of the feature data
        assertEquals(featureCollection.size(), TEST_SHAPES.size());
        assertFeatures(
                (SimpleFeatureIterator) featureCollection.features(),
                EXPORT_TYPE.GEOJSON);
    }

    /**
     * Invalid shapes throw errors that are ignored and all other shapes are
     * added to the shape collection.
     *
     * Will an IOException ever be thrown here though? The FeatureIterator is
     * created no matter what junk is thrown at it. However when iterating
     * through Features the FeatureIterator is quite strict and errors at the
     * smallest problem - with RuntimeExceptions. And ByteArrayInputStream does
     * not throw IOExceptions. The geotools library specifically states that an
     * IOException may be thrown, and that's their right to say so, therefore
     * this test was created to guard against that eventuality.
     *
     * @throws IOException if can't get SRS from CRS
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShapeCollectionInvalidShape() throws IOException {
        final Map<String, String> shapes = new HashMap<>();
        shapes.put(POINT_ID, Shape.generateShape(POINT_ID, POINT, POINT_COORDS));
        shapes.put(LINE_ID, Shape.generateShape(LINE_ID, LINE, LINE_COORDS));

        try ( MockedConstruction<FeatureJSON> mockFeatureJson = Mockito.mockConstruction(FeatureJSON.class,
                (mock, context) -> {
                    // throw Exception on first call, then call real method on all other calls
                    when(mock.streamFeatureCollection(any()))
                            .thenThrow(IOException.class)
                            .thenCallRealMethod();
                    /* The above constructor mocking seems to interfere with FeatureJSON writing to
                       the output stream, so short-circuit that call and write a dummy value, and
                       the verification will check the FeatureCollection passed to the write method. */
                    doAnswer(invocation -> {
                        ((OutputStream) invocation.getArguments()[1]).write("shape".getBytes());
                        return null;
                    }).when(mock).writeFeatureCollection(any(FeatureCollection.class), any(OutputStream.class));
                })) {

            Shape.generateShapeCollection("dummy", shapes, Collections.emptyMap());

            // check that the Feature write was only called once
            final ArgumentCaptor<FeatureCollection> fcCaptor
                    = ArgumentCaptor.forClass(FeatureCollection.class);
            verify(mockFeatureJson.constructed().get(0), times(1))
                    .writeFeatureCollection(fcCaptor.capture(), any());

            // check that the FeatureCollection to be output has only one Feature
            final FeatureCollection fc = fcCaptor.getValue();
            assertEquals(fc.size(), 1);

            // check that the written Feature is as expected
            try (final SimpleFeatureIterator it = (SimpleFeatureIterator) fc.features()) {
                final SimpleFeature feature = (SimpleFeature) it.next();
                final String featureName = (String) feature.getAttribute(NAME);
                assertTrue(featureName.equals(POINT_ID) || featureName.equals(LINE_ID));
            }

            // check that the class attempted to get features from both shapes
            verify(mockFeatureJson.constructed().get(0), times(2))
                    .streamFeatureCollection(any());
        }
    }

    /**
     * Can generate GeoJson when different method parameters are either empty or
     * null.
     *
     * @throws IOException if unable to generate shape GeoJson
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShapeCollectionParams() throws IOException {
        // empty shape collection
        final String noShapes = Shape.generateShapeCollection(
                "dummy", Collections.emptyMap(), getTestAttributes());
        assertFalse(noShapes.contains(FEATURE_MARKER));

        // emtpy attribute collection
        final String noAttr = Shape.generateShapeCollection(
                "dummy", getBasicTestShapes(), Collections.emptyMap());
        assertEquals(noAttr.split(FEATURE_MARKER).length - 1, BASIC_TEST_SHAPES.size());

        // null attribute collection
        final String nullAttr = Shape.generateShapeCollection(
                "dummy", getBasicTestShapes(), null);
        assertEquals(nullAttr.split(FEATURE_MARKER).length - 1, BASIC_TEST_SHAPES.size());
    }

    /**
     * Can construct KML to represent a collection of shapes with attributes.
     * The test collection includes a number of different shapes so that a
     * variety of different decision points can be verified. See
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestShapes}
     * and
     * {
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestAttributes}.
     *
     * @throws IOException if unable to generate shape KML
     * @throws XMLStreamException if features cannot be streamed from KML
     * @throws SAXException if features cannot be parsed from KML
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateKml() throws IOException, XMLStreamException, SAXException {
        // generate KML
        final String id = "kmlId";
        final String kml = Shape.generateKml(id, getTestShapes(), getTestAttributes());

        // check that the KML ID is as expected
        assertTrue(kml.contains("Document id=\"" + id));

        // get Features from KML String
        final PullParser parser = new PullParser(
                new KMLConfiguration(),
                new ByteArrayInputStream(kml.getBytes()),
                SimpleFeature.class);

        final List<SimpleFeature> features = new ArrayList<>();
        SimpleFeature simpleFeature = (SimpleFeature) parser.parse();
        while (simpleFeature != null) {
            features.add(simpleFeature);
            simpleFeature = (SimpleFeature) parser.parse();
        }
        final SimpleFeatureCollection fc = DataUtilities.collection(features);

        // size-1 because KML adds the FeatureType into the list of Features
        assertEquals(fc.size() - 1, TEST_SHAPES.size());
        assertFeatures(fc.features(), EXPORT_TYPE.KML);
    }

    private static final String KML_PLACEMARK = "<kml:Placemark id=\"";

    /**
     * Invalid shapes throw errors that are ignored and all other shapes are
     * added to the shape collection.
     *
     * Will an IOException ever be thrown here though? The FeatureIterator is
     * created no matter what junk is thrown at it. However when iterating
     * through Features the FeatureIterator is quite strict and errors at the
     * smallest problem - with RuntimeExceptions. And ByteArrayInputStream does
     * not throw IOExceptions. The geotools library specifically states that an
     * IOException may be thrown, and that's their right to say so, therefore
     * this test was created to guard against that eventuality.
     *
     * @throws IOException if can't get SRS from CRS
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateKmlInvalidShape() throws IOException {
        final Map<String, String> shapes = new HashMap<>();
        shapes.put(POINT_ID, Shape.generateShape(POINT_ID, POINT, POINT_COORDS));
        shapes.put(LINE_ID, Shape.generateShape(LINE_ID, LINE, LINE_COORDS));

        try ( MockedConstruction<FeatureJSON> mockFeatureJson = Mockito.mockConstruction(FeatureJSON.class,
                (mock, context) -> {
                    // throw exception on first call, then call real method on all other calls
                    when(mock.streamFeatureCollection(any())).thenThrow(IOException.class).thenCallRealMethod();
                })) {

            final String kml = Shape.generateKml("dummy", shapes, Collections.emptyMap());

            /* One shape will error and be ignored, one shape will pass and be
               output. Assert by checking that a placemark for point OR line is
               present, but not both. */
            final String pointPlacemark = KML_PLACEMARK + POINT_ID;
            final String linePlacemark = KML_PLACEMARK + LINE_ID;
            assertTrue(kml.contains(pointPlacemark) || kml.contains(linePlacemark));
            assertFalse(kml.contains(pointPlacemark) && kml.contains(linePlacemark));

            // check that the class attempted to get features from both shapes
            verify(mockFeatureJson.constructed().get(0), times(2))
                    .streamFeatureCollection(any());
        }
    }

    /**
     * Can generate KML when different method parameters are either empty or
     * null.
     *
     * @throws IOException if unable to generate shape KML
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateKmlParams() throws IOException {
        // empty shape collection
        final String noShapes = Shape.generateKml(
                "dummy", Collections.emptyMap(), getTestAttributes());
        assertFalse(noShapes.contains(KML_PLACEMARK));

        // emtpy attribute collection
        final String noAttr = Shape.generateKml(
                "dummy", getBasicTestShapes(), Collections.emptyMap());
        assertEquals(noAttr.split(KML_PLACEMARK).length, 5);

        // null attribute collection
        final String nullAttr = Shape.generateKml(
                "dummy", getBasicTestShapes(), null);
        assertEquals(nullAttr.split(KML_PLACEMARK).length, 5);
    }

    /* Convenience method to get a data store providing access to a test
       GeoPackage residing on the filesystem. */
    private DataStore getGeoPackageStore(final File f) throws IOException {
        final Map<String, Object> map = new HashMap<>();
        map.put(GeoPkgDataStoreFactory.DBTYPE.key, "geopkg");
        map.put(GeoPkgDataStoreFactory.DATABASE.key, f.getAbsolutePath());
        return DataStoreFinder.getDataStore(map);
    }

    /**
     * Can construct a GeoPackage to represent a collection of shapes with
     * attributes. The test collection includes a number of different shapes so
     * that a variety of different decision points can be verified. See
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestShapes}
     * and
     * {
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestAttributes}.
     *
     * @throws IOException if unable to generate shape GeoPackage
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateGeoPackage() throws IOException {
        final File f = File.createTempFile("tmp", "file");
        final String id = "geoPkgId";
        Shape.generateGeoPackage(id, getTestShapes(), getTestAttributes(),
                f, Shape.SpatialReference.WGS84);

        DataStore store = null;
        try {
            store = getGeoPackageStore(f);

            assertEquals(id, store.getTypeNames()[0]);
            final SimpleFeatureCollection features
                    = store.getFeatureSource(id).getFeatures();
            assertEquals(features.size(), TEST_SHAPES.size());
            assertFeatures(features.features(), EXPORT_TYPE.GEOPACKAGE);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    /**
     * Invalid shapes throw errors that are ignored and all other shapes are
     * added to the GeoPackage.
     *
     * Will an IOException ever be thrown here though? The FeatureIterator is
     * created no matter what junk is thrown at it. However when iterating
     * through Features the FeatureIterator is quite strict and errors at the
     * smallest problem - with RuntimeExceptions. And ByteArrayInputStream does
     * not throw IOExceptions. The geotools library specifically states that an
     * IOException may be thrown, and that's their right to say so, therefore
     * this test was created to guard against that eventuality.
     *
     * @throws IOException if can't get SRS from CRS
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateGeoPackageInvalidShape() throws IOException {
        final Map<String, String> shapes = new HashMap<>();
        shapes.put(POINT_ID, Shape.generateShape(POINT_ID, POINT, POINT_COORDS));
        shapes.put(LINE_ID, Shape.generateShape(LINE_ID, LINE, LINE_COORDS));

        /* FeatureJSON is deeply entrenched in GeoPackage, the class that takes
           a FeatureCollection and converts it to a GeoPackage, meaning that
           a constructor mocked FeatureJSON needs to behave more like a spy
           object than a mock object. For some reason at the time of writing
           providing CALLS_REAL_METHODS to a constructor mocked FeatureJSON in a
           MockSettings object isn't working - when added ClassCastExceptions
           are generated within geotools. Mockito can't constructor mock
           GeoPackage, it throws ByteBuddy errors. That leaves one approach
           left - mock FeatureJSON to cause the desired error behaviour while
           creating the FeatureCollection, allow GeoPackage to throw an
           Exception, catch the Exception and verify captured arguments from
           constructor mocks. Feel free to re-write later! */
        try ( MockedConstruction<FeatureJSON> mockFeatureJson = Mockito.mockConstruction(FeatureJSON.class,
                (mock, context) -> {
                    // throw exception on first call, then call real method on all other calls
                    when(mock.streamFeatureCollection(any())).thenThrow(IOException.class).thenCallRealMethod();
                })) {
            // mock construction of DefaultFeatureCollection so we can verify the Features generated
            try ( MockedConstruction<DefaultFeatureCollection> mockFeatures = Mockito.mockConstruction(DefaultFeatureCollection.class)) {
                final String geoPkgId = "geoPkgId";
                final File f = File.createTempFile("tmp", "file");

                try {
                    // will throw a NullPointer Exception, ignore it
                    Shape.generateGeoPackage(geoPkgId, shapes,
                            Collections.emptyMap(), f,
                            Shape.SpatialReference.WGS84);
                } catch (NullPointerException e) {
                    // do nothing
                }

                // check that only one expected feature was generated
                ArgumentCaptor<DefaultFeatureCollection> captor
                        = ArgumentCaptor.forClass(DefaultFeatureCollection.class);
                verify(mockFeatures.constructed().get(0)).addAll((Collection<SimpleFeature>) captor.capture());
                final List<SimpleFeature> features = (List) captor.getValue();
                assertEquals(features.size(), 1);
                final String featureName = (String) features.get(0).getAttribute(NAME);
                assertTrue(featureName.equals(POINT_ID) || featureName.equals(LINE_ID));

                // check that the class attempted to get features from both shapes
                verify(mockFeatureJson.constructed().get(0), times(2))
                        .streamFeatureCollection(any());
            }
        }

    }

    /**
     * Can generate a GeoPackage when different method parameters are either
     * empty or null.
     *
     * @throws IOException if unable to generate shape GeoPackage
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateGeoPackageParams() throws IOException {
        final String geoPkgId = "geoPkgId";

        // empty shape collection
        final File noShapesFile = File.createTempFile("tmp", "file");
        Shape.generateGeoPackage(geoPkgId, Collections.emptyMap(),
                getTestAttributes(), noShapesFile, Shape.SpatialReference.WGS84);
        DataStore noShapesStore = null;
        try {
            noShapesStore = getGeoPackageStore(noShapesFile);
            final SimpleFeatureCollection noShapes
                    = noShapesStore.getFeatureSource(geoPkgId).getFeatures();
            assertTrue(noShapes.size() == 0);
        } finally {
            if (noShapesStore != null) {
                noShapesStore.dispose();
            }
        }

        // emtpy attribute collection
        final File noAttrFile = File.createTempFile("tmp", "file");
        Shape.generateGeoPackage(geoPkgId, getBasicTestShapes(), Collections.emptyMap(),
                noAttrFile, Shape.SpatialReference.WGS84);
        DataStore noAttrStore = null;
        try {
            noAttrStore = getGeoPackageStore(noAttrFile);
            final SimpleFeatureCollection noAttr
                    = noAttrStore.getFeatureSource(geoPkgId).getFeatures();
            assertTrue(noAttr.size() == BASIC_TEST_SHAPES.size());
        } finally {
            if (noAttrStore != null) {
                noAttrStore.dispose();
            }
        }

        // null attribute collection
        final File nullAttrFile = File.createTempFile("tmp", "file");
        Shape.generateGeoPackage(geoPkgId, getBasicTestShapes(), null,
                nullAttrFile, Shape.SpatialReference.WGS84);
        DataStore nullAttrStore = null;
        try {
            nullAttrStore = getGeoPackageStore(nullAttrFile);
            final SimpleFeatureCollection nullAttr
                    = nullAttrStore.getFeatureSource(geoPkgId).getFeatures();
            assertTrue(nullAttr.size() == BASIC_TEST_SHAPES.size());
        } finally {
            if (nullAttrStore != null) {
                nullAttrStore.dispose();
            }
        }
    }

    /**
     * GeoPackage attribute names are created in a SQLLite compatible format.
     *
     * @throws IOException if unable to generate shape GeoPackage
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateGeoPackageAttributeNames() throws IOException {
        final Map<String, String> shapes = new HashMap<>();
        shapes.put(POINT_ID, Shape.generateShape(POINT_ID, POINT, POINT_COORDS));

        final String id1 = POINT_ATTR_ID1;
        final String id2 = ".a.test.id.";
        final String id2compatible = "_a_test_id_";
        final String id3 = ".._._.";
        final String id3compatible = "______";
        final String id4 = "...............";
        final String id4compatible = "_______________";
        final Map<String, Map<String, Object>> attributes = new HashMap<>();
        final Map<String, Object> pointAttr = new HashMap<>();
        pointAttr.put(id1, id1);
        pointAttr.put(id2, id2);
        pointAttr.put(id3, id3);
        pointAttr.put(id4, id4);
        attributes.put(POINT_ID, pointAttr);

        final String geoPkgId = "geoPkgId";
        final File f = File.createTempFile("tmp", "file");
        Shape.generateGeoPackage(geoPkgId, shapes, attributes,
                f, Shape.SpatialReference.WGS84_WEB_MERCATOR);

        DataStore store = null;
        try {
            store = getGeoPackageStore(f);
            try (final SimpleFeatureIterator it
                    = store.getFeatureSource(geoPkgId).getFeatures().features()) {
                SimpleFeature feature = it.next();
                assertEquals(id1, feature.getAttribute(id1));
                assertEquals(id2, feature.getAttribute(id2compatible));
                assertEquals(id3, feature.getAttribute(id3compatible));
                assertEquals(id4, feature.getAttribute(id4compatible));
            }
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    /* Convenience method to get a data store providing access to a test
       Shapefile residing on the filesystem. */
    private DataStore getShapefileStore(final File f) throws IOException {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("url", f.toURI().toURL());
        return DataStoreFinder.getDataStore(map);
    }

    // Shapefiles must be an expected file extension, such as .shp
    private static final String SHP_EXT = ".shp";

    /**
     * Can construct a Shapefile to represent a collection of shapes with
     * attributes. The test collection includes a number of different shapes so
     * that a variety of different decision points can be verified. See
     * {
     *
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestShapes}
     * and
     * {
     * @see
     * au.gov.asd.tac.constellation.utilities.geospatial.ShapeNGTeset.getTestAttributes}.
     *
     * @throws IOException if unable to generate Shapefile
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShapefile() throws IOException {
        final File f = File.createTempFile("tmp", SHP_EXT);
        Shape.generateShapefile("not_used", Shape.GeometryType.POLYGON,
                getTestShapes(), getTestAttributes(), f,
                Shape.SpatialReference.WGS84);

        DataStore store = null;
        try {
            store = getShapefileStore(f);
            final String id = store.getTypeNames()[0];
            final String expectedId = f.getName() // filename without file extension
                    .substring(0, f.getName().lastIndexOf("."));
            assertEquals(expectedId, id);

            assertFeatures(store.getFeatureSource(id).getFeatures().features(),
                    EXPORT_TYPE.SHAPEFILE);
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

    /**
     * Invalid shapes throw errors that are ignored and all other shapes are
     * added to the Shapefile.
     *
     * Will an IOException ever be thrown here though? The FeatureIterator is
     * created no matter what junk is thrown at it. However when iterating
     * through Features the FeatureIterator is quite strict and errors at the
     * smallest problem - with RuntimeExceptions. And ByteArrayInputStream does
     * not throw IOExceptions. The geotools library specifically states that an
     * IOException may be thrown, and that's their right to say so, therefore
     * this test was created to guard against that eventuality.
     *
     * @throws IOException if can't get SRS from CRS
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShapefileInvalidShape() throws IOException {
        final Map<String, String> shapes = new HashMap<>();
        shapes.put(POLY_ID, Shape.generateShape(POLY_ID, POLYGON, POLY_COORDS));
        shapes.put(BOX_ID, Shape.generateShape(BOX_ID, POLYGON, BOX_COORDS));

        try ( MockedConstruction<FeatureJSON> mockFeatureJson = Mockito.mockConstruction(FeatureJSON.class,
                (mock, context) -> {
                    // throw exception on first call, then call real method on all other calls
                    when(mock.streamFeatureCollection(any())).thenThrow(IOException.class).thenCallRealMethod();
                })) {

            final File f = File.createTempFile("tmp", SHP_EXT);
            Shape.generateShapefile("dummy", POLYGON,
                    shapes, Collections.emptyMap(), f,
                    Shape.SpatialReference.WGS84);

            DataStore store = null;
            try {
                store = getShapefileStore(f);
                // check that only one expected feature was generated
                final SimpleFeatureCollection features
                        = store.getFeatureSource(store.getTypeNames()[0]).getFeatures();
                assertEquals(features.size(), 1);

                // check that only one expected feature was generated
                try ( SimpleFeatureIterator it = features.features()) {
                    final String featureName = (String) it.next().getAttribute(NAME);
                    assertTrue(featureName.equals(POLY_ID)
                            || featureName.equals(BOX_ID));
                }
            } finally {
                if (store != null) {
                    store.dispose();
                }
            }

            // check that the class attempted to get features from both shapes
            verify(mockFeatureJson.constructed().get(0), times(2)).streamFeatureCollection(any());
        }
    }

    /**
     * Can generate a Shapefile when different method parameters are either
     * empty or null.
     *
     * @throws IOException if unable to generate Shapefile
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShapefileParams() throws IOException {
        final String geoPkgId = "geoPkgId";

        // empty shape collection
        final File noShapesFile = File.createTempFile("tmp", SHP_EXT);
        Shape.generateShapefile(geoPkgId, POLYGON, Collections.emptyMap(),
                getTestAttributes(), noShapesFile, Shape.SpatialReference.WGS84);
        DataStore noShapesStore = null;
        try {
            noShapesStore = getShapefileStore(noShapesFile);
            final SimpleFeatureCollection noShapes
                    = noShapesStore.getFeatureSource(noShapesStore.getTypeNames()[0]).getFeatures();
            assertTrue(noShapes.size() == 0);
        } finally {
            if (noShapesStore != null) {
                noShapesStore.dispose();
            }
        }

        // emtpy attribute collection
        final File noAttrFile = File.createTempFile("tmp", SHP_EXT);
        Shape.generateShapefile(geoPkgId, POLYGON, getBasicTestShapes(), Collections.emptyMap(),
                noAttrFile, Shape.SpatialReference.WGS84);
        DataStore noAttrStore = null;
        try {
            noAttrStore = getShapefileStore(noAttrFile);
            final SimpleFeatureCollection noAttr
                    = noAttrStore.getFeatureSource(noAttrStore.getTypeNames()[0]).getFeatures();
            assertTrue(noAttr.size() == BASIC_TEST_SHAPES.size());
        } finally {
            if (noAttrStore != null) {
                noAttrStore.dispose();
            }
        }

        // null attribute collection
        final File nullAttrFile = File.createTempFile("tmp", SHP_EXT);
        Shape.generateShapefile(geoPkgId, POLYGON, getBasicTestShapes(), null,
                nullAttrFile, Shape.SpatialReference.WGS84);
        DataStore nullAttrStore = null;
        try {
            nullAttrStore = getShapefileStore(nullAttrFile);
            final SimpleFeatureCollection nullAttr
                    = nullAttrStore.getFeatureSource(nullAttrStore.getTypeNames()[0]).getFeatures();
            assertTrue(nullAttr.size() == BASIC_TEST_SHAPES.size());
        } finally {
            if (nullAttrStore != null) {
                nullAttrStore.dispose();
            }
        }
    }

    /**
     * Shapefile attribute names are created in a compatible format.
     *
     * @throws IOException if unable to generate Shapefile
     */
    @Test(dependsOnGroups = {"ShapeNGTest.emptyCrsCache"})
    public void testGenerateShapefileAttributeNames() throws IOException {
        final Map<String, String> shapes = new HashMap<>();
        shapes.put(POINT_ID, Shape.generateShape(POINT_ID, POINT, POINT_COORDS));

        final String id1 = POINT_ATTR_ID1;
        final String id2 = ".this.is.a.header.";
        final String id2compatible = "header";
        final String id3 = "this.is.a.header.diff";
        final String id3compatible = "thisdiff";
        final String id4 = "this.aheadr";
        final String id4compatible = "thisaheadr";
        final String id5 = ".thisheadra";
        final String id5compatible = "thisheadra";
        final String id6 = "aheadrthis.";
        final String id6compatible = "aheadrthis";
        final String id7 = "source-src";
        final String id7compatible = "s-src";
        final String id8 = "destination-dest";
        final String id8compatible = "d-dest";
        final String id9 = "transaction-trs";
        final String id9compatible = "t-trs";
        final String id10 = "12345678901234567890";
        final String id10compatible = "1234567890";

        final Map<String, Map<String, Object>> attributes = new HashMap<>();
        final Map<String, Object> pointAttr = new HashMap<>();
        pointAttr.put(id1, id1);
        pointAttr.put(id2, id2);
        pointAttr.put(id3, id3);
        pointAttr.put(id4, id4);
        pointAttr.put(id5, id5);
        pointAttr.put(id6, id6);
        pointAttr.put(id7, id7);
        pointAttr.put(id8, id8);
        pointAttr.put(id9, id9);
        pointAttr.put(id10, id10);
        attributes.put(POINT_ID, pointAttr);

        final File f = File.createTempFile("tmp", SHP_EXT);
        Shape.generateShapefile("shapefileId", POINT, shapes, attributes,
                f, Shape.SpatialReference.WGS84_WEB_MERCATOR);

        DataStore store = null;
        try {
            store = getShapefileStore(f);
            try ( SimpleFeatureIterator it
                    = store.getFeatureSource(store.getTypeNames()[0]).getFeatures().features()) {
                final SimpleFeature feature = it.next();
                assertEquals(id1, feature.getAttribute(id1));
                assertEquals(id2, feature.getAttribute(id2compatible));
                assertEquals(id3, feature.getAttribute(id3compatible));
                assertEquals(id4, feature.getAttribute(id4compatible));
                assertEquals(id5, feature.getAttribute(id5compatible));
                assertEquals(id6, feature.getAttribute(id6compatible));
                assertEquals(id7, feature.getAttribute(id7compatible));
                assertEquals(id8, feature.getAttribute(id8compatible));
                assertEquals(id9, feature.getAttribute(id9compatible));
                assertEquals(id10, feature.getAttribute(id10compatible));
            }
        } finally {
            if (store != null) {
                store.dispose();
            }
        }
    }

}
