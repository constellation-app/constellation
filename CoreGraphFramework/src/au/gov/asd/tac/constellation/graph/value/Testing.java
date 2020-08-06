/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.graph.value;

import java.util.HashMap;
import java.util.Map;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionFilter;
import au.gov.asd.tac.constellation.graph.value.expression.ExpressionParser;
import au.gov.asd.tac.constellation.graph.value.ValueStore;
import au.gov.asd.tac.constellation.graph.value.converter.ConverterRegistry;
import au.gov.asd.tac.constellation.graph.value.expression.IndexedReadableProvider;
import au.gov.asd.tac.constellation.graph.value.types.booleanType.BooleanReadable;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntReadable;
import au.gov.asd.tac.constellation.graph.value.types.integerType.IntValueStore;

/**
 *
 * @author darren
 */
public class Testing {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        var token = ExpressionParser.parse("(age < '5') != score >= '5'");
        token.normalize();
        System.out.println(token);
        
        final var ageValueStore = new IntValueStore(new int[] {3, 7, 2, 9});
        final var scoreValueStore = new IntValueStore(new int[] {11, 4, 1, 12});
        
        final Map<String,ValueStore> valueStores = new HashMap<>();
        valueStores.put("age", ageValueStore);
        valueStores.put("score", scoreValueStore);
        final IndexedReadableProvider indexedReadableProvider = name -> {
            return valueStores.get(name);
        };
        
        final var expression = ExpressionFilter.createExpressionReadable(token, indexedReadableProvider, ConverterRegistry.getDefault());
        final var expressionValue = expression.createValue();
        final var expressionInt = ConverterRegistry.getDefault().convert(expressionValue, IntReadable.class);
        final var expressionBoolean = ConverterRegistry.getDefault().convert(expressionValue, BooleanReadable.class);
        
        for (int i = 0; i < 4; i++) {
            expression.read(i, expressionValue);
            System.out.println(expressionBoolean.readBoolean());
        }
    }
    
}
