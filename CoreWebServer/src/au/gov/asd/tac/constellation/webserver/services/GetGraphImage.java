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
package au.gov.asd.tac.constellation.webserver.services;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.utilities.visual.VisualManager;
import au.gov.asd.tac.constellation.webserver.restapi.RestService;
import static au.gov.asd.tac.constellation.webserver.restapi.RestService.HTTP_UNPROCESSABLE_ENTITY;
import au.gov.asd.tac.constellation.webserver.restapi.RestServiceException;
import static au.gov.asd.tac.constellation.webserver.restapi.RestServiceUtilities.IMAGE_PNG;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import org.openide.util.lookup.ServiceProvider;

/**
 * A screenshot of the graph in PNG format.
 *
 * @author algol
 */
@ServiceProvider(service = RestService.class)
public class GetGraphImage extends RestService {

    private static final String NAME = "get_graph_image";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "A screenshot of the graph in PNG format.";
    }

    @Override
    public String[] getTags() {
        return new String[]{"graph", "PNG"};
    }

    @Override
    public void callService(final PluginParameters parameters, final InputStream in, final OutputStream out) throws IOException {
        final Graph graph = GraphManager.getDefault().getActiveGraph();
        if (graph == null) {
            throw new RestServiceException(HTTP_UNPROCESSABLE_ENTITY, "No graph is opened in Constellation");
        }

        // This is asynchronous, so we need a Semaphore.
        //
        final GraphNode graphNode = GraphNode.getGraphNode(graph);
        final VisualManager visualManager = graphNode.getVisualManager();
        final BufferedImage[] img1 = new BufferedImage[1];

        if (visualManager != null) {
            final Semaphore waiter = new Semaphore(0);
            visualManager.exportToBufferedImage(img1, waiter);
            waiter.acquireUninterruptibly();

            ImageIO.write(img1[0], "png", out);
        } else {
            throw new IOException("Graph image unavailable");
        }
    }

    @Override
    public String getMimeType() {
        return IMAGE_PNG;
    }
}
