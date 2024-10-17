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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.reporting.PluginReportUtilities;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPlugin;
import au.gov.asd.tac.constellation.views.dataaccess.plugins.DataAccessPluginCoreType;
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
@PluginInfo(pluginType = PluginType.DELETE, tags = {PluginTags.DELETE})
public class RemoveUnusedAttributesPlugin extends SimpleEditPlugin implements DataAccessPlugin {

    @Override
    public String getName() {
        return "Remove Unused Attributes";
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
        
        // Local process-tracking varables
        int removedAttributeCount = 0;
        int currentProcessStep = 0;
        int totalProcessSteps = graph.getAttributeCount(GraphElementType.VERTEX) + graph.getAttributeCount(GraphElementType.TRANSACTION);
        interaction.setProgress(currentProcessStep, totalProcessSteps, "Removing unused attributes...", true, parameters);      

        //Loop through graph element types
        final Set<GraphElementType> graphElements = new HashSet<>();
        graphElements.add(GraphElementType.VERTEX);
        graphElements.add(GraphElementType.TRANSACTION);
        for (final GraphElementType element : graphElements) {
            final Set<Integer> nullSet = new HashSet<>();
            final int elementCount = (element == GraphElementType.VERTEX) ? graph.getVertexCount() : graph.getTransactionCount();
            final int elementAttributeCount = graph.getAttributeCount(element);

            //Loop though element attributes
            for (int i = 0; i < elementAttributeCount; i++) {
                final int attributeId = graph.getAttribute(element, i);

                if (graph.isPrimaryKey(attributeId)) {
                    continue;
                }

                boolean hasValue = false;

                //Loop through elements
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
                currentProcessStep++;
            }

            // Remove unused attributes found in the curent element
            for (final int attribute : nullSet) {
                removedAttributeCount++;
                interaction.setProgress(currentProcessStep, 
                        totalProcessSteps, 
                        String.format("Removing %s attribute: %s.", 
                                element.getShortLabel().toLowerCase(), 
                                graph.getAttributeName(attribute)
                        ),
                        true
                ); 
                graph.removeAttribute(attribute);
            }
        }
        
        // Set process to complete
        interaction.setProgress(currentProcessStep, 
                totalProcessSteps, 
                String.format("Removed %s.", 
                        PluginReportUtilities.getAttributeCountString(removedAttributeCount)
                ),
                true
        ); 
    }
}
