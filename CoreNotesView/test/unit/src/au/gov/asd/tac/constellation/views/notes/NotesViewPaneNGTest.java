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
package au.gov.asd.tac.constellation.views.notes;

import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import static org.mockito.Mockito.mock;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class NotesViewPaneNGTest {

    private static final Logger LOGGER = Logger.getLogger(NotesViewPaneNGTest.class.getName());

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

    /**
     * Test of constructor method, of class NotesViewPane.
     */
    @Test
    public void testConstructor() {
        System.out.println("setGraphReport");
        Platform.runLater(() -> {
            NotesViewPane instance = new NotesViewPane(mock(NotesViewController.class));
            assertEquals(instance.getClass(), NotesViewPane.class);
        });
    }

//    /**
//     * Test of setGraphReport method, of class NotesViewPane.
//     */
//    @Test
//    public void testSetGraphReport_0args() {
//        System.out.println("setGraphReport");
//        NotesViewPane instance = null;
//        instance.setGraphReport();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setGraphReport method, of class NotesViewPane.
//     */
//    @Test
//    public void testSetGraphReport_Graph() {
//        System.out.println("setGraphReport");
//        Graph graph = null;
//        NotesViewPane instance = null;
//        instance.setGraphReport(graph);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addPluginReport method, of class NotesViewPane.
//     */
//    @Test
//    public void testAddPluginReport() {
//        System.out.println("addPluginReport");
//        PluginReport pluginReport = null;
//        NotesViewPane instance = null;
//        instance.addPluginReport(pluginReport);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of processNewUndoRedoReport method, of class NotesViewPane.
//     */
//    @Test
//    public void testProcessNewUndoRedoReport() {
//        System.out.println("processNewUndoRedoReport");
//        UndoRedoReport undoRedoReport = null;
//        NotesViewPane instance = null;
//        instance.processNewUndoRedoReport(undoRedoReport);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getNotes method, of class NotesViewPane.
//     */
//    @Test
//    public void testGetNotes() {
//        System.out.println("getNotes");
//        NotesViewPane instance = null;
//        List expResult = null;
//        List result = instance.getNotes();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getFilters method, of class NotesViewPane.
//     */
//    @Test
//    public void testGetFilters() {
//        System.out.println("getFilters");
//        NotesViewPane instance = null;
//        List expResult = null;
//        List result = instance.getFilters();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setNotes method, of class NotesViewPane.
//     */
//    @Test
//    public void testSetNotes() {
//        System.out.println("setNotes");
//        List<NotesViewEntry> notesViewEntries = null;
//        NotesViewPane instance = null;
//        instance.setNotes(notesViewEntries);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setFilters method, of class NotesViewPane.
//     */
//    @Test
//    public void testSetFilters() {
//        System.out.println("setFilters");
//        List<String> selectedFilters = null;
//        NotesViewPane instance = null;
//        instance.setFilters(selectedFilters);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateNotesUI method, of class NotesViewPane.
//     */
//    @Test
//    public void testUpdateNotesUI() {
//        System.out.println("updateNotesUI");
//        NotesViewPane instance = null;
//        instance.updateNotesUI();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateFilters method, of class NotesViewPane.
//     */
//    @Test
//    public void testUpdateFilters() {
//        System.out.println("updateFilters");
//        NotesViewPane instance = null;
//        instance.updateFilters();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of clearNotes method, of class NotesViewPane.
//     */
//    @Test
//    public void testClearNotes() {
//        System.out.println("clearNotes");
//        NotesViewPane instance = null;
//        instance.clearNotes();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateSelectedElements method, of class NotesViewPane.
//     */
//    @Test
//    public void testUpdateSelectedElements() {
//        System.out.println("updateSelectedElements");
//        NotesViewPane instance = null;
//        instance.updateSelectedElements();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addToSelectedElements method, of class NotesViewPane.
//     */
//    @Test
//    public void testAddToSelectedElements() {
//        System.out.println("addToSelectedElements");
//        NotesViewEntry noteToEdit = null;
//        NotesViewPane instance = null;
//        instance.addToSelectedElements(noteToEdit);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of removeFromSelectedElements method, of class NotesViewPane.
//     */
//    @Test
//    public void testRemoveFromSelectedElements() {
//        System.out.println("removeFromSelectedElements");
//        NotesViewEntry noteToEdit = null;
//        NotesViewPane instance = null;
//        instance.removeFromSelectedElements(noteToEdit);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateTagsFiltersAvailable method, of class NotesViewPane.
//     */
//    @Test
//    public void testUpdateTagsFiltersAvailable() {
//        System.out.println("updateTagsFiltersAvailable");
//        NotesViewPane instance = null;
//        instance.updateTagsFiltersAvailable();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateSelectedTagsCombo method, of class NotesViewPane.
//     */
//    @Test
//    public void testUpdateSelectedTagsCombo() {
//        System.out.println("updateSelectedTagsCombo");
//        List<String> selectedTagsFilters = null;
//        NotesViewPane instance = null;
//        instance.updateSelectedTagsCombo(selectedTagsFilters);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateTagFilters method, of class NotesViewPane.
//     */
//    @Test
//    public void testUpdateTagFilters() {
//        System.out.println("updateTagFilters");
//        NotesViewPane instance = null;
//        instance.updateTagFilters();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of updateAutoNotesDisplayed method, of class NotesViewPane.
//     */
//    @Test
//    public void testUpdateAutoNotesDisplayed() {
//        System.out.println("updateAutoNotesDisplayed");
//        NotesViewEntry entry = null;
//        NotesViewPane instance = null;
//        boolean expResult = false;
//        boolean result = instance.updateAutoNotesDisplayed(entry);
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTagsFilters method, of class NotesViewPane.
//     */
//    @Test
//    public void testGetTagsFilters() {
//        System.out.println("getTagsFilters");
//        NotesViewPane instance = null;
//        List expResult = null;
//        List result = instance.getTagsFilters();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCreateNewNoteButton method, of class NotesViewPane.
//     */
//    @Test
//    public void testGetCreateNewNoteButton() {
//        System.out.println("getCreateNewNoteButton");
//        NotesViewPane instance = null;
//        Button expResult = null;
//        Button result = instance.getCreateNewNoteButton();
//        assertEquals(result, expResult);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
