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
package au.gov.asd.tac.constellation.graph.interaction.plugins.io;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.interaction.gui.VisualGraphTopComponent;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author antares
 */
@ServiceProvider(service = Plugin.class)
@NbBundle.Messages("SaveGraphPlugin=Save Graph")
@PluginInfo(pluginType = PluginType.EXPORT, tags = {PluginTags.EXPORT})
public class SaveGraphPlugin extends SimplePlugin {

    private static final Logger LOGGER = Logger.getLogger(SaveGraphPlugin.class.getName());

    public static final String GRAPH_PARAMETER = PluginParameter.buildId(SaveGraphPlugin.class, "graphId");
    public static final String FILE_PATH_PARAMETER = PluginParameter.buildId(SaveGraphPlugin.class, "saveFilePathId");

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> graphIdParameter = StringParameterType.build(GRAPH_PARAMETER);
        final PluginParameter<StringParameterValue> filePathParameter = StringParameterType.build(FILE_PATH_PARAMETER);

        graphIdParameter.setName("graphId");
        graphIdParameter.setDescription("The Id of the graph");
        parameters.addParameter(graphIdParameter);

        filePathParameter.setName("saveFilePath");
        filePathParameter.setDescription("Save Graph Plugin File Path");
        filePathParameter.setStringValue(System.getProperty("user.home"));
        parameters.addParameter(filePathParameter);

        return parameters;
    }

    @Override
    protected void execute(PluginGraphs graphs, PluginInteraction interaction, PluginParameters parameters) throws InterruptedException, PluginException {
        final Graph g = graphs.getAllGraphs().get(parameters.getStringValue(GRAPH_PARAMETER));
        final String filePath = parameters.getStringValue(FILE_PATH_PARAMETER);
        final GraphNode gn = GraphNode.getGraphNode(g);
        try {
            final File fileLocation = new File(filePath);
            if (fileLocation.exists()) {
                final DataFolder df = DataFolder.findFolder(FileUtil.createFolder(fileLocation));
                //Move the GraphNode's data object to the location inputted by the user
                gn.getDataObject().move(df);
                SwingUtilities.invokeLater(() -> {
                    try {
                        ((VisualGraphTopComponent) gn.getTopComponent()).saveGraph();
                    } catch (IOException ex) {
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                    }
                });
            } else {
                JOptionPane.showMessageDialog(null, "Error: Invalid file path parameter");
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }

    }

}
