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
package au.gov.asd.tac.constellation.views.analyticview.results;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.views.analyticview.results.EmptyResult.EmptyData;

/**
 * A placeholder result which stores no information.
 *
 * @author cygnus_x-1
 */
public class EmptyResult extends AnalyticResult<EmptyData> {

    public static class EmptyData extends AnalyticData {

        public EmptyData(final GraphElementType elementType, final int elementId,
                final String identifier, final boolean isNull, final float score) {
            super(elementType, elementId, identifier, isNull);
        }

        @Override
        public String toString() {
            return String.format("{%s;%s}", getClass().getSimpleName(), id.identifier);
        }
    }
}
