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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.PrivilegedActionException;

/**
 * Custom Error Manager to replace the Netbeans Error Manager
 * 
 * @author OrionsGuardian
 */
@ServiceProvider(service = Handler.class, supersedes = "org.netbeans.core.NbErrorManager")
public class ConstellationErrorManager extends Handler {

    
    public void addToErrorReport(final LogRecord errorRecord) {
        if (errorRecord != null && errorRecord.getThrown() != null) {
            final StackTraceElement[] elems = errorRecord.getThrown().getStackTrace();
            final StringBuilder errorMsg = new StringBuilder("");
            final Level errLevel = errorRecord.getLevel();
            final String errorSummary = errorRecord.getThrown().toString();
            int messageColon = errorSummary.lastIndexOf("Exception:") + 9;
            if (messageColon == -1) {
                messageColon = errorSummary.lastIndexOf("\n") - 1;
            }
            if (messageColon < 0 && !errorSummary.isEmpty()) {
                messageColon = 0;
            }
            String extractedMessage = messageColon != -1 ? errorSummary.substring(messageColon + 2) : "";
            final boolean autoBlockPopup = extractedMessage.startsWith(NotifyDisplayer.BLOCK_POPUP_FLAG);
            if (autoBlockPopup) {
                extractedMessage = extractedMessage.substring(NotifyDisplayer.BLOCK_POPUP_FLAG.length());
            }
            final int prevDotPos = errorSummary.substring(0, (messageColon != -1 ? messageColon : errorSummary.length())).lastIndexOf(SeparatorConstants.PERIOD);
            final String exceptionType = errorSummary.substring(prevDotPos + 1, (messageColon != -1 ? messageColon : errorSummary.length()));
            String recordHeader = extractedMessage.isEmpty() ? exceptionType : extractedMessage;
            String revisedSummary = errorSummary.substring(0, messageColon + 1) 
                    + errorSummary.substring(messageColon + 1 + (autoBlockPopup ? NotifyDisplayer.BLOCK_POPUP_FLAG.length() : 0));
            if (!revisedSummary.endsWith(SeparatorConstants.NEWLINE)) {
                revisedSummary += SeparatorConstants.NEWLINE;
            }
            if (elems == null || elems.length == 0) {
                errorMsg.append(" >> No top-level stacktrace available for error:")
                        .append(SeparatorConstants.NEWLINE).append(" >> ")
                        .append(recordHeader);
            } else {
                appendStackTrace(errorMsg, errorSummary, errorRecord.getThrown(), "");
            }
            appendCause(errorMsg, errorSummary, errorRecord.getThrown(), 0);
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

    private void appendCause(final StringBuilder sbErrors, final String initialMessage, final Throwable ef, final int depth){
        if (ef == null || depth > 20) {
            return;
        }
        appendWrappedException(sbErrors, initialMessage, ef, depth);
        final Throwable segmentCause = ef.getCause();
        if (segmentCause != null) {
            appendStackTrace(sbErrors, initialMessage, segmentCause, "  Caused By:\n");
            appendCause(sbErrors, initialMessage, segmentCause, (depth + 1));
        }
    }

    private void appendWrappedException(final StringBuilder sbErrors, final String initialMessage, final Throwable ef, final int depth) {
        if (ef instanceof InvocationTargetException ite) {
            appendStackTrace(sbErrors, initialMessage, ite.getTargetException(), "    InvocationTargetException:\n  Caused By:\n");
            appendCause(sbErrors, initialMessage, ite.getTargetException(), (depth + 1));
        } else if (ef instanceof UndeclaredThrowableException ute) {
            appendStackTrace(sbErrors, initialMessage, ute.getUndeclaredThrowable(), "    UndeclaredThrowableException:\n  Caused By:\n");
            appendCause(sbErrors, initialMessage, ute.getUndeclaredThrowable(), (depth + 1));
        } else if (ef instanceof ExceptionInInitializerError eiie) {
            appendStackTrace(sbErrors, initialMessage, eiie.getException(), "    ExceptionInInitializerError:\n  Caused By:\n");
            appendCause(sbErrors, initialMessage, eiie.getException(), (depth + 1));
        } else if (ef instanceof PrivilegedActionException pae) {
            appendStackTrace(sbErrors, initialMessage, pae.getException(), "    PrivilegedActionException:\n  Caused By:\n");
            appendCause(sbErrors, initialMessage, pae.getException(), (depth + 1));
        }
    }
    
    private void appendStackTrace(final StringBuilder sbErrors, final String initialMessage, final Throwable ef, final String hierarchyMessage) {
        final StringBuilder currentMessage = new StringBuilder("");
        boolean addedDescription = false;
        currentMessage.append(hierarchyMessage)
                .append(ef.toString())
                .append(SeparatorConstants.NEWLINE);
        if (!sbErrors.toString().contains(currentMessage.toString()) && !initialMessage.trim().contains(currentMessage.toString().trim())) {
            addedDescription = true;
            sbErrors.append(currentMessage.append(SeparatorConstants.NEWLINE).toString());
        }
        
        final StackTraceElement[] segmentElems = ef.getStackTrace();
        if (segmentElems != null && segmentElems.length > 0) {
            StringBuilder subStackTrace = new StringBuilder("");
            for (int i = 0; i < segmentElems.length; i++) {
                subStackTrace.append(segmentElems[i].toString())
                        .append(SeparatorConstants.NEWLINE);
            }
            if (!sbErrors.toString().contains(subStackTrace.toString())) {
                if (!addedDescription) {
                    sbErrors.append(hierarchyMessage)
                            .append(SeparatorConstants.NEWLINE);
                }
                sbErrors.append(subStackTrace.append(SeparatorConstants.NEWLINE).toString());
            }
        }
    }
    
    @Override
    public boolean isLoggable(final LogRecord logRec) {
        final boolean firstCheck = super.isLoggable(logRec);
        if (firstCheck && logRec.getThrown() != null) {
            return false;
        }
        return firstCheck;
    }
    
    @Override
    public void flush() {
        // no buffered data blocks to output
    }

    @Override
    public void close() throws SecurityException {
        // no persistent data objects to clear
    }

    @Override
    public void publish(final LogRecord logRec) {
        addToErrorReport(logRec);
    }
}
