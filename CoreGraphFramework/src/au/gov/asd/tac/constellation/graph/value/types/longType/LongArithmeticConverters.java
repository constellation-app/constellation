/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.longType;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.Comparison;
import au.gov.asd.tac.constellation.graph.value.readables.Difference;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThan;
import au.gov.asd.tac.constellation.graph.value.readables.LessThan;
import au.gov.asd.tac.constellation.graph.value.readables.Product;
import au.gov.asd.tac.constellation.graph.value.readables.Sum;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanValue;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntValue;
import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.Converter;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.LessThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Modulus;
import au.gov.asd.tac.constellation.graph.value.readables.Negative;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Positive;
import au.gov.asd.tac.constellation.graph.value.readables.Quotient;

/**
 *
 * @author darren
 */
public class LongArithmeticConverters {
     
    public static <P1 extends LongReadable, P2 extends LongReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
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
