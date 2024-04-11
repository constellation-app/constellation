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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.AnalyticConcept;
import au.gov.asd.tac.constellation.graph.schema.analytic.concept.ClusteringConcept;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.Plugin;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginGraphs;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginNotificationLevel;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.plugins.templates.SimplePlugin;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.histogram.access.HistogramAccess;
import au.gov.asd.tac.constellation.views.namedselection.NamedSelectionManager;
import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.FollowUpChoice;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/*
 * @author twilight_sparkle
 */
public class ContentAnalysisGraphProcessing {

    private static final Logger LOGGER = Logger.getLogger(ContentAnalysisGraphProcessing.class.getName());
    
    private final Graph graph;
    private final GraphElementType graphElementType;
    private final List<Plugin> followUpPlugins = new LinkedList<>();

    private ContentAnalysisGraphProcessing(final Graph graph) {
        this.graph = graph;
        this.graphElementType = GraphElementType.VERTEX;
    }

    public static void makeSelectionsFromClusters(final Graph graph, final Map<Integer, Integer> elementToCluster) {
        final ContentAnalysisGraphProcessing cagp = new ContentAnalysisGraphProcessing(graph);
        cagp.followUpPlugins.add(cagp.new MakeSelectionsPlugin(elementToCluster));
        cagp.performFollowUp();
    }

    public static void recordClusters(final Graph graph, final Map<Integer, Integer> elementToCluster) {
        final ContentAnalysisGraphProcessing cagp = new ContentAnalysisGraphProcessing(graph);
        cagp.followUpPlugins.add(cagp.new ClusterElementsPlugin(elementToCluster));
        cagp.followUpPlugins.add(cagp.new ShowClustersOnHistogramPlugin());
        cagp.performFollowUp();
    }

    public static void addTransactionsForPairwiseSimilarities(final Graph graph, final List<ElementSimilarity> pairwiseSimilarities) {
        final ContentAnalysisGraphProcessing cagp = new ContentAnalysisGraphProcessing(graph);
        cagp.followUpPlugins.add(cagp.new AddTransactionsPlugin(pairwiseSimilarities));
        cagp.performFollowUp();
    }

    public ContentAnalysisGraphProcessing(final Graph graph, final ContentVectorClusteringServices cvcs, final GraphElementType elementType, final FollowUpChoice followUpChoice) {
        this.graphElementType = elementType;
        this.graph = graph;
        switch (followUpChoice) {
            case MAKE_SELECTIONS -> followUpPlugins.add(new MakeSelectionsPlugin(cvcs.getClusters()));
            case CLUSTER -> {
                followUpPlugins.add(new ClusterElementsPlugin(cvcs.getClusters()));
                followUpPlugins.add(new ShowClustersOnHistogramPlugin());
            }
            default -> {
                // Do nothing 
            }
        }
    }

    public void performFollowUp() {
        for (final Plugin followUpPlugin : followUpPlugins) {
            final Future<?> f = PluginExecution.withPlugin(followUpPlugin).executeLater(graph);
            try {
                f.get();
            } catch (final InterruptedException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
                Thread.currentThread().interrupt();
            } catch (final ExecutionException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
            }
        }
    }

    @PluginInfo(pluginType = PluginType.NONE, tags = {"LOW LEVEL"})
    private class ShowClustersOnHistogramPlugin extends SimplePlugin {

        private static final String PLUGIN_NAME = "Show Clusters on Histogram";

        @Override
        public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {

            SwingUtilities.invokeLater(() -> {
                final HistogramAccess ha = HistogramAccess.getAccess();
                ha.requestHistogramActive();
                ha.setHistogramAttribute(graphElementType, ClusteringConcept.VertexAttribute.NAMED_CLUSTER.getName());
            });
        }

        @Override
        public String getName() {
            return PLUGIN_NAME;
        }
    }

    @PluginInfo(pluginType = PluginType.NONE, tags = {"LOW LEVEL"})
    private class ClusterElementsPlugin extends SimpleEditPlugin {

        private static final String PLUGIN_NAME = "Cluster Similar Elements";
        private final Map<Integer, Integer> elementToCluster;

        public ClusterElementsPlugin(final Map<Integer, Integer> elementToCluster) {
            this.elementToCluster = elementToCluster;
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            // Create the cluster attribtue if it is not already present in the rgaph, or if it was, reset all vertices to be unclustered.
            final int clusterElementAttr = ClusteringConcept.VertexAttribute.NAMED_CLUSTER.ensure(wg);
            int elementCount;
            elementCount = switch (graphElementType) {
                case VERTEX -> wg.getVertexCount();
                case TRANSACTION -> wg.getTransactionCount();
                default -> 0;
            };

            for (int i = 0; i < elementCount; i++) {
                int element;
                element = switch (graphElementType) {
                    case VERTEX -> wg.getVertex(i);
                    case TRANSACTION -> wg.getTransaction(i);
                    default -> -1;
                };
                wg.setStringValue(clusterElementAttr, element, "no cluster");
            }

            // Record the clusters 
            final Iterator<Integer> iter = elementToCluster.keySet().iterator();
            while (iter.hasNext()) {
                final int elID = iter.next();
                wg.setStringValue(clusterElementAttr, elID, elementToCluster.get(elID).toString());

            }
        }

        @Override
        public String getName() {
            return PLUGIN_NAME;
        }
    }

    @PluginInfo(pluginType = PluginType.NONE, tags = {"LOW LEVEL"})
    private class MakeSelectionsPlugin extends SimplePlugin {

        private static final String SELECTION_NAME_PREFIX = "n-gram similarity cluster ";
        private static final String PLUGIN_NAME = "Create Named Selections from N-Grammatically Similar Vertices";
        private final Map<Integer, Integer> elementToCluster;

