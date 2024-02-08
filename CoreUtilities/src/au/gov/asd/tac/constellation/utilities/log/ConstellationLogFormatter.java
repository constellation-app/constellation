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
package au.gov.asd.tac.constellation.utilities.log;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Guilty-Spark-343
 */
public class ConstellationLogFormatter extends Formatter {
    
    private String logMessage = "";
    
    @Override
    public final String format(final LogRecord record) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(LocalDateTime.now().toString().substring(0,22).replace("T"," "));
        sb.append("Z] - ");
        sb.append(record.getLevel());
        sb.append(" [");
        sb.append(record.getLoggerName());
        sb.append("]: ");
        
        final String formattedMessage;

        if (record.getMessage() != null && record.getParameters() != null) {
            formattedMessage = MessageFormat.format(record.getMessage(), record.getParameters());
        } else {
            formattedMessage = record.getMessage();
        }
        if(formattedMessage == null ? logMessage == null : formattedMessage.equals(logMessage)){
            sb.append("Last record repeated again.");
            return sb.toString();
        }
        final Throwable error = record.getThrown();
        if (error != null && StringUtils.isNotBlank(error.getMessage())) {
            sb.append(error.toString());
            sb.append(SeparatorConstants.NEWLINE);
            sb.append(SeparatorConstants.NEWLINE);

            final String stackTrace = Arrays.stream(error.getStackTrace())
                    .map(Objects::toString)
                    .collect(Collectors.joining(SeparatorConstants.NEWLINE));

            sb.append(stackTrace);
            sb.append(SeparatorConstants.NEWLINE);
        }else{
            sb.append(formattedMessage);
            sb.append(SeparatorConstants.NEWLINE);
        }
        logMessage = formattedMessage;
        return sb.toString();
    }
}
