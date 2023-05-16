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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Suite for the Error Report View
 * @author OrionsGuardian
 */
public class ErrorReportFullSuiteNGTest {

    private static final Logger LOGGER = Logger.getLogger(ErrorReportFullSuiteNGTest.class.getName());
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        try {
            if (!FxToolkit.isFXApplicationThreadRunning()) {
                FxToolkit.registerPrimaryStage();
            }
        } catch (Exception e) {
            System.out.println("\n**** SETUP ERROR: " + e);
            throw e;
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        } catch (Exception e) {
            if (e.toString().contains("HeadlessException")) {
                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
            } else {
                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
                throw e;
            }
        }        
    }
    
    @Test
    public void runSessionDataTest() {

        System.out.println("\n>>>> ERROR REPORT VIEW - TEST SUITE\n");
        
        System.setProperty("java.awt.headless", "true");
        
        final ErrorReportDialogManager erdm = ErrorReportDialogManager.getInstance();
        erdm.setErrorReportRunning(false);
        erdm.setLatestPopupDismissDate(null);
        assertTrue(erdm.getLatestPopupDismissDate() == null);
        assertTrue(erdm.getGracePeriodResumptionDate() == null);
        
        final ErrorReportSessionData session = ErrorReportSessionData.getInstance();
        ErrorReportSessionData.setLastUpdate(null);
        assertTrue(ErrorReportSessionData.getLastUpdate() == null);
        Date lastUpdateDateTest = new Date();
        ErrorReportSessionData.setLastUpdate(lastUpdateDateTest);
        assertTrue(ErrorReportSessionData.getLastUpdate().equals(lastUpdateDateTest));

        final String nineLineMessage = "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\n";
        final ErrorReportEntry testEntry = new ErrorReportEntry(Level.SEVERE, "heading1", "summary1", nineLineMessage, ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry2 = new ErrorReportEntry(Level.SEVERE, "heading2", "summary2", "message2", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry3 = new ErrorReportEntry(Level.SEVERE, "heading2", "summary2", nineLineMessage+"Line10\n", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry4 = testEntry2.copy();
        testEntry3.setEntryId(ErrorReportSessionData.getNextEntryId());
        testEntry3.setLastPopupDate(new Date());

        final String trimmedHeader = testEntry3.getTrimmedHeading(7).substring(0,2);
        final String entryToString = testEntry3.toString();
        System.out.println("\n>>>> Check header");
        assertTrue(entryToString.contains(trimmedHeader));
        
        final List<String> filters = new ArrayList<>();
        filters.add(Level.SEVERE.getName());

        session.storeSessionError(testEntry);
        session.storeSessionError(testEntry2);
        session.storeSessionError(testEntry3);
        session.storeSessionError(testEntry4);
        
        List<ErrorReportEntry> storedList = session.refreshDisplayedErrors(filters);
        
        // should contain 2 entries, each having 2 occurences
        System.out.println("\n>>>> Check list size");
        assertEquals(storedList.size(), 2);        
        final ErrorReportEntry storedData = session.findDisplayedEntryWithId(testEntry2.getEntryId());
        
        System.out.println("\n>>>> Check Occurrences");
        assertEquals(storedData.getOccurrences(), 2);
        
        ErrorReportSessionData.requestScreenUpdate(true);
        System.out.println("\n>>>> Check screen update requested");
        assertTrue(ErrorReportSessionData.isScreenUpdateRequested());

        List<String> activeLevels = erdm.getActivePopupErrorLevels();

        System.out.println("\n\n>>>> Waiting for SEVERE dialogs");
        // default popup mode 2 only allows 1 popup
        storedList = waitForDialogToBeDisplayed(new ArrayList<Level>(List.of(Level.SEVERE)), 1);
        System.out.println("\n\n>>>> Done Waiting");
        
        dismissPopups(storedList);
        
        session.removeEntry(testEntry.getEntryId());
        storedList = session.refreshDisplayedErrors(filters);
        System.out.println("\n>>>> Check new list size");
        assertEquals(storedList.size(), 1); 
        
        activeLevels = erdm.getActivePopupErrorLevels();

        System.out.println("\n>>>> Check active levels: " + activeLevels);
        System.out.println("\n>>>> resumption date: " + erdm.getGracePeriodResumptionDate() +
                           "\n>>>> latest pop dis data: " + erdm.getLatestPopupDismissDate());
        
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

        erdm.setLatestPopupDismissDate(null);
        // this also resets the grace period
        
        session.storeSessionError(partialEntry3);

        activeLevels = erdm.getActivePopupErrorLevels();
        System.out.println("\n>>>> Check active levels: " + activeLevels);
        
        System.out.println("\n\n>>>> Waiting for WARN/INFO/FINE dialogs");
        storedList = waitForDialogToBeDisplayed(new ArrayList<Level>(List.of(Level.WARNING, Level.INFO, Level.FINE)), 3);
        System.out.println("\n\n>>>> Done Waiting");
        System.out.println("\n>>>> Check WARN/INFO/FINE list size");
        assertEquals(storedList.size(), 3);       
                
        // clear ALL popups
        filters.clear();
        filters.add(Level.SEVERE.getName());
        filters.add(Level.WARNING.getName());
        filters.add(Level.INFO.getName());
        filters.add(Level.FINE.getName());        
        storedList = session.refreshDisplayedErrors(filters);
        dismissPopups(storedList);
        
        for (final ErrorReportEntry erEntry : storedList) {
            // confirm popups are being blocked correctly
            erdm.updatePopupSettings(0, filters);
            erdm.showErrorDialog(erEntry, true); // mode 0, block all popups
            erdm.updatePopupSettings(1, filters);
            erdm.showErrorDialog(erEntry, true); // should be blocked - no redisplay
            erdm.updatePopupSettings(3, filters);
            erdm.showErrorDialog(erEntry, true); // again should be blocked - no redisplay            
            erdm.updatePopupSettings(4, filters);
            erdm.showErrorDialog(erEntry, true); // should redisplay dialog in review mode
        }
        
        storedList = waitForDialogToBeDisplayed(new ArrayList<Level>(List.of(Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE)), 4);
        dismissPopups(storedList);
        
        final ErrorReportTopComponent ertcInstance = new ErrorReportTopComponent();
        // when the Top Component is started it takes over control of the popup handling        
        ertcInstance.handleComponentOpened();
        
        System.out.println("\n\n>>>> Waiting 5s for popup grace period");
        delay(5100);
        System.out.println("\n\n>>>> Done Waiting");        
        
        final ErrorReportEntry partialEntry5 = new ErrorReportEntry(Level.WARNING, null, "part summary", "part message", ErrorReportSessionData.getNextEntryId());
        session.storeSessionError(partialEntry5);
        
        System.out.println("\n\n>>>> Waiting for TC dialogs");
        storedList = waitForDialogToBeDisplayed(new ArrayList<Level>(List.of(Level.WARNING)), 1);
        System.out.println("\n\n>>>> Done Waiting");
        
        System.out.println("\n>>>> Check WARNINGS list size");
        assertEquals(storedList.size(), 1);        

        final boolean isFlashing = ertcInstance.isIconFlashing();
        assertTrue(isFlashing);
        ertcInstance.setReportsExpanded(false);
        ertcInstance.refreshSessionErrors();
        final ErrorReportEntry checkEntry = ertcInstance.findActiveEntryWithId(storedList.get(0).getEntryId());
        assertFalse(checkEntry.isExpanded());
        
        dismissPopups(storedList);
        
        ertcInstance.handleComponentClosed();
        ertcInstance.close();
        
        System.clearProperty("java.awt.headless");
        System.out.println("\n>>>> PASSED ALL TESTS");
        
    }
    
    private void delay(final long milliseconds){
        // may need to wait for timers to trigger and do their thing
        final Executor delayed = CompletableFuture.delayedExecutor(milliseconds, TimeUnit.MILLISECONDS);
        final CompletableFuture cf = CompletableFuture.supplyAsync(() -> (milliseconds) + "ms wait complete", delayed)
            .thenAccept(LOGGER::info);
        try {
            cf.get();
        } catch (final InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.INFO, "\n -------- future was not completed ? : {0}", ex.toString());
        }
    }

    private List<ErrorReportEntry> waitForDialogToBeDisplayed(final List<Level> logLevels, final int expectedCount){
        final List<String> filters = new ArrayList<>();
        logLevels.forEach(logLevel -> filters.add(logLevel.getName()));        
        int loopCounter = 0;
        int dialogCounter = 0;
        List<ErrorReportEntry> errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
        while(loopCounter < 60 && dialogCounter < expectedCount){
            dialogCounter = 0;
            errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
            for (final ErrorReportEntry entry : errorList) {
                if (entry.getDialog() != null) {
                    dialogCounter++;
                }
            }
            delay(3000);
            loopCounter++;
        }
        // fails if it takes over 3 minutes
        assertTrue(loopCounter < 60);
        return errorList;
    }

    private int dismissPopups(final Iterable<ErrorReportEntry> storedList) {
        int count = 0;
        for (final ErrorReportEntry erEntry : storedList) {
            System.out.println("\n>>>> Checking entry: " + erEntry);
            ErrorReportDialog erDialog = erEntry.getDialog();
            if (erDialog != null) {
                // simulate close
                erDialog.finaliseSessionSettings();
                erDialog.hideDialog();
                System.out.println("\n>>>> UPDATED and CLOSED popup");
            }
        }
        return count;
    }
}
