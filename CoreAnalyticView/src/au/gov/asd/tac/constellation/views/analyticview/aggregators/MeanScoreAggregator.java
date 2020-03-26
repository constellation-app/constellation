/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.aggregators;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.datastructure.ThreeTuple;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AnalyticAggregator.class, position = 10)
public class MeanScoreAggregator implements AnalyticAggregator<ScoreResult> {

    private static final String SCORE_NAME = "Mean Score";

    @Override
    public ScoreResult aggregate(final List<ScoreResult> results) {
        final ScoreResult aggregateResult = new ScoreResult();

        if (results == null || results.isEmpty()) {
            return aggregateResult;
        }

        aggregateResult.setIgnoreNullResults(results.stream()
                .anyMatch(result -> result.getIgnoreNullResults()));

        final Set<ThreeTuple<GraphElementType, Integer, String>> elements = new HashSet<>();
        for (final ScoreResult result : results) {
            for (final ElementScore score : result.get()) {
                elements.add(ThreeTuple.create(score.getElementType(), score.getElementId(), score.getIdentifier()));
            }
        }

        boolean isNull;
        final List<Float> scores = new ArrayList<>();
        for (final ThreeTuple<GraphElementType, Integer, String> element : elements) {
            final GraphElementType type = element.getFirst();
            final int id = element.getSecond();
            final String identifier = element.getThird();
            isNull = false;
            scores.clear();

            for (final ScoreResult result : results) {
                result.setIgnoreNullResults(false);
                for (final ElementScore score : result.get()) {
                    if (type == score.getElementType() && id == score.getElementId()) {
                        for (final String scoreName : score.getNamedScores().keySet()) {
                            isNull |= score.isNull();
                            scores.add(score.getNamedScore(scoreName));
                        }
                    }
                }
            }

            final Map<String, Float> aggregateScores = new HashMap<>();
            aggregateScores.put(SCORE_NAME, scores.stream().reduce((x, y) -> x + y).get() / scores.size());

            aggregateResult.add(new ElementScore(type, id, identifier, isNull, aggregateScores));
        }

        return aggregateResult;
    }

    @Override
    public String getName() {
        return SCORE_NAME;
    }

    @Override
    public Class<? extends AnalyticResult> getResultType() {
        return ScoreResult.class;
    }
}
