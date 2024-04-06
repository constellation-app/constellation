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
package au.gov.asd.tac.constellation.utilities.log;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * This class stores and processes the Connection Logging preference.
 * It is designed to be active for only a limited amount of time. This is to
 * avoid excessive logging in the cases where a user may forget to disable the setting.
 * 
 * @author OrionsGuardian
 */
public class LogPreferences {
    private static final Preferences PREFERENCES = NbPreferences.forModule(LogPreferences.class);
    private static final String CONNECTION_LOG_DATE_PREF = "connectionLogDate";
    private static final long LOGGING_TIMEOUT = 151*60*1000L; // 2.5 hours
    
    
    private LogPreferences(){
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * 
     * The Connection Logging preference is considered to be enabled while it is 
     * within the timeout period.
     * After the fixed timeout period has lapsed, Connection Logging will be effectively disabled.
     *
     * @return The enabled status of the Connection Logging preference
     */
    public static boolean isConnectionLoggingEnabled() {
        return logTimeRemaining() > 0;
    }

    /**
     * Compares the current time with the time the setting was enabled, 
     * to determine if there is any time remaining within the timeout period
     * 
     * @return The amount of time in milliseconds remaining, taken from the timeout period
     */
    public static long logTimeRemaining() {
        final long loggingStartTime = PREFERENCES.getLong(CONNECTION_LOG_DATE_PREF, 0);
        final long currentTime = System.currentTimeMillis();
        return Math.max(0, LOGGING_TIMEOUT - currentTime + loggingStartTime);
    }
    
    /**
     * When enabling Connection Logging, the preference will be set to the current time, 
     * otherwise it will be set to 0.
     *
     * @param activateLogging Set the preference to be effectively on or off
     *
     */
    public static void setConnectionLogging(final boolean activateLogging) {
        final long updatedValue = activateLogging ? System.currentTimeMillis() : 0;
        PREFERENCES.putLong(CONNECTION_LOG_DATE_PREF, updatedValue);
    }

}
