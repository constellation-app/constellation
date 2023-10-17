/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.gov.asd.tac.constellation.utilities.log;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author OrionsGuardian
 */
public class LogPreferences {
    private static final Preferences PREFERENCES = NbPreferences.forModule(LogPreferences.class);
    private static final String CONNECTION_LOGGING_PREF = "connectionLogging";
    private static final long LOGGING_DURATION = 150*60*1000; // 2.5 hours
    
    private LogPreferences(){
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Whether the Connection Logging preference is enabled
     *
     * @return The current preference
     */
    public static boolean isConnectionLoggingEnabled() {
        return logTimeRemaining() > 0;
    }

    public static long logTimeRemaining() {
        final long loggingStartTime = PREFERENCES.getLong(CONNECTION_LOGGING_PREF, 0);
        final long currentTime = System.currentTimeMillis();
        return Math.max(0, LOGGING_DURATION - currentTime + loggingStartTime);
    }
    
    /**
     * Set the new preference for whether the Connection Logging preference
     * is enabled
     *
     * @param checkChanged What the preference has been changed to
     *
     */
    public static void setConnectionLogging(boolean checkChanged) {
        final long updatedValue = checkChanged ? System.currentTimeMillis() : 0;
        PREFERENCES.putLong(CONNECTION_LOGGING_PREF, updatedValue);
    }

}
