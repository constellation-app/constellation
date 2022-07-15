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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.importing.file;

import au.gov.asd.tac.constellation.graph.processing.DatumProcessor;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.io.File;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Service provider for importing graph files
 *
 * @author antares
 */
public interface GraphFileImportProcessor extends DatumProcessor<File, PluginParameters> {
    
    /**
     * The name of the Graph File Type
     * 
     * @return The type of the graph file
     */
    public String getName();
    
    /**
     * The extension filter for this graph file type
     * 
     * @return An Extension filter containing the extensions for this graph file type
     */
    public ExtensionFilter getExtensionFilter();
    
}
