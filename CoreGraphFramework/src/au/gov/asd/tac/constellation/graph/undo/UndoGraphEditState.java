/*
 * Copyright 2010-2019 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.graph.undo;

import au.gov.asd.tac.constellation.graph.GraphOperation;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sirius
 */
public class UndoGraphEditState {

    private static final boolean PRINT_STATS = false;

    static final int REPEAT_MASK = 3;
    static final int REPEAT_SHIFT = 5;
    static final int OPERATION_MASK = 0x1F;

    public short[] operationStack = new short[1];
    public int operationCount = 0;

    public byte[] byteStack = new byte[1];
    public int byteCount = 0, bytePointer;

    public short[] shortStack = new short[1];
    public int shortCount = 0, shortPointer;

    public int[] intStack = new int[1];
    public int intCount = 0, intPointer;

    public long[] longStack = new long[1];
    public int longCount = 0, longPointer;

    public Object[] objectStack = new Object[1];
    public int objectCount = 0;
    public Map<Object, Integer> objectMap = new HashMap<>();

    public int currentAttribute = 0, currentId = 0, currentInt, currentObject = 0, currentFloat = 0;
    ;
    public long currentLong = 0, currentDouble = 0;
    ;

    public int finalAttribute, finalId, finalInt, finalObject, finalFloat;
    public long finalLong, finalDouble;

    private short currentOperation = 0xFF;
    private int extraOperationsCount = 0;

    public UndoGraphEditState() {
    }

    public UndoGraphEditState(DataInputStream in) throws Exception {

        operationCount = in.readInt();
        operationStack = new short[operationCount];
        for (int i = 0; i < operationCount; i++) {
            operationStack[i] = in.readShort();
        }

        byteCount = in.readInt();
        byteStack = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            byteStack[i] = in.readByte();
        }

        shortCount = in.readInt();
        shortStack = new short[shortCount];
        for (int i = 0; i < shortCount; i++) {
            shortStack[i] = in.readShort();
        }

        intCount = in.readInt();
        intStack = new int[intCount];
        for (int i = 0; i < intCount; i++) {
            intStack[i] = in.readInt();
        }

        longCount = in.readInt();
        longStack = new long[longCount];
        for (int i = 0; i < longCount; i++) {
            longStack[i] = in.readLong();
        }

        Map<Integer, Class<?>> classMap = new HashMap<>();
        objectMap.put(0, null);
        byte[] buffer = new byte[1024];

