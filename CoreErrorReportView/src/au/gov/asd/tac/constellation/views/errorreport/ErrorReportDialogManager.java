/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.errorreport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import java.util.logging.Logger;

/**
 * maintain a session variable with active dialogs that are shown
 * 
 * @author OrionsGuardian
 */
public class ErrorReportDialogManager {
    
    private static final Logger LOGGER = Logger.getLogger(ErrorReportDialogManager.class.getName());
    
    int popupDisplayMode = 1;
    List<Double> activePopupIds = new ArrayList<>();
    private Date latestPopupDismissDate = null;
    
    private static ErrorReportDialogManager instance = null;
    
    public static ErrorReportDialogManager getInstance(){
        if (instance == null) {
            instance = new ErrorReportDialogManager();
        }
        return instance;
    }

    public void setLatestDismissDate(Date dismissDate){
        LOGGER.info("\n ==--==-- setting latest dismiss date : " + dismissDate);
        latestPopupDismissDate = dismissDate;
    }

    public void updatePopupMode(int popupMode) {
        popupDisplayMode = popupMode;
    }   
    
    public void showErrorDialog(ErrorReportEntry entry){
        LOGGER.info("\n\n ******** **** showErrorDialog:  mode=" + popupDisplayMode + " , activePopups=" + activePopupIds.size());
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
        
        LOGGER.info("\n -- DIALOG MANAGER popup counter : " + activePopupIds.size());
        
        showDialog(entry);                        
        
    }    

    public void showDialog(ErrorReportEntry entry){
        LOGGER.info("\n -- DIALOG MANAGER prep to show dialog for " + entry);
        Platform.runLater(() -> {
            LOGGER.info("\n -- DIALOG MANAGER about to show dialog for " + entry);
            entry.setLastPopupDate(new Date());
            ErrorReportDialog ced = new ErrorReportDialog(entry);
            
            ced.showDialog("Unexpected Exception Occurred ...");
            LOGGER.info("\n -- DIALOG MANAGER ended show dialog for " + entry);
        });
    }
    
    public void removeActivePopupId(Double id){
        activePopupIds.remove(id);
    }
    
    
}
