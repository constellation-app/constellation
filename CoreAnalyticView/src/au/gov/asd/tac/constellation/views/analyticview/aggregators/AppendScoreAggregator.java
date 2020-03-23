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
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.lookup.ServiceProvider;

/**
 * Combine scores by appending them to a new {@link ScoreResult}.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AnalyticAggregator.class, position = 0)
public class AppendScoreAggregator implements AnalyticAggregator<ScoreResult> {

    @Override
    public ScoreResult aggregate(final List<ScoreResult> results) throws AnalyticException {
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
        Map<String, Float> namedScores = new HashMap<>();
        for (final ThreeTuple<GraphElementType, Integer, String> element : elements) {
            final GraphElementType type = element.getFirst();
            final int id = element.getSecond();
            final String identifier = element.getThird();
            isNull = false;
            namedScores.clear();

            for (final ScoreResult result : results) {
                result.setIgnoreNullResults(false);
                for (final ElementScore score : result.get()) {
                    if (type == score.getElementType() && id == score.getElementId()) {
                        for (final String scoreName : score.getNamedScores().keySet()) {
                            if (namedScores.containsKey(scoreName)) {
                                throw new AnalyticException("AppendScoreAggregator cannot combine multiple scores with the same name [" + scoreName + "]");
                            }
                            isNull |= score.isNull();
                            namedScores.put(scoreName, score.getNamedScore(scoreName));
                        }
                    }
                }
            }

            final Map<String, Float> aggregateScores = new HashMap<>(namedScores);

            aggregateResult.add(new ElementScore(type, id, identifier, isNull, aggregateScores));
        }

        return aggregateResult;
    }

    @Override
    public String getName() {
        return "Append Score";
    }

    @Override
    public Class<? extends AnalyticResult> getResultType() {
        return ScoreResult.class;
    }
}
