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
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportEntry;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportSessionData;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.openide.util.lookup.ServiceProvider;

/**
 * Custom Error Manager to replace the Netbeans Error Manager
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
            final Level errLevel = errorRecord.getLevel();
            final String errorSummary = errorRecord.getThrown().toString();
            final int firstColon = errorSummary.indexOf(SeparatorConstants.COLON);
            String extractedMessage = firstColon != -1 ? errorSummary.substring(firstColon + 2) : "";
            final boolean autoBlockPopup = extractedMessage.startsWith(NotifyDisplayer.BLOCK_POPUP_FLAG);
            if (autoBlockPopup) {
                extractedMessage = extractedMessage.substring(NotifyDisplayer.BLOCK_POPUP_FLAG.length());
            }
            final int prevDotPos = errorSummary.substring(0, (firstColon != -1 ? firstColon : errorSummary.length())).lastIndexOf(SeparatorConstants.PERIOD);
            final String exceptionType = errorSummary.substring(prevDotPos + 1, (firstColon != -1 ? firstColon : errorSummary.length()));
            String recordHeader = extractedMessage.isEmpty() ? exceptionType : extractedMessage;
            String revisedSummary = errorSummary.substring(0, firstColon + 1) 
                    + errorSummary.substring(firstColon + 1 + (autoBlockPopup ? NotifyDisplayer.BLOCK_POPUP_FLAG.length() : 0));
            if (!revisedSummary.endsWith(SeparatorConstants.NEWLINE)) {
                revisedSummary += SeparatorConstants.NEWLINE;
            }
            if (elems == null || elems.length == 0) {
                errorMsg.append(" >> No stacktrace available for error:")
                        .append(SeparatorConstants.NEWLINE).append(" >> ")
                        .append(recordHeader);
            } else {
                for (int i = 0; i < elems.length; i++) {
                    errorMsg.append(elems[i].toString())
                            .append(SeparatorConstants.NEWLINE);
                }
            }
            if (!recordHeader.endsWith(SeparatorConstants.NEWLINE)) {
                recordHeader += SeparatorConstants.NEWLINE;
            }

            final ErrorReportEntry repEntry = new ErrorReportEntry( errLevel, recordHeader, 
                                                                    revisedSummary, errorMsg.toString(), 
                                                                    ErrorReportSessionData.getNextEntryId());
            if (autoBlockPopup) {
                repEntry.setBlockRepeatedPopups(true);
                repEntry.setLastPopupDate(new Date());
            }
            ErrorReportSessionData.getInstance().storeSessionError(repEntry);
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
