/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value.types;

import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanLogicConverters;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanValue;

/**
 *
 * @author darren
 */
public class LogicConverters {
    
    private static boolean REGISTERED = false;
    
    public static synchronized void register() {
        if (!REGISTERED) {
            register(ConverterRegistry.getDefault());
            REGISTERED = true;
        }
    }
    
    public static void register(ConverterRegistry r) {
        BooleanLogicConverters.register(r, BooleanValue.class);
        BooleanLogicConverters.register(r, BooleanValue.class, BooleanValue.class);
    }
}
