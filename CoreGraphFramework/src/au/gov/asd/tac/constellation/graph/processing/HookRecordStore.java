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
package au.gov.asd.tac.constellation.graph.processing;

import java.util.List;

/**
 * A HookRecordStore acts as a delegate for a specified {@link RecordStore} with
 * the addition that it has the opportunity to perform some additional operation
 * when adding data to that {@link RecordStore}.
 *
 * @author capella
 */
public class HookRecordStore implements RecordStore {

    private final RecordStore recordStore;
    private final HookRecordStoreCallback callback;

    /**
     * Construct a HookRecordStore with a {@link RecordStore} to delegate to as
     * well as a {@link HookRecordStoreCallback} to facilitate extra operations
     * when {@link #add} is called.
     *
     * @param recordStore A {@link RecordStore} to delegate operations to.
     * @param callback A {@link HookRecordStoreCallback} to facilitate extra
     * operations when adding data to the specified {@link RecordStore}.
     */
    public HookRecordStore(final RecordStore recordStore, final HookRecordStoreCallback callback) {
        this.recordStore = recordStore;
        this.callback = callback;
    }

    @Override
    public int add() {
        int result = recordStore.add();
        callback.onAdd(recordStore);
        return result;
    }

    @Override
    public void add(final RecordStore recordStore) {
        throw new UnsupportedOperationException("You cannot add a RecordStore to a HookRecordStore.");
    }

    @Override
    public void close() {
        recordStore.close();
    }

    @Override
    public String get(final int record, final String key) {
        return recordStore.get(record, key);
    }

    @Override
    public List<String> getAll(final String key) {
        return recordStore.getAll(key);
    }

    @Override
    public boolean hasValue(final int record, final String key) {
        return recordStore.hasValue(record, key);
    }

    @Override
    public boolean next() {
        return recordStore.next();
    }

    @Override
    public void reset() {
        recordStore.reset();
    }

    @Override
    public void set(final int record, final String key, final String value) {
        recordStore.set(record, key, value);
    }

    @Override
    public int size() {
        return recordStore.size();
    }

    @Override
    public String toStringVerbose() {
        return recordStore.toStringVerbose();
    }

    @Override
    public List<String> values(final int record) {
        return recordStore.values(record);
    }

    @Override
    public String get(final String key) {
        return recordStore.get(key);
    }

    @Override
    public boolean hasValue(final String key) {
        return recordStore.hasValue(key);
    }

    @Override
    public List<String> keys() {
        return recordStore.keys();
    }

    @Override
    public void set(final String key, final String value) {
        recordStore.set(key, value);
    }

    @Override
    public List<String> values() {
        return recordStore.values();
    }

    @Override
    public int index() {
        return recordStore.index();
    }
}
