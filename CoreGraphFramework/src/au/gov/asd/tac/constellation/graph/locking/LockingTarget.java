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
package au.gov.asd.tac.constellation.graph.locking;

import au.gov.asd.tac.constellation.graph.DuplicateKeyException;
import static au.gov.asd.tac.constellation.graph.locking.LockingManager.VERBOSE;
import au.gov.asd.tac.constellation.graph.undo.GraphEdit;
import java.util.concurrent.locks.Lock;

/**
 * manages the locking of a graph
 *
 * @author sirius
 */
public abstract class LockingTarget implements ReadingInterface {

    Lock lock;
    protected GraphOperationMode operationMode = GraphOperationMode.EXECUTE;

    @Override
    public void release() {
        lock.unlock();

        if (VERBOSE) {
            System.out.println("Read lock released by " + Thread.currentThread());
        }
    }

    public void update() {
    }

    public abstract long getModificationCounter();

    public abstract void validateKeys() throws DuplicateKeyException;

    public abstract void setGraphEdit(final GraphEdit graphEdit);

    public void setOperationMode(final GraphOperationMode operationMode) {
        this.operationMode = operationMode;
    }
}
