/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import static au.gov.asd.tac.constellation.views.errorreport.ErrorReportTopComponent.REPORT_SETTINGS_PARAMETER_ID;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Suite for the Error Report View
 *
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
        } catch (final TimeoutException e) {
            System.out.println("\n**** SETUP ERROR: " + e);
            throw e;
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (final TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        } catch (final Exception e) {
            if (e.toString().contains("HeadlessException")) {
                System.out.println("\n**** EXPECTED TEARDOWN ERROR: " + e.toString());
            } else {
                System.out.println("\n**** UN-EXPECTED TEARDOWN ERROR: " + e.toString());
                throw e;
            }
        }
    } 

    @Test
    public void runSessionDataTest() throws InterruptedException {
        System.out.println("\n>>>> ERROR REPORT VIEW - TEST SUITE\n");
    
        final ErrorReportDialogManager erdm = ErrorReportDialogManager.getInstance();
        erdm.setErrorReportRunning(false);
        erdm.setLatestPopupDismissDate(null);
        assertNull(erdm.getLatestPopupDismissDate());
        assertNull(erdm.getGracePeriodResumptionDate());

        final ErrorReportSessionData session = ErrorReportSessionData.getInstance();
        ErrorReportSessionData.setLastUpdate(null);
        assertNull(ErrorReportSessionData.getLastUpdate());
        
        final Date lastUpdateDateTest = new Date();
        ErrorReportSessionData.setLastUpdate(lastUpdateDateTest);
        assertEquals(ErrorReportSessionData.getLastUpdate(), lastUpdateDateTest);

        final String nineLineMessage = "Line1\nLine2\nLine3\nLine4\nLine5\nLine6\nLine7\nLine8\nLine9\n";
        final ErrorReportEntry testEntry = new ErrorReportEntry(Level.SEVERE, "heading1", "summary1", nineLineMessage, ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry2 = new ErrorReportEntry(Level.SEVERE, "heading2", "summary2", "message2", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry3 = new ErrorReportEntry(Level.SEVERE, "heading2", "summary2", nineLineMessage + "Line10\n", ErrorReportSessionData.getNextEntryId());
        final ErrorReportEntry testEntry4 = testEntry2.copy();
        testEntry3.setEntryId(ErrorReportSessionData.getNextEntryId());
        testEntry3.setLastPopupDate(new Date());

        final String trimmedHeader = testEntry3.getTrimmedHeading(7).substring(0, 2);
        final String entryToString = testEntry3.toString();
        assertTrue(entryToString.contains(trimmedHeader));

        final List<String> filters = new ArrayList<>();
        filters.add(Level.SEVERE.getName());

        session.storeSessionError(testEntry);
        session.storeSessionError(testEntry2);
        session.storeSessionError(testEntry3);
        session.storeSessionError(testEntry4);

        List<ErrorReportEntry> storedList = session.refreshDisplayedErrors(filters);

        // should contain 3 entries, one of which has 2 occurences
        assertEquals(storedList.size(), 3);
        final ErrorReportEntry storedData = session.findDisplayedEntryWithId(testEntry2.getEntryId());
        
        assertEquals(storedData.getOccurrences(), 2);

        ErrorReportSessionData.requestScreenUpdate(true);
        assertTrue(ErrorReportSessionData.isScreenUpdateRequested());
        
        // default popup mode 2 only allows 1 popup
        storedList = waitForDialogToBeDisplayed(new ArrayList<>(List.of(Level.SEVERE)), 1, erdm);

        dismissPopups(storedList);

        session.removeEntry(testEntry.getEntryId());
        storedList = session.refreshDisplayedErrors(filters);
        assertEquals(storedList.size(), 2);

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

        storedList = waitForDialogToBeDisplayed(new ArrayList<>(List.of(Level.WARNING, Level.INFO, Level.FINE)), 3, erdm);
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

        storedList = waitForDialogToBeDisplayed(new ArrayList<>(List.of(Level.SEVERE, Level.WARNING, Level.INFO, Level.FINE)), 4, erdm);
        dismissPopups(storedList);

        final ErrorReportTopComponent ertcInstance = new ErrorReportTopComponent();
        // when the Top Component is started it takes over control of the popup handling        
        ertcInstance.handleComponentOpened();
        
        // allow content in the top component to be created in the FX thread before proceeding
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await();
        
        // the refresh will normally happen in a timer task, 
        // but in the interest of not waiting for the scheduled task to happen, we're manually triggering the refresh to keep the test moving along
        ertcInstance.refreshErrorFlashing();
        // allow refresh job in the top component to run in the FX thread before proceeding
        final CountDownLatch latch1 = new CountDownLatch(1);
        Platform.runLater(latch1::countDown);
        latch1.await();

        final ErrorReportEntry partialEntry5 = new ErrorReportEntry(Level.WARNING, null, "part summary", "part message", ErrorReportSessionData.getNextEntryId());
        session.storeSessionError(partialEntry5);
        
        // trigger new filter choice of WARNING
        MultiChoiceParameterType.MultiChoiceParameterValue multiChoiceValue = ertcInstance.getParams().getMultiChoiceValue(REPORT_SETTINGS_PARAMETER_ID);
        final List<String> checked = new ArrayList<>();
        checked.add(ErrorReportTopComponent.SeverityCode.WARNING.getCode());
        multiChoiceValue.setChoices(checked);
        @SuppressWarnings("unchecked") // REPORT_SETTINGS_PARAMETER will always be of type MultiChoiceParameter
        final PluginParameter<MultiChoiceParameterValue> filterTypeParameter = (PluginParameter<MultiChoiceParameterValue>) ertcInstance.getParams().getParameters().get(REPORT_SETTINGS_PARAMETER_ID);
        filterTypeParameter.fireChangeEvent(ParameterChange.PROPERTY);
        
        storedList = waitForDialogToBeDisplayed(new ArrayList<>(List.of(Level.WARNING)), 1, erdm);

        assertEquals(storedList.size(), 1);
        
        // another manual refresh to force updates through
        ertcInstance.refreshErrorFlashing();
        // allow refresh job in the top component to run in the FX thread before proceeding
        final CountDownLatch latch2 = new CountDownLatch(1);
        Platform.runLater(latch2::countDown);
        latch2.await();
        
        final boolean isFlashing = ertcInstance.isIconFlashing();
        // icon should be flashing while there are error popups, and stop flashing when they are all dismissed
        assertTrue(isFlashing);

        ertcInstance.setReportsExpanded(false);
        ertcInstance.refreshSessionErrors(); // sync sessionErrors & SessionErrorsBox
        final ErrorReportEntry checkEntry = ertcInstance.findActiveEntryWithId(storedList.get(0).getEntryId());
        assertFalse(checkEntry.isExpanded());

        dismissPopups(storedList);

        ertcInstance.handleComponentClosed();
        ertcInstance.close();

        System.out.println("\n>>>> PASSED ALL TESTS");
    }

    private List<ErrorReportEntry> waitForDialogToBeDisplayed(final List<Level> logLevels, final int expectedCount, final ErrorReportDialogManager erdm) throws InterruptedException {
        final List<String> filters = new ArrayList<>();
        logLevels.forEach(logLevel -> filters.add(logLevel.getName()));
        int dialogCounter = 0;
        // the refresh will normally happen in a timer task, 
        // but in the interest of not waiting for the scheduled task to happen, we're manually triggering the refresh to keep the test moving along
        erdm.refreshErrorPopups();
        // required to allow tasks to complete in the Swing thread before continuing
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(latch::countDown);
        latch.await();
        
        final List<ErrorReportEntry> errorList = ErrorReportSessionData.getInstance().refreshDisplayedErrors(filters);
        for (final ErrorReportEntry entry : errorList) {
            if (entry.getDialog() != null) {
                dialogCounter++;
            }
        }
        // dialog counter should match expected count
        assertEquals(dialogCounter, expectedCount);
        return errorList;
    }

    private int dismissPopups(final Iterable<ErrorReportEntry> storedList) {
        int count = 0;
        for (final ErrorReportEntry erEntry : storedList) {
            final ErrorReportDialog erDialog = erEntry.getDialog();
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
