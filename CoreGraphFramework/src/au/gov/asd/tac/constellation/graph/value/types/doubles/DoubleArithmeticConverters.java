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
package au.gov.asd.tac.constellation.graph.value.types.doubles;

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
public class DoubleArithmeticConverters {

    private DoubleArithmeticConverters() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static <P1 extends DoubleReadable, P2 extends DoubleReadable> void register(final ConverterRegistry r, 
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

    public static <P1 extends DoubleReadable> void register(final ConverterRegistry r, final Class<P1> parameterClass) {
        r.register(parameterClass, Negative.class, new NegativeConverter());
        r.register(parameterClass, Positive.class, new PositiveConverter());
    }

    public static class NegativeConverter implements Converter<DoubleReadable, Negative<DoubleValue>> {

        @Override
        public Negative<DoubleValue> convert(final DoubleReadable source) {
            return new Negative<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(final DoubleValue value) {
                    value.writeDouble(-source.readDouble());
                }
            };
        }
    }

    public static class PositiveConverter implements Converter<DoubleReadable, Positive<DoubleValue>> {

        @Override
        public Positive<DoubleValue> convert(final DoubleReadable source) {
            return new Positive<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(final DoubleValue value) {
                    value.writeDouble(source.readDouble());
                }
            };
        }
    }

    public static class ComparisonConverter implements Biconverter<DoubleReadable, DoubleReadable, Comparison> {

        @Override
        public Comparison convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new Comparison() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(final IntValue value) {
                    value.writeInt(Double.compare(source1.readDouble(), source2.readDouble()));
                }
            };
        }
    }

    public static class DifferenceConverter implements Biconverter<DoubleReadable, DoubleReadable, Difference<DoubleValue>> {

        @Override
        public Difference<DoubleValue> convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new Difference<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(final DoubleValue value) {
                    value.writeDouble(source1.readDouble() - source2.readDouble());
                }
            };
        }
    }

    public static class EqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, Equals> {

        @Override
        public Equals convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readDouble() == source2.readDouble());
                }
            };
        }
    }

    public static class NotEqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, NotEquals> {

        @Override
        public NotEquals convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readDouble() != source2.readDouble());
                }
            };
        }
    }

    public static class GreaterThanConverter implements Biconverter<DoubleReadable, DoubleReadable, GreaterThan> {

        @Override
        public GreaterThan convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new GreaterThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readDouble() > source2.readDouble());
                }
            };
        }
    }

    public static class GreaterThanOrEqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, GreaterThanOrEquals> {

        @Override
        public GreaterThanOrEquals convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new GreaterThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readDouble() >= source2.readDouble());
                }
            };
        }
    }

    public static class LessThanConverter implements Biconverter<DoubleReadable, DoubleReadable, LessThan> {

        @Override
        public LessThan convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new LessThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readDouble() < source2.readDouble());
                }
            };
        }
    }

    public static class LessThanOrEqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, LessThanOrEquals> {

        @Override
        public LessThanOrEquals convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new LessThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readDouble() <= source2.readDouble());
                }
            };
        }
    }

    public static class ProductConverter implements Biconverter<DoubleReadable, DoubleReadable, Product<DoubleValue>> {

        @Override
        public Product<DoubleValue> convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new Product<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(final DoubleValue value) {
                    value.writeDouble(source1.readDouble() * source2.readDouble());
                }
            };
        }
    }

    public static class QuotientConverter implements Biconverter<DoubleReadable, DoubleReadable, Quotient<DoubleValue>> {

        @Override
        public Quotient<DoubleValue> convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new Quotient<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(final DoubleValue value) {
                    value.writeDouble(source1.readDouble() / source2.readDouble());
                }
            };
        }
    }

    public static class ModulusConverter implements Biconverter<DoubleReadable, DoubleReadable, Modulus<DoubleValue>> {

        @Override
        public Modulus<DoubleValue> convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new Modulus<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(final DoubleValue value) {
                    value.writeDouble(source1.readDouble() % source2.readDouble());
                }
            };
        }
    }

    public static class SumConverter implements Biconverter<DoubleReadable, DoubleReadable, Sum<DoubleValue>> {

        @Override
        public Sum<DoubleValue> convert(final DoubleReadable source1, final DoubleReadable source2) {
            return new Sum<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(final DoubleValue value) {
                    value.writeDouble(source1.readDouble() + source2.readDouble());
                }
            };
        }
    }
}
