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
package au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisManager;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class handles the gathering of parameters and execution of the phrasiphy
 * content plugin
 *
 * @author twilight_sparkle
 * @author antares
 */
@ServiceProvider(service = Plugin.class)
@PluginInfo(pluginType = PluginType.NONE, tags = {"ANALYTIC"})
@Messages("PhrasiphyContentPlugin= Phrasiphy Content")
public class PhrasiphyContentPlugin extends SimplePlugin {

    private static final String SELECTED_ATTRIBUTE_NAME = "selected";

    @Override
    public String getName() {
        return "Phrasiphy Content";
    }

    @Override
    protected void execute(final PluginGraphs graph, final PluginInteraction interaction, final PluginParameters params) throws InterruptedException {
        // Construct the object which will encapsulate all the parameters for this plugin
        final PhrasiphyContentParameters phrasiphyContentParams = PhrasiphyContentParameters.getDefaultParameters();

        // Get a map relating parameter names to parameter values from the PluginParameters object
        final Map<String, PluginParameter<?>> parameters = params.getParameters();

        // Set the value of the phrase length and proximity parameters
        phrasiphyContentParams.setPhraseLength(parameters.get(PhrasiphyContentParameters.PHRASE_LENGTH_PARAMETER_ID).getIntegerValue());
        phrasiphyContentParams.setProximity(parameters.get(PhrasiphyContentParameters.PROXIMITY_PARAMETER_ID).getIntegerValue());
        phrasiphyContentParams.setThreshold(parameters.get(PhrasiphyContentParameters.THRESHOLD_PARAMETER_ID).getIntegerValue());
        phrasiphyContentParams.setElementType(parameters.get(PhrasiphyContentParameters.ELEMENT_TYPE_PARAMETER_ID).getStringValue());
        phrasiphyContentParams.setBackgroundFilter(parameters.get(PhrasiphyContentParameters.BACKGROUND_FILTER_PARAMETER_ID).getStringValue());

        // Attempt to retrieve the attribute ID from the graph corresponding to the value of the attribute to analyse parameter
        final Graph g = graph.getGraph();
        final ReadableGraph rg = g.getReadableGraph();
        final GraphElementType elementType = phrasiphyContentParams.getElementType();
        final int attributeIDToAnalyse = rg.getAttribute(elementType, parameters.get(PhrasiphyContentParameters.ATTRIBUTE_TO_ANALYSE_PARAMETER_ID).getStringValue());
        final int elCount = elementType.equals(GraphElementType.VERTEX) ? rg.getVertexCount() : rg.getTransactionCount();
        final int elCapacity = elementType.equals(GraphElementType.VERTEX) ? rg.getVertexCapacity() : rg.getTransactionCapacity();

        // Do nothing if we have a graph with no graph elements or no graph elements containing the desired attribute
        if (elCount == 0 || attributeIDToAnalyse < 0) {
            final String msg;
            if (attributeIDToAnalyse < 0) {
                msg = "No Attribute Selected or Attribute Not Found - No Word Cloud Generated";
            } else {
                msg = "Empty Graph Found - No Word Cloud Generated"; // else must have entered the if statement because of a null graph 
            }

            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

            rg.release();
            return;
        }

        // Set the on attribute ID
        phrasiphyContentParams.setOnAttributeID(attributeIDToAnalyse);

        // Build a (possible empty) list containing the indices of the currently selected vertices
        int[] selectedElements = new int[elCount];
        final int[] allElements = new int[elCount];
        final int elSelectedAttr = rg.getAttribute(elementType, SELECTED_ATTRIBUTE_NAME);
        // DO nothing if the selected attribute is not found
        if (elSelectedAttr == Graph.NOT_FOUND) {
            final String msg = "Attribute Not Found - No Word Cloud Generated";
            final NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

            rg.release();
            return;
        }

        int i = 0;
        for (int position = 0; position < elCount; position++) {
            final int elId = elementType.equals(GraphElementType.VERTEX) ? rg.getVertex(position) : rg.getTransaction(position);
            if (rg.getBooleanValue(elSelectedAttr, elId)) {
                selectedElements[i++] = elId;
            }
            allElements[position] = elId;
        }

        // If no vertices are selected, add all vertices to the array of selected elements 
        if (i == 0) {
            selectedElements = allElements;
            i = elCount;
        }

        selectedElements = Arrays.copyOf(selectedElements, i);
        rg.release();

        @SuppressWarnings("unchecked") //Object returned is a list of files 
        final List<File> files = (List<File>) parameters.get(PhrasiphyContentParameters.BACKGROUND_PARAMETER_ID).getObjectValue();
        File file = null;
        if (files != null && !files.isEmpty()) {
            file = files.get(0);
        }

        // Create a ContentAnalysisManager object with reference to the graph, the graph elements to analyse, and the plugin parameters
        final ContentAnalysisManager cam = new ContentAnalysisManager(g, selectedElements, new HashSet<>(), elCapacity, elementType, phrasiphyContentParams.getOnAttributeID());
        // Get the ContentAnalysisManager to phrasiphy content
        cam.phrasiphyContent(phrasiphyContentParams, file);
    }
}
