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
package au.gov.asd.tac.constellation.graph.value.types.booleans;

import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.Converter;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.And;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.ExclusiveOr;
import au.gov.asd.tac.constellation.graph.value.readables.Not;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;
import au.gov.asd.tac.constellation.graph.value.readables.Or;

/**
 *
 * @author sirius
 */
public class BooleanLogicConverters {

    private BooleanLogicConverters() {
        // added private constructor to hide implicit public constructor - S1118.
    }

    public static <P1 extends BooleanReadable, P2 extends BooleanReadable> void register(final ConverterRegistry r, 
            final Class<P1> parameterClass1, final Class<P2> parameterClass2) {
        r.register(parameterClass1, parameterClass2, And.class, new AndConverter());
        r.register(parameterClass1, parameterClass2, Or.class, new OrConverter());
        r.register(parameterClass1, parameterClass2, ExclusiveOr.class, new ExclusiveOrConverter());
        r.register(parameterClass1, parameterClass2, Equals.class, new EqualsConverter());
        r.register(parameterClass1, parameterClass2, NotEquals.class, new NotEqualsConverter());
    }

    public static <P extends BooleanReadable> void register(final ConverterRegistry r, final Class<P> parameterClass) {
        r.register(parameterClass, Not.class, new NotConverter());
    }

    public static class NotConverter implements Converter<BooleanReadable, Not> {

        @Override
        public Not convert(final BooleanReadable source) {
            return new Not() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(!source.readBoolean());
                }
            };
        }
    }

    public static class AndConverter implements Biconverter<BooleanReadable, BooleanReadable, And<BooleanValue>> {

        @Override
        public And<BooleanValue> convert(final BooleanReadable source1, final BooleanReadable source2) {
            return new And<>() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() && source2.readBoolean());
                }
            };
        }
    }

    public static class OrConverter implements Biconverter<BooleanReadable, BooleanReadable, Or<BooleanValue>> {

        @Override
        public Or<BooleanValue> convert(final BooleanReadable source1, final BooleanReadable source2) {
            return new Or<>() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() || source2.readBoolean());
                }
            };
        }
    }

    public static class ExclusiveOrConverter implements Biconverter<BooleanReadable, BooleanReadable, ExclusiveOr<BooleanValue>> {

        @Override
        public ExclusiveOr<BooleanValue> convert(final BooleanReadable source1, final BooleanReadable source2) {
            return new ExclusiveOr<>() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() ^ source2.readBoolean());
                }
            };
        }
    }

    public static class EqualsConverter implements Biconverter<BooleanReadable, BooleanReadable, Equals> {

        @Override
        public Equals convert(final BooleanReadable source1, final BooleanReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() == source2.readBoolean());
                }
            };
        }
    }

    public static class NotEqualsConverter implements Biconverter<BooleanReadable, BooleanReadable, NotEquals> {

        @Override
        public NotEquals convert(final BooleanReadable source1, final BooleanReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(final BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() != source2.readBoolean());
                }
            };
        }
    }
}
