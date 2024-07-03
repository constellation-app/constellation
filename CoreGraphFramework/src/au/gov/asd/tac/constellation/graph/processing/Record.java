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
 * An interface for a simple key-value map of Strings. Primarily used as a base
 * class of RecordStore, a table-like data structure which can be thought to
 * comprise several Records.
 *
 * @see RecordStore
 * @author capella
 */
public interface Record {

    /**
     * Checks if a key exists in this record.
     *
     * @param key The key to lookup
     * @return True if this record has a value for the specified key. False
     * otherwise.
     */
    public boolean hasValue(final String key);

    /**
     * Returns a value from this record.
     *
     * @param key The key to lookup
     * @return The value associated with this key if hasValue(key) is true. Null
     * otherwise
     */
    public String get(final String key);

    /**
     * Sets a key/value pair for this record
     *
     * @param key The key to set a value for.
     * @param value The value to set.
     */
    public void set(final String key, final String value);

    /**
     * Set a key/value pair for this record with an Object value. This is a
     * shortcut for calling
     * <pre><code>
     * set(key, value == null ? null : value.toString());
     * </code></pre>
     *
     * @param key The key whose value is being set
     * @param value The value to set.
     */
    public default void set(final String key, final Object value) {
        set(key, value == null ? null : value.toString());
    }

    /**
     * Return the collection of keys used in this record.
     *
     * @return A list of strings containing all the string keys in this record.
     */
    public List<String> keys();

    /**
     * Returns the collection of values stored in this record.
     *
     * @return A list of strings containing all the values in this record.
     */
    public List<String> values();
}
