/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.views.errorreport;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a data class to store the exception stack-trace 
 * and maintain a count of occurrences
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
    
    public ErrorReportEntry(String errorHeading, String errorMessage, double id){
        heading = errorHeading;
        errorData = errorMessage;
        entryId = id;
        lastDate = new Date();
        occurrences = 1;
    }
    
    public ErrorReportEntry copy(){
        ErrorReportEntry dataCopy = new ErrorReportEntry(heading, errorData, entryId);
        dataCopy.expanded = expanded;
        dataCopy.lastDate = new Date(lastDate.getTime());
        dataCopy.lastPopupDate = (lastPopupDate == null ? null : new Date(lastPopupDate.getTime()));
        dataCopy.occurrences = occurrences;
        return dataCopy;
    }
    
    public String getHeading(){
        if (heading == null) return null;
        int limit = Math.min(120, heading.length());
        return heading.substring(0, limit);
    }
    
    public void setHeading(String errorHeading){
        heading = errorHeading;
    }
    
    public void incrementOccurrences(){
        occurrences++;
        lastDate = new Date();
    }
    
    public String getErrorData(){
        return errorData;
    }
    
    public int getOccurrences(){
        return occurrences;
    }    
    
    public Date getLastDate(){
        return lastDate;
    }
    
    public String getTimeText(){
            SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm:ss ");
            return sdf.format(lastDate);
    }
    
    public boolean getExpanded(){
        return expanded;
    }
    
    public double getEntryId(){
        return entryId;
    }
    
    public void setEntryId(double id){
        entryId = id;
    }
    
    public void setExpanded(boolean expandedState){
        expanded = expandedState;
    }

    public Date getLastPopupDate() {
        return lastPopupDate;
    }

    public void setLastPopupDate(Date popupDate) {
        lastPopupDate = popupDate;
    }

    public boolean isBlockRepeatedPopups() {
        return preventRepeatedPopups;
    }

    public void setBlockRepeatedPopups(boolean blockRepeatedPopups) {
        preventRepeatedPopups = blockRepeatedPopups;
    }    

//    public boolean showDialog(){
//        LOGGER.info("\n\n ********* calling showDialog()");
//        if (dialogShown.compareAndSet(false, true)) {
//            if (dismissed.compareAndSet(true, false)){                
//                Platform.runLater(() -> {
//                    LOGGER.info("\n\n ********* call to create error report dialog");                                        
//                    lastPopupDate = new Date();
//                    ErrorReportDialog ced = new ErrorReportDialog(this);
//                    LOGGER.info("\n\n ********* about to show error report dialog");
//                    ced.showDialog("Unexpected Exception Occurred ...");
//                    LOGGER.info("\n\n ********* error report dialog is shown");
//                    try {
//                        LOGGER.info("\n\n ********* call wait on error report dialog");
//                        ced.wait();
//                        LOGGER.info("\n\n ********* ended wait call on error report dialog");
//                    } catch (InterruptedException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                    LOGGER.info("\n\n ********* setting dismissed to true");
//                    dismissed.set(true);
//                    LOGGER.info("\n\n ********* setting LatestDismissDate");
//                    ErrorReportDialogManager.getInstance().setLatestDismissDate(new Date());
//                });
//                LOGGER.info("\n\n ********* returning true");
//                return true;
//            }
//        }
//        LOGGER.info("\n\n ********* returning false");
//        return false;
//    }
    
    @Override
    public String toString(){
        return "[ErrorReportEntry:[id=" + entryId + "]"
                + ", [header=" + getHeading() + "]"
                + ", [occurrences=" + occurrences + "]"
                + ", [lastDate=" + lastDate + "]"
                + ", [lastPopupDate=" + lastPopupDate + "]"
                + ", [preventRepeatedPopups=" + preventRepeatedPopups + "]"
                + "]";
    }
}
