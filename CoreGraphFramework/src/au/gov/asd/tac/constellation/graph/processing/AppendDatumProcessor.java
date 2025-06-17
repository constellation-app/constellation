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
package au.gov.asd.tac.constellation.graph.processing;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An AppendDatumProcessor processes an input datum object through the specified
 * append {@link DatumProcessor} and appends this result to the each record
 * produced by processing the input datum object through the specified lead
 * {@link DatumProcessor}.
 *
 * @param <T> The type of the input datum to be processed.
 * @param <U> The type of the object used to hold any parameters used for
 * processing.
 *
 * @author capella
 */
public abstract class AppendDatumProcessor<T, U> implements DatumProcessor<T, U> {

    private static final Logger LOGGER = Logger.getLogger(AppendDatumProcessor.class.getName());

    protected final DatumProcessor<T, U> leadProcessor;
    protected final DatumProcessor<T, U> appendProcessor;

    /**
     * Construct an AppenDatumProcessor with a lead processor and an append
     * processor.
     *
     * @param leadProcessor A {@link DatumProcessor} which will act as the lead
     * processor.
     * @param appendProcessor A {@link DatumProcessor} which should only return
     * a single output record which will be appended to each record produced by
     * the lead processor.
     */
    protected AppendDatumProcessor(final DatumProcessor<T, U> leadProcessor, final DatumProcessor<T, U> appendProcessor) {
        this.leadProcessor = leadProcessor;
        this.appendProcessor = appendProcessor;
    }

    @Override
    public void process(final U parameters, final T input, final RecordStore output) throws ProcessingException {
        final HookRecordStore hookOutput = new HookRecordStore(output, recordStore -> {
            try {
                appendProcessor.process(parameters, input, recordStore);
            } catch (final ProcessingException ex) {
                LOGGER.log(Level.WARNING, "Error encountered during processing of appending processor");
            }
        });
        leadProcessor.process(parameters, input, hookOutput);
    }
}
