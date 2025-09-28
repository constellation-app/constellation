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
package au.gov.asd.tac.constellation.graph.value.types.floats;

import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.Converter;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.Comparison;
import au.gov.asd.tac.constellation.graph.value.readables.Difference;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThan;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.LessThan;
import au.gov.asd.tac.constellation.graph.value.readables.LessThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Modulus;
import au.gov.asd.tac.constellation.graph.value.readables.Negative;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Positive;
import au.gov.asd.tac.constellation.graph.value.readables.Product;
import au.gov.asd.tac.constellation.graph.value.readables.Quotient;
import au.gov.asd.tac.constellation.graph.value.readables.Sum;
import au.gov.asd.tac.constellation.graph.value.types.booleans.BooleanValue;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntValue;

/**
 *
 * @author sirius
 */
public class FloatArithmeticConverters {

    private FloatArithmeticConverters() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static <P1 extends FloatReadable, P2 extends FloatReadable> void register(final ConverterRegistry r, 
            final Class<P1> parameterClass1, final Class<P2> parameterClass2) {
        r.register(parameterClass1, parameterClass2, Product.class, new ProductConverter());
        r.register(parameterClass1, parameterClass2, Quotient.class, new QuotientConverter());
        r.register(parameterClass1, parameterClass2, Modulus.class, new ModulusConverter());
        r.register(parameterClass1, parameterClass2, Sum.class, new SumConverter());
        r.register(parameterClass1, parameterClass2, Difference.class, new DifferenceConverter());
        r.register(parameterClass1, parameterClass2, Comparison.class, new ComparisonConverter());
        r.register(parameterClass1, parameterClass2, Equals.class, new EqualsConverter());
        r.register(parameterClass1, parameterClass2, NotEquals.class, new NotEqualsConverter());
        r.register(parameterClass1, parameterClass2, GreaterThan.class, new GreaterThanConverter());
        r.register(parameterClass1, parameterClass2, GreaterThanOrEquals.class, new GreaterThanOrEqualsConverter());
        r.register(parameterClass1, parameterClass2, LessThan.class, new LessThanConverter());
        r.register(parameterClass1, parameterClass2, LessThanOrEquals.class, new LessThanOrEqualsConverter());
    }

    public static <P extends FloatReadable> void register(final ConverterRegistry r, final Class<P> parameterClass) {
        r.register(parameterClass, Negative.class, new NegativeConverter());
        r.register(parameterClass, Positive.class, new PositiveConverter());
    }

    public static class NegativeConverter implements Converter<FloatReadable, Negative<FloatValue>> {

        @Override
        public Negative<FloatValue> convert(final FloatReadable source) {
            return new Negative<>() {
                @Override
                public FloatValue createValue() {
                    return new FloatValue();
                }

                @Override
                public void read(final FloatValue value) {
                    value.writeFloat(-source.readFloat());
                }
            };
        }
    }

    public static class PositiveConverter implements Converter<FloatReadable, Positive<FloatValue>> {

        @Override
        public Positive<FloatValue> convert(final FloatReadable source) {
            return new Positive<>() {
                @Override
                public FloatValue createValue() {
                    return new FloatValue();
                }

                @Override
                public void read(final FloatValue value) {
                    value.writeFloat(source.readFloat());
                }
            };
        }
    }

    public static class ComparisonConverter implements Biconverter<FloatReadable, FloatReadable, Comparison> {

