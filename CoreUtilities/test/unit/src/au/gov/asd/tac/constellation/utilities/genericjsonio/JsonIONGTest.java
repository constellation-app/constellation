/*
 * Copyright 2010-2021 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.genericjsonio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class JsonIONGTest {
    public JsonIONGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        FxToolkit.cleanupStages();
    }
    
    @Test
    public void loadJsonPreferences_get_pojo() throws URISyntaxException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            final Optional<String> subDirectory = Optional.of("test");
            final Optional<String> filePrefix = Optional.of("my-");
            
            jsonIoDialogMockedStatic.when(() -> JsonIODialog.getSelection(List.of("preferences"), subDirectory, filePrefix))
                    .thenReturn(Optional.of("preferences"));
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(subDirectory))
                    .thenReturn(new File(JsonIONGTest.class.getResource("resources").toURI()));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .loadJsonPreferences(subDirectory, filePrefix, MyPreferences.class))
                    .thenCallRealMethod();
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .loadJsonPreferences(eq(subDirectory), eq(filePrefix), any(Function.class)))
                    .thenCallRealMethod();
            
            final MyPreferences loadedPreferences = JsonIO
                    .loadJsonPreferences(subDirectory, filePrefix, MyPreferences.class);
            
            final MyPreferences expectedPreferences = new MyPreferences();
            expectedPreferences.setName("Joe Bloggs");
            expectedPreferences.setVolume(5);
            
            assertEquals(loadedPreferences, expectedPreferences);
        }
    }
    
    @Test
    public void loadJsonPreferences_get_tree() throws URISyntaxException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            final Optional<String> subDirectory = Optional.of("test");
            final Optional<String> filePrefix = Optional.empty();
            
            jsonIoDialogMockedStatic.when(() -> JsonIODialog.getSelection(List.of("my-preferences"), subDirectory, filePrefix))
                    .thenReturn(Optional.of("my-preferences"));
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(subDirectory))
                    .thenReturn(new File(JsonIONGTest.class.getResource("resources").toURI()));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .loadJsonPreferences(subDirectory, filePrefix))
                    .thenCallRealMethod();
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .loadJsonPreferences(eq(subDirectory), eq(filePrefix), any(Function.class)))
                    .thenCallRealMethod();
            
            final JsonNode loadedPreferences = JsonIO
                    .loadJsonPreferences(subDirectory, filePrefix);
            
            final JsonNode expectedJsonNode = new ObjectMapper()
                    .createObjectNode()
                    .put("name", "Joe Bloggs")
                    .put("volume", 5);
            
            assertEquals(loadedPreferences, expectedJsonNode);
            
        }
    }
    
    @Test
    public void saveJsonPreferences() throws URISyntaxException, FileNotFoundException, IOException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
            
            // Ensure there is not old test data lying around
            outputFile.delete();
            
            final Optional<String> subDirectory = Optional.of("test");
            final Optional<String> filePrefix = Optional.of("my-");
            
            jsonIoDialogMockedStatic.when(JsonIODialog::getPreferenceFileName).thenReturn(Optional.of("preferences"));
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(subDirectory))
                    .thenReturn(new File(System.getProperty("java.io.tmpdir")));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .saveJsonPreferences(eq(subDirectory), any(ObjectMapper.class), any(Object.class), eq(filePrefix)))
                    .thenCallRealMethod();
            
            final MyPreferences myPreferences = new MyPreferences();
            myPreferences.setName("Joe Bloggs");
            myPreferences.setVolume(5);
            
            JsonIO.saveJsonPreferences(subDirectory, new ObjectMapper(), myPreferences, filePrefix);
                       
            final String output = IOUtils.toString(
                    new FileInputStream(outputFile), StandardCharsets.UTF_8);
            
            final String expectedOutput = IOUtils.toString(
                    new FileInputStream(new File(JsonIONGTest.class.getResource("resources/my-preferences.json").toURI())), StandardCharsets.UTF_8);
            
            // Clean up
            outputFile.delete();
            
            assertEquals(output, expectedOutput);
        }
    }
    
    @Test
    public void saveJsonPreferences_pref_dir_not_a_dir() throws URISyntaxException, FileNotFoundException, IOException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
            outputFile.delete();
            
            final Optional<String> subDirectory = Optional.of("test");
            final Optional<String> filePrefix = Optional.of("my-");
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(subDirectory))
                    .thenReturn(new File(System.getProperty("java.io.tmpdir") + "/samplefile"));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .saveJsonPreferences(eq(subDirectory), any(ObjectMapper.class), any(Object.class), eq(filePrefix)))
                    .thenCallRealMethod();
            
            JsonIO.saveJsonPreferences(subDirectory, null, null, filePrefix);
                       
            jsonIoDialogMockedStatic.verifyNoInteractions();
        }
    }
    
    @Test
    public void saveJsonPreferences_user_cancels() throws URISyntaxException, FileNotFoundException, IOException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
            
            // Ensure there is not old test data lying around
            outputFile.delete();
            
            final Optional<String> subDirectory = Optional.of("test");
            final Optional<String> filePrefix = Optional.of("my-");
            
            jsonIoDialogMockedStatic.when(JsonIODialog::getPreferenceFileName).thenReturn(Optional.empty());
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(subDirectory))
                    .thenReturn(new File(System.getProperty("java.io.tmpdir")));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .saveJsonPreferences(eq(subDirectory), any(ObjectMapper.class), any(Object.class), eq(filePrefix)))
                    .thenCallRealMethod();
            
            JsonIO.saveJsonPreferences(subDirectory, null, null, filePrefix);
            
            assertFalse(outputFile.exists());
            
            // Clean up
            outputFile.delete();
            
            
        }
    }
    
    @Test
    public void deleteJsonPreferences() throws URISyntaxException, FileNotFoundException, IOException {
        
        try (MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class)) {
            
            final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
            
            // Ensure there is not old test data lying around
            outputFile.delete();
            
            outputFile.createNewFile();
            
            assertTrue(outputFile.exists());
            
            final Optional<String> subDirectory = Optional.of("test");
            final Optional<String> filePrefix = Optional.of("my-");
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(subDirectory))
                    .thenReturn(new File(System.getProperty("java.io.tmpdir")));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .deleteJsonPreference(eq("preferences"), eq(subDirectory), eq(filePrefix)))
                    .thenCallRealMethod();
            
            JsonIO.deleteJsonPreference("preferences", subDirectory, filePrefix);
            
            assertFalse(outputFile.exists());
        }
    }
    
    static class MyPreferences {
        private String name;
        private int volume;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVolume() {
            return volume;
        }

        public void setVolume(int volume) {
            this.volume = volume;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final MyPreferences rhs = (MyPreferences) o;

            return new EqualsBuilder()
                    .append(getName(), rhs.getName())
                    .append(getVolume(), rhs.getVolume())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(getName())
                    .append(getVolume())
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("name", getName())
                    .append("volume", getVolume())
                    .toString();
        }
    }
}
