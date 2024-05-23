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
package au.gov.asd.tac.constellation.functionality;

import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportDialogManager;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportEntry;
import au.gov.asd.tac.constellation.views.errorreport.ErrorReportSessionData;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
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
        simulateException(Level.SEVERE, false);
        simulateException(Level.WARNING, false);
        simulateException(Level.INFO, false);
        simulateException(Level.FINE, false);

        LOGGER.log(Level.INFO, "\n ------- start wait for data ... {0}", new Date());
        boolean dataAvailable = waitForDataToBeAvailable(Level.SEVERE, 1);
        assertTrue(dataAvailable, "Data was not available within a reasonable amount of time");
        LOGGER.log(Level.INFO, "\n ------- finished waiting for data ... {0}", new Date());
        
        // confirm the exceptions have been stored in the SessionData class
        confirmExceptionStored(Level.SEVERE, 1);
        confirmExceptionStored(Level.WARNING, 1);
        confirmExceptionStored(Level.INFO, 1);
        confirmExceptionStored(Level.FINE, 1);
        
        confirmEntryOccurrences(Level.SEVERE, 1);
        
        // NOTE: the following 2 calls NEED to be on the SAME LINE to generate identical stack traces
        simulateException(Level.SEVERE, false); simulateException(Level.SEVERE, false);
        
        LOGGER.log(Level.INFO, "\n ------- 2.start wait for data ... {0}", new Date());
        dataAvailable = waitForDataToBeAvailable(Level.SEVERE, 2);
        assertTrue(dataAvailable, "Data was not available within a reasonable amount of time");
        LOGGER.log(Level.INFO, "\n ------- 2.finished waiting for data ... {0}", new Date());

        // confirm correct number of entries and occurrences
        confirmExceptionStored(Level.SEVERE, 2);
        confirmEntryOccurrences(Level.SEVERE, 2);
        
        simulateException(Level.SEVERE, true);
        confirmExceptionStored(Level.SEVERE, 3);
    }
    
    private void simulateException(final Level logLevel, final boolean autoBlockPopup){
        LOGGER.log(Level.INFO, "\n ------- simulating {0} exception", logLevel.getName());
        final Exception e = new Exception((autoBlockPopup ? NotifyDisplayer.BLOCK_POPUP_FLAG : "") + "Something totally not unexpected happened !");
        LOGGER.log(logLevel, "Simulating a " + logLevel.getName() + " exception !", e);
        LOGGER.info("\n ------- simulated.");
    }
    
    private void delay(final long milliseconds){
        // may need to wait for the error handler to do it's thing
        final Executor delayed = CompletableFuture.delayedExecutor(milliseconds, TimeUnit.MILLISECONDS);
        final CompletableFuture cf = CompletableFuture.supplyAsync(() -> (milliseconds) + "ms wait complete", delayed)
            .thenAccept(LOGGER::info);
        try {
            cf.get();
        } catch (final InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.INFO, "\n -------- future was not completed ? : {0}", ex.toString());
        }
    }
        
    private boolean waitForDataToBeAvailable(final Level logLevel, final int expectedCount){
        final List<String> filters = new ArrayList<>();
        filters.add(logLevel.getName());
        int counter = 0;
        List<ErrorReportEntry> errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
        while(counter < 60 && errorList.size() < expectedCount){
            errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
            delay(2000);
            counter++;
        }
        // fails if it takes over 2 minutes
        return counter < 60;
    }
    
    private void confirmExceptionStored(final Level logLevel, final int expectedCount){
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
    
    private void confirmEntryOccurrences(final Level logLevel, final int occurrences){
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
