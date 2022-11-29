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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.DateAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.FloatAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.IntegerAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ZonedDateTimeAttributeDescription;
import au.gov.asd.tac.constellation.graph.construction.GraphBuilder;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.ColorAttributeDescription;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author twilight_sparkle
 */
public class AttributeFillBuilder extends GraphBuilder {

    private static final SecureRandom R = new SecureRandom();

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID) {
        return fillAttribute(graph, attrID, null, null);
    }

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID, final Object value) {
        return fillAttribute(graph, attrID, value, null);
    }

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID, final Object lower, final Object upper) {
        final GraphElementType elementType = graph.getAttributeElementType(attrID);
        final int[] elementsToFill = elementType == GraphElementType.VERTEX ? new int[graph.getVertexCount()] : new int[graph.getTransactionCount()];
        for (int i = 0; i < elementsToFill.length; i++) {
            elementsToFill[i] = elementType == GraphElementType.VERTEX ? graph.getVertex(i) : graph.getTransaction(i);
        }
        return fillAttribute(graph, attrID, elementsToFill, lower, upper);
    }

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID, final int[] elementsToFill) {
        return fillAttribute(graph, attrID, elementsToFill, null, null);
    }

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID, final int[] elementsToFill, final Object value) {
        return fillAttribute(graph, attrID, elementsToFill, value, null);
    }

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID, final int[] elementsToFill, final Object lower, final Object upper) {

        final AttributeValueGenerator avg;

        Class<? extends AttributeDescription> ad = graph.getAttributeDataType(attrID);

        if (ad.equals(IntegerAttributeDescription.class)) {
            avg = new IntegerAttributeValueGenerator();
        } else if (ad.equals(FloatAttributeDescription.class)) {
            avg = new FloatAttributeValueGenerator();
        } else if (ad.equals(ZonedDateTimeAttributeDescription.class) || ad.getClass().equals(DateAttributeDescription.class)) {
            avg = new DateAttributeValueGenerator();
        } else if (ad.equals(ColorAttributeDescription.class)) {
            avg = new ColorValueAttributeValueGenerator();
        } else {
            return null;
        }

        if (lower == null && upper == null) {
            return fillAttribute(graph, attrID, elementsToFill, avg.createValues(elementsToFill.length), true);
        } else if (upper == null) {
            List<Object> singleOption = new ArrayList<>();
            singleOption.add(lower);
            return fillAttribute(graph, attrID, elementsToFill, singleOption, true);
        } else {
            return fillAttribute(graph, attrID, elementsToFill, avg.createValues(lower, upper, elementsToFill.length), true);
        }

    }

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID, final List<Object> options, final boolean sequential) {
        final GraphElementType elementType = graph.getAttributeElementType(attrID);
        final int[] elementsToFill = elementType == GraphElementType.VERTEX ? new int[graph.getVertexCount()] : new int[graph.getTransactionCount()];
        for (int i = 0; i < elementsToFill.length; i++) {
            elementsToFill[i] = elementType == GraphElementType.VERTEX ? graph.getVertex(i) : graph.getTransaction(i);
        }
        return fillAttribute(graph, attrID, elementsToFill, options, sequential);
    }

    public static AttributeFillBuilder fillAttribute(final GraphWriteMethods graph, final int attrID, final int[] elementsToFill, final List<Object> options, final boolean sequential) {

        for (int i = 0; i < elementsToFill.length; i++) {
            graph.setObjectValue(attrID, elementsToFill[i], sequential ? options.get(i % options.size()) : options.get(R.nextInt(options.size())));
        }
        return new AttributeFillBuilder(graph);
    }

    private interface AttributeValueGenerator<T> {

        public List<Object> createValues(final T low, final T high, final int numberOfValues);

        public List<Object> createValues(final int numberOfValues);

    }

    private static class IntegerAttributeValueGenerator implements AttributeValueGenerator<Integer> {

        private static final int DEFAULT_START = 0;
        private static final int DEFAULT_END = 10;

        @Override
        public List<Object> createValues(final Integer low, final Integer high, final int numberOfValues) {
            final int difference = high - low;
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < numberOfValues; i++) {
                values.add(low + ((difference * i) / numberOfValues));
            }
            return values;
        }

        @Override
        public List<Object> createValues(final int numberOfValues) {
            return createValues(DEFAULT_START, DEFAULT_END, numberOfValues);
        }

    }

    private static class FloatAttributeValueGenerator implements AttributeValueGenerator<Float> {

        private static final float DEFAULT_START = 0F;
        private static final float DEFAULT_END = 10F;

        @Override
        public List<Object> createValues(final Float low, final Float high, final int numberOfValues) {
            final float difference = high - low;
            List<Object> values = new ArrayList<>();
            for (int i = 0; i < numberOfValues; i++) {
                values.add(low + ((difference * i) / numberOfValues));
            }
            return values;
        }

        @Override
        public List<Object> createValues(final int numberOfValues) {
            return createValues(DEFAULT_START, DEFAULT_END, numberOfValues);
        }

    }

    private static class DateAttributeValueGenerator implements AttributeValueGenerator<Date> {

        private static final long ONE_YEAR = 1000L * 3600L * 24L * 365L;
        private static final Date DEFAULT_START = new Date(System.currentTimeMillis() - ONE_YEAR);
        private static final Date DEFAULT_END = new Date(System.currentTimeMillis());

        @Override
        public List<Object> createValues(final Date low, final Date high, final int numberOfValues) {
            List<Object> values = new ArrayList<>();
            final long lowTime = low.getTime();
            final long difference = high.getTime() - low.getTime();
            for (int i = 0; i < numberOfValues; i++) {
                values.add(new Date(lowTime + ((difference * i) / numberOfValues)));
            }
            return values;
        }

        @Override
        public List<Object> createValues(final int numberOfValues) {
            return createValues(DEFAULT_START, DEFAULT_END, numberOfValues);
        }

    }

    private static class ColorValueAttributeValueGenerator implements AttributeValueGenerator<ConstellationColor> {

        private static final ConstellationColor DEFAULT_START = ConstellationColor.getColorValue(0F, 0F, 0F, 1F);
        private static final ConstellationColor DEFAULT_END = ConstellationColor.getColorValue(1F, 1F, 1F, 1F);

        @Override
        public List<Object> createValues(final ConstellationColor low, final ConstellationColor high, final int numberOfValues) {

            List<Object> values = new ArrayList<>();
            final float redLow = low.getRed();
            final float greenLow = low.getGreen();
            final float blueLow = low.getBlue();

            final float redDiff = high.getRed() - redLow;
            final float greenDiff = high.getGreen() - greenLow;
            final float blueDiff = high.getBlue() - blueLow;

            for (int i = 0; i < numberOfValues; i++) {
                values.add(ConstellationColor.getColorValue(redLow + ((redDiff * i) / numberOfValues), greenLow + ((greenDiff * i) / numberOfValues), blueLow + ((blueDiff * i) / numberOfValues), 1F));
            }
            return values;
        }

        @Override
        public List<Object> createValues(final int numberOfValues) {
            return createValues(DEFAULT_START, DEFAULT_END, numberOfValues);
        }

    }

    private AttributeFillBuilder(final GraphWriteMethods graph) {
        super(graph);
    }

}
