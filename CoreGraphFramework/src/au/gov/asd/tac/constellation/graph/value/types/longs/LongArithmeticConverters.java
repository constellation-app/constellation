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
package au.gov.asd.tac.constellation.graph.value.types.longs;

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
import au.gov.asd.tac.constellation.graph.value.types.integers.IntValue;

/**
 *
 * @author sirius
 */
public class LongArithmeticConverters {

    private LongArithmeticConverters() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static <P1 extends LongReadable, P2 extends LongReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
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

    public static <P extends LongReadable> void register(ConverterRegistry r, Class<P> parameterClass) {
        r.register(parameterClass, Negative.class, new NegativeConverter());
        r.register(parameterClass, Positive.class, new PositiveConverter());
    }

    public static class NegativeConverter implements Converter<LongReadable, Negative<LongValue>> {

        @Override
        public Negative<LongValue> convert(LongReadable source) {
            return new Negative<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(-source.readLong());
                }
            };
        }
    }

    public static class PositiveConverter implements Converter<LongReadable, Positive<LongValue>> {

        @Override
        public Positive<LongValue> convert(LongReadable source) {
            return new Positive<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source.readLong());
                }
            };
        }
    }

    public static class ComparisonConverter implements Biconverter<LongReadable, LongReadable, Comparison> {

        @Override
        public Comparison convert(LongReadable source1, LongReadable source2) {
            return new Comparison() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(Long.compare(source1.readLong(), source2.readLong()));
                }
            };
        }
    }

    public static class DifferenceConverter implements Biconverter<LongReadable, LongReadable, Difference<LongValue>> {

        @Override
        public Difference<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new Difference<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() - source2.readLong());
                }
            };
        }
    }

    public static class AndConverter implements Biconverter<LongReadable, LongReadable, And<LongValue>> {

        @Override
        public And<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new And<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() & source2.readLong());
                }
            };
        }
    }

    public static class OrConverter implements Biconverter<LongReadable, LongReadable, Or<LongValue>> {

        @Override
        public Or<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new Or<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() | source2.readLong());
                }
            };
        }
    }

    public static class ExclusiveOrConverter implements Biconverter<LongReadable, LongReadable, ExclusiveOr<LongValue>> {

        @Override
        public ExclusiveOr<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new ExclusiveOr<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() ^ source2.readLong());
                }
            };
        }
    }

    public static class EqualsConverter implements Biconverter<LongReadable, LongReadable, Equals> {

        @Override
        public Equals convert(LongReadable source1, LongReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readLong() == source2.readLong());
                }
            };
        }
    }

    public static class NotEqualsConverter implements Biconverter<LongReadable, LongReadable, NotEquals> {

        @Override
        public NotEquals convert(LongReadable source1, LongReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readLong() == source2.readLong());
                }
            };
        }
    }

    public static class GreaterThanConverter implements Biconverter<LongReadable, LongReadable, GreaterThan> {

        @Override
        public GreaterThan convert(LongReadable source1, LongReadable source2) {
            return new GreaterThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readLong() > source2.readLong());
                }
            };
        }
    }

    public static class GreaterThanOrEqualsConverter implements Biconverter<LongReadable, LongReadable, GreaterThanOrEquals> {

        @Override
        public GreaterThanOrEquals convert(LongReadable source1, LongReadable source2) {
            return new GreaterThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readLong() >= source2.readLong());
                }
            };
        }
    }

    public static class LessThanConverter implements Biconverter<LongReadable, LongReadable, LessThan> {

        @Override
        public LessThan convert(LongReadable source1, LongReadable source2) {
            return new LessThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readLong() < source2.readLong());
                }
            };
        }
    }

    public static class LessThanOrEqualsConverter implements Biconverter<LongReadable, LongReadable, LessThanOrEquals> {

        @Override
        public LessThanOrEquals convert(LongReadable source1, LongReadable source2) {
            return new LessThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readLong() <= source2.readLong());
                }
            };
        }
    }

    public static class ProductConverter implements Biconverter<LongReadable, LongReadable, Product<LongValue>> {

        @Override
        public Product<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new Product<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() * source2.readLong());
                }
            };
        }
    }

    public static class QuotientConverter implements Biconverter<LongReadable, LongReadable, Quotient<LongValue>> {

        @Override
        public Quotient<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new Quotient<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() / source2.readLong());
                }
            };
        }
    }

    public static class ModulusConverter implements Biconverter<LongReadable, LongReadable, Modulus<LongValue>> {

        @Override
        public Modulus<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new Modulus<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() % source2.readLong());
                }
            };
        }
    }

    public static class SumConverter implements Biconverter<LongReadable, LongReadable, Sum<LongValue>> {

        @Override
        public Sum<LongValue> convert(LongReadable source1, LongReadable source2) {
            return new Sum<>() {
                @Override
                public LongValue createValue() {
                    return new LongValue();
                }

                @Override
                public void read(LongValue value) {
                    value.writeLong(source1.readLong() + source2.readLong());
                }
            };
        }
    }
}
