/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types.booleanType;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.readables.And;
import au.gov.asd.tac.constellation.graph.value.readables.Or;
import au.gov.asd.tac.constellation.graph.value.converter.Biconverter;
import au.gov.asd.tac.constellation.graph.value.converter.Converter;
import au.gov.asd.tac.constellation.graph.value.readables.Equals;
import au.gov.asd.tac.constellation.graph.value.readables.Not;
import au.gov.asd.tac.constellation.graph.value.readables.NotEquals;

/**
 *
 * @author darren
 */
public class BooleanLogicConverters {
    
    public static <P1 extends BooleanReadable, P2 extends BooleanReadable> void register(ConverterRegistry r, Class<P1> parameterClass1, Class<P2> parameterClass2) {
        r.register(parameterClass1, parameterClass2, And.class, new AndConverter());
        r.register(parameterClass1, parameterClass2, Or.class, new OrConverter());
        r.register(parameterClass1, parameterClass2, Equals.class, new EqualsConverter());
        r.register(parameterClass1, parameterClass2, NotEquals.class, new NotEqualsConverter());
    }
    
    public static <P extends BooleanReadable> void register(ConverterRegistry r, Class<P> parameterClass) {
        r.register(parameterClass, Not.class, new NotConverter());
    }
    
    public static class NotConverter implements Converter<BooleanReadable, Not> {
        @Override
        public Not convert(BooleanReadable source) {
            return new Not() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(!source.readBoolean());
                }
            };
        }
    }
    
    public static class AndConverter implements Biconverter<BooleanReadable, BooleanReadable, And> {
        @Override
        public And convert(BooleanReadable source1, BooleanReadable source2) {
            return new And() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() && source2.readBoolean());
                }
            };
        }
    }
    
    public static class OrConverter implements Biconverter<BooleanReadable, BooleanReadable, Or> {
        @Override
        public Or convert(BooleanReadable source1, BooleanReadable source2) {
            return new Or() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() || source2.readBoolean());
                }
            };
        }
    }
    
    public static class EqualsConverter implements Biconverter<BooleanReadable, BooleanReadable, Equals> {
        @Override
        public Equals convert(BooleanReadable source1, BooleanReadable source2) {
            return new Equals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() == source2.readBoolean());
                }
            };
        }
    }
    
    public static class NotEqualsConverter implements Biconverter<BooleanReadable, BooleanReadable, NotEquals> {
        @Override
        public NotEquals convert(BooleanReadable source1, BooleanReadable source2) {
            return new NotEquals() {
                @Override
                public BooleanValue createValue() {
                    return new BooleanValue();
                }

                @Override
                public void read(BooleanValue value) {
                    value.writeBoolean(source1.readBoolean() != source2.readBoolean());
                }
            };
        }
    }
}
