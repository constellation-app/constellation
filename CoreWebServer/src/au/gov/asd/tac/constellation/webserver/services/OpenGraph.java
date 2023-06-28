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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.file.GraphDataObject;
import au.gov.asd.tac.constellation.graph.file.io.GraphJsonReader;
import au.gov.asd.tac.constellation.graph.file.io.GraphParseException;
import au.gov.asd.tac.constellation.graph.file.opener.GraphOpener;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.StringParameterValue;
import au.gov.asd.tac.constellation.utilities.gui.HandleIoProgress;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * Open a .star graph file.
 * <p>
 * TODO why does this not work exactly the same as opening manually? Try this,
 * then manually open recent a couple of times, for example.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class OpenGraph extends RestService {

    private static final Logger LOGGER = Logger.getLogger(OpenGraph.class.getName());

    private static final String NAME = "open_graph";
    private static final String FILE_PARAMETER_ID = "filename";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Open a .star graph file.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph"};
    }

    @Override
    public RestServiceUtilities.HttpMethod getHttpMethod() {
        return RestServiceUtilities.HttpMethod.POST;
    }

    @Override
    public PluginParameters createParameters() {
        final PluginParameters parameters = new PluginParameters();

        final PluginParameter<StringParameterValue> fileParam = StringParameterType.build(FILE_PARAMETER_ID);
        fileParam.setName("File path");
        fileParam.setDescription("The fully qualified path of a .star file.");
        fileParam.setRequired(true);
        parameters.addParameter(fileParam);

        return parameters;
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final String filePath = parameters.getStringValue(FILE_PARAMETER_ID);

        final String existingId = RestServiceUtilities.activeGraphId();

        final File fnam = new File(filePath).getAbsoluteFile();
        String name = fnam.getName();
        if (StringUtils.endsWithIgnoreCase(name, GraphDataObject.FILE_EXTENSION)) {
            name = name.substring(0, name.length() - GraphDataObject.FILE_EXTENSION.length());
        }

        try {
            final Graph g = new GraphJsonReader().readGraphZip(fnam, new HandleIoProgress(String.format("Loading graph %s...", fnam)));
            GraphOpener.getDefault().openGraph(g, name, false);

            final String newId = RestServiceUtilities.waitForGraphChange(existingId).get(10, TimeUnit.SECONDS);
            final Graph graph = GraphNode.getGraphNode(newId).getGraph();

            final ObjectMapper mapper = new ObjectMapper();
            final ObjectNode root = mapper.createObjectNode();
            root.put("id", graph.getId());
            root.put("name", GraphNode.getGraphNode(graph.getId()).getDisplayName());
            root.put("schema", graph.getSchema().getFactory().getName());
            mapper.writeValue(out, root);
        } catch (final GraphParseException | FileNotFoundException ex) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, ex.getMessage());
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "This thread has been interrupted", ex);
        } catch (final ExecutionException | TimeoutException ex) {
            throw new RestServiceException(ex);
        }
    }
}
