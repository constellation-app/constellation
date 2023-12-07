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

import au.gov.asd.tac.constellation.views.wordcloud.content.ContentAnalysisOptions.TokenThresholdMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 *
 * @author twilight_sparkle
 */
public class ContentVectorClusteringServices {

    private VectorWeightingCalculator weightingCalculator;
    private Clustering clusteringMethod;
    private final SparseMatrix<Integer> tokenElementMatrix;
    private final SparseMatrix<Float> elementTokenMatrix;
    private final Map<Integer, Integer> moduli;
    private final Map<Integer, Integer> elementToCluster;
    private final Map<Integer, List<Integer>> clusters;
    private final List<Integer> clusterCentres;

    private ContentVectorClusteringServices(final SparseMatrix<Integer> tokenElementMatrix) {
        this.tokenElementMatrix = tokenElementMatrix;
        this.elementTokenMatrix = SparseMatrix.constructMatrix((Float) 0F);
        this.elementToCluster = new ConcurrentSkipListMap<>();
        this.clusterCentres = new ArrayList<>();
        moduli = new ConcurrentSkipListMap<>();
        clusters = new ConcurrentSkipListMap<>();
    }

    public enum WeightingMethod {
        APPEARANCE,
        RANK;
    }

    public static ContentVectorClusteringServices createKMeansClusteringService(final DefaultTokenHandler handler, final ClusterDocumentsParameters clusterDocumentsParams, final int numberOfElements) {
        final ContentVectorClusteringServices cvcs = new ContentVectorClusteringServices(handler.getTokenElementMatrix());
        cvcs.clusteringMethod = cvcs.new KMeansClustering(clusterDocumentsParams.getNumberOfMeans());
        if (clusterDocumentsParams.getThresholdMethod().equals(TokenThresholdMethod.APPEARANCE)) {
            cvcs.weightingCalculator = cvcs.new CountAppearancesCalculator(numberOfElements, clusterDocumentsParams.getThresholdPercentage(), true, clusterDocumentsParams.isBinarySpace(), clusterDocumentsParams.isSignificantAboveThreshold(), clusterDocumentsParams.getWeightingExponent());
        } else {
            cvcs.weightingCalculator = cvcs.new RankTokenCalculator(numberOfElements, cvcs.tokenElementMatrix.getNumColumns(), clusterDocumentsParams.getThresholdPercentage(), true, clusterDocumentsParams.isBinarySpace(), clusterDocumentsParams.isSignificantAboveThreshold(), clusterDocumentsParams.getWeightingExponent());
        }
        return cvcs;
    }

    private interface Clustering {

        public void cluster();
    }

    public Map<Integer, Integer> getClusters() {
        return new HashMap<>(elementToCluster);
    }

    private class KMeansClustering implements Clustering {

        private final int k;

        public KMeansClustering(final int k) {
            this.k = k;
        }

