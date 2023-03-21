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
package au.gov.asd.tac.constellation.views.errorreport;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a data class to store the exception stack-trace and maintain a count
 * of occurrences
 *
 * @author OrionsGuardian
 */
public class ErrorReportEntry {

    private String errorData = null;
    private String heading = null;
    private int occurrences = 0;
    private Date lastDate = null;
    private boolean expanded = true;
    private double entryId = -1;
    private Date lastPopupDate = null;
    private boolean preventRepeatedPopups = false;
    private String summaryHeading = null;

    public ErrorReportEntry(final String errorHeading, final String summary, final String errorMessage, final double id) {
        heading = errorHeading;
        summaryHeading = summary;
        errorData = errorMessage;
        entryId = id;
        lastDate = new Date();
        occurrences = 1;
    }

    public ErrorReportEntry copy() {
        ErrorReportEntry dataCopy = new ErrorReportEntry(heading, summaryHeading, errorData, entryId);
        dataCopy.expanded = expanded;
        dataCopy.lastDate = new Date(lastDate.getTime());
        dataCopy.lastPopupDate = (lastPopupDate == null ? null : new Date(lastPopupDate.getTime()));
        dataCopy.occurrences = occurrences;
        return dataCopy;
    }

    public String getTrimmedHeading(final int length) {
        if (heading == null) {
            return "-NULL-";
        }
        if (heading.length() < length) {
            return heading;
        }
        if (length <= 4) {
            return heading.substring(0, 4);
        }
        return heading.substring(0, length - 4) + " ...";
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
        return lastDate;
    }

    public String getTimeText() {
        final SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm:ss ");
        return sdf.format(lastDate);
    }

    public boolean getExpanded() {
        return expanded;
    }

    public double getEntryId() {
        return entryId;
    }

    public void setEntryId(final double id) {
        entryId = id;
    }

    public void setExpanded(final boolean expandedState) {
        expanded = expandedState;
    }

    public Date getLastPopupDate() {
        return lastPopupDate;
    }

    public void setLastPopupDate(final Date popupDate) {
        lastPopupDate = popupDate;
    }

    public boolean isBlockRepeatedPopups() {
        return preventRepeatedPopups;
    }

    public void setBlockRepeatedPopups(final boolean blockRepeatedPopups) {
        preventRepeatedPopups = blockRepeatedPopups;
    }

    @Override
    public String toString() {
        return "[ErrorReportEntry:[id=" + entryId + "]"
                + ", [header=" + heading + "]"
                + ", [occurrences=" + occurrences + "]"
                + ", [lastDate=" + lastDate + "]"
                + ", [lastPopupDate=" + lastPopupDate + "]"
                + ", [preventRepeatedPopups=" + preventRepeatedPopups + "]"
                + "]";
    }
}