        public MakeSelectionsPlugin(final Map<Integer, Integer> elementToCluster) {
            this.elementToCluster = new HashMap<>(elementToCluster);
        }

        @Override
        public void execute(final PluginGraphs graphs, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
            final Map<Integer, List<Integer>> namedSelections = new ConcurrentHashMap<>();
            Iterator<Integer> iter = elementToCluster.keySet().iterator();
            while (iter.hasNext()) {
                final int vxID = iter.next();
                final int cluster = elementToCluster.get(vxID);

                List<Integer> selection = namedSelections.get(cluster);
                if (selection == null) {
                    selection = new LinkedList<>();
                }

                selection.add(vxID);
                namedSelections.put(cluster, selection);
            }

            // Create a named selection from each cluster 
            boolean selectionCreationSuccess = true;
            iter = namedSelections.keySet().iterator();
            int selectionNumber = 0;
            while (iter.hasNext()) {
                final List<Integer> selection = namedSelections.get(iter.next());
                final int[] selectionArray = new int[selection.size()];
                final Iterator<Integer> listIter = selection.iterator();
                int i = 0;
                while (listIter.hasNext()) {
                    selectionArray[i++] = listIter.next();
                }
                selectionCreationSuccess = NamedSelectionManager.getDefault().createCustomNamedSelection(selectionArray, null, selectionCreationSuccess, SELECTION_NAME_PREFIX + (++selectionNumber));
            }
        }

        @Override
        public String getName() {
            return PLUGIN_NAME;
        }
    }

    @PluginInfo(pluginType = PluginType.NONE, tags = {"LOW LEVEL"})
    private class AddTransactionsPlugin extends SimpleEditPlugin {

        private static final String SIMILARITY_ATTRIBUTE_NAME = "n-gram Similarity";
        private static final String SIMILARITY_ATTR_TYPE = "float";
        private static final String SIMILARITY_ATTR_DESCRIPTION = "The N-Gramatic Similarity between the selectors represented by the two nodes";
        private static final String SUBTYPE_ATTRIBUTE_NAME = "SubType";
        private static final String SUBTYPE_ATTR_TYPE = "string";
        private static final String SUBTYPE_ATTR_DESCRIPTION = "Holds the hierachy of types from the top level down";
        private static final String NAME_ATTRIBUTE_NAME = "Name";
        private static final String NAME_ATTR_TYPE = "string";
        private static final String NAME_ATTR_DESCRIPTION = "Name";
        private static final String NGRAMSIMILARTYPE_VALUE = "SIMILARITY.nGrammaticallySimilar";
        private static final String SIMILARTYPE_VALUE = "SIMILARITY";
        private static final String LINK_NAME_PREFIX = "n-gram similarity plugin: ";
        private static final String PLUGIN_NAME = "Add Transactions Between N-Grammatically Similar Vertices";
        private static final String TOO_MANY_TRANSACTIONS_ERROR_MESSAGE = "Too manay transactions would be added. Try a smaller set of nodes or a different action to represent n-gram similarity.";
        private static final int MAX_TRANSACTIONS_TO_ADD = 1000000;

        private final List<ElementSimilarity> pairwiseSimilarities;

        public AddTransactionsPlugin(final List<ElementSimilarity> pairwiseSimilarities) {
            this.pairwiseSimilarities = new ArrayList<>(pairwiseSimilarities);
        }

        @Override
        public void edit(final GraphWriteMethods wg, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException, PluginException {
            final String currentDateTime = new Date(System.currentTimeMillis()).toString();
            final String linkName = LINK_NAME_PREFIX + currentDateTime;

            if (pairwiseSimilarities.size() > MAX_TRANSACTIONS_TO_ADD) {
                throw new PluginException(PluginNotificationLevel.WARNING, TOO_MANY_TRANSACTIONS_ERROR_MESSAGE);
            }

            final int nGramSimilarityAttr = wg.addAttribute(GraphElementType.TRANSACTION, SIMILARITY_ATTR_TYPE, SIMILARITY_ATTRIBUTE_NAME, SIMILARITY_ATTR_DESCRIPTION, 0F, null);
            final int typeAttr = AnalyticConcept.TransactionAttribute.TYPE.ensure(wg);
            final int subtypeAttr = wg.addAttribute(GraphElementType.TRANSACTION, SUBTYPE_ATTR_TYPE, SUBTYPE_ATTRIBUTE_NAME, SUBTYPE_ATTR_DESCRIPTION, null, null);
            final int name = wg.addAttribute(GraphElementType.TRANSACTION, NAME_ATTR_TYPE, NAME_ATTRIBUTE_NAME, NAME_ATTR_DESCRIPTION, null, null);
            final int colorAttr = VisualConcept.TransactionAttribute.COLOR.ensure(wg);

            int currentLinkNum = 0;

            for (final ElementSimilarity entry : pairwiseSimilarities) {
                final int srcID = entry.low;
                final int destID = entry.high;
                final double similarity = entry.score;

                // Add the transaction
                final int link = wg.addTransaction(srcID, destID, false);

                // Add the relevant attributes to the transaction
                wg.setDoubleValue(nGramSimilarityAttr, link, similarity);
                wg.setStringValue(typeAttr, link, SIMILARTYPE_VALUE);
                wg.setStringValue(subtypeAttr, link, NGRAMSIMILARTYPE_VALUE);
                wg.setStringValue(name, link, linkName + currentLinkNum++);
                wg.setObjectValue(colorAttr, link, ConstellationColor.PURPLE);
            }
        }

        @Override
        public String getName() {
            return PLUGIN_NAME;
        }
    }
}
