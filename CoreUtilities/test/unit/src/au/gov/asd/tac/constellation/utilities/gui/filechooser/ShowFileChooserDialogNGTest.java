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
package au.gov.asd.tac.constellation.utilities.gui.filechooser;

import java.io.File;
import java.util.List;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class ShowFileChooserDialogNGTest extends ConstellationTest {
    
    @Test
    public void run_open_save() {
        try (final MockedStatic<WindowManager> windowManagerMockedStatic = Mockito.mockStatic(WindowManager.class);
                final MockedStatic<TopComponent> topComponentMockedStatic = Mockito.mockStatic(TopComponent.class)) {
            final WindowManager windowManager = mock(WindowManager.class);
            windowManagerMockedStatic.when(WindowManager::getDefault).thenReturn(windowManager);
            final JFrame frame = mock(JFrame.class);
            when(windowManager.getMainWindow()).thenReturn(frame);
            
            final Registry registry = mock(Registry.class);
            when(registry.getActivated()).thenReturn(null);
            topComponentMockedStatic.when(() -> TopComponent.getRegistry()).thenReturn(registry);
            
            final FileChooserBuilder fileChooserBuilder = mock(FileChooserBuilder.class);
            final File file = mock(File.class);

            final ShowFileChooserDialog showFileChooserDialog = new ShowFileChooserDialog(
                    fileChooserBuilder, FileChooserMode.SAVE);

            assertEquals(showFileChooserDialog.getSelectedFiles(), Optional.empty());

            final JFileChooser jFileChooser = mock(JFileChooser.class);
            // Ensure the return value indicates the file selection was approved and not cancelled
            when(jFileChooser.showSaveDialog(any())).thenReturn(0);
            when(jFileChooser.getSelectedFile()).thenReturn(file);
            when(fileChooserBuilder.createFileChooser()).thenReturn(jFileChooser);

            showFileChooserDialog.run();

            assertEquals(showFileChooserDialog.getSelectedFiles().get(), List.of(file));

            verify(jFileChooser).showSaveDialog(any());
        }
    }
    
    @Test
    public void run_open_open() {
        final FileChooserBuilder fileChooserBuilder = mock(FileChooserBuilder.class);
        final File file = mock(File.class);
        
        when(fileChooserBuilder.showOpenDialog()).thenReturn(file);
        
        final ShowFileChooserDialog showFileChooserDialog = new ShowFileChooserDialog(
                fileChooserBuilder, FileChooserMode.OPEN);
        
        assertEquals(showFileChooserDialog.getSelectedFiles(), Optional.empty());
        
        showFileChooserDialog.run();
        
        assertEquals(showFileChooserDialog.getSelectedFiles().get(), List.of(file));
        
        verify(fileChooserBuilder).showOpenDialog();
    }
    
    @Test
    public void run_open_multi() {
        final FileChooserBuilder fileChooserBuilder = mock(FileChooserBuilder.class);
        final File file1 = mock(File.class);
        final File file2 = mock(File.class);
        
        when(fileChooserBuilder.showMultiOpenDialog()).thenReturn(new File[] {file1, file2});
        
        final ShowFileChooserDialog showFileChooserDialog = new ShowFileChooserDialog(
                fileChooserBuilder, FileChooserMode.MULTI);
        
        assertEquals(showFileChooserDialog.getSelectedFiles(), Optional.empty());
        
        showFileChooserDialog.run();
        
        assertEquals(showFileChooserDialog.getSelectedFiles().get(), List.of(file1, file2));
        
        verify(fileChooserBuilder).showMultiOpenDialog();
    }
}
