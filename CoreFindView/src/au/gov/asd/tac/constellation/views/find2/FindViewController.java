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
package au.gov.asd.tac.constellation.views.find2;

import au.gov.asd.tac.constellation.views.find2.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find2.plugins.ReplacePlugin;
import au.gov.asd.tac.constellation.views.find2.plugins.BasicFindPlugin;
import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find2.plugins.GraphAttributePlugin;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author Atlas139mkm
 */
public class FindViewController {

    // Find view controller instance
    private static FindViewController instance = null;
    private FindViewTopComponent parentComponent;

    // Parameters
    private final BasicFindReplaceParameters currentBasicFindParameters;
    private final BasicFindReplaceParameters currentBasicReplaceParameters;

    private static final Logger LOGGER = Logger.getLogger(FindViewController.class.getName());

    /**
     * Private constructor for singleton
     */
    private FindViewController() {
        currentBasicFindParameters = new BasicFindReplaceParameters();
        currentBasicReplaceParameters = new BasicFindReplaceParameters();
    }

    /**
     * Singleton instance retrieval
     *
     * @return the instance, if one is not made, it will make one.
     */
    public static synchronized FindViewController getDefault() {
        if (instance == null) {
            instance = new FindViewController();
        }
        return instance;
    }

    public FindViewController init(final FindViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;
        return instance;
    }

    /**
     * Updates the UI elements and controllers variables to match the UI
     */
    public void updateUI() {
        // Find attributes
        // Copies a list of the current attributes to avoid manipulating the original list
        final List<Attribute> findAttributes = new ArrayList<>(currentBasicFindParameters.getAttributeList());
        // repopulates the attributes list incase its changed updates the currentBasicParameters to match the UI
        populateAttributes(currentBasicFindParameters.getGraphElement(), findAttributes, Long.MIN_VALUE);

        // Replace attributes
        // Copies a list of the current attributes to avoid manipulating the original list
        final List<Attribute> replaceAttributes = new ArrayList<>(currentBasicReplaceParameters.getAttributeList());
        // repopulates the attributes list incase its changed updates the currentBasicParameters to match the UI
        populateAttributes(currentBasicReplaceParameters.getGraphElement(), replaceAttributes, Long.MIN_VALUE);

    }

    /**
     * This loops through all the attribute lists in each open graph adding all
     * unique attributes to the UI attribute list if they are of type string
     *
     * @param type the type of graph element
     * @param attributes the list of attributes
     * @param attributeModificationCounter
     */
    public List<String> populateAttributes(final GraphElementType type, final List<Attribute> attributes, final long attributeModificationCounter) {
        attributes.clear();
        final List<String> attributeList = new ArrayList<>();
        final List<Attribute> allAttributes = new ArrayList<>();

        for (final Graph graph : GraphManager.getDefault().getAllGraphs().values()) {

            final GraphAttributePlugin attrPlugin = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
            final Future<?> future = PluginExecution.withPlugin(attrPlugin).interactively(true).executeLater(graph);

            // Wait for the search to find its results:
            try {
                future.get();
            } catch (final InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            // Retrieve a list of all String attributes
            if (attrPlugin.getAttributeModificationCounter() != attributeModificationCounter) {
                allAttributes.addAll(attrPlugin.getAttributes());
                for (final Attribute a : allAttributes) {
                    if (a.getAttributeType().equals("string")) {
                        attributeList.add(a.getName());
                    }
                }
            }
        }
        final Set<String> set = new LinkedHashSet<>();
        set.addAll(attributeList);
        attributeList.clear();
        attributeList.addAll(set);
        return attributeList;
    }

    /**
     * Updates the controllers current parameters with those passed
     *
     * @param parameters
     */
    public void updateBasicFindParameters(final BasicFindReplaceParameters parameters) {
        currentBasicFindParameters.copyParameters(parameters);
    }

    public void updateBasicReplaceParameters(final BasicFindReplaceParameters parameters) {
        currentBasicReplaceParameters.copyParameters(parameters);
    }

    /**
     * Calls the BasicFindPlugin for the active graph if isSearchAllGraphs =
     * false or all graphs if isSearchAllGraphs = true. The plugin is the main
     * source of logic for the searching and selecting of graph elements.
     *
     * @param selectAll true if finding all graph elements
     * @param getNext true if finding the next element, false if the previous
     */
    public void retriveMatchingElements(final boolean selectAll, final boolean getNext) {
        final BasicFindPlugin basicfindPlugin = new BasicFindPlugin(currentBasicFindParameters, selectAll, getNext);
        if (currentBasicFindParameters.isSearchAllGraphs()) {
            for (final Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
                if (graph != null && currentBasicFindParameters.isSearchAllGraphs()) {
                    PluginExecution.withPlugin(basicfindPlugin).executeLater(graph);
                }
            }
        } else {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            if (graph != null) {
                PluginExecution.withPlugin(basicfindPlugin).executeLater(graph);
            }
        }
    }

    public void replaceMatchingElements(final boolean replaceAll, final boolean replaceNext) {
        final ReplacePlugin basicReplacePlugin = new ReplacePlugin(currentBasicReplaceParameters, replaceAll, replaceNext);

        if (currentBasicReplaceParameters.isSearchAllGraphs()) {
            for (final Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
                if (graph != null && currentBasicReplaceParameters.isSearchAllGraphs()) {
                    PluginExecution.withPlugin(basicReplacePlugin).executeLater(graph);
                }
            }
        } else {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            if (graph != null) {
                PluginExecution.withPlugin(basicReplacePlugin).executeLater(graph);
            }
        }
    }

    public BasicFindReplaceParameters getCurrentBasicFindParameters() {
        return currentBasicFindParameters;
    }

    public BasicFindReplaceParameters getCurrentBasicReplaceParameters() {
        return currentBasicReplaceParameters;
    }
}
