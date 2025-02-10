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
package au.gov.asd.tac.constellation.views.analyticview;

import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.views.analyticview.aggregators.AnalyticAggregator;
import au.gov.asd.tac.constellation.views.analyticview.utilities.AnalyticUtilities;

/**
 * Analytic Aggregator Parameter Value
 *
 * @author cygnus_x-1
 */
public class AnalyticAggregatorParameterValue extends ParameterValue {

    private AnalyticAggregator<?> analyticAggregator;

    public AnalyticAggregatorParameterValue() {
        this.analyticAggregator = null;
    }

    public AnalyticAggregatorParameterValue(final AnalyticAggregator<?> analyticAggregator) {
        this.analyticAggregator = analyticAggregator;
    }

    @Override
    public String validateString(final String s) {
        return AnalyticUtilities.lookupAnalyticAggregator(s) != null
                ? null : "Could not find an aggregator matching the key provided.";
    }

    @Override
    public boolean setStringValue(final String s) {
        final AnalyticAggregator<?> aggregator = AnalyticUtilities.lookupAnalyticAggregator(s);
        if (!aggregator.equals(analyticAggregator)) {
            analyticAggregator = aggregator;
            return true;
        }
        return false;
    }

    @Override
    public Object getObjectValue() {
        return analyticAggregator;
    }

    @Override
    public boolean setObjectValue(final Object o) {
        if (o instanceof AnalyticAggregator<?> aggregator && !analyticAggregator.getClass().equals(o.getClass())) {
            analyticAggregator = aggregator;
            return true;
        }
        return false;
    }

    @Override
    protected ParameterValue createCopy() {
        return new AnalyticAggregatorParameterValue(analyticAggregator);
    }

    @Override
    public String toString() {
        return analyticAggregator.getName();
    }
}
