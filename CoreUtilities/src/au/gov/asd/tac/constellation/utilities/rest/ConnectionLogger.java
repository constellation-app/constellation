/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.utilities.rest;

import au.gov.asd.tac.constellation.utilities.log.LogPreferences;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author OrionsGuardian
 */
public class ConnectionLogger {
    private static final Logger LOGGER = Logger.getLogger(ConnectionLogger.class.getName());
    private static ConnectionLogger instance = null;
    
    private ConnectionLogger(){
        getInstance().setLogLevel(Level.ALL);
    }
    
    public static ConnectionLogger getInstance(){
        if (instance == null) {
            instance = new ConnectionLogger();
        }
        return instance;
    }
    
    private void setLogLevel(final Level newLevel){
        LOGGER.setLevel(newLevel);
    }
    
    public void log(final Level logLevel, final String logMessage, final Object logParams){
        if (LogPreferences.isConnectionLoggingEnabled()) {
            LOGGER.log(logLevel, logMessage, logParams);
        }
    }

}
