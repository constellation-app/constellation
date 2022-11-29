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

/**
 * The HookRecordStoreCallback is an object which defines an operation to be
 * performed after adding a record to a {@link HookRecordStore}.
 *
 * @author capella
 */
@FunctionalInterface
public interface HookRecordStoreCallback {

    /**
     * Define an additional operation to perform after adding a record to the
     * specified {@link RecordStore}.
     *
     * @param recordStore A {@link RecordStore} on which to perform this
     * operation.
     */
    public void onAdd(final RecordStore recordStore);
}
