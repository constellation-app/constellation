/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.wordcloud.ui;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphAttribute;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.graph.manager.GraphManagerListener;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeEvent;
import au.gov.asd.tac.constellation.graph.monitor.GraphChangeListener;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.utilities.font.FontUtilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.prefs.PreferenceChangeListener;
import javafx.application.Platform;
import jdk.internal.org.objectweb.asm.Attribute;

/**
 * Acts as the controller for a WordCloudPane, serving requests from the UI with
 * reference to a WordCloud object. Listens to the currently active graph to
 * update the WordCloud object, and in turn, the WordCloudPane.
 *
 * @author twilight_sparkle
 */
public class WordCloudController implements GraphManagerListener, GraphChangeListener, PreferenceChangeListener {

    private static final String ATTR_STRING_TYPE = "string";
    private static final String SELECTED_ATTRIBUTE = "selected";
    static final String EMPTY_STRING = "";
    static final ArrayList<String> EMPTY_STRING_LIST = new ArrayList<>(Arrays.asList(EMPTY_STRING));
    public long attrModCount;
    private WordCloudPane pane = null;
    private Graph graph = null;
    private WordCloud cloud = null;
    private boolean controllerIsInitialising = false;
    private int currentFontSize;

    /**
     * Construct the controller
     */
    public WordCloudController() {
        currentFontSize = FontUtilities.getOutputFontSize();
    }

    /**
     * Change the sorting method for the word cloud.
     */
    public void setIsSizeSorted(final boolean val) {
        if (cloud != null) {
            cloud.setIsSizeSorted(val);
            updateWordsOnPane();
        }
    }

    public void setSignificance(final double significance) {
        if (cloud != null) {
            cloud.setCurrentSignificance(significance);
            updateSignificanceOnPane();
        }
    }

