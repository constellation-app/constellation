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
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;

/**
 * Maintain a session variable with active dialogs that are shown
 *
 * @author OrionsGuardian
 */
public class ErrorReportDialogManager {

    private int popupDisplayMode = 2;
    private final List<Double> activePopupIds = new ArrayList<>();
    private Date latestPopupDismissDate = null;
    private final ArrayList<String> popupTypeFilters = new ArrayList<>();
    private boolean isErrorReportRunning = false;
    private Timer refreshTimer = null;
    private Date latestRetrievalDate = null;
    private Date previousRetrievalDate = null;
    private Date backupRetrievalDate = null;
    private Date gracePeriodResumptionDate = null;
    private ArrayList<ErrorReportEntry> sessionPopupErrors = new ArrayList<>();

    private static ErrorReportDialogManager instance = null;
    
    public ErrorReportDialogManager(){
    }

    public static ErrorReportDialogManager getInstance() {
        if (instance == null) {
            instance = new ErrorReportDialogManager();
            instance.startDefaultProcessor();
        }
        return instance;
    }

    public void startDefaultProcessor(){
        popupTypeFilters.add("SEVERE");
        popupTypeFilters.add("WARNING");
        final TimerTask refreshAction = new TimerTask() {
            @Override
            public void run() {
                if (!isErrorReportRunning) {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            final Date currentDate = new Date();
                            if (gracePeriodResumptionDate != null && currentDate.before(gracePeriodResumptionDate)) {
                                return;
                            }
                            if (latestRetrievalDate == null || latestRetrievalDate.before(ErrorReportSessionData.lastUpdate)) {
                                backupRetrievalDate = previousRetrievalDate == null ? null : new Date(previousRetrievalDate.getTime());
                                previousRetrievalDate = latestRetrievalDate == null ? null : new Date(latestRetrievalDate.getTime());
                                latestRetrievalDate = currentDate;
                                sessionPopupErrors = ErrorReportSessionData.getInstance().refreshDisplayedErrors(popupTypeFilters);
                                for (final ErrorReportEntry reportEntry : sessionPopupErrors) {
                                    if (popupTypeFilters.contains(reportEntry.getErrorLevel().getName())) {
                                        if (previousRetrievalDate == null || reportEntry.getLastPopupDate() == null || reportEntry.getLastDate().after(previousRetrievalDate)) {
                                            showErrorDialog(reportEntry);
                                        }
                                    }
                                }

                            }
                        }
                    });
                }
            }
        };
        refreshTimer = new Timer();
        refreshTimer.schedule(refreshAction, 175, 475);        
    }
    
    public void updatePopupSettings(final int popupMode, ArrayList<String> popupFilters) {
        popupDisplayMode = popupMode;
        popupTypeFilters.clear();
        for (final String filterEntry : popupFilters) {
            popupTypeFilters.add(filterEntry);
        }
    }

    public void setErrorReportRunning(boolean isRunning){
        isErrorReportRunning = isRunning;
    }
    
    public void showErrorDialog(final ErrorReportEntry entry) {
        if (activePopupIds.size() >= 5) {
            // limit of 5 popups on screen at a time
            return;
        }
        final Date currentDate = new Date();
        gracePeriodResumptionDate = latestPopupDismissDate == null ? currentDate : new Date(latestPopupDismissDate.getTime() + 5000);
        if (currentDate.before(gracePeriodResumptionDate)) {
            // prevent popups for 5 seconds ... just in case of an infinite cycle of popups
            // this allows some time to set the popup mode to 0 (disabling any more popups)
            
            // We set the retrieval dates back, then retry them after the grace period.
            latestRetrievalDate = previousRetrievalDate != null ? new Date(previousRetrievalDate.getTime()) : null;
            previousRetrievalDate = backupRetrievalDate != null ? new Date(backupRetrievalDate.getTime()) : null;
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
            errorDlg.showDialog((review ? "Reviewing: " : "") + "Unexpected Exception ...");
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

    public Date getGracePeriodResumptionDate(){
        return gracePeriodResumptionDate;
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
