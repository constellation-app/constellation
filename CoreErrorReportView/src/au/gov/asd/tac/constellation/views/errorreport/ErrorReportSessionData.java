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
    
        private List<ErrorReportEntry> sessionErrors = new ArrayList<>();

        private static ErrorReportSessionData instance = null;
        private static double entryId = 0;
        public static Date lastUpdate = new Date();
        
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
}
