/*
 * Copyright 2010-2025 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.views.analyticview.results.FactResult.ElementFact;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stores {@link ElementFact} objects mapping graph elements to a named, boolean
 * value.
 *
 * @author cygnus_x-1
 */
public class FactResult extends AnalyticResult<ElementFact> {

    public Set<String> getUniqueFactNames() {
        return result.values().stream()
                .map(ElementFact::getFactName)
                .collect(Collectors.toSet());
    }

    public static class ElementFact extends AnalyticData implements Comparable<ElementFact> {

        private final String factName;
        private final boolean factValue;

        public ElementFact(final GraphElementType elementType, final int elementId, final String identifier, final boolean isNull, final String factName, final boolean factValue) {
            super(elementType, elementId, identifier, isNull);
            this.factName = factName;
            this.factValue = factValue;
        }

        /**
         * Get the fact name for the graph element this AnalyticResult
         * corresponds to.
         *
         * @return
         */
        public String getFactName() {
            return factName;
        }

        /**
         * Get the fact value for the graph element this AnalyticResult
         * corresponds to.
         *
         * @return
         */
        public boolean getFactValue() {
            return factValue;
        }

        @Override
        public String toString() {
            return String.format("{%s;%s;%s=%s}", getClass().getSimpleName(), id.identifier, factName, factValue);
        }

        @Override
        public int compareTo(final ElementFact other) {
            final int factComparison = other.factName.compareTo(this.factName);
            return factComparison == 0 ? Boolean.compare(other.factValue, this.factValue) : factComparison;
        }
    }
}