        @Override
        public void cluster() {
            // Picking first column index and setting up general infrastructure
            final Integer[] columnKeys = elementTokenMatrix.getColumnKeys();
            final int numberOfColumns = elementTokenMatrix.getNumColumns();
            if (numberOfColumns == 0) {
                return;
            }

            final Random r = new Random();
            final int firstCentreIndex = r.nextInt(numberOfColumns);
            final float[] minimumDistances = new float[numberOfColumns];
            clusterCentres.add(columnKeys[firstCentreIndex]);

            // K-Means++ initialisation
            for (int i = 1; i < k; i++) {
                final ProbabilityDensityFunction pdf = new ProbabilityDensityFunction(numberOfColumns);
                float totalDistance = 0;
                for (int j = 0; j < columnKeys.length; j++) {
                    final Iterator<Integer> iter = clusterCentres.iterator();
                    float smallestDistanceSoFar = Float.MAX_VALUE;
                    while (iter.hasNext()) {
                        final int centreKey = iter.next();
                        final float currentDistance = elementTokenMatrix.getCommonalityDistanceBetweenColumns(centreKey, columnKeys[j]);
                        if (currentDistance < smallestDistanceSoFar) {
                            smallestDistanceSoFar = currentDistance;
                        }
                    }
                    minimumDistances[j] = smallestDistanceSoFar;
                    totalDistance += smallestDistanceSoFar;
                }
                for (int j = 0; j < columnKeys.length; j++) {
                    pdf.addProbability(minimumDistances[j] / (double) totalDistance);
                }
                clusterCentres.add(columnKeys[pdf.sample(r.nextDouble())]);
            }

            float lastTotalError;
            float totalError = Float.MAX_VALUE;
            // K-Means iteration - error must reduce by at least ten percetn to continue
            do {
                // reassign elements to clusters
                clusters.clear();
                lastTotalError = totalError;
                totalError = 0;

                for (int j = 0; j < columnKeys.length; j++) {
                    final Iterator<Integer> iter = clusterCentres.iterator();
                    float smallestDistanceSoFar = Float.MAX_VALUE;
                    int closestCentre = -1;
                    while (iter.hasNext()) {
                        final int centreKey = iter.next();
                        final float currentDistance = elementTokenMatrix.getCommonalityDistanceBetweenColumns(centreKey, columnKeys[j]);
                        if (currentDistance < smallestDistanceSoFar || (currentDistance == smallestDistanceSoFar && r.nextBoolean())) {
                            smallestDistanceSoFar = currentDistance;
                            closestCentre = centreKey;
                        }
                    }

                    totalError += Math.pow(smallestDistanceSoFar, 2);
                    elementToCluster.put(columnKeys[j], clusterCentres.indexOf(closestCentre));
                    List<Integer> clusterList = clusters.get(closestCentre);
                    if (clusterList == null) {
                        clusterList = new LinkedList<>();
                    }

                    clusterList.add(columnKeys[j]);
                    clusters.put(closestCentre, clusterList);
                }

                // Update the cluster centre
                clusterCentres.clear();
                int clusterNumber = 0;
                final Iterator<Integer> iter = clusters.keySet().iterator();
                while (iter.hasNext()) {
                    final List<Integer> clusterList = clusters.get(iter.next());
                    elementTokenMatrix.calculateCentreOfColumns(clusterList.toArray(new Integer[0]), --clusterNumber);
                    clusterCentres.add(clusterNumber);
                }
            } while (totalError <= lastTotalError * 0.9F && totalError != 0);
        }
    }

    private class ProbabilityDensityFunction {

        private final double[] function;
        private int currentIndex;

        public ProbabilityDensityFunction(final int sampleSize) {
            this.function = new double[sampleSize];
            currentIndex = -1;
        }

        public void addProbability(final double probability) {
            final double cumulativeProbability = (currentIndex == -1) ? 0 : function[currentIndex];
            function[++currentIndex] = cumulativeProbability + probability;
        }

        public int sample(final double roll) {
            for (int i = 0; i < function.length; i++) {
                if (function[i] > +roll) {
                    return i;
                }
            }
            return function.length - 1;
        }
    }

    public void createAndRunThreads(final ThreadAllocator allocator) {
        allocator.resetThreadAllocation(tokenElementMatrix.getNumColumns());
        while (allocator.hasMore()) {
            final ThreadedElementVectorComputation vectorComp = new ThreadedElementVectorComputation(allocator);
            final Thread t = new Thread(vectorComp);
            t.start();
            allocator.indicateAllocated();
        }
        allocator.waitOnOthers();
        clusteringMethod.cluster();
    }

    private class ThreadedElementVectorComputation implements Runnable {

        private final ThreadAllocator allocator;
        private final int threadID;
        private final int tokenLowPos;
        private final int workLoad;

        public ThreadedElementVectorComputation(final ThreadAllocator allocator) {
            this.allocator = allocator;
            this.threadID = allocator.getNumAllocated();
            tokenLowPos = allocator.getLowerPos();
            workLoad = allocator.getWorkload();
        }

        @Override
        public void run() {
            Thread.currentThread().setName("ContentAnalysis.TokenElementComputation.Thread." + threadID);
            computeElements();
            allocator.waitOnOthers();
        }

