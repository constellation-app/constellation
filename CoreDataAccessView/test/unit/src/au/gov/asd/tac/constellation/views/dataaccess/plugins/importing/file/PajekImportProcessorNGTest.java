/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file;

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.ProcessingException;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.gui.NotifyDisplayer;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.ImportGraphFilePlugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author antares
 */
public class PajekImportProcessorNGTest {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }
    
    /**
     * Test of process method, of class PajekImportProcessor. Null parameters
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test
    public void testProcessNullParameters() throws ProcessingException {
        System.out.println("processNullParameters");
        
        final RecordStore output = new GraphRecordStore();
        
        final File file = new File(PajekImportProcessorNGTest.class.getResource("resources/test.net").getPath());
        
        final PajekImportProcessor instance = new PajekImportProcessor();
        instance.process(null, file, output);
        
        // should be one row each node and edge in the file
        assertEquals(output.size(), 6);
    }
    
    /**
     * Test of process method, of class PajekImportProcessor. Empty parameters
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test
    public void testProcessEmptyParameters() throws ProcessingException {
        System.out.println("processEmptyParameters");
        
        final RecordStore output = new GraphRecordStore();
        
        final File file = new File(PajekImportProcessorNGTest.class.getResource("resources/test.net").getPath());
        
        final PajekImportProcessor instance = new PajekImportProcessor();
        instance.process(new PluginParameters(), file, output);
        
        // should be one row each node and edge in the file
        assertEquals(output.size(), 6);
    }
    
    /**
     * Test of process method, of class PajekImportProcessor. Null File
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test(expectedExceptions = {ProcessingException.class}, expectedExceptionsMessageRegExp = "Please specify a file to read from")
    public void testProcessNullFile() throws ProcessingException {
        System.out.println("processNullFile");
        
        // get the parameters for processing
        final ImportGraphFilePlugin plugin = new ImportGraphFilePlugin();
        final PluginParameters parameters = plugin.createParameters();
        
        final RecordStore output = new GraphRecordStore();
        
        final PajekImportProcessor instance = new PajekImportProcessor();
        instance.process(parameters, null, output);
    }
    
    /**
     * Test of process method, of class PajekImportProcessor. Null Output Record Processor
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test(expectedExceptions = {ProcessingException.class}, expectedExceptionsMessageRegExp = "Please specify a record store to output to")
    public void testProcessNullOutput() throws ProcessingException {
        System.out.println("processNullOutput");
        
        // get the parameters for processing
        final ImportGraphFilePlugin plugin = new ImportGraphFilePlugin();
        final PluginParameters parameters = plugin.createParameters();
        
        final File file = new File(PajekImportProcessorNGTest.class.getResource("resources/test.net").getPath());
        
        final PajekImportProcessor instance = new PajekImportProcessor();
        instance.process(parameters, file, null);
    }

    /**
     * Test of process method, of class PajekImportProcessor. Only importing nodes
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test
    public void testProcessImportNodesOnly() throws ProcessingException {
        System.out.println("processImportNodesOnly");
        
        // get the parameters for processing
        final ImportGraphFilePlugin plugin = new ImportGraphFilePlugin();
        final PluginParameters parameters = plugin.createParameters();
        parameters.setBooleanValue("ImportGraphFilePlugin.retrieve_transactions", false);
        
        final File file = new File(PajekImportProcessorNGTest.class.getResource("resources/test.net").getPath());
        
        final RecordStore output = new GraphRecordStore();
        
        final PajekImportProcessor instance = new PajekImportProcessor();
        instance.process(parameters, file, output);
        
        // should be one row each node in the file
        assertEquals(output.size(), 4);
    }
    
    /**
     * Test of process method, of class PajekImportProcessor forcing FileNotFound exception
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test
    public void testProcessFileNotFoundException() throws ProcessingException {
        System.out.println("processFileNotFoundException");
        
        // Mock the NotifyDisplayer to prevent dialogs from being displayed on screen
        try (final MockedStatic<NotifyDisplayer> notiDispMock = Mockito.mockStatic(NotifyDisplayer.class)) {
            // get the parameters for processing
            final ImportGraphFilePlugin plugin = new ImportGraphFilePlugin();
            final PluginParameters parameters = plugin.createParameters();
            parameters.setBooleanValue("ImportGraphFilePlugin.retrieve_transactions", false);

            // use an invalid file name to generate FileNotFound exception
            final File file = new File("xYz");
            final RecordStore output = new GraphRecordStore();
            final PajekImportProcessor instance = new PajekImportProcessor();
            instance.process(parameters, file, output);

            // should have no output due to the File Not Found exception
            assertEquals(output.size(), 0);
        }
    }
    
    /**
     * Test of process method, of class PajekImportProcessor forcing IO exception
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test
    public void testProcessIOException() throws ProcessingException {
        System.out.println("processIOException");
        
        // Mock the NotifyDisplayer to prevent dialogs from being displayed on screen
        // Mock the buffered reader to always throw an IO exception when the readLine() method is called
        try (final MockedStatic<NotifyDisplayer> notiDispMock = Mockito.mockStatic(NotifyDisplayer.class);
                final MockedConstruction<BufferedReader> mockedBufferedReader = Mockito.mockConstruction(BufferedReader.class, (mock, context) -> {
                    when(mock.readLine()).thenAnswer(iom -> {
                        throw new IOException("mocked IO exception");
                    });
                })                
            ) {
            // get the parameters for processing
            final ImportGraphFilePlugin plugin = new ImportGraphFilePlugin();
            final PluginParameters parameters = plugin.createParameters();
            parameters.setBooleanValue("ImportGraphFilePlugin.retrieve_transactions", false);

            final File file = new File(PajekImportProcessorNGTest.class.getResource("resources/test.net").getPath());
            final RecordStore output = new GraphRecordStore();
            final PajekImportProcessor instance = new PajekImportProcessor();
            instance.process(parameters, file, output);

            // should have no output due to the IO exception
            assertEquals(output.size(), 0);
        }
    }
    
    /**
     * Test of process method, of class PajekImportProcessor. Importing nodes and transactions
     * @throws au.gov.asd.tac.constellation.graph.processing.ProcessingException
     */
    @Test
    public void testProcessImportNodesAndTransactions() throws ProcessingException {
        System.out.println("processImportNodesOnly");
        
        // get the parameters for processing
        final ImportGraphFilePlugin plugin = new ImportGraphFilePlugin();
        final PluginParameters parameters = plugin.createParameters();
        
        final File file = new File(PajekImportProcessorNGTest.class.getResource("resources/test.net").getPath());
        
        final RecordStore output = new GraphRecordStore();
        
        final PajekImportProcessor instance = new PajekImportProcessor();
        instance.process(parameters, file, output);
        
        // should be one row each node and edge in the file
        assertEquals(output.size(), 6);
    }
}
