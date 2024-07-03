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
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticException;
import java.util.List;

/**
 * A method for combining a set of AnalyticResults.
 *
 * @param <R> The AnalyticResult class supported by this AnalyticAggregator.
 *
 * @author cygnus_x-1
 */
public interface AnalyticAggregator<R extends AnalyticResult<?>> {

    public R aggregate(final List<R> results) throws AnalyticException;

    public String getName();

    public abstract Class<? extends AnalyticResult<?>> getResultType();
}
