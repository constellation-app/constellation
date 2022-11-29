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
package au.gov.asd.tac.constellation.utilities.memory;

/**
 * A MemoryManagerListener is alerted by the {@link MemoryManager} when objects
 * of registered classes are instantiated or finalized. Listening to these
 * events is mainly of use in debugging situations to ensure that objects are
 * being properly garbage collected.
 *
 * @author sirius
 */
public interface MemoryManagerListener {

    /**
     * Called by the {@link MemoryManager} when an object of a registered class
     * is instantiated.
     *
     * @param c the class of the object that has been instantiated.
     */
    public void newObject(Class<?> c);

    /**
     * Called by the {@link MemoryManager} when an object of a registered class
     * is finalized.
     *
     * @param c the class of the object that has been finalized.
     */
    public void finalizeObject(Class<?> c);

}
