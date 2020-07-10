/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.testng.Assert;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Recent Parameter Values Test.
 *
 * @author arcturus
 */
public class RecentParameterValuesNGTest {

    private String recentValues;

    public RecentParameterValuesNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        final File file = new File(this.getClass().getResource("./resources/pluginframework.properties").getFile());
        final BufferedReader in = new BufferedReader(new FileReader(file));
        String readLine = in.readLine();
        recentValues = readLine.replace("recentValues=", "");
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of storeRecentValue method, of class RecentParameterValues.
     */
    @Test
    public void testStoreRecentValue() {
        final String parameterId = "key";
        final String parameterValue = "[\"✓ Option 1\\\\n✓ Option 2\"]";

        RecentParameterValues.storeRecentValue(parameterId, parameterValue);
        List<String> result = RecentParameterValues.getRecentValues(parameterId);
        final List<String> expResult = new ArrayList<>();
        expResult.add(parameterValue);
        assertEquals(result, expResult);
        RecentParameterValues.storeRecentValue(parameterId, parameterValue);
        result = RecentParameterValues.getRecentValues(parameterId);
        assertEquals(result, expResult);
    }

    /**
     * Test of saveToPreferences method, of class RecentParameterValues.
     */
    @Test
    public void testSaveToPreferences() {
        final Preferences prefs = NbPreferences.forModule(RecentParameterValuesKey.class);
        prefs.put(RecentParameterValuesKey.RECENT_VALUES, recentValues);
        RecentParameterValues.loadFromPreference();
        RecentParameterValues.saveToPreferences();
        final List<String> result = RecentParameterValues.getRecentValues("TestChainer.planets");
        final String expResult = "Mercury\\nVenus\\n✓ Earth\\nMars\\nJupiter\\nSaturn\\nUranus\\nNeptune\\nCoruscant";
        Assert.assertEquals(result.get(0), expResult);
    }

    @Test
    public void testSaveToPreferencesWithUtf8Ticks() {
        final Preferences prefs = NbPreferences.forModule(RecentParameterValuesKey.class);
        final String utf8Json = "{\"foo\":[\"✓ Newline ✓ Comma Whitespace\"]}";
        prefs.put(RecentParameterValuesKey.RECENT_VALUES, utf8Json);
        RecentParameterValues.loadFromPreference();
        RecentParameterValues.saveToPreferences();
        final List<String> result = RecentParameterValues.getRecentValues("foo");
        final String expResult = "✓ Newline ✓ Comma Whitespace";
        Assert.assertEquals(result.get(0), expResult);
    }

    /**
     * Test of loadFromPreference method, of class RecentParameterValues.
     *
     * @throws java.io.IOException
     * @throws java.util.prefs.BackingStoreException
     */
    @Test
    public void testLoadFromPreference() throws IOException, BackingStoreException {
        final Preferences prefs = NbPreferences.forModule(RecentParameterValuesKey.class);
        prefs.put(RecentParameterValuesKey.RECENT_VALUES, recentValues);
        RecentParameterValues.loadFromPreference();
        final List<String> result = RecentParameterValues.getRecentValues("TestChainer.planets");
        final String expResult = "Mercury\\nVenus\\n✓ Earth\\nMars\\nJupiter\\nSaturn\\nUranus\\nNeptune\\nCoruscant";
        Assert.assertEquals(result.get(0), expResult);
    }

    @Test
    public void testLoadFromPreferenceWithUtf8Ticks() throws IOException, BackingStoreException {
        final Preferences prefs = NbPreferences.forModule(RecentParameterValuesKey.class);
        final String utf8Json = "{\"foo\":[\"✓ Newline ✓ Comma Whitespace\"]}";
        prefs.put(RecentParameterValuesKey.RECENT_VALUES, utf8Json);
        RecentParameterValues.loadFromPreference();
        final List<String> result = RecentParameterValues.getRecentValues("foo");
        final String expResult = "✓ Newline ✓ Comma Whitespace";
        Assert.assertEquals(result.get(0), expResult);
    }

//    @Test
//    public void testByteArrayOutputStreamContainingUtf8() throws IOException, BackingStoreException {
//        JsonFactory jsonFactory = new MappingJsonFactory();
//        final String utf8String = "✓ Newline";
//        final ByteArrayOutputStream json = new ByteArrayOutputStream();
//        final JsonGenerator jg = jsonFactory.createGenerator(json);
//        jg.writeString(utf8String);
//        jg.flush();
//
//        final Preferences prefs = NbPreferences.forModule(RecentParameterValuesKey.class);
//        prefs.put(RecentParameterValuesKey.RECENT_VALUES, json.toString(StandardCharsets.UTF_8.name()));
//        prefs.flush();
//
//        final String recentValuesJSON = prefs.get(RecentParameterValuesKey.RECENT_VALUES, "");
//
//        final String expResult = "\"✓ Newline\"";
//        Assert.assertEquals(recentValuesJSON, expResult);
//    }
}
