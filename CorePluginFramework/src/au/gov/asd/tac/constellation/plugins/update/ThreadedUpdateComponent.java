/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.update;

/**
 * An extension of {@link UpdateComponent} that ensures updates are always
 * started on a new thread.
 *
 * @see UpdateComponent
 * @author sirius
 */
public abstract class ThreadedUpdateComponent<U> extends UpdateComponent<U> {

    private final String threadName;

    protected ThreadedUpdateComponent(UpdateController<U> controller, String threadName) {
        this.threadName = threadName;
    }

    @Override
    protected boolean updateThread(Runnable runnable) {
        if (threadName.equals(Thread.currentThread().getName())) {
            return false;
        }
        new Thread(runnable, threadName).start();
        return true;
    }

}
