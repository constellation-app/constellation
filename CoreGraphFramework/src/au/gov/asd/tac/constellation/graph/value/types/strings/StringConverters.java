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
package au.gov.asd.tac.constellation.graph.value.types.strings;

import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.Comparison;
import au.gov.asd.tac.constellation.graph.value.readables.Contains;
import au.gov.asd.tac.constellation.graph.value.readables.EndsWith;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThan;
import au.gov.asd.tac.constellation.graph.value.readables.GreaterThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.LessThan;
import au.gov.asd.tac.constellation.graph.value.readables.LessThanOrEquals;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;
import au.gov.asd.tac.constellation.graph.value.readables.StartsWith;
import au.gov.asd.tac.constellation.graph.value.readables.Sum;
import au.gov.asd.tac.constellation.graph.value.types.booleans.BooleanValue;
import au.gov.asd.tac.constellation.graph.value.types.integers.IntValue;

/**
 *
 * @author sirius
 */
public class StringConverters {

    private StringConverters() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static <P1 extends StringReadable, P2 extends StringReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
        r.register(parameterClass1, parameterClass2, Comparison.class, new ComparisonConverter());
        r.register(parameterClass1, parameterClass2, Equals.class, new EqualsConverter());
        r.register(parameterClass1, parameterClass2, NotEquals.class, new NotEqualsConverter());
        r.register(parameterClass1, parameterClass2, GreaterThan.class, new GreaterThanConverter());
        r.register(parameterClass1, parameterClass2, GreaterThanOrEquals.class, new GreaterThanOrEqualsConverter());
        r.register(parameterClass1, parameterClass2, LessThan.class, new LessThanConverter());
        r.register(parameterClass1, parameterClass2, LessThanOrEquals.class, new LessThanOrEqualsConverter());
        r.register(parameterClass1, parameterClass2, Contains.class, new ContainsConverter());
        r.register(parameterClass1, parameterClass2, StartsWith.class, new StartsWithConverter());
        r.register(parameterClass1, parameterClass2, EndsWith.class, new EndsWithConverter());

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
                    final String source1Value = source1.readString();
                    if (source1Value == null) {
                        value.writeBoolean(false);
                    } else {
                        final String source2Value = source2.readString();
                        value.writeBoolean(source2Value != null && source1Value.contains(source2Value));
                    }
                }
            };
        }
    }

    public static class StartsWithConverter implements Biconverter<StringReadable, StringReadable, StartsWith> {

        @Override
        public StartsWith convert(StringReadable source1, StringReadable source2) {
            return new StartsWith() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    final String source1Value = source1.readString();
                    if (source1Value == null) {
                        value.writeBoolean(false);
                    } else {
                        final String source2Value = source2.readString();
                        value.writeBoolean(source2Value != null && source1Value.startsWith(source2Value));
                    }
                }
            };
        }
    }

    public static class EndsWithConverter implements Biconverter<StringReadable, StringReadable, EndsWith> {

        @Override
        public EndsWith convert(StringReadable source1, StringReadable source2) {
            return new EndsWith() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    final String source1Value = source1.readString();
                    if (source1Value == null) {
                        value.writeBoolean(false);
                    } else {
                        final String source2Value = source2.readString();
                        value.writeBoolean(source2Value != null && source1Value.endsWith(source2Value));
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
                    final String value1 = source1.readString();
                    if (value1 == null) {
                        value.writeString(source2.readString());
                    } else {
                        final String value2 = source2.readString();
                        value.writeString(value2 == null ? value1 : (value1 + value2));
                    }
                }
            };
        }
    }
}
