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
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores {@link ElementScore} objects mapping graph elements to multiple named,
 * float scores.
 *
 * @author cygnus_x-1
 */
public class ScoreResult extends AnalyticResult<ElementScore> {

    public Set<String> getUniqueScoreNames() {
        return result.values().stream()
                .map(elementScores -> elementScores.getNames())
                .flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        result.values().forEach(score -> {
            sb.append(score.getElementId()).append(": {\n");
            score.namedScores.forEach((name, value) -> sb.append("\tname: ").append(name).append(", value: ").append(value));
            sb.append(" }\n");
        });
        return sb.toString();
    }

    public ScoreResult combine(final ScoreResult otherResult) {
        otherResult.result.forEach((key, value)
                -> result.merge(key, value, ElementScore::combineReplace)
        );
        return this;
    }

    public static class ElementScore extends AnalyticData implements Comparable<ElementScore> {

        private final Map<String, Float> namedScores;

        public ElementScore(final GraphElementType elementType, final int elementId,
                final String identifier, final boolean isNull, final Map<String, Float> namedScores) {
            super(elementType, elementId, identifier, isNull);
            this.namedScores = namedScores;
        }

        public static ElementScore combineReplace(final ElementScore es1, final ElementScore es2) {
            final Map<String, Float> combinedNamedScores = new HashMap<>(es1.getNamedScores());
            combinedNamedScores.putAll(es2.getNamedScores());

            return new ElementScore(es1.getElementType(), es1.getElementId(), es1.getIdentifier(), es1.isNull() && es2.isNull(), combinedNamedScores);
        }

        /**
         * Get the set of unique score names.
         *
         * @return
         */
        public Set<String> getNames() {
            return namedScores.keySet();
        }

        /**
         * Get the score associated with the given name for the graph element
         * this AnalyticResult corresponds to.
         *
         * @param name the name to use for looking up the score
         * @return
         */
        public Float getNamedScore(final String name) {
            return namedScores.get(name);
        }

        /**
         * Get the named scores for the graph element this AnalyticResult
         * corresponds to.
         *
         * @return
         */
        public Map<String, Float> getNamedScores() {
            return namedScores;
        }

        @Override
        public String toString() {
            return String.format("{%s;%s;%s}", getClass().getSimpleName(), this.getIdentifier(), namedScores);
        }

        @Override
        public int compareTo(final ElementScore other) {
            final float otherMeanScore = other.namedScores.values().stream().reduce((x, y) -> x + y).orElse((float) 0.0) / other.namedScores.values().size();
            final float thisMeanScore = this.namedScores.values().stream().reduce((x, y) -> x + y).orElse((float) 0.0) / this.namedScores.values().size();
            return Float.compare(otherMeanScore, thisMeanScore);
        }
    }
}
