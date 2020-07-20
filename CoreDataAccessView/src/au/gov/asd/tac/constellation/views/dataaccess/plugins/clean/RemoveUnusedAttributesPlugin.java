/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.DataAccessPluginCoreType;
import java.util.HashSet;
import java.util.Set;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Remove Unused Attributes Plugin
 *
 * @author antares
 */
@ServiceProviders({
    @ServiceProvider(service = DataAccessPlugin.class),
    @ServiceProvider(service = Plugin.class)})
@NbBundle.Messages("RemoveUnusedAttributesPlugin=Remove Unused Attributes")
public class RemoveUnusedAttributesPlugin extends SimpleEditPlugin implements DataAccessPlugin {

    @Override
    public String getName() {
        return "Remove Unsued Attributes";
    }

    @Override
    public String getDescription() {
        return "Removes all unused (non-key) atrributes from the graph";
    }

    @Override
    public String getType() {
        return DataAccessPluginCoreType.CLEAN;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
        if (graph.getVertexCount() > 0) {
            final Set<GraphElementType> graphElements = new HashSet<>();
            graphElements.add(GraphElementType.VERTEX);
            graphElements.add(GraphElementType.TRANSACTION);
            final Set<Integer> nullSet = new HashSet<>();

            for (final GraphElementType element : graphElements) {
                final int elementCount = (element == GraphElementType.VERTEX) ? graph.getVertexCount() : graph.getTransactionCount();
                final int elementAttributeCount = graph.getAttributeCount(element);

                for (int i = 0; i < elementAttributeCount; i++) {
                    final int attributeId = graph.getAttribute(element, i);

                    if (graph.isPrimaryKey(attributeId)) {
                        continue;
                    }

                    boolean hasValue = false;

                    for (int j = 0; j < elementCount; j++) {
                        final int elementId = (element == GraphElementType.VERTEX) ? graph.getVertex(j) : graph.getTransaction(j);
                        if (graph.getStringValue(attributeId, elementId) != null) {
                            hasValue = true;
                            break;
                        }
                    }

                    if (!hasValue) {
                        nullSet.add(attributeId);
                    }
                }
            }

            for (final int attribute : nullSet) {
                graph.removeAttribute(attribute);
            }
        }
    }
}
