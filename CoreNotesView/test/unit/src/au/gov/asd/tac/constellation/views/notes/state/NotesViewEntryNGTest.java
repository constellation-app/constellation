/*
 * Copyright 2010-2026 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.notes.state;

import au.gov.asd.tac.constellation.plugins.reporting.PluginReport;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for null-safe NotesViewEntry construction and updates.
 *
 * @author arimu1
 */
public class NotesViewEntryNGTest {

    private static final Logger LOGGER = Logger.getLogger(NotesViewEntryNGTest.class.getName());

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

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Null title and content from plugin reports must not be stored as null.
     */
    @Test
    public void testConstructorNullTitleAndContent() {
        final NotesViewEntry entry = new NotesViewEntry(
                "0",
                null,
                null,
                false,
                false,
                "#ffffff",
                false
        );

        assertEquals(entry.getNoteTitle(), "");
        assertEquals(entry.getNoteContent(), "");
        assertFalse(entry.isUserCreated());
        assertNotNull(entry.getNodeColour());
    }

    /**
     * Null colour argument must leave the default colour in place.
     */
    @Test
    public void testConstructorNullColourKeepsDefault() {
        final NotesViewEntry entry = new NotesViewEntry(
                "0",
                "title",
                "content",
                true,
                true,
                null,
                false
        );

        assertEquals(entry.getNodeColour(), "#a26fc0");
        assertNotNull(entry.getNodesSelected());
        assertNotNull(entry.getTransactionsSelected());
    }

    /**
     * Setters must not accept null title/content/colour.
     */
    @Test
    public void testSettersRejectNull() {
        final NotesViewEntry entry = new NotesViewEntry(
                "0",
                "title",
                "content",
                true,
                true,
                "#123456",
                false
        );

        entry.setNoteTitle(null);
        entry.setNoteContent(null);
        entry.setNodeColour(null);

        assertEquals(entry.getNoteTitle(), "");
        assertEquals(entry.getNoteContent(), "");
        assertEquals(entry.getNodeColour(), "#123456");
    }

    /**
     * Plugin report updates with a null last message must not store null content.
     */
    @Test
    public void testPluginReportChangedNullMessage() {
        final NotesViewEntry entry = new NotesViewEntry(
                "0",
                "plugin",
                "initial",
                false,
                false,
                "#ffffff",
                false
        );

        final PluginReport report = mock(PluginReport.class);
        when(report.getLastMessage()).thenReturn(null);

        entry.pluginReportChanged(report);

        assertEquals(entry.getNoteContent(), "");
    }
}
