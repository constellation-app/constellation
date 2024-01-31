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
package au.gov.asd.tac.constellation.views.wordcloud.content;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import au.gov.asd.tac.constellation.views.wordcloud.phraseanalysis.PhrasiphyContentParameters;
import au.gov.asd.tac.constellation.views.wordcloud.ui.WordCloud;
import au.gov.asd.tac.constellation.views.wordcloud.ui.WordCloudAttributeDescription;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author twilight_sparkle
 */
public class ContentAnalysisManager {

    private static final Logger LOGGER = Logger.getLogger(ContentAnalysisManager.class.getName());

    private static final int AVAILABLE_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    private static final int MAX_THRESHOLD = 50; // The maximum number of items to assign each thread (until we don't have enough threads anyway).
    private final Graph graph;
    private final int[] performOnElements;
    private final Set<Integer> elementsOfInterest;
    private final int graphElementCapacity;
    private final int querySize;
    private final int performOnAttributeID;
    private final GraphElementType elementType;

    public ContentAnalysisManager(final Graph graph, final int[] performOnElements, final Set<Integer> elementsOfInterest, final int graphElementCapacity, final GraphElementType elementType, final int performOnAttributeID) {
        this.graph = graph;
        this.performOnElements = performOnElements.clone();
        this.elementsOfInterest = new HashSet<>(elementsOfInterest);
        this.graphElementCapacity = graphElementCapacity;
        this.performOnAttributeID = performOnAttributeID;
        this.elementType = elementType;
        querySize = performOnElements.length;
    }

    protected class StringListThreadedPhraseAdaptor extends ThreadedPhraseAdaptor {

        private final int elLowPosition;
        private int elCurrentPosition;
        private final List<String> list;
        private final int workload;

        protected StringListThreadedPhraseAdaptor(final ThreadAllocator allocator, final List<String> list) {
            this.list = new ArrayList<>(list);
            elCurrentPosition = elLowPosition = allocator.getLowerPos();
            workload = allocator.getWorkload();
        }

        @Override
        public boolean hasNextPhrase() {
            return elCurrentPosition < (elLowPosition + workload);
        }

        @Override
        public String getNextPhrase() {
            return list.get(elCurrentPosition++);
        }

        @Override
        public int getCurrentElementID() {
            return elCurrentPosition - 1;
        }

        @Override
        public void connect() {
            // Does nothing as not required
        }

        @Override
        public void disconnect() {
            // Does nothing as not required
        }

        @Override
        public int getWorkload() {
            return workload;
        }
    }

    public ThreadAllocator getStringListAllocator(final List<String> list) {
        return ThreadAllocator.buildThreadAllocator(AVAILABLE_THREADS, MAX_THRESHOLD, list.size(), (final ThreadAllocator forAllocator) -> new StringListThreadedPhraseAdaptor(forAllocator, list));
    }

    private class GraphElementThreadedPhraseAdaptor extends ThreadedPhraseAdaptor {

        private final int elLowPosition;
        private int elCurrentPosition;
        private ReadableGraph rg;
        private final int workload;

        private GraphElementThreadedPhraseAdaptor(final ThreadAllocator allocator) {
            elCurrentPosition = elLowPosition = allocator.getLowerPos();
            workload = allocator.getWorkload();
        }

        @Override
        public boolean hasNextPhrase() {
            return elCurrentPosition < (elLowPosition + workload);
        }

        @Override
        public String getNextPhrase() {
            return rg.getStringValue(performOnAttributeID, performOnElements[elCurrentPosition++]);
        }

        @Override
        public int getCurrentElementID() {
            return performOnElements[elCurrentPosition - 1];
        }

        @Override
        public void connect() {
            rg = graph.getReadableGraph();
        }

        @Override
        public void disconnect() {
            rg.release();
        }

        @Override
        public int getWorkload() {
            return workload;
        }
    }

    public ThreadAllocator getGraphElementThreadAllocator() {
        return ThreadAllocator.buildThreadAllocator(AVAILABLE_THREADS, MAX_THRESHOLD, querySize, (final ThreadAllocator forAllocator) -> new GraphElementThreadedPhraseAdaptor(forAllocator));
    }

    public void clusterDocuments(final ClusterDocumentsParameters clusterDocumentsParams) {
        final ThreadAllocator allocator = getGraphElementThreadAllocator();
        final DefaultTokenHandler th = new DefaultTokenHandler();
        ContentTokenizingServices.createDocumentClusteringTokenizingService(th, clusterDocumentsParams, allocator);

        ContentVectorClusteringServices cvcs = ContentVectorClusteringServices.createKMeansClusteringService(th, clusterDocumentsParams, querySize);
        cvcs.createAndRunThreads(allocator);

        ContentAnalysisGraphProcessing gp = new ContentAnalysisGraphProcessing(graph, cvcs, elementType, clusterDocumentsParams.getFollowUpChoice());
        gp.performFollowUp();
    }

