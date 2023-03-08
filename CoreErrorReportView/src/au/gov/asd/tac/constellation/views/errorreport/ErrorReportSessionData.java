/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.errorreport;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author OrionsGuardian
 */
public class ErrorReportSessionData {
 
    public static final Logger LOGGER = Logger.getLogger(ErrorReportSessionData.class.getName());
    
        final private List<ErrorReportEntry> sessionErrors = new ArrayList<>();
        final private List<ErrorReportEntry> displayedErrors = new ArrayList<>();
        
        private static ErrorReportSessionData instance = null;
        private static double entryId = 0;
        public static Date lastUpdate = new Date();
        
        public static boolean screenUpdateRequested = false;
        
        ErrorReportSessionData(){
            
        }
        
        public static ErrorReportSessionData getInstance(){
            if (instance == null) {
                LOGGER.info("Creating instance of ErrorReportSessionData");
                instance = new ErrorReportSessionData();
            }
            return instance;
        }
        
        public boolean storeSessionError(ErrorReportEntry entry){
            
        LOGGER.info("\n -- prevent overlapping updates");
        boolean foundMatch = false;
        synchronized(sessionErrors) {
            LOGGER.info("\n -- SESSION DATA store error : " + entry.getHeading());
            String comparisonData = entry.getErrorData();
            int errCount = sessionErrors.size();
            for (int i=0; i<errCount ;i++){
                if (sessionErrors.get(i).getErrorData().equals(comparisonData)){
                    ErrorReportEntry ere = sessionErrors.get(i);
                    ere.incrementOccurrences();
                    ere.setExpanded(true);
                    ere.setHeading(entry.getHeading());
                    foundMatch=true;
                    LOGGER.info("\n -- SESSION DATA FOUND MATCH : incremented existing entry");
                    if (i > 0) {
                        sessionErrors.remove(i);
                        sessionErrors.add(0, ere); // most recent at the top
                    }
                }
            }
            if (!foundMatch) {
                sessionErrors.add(0, entry);
                LOGGER.info("\n -- SESSION DATA NO MATCH : add new entry");
            }
                LOGGER.info("\n -- SESSION DATA : RETURNING : " + (!foundMatch));
                lastUpdate = new Date();
            }
        return !foundMatch;
    }

    public void removeEntry(double entryId){
        synchronized(sessionErrors){
            LOGGER.info("\n -- SESSION DATA remove entry with id=" + entryId);
            int errorCount = sessionErrors.size();
            int foundPos = -1;
            for (int i = 0; i < errorCount && foundPos == -1; i++){
                if (sessionErrors.get(i).getEntryId() == entryId){
                    foundPos = i;
                }
            }
            if (foundPos > -1) {
                sessionErrors.remove(foundPos);
                LOGGER.info("\n -- SESSION DATA removed entry at position " + foundPos);
            }
            lastUpdate = new Date();
        }
    }
        
    public List<ErrorReportEntry> getSessionErrorsCopy(){
        LOGGER.info("\n -- SESSION DATA creating session data copy ");
        List<ErrorReportEntry> returnData = new ArrayList<>();
        for (ErrorReportEntry entry : sessionErrors) {
            returnData.add(entry.copy());
        }
        return returnData;
    }
    
    public List<ErrorReportEntry> refreshDisplayedErrors(){
        // take a snapshot of sessionErrors to use in the display screen
        List<ErrorReportEntry> refreshedData = new ArrayList<>();
        synchronized(sessionErrors){
             synchronized(displayedErrors){
                LOGGER.info("\n -- SESSION DATA creating session data copy ");
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

    public ErrorReportEntry findDisplayedEntryWithId(double id){
        for (ErrorReportEntry activeEntry: displayedErrors) {
            if (activeEntry.getEntryId() == id) {
                return activeEntry;
            }
        }        
        return null;
    }
    
    public void requestScreenUpdate(boolean updateRequested){
        screenUpdateRequested = updateRequested;
    }
    
    public void updateDisplayedEntryScreenSettings(double entryId, Date lastPopupDate, Boolean blockPopups, Boolean expanded){
        LOGGER.info("\n -- SESSION DATA update on entry : " + entryId);
        synchronized(displayedErrors){
            ErrorReportEntry displayedEntry = findDisplayedEntryWithId(entryId);
            if (displayedEntry != null) {
                if (blockPopups != null) {
                    LOGGER.info("\n -- SESSION DATA block popups : " + blockPopups);
                    displayedEntry.setBlockRepeatedPopups(blockPopups);
                }
                if (lastPopupDate != null) {
                    LOGGER.info("\n -- SESSION DATA lastPopupDate : " + lastPopupDate);
                    displayedEntry.setLastPopupDate(lastPopupDate);
                }
                if (expanded != null) {
                    LOGGER.info("\n -- SESSION DATA expanded : " + expanded);
                    displayedEntry.setExpanded(expanded);
                }
            }
        }
    }
    
}
