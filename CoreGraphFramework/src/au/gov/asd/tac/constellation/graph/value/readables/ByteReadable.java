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
package au.gov.asd.tac.constellation.graph.value.readables;

import au.gov.asd.tac.constellation.graph.value.Updatable;

/**
 *
 * @author sirius
 */
public interface ByteReadable extends ShortReadable, Updatable {

    byte readByte();

    @Override
    default short readShort() {
        return readByte();
    }

    @Override
    default int readInt() {
        return readByte();
    }

    @Override
    default long readLong() {
        return readByte();
    }

    @Override
    default float readFloat() {
        return readByte();
    }

    @Override
    default double readDouble() {
        return readByte();
    }

    @Override
    default void update() {
        readByte();
    }
}