        @Override
        public Comparison convert(final FloatReadable source1, final FloatReadable source2) {
            return new Comparison() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(final IntValue value) {
                    value.writeInt(Float.compare(source1.readFloat(), source2.readFloat()));
                }
            };
        }
    }

    public static class EqualsConverter implements Biconverter<FloatReadable, FloatReadable, Equals> {

        @Override
        public Equals convert(final FloatReadable source1, final FloatReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readFloat() == source2.readFloat());
                }
            };
        }
    }

    public static class NotEqualsConverter implements Biconverter<FloatReadable, FloatReadable, NotEquals> {

        @Override
        public NotEquals convert(final FloatReadable source1, final FloatReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readFloat() != source2.readFloat());
                }
            };
        }
    }

    public static class GreaterThanConverter implements Biconverter<FloatReadable, FloatReadable, GreaterThan> {

        @Override
        public GreaterThan convert(final FloatReadable source1, final FloatReadable source2) {
            return new GreaterThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readFloat() > source2.readFloat());
                }
            };
        }
    }

    public static class GreaterThanOrEqualsConverter implements Biconverter<FloatReadable, FloatReadable, GreaterThanOrEquals> {

        @Override
        public GreaterThanOrEquals convert(final FloatReadable source1, final FloatReadable source2) {
            return new GreaterThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readFloat() >= source2.readFloat());
                }
            };
        }
    }

    public static class LessThanConverter implements Biconverter<FloatReadable, FloatReadable, LessThan> {

        @Override
        public LessThan convert(final FloatReadable source1, final FloatReadable source2) {
            return new LessThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readFloat() < source2.readFloat());
                }
            };
        }
    }

    public static class LessThanOrEqualsConverter implements Biconverter<FloatReadable, FloatReadable, LessThanOrEquals> {

        @Override
        public LessThanOrEquals convert(final FloatReadable source1, final FloatReadable source2) {
            return new LessThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readFloat() <= source2.readFloat());
                }
            };
        }
    }

    public static class ProductConverter implements Biconverter<FloatReadable, FloatReadable, Product<FloatValue>> {

        @Override
        public Product<FloatValue> convert(final FloatReadable source1, final FloatReadable source2) {
            return new Product<>() {
                @Override
                public FloatValue createValue() {
                    return new FloatValue();
                }

                @Override
                public void read(final FloatValue value) {
                    value.writeFloat(source1.readFloat() * source2.readFloat());
                }
            };
        }
    }

    public static class QuotientConverter implements Biconverter<FloatReadable, FloatReadable, Quotient<FloatValue>> {

        @Override
        public Quotient<FloatValue> convert(final FloatReadable source1, final FloatReadable source2) {
            return new Quotient<>() {
                @Override
                public FloatValue createValue() {
                    return new FloatValue();
                }

                @Override
                public void read(final FloatValue value) {
                    value.writeFloat(source1.readFloat() / source2.readFloat());
                }
            };
        }
    }

    public static class ModulusConverter implements Biconverter<FloatReadable, FloatReadable, Modulus<FloatValue>> {

        @Override
        public Modulus<FloatValue> convert(final FloatReadable source1, final FloatReadable source2) {
            return new Modulus<>() {
                @Override
                public FloatValue createValue() {
                    return new FloatValue();
                }

                @Override
                public void read(final FloatValue value) {
                    value.writeFloat(source1.readFloat() % source2.readFloat());
                }
            };
        }
    }

    public static class DifferenceConverter implements Biconverter<FloatReadable, FloatReadable, Difference<FloatValue>> {

        @Override
        public Difference<FloatValue> convert(final FloatReadable source1, final FloatReadable source2) {
            return new Difference<>() {
                @Override
                public FloatValue createValue() {
                    return new FloatValue();
                }

                @Override
                public void read(final FloatValue value) {
                    value.writeFloat(source1.readFloat() - source2.readFloat());
                }
            };
        }
    }

    public static class SumConverter implements Biconverter<FloatReadable, FloatReadable, Sum<FloatValue>> {

        @Override
        public Sum<FloatValue> convert(final FloatReadable source1, final FloatReadable source2) {
            return new Sum<>() {
                @Override
                public FloatValue createValue() {
                    return new FloatValue();
                }

                @Override
                public void read(final FloatValue value) {
                    value.writeFloat(source1.readFloat() + source2.readFloat());
                }
            };
        }
    }
}
