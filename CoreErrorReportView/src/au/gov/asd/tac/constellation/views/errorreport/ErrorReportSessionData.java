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

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * This is a data storage class to maintain the list of errors that have been
 * generated, and the list of errors that have been displayed in the Error
 * Report View
 *
 * @author OrionsGuardian
 */
public class ErrorReportSessionData {
    
    private final List<ErrorReportEntry> sessionErrors = new ArrayList<>();
    private final List<ErrorReportEntry> displayedErrors = new ArrayList<>();

    private static ErrorReportSessionData instance = null;
    private static Double nextEntryId = 0D;
    private static Object accessControl = new Object();
    private static Date lastUpdate = new Date();

    private static boolean screenUpdateRequested = false;

    public static ErrorReportSessionData getInstance() {
        if (instance == null) {
            instance = new ErrorReportSessionData();
            ErrorReportDialogManager.getInstance();
        }
        return instance;
    }

    /**
     * process an incoming error message and either add a new entry or increment
     * the occurrences of an existing one.
     *
     * @param entry
     * @return
     */
    public boolean storeSessionError(final ErrorReportEntry entry) {
        boolean foundMatch = false;
        synchronized (sessionErrors) {
            // check for a repeated exception from the same constellation source, but compare only the top portion
            // of the stacktrace, as there are cases where different threads can generate the same exception,
            // but have a different origin (thread number) in their stack trace
            final String[] comparisonLines = entry.getErrorData().split(SeparatorConstants.NEWLINE);
            final StringBuilder sb = new StringBuilder();
            final String[] constyLines = extractMatchingLines(comparisonLines, ".constellation.");
            if (comparisonLines.length > 7) {
                final int topNlines = 2 + comparisonLines.length / 4;
                for (int i = 0; i < topNlines; i++) {
                    sb.append(comparisonLines[i]).append(SeparatorConstants.NEWLINE);
                }
            } else {
                sb.append(entry.getErrorData());
            }
            final String comparisonData = sb.toString(); 
            final int errCount = sessionErrors.size();
            for (int i = 0; i < errCount; i++) {
                boolean allConstyLinesMatch = true;
                for (final String constyLine : constyLines) {
                    if (!sessionErrors.get(i).getErrorData().contains(constyLine)) {
                        allConstyLinesMatch = false;
                        break;
                    }
                }
                if (allConstyLinesMatch && sessionErrors.get(i).getErrorLevel().equals(entry.getErrorLevel()) && sessionErrors.get(i).getErrorData().startsWith(comparisonData)) {
                    final ErrorReportEntry ere = sessionErrors.get(i);
                    ere.incrementOccurrences();
                    ere.setExpanded(true);
                    ere.setHeading(entry.getHeading());
                    ere.setSummaryHeading(entry.getSummaryHeading());
                    foundMatch = true;
                    if (i > 0) {
                        // move updated entry to the top
                        sessionErrors.remove(i); // NOSONAR
                        sessionErrors.add(0, ere);
                    }
                }
            }
            if (!foundMatch) {
                // add new entry to the top
                sessionErrors.add(0, entry);
            }
            lastUpdate = new Date();
        }
        return !foundMatch;
    }

    private String[] extractMatchingLines(final String[] sourceLines, final String patternToMatch) {
        final StringBuilder destination = new StringBuilder();
        for (int i = 0; i< sourceLines.length; i++) {
            if (sourceLines[i].contains(patternToMatch)) {
                destination.append(sourceLines[i]).append(SeparatorConstants.NEWLINE);
            }
        }
        return destination.toString().split(SeparatorConstants.NEWLINE);
    }
    
    public void removeEntry(final double entryId) {
        synchronized (sessionErrors) {
            final int errorCount = sessionErrors.size();
            int foundPos = -1;
            for (int i = 0; i < errorCount && foundPos == -1; i++) {
                if (sessionErrors.get(i).getEntryId() == entryId) {
                    foundPos = i;
                }
            }
            if (foundPos > -1) {
                sessionErrors.remove(foundPos);
            }
            screenUpdateRequested = true;
        }
    }

