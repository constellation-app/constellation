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
package au.gov.asd.tac.constellation.views.errorreport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author OrionsGuardian
 */
public class ErrorReportFullSuiteNGTest {

    private static final Logger LOGGER = Logger.getLogger(ErrorReportFullSuiteNGTest.class.getName());
    
    //private ExecutorService executor;

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }
    
    @Test
    public void runSessionDataTest() {

//        executor = mock(ExecutorService.class);
//        implementAsDirectExecutor(executor);
 
//       MockedStatic<SwingUtilities> mockedStatic = Mockito.mockStatic(SwingUtilities.class, Mockito.CALLS_REAL_METHODS);
        
//        doAnswer(new Answer<Object>() {
//            public Object answer(InvocationOnMock invocation) throws Exception {
//                ((Runnable) invocation.getArguments()[0]).run();
//                return null;
//            }
//        }).when(mockedStatic).invokeLater(any(Runnable.class));
                
//        Answer<Object> laterAnswer = new Answer<Object>() {
//            public Object answer(InvocationOnMock invocation) throws Exception {
//                ((Runnable) invocation.getArguments()[0]).run();
//                LOGGER.log(Level.INFO, "\n\n>>>><<<< = = = = = = HERE AND NOW + + + + + +\n");
//                return null;
//            };
//        };
//
//        mockedStatic.when(() ->
//            SwingUtilities.invokeLater(any())) // <<<<<<<<<<<<<<<<<<<<<<
//            .then(laterAnswer);

//        mockedStatic.when(SwingUtilities::invokeLater).answer(laterAnswer);

//        final MockedStatic<ErrorReportDialogManager> erdmStatic = Mockito.mockStatic(ErrorReportDialogManager.class);
//        final ErrorReportDialogManager erdm = spy(new ErrorReportDialogManager());
//        erdmStatic.when(ErrorReportDialogManager::getInstance).thenReturn(erdm);


        final ErrorReportDialogManager erdm = ErrorReportDialogManager.getInstance();
        erdm.setErrorReportRunning(false);
        
        final ErrorReportSessionData session = ErrorReportSessionData.getInstance();

        final ErrorReportEntry testEntry = new ErrorReportEntry(Level.SEVERE, "heading1", "summary1", "message1", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry2 = new ErrorReportEntry(Level.SEVERE, "heading2", "summary2", "message2", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry3 = testEntry2.copy();
        testEntry3.setEntryId(ErrorReportSessionData.getNextEntryId());
        testEntry3.setLastPopupDate(new Date());
        final String trimmedHeader = testEntry3.getTrimmedHeading(7).substring(0,2);
        final String entryToString = testEntry3.toString();
        System.out.println("\n>>>> Check header");
        assertTrue(entryToString.contains(trimmedHeader));
        
        session.storeSessionError(testEntry);
        session.storeSessionError(testEntry2);
        session.storeSessionError(testEntry3);
        
        final List<String> filters = new ArrayList<>();
        filters.add(Level.SEVERE.getName());
        List<ErrorReportEntry> storedList = session.refreshDisplayedErrors(filters);
        
        // should contain 2 entries, with the second one having 2 occurences
        System.out.println("\n>>>> Check list size");
        assertEquals(storedList.size(), 2);        
        final ErrorReportEntry storedData = session.findDisplayedEntryWithId(testEntry2.getEntryId());
        
        System.out.println("\n>>>> Check occs");
        assertEquals(storedData.getOccurrences(), 2);
        
        ErrorReportSessionData.requestScreenUpdate(true);
        System.out.println("\n>>>> Check requested");
        assertTrue(ErrorReportSessionData.isScreenUpdateRequested());

        List<String> activeLevels = erdm.getActivePopupErrorLevels();

        System.out.println("\n\n>>>> Waiting for dialog");
        storedList = waitForDialogToBeDisplayed(new ArrayList<Level>(List.of(Level.SEVERE)), 1);
        System.out.println("\n\n>>>> Done Waiting");
        
        for (final ErrorReportEntry erEntry : storedList) {
            System.out.println("\n>>>> Checking entry: " + erEntry);
            ErrorReportDialog erDialog = erEntry.getDialog();
            if (erDialog != null) {
                // simulate close
                erDialog.finaliseSessionSettings();
                erDialog.hideDialog();
                System.out.println("\n>>>> UPDATE and CLOSE !!");
            }
        }
        
        System.out.println("\n>>>> Check active levels: " + activeLevels);
        System.out.println("\n>>>> resumption date: " + erdm.getGracePeriodResumptionDate() +
                                "\n>>>> latest pop dis data: " + erdm.getLatestPopupDismissDate());
        
        session.removeEntry(testEntry.getEntryId());
        storedList = session.refreshDisplayedErrors(filters);
        System.out.println("\n>>>> Check new list size");
        assertEquals(storedList.size(), 1);         
        
        activeLevels = erdm.getActivePopupErrorLevels();

        System.out.println("\n>>>> Check active levels: " + activeLevels);
        System.out.println("\n>>>> resumption date: " + erdm.getGracePeriodResumptionDate() +
                                "\n>>>> latest pop dis data: " + erdm.getLatestPopupDismissDate());

        System.out.println("\n\n>>>> Waiting 5s for popup grace period");
        delay(5100);
        System.out.println("\n\n>>>> Done Waiting");
        
        System.out.println("\n\n>>>> Generating WARNING, INFO, and FINE entries");
        
        final ErrorReportEntry partialEntry = new ErrorReportEntry(Level.WARNING, null, "part summary", "part message", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry partialEntry2 = new ErrorReportEntry(Level.INFO, "part header 2", null, "part message 2", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry partialEntry3 = new ErrorReportEntry(Level.FINE, null, null, "part message 3", ErrorReportSessionData.getNextEntryId());

        filters.clear();
        filters.add(Level.WARNING.getName());
        filters.add(Level.INFO.getName());
        filters.add(Level.FINE.getName());
        erdm.updatePopupSettings(4, filters);
        
        session.storeSessionError(partialEntry);
        session.storeSessionError(partialEntry2);
        session.storeSessionError(partialEntry3);

        activeLevels = erdm.getActivePopupErrorLevels();

        System.out.println("\n>>>> Check active levels: " + activeLevels);
        
        System.out.println("\n\n>>>> Waiting for dialogs");
        storedList = waitForDialogToBeDisplayed(new ArrayList<Level>(List.of(Level.WARNING, Level.INFO, Level.FINE)), 3);
        System.out.println("\n\n>>>> Done Waiting");
        System.out.println("\n>>>> Check WARNINGS list size");
        assertEquals(storedList.size(), 3);        
        
        for (final ErrorReportEntry erEntry : storedList) {
            System.out.println("\n>>>> Checking entry: " + erEntry);
            ErrorReportDialog erDialog = erEntry.getDialog();
            if (erDialog != null) {
                // simulate close
                erDialog.finaliseSessionSettings();
                erDialog.hideDialog();
                System.out.println("\n>>>> UPDATE and CLOSE !!");
            }
        }
        
        ErrorReportTopComponent ertc = new ErrorReportTopComponent();
        // when this is started it takes over control of the popup handling
        ertc.handleComponentOpened();
        
        System.out.println("\n\n>>>> Waiting 5s for popup grace period");
        delay(5100);
        System.out.println("\n\n>>>> Done Waiting");        
        
        // there should be some report entries already available

        final ErrorReportEntry partialEntry5 = new ErrorReportEntry(Level.WARNING, null, "part summary", "part message", ErrorReportSessionData.getNextEntryId());
        session.storeSessionError(partialEntry5);
        
        System.out.println("\n\n>>>> Waiting for dialogs");
        storedList = waitForDialogToBeDisplayed(new ArrayList<Level>(List.of(Level.WARNING)), 1);
        System.out.println("\n\n>>>> Done Waiting");
        
        System.out.println("\n>>>> Check WARNINGS list size");
        assertEquals(storedList.size(), 1);        

        System.out.println("\n\n>>>> Waiting 3s for confirmation");
        delay(5100);
        System.out.println("\n\n>>>> Done Waiting");        
        
        for (final ErrorReportEntry erEntry : storedList) {
            System.out.println("\n>>>> Checking entry: " + erEntry);
            ErrorReportDialog erDialog = erEntry.getDialog();
            if (erDialog != null) {
                // simulate close
                erDialog.finaliseSessionSettings();
                erDialog.hideDialog();
                System.out.println("\n>>>> UPDATE and CLOSE !!");
            }
        }
        
        ertc.close();
        
        System.out.println("\n>>>> PASSED everything");
        
    }

    protected void implementAsDirectExecutor(ExecutorService executor) {
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Exception {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executor).submit(any(Runnable.class));
    }    
    
    public void delay(final long milliseconds){
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

    public List<ErrorReportEntry> waitForDialogToBeDisplayed(final List<Level> logLevels, final int expectedCount){
        final List<String> filters = new ArrayList<>();
        logLevels.forEach(logLevel -> filters.add(logLevel.getName()));        
        int loopCounter = 0;
        int dialogCounter = 0;
        List<ErrorReportEntry> errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
        while(loopCounter < 6 && dialogCounter < expectedCount){
            dialogCounter = 0;
            errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
            for (final ErrorReportEntry entry : errorList) {
                if (entry.getDialog() != null) {
                    dialogCounter++;
                }
            }
            delay(1000);
            loopCounter++;
        }
        // fails if it takes over 1 minute
        assertTrue(loopCounter < 6);
        return errorList;
    }
    
}
