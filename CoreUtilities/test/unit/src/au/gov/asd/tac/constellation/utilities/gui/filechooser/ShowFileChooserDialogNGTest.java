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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
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
        
        assertNull(showFileChooserDialog.getSelectedFile());
        
        showFileChooserDialog.run();
        
        assertSame(showFileChooserDialog.getSelectedFile(), file);
        
        verify(fileChooserBuilder).showSaveDialog();
    }
    
    @Test
    public void run_open_open() {
        final FileChooserBuilder fileChooserBuilder = mock(FileChooserBuilder.class);
        final File file = mock(File.class);
        
        when(fileChooserBuilder.showOpenDialog()).thenReturn(file);
        
        final ShowFileChooserDialog showFileChooserDialog = new ShowFileChooserDialog(
                fileChooserBuilder, FileChooserMode.OPEN);
        
        assertNull(showFileChooserDialog.getSelectedFile());
        
        showFileChooserDialog.run();
        
        assertSame(showFileChooserDialog.getSelectedFile(), file);
        
        verify(fileChooserBuilder).showOpenDialog();
    }
}
