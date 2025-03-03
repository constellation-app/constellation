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
package au.gov.asd.tac.constellation.utilities.log;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

/**
 * All Constellation log messages will come through this class for formatting.
 * They will have a fixed format: 
 * [date_of_log_entry] log.LEVEL [class_which_generated_the_log]: log message 
 *   Exception stack trace (if present)
 *
 * @author Guilty-Spark-343
 * @author OrionsGuardian
 */
public class ConstellationLogFormatter extends Formatter {

    // Store a copy of the previous log message, so we can check for repeats.
    private String repeatedLogMessage = "";
    private String repeatedExceptionBlock = "";
    private boolean repeatedException = false;
    private Integer repeatedLogCount = 0;
    
    // Pending message is continually updated with a potential final message when repeat messages occur, 
    // but will only be logged when the new message is not a repeat of the previous message.
    private String pendingMessage = "";

    @Override
    public final String format(final LogRecord logRecord) {
        final Throwable error = logRecord.getThrown();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final StringBuilder prefix = new StringBuilder();
        final String logDate = "[" + sdf.format(new Date(logRecord.getMillis())) + "Z] ";
        final StringBuilder exceptionBlock = new StringBuilder();

        prefix.append(logDate);
        prefix.append(logRecord.getLevel().getName());
        prefix.append(" [");
        prefix.append(logRecord.getLoggerName() != null ? logRecord.getLoggerName() : " ");
        prefix.append("]: ");

        final String formattedMessage;
        if (logRecord.getMessage() != null && logRecord.getParameters() != null) {
            formattedMessage = MessageFormat.format(logRecord.getMessage(), logRecord.getParameters()) + SeparatorConstants.NEWLINE;
        } else {
            formattedMessage = (logRecord.getMessage() != null ? logRecord.getMessage() : "") + SeparatorConstants.NEWLINE;
        }

        final StackTraceElement[] errTrace;
        if (error != null && error.getStackTrace() != null) {
            errTrace = error.getStackTrace();
        } else {
            errTrace = null;
        }
        repeatedException = false;
        if (errTrace != null) {
            exceptionBlock.append((logRecord.getMessage() != null ? logRecord.getMessage() : ""));
            exceptionBlock.append(SeparatorConstants.NEWLINE);
            exceptionBlock.append("  ");
            exceptionBlock.append(error.toString());
            exceptionBlock.append(SeparatorConstants.NEWLINE);

            final String stackTrace = Arrays.stream(errTrace)
                    .map(entry -> "    at " + entry.toString())
                    .collect(Collectors.joining(SeparatorConstants.NEWLINE));

            repeatedException = stackTrace.equals(repeatedExceptionBlock);
            if (!repeatedException) {
                repeatedExceptionBlock = stackTrace;
            }
            exceptionBlock.append(stackTrace);
            exceptionBlock.append(SeparatorConstants.NEWLINE);
        }
        if (!repeatedException && exceptionBlock.length() == 0) {
            repeatedExceptionBlock = "";
        }

        if (repeatedException || (!repeatedLogMessage.isBlank() && formattedMessage.equals(repeatedLogMessage))) {
            final StringBuilder repeatedResponse = new StringBuilder();
            repeatedResponse.append(prefix.toString());
            repeatedLogCount += 1;
            final String messagePrefix = repeatedException ? "Last exception record repeated " : "Last record repeated ";
            if (repeatedLogCount == 11) {
                repeatedResponse.append(messagePrefix).append("more than 10 times, further logs of this record are ignored until the log record changes.");
                repeatedResponse.append(SeparatorConstants.NEWLINE);
                pendingMessage = prefix.toString() + messagePrefix + repeatedLogCount + " times in total." + SeparatorConstants.NEWLINE;
            } else if (repeatedLogCount == 1) {
                repeatedResponse.append(messagePrefix).append("again.");
                repeatedResponse.append(SeparatorConstants.NEWLINE);
            } else if (repeatedLogCount > 11) {
                pendingMessage = prefix.toString() + messagePrefix + repeatedLogCount + " times in total." + SeparatorConstants.NEWLINE;
                return ""; // No log for this message.
            } else {
                pendingMessage = prefix.toString() + messagePrefix + repeatedLogCount + " more times." + SeparatorConstants.NEWLINE;
                return ""; // No log for this message.
            }
            return repeatedResponse.toString();
        } else {
            repeatedLogCount = 0;
        }

        repeatedLogMessage = exceptionBlock.length() > 0 ? exceptionBlock.toString() : formattedMessage;
        final String outputString = pendingMessage + prefix.toString() + repeatedLogMessage;
        pendingMessage = "";
        return outputString;
    }
}
