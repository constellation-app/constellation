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

import com.google.common.util.concurrent.AtomicDouble;
import java.awt.Color;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.opengis.filter.FilterFactory;

/**
 * Style Generator.
 *
 * @author cygnus_x-1
 */
public class StyleGenerator {

    private static final Logger LOGGER = Logger.getLogger(StyleGenerator.class.getName());
    private static final Iterator<Color> COLORS = Colors.iterator();

    public static Style createStyle(final SimpleFeatureCollection features) {
        if (features.size() == 0) {
            LOGGER.warning("No features available to generate style");
            return null;
        }

        final Class<?> geometryType = features.getSchema().getGeometryDescriptor().getType().getBinding();
        if (Polygon.class.isAssignableFrom(geometryType)
                || MultiPolygon.class.isAssignableFrom(geometryType)) {
            return createPolygonStyle();
        } else if (LineString.class.isAssignableFrom(geometryType)) {
            return createLineStyle();
        } else if (Point.class.isAssignableFrom(geometryType)) {
            return createPointStyle();
        } else {
            LOGGER.log(Level.WARNING, "Style cannot be generated from type: {0}", geometryType);
            return null;
        }
    }

    private static Style createPolygonStyle() {
        final StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        final FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        // create a partially opaque outline stroke
        final Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLACK),
                filterFactory.literal(1),
                filterFactory.literal(.5)
        );

        // create a partially opaque fill
        final Fill fill = styleFactory.createFill(
                filterFactory.literal(COLORS.next()),
                filterFactory.literal(.5)
        );

        // setting the geometryPropertyName arg to null signals that we want to draw the default geometry of features
        final PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        // make rule
        final Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);

        final FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        final Style style = styleFactory.createStyle();
        style.getDescription().setTitle("Polygon Style");
        style.featureTypeStyles().add(fts);

        return style;
    }

    private static Style createLineStyle() {
        final StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        final FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        // create a partially opaque outline stroke
        final Stroke stroke = styleFactory.createStroke(filterFactory.literal(Color.WHITE),
                filterFactory.literal(1), filterFactory.literal(.5));

        // create a partially opaque fill
        final Fill fill = styleFactory.createFill( filterFactory.literal(Color.RED), filterFactory.literal(.25));

        // setting the geometryPropertyName arg to null signals that we want to draw the default geometry of features
        final PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        // make rule
        final Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);

        final FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        final Style style = styleFactory.createStyle();
        style.getDescription().setTitle("Line Style");
        style.featureTypeStyles().add(fts);

        return style;
    }

    private static Style createPointStyle() {
        final StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
        final FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();

        final Mark mark = styleFactory.getCircleMark();
        mark.setStroke(styleFactory.createStroke(filterFactory.literal(Color.BLUE), filterFactory.literal(1)));
        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

        final Graphic gr = styleFactory.getDefaultGraphic();
        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);
        gr.setSize(filterFactory.literal(5));

        // setting the geometryPropertyName arg to null signals that we want to draw the default geometry of features
        final PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        // make rule
        final Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);

        final FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        final Style style = styleFactory.createStyle();
        style.getDescription().setTitle("Point Style");
        style.featureTypeStyles().add(fts);

        return style;
    }

    public static String formatColor(final Color color) {
        // convert a color to the expected kml format (aabbggrr)
        return "bf" + String.format("%02x", (0xFF & color.getBlue()))
                + String.format("%02x", (0xFF & color.getGreen()))
                + String.format("%02x", (0xFF & color.getRed()));
    }

    private static class Colors {

        /**
         * provides an iterator to walk through the color wheel choosing the
         * next color based on the golden ratio.
         *
         * @return the next color
         */
        public static Iterator<Color> iterator() {
            return new Iterator<Color>() {

                private final AtomicDouble counter = new AtomicDouble(0D);

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Color next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final double currColorValue = counter.getAndAdd(1D);
                    final double num = (currColorValue - 1.0) * 0.618033988749895;
                    final double fmod = num - Math.floor(num);
                    return Color.getHSBColor((float) num, (float) Math.sqrt(fmod), 0.8F);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Cannot remove from a color iterator.");
                }
            };
        }
    }
}