    public void compareNodesWithNGrams(final NGramAnalysisParameters nGramParams) {
        final ThreadAllocator allocator = getGraphElementThreadAllocator();
        final PairwiseComparisonTokenHandler th = new PairwiseComparisonTokenHandler(graphElementCapacity, elementsOfInterest);
        ContentTokenizingServices.computeNGrams(th, nGramParams, allocator);

        if (nGramParams.getFollowUpChoice().equals(ContentAnalysisOptions.FollowUpChoice.ADD_TRANSACTIONS)) {
            final List<ElementSimilarity> pairwiseSimilarities = ContentPairwiseSimilarityServices.scoreSimilarPairs(th, nGramParams);
            ContentAnalysisGraphProcessing.addTransactionsForPairwiseSimilarities(graph, pairwiseSimilarities);
            return;
        }

        final Map<Integer, Integer> elementToCluster = ContentPairwiseSimilarityServices.clusterSimilarElements(th, nGramParams);

        if (nGramParams.getFollowUpChoice().equals(ContentAnalysisOptions.FollowUpChoice.CLUSTER)) {
            ContentAnalysisGraphProcessing.recordClusters(graph, elementToCluster);
        } else if (nGramParams.getFollowUpChoice().equals(ContentAnalysisOptions.FollowUpChoice.MAKE_SELECTIONS)) {
            ContentAnalysisGraphProcessing.makeSelectionsFromClusters(graph, elementToCluster);
        }
    }

    // TODO: Implement plugin class and plugin params, then test, then gui!
    public void phrasiphyContent(final PhrasiphyContentParameters phrasiphyContentParams) {
        phrasiphyContent(phrasiphyContentParams, null);
    }

    private List<String> processBackground(final File file) {
        if (file == null) {
            return Collections.emptyList();
        }
        final List<String> lines = new ArrayList<>();

        try (final BufferedReader background = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8.name()))) {
            String line = "";
            while (line != null) {
                lines.add(line);
                line = background.readLine();
            }
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage());
        }
        return lines;
    }

    public void phrasiphyContent(final PhrasiphyContentParameters phrasiphyContentParams, final File background) {
        final PhraseTokenHandler bgHandler;
        final List<String> lines = processBackground(background);
        if (lines != null) {
            final ThreadAllocator bgAllocator = getStringListAllocator(lines);
            bgHandler = new PhraseTokenHandler();
            ContentTokenizingServices.createPhraseAnalysisTokenizingService(bgHandler, phrasiphyContentParams, bgAllocator);
        } else {
            bgHandler = null;
        }

        final ThreadAllocator allocator = getGraphElementThreadAllocator();
        final PhraseTokenHandler handler = new PhraseTokenHandler();
        ContentTokenizingServices.createPhraseAnalysisTokenizingService(handler, phrasiphyContentParams, allocator);

        final Future<?> f = PluginExecution.withPlugin(new SimpleEditPlugin("Display Word Cloud") {
            @Override
            public void edit(final GraphWriteMethods graph, final PluginInteraction interaction, final PluginParameters parameters) throws InterruptedException {
                int cloudAttr = graph.getAttribute(GraphElementType.META, WordCloud.WORD_CLOUD_ATTR);
                final WordCloud wordCloud;
                if (cloudAttr == Graph.NOT_FOUND) {
                    cloudAttr = graph.addAttribute(GraphElementType.META, WordCloudAttributeDescription.ATTRIBUTE_NAME, WordCloud.WORD_CLOUD_ATTR, WordCloud.WORD_CLOUD_DESCR, null, null);
                    wordCloud = new WordCloud(handler, bgHandler, elementType, phrasiphyContentParams.getThreshold(), phrasiphyContentParams.hasFilterAllWords());
                } else {
                    wordCloud = new WordCloud(handler, bgHandler, elementType, phrasiphyContentParams.getThreshold(), phrasiphyContentParams.hasFilterAllWords(), (WordCloud) graph.getObjectValue(cloudAttr, 0));
                }

                LOGGER.log(Level.SEVERE, "Word cloud has been created");
                wordCloud.setQueryInfo(phrasiphyContentParams.getPhraseLength(), phrasiphyContentParams.getProximity(), graph.getAttributeName(phrasiphyContentParams.getOnAttributeID()));
                graph.setObjectValue(cloudAttr, 0, wordCloud);
            }
        }).executeLater(graph);

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
