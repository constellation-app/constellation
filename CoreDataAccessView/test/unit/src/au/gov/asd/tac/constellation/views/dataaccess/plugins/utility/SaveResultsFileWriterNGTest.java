/*
 * Copyright 2010-2019 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginGraphs;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.openide.util.HelpCtx;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Save Results File Writer Test.
 *
 * @author arcturus
 */
public class SaveResultsFileWriterNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    public SaveResultsFileWriterNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

//    /**
//     * Test of write method, of class SaveResultsFileWriter.
//     *
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testWrite() throws Exception {
//        System.out.println("write");
//        Plugin plugin = null;
//        String data = "";
//        String filenameSuffix = "";
//        SaveResultsFileWriter.write(plugin, data, filenameSuffix);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of writeRecordStore method, of class SaveResultsFileWriter.
//     *
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testWriteRecordStore() throws Exception {
//        System.out.println("writeRecordStore");
//        Plugin plugin = null;
//        final RecordStore recordstore = null;
//        SaveResultsFileWriter.writeRecordStore(plugin, recordstore);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of writeXml method, of class SaveResultsFileWriter.
//     *
//     * @throws java.lang.Exception
//     */
//    @Test
//    public void testWriteXml() throws Exception {
//        System.out.println("writeXml");
//        Plugin plugin = null;
//        String xml = "";
//        SaveResultsFileWriter.writeXml(plugin, xml);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of generateFilename method, of class SaveResultsFileWriter.
     */
    @Test
    public void testGenerateFilenameLength() {
        Plugin plugin = new ExampleClass();
        String filename = SaveResultsFileWriter.generateFilename(plugin, "xml");
        int expectedFilenameLength = (LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssSSS")) + "-ExampleClass.xml").length();
        Assert.assertTrue(filename.length() == expectedFilenameLength);
    }

    /**
     * Test of generateFilename method, of class SaveResultsFileWriter.
     */
    @Test
    public void testGenerateFilenameSuffix() {
        Plugin plugin = new ExampleClass();
        String filename = SaveResultsFileWriter.generateFilename(plugin, "xml");
        Assert.assertTrue(filename.endsWith(".xml"));
    }

    /**
     * Test of generateFilename method, of class SaveResultsFileWriter.
     */
    @Test
    public void testGenerateFilenameSuffix2() {
        Plugin plugin = new ExampleClass();
        String filename = SaveResultsFileWriter.generateFilename(plugin, "xml");
        Assert.assertFalse(filename.endsWith("..xml"));
    }

    private static class ExampleClass implements Plugin {

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
