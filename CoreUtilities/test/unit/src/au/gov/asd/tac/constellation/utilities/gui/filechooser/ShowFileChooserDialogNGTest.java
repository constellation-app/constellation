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
package au.gov.asd.tac.constellation.utilities.gui.filechooser;

import java.io.File;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class ShowFileChooserDialogNGTest {
    
    @Test
    public void run_open_save() {
        final FileChooserBuilder fileChooserBuilder = mock(FileChooserBuilder.class);
        final File file = mock(File.class);
        
        when(fileChooserBuilder.showSaveDialog()).thenReturn(file);
        
        final ShowFileChooserDialog showFileChooserDialog = new ShowFileChooserDialog(
                fileChooserBuilder, FileChooserMode.SAVE);
        
        assertEquals(showFileChooserDialog.getSelectedFiles(), Optional.empty());
        
        showFileChooserDialog.run();
        
        assertEquals(showFileChooserDialog.getSelectedFiles().get(), List.of(file));
        
        verify(fileChooserBuilder).showSaveDialog();
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
