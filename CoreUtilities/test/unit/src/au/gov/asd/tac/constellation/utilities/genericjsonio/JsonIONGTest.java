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

import au.gov.asd.tac.constellation.utilities.file.FilenameEncoder;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testfx.util.NodeQueryUtils.hasText;
import org.testfx.util.WaitForAsyncUtils;
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
    private static final Optional<String> SUB_DIRECTORY = Optional.of("test");
    private static final Optional<String> FILE_PREFIX = Optional.of("my-");
    
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
    public void loadJsonPreferences_get_pojo_without_prefix() throws URISyntaxException, FileNotFoundException, IOException {
        
        try (MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class)) {
            jsonIoMockedStatic.when(() -> JsonIO
                .loadJsonPreferences(any(Optional.class), any(Class.class)))
                .thenCallRealMethod();
            
            JsonIO.loadJsonPreferences(SUB_DIRECTORY, MyPreferences.class);
            
            jsonIoMockedStatic.verify(() -> JsonIO
                .loadJsonPreferences(SUB_DIRECTORY, Optional.empty(), MyPreferences.class));
        }
    }
    
    @Test
    public void loadJsonPreferences_get_tree_without_prefix() throws URISyntaxException, FileNotFoundException, IOException {
        
        try (MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class)) {
            jsonIoMockedStatic.when(() -> JsonIO
                .loadJsonPreferences(any(Optional.class)))
                .thenCallRealMethod();
            
            JsonIO.loadJsonPreferences(SUB_DIRECTORY);
            
            jsonIoMockedStatic.verify(() -> JsonIO
                .loadJsonPreferences(SUB_DIRECTORY, Optional.empty()));
        }
    }
    
    @Test
    public void loadJsonPreferences_get_pojo() throws URISyntaxException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class, Mockito.CALLS_REAL_METHODS);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            jsonIoDialogMockedStatic.when(() -> JsonIODialog.getSelection(List.of("preferences"), SUB_DIRECTORY, FILE_PREFIX))
                    .thenReturn(Optional.of("preferences"));
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(SUB_DIRECTORY))
                    .thenReturn(new File(JsonIONGTest.class.getResource("resources").toURI()));
            
            final MyPreferences loadedPreferences = JsonIO
                    .loadJsonPreferences(SUB_DIRECTORY, FILE_PREFIX, MyPreferences.class);
            
            assertEquals(loadedPreferences, fixture());
        }
    }
    
    @Test
    public void loadJsonPreferences_pref_dir_not_a_dir() throws URISyntaxException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class, Mockito.CALLS_REAL_METHODS);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            // The returned preference directory is not a directory so the UI is
            // opened with an empty list and the user hits cancel.
            jsonIoDialogMockedStatic.when(() -> JsonIODialog.getSelection(Collections.emptyList(), SUB_DIRECTORY, FILE_PREFIX))
                    .thenReturn(Optional.empty());
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(SUB_DIRECTORY))
                    .thenReturn(new File(System.getProperty("java.io.tmpdir") + "/samplefile"));
            
            final MyPreferences loadedPreferences = JsonIO
                    .loadJsonPreferences(SUB_DIRECTORY, FILE_PREFIX, MyPreferences.class);
            
            assertEquals(loadedPreferences, null);
        }
    }
    
    @Test
    public void loadJsonPreferences_get_tree() throws URISyntaxException {
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class, Mockito.CALLS_REAL_METHODS);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            
            final Optional<String> filePrefix = Optional.empty();
            
            jsonIoDialogMockedStatic.when(() -> JsonIODialog.getSelection(List.of("my-preferences"), SUB_DIRECTORY, filePrefix))
                    .thenReturn(Optional.of("my-preferences"));
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(SUB_DIRECTORY))
                    .thenReturn(new File(JsonIONGTest.class.getResource("resources").toURI()));
            
            final JsonNode loadedPreferences = JsonIO
                    .loadJsonPreferences(SUB_DIRECTORY, filePrefix);
            
            final JsonNode expectedJsonNode = new ObjectMapper()
                    .createObjectNode()
                    .put("name", "Joe Bloggs")
                    .put("volume", 5);
            
            assertEquals(loadedPreferences, expectedJsonNode);
        }
    }
    
    @Test
    public void saveJsonPreferences() throws URISyntaxException, FileNotFoundException, IOException {
        
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            setupStaticMocksForSavePreference(jsonIoMockedStatic, jsonIoDialogMockedStatic, Optional.of("preferences"));
            
            JsonIO.saveJsonPreferences(SUB_DIRECTORY, new ObjectMapper(), fixture(), FILE_PREFIX);
                       
            verifyOutputFileMatchesFixture(outputFile);
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void saveJsonPreferences_without_prefix() throws URISyntaxException, FileNotFoundException, IOException {
        
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try (MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class)) {
            jsonIoMockedStatic.when(() -> JsonIO
                .saveJsonPreferences(any(Optional.class), any(ObjectMapper.class), any()))
                .thenCallRealMethod();
            
            final ObjectMapper mapper = new ObjectMapper();
            
            JsonIO.saveJsonPreferences(SUB_DIRECTORY, mapper, fixture());
                       
            jsonIoMockedStatic.verify(() -> JsonIO
                .saveJsonPreferences(SUB_DIRECTORY, mapper, fixture(), Optional.empty()));
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void saveJsonPreferences_file_exists_dont_write() throws URISyntaxException, FileNotFoundException, IOException, InterruptedException, ExecutionException {
        final FxRobot robot = new FxRobot();
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try {
            outputFile.createNewFile();
            
            final Future<Void> future = WaitForAsyncUtils.asyncFx(() -> {
                try (
                    final MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                    final MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
                ) {
                    setupStaticMocksForSavePreference(jsonIoMockedStatic, jsonIoDialogMockedStatic, Optional.of("preferences"));

                    JsonIO.saveJsonPreferences(SUB_DIRECTORY, new ObjectMapper(), fixture(), FILE_PREFIX);
                }
            });
            
            final Stage dialog = getDialog(robot);
            
            robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("Cancel"))
                .queryAs(Button.class));
            
            WaitForAsyncUtils.waitFor(future);
            
            final String output = IOUtils.toString(
                new FileInputStream(outputFile), StandardCharsets.UTF_8);
            
            assertTrue(output.isBlank());
            
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void saveJsonPreferences_file_exists_overwrite() throws URISyntaxException, FileNotFoundException, IOException, InterruptedException, ExecutionException {
        final FxRobot robot = new FxRobot();
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try {
            outputFile.createNewFile();
            
            final Future<Void> future = WaitForAsyncUtils.asyncFx(() -> {
                try (
                    final MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                    final MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
                ) {
                    setupStaticMocksForSavePreference(jsonIoMockedStatic, jsonIoDialogMockedStatic, Optional.of("preferences"));

                    JsonIO.saveJsonPreferences(SUB_DIRECTORY, new ObjectMapper(), fixture(), FILE_PREFIX);
                }
            });
            
            final Stage dialog = getDialog(robot);
            
            robot.clickOn(robot.from(dialog.getScene().getRoot())
                .lookup(".button")
                .lookup(hasText("OK"))
                .queryAs(Button.class));
            
            WaitForAsyncUtils.waitFor(future);
            
            verifyOutputFileMatchesFixture(outputFile);
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void saveJsonPreferences_no_name_provided() throws URISyntaxException, FileNotFoundException, IOException {
        
        final Instant fakeNow = Instant.parse("2020-01-01T00:00:00.00Z");
        final String expectedDateTimeString = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                .withZone(ZoneId.systemDefault()).format(fakeNow);
        
        // Because the user enters an empty string, the file name is a
        // combination of the user name and current date time
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/" 
                + FilenameEncoder.encode(
                        "my-" + System.getProperty("user.name") + " at "
                                + expectedDateTimeString + ".json"
                )
        );
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
                MockedStatic<Instant> instantMockedStatic = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS);
            ) {
            instantMockedStatic.when(Instant::now).thenReturn(fakeNow);
            
            setupStaticMocksForSavePreference(jsonIoMockedStatic, jsonIoDialogMockedStatic, Optional.of("   "));
            
            JsonIO.saveJsonPreferences(SUB_DIRECTORY, new ObjectMapper(), fixture(), FILE_PREFIX);

            verifyOutputFileMatchesFixture(outputFile);
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void saveJsonPreferences_pref_dir_not_a_dir() throws URISyntaxException, FileNotFoundException, IOException {
        
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
                MockedStatic<NotifyDisplayer> notifyDisplayerMockedStatic = Mockito.mockStatic(NotifyDisplayer.class);
            ) {
            final File preferenceDirectory = new File(System.getProperty("java.io.tmpdir") + "/samplefile");
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(SUB_DIRECTORY))
                    .thenReturn(preferenceDirectory);
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .saveJsonPreferences(any(Optional.class), any(ObjectMapper.class), any(), any(Optional.class)))
                    .thenCallRealMethod();
            
            JsonIO.saveJsonPreferences(SUB_DIRECTORY, new ObjectMapper(), new Object(), FILE_PREFIX);
            
            // Verify no JSON IO dialogs were opened
            jsonIoDialogMockedStatic.verifyNoInteractions();
            
            // Verify the correct error dialog was presented
            notifyDisplayerMockedStatic.verify(() -> NotifyDisplayer.display("Can't create preference directory '"
                    + preferenceDirectory + "'.", NotifyDescriptor.ERROR_MESSAGE));
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void saveJsonPreferences_user_cancels() throws URISyntaxException, FileNotFoundException, IOException {
        
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<JsonIODialog> jsonIoDialogMockedStatic = Mockito.mockStatic(JsonIODialog.class);
            ) {
            setupStaticMocksForSavePreference(jsonIoMockedStatic, jsonIoDialogMockedStatic, Optional.empty());
            
            JsonIO.saveJsonPreferences(SUB_DIRECTORY, new ObjectMapper(), new Object(), FILE_PREFIX);
            
            assertFalse(outputFile.exists());
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void deleteJsonPreferences() throws URISyntaxException, FileNotFoundException, IOException {
        
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try (MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class)) {
            outputFile.createNewFile();
            
            assertTrue(outputFile.exists());
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(SUB_DIRECTORY))
                    .thenReturn(new File(System.getProperty("java.io.tmpdir")));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .deleteJsonPreference(eq("preferences"), eq(SUB_DIRECTORY), eq(FILE_PREFIX)))
                    .thenCallRealMethod();
            
            JsonIO.deleteJsonPreference("preferences", SUB_DIRECTORY, FILE_PREFIX);
            
            assertFalse(outputFile.exists());
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    @Test
    public void deleteJsonPreferences_fails() throws URISyntaxException, FileNotFoundException, IOException {
        
        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");
        
        try (
                MockedStatic<JsonIO> jsonIoMockedStatic = Mockito.mockStatic(JsonIO.class);
                MockedStatic<NotifyDisplayer> notifyDisplayerMockedStatic = Mockito.mockStatic(NotifyDisplayer.class);
                MockedStatic<Files> filesMockedStatic = Mockito.mockStatic(Files.class);
            ) {
            filesMockedStatic.when(() -> Files.deleteIfExists(outputFile.toPath())).thenThrow(new SecurityException("Some error"));
            
            jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(SUB_DIRECTORY))
                    .thenReturn(new File(System.getProperty("java.io.tmpdir")));
            
            jsonIoMockedStatic.when(() -> JsonIO
                    .deleteJsonPreference(eq("preferences"), eq(SUB_DIRECTORY), eq(FILE_PREFIX)))
                    .thenCallRealMethod();
            
            JsonIO.deleteJsonPreference("preferences", SUB_DIRECTORY, FILE_PREFIX);
            
            assertFalse(outputFile.exists());
            notifyDisplayerMockedStatic.verify(() -> NotifyDisplayer.display("Failed to delete file my-preferences.json from disk", NotifyDescriptor.ERROR_MESSAGE));
        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }
    }
    
    /**
     * Sets up common mock requirements for the save preference tests.
     *
     * @param jsonIoMockedStatic static mock for JsonIO
     * @param jsonIoDialogMockedStatic static mock for JsonIODialog
     * @param userResponse the expected user input for file name selection
     */
    private void setupStaticMocksForSavePreference(final MockedStatic<JsonIO> jsonIoMockedStatic,
                                                   final MockedStatic<JsonIODialog> jsonIoDialogMockedStatic,
                                                   final Optional<String> userResponse) {
        jsonIoDialogMockedStatic.when(JsonIODialog::getPreferenceFileName)
                .thenReturn(userResponse);
            
        jsonIoMockedStatic.when(() -> JsonIO.getPrefereceFileDirectory(SUB_DIRECTORY))
                .thenReturn(new File(System.getProperty("java.io.tmpdir")));

        jsonIoMockedStatic.when(() -> JsonIO
                .saveJsonPreferences(any(Optional.class), any(ObjectMapper.class), any(), any(Optional.class)))
                .thenCallRealMethod();
    }
    
    /**
     * Get a representation of the JSON file in the resources package.
     *
     * @return the {@link MyPreferences} that represents the JSON file
     */
    private MyPreferences fixture() {
        final MyPreferences myPreferences = new MyPreferences();
        myPreferences.setName("Joe Bloggs");
        myPreferences.setVolume(5);
        
        return myPreferences;
    }
    
    /**
     * Verify that the passed output file has the same contents as the JSON file
     * {@code resources/my-preferences.json}.
     *
     * @param outputFile the file to compare with the fixture
     * @throws IOException if there is an issue reading the files
     * @throws URISyntaxException if there is an issue locating the fixture file
     */
    private void verifyOutputFileMatchesFixture(final File outputFile) throws IOException, URISyntaxException {
        final String output = IOUtils.toString(
                new FileInputStream(outputFile), StandardCharsets.UTF_8);

        final String expectedOutput = IOUtils.toString(
                new FileInputStream(new File(
                        JsonIONGTest.class.getResource("resources/my-preferences.json").toURI()
                )), StandardCharsets.UTF_8
        );

        assertEquals(output, expectedOutput);
    }
    
    /**
     * Get a dialog that has been displayed to the user. This will iterate through
     * all open windows and identify one that is modal. The assumption is that there
     * will only ever be one dialog open.
     * <p/>
     * If a dialog is not found then it will wait for the JavaFX thread queue to empty
     * and try again.
     *
     * @param robot the FX robot for these tests
     * @return the found dialog
     */
    private Stage getDialog(final FxRobot robot) {
        Stage dialog = null;
        while(dialog == null) {
            dialog = robot.robotContext().getWindowFinder().listWindows().stream()
                        .filter(window -> window instanceof javafx.stage.Stage)
                        .map(window -> (javafx.stage.Stage) window)
                        .filter(stage -> stage.getModality() == Modality.APPLICATION_MODAL)
                        .findFirst()
                        .orElse(null);
            
            if (dialog == null) {
                WaitForAsyncUtils.waitForFxEvents();
            }
        }
        return dialog;
    }
    
    /**
     * Test POJO used for verifying the serialization and de-serialization components.
     */
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
