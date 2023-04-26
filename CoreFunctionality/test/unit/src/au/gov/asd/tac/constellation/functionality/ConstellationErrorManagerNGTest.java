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
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.views.errorreport.ErrorReportDialogManager;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportEntry;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportSessionData;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * Testing for the ConstellationErrorManager class
 * @author OrionsGuardian
 */
public class ConstellationErrorManagerNGTest {
    private static final Logger LOGGER = Logger.getLogger(ConstellationErrorManagerNGTest.class.getName());
    
    @Test
    public void runExceptionsTest() {
        final MockedStatic<ErrorReportDialogManager> erdmStatic = Mockito.mockStatic(ErrorReportDialogManager.class);
        final ErrorReportDialogManager erdm = mock(ErrorReportDialogManager.class);
        erdmStatic.when(ErrorReportDialogManager::getInstance).thenReturn(erdm);

        final ConstellationErrorManager errorManager = new ConstellationErrorManager();
        LOGGER.addHandler(errorManager);
        LOGGER.setLevel(Level.ALL);
        
        // generate exception log messages
        simulateException(Level.SEVERE);
        simulateException(Level.WARNING);
        simulateException(Level.INFO);
        simulateException(Level.FINE);
        delay(1000);
        
        // confirm the exceptions have been stored in the SessionData class
        confirmExceptionStored(Level.SEVERE, 1);
        confirmExceptionStored(Level.WARNING, 1);
        confirmExceptionStored(Level.INFO, 1);
        confirmExceptionStored(Level.FINE, 1);
        
        confirmEntryOccurrences(Level.SEVERE, 1);
        
        // NOTE: the following 2 calls NEED to be on the SAME LINE to generate identical stack traces
        simulateException(Level.SEVERE); simulateException(Level.SEVERE);
        
        delay(1000);
        
        // confirm correct number of entries and occurrences
        confirmExceptionStored(Level.SEVERE, 2);
        confirmEntryOccurrences(Level.SEVERE, 2);
    }
    
    public void simulateException(final Level logLevel){
        LOGGER.log(Level.INFO, "\n ------- simulating {0} exception", logLevel.getName());
        final Exception e = new Exception("Something totally not unexpected happened !");
        LOGGER.log(logLevel, "Simulating a " + logLevel.getName() + " exception !", e);
        LOGGER.info("\n ------- simulated.");
    }
    
    public void delay(final int milliseconds){
        // may need to wait for the error handler to do it's thing
        try {
            Thread.sleep(milliseconds);
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.INFO, "\n -------- sleep disturbed: {0}", ex.toString());
        }
    }
    
    public void confirmExceptionStored(final Level logLevel, final int expectedCount){
        LOGGER.log(Level.INFO, "\n -------- confirming entries : {0} separate instances of {1}", new Object[]{expectedCount, logLevel.getName()});
        final List<String> filters = new ArrayList<>();
        filters.add(logLevel.getName());
        final List<ErrorReportEntry> errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
        for (final ErrorReportEntry entry : errorList) {
            assertEquals(entry.getErrorLevel().getName(), logLevel.getName());
        }
        assertEquals(errorList.size(), expectedCount);
        LOGGER.info("\n -------- confirmed.");
    }
    
    public void confirmEntryOccurrences(final Level logLevel, final int occurrences){
        LOGGER.log(Level.INFO, "\n -------- confirming occurrences : {0} occurrences in 1 instance of {1}", new Object[]{occurrences, logLevel.getName()});
        final List<String> filters = new ArrayList<>();
        filters.add(logLevel.getName());
        final List<ErrorReportEntry> errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
        boolean foundMatchingOccurrences = false;
        for (final ErrorReportEntry entry : errorList) {
            assertEquals(entry.getErrorLevel().getName(), logLevel.getName());
            if (entry.getOccurrences() == occurrences) {
                foundMatchingOccurrences = true;
            }
        }
        assertTrue(foundMatchingOccurrences, "Did not find an entry with " + occurrences + " occurrences");
        LOGGER.info("\n -------- confirmed.");
    }
}
