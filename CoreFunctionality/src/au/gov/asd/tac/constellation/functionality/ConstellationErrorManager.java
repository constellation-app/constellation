/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.views.errorreport.ErrorReportEntry;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportSessionData;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author OrionsGuardian
 */
@ServiceProvider(service = Handler.class, supersedes = "org.netbeans.core.NbErrorManager")
public class ConstellationErrorManager extends Handler {

    @Override
    public void publish(final LogRecord errorRecord) {
        if (errorRecord != null && errorRecord.getThrown() != null) {
            final StackTraceElement[] elems = errorRecord.getThrown().getStackTrace();
            final StringBuilder errorMsg = new StringBuilder();
            String recordHeader = errorRecord.getThrown().getLocalizedMessage() != null ? errorRecord.getThrown().getLocalizedMessage() : "<< No Message >>";
            if (!recordHeader.endsWith("\n")) {
                recordHeader += "\n";
            }
            String errorSummary = errorRecord.getThrown().toString();
            if (!errorSummary.endsWith("\n")) {
                errorSummary += "\n";
            }
            if (elems == null || elems.length == 0) {
                errorMsg.append(" >> No stacktrace available for error:\n >> ").append(recordHeader);
            } else {
                for (int i = 0; i < elems.length; i++) {
                    errorMsg.append(elems[i].toString()).append("\n");
                }
            }
            final ErrorReportEntry rep4 = new ErrorReportEntry(recordHeader, errorSummary, errorMsg.toString(), ErrorReportSessionData.getNextEntryId());
            ErrorReportSessionData.getInstance().storeSessionError(rep4);
        }
    }

    @Override
    public void flush() {
        // no buffered data blocks to output
    }

    @Override
    public void close() throws SecurityException {
        // no persistent data objects to clear
    }

}
