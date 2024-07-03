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
package au.gov.asd.tac.constellation.views.analyticview.aggregators;

import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AnalyticAggregator.class, position = 20)
public class SumScoreAggregator implements AnalyticAggregator<ScoreResult> {

    private static final String SCORE_NAME = "Sum Score";

    @Override
    public ScoreResult aggregate(final List<ScoreResult> results) {
        final ScoreResult combinedResults = new ScoreResult();
        final ScoreResult aggregateResult = new ScoreResult();

        if (CollectionUtils.isEmpty(results)) {
            return aggregateResult;
        }

        aggregateResult.setIgnoreNullResults(results.stream()
                .anyMatch(result -> result.isIgnoreNullResults()));

        results.forEach(combinedResults::combine);
        combinedResults.getResult().forEach((key, value) -> {
            final Map<String, Float> aggregateScores = new HashMap<>();
            aggregateScores.put(SCORE_NAME, value.getNamedScores().values().stream().reduce((x, y) -> x + y).orElse(0.0F));
            aggregateResult.add(new ElementScore(key.getElementType(), key.getElementId(), key.getIdentifier(), false, aggregateScores));
        });

        return aggregateResult;
    }

    @Override
    public String getName() {
        return SCORE_NAME;
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ScoreResult.class;
    }
}
