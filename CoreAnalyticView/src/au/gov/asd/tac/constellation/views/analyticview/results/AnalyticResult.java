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
package au.gov.asd.tac.constellation.views.analyticview.results;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.analyticview.AnalyticViewController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The result of an AnalyticPlugin, which will be supported by one or more
 * AnalyticVisualisation.
 *
 * @author cygnus_x-1
 * @param <D>
 */
public abstract class AnalyticResult<D extends AnalyticData> {

    protected final Map<IdentificationData, D> result = new LinkedHashMap<>();
    protected final Map<String, String> metadata = new HashMap<>();
    protected boolean ignoreNullResults = false;
    protected AnalyticViewController analyticViewController = null;
    protected final List<ResultListener<D>> resultListeners = new ArrayList<>();

    public void setSelectionOnGraph(final Iterable<D> results) {
        final List<Integer> verticesToSelect = new ArrayList<>();
        final List<Integer> transactionsToSelect = new ArrayList<>();
        results.forEach(analyticResult -> {
            if (analyticResult.getElementType() == GraphElementType.VERTEX) {
                verticesToSelect.add(analyticResult.getElementId());
            } else if (analyticResult.getElementType() == GraphElementType.TRANSACTION) {
                transactionsToSelect.add(analyticResult.getElementId());
            }
        });
        analyticViewController.selectOnGraph(GraphElementType.VERTEX, verticesToSelect);
        analyticViewController.selectOnGraph(GraphElementType.TRANSACTION, transactionsToSelect);
    }

    public void setSelectionOnVisualisation(final GraphElementType elementType, final Collection<Integer> elementIds) {
        final List<D> selectedElementScores = new ArrayList<>();
        final List<D> ignoredElementScores = new ArrayList<>();
        result.values().forEach(elementScore -> {
            if (!elementType.equals(elementScore.getElementType())) {
                ignoredElementScores.add(elementScore);
            } else if (elementIds.contains(elementScore.getElementId())) {
                selectedElementScores.add(elementScore);
            }
        });
        resultListeners.forEach(listener -> listener.resultChanged(selectedElementScores, ignoredElementScores));
    }

    public final int size() {
        return result.size();
    }

    public final void sort() {

        final List<Entry<IdentificationData, D>> mapPairs = new ArrayList<>(result.entrySet());
        final List<D> mapValues = new ArrayList<>(result.values());
        mapValues.sort(null);

        result.clear();

        final Iterator<D> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            final D val = valueIt.next();

            final Iterator<Entry<IdentificationData, D>> pairIt = mapPairs.iterator();
            while (pairIt.hasNext()) {
                final Entry<IdentificationData, D> pair = pairIt.next();

                if (pair.getValue().equals(val)) {
                    mapPairs.remove(pair);
                    result.put(pair.getKey(), pair.getValue());
                    break;
                }
            }
        }
    }

    public final List<D> get() {
        return Collections.unmodifiableList(result.values().stream().toList());
    }

    public final Map<IdentificationData, D> getResult() {
        return result;
    }

    public void add(final D resultData) {
        this.result.put(resultData.getIdentificationData(), resultData);
    }

    public void addAll(final Iterable<D> results) {
        results.forEach(analyticResult -> this.result.put(analyticResult.getIdentificationData(), analyticResult));
    }

    public final boolean hasMetadata() {
        return !metadata.isEmpty();
    }

    public final Map<String, String> getMetadata() {
        return metadata;
    }

    public final void addMetadata(final String key, final String value) {
        this.metadata.put(key, value);
    }

    public final boolean isIgnoreNullResults() {
        return ignoreNullResults;
    }

    public final void setIgnoreNullResults(final boolean ignoreNullResults) {
        this.ignoreNullResults = ignoreNullResults;
    }

    public final void setAnalyticViewController(final AnalyticViewController analyticViewController) {
        this.analyticViewController = analyticViewController;
    }

    public final void addResultListener(final ResultListener<D> listener) {
        this.resultListeners.add(listener);
    }

    @FunctionalInterface
    public static interface ResultListener<D> {

        public void resultChanged(final List<D> changedItems, final List<D> ignoredItems);
    }
}
