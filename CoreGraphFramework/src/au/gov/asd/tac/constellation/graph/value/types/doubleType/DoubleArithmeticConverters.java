/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.doubleType;

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
public class DoubleArithmeticConverters {
    
    public static <P1 extends DoubleReadable, P2 extends DoubleReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
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
    
    public static <P1 extends DoubleReadable> void register(ConverterRegistry r, Class<P1> parameterClass) {
        r.register(parameterClass, Negative.class, new NegativeConverter());
        r.register(parameterClass, Positive.class, new PositiveConverter());
    }
    
    public static class NegativeConverter implements Converter<DoubleReadable, Negative<DoubleValue>> {
        @Override
        public Negative<DoubleValue> convert(DoubleReadable source) {
            return new Negative<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(DoubleValue value) {
                    value.writeDouble(-source.readDouble());
                }
            };
        }
    }
    
    public static class PositiveConverter implements Converter<DoubleReadable, Positive<DoubleValue>> {
        @Override
        public Positive<DoubleValue> convert(DoubleReadable source) {
            return new Positive<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(DoubleValue value) {
                    value.writeDouble(source.readDouble());
                }
            };
        }
    }
    
    public static class ComparisonConverter implements Biconverter<DoubleReadable, DoubleReadable, Comparison> {
        @Override
        public Comparison convert(DoubleReadable source1, DoubleReadable source2) {
            return new Comparison() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }

                @Override
                public void read(IntValue value) {
                    value.writeInt(Double.compare(source1.readDouble(), source2.readDouble()));
                }
            };
        }
    }
    
    public static class DifferenceConverter implements Biconverter<DoubleReadable, DoubleReadable, Difference<DoubleValue>> {
        @Override
        public Difference<DoubleValue> convert(DoubleReadable source1, DoubleReadable source2) {
            return new Difference<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(DoubleValue value) {
                    value.writeDouble(source1.readDouble() - source2.readDouble());
                }
            };
        }
    }
    
    public static class EqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, Equals> {
        @Override
        public Equals convert(DoubleReadable source1, DoubleReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readDouble() == source2.readDouble());
                }
            };
        }
    }
    
    public static class NotEqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, NotEquals> {
        @Override
        public NotEquals convert(DoubleReadable source1, DoubleReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readDouble() != source2.readDouble());
                }
            };
        }
    }
    
    public static class GreaterThanConverter implements Biconverter<DoubleReadable, DoubleReadable, GreaterThan> {
        @Override
        public GreaterThan convert(DoubleReadable source1, DoubleReadable source2) {
            return new GreaterThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readDouble() > source2.readDouble());
                }
            };
        }
    }
    
    public static class GreaterThanOrEqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, GreaterThanOrEquals> {
        @Override
        public GreaterThanOrEquals convert(DoubleReadable source1, DoubleReadable source2) {
            return new GreaterThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readDouble() >= source2.readDouble());
                }
            };
        }
    }
    
    public static class LessThanConverter implements Biconverter<DoubleReadable, DoubleReadable, LessThan> {
        @Override
        public LessThan convert(DoubleReadable source1, DoubleReadable source2) {
            return new LessThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readDouble() < source2.readDouble());
                }
            };
        }
    }
    
    public static class LessThanOrEqualsConverter implements Biconverter<DoubleReadable, DoubleReadable, LessThanOrEquals> {
        @Override
        public LessThanOrEquals convert(DoubleReadable source1, DoubleReadable source2) {
            return new LessThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readDouble() <= source2.readDouble());
                }
            };
        }
    }
    
    public static class ProductConverter implements Biconverter<DoubleReadable, DoubleReadable, Product<DoubleValue>> {
        @Override
        public Product<DoubleValue> convert(DoubleReadable source1, DoubleReadable source2) {
            return new Product<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(DoubleValue value) {
                    value.writeDouble(source1.readDouble() * source2.readDouble());
                }
            };
        }
    }
    
    public static class QuotientConverter implements Biconverter<DoubleReadable, DoubleReadable, Quotient<DoubleValue>> {
        @Override
        public Quotient<DoubleValue> convert(DoubleReadable source1, DoubleReadable source2) {
            return new Quotient<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(DoubleValue value) {
                    value.writeDouble(source1.readDouble() / source2.readDouble());
                }
            };
        }
    }
    
    public static class ModulusConverter implements Biconverter<DoubleReadable, DoubleReadable, Modulus<DoubleValue>> {
        @Override
        public Modulus<DoubleValue> convert(DoubleReadable source1, DoubleReadable source2) {
            return new Modulus<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(DoubleValue value) {
                    value.writeDouble(source1.readDouble() % source2.readDouble());
                }
            };
        }
    }
    
    public static class SumConverter implements Biconverter<DoubleReadable, DoubleReadable, Sum<DoubleValue>> {
        @Override
        public Sum<DoubleValue> convert(DoubleReadable source1, DoubleReadable source2) {
            return new Sum<>() {
                @Override
                public DoubleValue createValue() {
                    return new DoubleValue();
                }

                @Override
                public void read(DoubleValue value) {
                    value.writeDouble(source1.readDouble() + source2.readDouble());
                }
            };
        }
    }
}
