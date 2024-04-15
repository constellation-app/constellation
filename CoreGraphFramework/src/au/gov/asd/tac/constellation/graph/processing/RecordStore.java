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
package au.gov.asd.tac.constellation.graph.processing;

import java.util.List;

/**
 * An interface for a data structure consisting of multiple records, each of
 * which is a key-value map of Strings. The union of key sets across the records
 * can be thought of as columns, while the records themselves can be though of
 * rows, hence this can be imagined and implemented as a sparse table.
 * <p>
 * Note that RecordStore extends the Record interface. There is no requirement
 * for an implementation of RecordStore to use an implementation of the Record
 * interface to store each row. However, calling Record methods on a RecordStore
 * should be equivalent to calling the same method on the current row as if it
 * were an implementation of Record.
 * <p>
 * RecordStore is typically used as an interchange format between graphs and
 * plugins that require, or return, a tabular view of a graph. It is also a
 * convenient way of representing the data in a graph independently from the
 * graph implementation, and so is often used to copy data between graphs.
 * <p>
 * A common task with RecordStore is iterating through its list of records and
 * getting or setting various key/value pairs. A simple example of this workflow
 * is as follows:
 *
 * <pre>
 * <code>
 * recordStore.reset();
 * while (recordStore.next()) {
 *      String value = recordStore.get(key); // Get the value for a given key
 *      value = doSomething(value); // process the value in some way
 *      recordStore.set(key, value); // set the updated value for the given key
 * }
 * </code>
 * </pre>
 *
 * @see
 * au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin
 * @see RecordStoreUtilities
 * @see TabularRecordStore
 *
 * @author sirius
 */
public interface RecordStore extends Record {

    /**
     * Add a new record and sets the current record to point to the new record.
     *
     * @return The index of the new record.
     */
    public int add();

    /**
     * Copy all records from the specified record store into this record store
     * without changing the current record.
     *
     * @param recordStore The RecordStore to add all records from.
     */
    public void add(final RecordStore recordStore);

    /**
     * Get the index of the current record.
     *
     * @return The index of the current record
     */
    public int index();

    /**
     * Advance the current record.
     *
     * @return true if the current record has not moved past the last record.
     */
    public boolean next();

    /**
     * Reset the current record to just before the first record in preparation
     * for a call to next().
     */
    public void reset();

    /**
     * Close this record store.
     */
    public void close();

    /**
     * Check if the specified record has a value for the specified key.
     *
     * @param record The index of the record
     * @param key The key to lookup in the record
     * @return True if the specified record exists and has a value for the given
     * key. False otherwise.
     */
    public boolean hasValue(final int record, final String key);

    /**
     * Get a value from the specified record.
     *
     * @param record The index of the record
     * @param key The key to lookup
     * @return The String value associated with the key for the specified
     * record, if hasValue(record, key) is true. Null otherwise.
     */
    public String get(final int record, final String key);

    /**
     * Set a key/value pair for the specified record.
     *
     * @param record The index of the record
     * @param key The key whose value is being set
     * @param value The value to set.
     */
    public void set(final int record, final String key, final String value);

    /**
     * Set a key/value pair for the specified record with an Object value. This
     * is a shortcut for calling
     * <pre><code>
     * set(record, key, value == null ? null : value.toString());
     * </code></pre>
     *
     * @param record The index of the record
     * @param key The key whose value is being set
     * @param value The value to set.
     */
    public default void set(final int record, final String key, final Object value) {
        set(record, key, value == null ? null : value.toString());
    }

    /**
     * Return the collection of values in the specified record.
     *
     * @param record the index of the record to return values for
     * @return A list of the string values for the specified record
     */
    public List<String> values(final int record);

    /**
     * Return a list containing the values for a specified key for all records.
     * The value for a record will appear in the corresponding position in the
     * list.
     *
     * @param key The string key to return values for
     * @return A list of string values for the specified key
     */
    public List<String> getAll(final String key);

    /**
     * Return the number of records in this RecordStore.
     *
     * @return The number of records in this RecordStore
     */
    public int size();

    /**
     * Return a string containing the entire record store.
     *
     * @return A string representing all the data in the RecordStore
     */
    public String toStringVerbose();
}
