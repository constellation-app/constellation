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
package au.gov.asd.tac.constellation.views.scatterplot;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Generic error dialog for use with the Scatter Plot.
 *
 * @author cygnus_x-1
 */
public class ScatterPlotErrorDialog {

    /**
     * Create an error dialog with a custom message.
     *
     * @param message the message to display in the dialog.
     */
    public static void create(final String message) {
        final NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }
}
