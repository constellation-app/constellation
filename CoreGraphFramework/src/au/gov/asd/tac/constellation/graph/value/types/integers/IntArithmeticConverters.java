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
package au.gov.asd.tac.constellation.graph.value.types.integers;

import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.Converter;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.And;
import au.gov.asd.tac.constellation.graph.value.readables.Comparison;
import au.gov.asd.tac.constellation.graph.value.readables.Difference;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.ExclusiveOr;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThan;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.LessThan;
import au.gov.asd.tac.constellation.graph.value.readables.LessThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Modulus;
import au.gov.asd.tac.constellation.graph.value.readables.Negative;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Or;
import au.gov.asd.tac.constellation.graph.value.readables.Positive;
import au.gov.asd.tac.constellation.graph.value.readables.Product;
import au.gov.asd.tac.constellation.graph.value.readables.Quotient;
import au.gov.asd.tac.constellation.graph.value.readables.Sum;
import au.gov.asd.tac.constellation.graph.value.types.booleans.BooleanValue;
import au.gov.asd.tac.constellation.graph.value.types.bytes.ByteValue;
import au.gov.asd.tac.constellation.graph.value.types.shorts.ShortValue;

/**
 *
 * @author sirius
 */
public class IntArithmeticConverters {

    private IntArithmeticConverters() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static void register(ConverterRegistry r) {
        register(r, IntValue.class, IntValue.class);

        register(r, IntValue.class, ShortValue.class);
        register(r, ShortValue.class, IntValue.class);

        register(r, IntValue.class, ByteValue.class);
        register(r, ByteValue.class, IntValue.class);
    }

    public static <P1 extends IntReadable, P2 extends IntReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
        r.register(parameterClass1, parameterClass2, Product.class, new ProductConverter());
        r.register(parameterClass1, parameterClass2, Quotient.class, new QuotientConverter());
        r.register(parameterClass1, parameterClass2, Modulus.class, new ModulusConverter());
        r.register(parameterClass1, parameterClass2, Sum.class, new SumConverter());
        r.register(parameterClass1, parameterClass2, Difference.class, new DifferenceConverter());
        r.register(parameterClass1, parameterClass2, And.class, new AndConverter());
        r.register(parameterClass1, parameterClass2, Or.class, new OrConverter());
        r.register(parameterClass1, parameterClass2, ExclusiveOr.class, new ExclusiveOrConverter());
        r.register(parameterClass1, parameterClass2, Comparison.class, new ComparisonConverter());
        r.register(parameterClass1, parameterClass2, Equals.class, new EqualsConverter());
        r.register(parameterClass1, parameterClass2, NotEquals.class, new NotEqualsConverter());
        r.register(parameterClass1, parameterClass2, GreaterThan.class, new GreaterThanConverter());
        r.register(parameterClass1, parameterClass2, GreaterThanOrEquals.class, new GreaterThanOrEqualsConverter());
        r.register(parameterClass1, parameterClass2, LessThan.class, new LessThanConverter());
        r.register(parameterClass1, parameterClass2, LessThanOrEquals.class, new LessThanOrEqualsConverter());
    }

    public static <P extends IntReadable> void register(ConverterRegistry r, Class<P> parameterClass) {
        r.register(parameterClass, Negative.class, new NegativeConverter());
        r.register(parameterClass, Positive.class, new PositiveConverter());
    }

    public static class NegativeConverter implements Converter<IntReadable, Negative<IntValue>> {

        @Override
        public Negative<IntValue> convert(IntReadable source) {
            return new Negative<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(-source.readInt());
                }
            };
        }
    }

    public static class PositiveConverter implements Converter<IntReadable, Positive<IntValue>> {

        @Override
        public Positive<IntValue> convert(IntReadable source) {
            return new Positive<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source.readInt());
                }
            };
        }
    }

    public static class ComparisonConverter implements Biconverter<IntReadable, IntReadable, Comparison> {

        @Override
        public Comparison convert(IntReadable source1, IntReadable source2) {
            return new Comparison() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(Integer.compare(source1.readInt(), source2.readInt()));
                }
            };
        }
    }

    public static class DifferenceConverter implements Biconverter<IntReadable, IntReadable, Difference<IntValue>> {

        @Override
        public Difference<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new Difference<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() - source2.readInt());
                }
            };
        }
    }

    public static class AndConverter implements Biconverter<IntReadable, IntReadable, And<IntValue>> {

        @Override
        public And<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new And<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() & source2.readInt());
                }
            };
        }
    }

    public static class OrConverter implements Biconverter<IntReadable, IntReadable, Or<IntValue>> {

        @Override
        public Or<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new Or<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() | source2.readInt());
                }
            };
        }
    }

    public static class ExclusiveOrConverter implements Biconverter<IntReadable, IntReadable, ExclusiveOr<IntValue>> {

        @Override
        public ExclusiveOr<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new ExclusiveOr<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() ^ source2.readInt());
                }
            };
        }
    }

    public static class EqualsConverter implements Biconverter<IntReadable, IntReadable, Equals> {

        @Override
        public Equals convert(IntReadable source1, IntReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readInt() == source2.readInt());
                }
            };
        }
    }

    public static class NotEqualsConverter implements Biconverter<IntReadable, IntReadable, NotEquals> {

        @Override
        public NotEquals convert(IntReadable source1, IntReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readInt() != source2.readInt());
                }
            };
        }
    }

    public static class GreaterThanConverter implements Biconverter<IntReadable, IntReadable, GreaterThan> {

        @Override
        public GreaterThan convert(IntReadable source1, IntReadable source2) {
            return new GreaterThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readInt() > source2.readInt());
                }
            };
        }
    }

    public static class GreaterThanOrEqualsConverter implements Biconverter<IntReadable, IntReadable, GreaterThanOrEquals> {

        @Override
        public GreaterThanOrEquals convert(IntReadable source1, IntReadable source2) {
            return new GreaterThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readInt() >= source2.readInt());
                }
            };
        }
    }

    public static class LessThanConverter implements Biconverter<IntReadable, IntReadable, LessThan> {

        @Override
        public LessThan convert(IntReadable source1, IntReadable source2) {
            return new LessThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readInt() < source2.readInt());
                }
            };
        }
    }

    public static class LessThanOrEqualsConverter implements Biconverter<IntReadable, IntReadable, LessThanOrEquals> {

        @Override
        public LessThanOrEquals convert(IntReadable source1, IntReadable source2) {
            return new LessThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readInt() <= source2.readInt());
                }
            };
        }
    }

    public static class ProductConverter implements Biconverter<IntReadable, IntReadable, Product<IntValue>> {

        @Override
        public Product<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new Product<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() * source2.readInt());
                }
            };
        }
    }

    public static class QuotientConverter implements Biconverter<IntReadable, IntReadable, Quotient<IntValue>> {

        @Override
        public Quotient<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new Quotient<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() / source2.readInt());
                }
            };
        }
    }

    public static class ModulusConverter implements Biconverter<IntReadable, IntReadable, Modulus<IntValue>> {

        @Override
        public Modulus<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new Modulus<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() % source2.readInt());
                }
            };
        }
    }

    public static class SumConverter implements Biconverter<IntReadable, IntReadable, Sum<IntValue>> {

        @Override
        public Sum<IntValue> convert(IntReadable source1, IntReadable source2) {
            return new Sum<>() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(source1.readInt() + source2.readInt());
                }
            };
        }
    }
}
