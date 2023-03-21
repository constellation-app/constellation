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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author OrionsGuardian
 */
public class ErrorReportSessionData {

    final private List<ErrorReportEntry> sessionErrors = new ArrayList<>();
    final private List<ErrorReportEntry> displayedErrors = new ArrayList<>();

    private static ErrorReportSessionData instance = null;
    private static double entryId = 0;
    public static Date lastUpdate = new Date();

    public static boolean screenUpdateRequested = false;

    ErrorReportSessionData() {
    }

    public static ErrorReportSessionData getInstance() {
        if (instance == null) {
            instance = new ErrorReportSessionData();
        }
        return instance;
    }

    public boolean storeSessionError(final ErrorReportEntry entry) {
        boolean foundMatch = false;
        synchronized (sessionErrors) {
            final String comparisonData = entry.getErrorData();
            final int errCount = sessionErrors.size();
            for (int i = 0; i < errCount; i++) {
                if (sessionErrors.get(i).getErrorData().equals(comparisonData)) {
                    final ErrorReportEntry ere = sessionErrors.get(i);
                    ere.incrementOccurrences();
                    ere.setExpanded(true);
                    ere.setHeading(entry.getHeading());
                    foundMatch = true;
                    if (i > 0) {
                        sessionErrors.remove(i);
                        sessionErrors.add(0, ere); // move updated entry to the top
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
            lastUpdate = new Date();
        }
    }

    public List<ErrorReportEntry> getSessionErrorsCopy() {
        final List<ErrorReportEntry> returnData = new ArrayList<>();
        for (ErrorReportEntry entry : sessionErrors) {
            returnData.add(entry.copy());
        }
        return returnData;
    }

    public List<ErrorReportEntry> refreshDisplayedErrors() {
        // take a snapshot of sessionErrors to use in the display screen
        final List<ErrorReportEntry> refreshedData = new ArrayList<>();
        synchronized (sessionErrors) {
            synchronized (displayedErrors) {
                for (ErrorReportEntry entry : sessionErrors) {
                    ErrorReportEntry refreshedEntry = entry.copy();
                    ErrorReportEntry displayedEntry = findDisplayedEntryWithId(entry.getEntryId());
                    if (displayedEntry != null) {
                        refreshedEntry.setExpanded(displayedEntry.getExpanded());
                        refreshedEntry.setBlockRepeatedPopups(displayedEntry.isBlockRepeatedPopups());
                        if (displayedEntry.getLastPopupDate() != null) {
                            refreshedEntry.setLastPopupDate(new Date(displayedEntry.getLastPopupDate().getTime()));
                        }
                    }
                    refreshedData.add(refreshedEntry);
                }
                displayedErrors.clear();
                displayedErrors.addAll(refreshedData);
            }
        }
        return displayedErrors;
    }

    public ErrorReportEntry findDisplayedEntryWithId(final double id) {
        for (ErrorReportEntry activeEntry : displayedErrors) {
            if (activeEntry.getEntryId() == id) {
                return activeEntry;
            }
        }
        return null;
    }

    public void requestScreenUpdate(final boolean updateRequested) {
        screenUpdateRequested = updateRequested;
    }

    public void updateDisplayedEntryScreenSettings(final double entryId, final Date lastPopupDate, final Boolean blockPopups, final Boolean expanded) {
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
            }
        }
    }

}
