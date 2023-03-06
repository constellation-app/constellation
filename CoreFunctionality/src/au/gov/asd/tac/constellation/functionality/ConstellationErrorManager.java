/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.views.errorreport.ErrorReportEntry;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportSessionData;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author OrionsGuardian
 */
@ServiceProvider(service = Handler.class, supersedes = "org.netbeans.core.NbErrorManager")
public class ConstellationErrorManager extends Handler {
    
    private static final Logger LOGGER = Logger.getLogger(ConstellationErrorManager.class.getName());
    
    private static double entryId = 0;
    
    @Override
    public void publish(LogRecord record) {
        if (record != null && record.getThrown() != null) {
            LOGGER.info("==================\n======= publish ========");
            StackTraceElement[] elems = record.getThrown().getStackTrace();
            StringBuffer errorMsg = new StringBuffer();
            String hedr = record.getThrown().getLocalizedMessage() + "\n";
            for (int i=0; i<elems.length; i++){
                errorMsg.append(elems[i].toString() + "\n");
            }
            LOGGER.info("==================\nTrace[0]:" + elems[0] + "\n===============");
             
            final ErrorReportEntry rep4 = new ErrorReportEntry(hedr, errorMsg.toString(), entryId++);
            
            LOGGER.info("==================\nRepEntryHeader:" + rep4.getHeading() + "\n===============");
            boolean showDialog = ErrorReportSessionData.getInstance().storeSessionError(rep4);            
            LOGGER.info("==================\nshowDialog:" + showDialog + "===============");
        }
    }

    @Override
    public void flush() {
        LOGGER.info("==================\n======= flush ========\n===============");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws SecurityException {
        LOGGER.info("==================\n======= close ========\n===============");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
