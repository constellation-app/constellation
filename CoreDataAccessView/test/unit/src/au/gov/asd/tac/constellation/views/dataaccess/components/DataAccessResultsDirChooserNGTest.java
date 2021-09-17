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
package au.gov.asd.tac.constellation.views.dataaccess.components;

import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.io.File;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.openide.filesystems.FileChooserBuilder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class DataAccessResultsDirChooserNGTest {
    
    
    @Test
    public void init() {
        final DataAccessResultsDirChooser chooser = spy(new DataAccessResultsDirChooser());
        assertNotNull(chooser.getFileChooser());
        
        final FileChooserBuilder fileChooser = mock(FileChooserBuilder.class);
        doReturn(fileChooser).when(chooser).getFileChooser();
        
        final File expectedSelectedDir = new File("/tmp");
        when(fileChooser.showSaveDialog()).thenReturn(expectedSelectedDir);
        
        try (final MockedStatic<DataAccessPreferenceUtilities> prefUtilsMockedStatic =
                Mockito.mockStatic(DataAccessPreferenceUtilities.class)) {
                final File selectedDir = chooser.openAndSaveToPreferences();
                
                assertEquals(selectedDir, expectedSelectedDir);
                prefUtilsMockedStatic.verify(() -> DataAccessPreferenceUtilities
                        .setDataAccessResultsDir(expectedSelectedDir));
        }
        
    }
}