    /**
     * Take as snapshot of sessionErrors to use in the display screen
     * limited to entries that match on one of the filters
     * 
     * @param filters
     * @return
     */
    public List<ErrorReportEntry> refreshDisplayedErrors(final Collection<String> filters) {
        final List<ErrorReportEntry> refreshedData = new ArrayList<>();
        synchronized (sessionErrors) {
            synchronized (displayedErrors) {
                for (final ErrorReportEntry entry : sessionErrors) {
                    if (filters.contains(entry.getErrorLevel().getName())) {
                        final ErrorReportEntry refreshedEntry = entry.copy();
                        final ErrorReportEntry displayedEntry = findDisplayedEntryWithId(entry.getEntryId());
                        if (displayedEntry != null) {
                            refreshedEntry.setExpanded(displayedEntry.isExpanded());
                            refreshedEntry.setBlockRepeatedPopups(displayedEntry.isBlockRepeatedPopups());
                            refreshedEntry.setDialog(displayedEntry.getDialog());
                            entry.setExpanded(displayedEntry.isExpanded());
                            entry.setBlockRepeatedPopups(displayedEntry.isBlockRepeatedPopups());
                            entry.setDialog(displayedEntry.getDialog());
                            if (displayedEntry.getLastPopupDate() != null) {
                                refreshedEntry.setLastPopupDate(new Date(displayedEntry.getLastPopupDate().getTime()));
                                entry.setLastPopupDate(new Date(displayedEntry.getLastPopupDate().getTime()));
                            }
                        }
                        refreshedData.add(refreshedEntry);
                    }
                }
                displayedErrors.clear();
                displayedErrors.addAll(refreshedData);
            }
        }
        final List<ErrorReportEntry> returnDisplayedErrors = new ArrayList<>();
        displayedErrors.forEach(entry -> returnDisplayedErrors.add(entry.copy()));
        return returnDisplayedErrors;
    }

    public ErrorReportEntry findDisplayedEntryWithId(final double id) {
        for (final ErrorReportEntry activeEntry : displayedErrors) {
            if (activeEntry.getEntryId() == id) {
                return activeEntry;
            }
        }
        return null;
    }

    public static void requestScreenUpdate(final boolean updateRequested) {
        screenUpdateRequested = updateRequested;
    }

    public static boolean isScreenUpdateRequested(){
        return screenUpdateRequested;
    }
    
    public static Date getLastUpdate() {
        if (lastUpdate == null) {
            return null;
        }
        return new Date(lastUpdate.getTime());
    }
    
    public static void setLastUpdate(final Date updateDate) {
        if (updateDate == null) {
            lastUpdate = null;
        } else {
            lastUpdate = new Date(updateDate.getTime());
        }
    }
    
    /**
     * For a specified entry id, this sets the lastPopupDate, blockPopups flag,
     * and if the control is in expanded form.
     *
     * @param entryId
     * @param lastPopupDate
     * @param blockPopups
     * @param expanded
     */
    public void updateDisplayedEntryScreenSettings(final double entryId, final Date lastPopupDate, final Boolean blockPopups, final Boolean expanded, final ErrorReportDialog errorDialog) {
        synchronized (displayedErrors) {
            final ErrorReportEntry displayedEntry = findDisplayedEntryWithId(entryId);
            if (displayedEntry != null) {
                if (blockPopups != null) {
                    displayedEntry.setBlockRepeatedPopups(blockPopups);
                }
                if (lastPopupDate != null) {
                    displayedEntry.setLastPopupDate(lastPopupDate);
                }
                if (expanded != null) {
                    displayedEntry.setExpanded(expanded);
                }
                displayedEntry.setDialog(errorDialog);
            }
        }
    }

    public static double getNextEntryId() {
        double returnVal = -1;
        synchronized (accessControl) {
            returnVal = nextEntryId;
            nextEntryId++;
        }
        return returnVal;
    }

}
