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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing;

import au.gov.asd.tac.constellation.graph.node.plugins.DefaultPluginInteraction;
import au.gov.asd.tac.constellation.graph.node.plugins.PluginManager;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import java.util.Arrays;
import javafx.stage.FileChooser.ExtensionFilter;
import static org.mockito.Mockito.mock;
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
public class ImportGraphFilePluginNGTest {
    
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
     * Test of createParameters method, of class ImportGraphFilePlugin.
     */
    @Test
    public void testCreateParameters() {
        System.out.println("createParameters");
        
        final ImportGraphFilePlugin instance = new ImportGraphFilePlugin();
        final PluginParameters params = instance.createParameters();
        assertEquals(params.getParameters().size(), 3);
        
        // now to test that the controller works as expected
        final PluginParameter<FileParameterValue> fileName = (PluginParameter<FileParameterValue>) params.getParameters().get("ImportGraphFilePlugin.file_name");
        final ExtensionFilter filterTypesBeforeTypeChange = FileParameterType.getFileFilters(fileName);
        assertEquals(filterTypesBeforeTypeChange.getExtensions(), Arrays.asList(".gml"));
        
        final PluginParameter<SingleChoiceParameterValue> fileType = (PluginParameter<SingleChoiceParameterValue>) params.getParameters().get("ImportGraphFilePlugin.file_type");
        SingleChoiceParameterType.setChoice(fileType, "GraphML");
        final ExtensionFilter filterTypesAfterTypeChange = FileParameterType.getFileFilters(fileName);
        assertEquals(filterTypesAfterTypeChange.getExtensions(), Arrays.asList(".graphml"));
    }

    /**
     * Test of query method, of class ImportGraphFilePlugin. Include Transactions.
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testQueryIncludeTransactions() throws InterruptedException, PluginException {
        System.out.println("queryIncludeTransactions");
        final ImportGraphFilePlugin instance = new ImportGraphFilePlugin();
        final PluginParameters params = instance.createParameters();
        
        final PluginParameter<FileParameterValue> fileName = (PluginParameter<FileParameterValue>) params.getParameters().get("ImportGraphFilePlugin.file_name");
        fileName.setStringValue(ImportGraphFilePluginNGTest.class.getResource("file/resources/test.gml").getPath());
       
        final PluginManager manager = mock(PluginManager.class);        
        when(manager.getGraphNode()).thenReturn(null);
        when(manager.getPlugin()).thenReturn(instance);
        
        final RecordStore result = instance.query(null, new DefaultPluginInteraction(manager, null), params);
        // one entry for each node and edge
        assertEquals(result.size(), 6);
    }
    
    /**
     * Test of query method, of class ImportGraphFilePlugin. Exclude Transactions.
     * @throws java.lang.InterruptedException
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testQueryExcludeTransactions() throws InterruptedException, PluginException {
        System.out.println("querExcludeTransactions");
        final ImportGraphFilePlugin instance = new ImportGraphFilePlugin();
        final PluginParameters params = instance.createParameters();
        
        final PluginParameter<FileParameterValue> fileName = (PluginParameter<FileParameterValue>) params.getParameters().get("ImportGraphFilePlugin.file_name");
        fileName.setStringValue(ImportGraphFilePluginNGTest.class.getResource("file/resources/test.gml").getPath());
        params.setBooleanValue("ImportGraphFilePlugin.retrieve_transactions", false);
        
        final PluginManager manager = mock(PluginManager.class);        
        when(manager.getGraphNode()).thenReturn(null);
        when(manager.getPlugin()).thenReturn(instance);
        
        final RecordStore result = instance.query(null, new DefaultPluginInteraction(manager, null), params);
        // one entry for each node and edge
        assertEquals(result.size(), 4);
    }
}
