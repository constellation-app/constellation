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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.utility;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.processing.RecordStoreUtilities;
import au.gov.asd.tac.constellation.graph.processing.TabularRecordStore;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.logging.ConstellationLoggerHelper;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.utilities.DataAccessPreferenceUtilities;
import java.io.File;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import org.openide.util.HelpCtx;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Save Results File Writer Test.
 *
 * @author arcturus, serpens24
 */
public class SaveResultsFileWriterNGTest {

    // Mocked dependencies
    private MockedStatic<DataAccessPreferenceUtilities> mockedDataAccessPreferenceKeys;
    private MockedStatic<RecordStoreUtilities> mockedRecordStoreUtilities;
    private MockedStatic<ConstellationLoggerHelper> mockedConstellationLoggerHelper;

    // Flags capturing validity checks
    private boolean fileCreated = true;  // Was the file with requesated filename/path created (well, does it exist).
    private String constellationLoggerHelperStatus = "";  // Status string passed to exportPropertyBuilder.
    private boolean dataValid = true;  // Does the contents of the saved file match the expected contents.

    @BeforeMethod
    public void setUpMethod() throws Exception {
        mockedDataAccessPreferenceKeys = mockStatic(DataAccessPreferenceUtilities.class);
        mockedRecordStoreUtilities = mockStatic(RecordStoreUtilities.class);
        mockedConstellationLoggerHelper = mockStatic(ConstellationLoggerHelper.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        mockedDataAccessPreferenceKeys.close();
        mockedRecordStoreUtilities.close();
        mockedConstellationLoggerHelper.close();
    }

    /**
     * Test of generateFilename method. Ensure that generated filename is of the
     * correct format yyyyMMddTHHmmssSSS.
     */
    @Test
    public void testGenerateFilename() {
        Plugin plugin = new ExampleClass();
        String filename = SaveResultsFileWriter.generateFilename(plugin, "xml");
        Pattern pattern = Pattern.compile("[0-9]{8}T[0-9]{9}-ExampleClass.xml");
        Matcher matcher = pattern.matcher(filename);
        Assert.assertTrue(matcher.matches(), "Testing generated filename matches pattern - yyyyMMddTHHmmssSSS.");
    }

    /**
     * Test of writeRecordStore method for case when
     * DataAccessPreferenceKeys.getDataAccessResultsDir() returns null. The
     * expectation is that no attempted writes are made. This test executes a
     * code path through SaveResultsFileWriter.writeRecordStore that does not
     * throw an exception.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testWriteRecordStoreNoDataAccessResultsDir() throws Exception {
        Plugin plugin = new ExampleClass();
        TabularRecordStore tabularRecordStore = new TabularRecordStore();

        mockedDataAccessPreferenceKeys.when(() -> DataAccessPreferenceUtilities.getDataAccessResultsDir()).thenReturn(null);
        SaveResultsFileWriter.writeRecordStore(plugin, tabularRecordStore);
        Assert.assertTrue(true, "Testing writing record to non existant directory - code will do nothing.");
    }

    /**
     * Test of writeRecordStore method for case when
     * DataAccessPreferenceKeys.getDataAccessResultsDir() returns an invalid
     * directory. Because directory (and hence generated file path) is invalid,
     * and exception will be thrown and
     * ConstellationLoggerHelper.exportPropertyBuilder called with appropriate
     * ERROR status.
     */
    @Test
    public void testWriteRecordStoreInvalidDataAccessResultsDir() {
        Plugin plugin = new ExampleClass();
        TabularRecordStore tabularRecordStore = new TabularRecordStore();

        mockedDataAccessPreferenceKeys.when(() -> DataAccessPreferenceUtilities.getDataAccessResultsDir()).thenReturn(new File("/BADDIR/"));
        mockedConstellationLoggerHelper.when(() -> ConstellationLoggerHelper.exportPropertyBuilder(any(), any(), any(), any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            File passedFile = (File) args[2];
            constellationLoggerHelperStatus = (String) args[3];

            // Ensure the file was not created
            fileCreated = passedFile.exists();
            if (fileCreated) {
                passedFile.delete();
            }

            // Return properties (which are ignored)
            Properties properties = new Properties();
            return properties;
        });
        try {
            SaveResultsFileWriter.writeRecordStore(plugin, tabularRecordStore);

            // Execution should not make it this far, an invalid file should result in an exception.
            Assert.fail("PluginException is expected.");

        } catch (Exception ex) {
            mockedRecordStoreUtilities.verify(times(0), () -> RecordStoreUtilities.toCsv(Mockito.any(), Mockito.any()));
            Assert.assertFalse(fileCreated, "Record store file was not created.");
            Assert.assertEquals(constellationLoggerHelperStatus, ConstellationLoggerHelper.FAILURE,
                    "ConstellationLoggerHelper passed status = FAILURE.");
        }
    }

    /**
     * Test of writeRecordStore method for case when
     * DataAccessPreferenceKeys.getDataAccessResultsDir() returns a valid
     * directory. Confirm file is created with expected contents and that no
     * exception is thrown. confirm
     * ConstellationLoggerHelper.exportPropertyBuilder called with appropriate
     * SUCCESS status. This test executes a code path through
     * SaveResultsFileWriter.writeRecordStore that does not throw an exception.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testWriteRecordStore() throws Exception {
        Plugin plugin = new ExampleClass();
        TabularRecordStore tabularRecordStore = new TabularRecordStore();
        String key = "TEST1KEY";
        String value = "TEST1VALUE";
        tabularRecordStore.add();
        tabularRecordStore.set(key, value);

        // This test actually creates the output file in user.home directory, checks its contents, and then removes it.
        mockedDataAccessPreferenceKeys.when(DataAccessPreferenceUtilities::getDataAccessResultsDir).thenReturn(new File(System.getProperty("user.home")));
        mockedRecordStoreUtilities.when(() -> RecordStoreUtilities.toCsv(Mockito.any(), Mockito.any())).thenCallRealMethod();
        mockedConstellationLoggerHelper.when(() -> ConstellationLoggerHelper.exportPropertyBuilder(any(), any(), any(), any())).thenAnswer((var invocation) -> {
            Object[] args = invocation.getArguments();
            File passedFile = (File) args[2];
            constellationLoggerHelperStatus = (String) args[3];

            // Ensure the file was created
            fileCreated = passedFile.exists();
            if (fileCreated) {
                // Read file and confirm it contains 3 rows
                int rows = 0;
                Scanner reader = new Scanner(passedFile);
                while (reader.hasNextLine()) {
                    rows++;
                    String data = reader.nextLine();
                    if (rows == 1) {
                        dataValid = dataValid && (data.equals(key));
                    }
                    if (rows == 2) {
                        dataValid = dataValid && (data.equals(value));
                    }
                    if (rows >= 3) {
                        dataValid = false;
                    }
                }
                if (rows == 0) {
                    dataValid = false;
                }
                passedFile.delete();
            }

            // Return properties (which are ignored)
            Properties properties = new Properties();
            return properties;
        });

        SaveResultsFileWriter.writeRecordStore(plugin, tabularRecordStore);
        mockedRecordStoreUtilities.verify(times(1), () -> RecordStoreUtilities.toCsv(Mockito.any(), Mockito.any()));

        Assert.assertTrue(fileCreated, "Record store file was created.");
        Assert.assertEquals(constellationLoggerHelperStatus, ConstellationLoggerHelper.SUCCESS,
                "ConstellationLoggerHelper passed status = SUCCESS.");
        Assert.assertTrue(dataValid, "Record contents matches expected.");
    }

    private static class ExampleClass implements Plugin {

        @Override
        public String getName() {
            // getName must return a string and not an exception as it is called during testing.
            return "ExampleClassName";
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String[] getTags() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public PluginParameters createParameters() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void updateParameters(final Graph graph, final PluginParameters parameters) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void run(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void run(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void run(final GraphReadMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public HelpCtx getHelpCtx() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
