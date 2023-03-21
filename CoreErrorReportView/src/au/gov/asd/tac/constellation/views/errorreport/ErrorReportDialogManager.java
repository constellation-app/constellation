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
 * maintain a session variable with active dialogs that are shown
 *
 * @author OrionsGuardian
 */
public class ErrorReportDialogManager {

    private int popupDisplayMode = 1;
    private List<Double> activePopupIds = new ArrayList<>();
    private Date latestPopupDismissDate = null;

    private static ErrorReportDialogManager instance = null;

    public static ErrorReportDialogManager getInstance() {
        if (instance == null) {
            instance = new ErrorReportDialogManager();
        }
        return instance;
    }

    public void setLatestDismissDate(final Date dismissDate) {
        latestPopupDismissDate = dismissDate;
    }

    public void updatePopupMode(final int popupMode) {
        popupDisplayMode = popupMode;
    }

    public void showErrorDialog(final ErrorReportEntry entry) {
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
        showDialog(entry);
    }

    public void showDialog(final ErrorReportEntry entry) {
        Platform.runLater(() -> {
            entry.setLastPopupDate(new Date());
            ErrorReportDialog ced = new ErrorReportDialog(entry);
            ced.showDialog("Unexpected Exception Occurred ...");
        });
    }

    public void removeActivePopupId(final Double id) {
        activePopupIds.remove(id);
    }

    public Date getLatestPopupDismissDate() {
        return latestPopupDismissDate;
    }

    public void setLatestPopupDismissDate(final Date latestPopupDismissDate) {
        this.latestPopupDismissDate = latestPopupDismissDate;
    }

}
