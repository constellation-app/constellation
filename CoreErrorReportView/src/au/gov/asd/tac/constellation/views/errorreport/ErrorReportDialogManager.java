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
import javafx.application.Platform;

/**
 * Maintain a session variable with active dialogs that are shown
 *
 * @author OrionsGuardian
 */
public class ErrorReportDialogManager {

    private int popupDisplayMode = 1;
    private final List<Double> activePopupIds = new ArrayList<>();
    private Date latestPopupDismissDate = null;

    private static ErrorReportDialogManager instance = null;

    public static ErrorReportDialogManager getInstance() {
        if (instance == null) {
            instance = new ErrorReportDialogManager();
        }
        return instance;
    }

    public void updatePopupMode(final int popupMode) {
        popupDisplayMode = popupMode;
    }

    public void showErrorDialog(final ErrorReportEntry entry) {
        if (activePopupIds.size() >= 10) {
            // too many different errors at the same time
            return;
        }
        Date currentDate = new Date();
        Date latestDismissDate = ErrorReportDialogManager.getInstance().getLatestPopupDismissDate();
        Date graceIntervalDate = latestDismissDate == null ? new Date() : new Date(latestDismissDate.getTime() + 5000);
        if (currentDate.before(graceIntervalDate)) {
            // prevent popups for 5 seconds ... just in case of an infinite cycle of popups
            // this allows some time to set the popup mode to 0 (disabling any more popups)
            return;
        }

        if (entry.isBlockRepeatedPopups() || popupDisplayMode == 0 || (latestPopupDismissDate != null && entry.getLastDate().before(latestPopupDismissDate))) {
            return; // mode 0 = Never                    
        }
        if (popupDisplayMode == 1) {
            // check if there are any current popups being displayed or if this entry has been displayed
            if (activePopupIds.size() > 0 || entry.getLastPopupDate() != null) {
                return;
            }
        } else if (popupDisplayMode == 2) {
            // only need to check if something is being displayed
            if (activePopupIds.size() > 0) {
                return;
            }
        } else if (popupDisplayMode == 3) {
            // only need to check if the entry has already been displayed
            if (entry.getLastPopupDate() != null) {
                return;
            }
        } else if (popupDisplayMode == 4) {
            // only need to check if the entry is currently being displayed
            if (activePopupIds.contains(entry.getEntryId())) {
                return;
            }
        }
        // display the popup
        activePopupIds.add(entry.getEntryId());
        showDialog(entry, false);
    }

    /**
     * Create and show an Error Report Dialog for the supplied error report entry
     * 
     * @param entry 
     * @param review 
     */
    public void showDialog(final ErrorReportEntry entry, boolean review) {
        Platform.runLater(() -> {
            entry.setLastPopupDate(new Date());
            final ErrorReportDialog errorDlg = new ErrorReportDialog(entry);
            errorDlg.showDialog((review ? "Reviewing: " : "") + "Unexpected Exception ..." + (entry.getOccurrences() > 1 ? " [" + entry.getOccurrences() + " Repeated Occurrences]" : ""));
        });
    }

    /**
     * Remove entry from active popup list
     * @param id 
     */
    public void removeActivePopupId(final Double id) {
        activePopupIds.remove(id);
    }

    public Date getLatestPopupDismissDate() {
        return latestPopupDismissDate;
    }

    /**
     * set the date/time at which the latest popup was closed.
     * @param latestPopupDismissDate 
     */
    public void setLatestPopupDismissDate(final Date latestPopupDismissDate) {
        this.latestPopupDismissDate = latestPopupDismissDate;
    }

    public ArrayList<String> getActivePopupErrorLevels(){
        final ArrayList<String> resultList = new ArrayList<>();
        for (final Double id : activePopupIds) {
            final String errorLevel = ErrorReportSessionData.getInstance().findDisplayedEntryWithId(id).getErrorLevel().getName();
            resultList.add(errorLevel);
        }
        return resultList;
    }
}
