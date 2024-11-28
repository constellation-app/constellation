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
package au.gov.asd.tac.constellation.utilities.keyboardshortcut;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import javafx.scene.control.Dialog;
import org.apache.commons.io.IOUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterClass;

/**
 *
 * @author spica
 */
public class RecordKeyboardShortcutNGTest {  

    @Test
    public void test_keyboardShortCutAlreadyAssigned() throws Exception {

        final File outputFile = new File(System.getProperty("java.io.tmpdir") + "/my-preferences.json");

        try {
            outputFile.createNewFile();

            String keyboardShortcut = "ctrl 1";
            MockedStatic<RecordKeyboardShortcut> recordKeyboardShortcutDialogMockedStatic = Mockito.mockStatic(RecordKeyboardShortcut.class);

            setupStaticMocksForKeyboardShortCutAlreadyAssigned(recordKeyboardShortcutDialogMockedStatic, outputFile, keyboardShortcut);

            File file = RecordKeyboardShortcut.keyboardShortCutAlreadyAssigned(outputFile, keyboardShortcut);

              assertEquals(outputFile, file);

        } finally {
            Files.deleteIfExists(outputFile.toPath());
        }

    }

    private void setupStaticMocksForKeyboardShortCutAlreadyAssigned(final MockedStatic<RecordKeyboardShortcut> recordKeyboardShortcutMockedStatic,
            File outputFile, final String keyboardShortcut) {

        recordKeyboardShortcutMockedStatic.when(() -> RecordKeyboardShortcut.keyboardShortCutAlreadyAssigned(outputFile, keyboardShortcut))
                .thenReturn(outputFile);

    }  

}
