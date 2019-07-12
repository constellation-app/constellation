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
package au.gov.asd.tac.constellation.graph.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link RecordStore} which is structured like a table
 * with rows and columns.
 *
 * @author cygnus_x-1
 */
public class TabularRecordStore implements RecordStore {

    protected static final Object NULL = new Object();
    protected static final int BATCH_SIZE = 256;
    protected static final int BATCH_BITS = 8;
    protected static final int BATCH_MASK = 0xFF;
    protected static final int INITIAL_BATCH_COUNT = 1;

    protected Map<String, String> cache;

    protected final Map<String, Object[][]> records = new LinkedHashMap<>();
    protected int size = 0;
    protected int capacity = INITIAL_BATCH_COUNT * BATCH_SIZE;
    protected int currentRecord = -1;

    /**
     * Construct a TabularRecordStore.
     */
    public TabularRecordStore() {
        this(true);
    }

    /**
     * Construct a TabularRecordStore with the optional ability to cache keys
     * and values within the RecordStore for more efficient lookup.
     *
     * @param cacheStrings A flag indicating whether or not to create a cache
     * for {@link String} values to make lookup faster.
     */
    public TabularRecordStore(boolean cacheStrings) {
        if (cacheStrings) {
            cache = new HashMap<>();
        }
    }

    /**
     * Get the values associated with a column in this TabularRecordStore.
     *
     * @param key A {@link String value} representing a key in this
     * TabularRecordStore. This can be thought of as a column name.
     * @return An array of arrays of {@link Object} representing a set of values
     * from this TabularRecordStore. This data structure is such that arrays are
     * minimally created, effectively conserving memory.
     */
    protected Object[][] getColumn(String key) {
        Object[][] values = records.get(key);
        if (values == null) {
            values = records.get(key);
        }
        return values;
    }

    /**
     * Create a new column for this TabularRecordStore.
     *
     * @param key A {@link String value} representing a key in this
     * TabularRecordStore. This can be thought of as a column name.
     * @param values An array of arrays of {@link Object} representing a set of
     * values from this TabularRecordStore. This data structure is such that
     * arrays are minimally created, effectively conserving memory.
     */
    protected void createColumn(String key, Object[][] values) {
        records.put(key, values);
    }

    /**
     * Check if a value exists in the specified set of values extracted from
     * this TabularRecordStore.
     *
     * @param values An array of arrays of {@link Object} representing a set of
     * values from this TabularRecordStore. This data structure is such that
     * arrays are minimally created, effectively conserving memory.
     * @param record An integer value representing the id of the record you wish
     * to retrieve a value for.
     * @return True is the requested value exists, otherwise false.
     */
    protected static boolean hasValue(Object[][] values, int record) {
        if (values == null || values.length <= record >>> BATCH_BITS) {
            return false;
        }
        final Object[] batch = values[record >>> BATCH_BITS];
        if (batch == null) {
            return false;
        }
        return batch[record & BATCH_MASK] != null;
    }

    /**
     * Get a value from the specified set of values extracted from this
     * TabularRecordStore.
     *
     * @param values An array of arrays of {@link Object} representing a set of
     * values from this TabularRecordStore. This data structure is such that
     * arrays are minimally created, effectively conserving memory.
     * @param record An integer value representing the id of the record you wish
     * to retrieve a value for.
     * @return A {@link String} object representing the requested value.
     */
    protected static String getValue(Object[][] values, int record) {
        if (values == null || values.length <= record >>> BATCH_BITS) {
            return null;
        }
        final Object[] batch = values[record >>> BATCH_BITS];
        final Object value = batch == null ? null : batch[record & BATCH_MASK];
        return value == NULL ? null : (String) value;
    }

    @Override
    public int add() {
        currentRecord = size++;
        if (size > capacity) {
            capacity <<= 1;
        }
        return currentRecord;
    }

    @Override
    public void add(RecordStore recordStore) {
        for (int record = 0; record < recordStore.size(); record++) {
            final int newRecord = add();
            for (final String key : recordStore.keys()) {
                final String value = recordStore.get(record, key);
                if (value != null) {
                    set(newRecord, key, value);
                }
            }
        }
    }

    @Override
    public int index() {
        return currentRecord;
    }

    @Override
    public final boolean next() {
        if (++currentRecord >= size) {
            currentRecord = size;
            return false;
        }
        return true;
    }

    @Override
    public void reset() {
        currentRecord = -1;
    }

    @Override
    public void close() {
        cache = null;
    }

    @Override
    public boolean hasValue(String key) {
        return hasValue(currentRecord, key);
    }

