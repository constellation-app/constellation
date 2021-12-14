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
package au.gov.asd.tac.constellation.utilities.support;

import au.gov.asd.tac.constellation.utilities.gui.filechooser.FileChooser;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.openide.filesystems.FileChooserBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for SupportPackageAction.
 *
 * @author sol695510
 */
public class SupportPackageActionNGTest {

    private static MockedStatic<FileChooser> fileChooserStaticMock;

    public SupportPackageActionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        fileChooserStaticMock = Mockito.mockStatic(FileChooser.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        fileChooserStaticMock.close();
    }

    /**
     * Test of actionPerformed method, of class SupportPackageAction.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("testActionPerformed");

        final SupportPackageAction instance = new SupportPackageAction();
        final ActionEvent e = null;

        final String title = Bundle.MSG_SaveAsTitle();
        final File savedDirectory = FileChooser.DEFAULT_DIRECTORY;
        final FileNameExtensionFilter filter = null;

        final File file = spy(new File("testDir\'"));
        final Optional<File> optionalFile = Optional.ofNullable(file);

        fileChooserStaticMock.when(()
                -> FileChooser.getBaseFileChooserBuilder(
                        title,
                        savedDirectory,
                        filter))
                .thenCallRealMethod();

        fileChooserStaticMock.when(()
                -> FileChooser.openOpenDialog(Mockito.any(FileChooserBuilder.class)))
                .thenReturn(CompletableFuture.completedFuture(optionalFile));

        instance.actionPerformed(e);

        verify(file, times(1)).getPath();
    }
}
