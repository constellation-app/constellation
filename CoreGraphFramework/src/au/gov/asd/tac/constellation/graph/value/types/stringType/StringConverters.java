/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.stringType;

import au.gov.asd.tac.constellation.graph.value.readables.Comparison;
import au.gov.asd.tac.constellation.graph.value.readables.Sum;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntValue;
import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.Contains;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThan;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.LessThan;
import au.gov.asd.tac.constellation.graph.value.readables.LessThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanValue;

/**
 *
 * @author darren
 */
public class StringConverters {
    
    public static <P1 extends StringReadable, P2 extends StringReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
        r.register(parameterClass1, parameterClass2, Comparison.class, new ComparisonConverter());
        r.register(parameterClass1, parameterClass2, Equals.class, new EqualsConverter());
        r.register(parameterClass1, parameterClass2, NotEquals.class, new NotEqualsConverter());
        r.register(parameterClass1, parameterClass2, GreaterThan.class, new GreaterThanConverter());
        r.register(parameterClass1, parameterClass2, GreaterThanOrEquals.class, new GreaterThanOrEqualsConverter());
        r.register(parameterClass1, parameterClass2, LessThan.class, new LessThanConverter());
        r.register(parameterClass1, parameterClass2, LessThanOrEquals.class, new LessThanOrEqualsConverter());
        r.register(parameterClass1, parameterClass2, Contains.class, new ContainsConverter());
        
        r.register(parameterClass1, parameterClass2, Sum.class, new SumConverter());
    }
    
    private static int compareStrings(String a, String b) {
        if (a == null) {
            return b == null ? 0 : -1;
        } else {
            return b == null ? 1 : a.compareTo(b);
        }
    }
    
    public static class ComparisonConverter implements Biconverter<StringReadable, StringReadable, Comparison> {
        @Override
        public Comparison convert(StringReadable source1, StringReadable source2) {
            return new Comparison() {
                @Override
                public IntValue createValue() {
                    return new IntValue();
                }
                
                @Override
                public void read(IntValue value) {
                    value.writeInt(compareStrings(source1.readString(), source2.readString()));
                }
            };
        }
    }
    
    public static class EqualsConverter implements Biconverter<StringReadable, StringReadable, Equals> {
        @Override
        public Equals convert(StringReadable source1, StringReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(compareStrings(source1.readString(), source2.readString()) == 0);
                }
            };
        }
    }
    
    public static class NotEqualsConverter implements Biconverter<StringReadable, StringReadable, NotEquals> {
        @Override
        public NotEquals convert(StringReadable source1, StringReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(compareStrings(source1.readString(), source2.readString()) != 0);
                }
            };
        }
    }
    
    public static class GreaterThanConverter implements Biconverter<StringReadable, StringReadable, GreaterThan> {
        @Override
        public GreaterThan convert(StringReadable source1, StringReadable source2) {
            return new GreaterThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(compareStrings(source1.readString(), source2.readString()) > 0);
                }
            };
        }
    }
    
    public static class GreaterThanOrEqualsConverter implements Biconverter<StringReadable, StringReadable, GreaterThanOrEquals> {
        @Override
        public GreaterThanOrEquals convert(StringReadable source1, StringReadable source2) {
            return new GreaterThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(compareStrings(source1.readString(), source2.readString()) >= 0);
                }
            };
        }
    }
    
    public static class LessThanConverter implements Biconverter<StringReadable, StringReadable, LessThan> {
        @Override
        public LessThan convert(StringReadable source1, StringReadable source2) {
            return new LessThan() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(compareStrings(source1.readString(), source2.readString()) < 0);
                }
            };
        }
    }
    
    public static class LessThanOrEqualsConverter implements Biconverter<StringReadable, StringReadable, LessThanOrEquals> {
        @Override
        public LessThanOrEquals convert(StringReadable source1, StringReadable source2) {
            return new LessThanOrEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(compareStrings(source1.readString(), source2.readString()) <= 0);
                }
            };
        }
    }
    
    public static class ContainsConverter implements Biconverter<StringReadable, StringReadable, Contains> {
        @Override
        public Contains convert(StringReadable source1, StringReadable source2) {
            return new Contains() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    final var source1Value = source1.readString();
                    if (source1Value == null) {
                        value.writeBoolean(false);
                    } else {
                        final var source2Value = source2.readString();
                        value.writeBoolean(source2Value != null && source1Value.contains(source2Value));
                    }
                }
            };
        }
    }
    
    public static class SumConverter implements Biconverter<StringReadable, StringReadable, Sum<StringValue>> {
        @Override
        public Sum<StringValue> convert(StringReadable source1, StringReadable source2) {
            return new Sum<>() {
                @Override
                public StringValue createValue() {
                    return new StringValue();
                }

                @Override
                public void read(StringValue value) {
                    final var value1 = source1.readString();
                    if (value1 == null) {
                        value.writeString(source2.readString());
                    } else {
                        final var value2 = source2.readString();
                        value.writeString(value2 == null ? value1 : (value1 + value2));
                    }
                }
            };
        }
    }
}
