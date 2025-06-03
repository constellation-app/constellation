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
package au.gov.asd.tac.constellation.views.errorreport;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a data class to store the exception stack-trace and maintain a count
 * of occurrences
 *
 * @author OrionsGuardian
 */
public class ErrorReportEntry {

    private Level errorLevel = null;
    private String errorData = null;
    private String heading = null;
    private int occurrences = 0;
    private Date lastDate = null;
    private boolean expanded = true;
    private double entryId = -1;
    private Date lastPopupDate = null;
    private boolean preventRepeatedPopups = false;
    private String summaryHeading = null;
    private ErrorReportDialog dialog = null;

    public ErrorReportEntry(final Level errLevel, final String errorHeading, final String summary, final String errorMessage, final double id) {
        errorLevel = errLevel;
        heading = errorHeading;
        summaryHeading = summary;
        errorData = errorMessage;
        entryId = id;
        lastDate = new Date();
        occurrences = 1;
    }

    public ErrorReportEntry copy() {
        final ErrorReportEntry dataCopy = new ErrorReportEntry(errorLevel, heading, summaryHeading, errorData, entryId);
        dataCopy.expanded = expanded;
        dataCopy.lastDate = new Date(lastDate.getTime());
        dataCopy.lastPopupDate = (lastPopupDate == null ? null : new Date(lastPopupDate.getTime()));
        dataCopy.occurrences = occurrences;
        dataCopy.preventRepeatedPopups = preventRepeatedPopups;
        dataCopy.dialog = dialog;
        return dataCopy;
    }

    public String getTrimmedHeading(final int length) {
        if (heading == null) {
            return "-NULL-";
        }

        String adjustedHeading = heading;
        if (adjustedHeading.endsWith(SeparatorConstants.NEWLINE)) {
            adjustedHeading = adjustedHeading.substring(0, adjustedHeading.lastIndexOf(SeparatorConstants.NEWLINE));
        }
        adjustedHeading = adjustedHeading.replace(SeparatorConstants.NEWLINE, " . ") + SeparatorConstants.NEWLINE;
        
        if (adjustedHeading.length() < length) {
            return adjustedHeading;
        }
        if (length <= 4) {
            return adjustedHeading.substring(0, 4);
        }
        return adjustedHeading.substring(0, length - 4) + " ...";
    }

    public String getHeading() {
        if (heading == null) {
            return "-NULL-";
        }
        return heading;
    }

    public void setHeading(final String errorHeading) {
        heading = errorHeading;
    }

    public String getSummaryHeading() {
        return summaryHeading;
    }

    public void setSummaryHeading(final String revisedSummary) {
        summaryHeading = revisedSummary;
    }
    
    public void incrementOccurrences() {
        occurrences++;
        lastDate = new Date();
    }

    public String getErrorData() {
        return errorData;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public Date getLastDate() {
        if (lastDate == null) {
            return null;
        }
        return new Date(lastDate.getTime());
    }

    public String getTimeText() {
        final SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm:ss ");
        return sdf.format(lastDate);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public double getEntryId() {
        return entryId;
    }

    public ErrorReportDialog getDialog() {
        return dialog;
    }
    
    public void setDialog(final ErrorReportDialog errorDialog) {
        dialog = errorDialog;
    }
    
    public void setEntryId(final double id) {
        entryId = id;
    }

    public void setExpanded(final boolean expandedState) {
        expanded = expandedState;
    }

    public Date getLastPopupDate() {
        if (lastPopupDate == null) {
            return null;
        }
        return new Date(lastPopupDate.getTime());
    }

    public void setLastPopupDate(final Date popupDate) {
        if (popupDate == null) {
            lastPopupDate = null;
        } else {
            lastPopupDate = new Date(popupDate.getTime());
        }
    }

    public boolean isBlockRepeatedPopups() {
        return preventRepeatedPopups;
    }

    public void setBlockRepeatedPopups(final boolean blockRepeatedPopups) {
        preventRepeatedPopups = blockRepeatedPopups;
    }

    public Level getErrorLevel(){
        return errorLevel;
    }
    
    @Override
    public String toString() {
        return "[ErrorReportEntry:[id=" + getEntryId() + "]"
                + ", [header=" + getHeading() + "]"
                + ", [summaryHeading=" + getSummaryHeading() + "]"
                + ", [errorLevel=" + getErrorLevel().getName() + "]"
                + ", [errorData_length=" + getErrorData().length() + "]"
                + ", [occurrences=" + getOccurrences() + "]"
                + ", [lastDate=" + getLastDate() + "]"
                + ", [lastPopupDate=" + getLastPopupDate() + "]"
                + ", [preventRepeatedPopups=" + isBlockRepeatedPopups() + "]"
                + ", [expanded=" + isExpanded() + "]"
                + ", [dialog=" + getDialog() + "]"
                + "]";
    }
}