        private void computeElements() {
            final Integer[] tokensKeySet = tokenElementMatrix.getColumnKeys();
            // For each token bin in this thread's work load
            for (int tokenBinPos = tokenLowPos; tokenBinPos < tokenLowPos + workLoad; tokenBinPos++) {
                // Iterator over the list of elements seen with this token
                final int token = tokensKeySet[tokenBinPos];
                final SparseMatrix<Integer>.MatrixColumnIterator tokenIter = tokenElementMatrix.getColumn(token);
                while (tokenIter.hasNext()) {
                    SparseMatrix.ElementValuePair<Integer> element = tokenIter.next();
                    // Calculate the modulii of each element
                    updateModulii(element, token, tokenIter);
                    // Perform algorithm specific processing of each element
                    processElementsSeenWithToken(element, token, tokenIter);
                }
            }
        }

        protected void updateModulii(final SparseMatrix.ElementValuePair<Integer> element, final int token, final SparseMatrix<Integer>.MatrixColumnIterator tokenIter) {
            // The modulus of a token is the total number of elements that were seen with it.
            moduli.put(token, tokenIter.getSize());
        }

        protected void processElementsSeenWithToken(final SparseMatrix.ElementValuePair<Integer> element, final int token, final SparseMatrix<Integer>.MatrixColumnIterator tokenIter) {
            synchronized (elementTokenMatrix) {
                // Record the number of times the element occured witht the token
                if (weightingCalculator.isSignificantEntry(moduli.get(token))) {
                    elementTokenMatrix.putCell(element.el, token, weightingCalculator.getWeight(element.val, moduli.get(token)));
                }
            }
        }
    }

    private class VectorWeightingCalculator {

        private final boolean binarySpace;
        protected int threshold;
        private final float weightingExponent;
        private final int numberOfElements;
        private final boolean significantAboveThreshold;

        public VectorWeightingCalculator(final boolean binarySpace, final int numberOfElements, final boolean significantAboveThreshold, final float weightingExponent) {
            this.binarySpace = binarySpace;
            this.numberOfElements = numberOfElements;
            this.significantAboveThreshold = significantAboveThreshold;
            this.weightingExponent = weightingExponent;
        }

        public VectorWeightingCalculator(final boolean binarySpace, final int numberOfElements, final boolean significantAboveThreshold) {
            this(binarySpace, numberOfElements, significantAboveThreshold, 0);
        }

        public float getWeight(final int elementFrequency, final int tokenFrequency) {
            final float weighting = (float) Math.pow(tokenFrequency / (float) numberOfElements, weightingExponent);
            return binarySpace ? weighting : elementFrequency * weighting;
        }

        public boolean isSignificantEntry(final int tokenFrequency) {
            return ((tokenFrequency >= threshold) == significantAboveThreshold);
        }
    }

    private class CountAppearancesCalculator extends VectorWeightingCalculator {

        public CountAppearancesCalculator(final int numberOfElements, final int threshold, final boolean thresholdIsPercentage, final boolean binarySpace, final boolean significantAboveThreshold, final float weightingExponent) {
            super(binarySpace, numberOfElements, significantAboveThreshold, weightingExponent);
            this.threshold = thresholdIsPercentage ? (threshold * numberOfElements) / 100 : threshold;
        }
    }

    private class RankTokenCalculator extends VectorWeightingCalculator {

        // If thresholdIsPercentage is true, then threshold is an integer between 1 and 100 representing a percentage
        public RankTokenCalculator(final int numberOfElements, final int numberOfFeatures, final int threshold, final boolean thresholdIsPercentage, final boolean binarySpace, final boolean significantAboveThreshold, final float weightingExponent) {
            super(binarySpace, numberOfElements, significantAboveThreshold, weightingExponent);
            final int thresholdIndex = thresholdIsPercentage ? (threshold * numberOfFeatures) / 100 : threshold;
            this.threshold = (Integer) moduli.values().toArray()[thresholdIndex];
        }
    }
}