    @Override
    public boolean hasValue(int record, String key) {
        return TabularRecordStore.hasValue(getColumn(key), record);
    }

    @Override
    public String get(String key) {
        return get(currentRecord, key);
    }

    @Override
    public String get(int record, String key) {
        return TabularRecordStore.getValue(getColumn(key), record);
    }

    @Override
    public void set(String key, String value) {
        set(currentRecord, key, value);
    }

    @Override
    public void set(int record, String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null.");
        }
        if (record < 0 || record >= size) {
            throw new IllegalArgumentException("Invalid record: " + record);
        }

        if (cache != null) {
            if (value != null) {
                final String unique = cache.get(value);
                if (unique != null) {
                    value = unique;
                } else {
                    cache.put(value, value);
                }
            }

            final String unique = cache.get(key);
            if (unique != null) {
                key = unique;
            } else {
                cache.put(key, key);
            }
        }

        Object[][] values = getColumn(key);

        // If this is the first time this key has been set
        if (values == null) {
            values = new Object[capacity >>> BATCH_BITS][];
            createColumn(key, values);

            // If there is not enough capacity to hold this record
        } else if (values.length <= record >>> BATCH_BITS) {
            values = Arrays.copyOf(values, capacity >>> BATCH_BITS);
            createColumn(key, values);
        }

        Object[] batch = values[record >>> BATCH_BITS];
        if (batch == null) {
            values[record >>> BATCH_BITS] = batch = new Object[BATCH_SIZE];
        }
        batch[record & BATCH_MASK] = value == null ? NULL : value;
    }

    @Override
    public List<String> values() {
        return values(currentRecord);
    }

    @Override
    public List<String> values(int record) {
        final List<String> values = new ArrayList<>(records.size());
        for (Object[][] v : records.values()) {
            values.add(TabularRecordStore.getValue(v, record));
        }
        return values;
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(records.keySet());
    }

    @Override
    public List<String> getAll(String key) {
        final Object[][] values = getColumn(key);
        final List<String> result = new ArrayList<>(size);
        for (int record = 0; record < size; record++) {
            result.add(TabularRecordStore.getValue(values, record));
        }
        return result;
    }

    @Override
    public int size() {
        return size;
    }

//    @Override
//    public int hashCode() {
//        int hash = 3;
//        hash = 73 * hash + Objects.hashCode(this.cache);
//        hash = 73 * hash + Objects.hashCode(this.records);
//        hash = 73 * hash + this.size;
//        hash = 73 * hash + this.capacity;
//        hash = 73 * hash + this.currentRecord;
//        return hash;
//    }
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final TabularRecordStore other = (TabularRecordStore) obj;
//        if (!Objects.equals(this.cache, other.cache)) {
//            return false;
//        }
//        if (!Objects.equals(this.records, other.records)) {
//            return false;
//        }
//        if (this.size != other.size) {
//            return false;
//        }
//        if (this.capacity != other.capacity) {
//            return false;
//        }
//
////                if (this.typedRecords.size() != other.typedRecords.size()) {
////            return false;
////        }
////
////        // now that the size is the same, it will mean that looping through the keys will not miss anything
////        for (final Map.Entry<String, Object[][]> entry : this.typedRecords.entrySet()) {
////            if (!other.typedRecords.containsKey(entry.getKey())) {
////                return false;
////            }
////
////            for (int record = 0; record < size; record++) {
////                for (final Map.Entry<String, Object[][]> e : this.typedRecords.entrySet()) {
////                    if (!(TabularRecordStore.hasValue(e.getValue(), record) && TabularRecordStore.hasValue(other.typedRecords.get(e.getKey()), record))) {
////                        return false;
////                    }
////
////                    if (!TabularRecordStore.getValue(e.getValue(), record).equals(TabularRecordStore.getValue(other.typedRecords.get(e.getKey()), record))) {
////                        return false;
////                    }
////                }
////            }
////        }
//        return true;
//    }
    @Override
    public String toString() {
        return "Record Store with " + size + " rows and " + records.size() + " columns.";
    }

    @Override
    public String toStringVerbose() {
        final StringBuilder out = new StringBuilder();
        for (int record = 0; record < size; record++) {
            boolean first = true;
            for (final Map.Entry<String, Object[][]> e : records.entrySet()) {
                if (TabularRecordStore.hasValue(e.getValue(), record)) {
                    if (!first) {
                        out.append(", ");
                    } else {
                        first = false;
                    }
                    out.append(e.getKey());
                    out.append(" = ");
                    out.append(TabularRecordStore.getValue(e.getValue(), record));
                }
            }
            out.append('\n');
        }
        return out.toString();
    }
}
