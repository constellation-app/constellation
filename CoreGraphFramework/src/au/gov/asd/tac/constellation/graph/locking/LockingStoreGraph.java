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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.WritableGraph;
import static au.gov.asd.tac.constellation.graph.locking.LockingManager.VERBOSE;
import au.gov.asd.tac.constellation.graph.schema.Schema;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A LockingStoreGraph extends StoreGraph to provide methods releasing a read or
 * write lock.
 *
 * @author sirius
 */
public class LockingStoreGraph extends StoreGraph implements ReadableGraph, WritableGraph {

    private static final Logger LOGGER = Logger.getLogger(LockingStoreGraph.class.getName());

    private final LockingManager<?> lockingManager;
    private final int instance;

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final Schema schema) {
        super(schema);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final Schema schema, final String id) {
        super(schema, id);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final StoreGraph storeGraph) {
        super(storeGraph);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final StoreGraph storeGraph, final boolean newId) {
        super(storeGraph, newId);
        this.lockingManager = lockingManager;
        this.instance = instance;
    }

    public LockingStoreGraph(final LockingManager<?> lockingManager, final int instance, final StoreGraph storeGraph, final String id) {
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
    public void commit(final Object description, final String commitName) {
        lockingManager.commit(description, commitName);

        if (VERBOSE) {
            LOGGER.log(Level.INFO,"Write lock committed by {0}", Thread.currentThread());
        }
    }

    @Override
    public WritableGraph flush(final boolean announce) {
        return (WritableGraph) lockingManager.flush(null, announce);
    }

    @Override
    public WritableGraph flush(final Object description, final boolean announce) {
        return (WritableGraph) lockingManager.flush(description, announce);
    }

    @Override
    public void rollBack() {
        lockingManager.rollBack();
    }
}
