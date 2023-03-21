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
    
    private static double entryId = 0;
    
    @Override
    public void publish(final LogRecord record) {
        if (record != null && record.getThrown() != null) {
            final StackTraceElement[] elems = record.getThrown().getStackTrace();
            final StringBuilder errorMsg = new StringBuilder();
            String recordHeader = record.getThrown().getLocalizedMessage() != null ? record.getThrown().getLocalizedMessage() : "<< No Message >>";
            if (!recordHeader.endsWith("\n")) {
                recordHeader += "\n";
            }
            String summary = record.getThrown().toString();
            if (!summary.endsWith("\n")) {
                summary += "\n";
            }
            if (elems == null || elems.length == 0) {
                errorMsg.append(" >> No stacktrace available for error:\n >> ").append(recordHeader);
            } else {
                for (int i=0; i<elems.length; i++){
                    errorMsg.append(elems[i].toString()).append("\n");
                }
            }
            final ErrorReportEntry rep4 = new ErrorReportEntry(recordHeader, summary, errorMsg.toString(), entryId++);
            ErrorReportSessionData.getInstance().storeSessionError(rep4);            
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
}
