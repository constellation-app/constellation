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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import static au.gov.asd.tac.constellation.graph.locking.LockingManager.VERBOSE;
import au.gov.asd.tac.constellation.graph.schema.Schema;

/**
 * A LockingStoreGraph extends StoreGraph to provide methods releasing a read or
 * write lock.
 *
 * @author sirius
 */
public class LockingStoreGraph extends StoreGraph implements ReadableGraph, WritableGraph {

    private final LockingManager<?> lockingManager;
    private final int instance;

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, Schema schema) {
        super(schema);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, Schema schema, final String id) {
        super(schema, id);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final StoreGraph storeGraph) {
        super(storeGraph);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final StoreGraph storeGraph, boolean newId) {
        super(storeGraph, newId);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final StoreGraph storeGraph, String id) {
        super(storeGraph, id);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    @Override
    public void commit() throws DuplicateKeyException {
        commit(null);
    }

    @Override
    public void commit(final Object description) throws DuplicateKeyException {
        commit(description, null);
    }

    @Override
    public void commit(Object description, String commitName) {
        lockingManager.commit(description, commitName);

        if (VERBOSE) {
            System.out.println("Write lock committed by " + Thread.currentThread());
        }
    }

    @Override
    public WritableGraph flush(final boolean announce) {
        WritableGraph wg = (WritableGraph) lockingManager.flush(null, announce);
        return wg;
    }

    @Override
    public WritableGraph flush(final Object description, final boolean announce) {
        WritableGraph wg = (WritableGraph) lockingManager.flush(description, announce);
        return wg;
    }

    @Override
    public void rollBack() {
        lockingManager.rollBack();
    }

    @Override
    public void close() {
        release();
    }
}
