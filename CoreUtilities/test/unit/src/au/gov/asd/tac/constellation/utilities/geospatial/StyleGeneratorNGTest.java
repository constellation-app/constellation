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

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Stroke;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.style.Style;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class StyleGeneratorNGTest {
    
    private static final Logger LOGGER = Logger.getLogger(StyleGenerator.class.getName());
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
    
    private static final String ZERO_FEATURES_MSG = 
            "No features available to generate style";
    private static final String UNSUPPORTED_GEOMETRY_PATTERN = 
            "Style cannot be generated from type: %s";
    
    private static Handler[] existingLogHandlers;
    
    // removes all handlers from a logger
    private void removeHandlers(final Logger logger, final Handler[] handlers) {
        for (final Handler h : handlers) {
            logger.removeHandler(h);
        }
    }
    
    /**
     * Attaches customLogHandler to the FontUtilities logger, which will also
     * receive logging events, and removes the class console logger.
     */
    @BeforeClass
    public void attachLogCapturer() {
        // remove the existing handlers, but store them so they can be restored
        existingLogHandlers = LOGGER.getParent().getHandlers();
        removeHandlers(LOGGER.getParent(), existingLogHandlers);
        
        // add a custom handler based off the first existing handler
        logCapturingStream = new ByteArrayOutputStream();
        customLogHandler = new StreamHandler(logCapturingStream, 
                existingLogHandlers[0].getFormatter());
        LOGGER.getParent().addHandler(customLogHandler);
    }
    
    /**
     * Removes the Handler from the FontUtilities logger and restores
     * the console logger.
     */
    @AfterClass
    public void removeLogCapturer() {
        removeHandlers(LOGGER.getParent(), LOGGER.getParent().getHandlers());
        for (final Handler h : existingLogHandlers) {
            LOGGER.getParent().addHandler(h);
        }
    }
    
    /**
     * Gets any logs captured so far by the customLogHandler.
     * 
     * @return any logs captured
     * @throws IOException if logs can't be retrieved
     */
    public String getCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }
    
    /* StyleGenerator returns styles for Polygon, MultiPolygon, LineString and
       Point. It will return styles for any Geometry type class that is 
       assignable to any of the four aforementioned types. Test this by using
       dummy subclasses of each type. */
    public class TestPolygon extends Polygon {
        public TestPolygon(final LinearRing shell, final LinearRing[] holes, 
                final GeometryFactory factory) {
            super(shell, holes, factory);
        }
    }
    public class TestMultiPolygon extends MultiPolygon {
        public TestMultiPolygon(final Polygon[] polygons, 
                final GeometryFactory factory) {
            super(polygons, factory);
        }
    }
    public class TestLineString extends LineString {
        public TestLineString(final CoordinateSequence points, 
                final GeometryFactory factory) {
            super(points, factory);
        }
    }
    public class TestPoint extends Point {
        public TestPoint(final CoordinateSequence coordinates, 
                final GeometryFactory factory) {
            super(coordinates, factory);
        }
    }
    
    /**
     * Returns null and outputs a log message when an attempt is made to get a 
     * style from an empty Feature collection.
     * 
     * @throws IOException if logs cannot be captured
     */
    @Test
    public void testZeroFeatures() throws IOException {
        final SimpleFeatureCollection sfc = mock(SimpleFeatureCollection.class);
        assertNull(StyleGenerator.createStyle(sfc));
        assertTrue(getCapturedLog().contains(ZERO_FEATURES_MSG));
    }
    
    /* Convenience method to get a mock SimpleFeatureCollection that will 
       return the desired Geometry type class */
    private SimpleFeatureCollection getSfcMock(final Class geometryType) {
        final SimpleFeatureCollection sfc = mock(SimpleFeatureCollection.class);
        final SimpleFeatureType sft = mock(SimpleFeatureType.class);
        final GeometryDescriptor gd = mock(GeometryDescriptor.class);
        final GeometryType gt = mock(GeometryType.class);
        when(sfc.size()).thenReturn(1);
        when(sfc.getSchema()).thenReturn(sft);
        when(sft.getGeometryDescriptor()).thenReturn(gd);
        when(gd.getType()).thenReturn(gt);
        when(gt.getBinding()).thenReturn(geometryType);
        return sfc;
    }
    
    /**
     * Returns null and outputs a log message when an attempt is made to get a 
     * style for a Geometry type not assignable to either Polygon, MultiPolygon,
     * LineString or Point.
     * 
     * @throws IOException if logs cannot be captured
     */
    @Test
    public void testUnsupportedGeometry() throws IOException {
        final SimpleFeatureCollection sfc = getSfcMock(Graphic.class);
        assertNull(StyleGenerator.createStyle(sfc));
        assertTrue(getCapturedLog().contains(
                String.format(UNSUPPORTED_GEOMETRY_PATTERN, Graphic.class)));
    }
    
    /* convenience method to check the correct style is returned for both
       Polygon and MultiPolygon Geometry types, which are exactly the same */
    private void assertPolygonStyle(final Style s) {
        // check the title
        assertEquals(s.getDescription().getTitle().toString(), "Polygon Style");
        
        // check the components of the style
        final PolygonSymbolizer ps = (PolygonSymbolizer) 
                s.featureTypeStyles().get(0).rules().get(0).symbolizers().get(0);
        
        assertNull(ps.getGeometryPropertyName());
        
        final Fill fill = ps.getFill();
        /* the color we get is the next one the static inner class decides to 
           give us, which may not be deterministic */
        assertTrue(fill.getColor().evaluate(null, Color.class) instanceof Color);
        final double fillOpacity = fill.getOpacity().evaluate(null, Double.class);
        assertEquals(fillOpacity, 0.5D);
        
        final Stroke stroke = ps.getStroke();
        final Color strokeColor = stroke.getColor().evaluate(null, Color.class);
        assertEquals(strokeColor, Color.BLACK);
        final double strokeOpacity = stroke.getOpacity().evaluate(null, Double.class);
        assertEquals(strokeOpacity, 0.5D);
        final double strokeWidth = stroke.getWidth().evaluate(null, Double.class);
        assertEquals(strokeWidth, 1D);
    }
    
    /**
     * Can get the style for the Polygon Geometry type.
     */
    @Test
    public void testPolygonStyle() {
        assertPolygonStyle(
                StyleGenerator.createStyle(getSfcMock(TestPolygon.class)));
    }
    
    /**
     * Can get the style for the MultiPolygon Geometry type.
     */
    @Test
    public void testMultiPolygonStyle() {
        assertPolygonStyle(
                StyleGenerator.createStyle(getSfcMock(TestMultiPolygon.class)));
    }
    
    /**
     * Every time a Polygon style is generated StyleGenerator will attempt to 
     * use a different colour. This is based on the number of times an internal
     * static class is called meaning that the colour returned may not be
     * deterministic within a suite of tests running in parallel. It's also 
     * possible (albeit unlikely) that the same colour is returned more than 
     * once. However, it is possible to test that StyleGenerator is constructing 
     * a new Color object every time a Polygon style is generated.
     */
    @Test
    public void testPolygonColors() {
        // get 10 colors
        final List<Color> colors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final Color c = ((PolygonSymbolizer) 
                    StyleGenerator.createStyle(getSfcMock(TestPolygon.class))
                            .featureTypeStyles().get(0)
                            .rules().get(0)
                            .symbolizers().get(0))
                    .getFill()
                    .getColor().evaluate(this, Color.class);
            colors.add(c);
        }
        
        // check that each Color object appears in the list exactly one time
        for (final Color color : colors) {
            int count = 0;
            for (final Color cmp : colors) {
                if (color == cmp) {
                    count++;
                }
            }
            assertEquals(count, 1, "StyleGenerator can return the same "
                    + "Color twice, but should never return the same Color "
                    + "object twice.");
        }
    }
    
    /**
     * Can get the style for the LineString Geometry type.
     */
    @Test
    public void testLineStringStyle() {
        final Style s = StyleGenerator.createStyle(getSfcMock(TestLineString.class));
        
        // check the title
        assertEquals(s.getDescription().getTitle().toString(), "Line Style");
        
        // check the components of the style
        final PolygonSymbolizer ps = (PolygonSymbolizer) 
                s.featureTypeStyles().get(0).rules().get(0).symbolizers().get(0);
        
        assertNull(ps.getGeometryPropertyName());
        
        final Fill fill = ps.getFill();
        /* the color we get is the next one the static inner class decides to 
           give us, which may not be deterministic */
        final Color fillColor = fill.getColor().evaluate(null, Color.class);
        assertEquals(fillColor, Color.RED);
        final double fillOpacity = fill.getOpacity().evaluate(null, Double.class);
        assertEquals(fillOpacity, 0.25D);
        
        final Stroke stroke = ps.getStroke();
        final Color strokeColor = stroke.getColor().evaluate(null, Color.class);
        assertEquals(strokeColor, Color.WHITE);
        final double strokeOpacity = stroke.getOpacity().evaluate(null, Double.class);
        assertEquals(strokeOpacity, 0.5D);
        final double strokeWidth = stroke.getWidth().evaluate(null, Double.class);
        assertEquals(strokeWidth, 1D);
    }
    
    /**
     * Can get the style for the Point Geometry type.
     */
    @Test
    public void testPointStyle() {
        final Style s = StyleGenerator.createStyle(getSfcMock(TestPoint.class));
        
        // check the title
        assertEquals(s.getDescription().getTitle().toString(), "Point Style");
        
        // check the components of the style
        final PointSymbolizer ps = (PointSymbolizer) 
                s.featureTypeStyles().get(0).rules().get(0).symbolizers().get(0);
        
        assertNull(ps.getGeometryPropertyName());
        
        final Graphic graphic = ps.getGraphic();
        final double graphicSize = graphic.getSize().evaluate(null, Double.class);
        assertEquals(graphicSize, 5D);
        
        assertEquals(graphic.graphicalSymbols().size(), 1);
        final Mark mark = (Mark) graphic.graphicalSymbols().get(0);
        assertEquals(
                mark.getFill().getColor().evaluate(null, Color.class), 
                Color.CYAN);
        final Stroke markStroke = mark.getStroke();
        final Color strokeColor = markStroke.getColor().evaluate(null, Color.class);
        assertEquals(strokeColor, Color.BLUE);
        final double strokeOpacity = markStroke.getOpacity().evaluate(null, Double.class);
        assertEquals(strokeOpacity, 1D);
    }
    
    /**
     * Can convert a color to the expected KML format.
     */
    @Test
    public void testFormatColor() {
        assertEquals(
                StyleGenerator.formatColor(Color.MAGENTA),
                "bfff00ff");
    }
}
