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
package au.gov.asd.tac.constellation.views.find;

import au.gov.asd.tac.constellation.graph.Attribute;
import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.views.find.components.advanced.utilities.AdvancedSearchParameters;
import au.gov.asd.tac.constellation.views.find.plugins.BasicFindGraphSelectionPlugin;
import au.gov.asd.tac.constellation.views.find.plugins.BasicFindPlugin;
import au.gov.asd.tac.constellation.views.find.plugins.DeleteResultsPlugin;
import au.gov.asd.tac.constellation.views.find.plugins.GraphAttributePlugin;
import au.gov.asd.tac.constellation.views.find.plugins.ReplacePlugin;
import au.gov.asd.tac.constellation.views.find.plugins.advanced.AdvancedFindGraphSelectionPlugin;
import au.gov.asd.tac.constellation.views.find.plugins.advanced.AdvancedSearchPlugin;
import au.gov.asd.tac.constellation.views.find.utilities.ActiveFindResultsList;
import au.gov.asd.tac.constellation.views.find.utilities.BasicFindReplaceParameters;
import au.gov.asd.tac.constellation.views.find.utilities.FindResult;
import au.gov.asd.tac.constellation.views.find.utilities.FindResultsList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * This controller class handles the interaction between the findView2 UI
 * elements and the background search / replace logic.
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
    private final AdvancedSearchParameters currentAdvancedSearchParameters;
    private static final Logger LOGGER = Logger.getLogger(FindViewController.class.getName());

    private static final String DELETE = "Delete";
    private static final String CANCEL = "Cancel";
    private static final String ALL_OPEN_GRAPHS = "All Open Graphs";

    private final IntegerProperty numResultsFoundFlag = new SimpleIntegerProperty(0);

    private static int graphsSearched = 0;

    public static int getGraphsSearched() {
        return graphsSearched;
    }

    public static void setGraphsSearched(final int graphsSearched) {
        FindViewController.graphsSearched = graphsSearched;
    }

    /**
     * Private constructor for singleton
     */
    private FindViewController() {
        currentBasicFindParameters = new BasicFindReplaceParameters();
        currentBasicReplaceParameters = new BasicFindReplaceParameters();
        currentAdvancedSearchParameters = new AdvancedSearchParameters();
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

    /**
     *
     * @param parentComponent
     * @return
     */
    public FindViewController init(final FindViewTopComponent parentComponent) {
        this.parentComponent = parentComponent;
        return instance;
    }

    /**
     * Returns a list of all available attributes on all open graphs. Used for
     * the attributes in the advanced find tab.
     *
     * @param type
     * @param attributeModificationCounter
     * @return
     */
    public List<Attribute> populateAllAttributes(final GraphElementType type, final long attributeModificationCounter) {
        final List<Attribute> allAttributes = new ArrayList<>();

        for (final Graph graph : GraphManager.getDefault().getAllGraphs().values()) {

            // Call the plugin that retrieves all current attributes on the graph
            final GraphAttributePlugin attrPlugin = new GraphAttributePlugin(type, allAttributes, attributeModificationCounter);
            final Future<?> future = PluginExecution.withPlugin(attrPlugin).interactively(true).executeLater(graph);

            // Wait for the search to find its results:
            try {
                future.get();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
            // get all the current attributes based on the currently selcted type
            allAttributes.addAll(attrPlugin.getAttributes());
        }
        final Set<Attribute> attributeSet = new LinkedHashSet<>();
        attributeSet.addAll(allAttributes);
        allAttributes.clear();
        allAttributes.addAll(attributeSet);
        allAttributes.sort(Comparator.comparing(Attribute::getName));

        return allAttributes;

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
        /**
         * Clear the attributes, create a attributeList to contain all of the
         * string attributes that are found, and create allAttributes to contain
         * a list of allAttributes regardless of type.
         */
        attributes.clear();
        final List<String> attributeList = new ArrayList<>();
        final List<Attribute> allAttributes = new ArrayList<>();

        // loop through all open graphs
        for (final Graph graph : GraphManager.getDefault().getAllGraphs().values()) {

            //Call the plugin that retrieves all current attributes on the graph
            final GraphAttributePlugin attrPlugin = new GraphAttributePlugin(type, allAttributes, attributeModificationCounter);
            final Future<?> future = PluginExecution.withPlugin(attrPlugin).interactively(true).executeLater(graph);

            // Wait for the search to find its results:
            try {
                future.get();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }

            // Retrieve a list of all String attributes
            if (attrPlugin.getAttributeModificationCounter() != attributeModificationCounter) {
                // Add all existing attributes to allAttributes
                allAttributes.addAll(attrPlugin.getAttributes());
                // loop through looking for all attributes of type string
                for (final Attribute a : allAttributes) {
                    // If its of type string add it to the attributes
                    // and attributeList
                    if (("string").equals(a.getAttributeType())) {
                        attributes.add(a);
                        attributeList.add(a.getName());
                    }
                }
            }
        }
        // Ensure only unique attributes are added to attributes.
        // There would be duplicates in searching multiple graphs
        final Set<Attribute> attributeSet = new LinkedHashSet<>();
        attributeSet.addAll(attributes);
        attributes.clear();
        attributes.addAll(attributeSet);

        // Ensure only unique attribute names are added to attributes.
        // There would be duplicates in searching multiple graphs
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
        if (ActiveFindResultsList.getBasicResultsList() != null && !currentBasicFindParameters.equals(parameters)) {
            ActiveFindResultsList.getBasicResultsList().clear();
            ActiveFindResultsList.getBasicResultsList().setCurrentIndex(-1);
        }
        currentBasicFindParameters.copyParameters(parameters);  
    }

    /**
     * Updates the controllers currentBasicReplaceParameters with the parameters
     * passed in
     *
     * @param parameters
     */
    public void updateBasicReplaceParameters(final BasicFindReplaceParameters parameters) {
        currentBasicReplaceParameters.copyParameters(parameters);
        
    }

    /**
     * Updates the controllers currentAdvancedSearchParameters with the
     * parameters passed in
     *
     * @param parameters
     */
    public void updateAdvancedSearchParameters(final AdvancedSearchParameters parameters) {
        if (ActiveFindResultsList.getAdvancedResultsList() != null && !currentAdvancedSearchParameters.equals(parameters)) {
            ActiveFindResultsList.getAdvancedResultsList().clear();
            ActiveFindResultsList.getAdvancedResultsList().setCurrentIndex(-1);
        }
        currentAdvancedSearchParameters.copyParameters(parameters);
    }

    /**
     * This function calls the basic find plugin, passing the
     * currentBasicFindParameters, whether to find all matching element, find the
     * next element or find the previous element.
     *
     * @param selectAll true if finding all graph elements
     * @param getNext true if finding the next element, false if the previous
     */
    public void retriveMatchingElements(final boolean selectAll, final boolean getNext, final boolean zoomToSelection) {
        final BasicFindPlugin basicFindPlugin = new BasicFindPlugin(currentBasicFindParameters, selectAll, getNext);
        final BasicFindGraphSelectionPlugin findGraphSelectionPlugin = new BasicFindGraphSelectionPlugin(currentBasicFindParameters, selectAll, zoomToSelection);
        setGraphsSearched(0);

        /**
         * If search all graphs is true, execute the find plugin on all open graphs. If not only call it on the active graph.
         */
        try {
            if (currentBasicFindParameters.isSearchAllGraphs()) {

                /**
                 * If there are a different number of graphs in this search than the previous one
                 * then reset the list of results 
                 */
                final int numberOfUniqueGraphs = GraphManager.getDefault().getAllGraphs().values().size();
                if (numberOfUniqueGraphs != ActiveFindResultsList.getUniqueGraphCount(ActiveFindResultsList.getBasicResultsList())) {
                    ActiveFindResultsList.setBasicResultsList(null);
                }

                for (final Graph currentGraph : GraphManager.getDefault().getAllGraphs().values()) {
                    // check to see the graph is not null
                    if (currentGraph != null) {
                        PluginExecution.withPlugin(basicFindPlugin).executeLater(currentGraph).get();
                    }
                    setGraphsSearched(getGraphsSearched() + 1);
                }
            } else {
                final Graph graph = GraphManager.getDefault().getActiveGraph();
                // check to see the graph is not null
                if (graph != null) {
                    PluginExecution.withPlugin(basicFindPlugin).executeLater(graph).get();
                }
                setGraphsSearched(1);
            }

            // Change the active graph to the one where the next result is
            if (!ActiveFindResultsList.getBasicResultsList().isEmpty()) {
                if (getNext) {
                    ActiveFindResultsList.getBasicResultsList().incrementCurrentIndex();
                } else {
                    ActiveFindResultsList.getBasicResultsList().decrementCurrentIndex();
                }

                final Graph graph = GraphManager.getDefault().getAllGraphs().get(ActiveFindResultsList.getBasicResultsList().get(ActiveFindResultsList.getBasicResultsList().getCurrentIndex()).getGraphId());
                PluginExecution.withPlugin(findGraphSelectionPlugin).executeLater(graph);
            }

            // Update the UI with how many results were found
            final int foundResultsLength = ActiveFindResultsList.getBasicResultsList().isEmpty() ? 0 : ActiveFindResultsList.getBasicResultsList().size();
            Platform.runLater(() -> FindViewController.getDefault().setNumResultsFound(foundResultsLength));

        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        } 
    }

    /**
     * This function calls the basic replace plugin, passing the
     * currentBasicReplaceParameters, whether to replace all matching element or
     * just replacing the next element.
     *
     * @param replaceAll true if replacing all matching elements
     * @param replaceNext true if replacing just the next element
     */
    public void replaceMatchingElements(final boolean replaceAll, final boolean replaceNext, final boolean zoomToSelection) {
        final ReplacePlugin basicReplacePlugin = new ReplacePlugin(currentBasicReplaceParameters, replaceAll, replaceNext, zoomToSelection);

        /**
         * If search all graphs is true, execute the replace plugin on all open
         * graphs. If not only call it on the active graph.
         */
        if (currentBasicReplaceParameters.isSearchAllGraphs()) {
            for (final Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
                // check to see the graph is not null
                if (graph != null && currentBasicReplaceParameters.isSearchAllGraphs()) {
                    PluginExecution.withPlugin(basicReplacePlugin).executeLater(graph);
                }
            }
        } else {
            final Graph graph = GraphManager.getDefault().getActiveGraph();
            // check to see the graph is not null
            if (graph != null) {
                PluginExecution.withPlugin(basicReplacePlugin).executeLater(graph);
            }
        }
    }

    public void retrieveAdvancedSearch(final boolean findAll, final boolean findNext, final boolean zoomToSelection) {
        final AdvancedSearchPlugin advancedSearchPlugin = new AdvancedSearchPlugin(currentAdvancedSearchParameters, findAll, findNext);
        final AdvancedFindGraphSelectionPlugin findGraphSelectionPlugin = new AdvancedFindGraphSelectionPlugin(currentAdvancedSearchParameters, findAll, findNext, zoomToSelection);
        setGraphsSearched(0);

        /**
         * If search all graphs is true, execute the advanced find plugin on all
         * open graphs. If not only call it on the active graph.
         */
        try {
            if (currentAdvancedSearchParameters.getSearchInLocation().equals(ALL_OPEN_GRAPHS)) {

                /**
                 * If there are a different number of graphs in this search than the previous one
                 * then reset the list of results
                 */
                final int numberOfUniqueGraphs = GraphManager.getDefault().getAllGraphs().values().size();
                if (numberOfUniqueGraphs != ActiveFindResultsList.getUniqueGraphCount(ActiveFindResultsList.getAdvancedResultsList())) {
                    ActiveFindResultsList.setAdvancedResultsList(null);
                }

                for (final Graph graph : GraphManager.getDefault().getAllGraphs().values()) {
                    // check to see the graph is not null
                    if (graph != null && currentAdvancedSearchParameters.getSearchInLocation().equals(ALL_OPEN_GRAPHS)) {
                        PluginExecution.withPlugin(advancedSearchPlugin).executeLater(graph).get();
                    }
                    setGraphsSearched(getGraphsSearched() + 1);
                }
            } else {
                final Graph graph = GraphManager.getDefault().getActiveGraph();
                // check to see the graph is not null
                if (graph != null) {
                    PluginExecution.withPlugin(advancedSearchPlugin).executeLater(graph).get();
                }
                setGraphsSearched(1);
            }
        } catch (final InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }

        // Change the active graph to the one where the next result is
        if (!ActiveFindResultsList.getAdvancedResultsList().isEmpty()) {
            if (findNext) {
                ActiveFindResultsList.getAdvancedResultsList().incrementCurrentIndex();
            } else {
                ActiveFindResultsList.getAdvancedResultsList().decrementCurrentIndex();
            }

            final int currentIndex = ActiveFindResultsList.getAdvancedResultsList().getCurrentIndex();
            final Graph graph = GraphManager.getDefault().getAllGraphs().get(ActiveFindResultsList.getAdvancedResultsList().get(currentIndex).getGraphId());

            PluginExecution.withPlugin(findGraphSelectionPlugin).executeLater(graph);
        }

        // Update the UI with how many results were found
        final int foundResultsLength = ActiveFindResultsList.getAdvancedResultsList().isEmpty() ? 0 : ActiveFindResultsList.getAdvancedResultsList().size();
        Platform.runLater(() -> FindViewController.getDefault().setNumResultsFound(foundResultsLength));
    }


    /**
     * Gets the controllers parent
     *
     * @return parentComponent
     */
    public FindViewTopComponent getParentComponent() {
        return parentComponent;
    }

    /**
     * Gets the current basic find parameters
     *
     * @return currentBasicFindParameters
     */
    public BasicFindReplaceParameters getCurrentBasicFindParameters() {
        return currentBasicFindParameters;
    }

    /**
     * Gets the current replace parameters
     *
     * @return currentBasicReplaceParameters
     */
    public BasicFindReplaceParameters getCurrentBasicReplaceParameters() {
        return currentBasicReplaceParameters;
    }

    /**
     * Gets the current advanced search parameters
     *
     * @return
     */
    public AdvancedSearchParameters getCurrentAdvancedSearchParameters() {
        return currentAdvancedSearchParameters;
    }

    /**
     * Gets the amount of results found by advanced search
     *
     * @return
     */
    public IntegerProperty getNumResultsFound() {
        return numResultsFoundFlag;
    }

    /**
     * Sets amount of results found by advanced search
     */
    public void setNumResultsFound(final int value) {
        numResultsFoundFlag.set(value);
    }

    /**
     * Create a dialog to show the user how many results are going to be deleted across the currently open graphs
     *
     * @param foundResults
     * @param graphsSearched
     */
    public void deleteResults(final FindResultsList foundResults, final int graphsSearched) {
        // Show user deletion dialog box
        final String message = String.format("%s results found across %s graph(s). Delete now?", foundResults.size(), graphsSearched);
        final Object[] options = new Object[]{DELETE, CANCEL};

        final NotifyDescriptor dialog = new NotifyDescriptor(message, "Delete Results", NotifyDescriptor.YES_NO_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE, options, DELETE);

        // If delete is chosen
        if (DialogDisplayer.getDefault().notify(dialog).equals(DELETE)) {
            for (final FindResult result : foundResults) {
                try {
                    // Delete each found result
                    final DeleteResultsPlugin deleteResultsPlugin = new DeleteResultsPlugin(result);
                    final Graph graph = GraphManager.getDefault().getAllGraphs().get(result.getGraphId());

                    PluginExecution.withPlugin(deleteResultsPlugin).executeLater(graph).get();

                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                }
            }
        }
    }
}
