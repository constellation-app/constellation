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
package au.gov.asd.tac.constellation.plugins.algorithms;

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import org.openide.NotifyDescriptor;

/**
 * A RuntimeException for when underlying algorithms don't work.
 *
 * @author cygnus_x-1
 */
public class AlgorithmicError extends RuntimeException {

    public AlgorithmicError(final String message) {
        super(message);
    }

    /**
     * Create an error dialog with a custom message.
     *
     * @param message the error message
     */
    public static void createDialog(final String message) {
        NotifyDisplayer.display(message, NotifyDescriptor.WARNING_MESSAGE);
    }
}
