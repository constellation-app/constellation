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
package au.gov.asd.tac.constellation.views.analyticview.utilities;

import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.questions.AnalyticQuestionDescription;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.translators.GraphVisualisationTranslator;
import au.gov.asd.tac.constellation.views.analyticview.translators.InternalVisualisationTranslator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.openide.util.Lookup;

/**
 *
 * @author cygnus_x-1
 */
public class AnalyticUtilities {

    private static final Map<String, AnalyticQuestionDescription> ANALYTIC_QUESTION_DESCRIPTIONS = new HashMap<>();
    private static final Map<String, AnalyticAggregator<?>> ANALYTIC_AGGREGATORS = new HashMap<>();
    private static final Map<String, InternalVisualisationTranslator> INTERNAL_VISUALISATION_TRANSLATORS = new HashMap<>();
    private static final Map<String, GraphVisualisationTranslator> GRAPH_VISUALISATION_TRANSLATORS = new HashMap<>();

    static {
        Lookup.getDefault().lookupAll(AnalyticQuestionDescription.class).forEach(analyticQuestionDescription -> {
            ANALYTIC_QUESTION_DESCRIPTIONS.put(analyticQuestionDescription.getName(), analyticQuestionDescription);
        });
        Lookup.getDefault().lookupAll(AnalyticAggregator.class).forEach(aggregator -> {
            ANALYTIC_AGGREGATORS.put(aggregator.getName(), aggregator);
        });
        Lookup.getDefault().lookupAll(InternalVisualisationTranslator.class).forEach(internalVisualisationTranslator -> {
            INTERNAL_VISUALISATION_TRANSLATORS.put(internalVisualisationTranslator.getName(), internalVisualisationTranslator);
        });
        Lookup.getDefault().lookupAll(GraphVisualisationTranslator.class).forEach(graphVisualisationTranslator -> {
            GRAPH_VISUALISATION_TRANSLATORS.put(graphVisualisationTranslator.getName(), graphVisualisationTranslator);
        });
    }

    private AnalyticUtilities() {
        throw new IllegalStateException("Utility class");
    }
    
    public static Collection<AnalyticQuestionDescription> getAnalyticQuestionDescriptions() {
        return Collections.unmodifiableCollection(ANALYTIC_QUESTION_DESCRIPTIONS.values());
    }

    public static Collection<AnalyticQuestionDescription> lookupAnalyticQuestionDescriptions(final Class<? extends AnalyticResult<?>> analyticResultType) {
        return Collections.unmodifiableCollection(ANALYTIC_QUESTION_DESCRIPTIONS.values().stream()
                .filter(aggregator -> aggregator.getResultType().isAssignableFrom(analyticResultType))
                .collect(Collectors.toList()));
    }

    public static AnalyticQuestionDescription<?> lookupAnalyticQuestionDescription(final Class<? extends AnalyticQuestionDescription<?>> questionDescriptionType) {
        return ANALYTIC_QUESTION_DESCRIPTIONS.values().stream()
                .filter(questionDescription -> questionDescriptionType.isInstance(questionDescription))
                .collect(Collectors.toList()).get(0);
    }

    public static AnalyticQuestionDescription<?> lookupAnalyticQuestionDescription(final String analyticQuestionDescriptionName) {
        return ANALYTIC_QUESTION_DESCRIPTIONS.get(analyticQuestionDescriptionName);
    }

    public static Collection<AnalyticAggregator<?>> getAnalyticAggregators() {
        return Collections.unmodifiableCollection(ANALYTIC_AGGREGATORS.values());
    }

    public static Collection<AnalyticAggregator<?>> lookupAnalyticAggregators(final Class<? extends AnalyticResult<?>> analyticResultType) {
        return Collections.unmodifiableCollection(ANALYTIC_AGGREGATORS.values().stream()
                .filter(aggregator -> aggregator.getResultType().isAssignableFrom(analyticResultType))
                .collect(Collectors.toList()));
    }

    public static AnalyticAggregator<?> lookupAnalyticAggregator(final Class<? extends AnalyticAggregator<?>> analyticAggregatorType) {
        return ANALYTIC_AGGREGATORS.values().stream()
                .filter(aggregator -> analyticAggregatorType.isInstance(aggregator))
                .collect(Collectors.toList()).get(0);
    }

    public static AnalyticAggregator<?> lookupAnalyticAggregator(final String analyticAggregatorName) {
        return ANALYTIC_AGGREGATORS.get(analyticAggregatorName);
    }

    public static Collection<InternalVisualisationTranslator> getInternalVisualisationTranslators() {
        return Collections.unmodifiableCollection(INTERNAL_VISUALISATION_TRANSLATORS.values());
    }

    public static Collection<InternalVisualisationTranslator> lookupInternalVisualisationTranslators(final Class<? extends AnalyticResult<?>> analyticResultType) {
        return Collections.unmodifiableCollection(INTERNAL_VISUALISATION_TRANSLATORS.values().stream()
                .filter(internalVisualisationTranslator -> internalVisualisationTranslator.getResultType().isAssignableFrom(analyticResultType))
                .collect(Collectors.toList()));
    }

    public static InternalVisualisationTranslator<?, ?> lookupInternalVisualisationTranslator(final Class<? extends InternalVisualisationTranslator<?, ?>> internalVisualisationTranslatorType) {
        return INTERNAL_VISUALISATION_TRANSLATORS.values().stream()
                .filter(internalVisualisationTranslator -> internalVisualisationTranslatorType.isInstance(internalVisualisationTranslator))
                .collect(Collectors.toList()).get(0);
    }

    public static InternalVisualisationTranslator<?, ?> lookupInternalVisualisationTranslator(final String internalVisualisationTranslatorName) {
        return INTERNAL_VISUALISATION_TRANSLATORS.get(internalVisualisationTranslatorName);
    }

    public static Collection<GraphVisualisationTranslator> getGraphVisualisationTranslators() {
        return Collections.unmodifiableCollection(GRAPH_VISUALISATION_TRANSLATORS.values());
    }

    public static Collection<GraphVisualisationTranslator> lookupGraphVisualisationTranslators(final Class<? extends AnalyticResult<?>> analyticResultType) {
        return Collections.unmodifiableCollection(GRAPH_VISUALISATION_TRANSLATORS.values().stream()
                .filter(graphVisualisationTranslator -> graphVisualisationTranslator.getResultType().isAssignableFrom(analyticResultType))
                .collect(Collectors.toList()));
    }

    public static GraphVisualisationTranslator<?, ?> lookupGraphVisualisationTranslator(final Class<? extends GraphVisualisationTranslator<?, ?>> graphVisualisationTranslatorType) {
        return GRAPH_VISUALISATION_TRANSLATORS.values().stream()
                .filter(graphVisualisationTranslator -> graphVisualisationTranslatorType.isInstance(graphVisualisationTranslator))
                .collect(Collectors.toList()).get(0);
    }

    public static GraphVisualisationTranslator<?, ?> lookupGraphVisualisationTranslator(final String graphVisualisationTranslatorName) {
        return GRAPH_VISUALISATION_TRANSLATORS.get(graphVisualisationTranslatorName);
    }
}
