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
package au.gov.asd.tac.constellation.graph.construction;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.StoreGraph;

/**
 * Builder class to add primary keys to a graph, or set primary keys to the
 * desired attribute
 *
 * @author twilight_sparkle
 */
public class PrimaryKeyBuilder extends GraphBuilder {

    private static final String PRIMARY_KEY_ATTRIBUTE_NAME = "DummyPrimaryKey";
    private static final String DEFAULT_KEY_TYPE = "integer";
    private static final int DEFAULT_KEY_VAL = 0;
    private static final int DEFAULT_NUM_VERTEX_KEYS = 1;
    private static final int DEFAULT_NUM_TRANS_KEYS = 1;

    public static PrimaryKeyBuilder addPrimaryKey(final GraphWriteMethods graph) {
        return addPrimaryKey(graph, DEFAULT_NUM_VERTEX_KEYS, DEFAULT_NUM_TRANS_KEYS, DEFAULT_KEY_TYPE, DEFAULT_KEY_TYPE, DEFAULT_KEY_VAL, DEFAULT_KEY_VAL, true, true);
    }

    public static PrimaryKeyBuilder addPrimaryKey(final int numberOfVertexKeys, final int numberOfTransactionKeys, final String vertexKeyType, final String transactionKeyType, final Object vertexDefaultValue, final Object transactionDefaultValue, final boolean fillVertexKeys, final boolean fillTransactionKeys) {
        return addPrimaryKey(new StoreGraph(), numberOfVertexKeys, numberOfTransactionKeys, vertexKeyType, transactionKeyType, vertexDefaultValue, transactionDefaultValue, fillVertexKeys, fillTransactionKeys);
    }

    public static PrimaryKeyBuilder addPrimaryKey(final GraphWriteMethods graph, final int numberOfVertexKeys, final int numberOfTransactionKeys, final String vertexKeyType, final String transactionKeyType, final Object vertexDefaultValue, final Object transactionDefaultValue, final boolean fillVertexKeys, final boolean fillTransactionKeys) {

        final int[] vertexKeys = makeKeys(graph, GraphElementType.VERTEX, numberOfVertexKeys, vertexKeyType, vertexDefaultValue);
        final int[] transactionKeys = makeKeys(graph, GraphElementType.TRANSACTION, numberOfTransactionKeys, transactionKeyType, transactionDefaultValue);

        KeySetter vertexKeySetter;
        switch (vertexKeyType) {
            case DEFAULT_KEY_TYPE:
                vertexKeySetter = new IntegerKeySetter();
                break;
            case "float":
                vertexKeySetter = new FloatKeySetter();
                break;
            default:
                vertexKeySetter = new StringKeySetter();
                break;
        }
        KeySetter transactionKeySetter;
        switch (transactionKeyType) {
            case DEFAULT_KEY_TYPE:
                transactionKeySetter = new IntegerKeySetter();
                break;
            case "float":
                transactionKeySetter = new FloatKeySetter();
                break;
            default:
                transactionKeySetter = new StringKeySetter();
                break;
        }

        if (fillVertexKeys) {
            fillKeys(graph, vertexKeySetter, GraphElementType.VERTEX, vertexKeys);
        }
        if (fillTransactionKeys) {
            fillKeys(graph, transactionKeySetter, GraphElementType.TRANSACTION, transactionKeys);
        }

        graph.setPrimaryKey(GraphElementType.VERTEX, vertexKeys);
        graph.setPrimaryKey(GraphElementType.TRANSACTION, transactionKeys);

        return new PrimaryKeyBuilder(graph, vertexKeys, transactionKeys);
    }

    public static int[] makeKeys(final GraphWriteMethods graph, final GraphElementType elementType, final int numberOfKeys, final String keyType, final Object defaultVal) {
        int[] keys = new int[numberOfKeys];
        for (int i = 0; i < numberOfKeys; i++) {
            keys[i] = graph.addAttribute(elementType, keyType, PRIMARY_KEY_ATTRIBUTE_NAME + Integer.toString(i), PRIMARY_KEY_ATTRIBUTE_NAME + Integer.toString(i), defaultVal, null);
        }
        return keys;
    }

    private static void fillKeys(final GraphWriteMethods graph, final KeySetter keySetter, final GraphElementType elementType, final int[] keyAttributes) {

        if (keyAttributes.length == 0) {
            return;
        }
        final int numOfElements = (elementType == GraphElementType.VERTEX) ? graph.getVertexCount() : graph.getTransactionCount();
        final int numKeys = keyAttributes.length;

        // Determine the number of different values required for each key
        final int[] keyLengths = new int[numKeys];
        int n = numOfElements;
        for (int i = numKeys; i > 0; i--) {
            keyLengths[i - 1] = (int) Math.ceil(Math.pow(n, Math.pow(i, -1)));
            n = (int) Math.ceil(n / ((double) i));
        }

        // Set up an array holding the current values numbers of the key attributes
        final int[] currentValues = new int[numKeys];
        for (int i = 0; i < numKeys; i++) {
            currentValues[i] = 0;
        }

        for (int i = 0; i < numOfElements; i++) {

            // For each graph element set its value for each key
            final int elId = elementType == GraphElementType.VERTEX ? graph.getVertex(i) : graph.getTransaction(i);
            for (int j = 0; j < numKeys; j++) {
                keySetter.setKey(graph, keyAttributes[j], elId, currentValues[j]);
            }

            // update the current value numbers of each key attribute as required.
            int currentKeyPosition = 0;
            while (true) {
                currentValues[currentKeyPosition] = (currentValues[currentKeyPosition] + 1) % keyLengths[currentKeyPosition];
                if (currentValues[currentKeyPosition] == 0) {
                    currentKeyPosition++;
                } else {
                    break;
                }
            }
        }
    }

    private interface KeySetter {

        public void setKey(final GraphWriteMethods graph, final int attrId, final int elId, final int valueNumber);

    }

    private static class IntegerKeySetter implements KeySetter {

        @Override
        public void setKey(final GraphWriteMethods graph, final int attrId, final int elId, final int valueNumber) {
            graph.setIntValue(attrId, elId, valueNumber);
        }

    }

    private static class StringKeySetter implements KeySetter {

        @Override
        public void setKey(final GraphWriteMethods graph, final int attrId, final int elId, final int valueNumber) {
            graph.setStringValue(attrId, elId, Integer.toString(valueNumber));
        }

    }

    private static class FloatKeySetter implements KeySetter {

        @Override
        public void setKey(final GraphWriteMethods graph, final int attrId, final int elId, final int valueNumber) {
            graph.setFloatValue(attrId, elId, ((float) valueNumber / 10));
        }

    }

    public final int[] vertexKeys;
    public final int[] transactionKeys;

    private PrimaryKeyBuilder(final GraphWriteMethods graph, final int[] vertexKeys, final int[] transactionKeys) {
        super(graph);
        this.vertexKeys = vertexKeys;
        this.transactionKeys = transactionKeys;
    }

}
