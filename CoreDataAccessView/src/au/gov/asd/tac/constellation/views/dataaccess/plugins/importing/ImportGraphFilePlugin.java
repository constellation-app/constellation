/*
 * Copyright 2010-2022 Australian Signals Directorate
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

import au.gov.asd.tac.constellation.graph.processing.GraphRecordStore;
import au.gov.asd.tac.constellation.graph.processing.ProcessingException;
import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.FileParameterType.FileParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportUtilities;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file.GraphFileImportProcessor;
import au.gov.asd.tac.constellation.views.dataaccess.templates.RecordStoreQueryPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Import from a range of graph files
 *
 * @author antares
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@PluginInfo(pluginType = PluginType.IMPORT, tags = {"IMPORT"})
@Messages("ImportGraphFilePlugin=Import Graph File")
public class ImportGraphFilePlugin extends RecordStoreQueryPlugin implements DataAccessPlugin {

    private static final Logger LOGGER = Logger.getLogger(ImportGraphFilePlugin.class.getName());
    
    public static final String FILE_TYPE_PARAMETER_ID = PluginParameter.buildId(ImportGraphFilePlugin.class, "file_type");
    public static final String FILE_NAME_PARAMETER_ID = PluginParameter.buildId(ImportGraphFilePlugin.class, "file_name");
    public static final String RETRIEVE_TRANSACTIONS_PARAMETER_ID = PluginParameter.buildId(ImportGraphFilePlugin.class, "retrieve_transactions");
    
    private static final List<String> GRAPH_FILE_IMPORT_PROCESSOR_NAMES = new ArrayList<>();
    private static final Map<String, GraphFileImportProcessor> GRAPH_FILE_IMPORT_PROCESSORS = new HashMap<>();
    
    private static GraphFileImportProcessor importProcessor;
    
    public ImportGraphFilePlugin() {
        if (GRAPH_FILE_IMPORT_PROCESSORS.isEmpty()) {
            for (final GraphFileImportProcessor processor : Lookup.getDefault().lookupAll(GraphFileImportProcessor.class)) {
                GRAPH_FILE_IMPORT_PROCESSOR_NAMES.add(processor.getName());
                GRAPH_FILE_IMPORT_PROCESSORS.put(processor.getName(), processor);
            }
            Collections.sort(GRAPH_FILE_IMPORT_PROCESSOR_NAMES);
        }
    }

    @Override
    public String getType() {
        return DataAccessPluginCoreType.IMPORT;
    }

    @Override
    public int getPosition() {
        return 100;
    }
    
    @Override
    public String getDescription() {
        return "Import a Graph File";
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters params = new PluginParameters();
        
        final PluginParameter<SingleChoiceParameterValue> fileTypeParam = SingleChoiceParameterType.build(FILE_TYPE_PARAMETER_ID);
        fileTypeParam.setName("File Type");
        fileTypeParam.setDescription("The type of file to import");
        if (!GRAPH_FILE_IMPORT_PROCESSOR_NAMES.isEmpty()) {
            SingleChoiceParameterType.setOptions(fileTypeParam, GRAPH_FILE_IMPORT_PROCESSOR_NAMES);
            SingleChoiceParameterType.setChoice(fileTypeParam, GRAPH_FILE_IMPORT_PROCESSOR_NAMES.get(0));
            importProcessor = GRAPH_FILE_IMPORT_PROCESSORS.get(GRAPH_FILE_IMPORT_PROCESSOR_NAMES.get(0));
        }
        params.addParameter(fileTypeParam);
        
        final PluginParameter<FileParameterValue> fileNameParam = FileParameterType.build(FILE_NAME_PARAMETER_ID);
        fileNameParam.setName("File");
        fileNameParam.setDescription("File to extract graph from");
        if (!GRAPH_FILE_IMPORT_PROCESSORS.isEmpty()) {
            // this should match up with the default value for the File Type Parameter
            FileParameterType.setFileFilters(fileNameParam, GRAPH_FILE_IMPORT_PROCESSORS.get(GRAPH_FILE_IMPORT_PROCESSOR_NAMES.get(0)).getExtensionFilter());
        }
        params.addParameter(fileNameParam);
        
        final PluginParameter<BooleanParameterValue> retrieveTransactionsParam = BooleanParameterType.build(RETRIEVE_TRANSACTIONS_PARAMETER_ID);
        retrieveTransactionsParam.setName("Retrieve Transactions");
        retrieveTransactionsParam.setDescription("Retrieve Transactions from File");
        retrieveTransactionsParam.setBooleanValue(true);
        params.addParameter(retrieveTransactionsParam);
        
        params.addController(FILE_TYPE_PARAMETER_ID, (master, parameters, change) -> {
            if (change == ParameterChange.VALUE) {
                final GraphFileImportProcessor selection = GRAPH_FILE_IMPORT_PROCESSORS.get(master.getStringValue());
                importProcessor = selection;
                
                @SuppressWarnings("unchecked") //FILE_NAME_PARAMETER_ID is always of type FileParameter
                final PluginParameter<FileParameterValue> fileName = (PluginParameter<FileParameterValue>) parameters.get(FILE_NAME_PARAMETER_ID);
                FileParameterType.setFileFilters(fileName, selection.getExtensionFilter());
            }
        });
        
        return params;
    }
    
    @Override
    protected RecordStore query(final RecordStore query, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        
        // Retrieve PluginParameter values 
        final String filename = parameters.getParameters().get(FILE_NAME_PARAMETER_ID).getStringValue();    
        
        // Local process-tracking varables (Process is indeteminate)
        interaction.setProgress(0, -1, "Importing...", true);
        
        // Import the file
        final RecordStore result = new GraphRecordStore();  
        try {
            importProcessor.process(parameters, new File(filename), result);
        } catch (final ProcessingException ex) {
            LOGGER.log(Level.SEVERE, "Unable to process graph file");
        }
        
        // Set process to complete
        interaction.setProgress(1, 0, String.format("Imported %s.", PluginReportUtilities.getFileCountString(result.size())), true);
        
        return result;
    }
}