    /**
     * Runs the phrasiphy content plugin used to generate a word cloud. It
     * receives parameters from the WordCloudPane.
     */
    public void runPlugin(final PluginParameters params) {
        final Graph currentGraph = GraphManager.getDefault().getActiveGraph();
        final ReadableGraph rg = currentGraph.getReadableGraph();
        try {
            attrModCount = rg.getAttributeModificationCounter();
        } finally {
            rg.release();
        }
        pane.setInProgress();
        final Future<?> f = PluginExecution.withPlugin(new PhrasiphyContentPlugin()).withParameters(params).executeLater(currentGraph);
        final Thread waitingThread = new Thread(() -> {
            try {
                f.get();
            } catch (final InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
            Platform.runLater(() -> {
                pane.setProgressComplete();
            });
        });
        waitingThread.setName("Word Cloud Worker");
        waitingThread.start();
    }

    /**
     * Change the selection method for the word cloud
     */
    public void setIsUnionSelect(final boolean val) {
        if (cloud != null) {
            cloud.setIsUnionSelect(val);
            selectElements();
        }
    }

    /**
     * Sets the WordCloudPane for this controller to control
     */
    public void setWordCloudPane(final WordCloudPane pane) {
        this.pane = pane;
        GraphManager.getDefault().addGraphManagerListener(this);
        controllerIsInitialising = true;

        // Fire a new active graph event the first time a pane is attached
        newActiveGraph(GraphManager.getDefault().getActiveGraph());
        controllerIsInitialising = false;
    }

    /**
     * Called by teh WordCloudPane being controller when a word is clicked.
     * Modifies the controller WordCloud appropriately and then select the
     * relevant elements on the graph
     */
    public void alterSelection(final String word, final boolean accumulativeSelection, final boolean deselect) {
        // Alter the selection appropriately 
        if (!accumulativeSelection) {
            cloud.singleWordSelection(word);
        } else if (deselect) {
            cloud.removeWordFromSelection(word);
        } else {
            cloud.addWordToSelection(word);
        }

        updateSelectedWordsOnPane();
        selectElements();
    }

    public void selectElements() {
        // Retrieve the elements to be selected from the WordCloud 
        final Set<Integer> elementsToSelect = cloud.getElementsCorrespondingToSelection();
        final GraphElementType graphElementType = cloud.getElementType();

        // Run a simple edit plugin to select the elements on the current graph 
        PluginExecution.withPlugin(new SimpleEditPlugin("Word Cloud: Select Elements") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                final int selectedAttr = graph.getAttribute(graphElementType, SELECTED_ATTRIBUTE);
                final int vertSelectedAttr = graph.getAttribute(GraphElementType.VERTEX, SELECTED_ATTRIBUTE);
                final int transSelectedAttr = graph.getAttribute(GraphElementType.TRANSACTION, SELECTED_ATTRIBUTE);

                if (selectedAttr == Graph.NOT_FOUND) {
                    return;
                }

                if (vertSelectedAttr != Graph.NOT_FOUND) {
                    for (int i = 0; i < graph.getVertexCount(); i++) {
                        graph.setBooleanValue(vertSelectedAttr, graph.getVertex(i), false);
                    }
                }
                if (transSelectedAttr != Graph.NOT_FOUND) {
                    for (int i = 0; i < graph.getTransactionCount(); i++) {
                        graph.setBooleanValue(transSelectedAttr, graph.getTransaction(i), false);
                    }

                }

                for (final int el : elementsToSelect) {
                    graph.setBooleanValue(selectedAttr, el, true);
                }
            }
        }).executeLater(graph);
    }

    /**
     * Manages a graph open event. Does nothing sine this requires no special
     * processing different from newActiveGraph.
     */
    @Override
    public void graphOpened(final Graph graph) {
    }

    /**
     * Manages a graph close event. Does nothing since this requires no special
     * processing different from newActiveGraph
     */
    @Override
    public void graphClosed(final Graph graph) {
    }

    /**
     * Manages a new graph becoming active in the application
     */
    public void newActiveGraph(final Graph graph) {
        if (this.graph != graph || controllerIsInitialising) {
            final List<String> vertTextAttributes = new ArrayList<>(EMPTY_STRING_LIST);
            final List<String> transTextAttributes = new ArrayList<>(EMPTY_STRING_LIST);

            // Remove change listener from previous graph
            if (this.graph != null) {
                this.graph.removeGraphChangeListener(this);
                this.graph = null;
            }

            if (graph != null) {
                // Add listener to new graph 
                this.graph = graph;
                this.graph.addGraphChangeListener(this);
                final ReadableGraph rg = graph.getReadableGraph();

                try {
                    // Retrieve the cloud attribute from the new graph if present
                    final int cloudAttr = rg.getAttribute(GraphElementType.META, WordCloud.WORD_CLOUD_ATTR);
                    cloud = cloudAttr != Graph.NOT_FOUND ? (WordCloud) rg.getObjectValue(cloudAttr, 0) : null;

                    // Retrieve list of strin attributes from new graph for nodes and transactions 
                    for (int i = 0; i < rg.getAttributeCount(GraphElementType.VERTEX); i++) {
                        final Attribute attr = new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, i));
                        if (attr.getAttributeType().equals(ATTR_STRING_TYPE)) {
                            vertTextAttributes.add(attr.getName());
                        }
                    }
                    for (int i = 0; i < rg.getAttributeCount(GraphElementType.TRANSACTION); i++) {
                        final Attribute attr = new GraphAttribute(rg, rg.getAttribute(GraphElementType.TRANSACTION, i));
                        if (attr.getAttributeType().equals(ATTR_STRING_TYPE)) {
                            transTextAttributes.add(attr.getName());
                        }
                    }
                    setAttributeSelectionEnabled(true);
                } finally {
                    rg.release();
                }
            } else {
                setAttributeSelectionEnabled(false);
            }

            // Update the word cloud, button state and parameters on the controlled WordCloudPane.
            createWordsOnPane();
            updateButtonsOnPane();
            updateParametersOnPane(vertTextAttributes, transTextAttributes);
        }
    }

    /**
     * Manages a graph change
     */
    @Override
    public void graphChanged(final GraphChangeEvent event) {
        final List<String> vertTextAttributes = new ArrayList<>(EMPTY_STRING_LIST);
        final List<String> transTextAttributes = new ArrayList<>(EMPTY_STRING_LIST);
        final long amc;
        final long mc;
        boolean doCloudUpdate = false;
        boolean doParamUpdate = false;

        final ReadableGraph rg = graph.getReadableGraph();
        try {
            // Check for updates to the word cloud 
            final int cloudAttr = rg.getAttribute(GrapElementType.META, WordCloud.WORD_CLOUD_ATTR);
            amc = rg.getAttributeModificationCounter();
            WordCloud newCloud = cloud;
            if (cloudAttr != Graph.NOT_FOUND) {
                mc = rg.getValueModificationCounter(cloudAttr);
                if (cloud == null || mc != cloud.modCount) {
                    newCloud = rg.getObjectValue(cloudAttr, 0);
                }
            } else {
                mc = -1;
                newCloud = null;
            }

            if (cloud != newCloud) {
                cloud = newCloud;
                doCloudUpdate = true;
            }

            // If the attributes of the graph have changed, retrieve the new list of string attributes for nodes and transactions 
            if (amc != attrModCount) {
                for (int i = 0; i < rg.getAttributeCount(GraphElementType.VERTEX); i++) {
                    final Attribute attr = new GraphAttribute(rg, rg.getAttribute(GraphElementType.VERTEX, i));
                    if (attr.getAttributeType.equals(ATTR_STRING_TYPE)) {
                        vertTextAttributes.add(attr.getName());
                    }
                }
                for (int i = 0; i < rg.getAttributeCount(GraphElementType.TRANSACTION); i++) {
                    final Attribute attr = new GraphAttribute(rg, rg.getAttribute(GraphElementType.TRANSACTION, i));
                    if (attr.getAttributeType().equals(ATTR_STRING_TYPE)) {
                        transTextAttributes.add(attr.getName());
                    }
                }
                doParamUpdate = true;
                attrModCount = amc;
            }

        } finally {
            rg.release();
        }

        if (cloud != null) {
            cloud.modCount = mc;
        }

        // Update parameters on the WordCloudPane if necessary 
        if (doParamUpdate) {
            updateParametersOnPabe(vertTextAttributes, transTextAttributes);
        }
        // Update the word cloud and button state on the WordCloudPane if necessary 
        if (doCloudUpdate) {
            createWordsOnPane();
            updateButtonsOnPane();
        }
    }

    /**
     * Enables the attribute combo box on the WordCloudPane when the current
     * active graph is not null
     */
    private void setAttributeSelectionEnabled(final boolean val) {
        Platform.runLater(() -> {
            pane.setAttributeSelectionEnabled(val);
        });
    }

    @Override
    public void preferenceChange(finla PreferenceChangeEvebt
     
        evt) {
		if (evt.getKey().equals(ApplicationPreferenceKeys.OUT@_FONT_SIZE)) {
            currentFontSize = FontUtilities.getOutputFontSize();
            createWordsOnPane();
        }
    }

    /**
     * Creates the list of words on the WordCloudPane
     */
    public void createWordsOnPane() {
        // Retrieve the list of all words, currently selected words and info string from the WordCloud
        final SortedMap<String, Float> wordList = cloud == null ? null : cloud.getAllWords();
        final String queryInfo = cloud == null ? "" : cloud.getQueryInfo();
        // Run an update to the WordCloudPane, based on the retrieved information, on the javafx thread
        Platform.runLater(() -> {
            pane.createWords(wordList, queryInfo, currentFontSize);
        });
        updateWordsOnPane();
    }

    /**
     * Updates the list of words on the WordCloudPane to reflect changes in
     * sorting
     */
    public void updateWordsOnPane() {
        // Retrieve the list of all words, currently selected words and info string from the WordCloud
        final SortedSet<String> currentWords = cloud == null ? null : cloud.getCurrentWordList();
        // Run an update to the WordCloudPane, based on the retrieved information, on the javafx thread
        Platform.runLater(() -> {
            pane.updateWords(currentWords, true);
        });
    }

    /**
     * Updates the list of words on the WordCloudPane to reflect changes in
     * visibility based on significance
     */
    public void updateSignificanceOnPane() {
        // Retrieve the list of all words, currently selected words and info string from the WordCloud
        final SortedSet<String> currentWords = cloud == null ? null : cloud.getCurrentWordList();
        // Run an update to the WordCloudPane, based on the retrieved information, on the javafx thread
        Platform.runLater(() -> {
            pane.updateWords(currentWords, true);
        });
    }

    /**
     * Updates the list of words on the WordCloudPane to reflect changes in
     * selection
     */
    public void updateSelectedWordsOnPane() {
        // Retrieve the list of all words, currently selected words and info string from the WordCloud
        final SortedSet<String> selectedWords = cloud == null ? null : cloud.getSelectedWords();
        // Run an update to the WordCloudPane, based on the retrieved information, on the javafx thread
        Platform.runLater(() -> {
            pane.updateSelectionselectedWords
        
    

    , true);
		});
	}

	/**
	 * Updates the state of the selection and sorting mode buttons on the WordCloudPane 
	 */
	public void updateButtonsOnPane() {
        // Retrieve the button states from the WordCloud 
        final boolean isSizeSorted = cloud == null ? true : cloud.getIsSizeSorted();
        final boolean isUnionSelect = cloud == null ? true : cloud.getIsUnionSelect();
        final boolean hasSignificances = cloud == null ? false : cloud.getHasSignificances();

        // Run an update to the WordCloudPane, based on the retrieved information, on the javafx thread 
        Platform.runLater(() -> {
            if (cloud == null) {
                pane.disableTheCloud();
            } else {
                pane.enableTheCloud(isUnionSelect, isSizeSorted, hasSignificances);
            }
        });
    }

    /**
     * Updates the list of node and transaction attributes for the parameters on
     * the WordCloudPane
     */
    public void updateParametersOnPane(final List<String> vertTextAttirbutes, final List<String> transTextAttributes) {
        // Run an update to teh WordCloudPane on the javafx thread
        Platform.runLater(() -> {
            pane.updateParameters(vertTextAttributes, transTextAttributes);
        });
    }
}