        objectCount = in.readInt();
        objectStack = new Object[objectCount];
        for (int i = 0; i < objectCount; i++) {
            int objectClassIndex = in.readInt();
            Class<?> objectClass;
            if (objectMap.containsKey(objectClassIndex)) {
                objectClass = classMap.get(objectClassIndex);
            } else {
                int classLength = in.readInt();
                if (classLength > buffer.length) {
                    buffer = Arrays.copyOf(buffer, classLength);
                }
                in.read(buffer, 0, classLength);
                String objectName = new String(buffer, 0, classLength, UTF8);
                objectClass = Class.forName(objectName);
                classMap.put(objectClassIndex, objectClass);
            }
        }
    }

    public void addInstruction(short operation) {
        if (currentOperation == operation) {
            operationStack[operationCount - 1] = (short) (operation | (++extraOperationsCount << REPEAT_SHIFT));
            if (extraOperationsCount == REPEAT_MASK) {
                currentOperation = 0xFF;
                extraOperationsCount = 0;
            }
        } else {
            if (operationCount == operationStack.length) {
                operationStack = Arrays.copyOf(operationStack, operationCount * 2);
            }
            operationStack[operationCount++] = operation;
            currentOperation = operation;
            extraOperationsCount = 0;
        }
    }

    public void addByte(byte value) {
        if (byteCount == byteStack.length) {
            byteStack = Arrays.copyOf(byteStack, byteCount * 2);
        }
        byteStack[byteCount++] = value;
    }

    public void addShort(short value) {
        if (shortCount == shortStack.length) {
            shortStack = Arrays.copyOf(shortStack, shortCount * 2);
        }
        shortStack[shortCount++] = value;
    }

    public void addInt(int value) {
        if (intCount == intStack.length) {
            intStack = Arrays.copyOf(intStack, intCount * 2);
        }
        intStack[intCount++] = value;
    }

    public void addLong(long value) {
        if (longCount == longStack.length) {
            longStack = Arrays.copyOf(longStack, longCount * 2);
        }
        longStack[longCount++] = value;
    }

    public int addObject(Object object) {
        Integer existingObjectIndex = objectMap.get(object);
        if (existingObjectIndex != null) {
            return existingObjectIndex;
        }
        objectMap.put(object, objectCount);
        if (objectCount == objectStack.length) {
            objectStack = Arrays.copyOf(objectStack, objectCount * 2);
        }
        int objectIndex = objectCount;
        objectStack[objectCount++] = object;
        return objectIndex;
    }

    public void finish() {
        operationStack = Arrays.copyOf(operationStack, operationCount);
        byteStack = Arrays.copyOf(byteStack, byteCount);
        shortStack = Arrays.copyOf(shortStack, shortCount);
        intStack = Arrays.copyOf(intStack, intCount);
        longStack = Arrays.copyOf(longStack, longCount);
        objectStack = Arrays.copyOf(objectStack, objectCount);

        objectMap = null;

        finalAttribute = currentAttribute;
        finalId = currentId;
        finalInt = currentInt;
        finalObject = currentObject;
        finalFloat = currentFloat;
        finalLong = currentLong;
        finalDouble = currentDouble;

        if (PRINT_STATS) {
            printStats();
        }
    }

    public void printStats() {
        bytePointer = 0;
        shortPointer = 0;
        intPointer = 0;
        longPointer = 0;

        currentAttribute = 0;
        currentId = 0;
        currentInt = 0;
        currentObject = 0;
        currentFloat = 0;
        currentLong = 0L;
        currentDouble = 0L;

        int[][] stats = new int[UndoGraphEditOperation.values().length][6];
        int oldBytePointer = 0;
        int oldShortPointer = 0;
        int oldIntPointer = 0;
        int oldLongPointer = 0;

        int graphOperationCount = 0;

        for (int operation : operationStack) {

            int runCount = ((operation >>> REPEAT_SHIFT) & REPEAT_MASK) + 1;
            for (int run = 0; run < runCount; run++) {

                UndoGraphEditOperation op = UndoGraphEditOperation.values()[operation & OPERATION_MASK];
                op.updateExecute(this, operation);

                if (op == UndoGraphEditOperation.EXECUTE_GRAPH_OPERATION) {
                    graphOperationCount += ((GraphOperation) objectStack[currentObject]).size();
                }
            }

            stats[operation & OPERATION_MASK][0]++;
            stats[operation & OPERATION_MASK][1] += runCount;
            stats[operation & OPERATION_MASK][2] += bytePointer - oldBytePointer;
            stats[operation & OPERATION_MASK][3] += shortPointer - oldShortPointer;
            stats[operation & OPERATION_MASK][4] += intPointer - oldIntPointer;
            stats[operation & OPERATION_MASK][5] += longPointer - oldLongPointer;

            oldBytePointer = bytePointer;
            oldShortPointer = shortPointer;
            oldIntPointer = intPointer;
            oldLongPointer = longPointer;
        }

        int total = (operationCount * 2) + byteCount + (shortCount * 2) + (intCount * 4) + (longCount * 8) + (objectCount * 4) + graphOperationCount;
        System.out.println("STATS: OPERATIONS = " + operationCount + " BYTES = " + byteCount + " SHORTS = " + shortCount + " INTS = " + intCount + " LONGS = " + longCount + " OBJECTS = " + objectCount + " GRAPH_OPERATIONS = " + graphOperationCount + " TOTAL = " + total);
        for (UndoGraphEditOperation operation : UndoGraphEditOperation.values()) {
            System.out.print("    " + operation.ordinal() + " " + operation.getName());
            int[] counts = stats[operation.ordinal()];
            for (int i = 0; i < 6; i++) {
                System.out.print(" " + counts[i]);
            }
            int size = (counts[0] * 2) + counts[2] + (counts[3] * 2) + (counts[4] * 4) + (counts[5] * 8);
            System.out.println(" " + size);
        }
    }

    public void execute(GraphWriteMethods graph) {

        bytePointer = 0;
        shortPointer = 0;
        intPointer = 0;
        longPointer = 0;

        currentAttribute = 0;
        currentId = 0;
        currentInt = 0;
        currentObject = 0;
        currentFloat = 0;
        currentLong = 0L;
        currentDouble = 0L;

        for (int operation : operationStack) {

            int runCount = ((operation >>> REPEAT_SHIFT) & REPEAT_MASK) + 1;
            for (int run = 0; run < runCount; run++) {
                UndoGraphEditOperation.values()[operation & OPERATION_MASK].updateExecute(this, operation);
                UndoGraphEditOperation.values()[operation & OPERATION_MASK].execute(this, graph);
            }
        }
    }

    public void undo(GraphWriteMethods graph) {

        bytePointer = byteCount;
        shortPointer = shortCount;
        intPointer = intCount;
        longPointer = longCount;

        currentAttribute = finalAttribute;
        currentId = finalId;
        currentInt = finalInt;
        currentObject = finalObject;
        currentFloat = finalFloat;
        currentLong = finalLong;
        currentDouble = finalDouble;

        for (int operationIndex = operationCount - 1; operationIndex >= 0; operationIndex--) {
            int operation = operationStack[operationIndex];

            int runCount = ((operation >>> REPEAT_SHIFT) & REPEAT_MASK) + 1;
            for (int run = 0; run < runCount; run++) {
                UndoGraphEditOperation.values()[operation & OPERATION_MASK].undo(this, graph);
                UndoGraphEditOperation.values()[operation & OPERATION_MASK].updateUndo(this, operation);
            }
        }
    }

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public void write(DataOutputStream out) throws IOException {

        out.writeInt(operationCount);
        for (int i = 0; i < operationCount; i++) {
            out.writeShort(operationStack[i]);
        }

        out.writeInt(byteCount);
        for (int i = 0; i < byteCount; i++) {
            out.writeByte(byteStack[i]);
        }

        out.writeInt(shortCount);
        for (int i = 0; i < shortCount; i++) {
            out.writeShort(shortStack[i]);
        }

        out.writeInt(intCount);
        for (int i = 0; i < intCount; i++) {
            out.writeInt(intStack[i]);
        }

        out.writeInt(longCount);
        for (int i = 0; i < longCount; i++) {
            out.writeLong(longStack[i]);
        }

        final Map<Class<?>, Integer> classMap = new HashMap<>();
        classMap.put(null, 0);

        out.writeInt(objectCount);
        for (int i = 0; i < objectCount; i++) {
            final Object object = objectStack[i];
            final Class<?> objectClass = object == null ? null : object.getClass();
            if (classMap.containsKey(objectClass)) {
                out.writeInt(classMap.get(objectClass));
            } else {
                out.writeInt(classMap.size());
                if (objectClass != null) {
                    final String className = objectClass.getCanonicalName();
                    byte[] utf8 = className.getBytes(UTF8);
                    out.write(utf8.length);
                    out.write(utf8);
                }
                classMap.put(objectClass, classMap.size());
            }
        }
    }
}
