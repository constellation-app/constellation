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
package au.gov.asd.tac.constellation.utilities.font;

import au.gov.asd.tac.constellation.preferences.ApplicationPreferenceKeys;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.UIManager;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import org.openide.util.NbPreferences;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class FontUtilitiesNGTest {

    private static final Logger LOGGER = Logger.getLogger(FontUtilities.class.getName());
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;

    private static final String NODE_NOT_EXIST_LOG_MSG = "Node does not exist";
    private static final String INT_PARSE_ERROR_PATTERN = "For input string: \"%s\"";

    private static Handler[] existingLogHandlers;

    // removes all handlers from a logger
    private static void removeHandlers(final Logger logger, final Handler[] handlers) {
        for (final Handler h : handlers) {
            logger.removeHandler(h);
        }
    }

    /**
     * Attaches customLogHandler to the FontUtilities logger, which will also receive logging events, and removes the
     * class console logger.
     */
    @BeforeClass
    public static void setUpClass() {
        // remove the existing handlers, but store them so they can be restored
        existingLogHandlers = LOGGER.getParent().getHandlers();
        removeHandlers(LOGGER.getParent(), existingLogHandlers);

        // add a custom handler based off the first existing handler
        logCapturingStream = new ByteArrayOutputStream();
        customLogHandler = new StreamHandler(logCapturingStream,
                existingLogHandlers[0].getFormatter());
        LOGGER.getParent().addHandler(customLogHandler);
    }

    /**
     * Removes the Handler from the FontUtilities logger and restores the console logger.
     */
    @AfterClass
    public static void tearDownClass() {
        removeHandlers(LOGGER.getParent(), LOGGER.getParent().getHandlers());
        for (final Handler h : existingLogHandlers) {
            LOGGER.getParent().addHandler(h);
        }
    }

    /**
     * Gets any logs captured so far by the customLogHandler.
     *
     * @return any logs captured
     * @throws IOException if logs can't be retrieved
     */
    public String getCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }

    /**
     * Can set the default application font size and family Preferences.
     *
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testInitAppFontPrefs() throws BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();

        /* keep the original application preferences for font size and family so
           they can be restored later */
        final String defaultFontSize = ApplicationPreferenceKeys.FONT_SIZE_DEFAULT;
        final String defaultFontFamily = ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT;

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);
            final String testFontSize1 = "951357";
            final String testFontFamily1 = "a dummy font family";
            final String testFontSize2 = "359157";
            final String testFontFamily2 = "another dummy font family";

            /* Preferences will be created and set to default values if attempt 
               to get Preferences when they do not exist */
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_SIZE, testFontSize1);
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_FAMILY, testFontFamily1);

            FontUtilities.initialiseApplicationFontPreferenceOnFirstUse();
            final String fontSize = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);
            final String fontFamily = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);

            assertEquals(fontSize, testFontSize1);
            assertEquals(fontFamily, testFontFamily1);

            /* as Preferences now exist the application default Preferences are 
               set to the values currently in Preferences */
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_SIZE, testFontSize2);
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_FAMILY, testFontFamily2);

            FontUtilities.initialiseApplicationFontPreferenceOnFirstUse();

            final String resultFontFamily = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
            final String resultFontSize = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);

            assertEquals(testFontSize2, resultFontSize);
            assertEquals(testFontFamily2, resultFontFamily);
        } finally {
            // clean up, first remove Preferences nodes this test plays with
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();

            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_SIZE, defaultFontSize);
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_FAMILY, defaultFontFamily);

            final String resultFontFamily = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
            final String resultFontSize = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);

            assertEquals(resultFontSize, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);
            assertEquals(resultFontFamily, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
        }
    }

    /**
     * While setting the default application font size and family a BackingStoreException is thrown, resulting in
     * nothing happening except for the generation of a log message.
     *
     * @throws IOException if logs can't be captured
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testInitAppFontPrefsException() throws IOException, BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(
                    new BorkedPreferences());

            FontUtilities.initialiseApplicationFontPreferenceOnFirstUse();
            assertFalse(p.nodeExists(ApplicationPreferenceKeys.FONT_PREFERENCES));
            assertTrue(getCapturedLog().contains(BackingStoreException.class.getName()));
        }
    }

    /**
     * Can cache the font to be used by the application.
     */
    @Test
    public void testGetApplicationFont() {
        final int size = Integer.MAX_VALUE;
        final String family = "ridiculousFontFamily";
        try (final MockedStatic<FontUtilities> fontUtilsStatic = mockStatic(FontUtilities.class)) {
            fontUtilsStatic.when(() -> FontUtilities.getApplicationFontSize()).thenReturn(size);
            fontUtilsStatic.when(() -> FontUtilities.getApplicationFontFamily()).thenReturn(family);
            fontUtilsStatic.when(() -> FontUtilities.getApplicationFont()).thenCallRealMethod();

            // first invocation places a Font in the cache
            final Font font = FontUtilities.getApplicationFont();
            assertEquals(font.getName(), family);
            assertEquals(font.getStyle(), Font.PLAIN);
            assertEquals(font.getSize(), size);

            // second invocation gets the existing Font from the cache
            assertSame(FontUtilities.getApplicationFont(), font);
        }
    }

    /**
     * Can get the user's default font size.
     *
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetApplicationFontSize() throws BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();

        try (
                final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class); final MockedStatic<UIManager> managerMockedStatic = mockStatic(UIManager.class);) {
            // set up mocks
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);
            final Font font = mock(Font.class);
            final int fontSizeUiManger = Integer.MAX_VALUE;
            when(font.getSize()).thenReturn(fontSizeUiManger);
            managerMockedStatic.when(() -> UIManager.getFont(any())).thenReturn(font);

            /* assert that the value from the UIManager mock is returned when no 
               Preference exists */
            assertEquals(FontUtilities.getApplicationFontSize(), fontSizeUiManger);

            /* create the Preference and assert that when invoked, the 
               Preference is returned */
            final String fontSizePref = "987654321";
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES)
                    .put(ApplicationPreferenceKeys.FONT_SIZE, fontSizePref);
            assertEquals(FontUtilities.getApplicationFontSize(),
                    Integer.parseInt(fontSizePref));
        } finally {
            // clean up, remove Preferences node this test plays with
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();
        }
    }

    /**
     * While retrieving the user's default font size a BackingStoreException or NumberFormatException is thrown,
     * resulting in the default font size returned and the generation of a log message.
     *
     * @throws IOException if logs can't be captured
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetApplicationFontSizeException() throws IOException, BackingStoreException {
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);

        // keep the original application Preference so it can be restored later
        final String defaultFontSize = ApplicationPreferenceKeys.FONT_SIZE_DEFAULT;

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            final String dummyPref = "dummy";
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES)
                    .put(ApplicationPreferenceKeys.FONT_SIZE, dummyPref);

            // BackingStoreException when accessing Preferences
            int fontSizeBse = 45698711;
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_SIZE, Integer.toString(fontSizeBse));

            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(
                    new BorkedPreferences());

            // Expect defualt size because of exception
            assertEquals(FontUtilities.getApplicationFontSize(), Integer.parseInt(ApplicationPreferenceKeys.FONT_SIZE_DEFAULT));
            assertTrue(getCapturedLog().contains(NODE_NOT_EXIST_LOG_MSG));

            // NumberFormatException when parsing font size pref String
            int fontSizeNfe = 98712366;
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_SIZE, Integer.toString(fontSizeNfe));

            prefsMockedStatic.reset();
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);

            assertEquals(FontUtilities.getApplicationFontSize(), fontSizeNfe);

        } finally {
            // clean up, first remove Preferences node this test plays with
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();
            // and set the application Preference back to the original setting
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_SIZE, defaultFontSize);

            final String resultFontSize = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_SIZE, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);

            assertEquals(resultFontSize, ApplicationPreferenceKeys.FONT_SIZE_DEFAULT);
        }
    }

    /**
     * Can get the user's default font family.
     *
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetApplicationFontFamily() throws BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();

        // keep the original application default so it can be restored later
        final String defaultFontFamily = ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT;

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);

            // assert that the default value is returned when no Preference exists
            assertEquals(FontUtilities.getApplicationFontFamily(), ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);

            /* create the Preference and assert that when invoked, the 
               Preference is returned */
            final String testFontFamily2 = "another dummy font family";
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES)
                    .put(ApplicationPreferenceKeys.FONT_FAMILY, testFontFamily2);
            assertEquals(FontUtilities.getApplicationFontFamily(), testFontFamily2);
        } finally {
            // clean up, first remove Preferences node this test plays with
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).removeNode();
            // and set the application Preference back to the original setting
            p.node(ApplicationPreferenceKeys.FONT_PREFERENCES).put(ApplicationPreferenceKeys.FONT_FAMILY, defaultFontFamily);

            final String resultFontFamily = p.node(
                    ApplicationPreferenceKeys.FONT_PREFERENCES).get(ApplicationPreferenceKeys.FONT_FAMILY, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);

            assertEquals(resultFontFamily, ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
        }
    }

    /**
     * While retrieving the user's default font size a BackingStoreException is thrown, resulting in the default font
     * family returned and the generation of a log message.
     *
     * @throws IOException if logs can't be captured
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetApplicationFontFamilyException() throws IOException, BackingStoreException {
        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(new BorkedPreferences());

            assertEquals(FontUtilities.getApplicationFontFamily(), ApplicationPreferenceKeys.FONT_FAMILY_DEFAULT);
            assertTrue(getCapturedLog().contains(NODE_NOT_EXIST_LOG_MSG));
        }
    }

    /**
     * Can set the default output font size and family as Preferences.
     *
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testInitOutputFontPrefs() throws BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);

            /* Preferences will be created and set to default values if attempt 
               to get Preferences when they do not exist */
            FontUtilities.initialiseOutputFontPreferenceOnFirstUse();
            final String fontSize = p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE)
                    .get(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE, ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT);
            assertEquals(fontSize, ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT);

            /* as Preferences exist a second invocation does nothing, so to
               verify this manually set the Preference to a dummy value, then 
               assert it is not changed back to the default value */
            final String dummyPref = "dummy";
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).put(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE, dummyPref);
            FontUtilities.initialiseOutputFontPreferenceOnFirstUse();
            final String fontSize2 = p.node(
                    ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).get(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE, ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT);
            assertEquals(fontSize2, dummyPref);
        } finally {
            // clean up, remove Preferences nodes this test plays with
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();
        }
    }

    /**
     * While setting the default output font size and family a BackingStoreException is thrown, resulting in nothing
     * happening except for the generation of a log message.
     *
     * @throws IOException if logs can't be captured
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testInitOutputFontPrefsException() throws IOException, BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(
                    new BorkedPreferences());

            FontUtilities.initialiseOutputFontPreferenceOnFirstUse();
            assertFalse(p.nodeExists(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE));
            assertTrue(getCapturedLog().contains(BackingStoreException.class.getName()));
        }
    }

    /**
     * Can cache the font to be used for the output window.
     */
    @Test
    public void testGetOutputFont() {
        final int size = Integer.MAX_VALUE;
        final String family = "ridiculousFontFamily";
        try (final MockedStatic<FontUtilities> fontUtilsStatic = mockStatic(FontUtilities.class)) {
            fontUtilsStatic.when(() -> FontUtilities.getOutputFontSize()).thenReturn(size);
            fontUtilsStatic.when(() -> FontUtilities.getOutputFontFamily()).thenReturn(family);
            fontUtilsStatic.when(() -> FontUtilities.getOutputFont()).thenCallRealMethod();

            // first invocation places a Font in the cache
            final Font font = FontUtilities.getOutputFont();
            assertEquals(font.getName(), family);
            assertEquals(font.getStyle(), Font.PLAIN);
            assertEquals(font.getSize(), size);

            // second invocation gets the existing Font from the cache
            assertSame(FontUtilities.getOutputFont(), font);
        }
    }

    /**
     * Can get the default font size for the output window.
     *
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetOutputFontSize() throws BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class); final MockedStatic<UIManager> managerMockedStatic = mockStatic(UIManager.class)) {
            // set up mocks
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);
            final Font font = mock(Font.class);
            final int fontSizeUiManger = Integer.MAX_VALUE;
            when(font.getSize()).thenReturn(fontSizeUiManger);
            managerMockedStatic.when(() -> UIManager.getFont(any())).thenReturn(font);

            /* assert that the value from the UIManager mock is returned when no 
               Preference exists */
            assertEquals(FontUtilities.getOutputFontSize(), fontSizeUiManger);

            /* create the Preference and assert that when invoked, the 
               Preference is returned */
            final String fontSizePref = "987654321";
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE)
                    .put(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE, fontSizePref);
            assertEquals(FontUtilities.getOutputFontSize(), Integer.parseInt(fontSizePref));
        } finally {
            // clean up, remove Preferences node this test plays with
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();
        }
    }

    /**
     * While retrieving the default font size for the output window a BackingStoreException or NumberFormatException is
     * thrown, resulting in the default font size returned and the generation of a log message.
     *
     * @throws IOException if logs can't be captured
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetOutputFontSizeException() throws IOException, BackingStoreException {
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            final String dummyPref = "dummy";
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE)
                    .put(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE, dummyPref);

            // BackingStoreException when accessing Preferences
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(new BorkedPreferences());
            assertEquals(FontUtilities.getOutputFontSize(), Integer.parseInt(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT));
            assertTrue(getCapturedLog().contains(NODE_NOT_EXIST_LOG_MSG));

            // NumberFormatException when parsing font size pref String
            prefsMockedStatic.reset();
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);
            assertEquals(FontUtilities.getOutputFontSize(), Integer.parseInt(ApplicationPreferenceKeys.OUTPUT2_FONT_SIZE_DEFAULT));
            assertTrue(getCapturedLog().contains(
                    String.format(INT_PARSE_ERROR_PATTERN, dummyPref)));
        } finally {
            // clean up, remove Preferences node this test plays with
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();
        }
    }

    /**
     * Can get the default font family for the output window.
     *
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetOutputFontFamily() throws BackingStoreException {
        // set up by ensuring Preferences for this test do not exist
        final Preferences p = Preferences.userNodeForPackage(FontUtilitiesNGTest.class);
        p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();

        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(p);

            // assert that the default value is returned when no Preference exists
            assertEquals(FontUtilities.getOutputFontFamily(), ApplicationPreferenceKeys.OUTPUT2_FONT_FAMILY_DEFAULT);

            /* create the Preference and assert that when invoked, the 
               Preference is returned */
            final String testFontFamily = "dummy font family";
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE)
                    .put(ApplicationPreferenceKeys.OUTPUT2_FONT_FAMILY, testFontFamily);
            assertEquals(FontUtilities.getOutputFontFamily(), testFontFamily);
        } finally {
            // clean up, remove Preferences node this test plays with
            p.node(ApplicationPreferenceKeys.OUTPUT2_PREFERENCE).removeNode();
        }
    }

    /**
     * While retrieving the default font family for the output window a BackingStoreException is thrown, resulting in
     * the default font family returned and the generation of a log message.
     *
     * @throws IOException if logs can't be captured
     * @throws BackingStoreException if problems occur when accessing Preferences
     */
    @Test
    public void testGetOutputFontFamilyException() throws IOException, BackingStoreException {
        try (final MockedStatic<NbPreferences> prefsMockedStatic = mockStatic(NbPreferences.class)) {
            prefsMockedStatic.when(() -> NbPreferences.root()).thenReturn(new BorkedPreferences());
            assertEquals(FontUtilities.getOutputFontFamily(), ApplicationPreferenceKeys.OUTPUT2_FONT_FAMILY_DEFAULT);
            assertTrue(getCapturedLog().contains(NODE_NOT_EXIST_LOG_MSG));
        }
    }

    /**
     * Broken Preferences implementation to facilitate Exception testing.
     */
    public class BorkedPreferences extends Preferences {

        @Override
        public void put(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String get(String string, String string1) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void remove(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clear() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void putInt(String string, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getInt(String string, int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void putLong(String string, long l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getLong(String string, long l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void putBoolean(String string, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean getBoolean(String string, boolean bln) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void putFloat(String string, float f) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float getFloat(String string, float f) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void putDouble(String string, double d) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getDouble(String string, double d) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void putByteArray(String string, byte[] bytes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public byte[] getByteArray(String string, byte[] bytes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] keys() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] childrenNames() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Preferences parent() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Preferences node(String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean nodeExists(String string) throws BackingStoreException {
            throw new BackingStoreException(NODE_NOT_EXIST_LOG_MSG);
        }

        @Override
        public void removeNode() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String name() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String absolutePath() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isUserNode() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toString() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void flush() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void sync() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addNodeChangeListener(NodeChangeListener ncl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeNodeChangeListener(NodeChangeListener ncl) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void exportNode(OutputStream out) throws IOException, BackingStoreException {
            throw new BackingStoreException("Not supported yet.");
        }

        @Override
        public void exportSubtree(OutputStream out) throws IOException, BackingStoreException {
            throw new BackingStoreException("Not supported yet.");
        }
    }
}
