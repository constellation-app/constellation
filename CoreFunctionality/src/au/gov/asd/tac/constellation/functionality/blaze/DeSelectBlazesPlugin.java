/*
 * Copyright 2010-2019 Australian Signals Directorate. All Rights Reserved.
 *
 * NOTICE: All information contained herein remains the property of the
 * Australian Signals Directorate. The intellectual and technical concepts
 * contained herein are proprietary to the Australian Signals Directorate and
 * are protected by copyright law. Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from the Australian Signals Directorate.
 */
package au.gov.asd.tac.constellation.functionality.blaze;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.visual.blaze.BlazeAttributeDescription;
import au.gov.asd.tac.constellation.graph.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.pluginframework.Plugin;
import au.gov.asd.tac.constellation.pluginframework.PluginException;
import au.gov.asd.tac.constellation.pluginframework.PluginInteraction;
import au.gov.asd.tac.constellation.pluginframework.parameters.PluginParameters;
import au.gov.asd.tac.constellation.pluginframework.templates.SimpleEditPlugin;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author mimosa
 */
@ServiceProviders({
    @ServiceProvider(service = Plugin.class)
})
@NbBundle.Messages("DeSelectBlazesPlugin=Deselect Blazes")
public class DeSelectBlazesPlugin extends SimpleEditPlugin {

    @Override
    protected void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {

        final int selectedAttr = VisualConcept.VertexAttribute.SELECTED.get(graph);
        final int blazeAttr = VisualConcept.VertexAttribute.BLAZE.get(graph);

        if (blazeAttr != Graph.NOT_FOUND) {
            for (int position = 0; position < graph.getVertexCount(); position++) {
                final int vertexId = graph.getVertex(position);

                if (graph.getObjectValue(blazeAttr, vertexId) != BlazeAttributeDescription.DEFAULT_VALUE) {
                    graph.setBooleanValue(selectedAttr, vertexId, false);
                }
            }
        }

    }
}
