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
    
    private LogPreferences(){
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * Whether the Connection Logging preference is enabled
     *
     * @return The current preference
     */
    public static boolean isConnectionLoggingEnabled() {
        final long loggingStartTime = PREFERENCES.getLong(CONNECTION_LOGGING_PREF, 0);
        final long currentTime = System.currentTimeMillis();
        final boolean active = (currentTime > loggingStartTime && currentTime - loggingStartTime < 180*1000); // logging is active for 3 hours from activation time
        return active;
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
