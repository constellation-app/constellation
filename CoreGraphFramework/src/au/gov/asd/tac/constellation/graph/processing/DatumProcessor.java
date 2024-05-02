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

/**
 * A DatumProcessor is a class which defines how to translate an input datum
 * object into a {@link RecordStore}.
 *
 * @param <T> The type of the input datum to be processed.
 * @param <U> The type of the object used to hold any parameters used for
 * processing.
 *
 * @author twilight_sparkle
 */
@FunctionalInterface
public interface DatumProcessor<T, U> {

    /**
     * Process an input data object to a RecordStore.
     *
     * @param parameters An object which defines parameter names and values to
     * be used during processing.
     * @param input The object representing a single datum to be processed.
     * @param output The {@link RecordStore} accumulating the results of
     * processing.
     * @throws ProcessingException If there is some problem during the
     * transformation from datum to {@link RecordStore}.
     */
    public void process(final U parameters, final T input, final RecordStore output) throws ProcessingException;
}
