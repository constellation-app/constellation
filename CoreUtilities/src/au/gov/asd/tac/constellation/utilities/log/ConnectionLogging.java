/*
 * Copyright 2010-2024 Australian Signals Directorate
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ConnectionLogging class is used as a portal to provide session logging for api connections.
 * It will log ALL Levels of errors when the ConnectionLogging preference is enabled.
 * It is primarily intended for Data Access plugins using the RestClient,
 * with enabling/disabling done through the Data Access View -> Workflow Options
 * 
 * @author OrionsGuardian
 */
public class ConnectionLogging {
    private static final Logger LOGGER = Logger.getLogger(ConnectionLogging.class.getName());
    private static ConnectionLogging instance = null;
    
    private ConnectionLogging(){
        LOGGER.setLevel(Level.ALL); // NOSONAR
    }
    
    public static ConnectionLogging getInstance(){
        if (instance == null) {
            instance = new ConnectionLogging();
        }
        return instance;
    }
    
    public void log(final Level logLevel, final String logMessage, final Object logParams){
        if (LogPreferences.isConnectionLoggingEnabled()) {
            if (logParams == null) { 
                LOGGER.log(logLevel, logMessage);
            } else {
                LOGGER.log(logLevel, logMessage, logParams);
            }
        }
    }

}
