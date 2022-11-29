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
package au.gov.asd.tac.constellation.utilities.gui;

/**
 * An interface to provide a progress indicator.
 * <p>
 * We don't want to hard-code a UI dependent progress indicator. This interface
 * allows the caller to provide something appropriate.
 * <p>
 * The methods are suspiciously similar to some of those in ProgressHandle.
 *
 * @author algol
 */
public interface IoProgress {

    /**
     * Start the progress indication for indeterminate task.
     */
    void start();

    /**
     * Start the progress indication for a task with known number of steps.
     *
     * @param workunits Total number of workunits that will be processed.
     */
    void start(final int workunits);

    /**
     * Notify the user about completed workunits.
     *
     * @param workunit Completed workunits.
     */
    void progress(final int workunit);

    /**
     * Notify the user about progress by showing message with details.
     *
     * @param message A message to be displayed in the progress bar.
     */
    void progress(final String message);

    /**
     * Notify the user about completed workunits and show additional detailed
     * message.
     *
     * @param message The message to be displayed in the progress bar.
     * @param workunit Completed workunits.
     */
    void progress(final String message, final int workunit);

    /**
     * Finish the task, clean up the UI.
     */
    void finish();
}
