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
import au.gov.asd.tac.constellation.views.find.advanced.GraphAttributePlugin;
import au.gov.asd.tac.constellation.views.find2.components.FindViewPane;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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

    // Layers view controller instance
    private static FindViewController instance = null;
    private FindViewTopComponent parentComponent;

    private BasicFindReplaceParameters currentBasicFindParameters;
    private boolean addToCurrentSelection = false;
    private boolean removeFromCurrentSelection = false;

    private BasicFindReplaceParameters currentBasicReplaceParameters;

    private static final Logger LOGGER = Logger.getLogger(FindViewController.class.getName());

    /**
     * Private constructor for singleton
     */
    private FindViewController() {
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
        if (currentBasicFindParameters != null) {
            /**
             * Copies a list of the current attributes to avoid manipulating the
             * original list
             */
            ArrayList<Attribute> attributes = new ArrayList();
            for (Attribute a : currentBasicFindParameters.getAttributeList()) {
                attributes.add(a);
            }
            /*
             * repopulates the attributes list incase its changed
             * updates the currentBasicParameters to match the UI
             */
            populateAttributes(currentBasicFindParameters.getGraphElement(), attributes, Long.MIN_VALUE);
            updateBasicReplaceParameters(currentBasicReplaceParameters);
        }
        if (currentBasicReplaceParameters != null) {
            ArrayList<Attribute> attributes = new ArrayList();
            for (Attribute a : currentBasicReplaceParameters.getAttributeList()) {
                attributes.add(a);
            }
            populateAttributes(currentBasicReplaceParameters.getGraphElement(), attributes, Long.MIN_VALUE);
            updateBasicReplaceParameters(currentBasicReplaceParameters);
        }
    }

    /**
     * This loops through all the attribute lists in each open graph adding all
     * unique attributes to the UI attribute list if they are of type string
     *
     * @param type the type of graph element
     * @param attributes the list of attributes
     * @param attributeModificationCounter
     */
    public ArrayList<String> populateAttributes(GraphElementType type, ArrayList<Attribute> attributes, long attributeModificationCounter) {
        attributes.clear();
        ArrayList<String> attributeList = new ArrayList<>();
        ArrayList<Attribute> allAttributes = new ArrayList<>();

        for (Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
            final GraphAttributePlugin attrPlugin = new GraphAttributePlugin(type, attributes, attributeModificationCounter);
            final Future<?> future = PluginExecution.withPlugin(attrPlugin).interactively(true).executeLater(graph);

            // Wait for the search to find its results:
            try {
                future.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

            // Retrieve a list of all String attributes
            if (attrPlugin.getAttributeModificationCounter() != attributeModificationCounter) {
                allAttributes = attrPlugin.getAttributes();
                for (Attribute a : allAttributes) {
                    if (a.getAttributeType().equals("string")) {
                        attributeList.add(a.getName());

                    }
                }
            }
        }
        Set<String> set = new LinkedHashSet<>();
        set.addAll(attributeList);
        attributeList.clear();
        attributeList.addAll(set);
        return attributeList;
    }

    /**
     * Disables the find view. This is triggered when no graphs are open open
     *
     * @param pane the find view pane
     * @param disable true if no graphs are open
     */
    public void disableFindView(FindViewPane pane, boolean disable) {
        pane.setDisable(disable);
    }

    public void focusFindTextField() {
        parentComponent.focusFindTextField();
    }

    /**
     * Updates the controllers current parameters with those passed
     *
     * @param parameters
     */
    public void updateBasicFindParameters(BasicFindReplaceParameters parameters) {
        currentBasicFindParameters = parameters;
    }

    public void updateBasicReplaceParameters(BasicFindReplaceParameters parameters) {
        currentBasicReplaceParameters = parameters;
    }

    /**
     * updates the add to current and remove from current variables based on the
     * UI selection
     *
     * @param addToCurrent
     * @param removeFromCurrent
     */
    public void updateSelectionFactors(final boolean addToCurrent, final boolean removeFromCurrent) {
        this.addToCurrentSelection = addToCurrent;
        this.removeFromCurrentSelection = removeFromCurrent;
    }

    /**
     * Calls the BasicFindPlugin for the active graph if isSearchAllGraphs =
     * false or all graphs if isSearchAllGraphs = true. The plugin is the main
     * source of logic for the searching and selecting of graph elements.
     *
     * @param selectAll true if finding all graph elements
     * @param getNext true if finding the next element, false if the previous
     */
    public void retriveMatchingElements(boolean selectAll, boolean getNext) {
        if (currentBasicFindParameters.isSearchAllGraphs()) {
            for (Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
                if (graph != null) {
                    if (currentBasicFindParameters.isSearchAllGraphs()) {
                        BasicFindPlugin basicfindPlugin = new BasicFindPlugin(currentBasicFindParameters, addToCurrentSelection, selectAll, getNext);
                        PluginExecution.withPlugin(basicfindPlugin).executeLater(graph);
                    }
                }
            }
        } else {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            if (graph != null) {
                BasicFindPlugin basicfindPlugin = new BasicFindPlugin(currentBasicFindParameters, addToCurrentSelection, selectAll, getNext);
                PluginExecution.withPlugin(basicfindPlugin).executeLater(graph);
            }
        }
    }

    public void replaceMatchingElements(boolean selectAll, boolean getNext) {
        if (currentBasicReplaceParameters.isSearchAllGraphs()) {
            for (Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
                if (graph != null) {
                    if (currentBasicReplaceParameters.isSearchAllGraphs()) {
                        ReplacePlugin basicReplacePlugin = new ReplacePlugin(currentBasicReplaceParameters, selectAll, getNext);
                        PluginExecution.withPlugin(basicReplacePlugin).executeLater(graph);
                    }
                }
            }
        } else {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            if (graph != null) {
                ReplacePlugin basicReplacePlugin = new ReplacePlugin(currentBasicReplaceParameters, selectAll, getNext);
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

    public boolean isAddToCurrentSelection() {
        return addToCurrentSelection;
    }

    public boolean isRemoveFromCurrentSelection() {
        return removeFromCurrentSelection;
    }

}
