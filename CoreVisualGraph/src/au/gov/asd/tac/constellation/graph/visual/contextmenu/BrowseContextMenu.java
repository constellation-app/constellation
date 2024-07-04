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
package au.gov.asd.tac.constellation.graph.visual.contextmenu;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphReadMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.attribute.HyperlinkAttributeDescription;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * A context menu that allows the user to open URI attributes on their browser.
 *
 * @author sirius
 */
@ServiceProvider(service = ContextMenuProvider.class, position = 10000)
public class BrowseContextMenu implements ContextMenuProvider {

    private static final Logger LOGGER = Logger.getLogger(BrowseContextMenu.class.getName());

    @Override
    public List<String> getMenuPath(final GraphElementType elementType) {
        return Arrays.asList("Browse To");
    }

    @Override
    public List<String> getItems(final GraphReadMethods graph, final GraphElementType elementType, final int entity) {
        final List<String> items = new ArrayList<>();
        final int attributeCount = graph.getAttributeCount(elementType);
        for (int position = 0; position < attributeCount; position++) {
            final int attributeId = graph.getAttribute(elementType, position);
            final Attribute attribute = new GraphAttribute(graph, attributeId);
            if (attribute.getDataType() == HyperlinkAttributeDescription.class) {
                final URI value = (URI) graph.getObjectValue(attributeId, entity);
                if (value != null) {
                    items.add(attribute.getName());
                }
            }
        }
        return items;
    }

    @Override
    public void selectItem(final String item, final Graph graph, final GraphElementType elementType, final int elementId, final Vector3f unprojected) {
        final ReadableGraph rg = graph.getReadableGraph();
        try {
            final int attribute = rg.getAttribute(elementType, item);
            if (attribute != Graph.NOT_FOUND) {
                final URI uri = (URI) rg.getObjectValue(attribute, elementId);
                if (uri != null) {
                    Desktop.getDesktop().browse(uri);
                    // unable to use the plugin due to a circular dependency
//                    PluginExecution.withPlugin(VisualGraphPluginRegistry.OPEN_IN_BROWSER)
//                            .withParameter(OpenInBrowserPlugin.APPLICATION_PARAMETER_ID, "Browse To")
//                            .withParameter(OpenInBrowserPlugin.URL_PARAMETER_ID, uri)
//                            .executeLater(null);
                }
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        } finally {
            rg.release();
        }
    }
}
