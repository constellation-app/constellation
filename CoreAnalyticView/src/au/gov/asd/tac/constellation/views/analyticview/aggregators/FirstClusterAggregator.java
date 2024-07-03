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
import au.gov.asd.tac.constellation.views.analyticview.results.ClusterResult;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AnalyticAggregator.class, position = 0)
public class FirstClusterAggregator implements AnalyticAggregator<ClusterResult> {

    private static final String SCORE_NAME = "First Cluster";

    @Override
    public ClusterResult aggregate(final List<ClusterResult> results) {
        if (CollectionUtils.isEmpty(results) || results.get(0) == null) {
            return new ClusterResult();
        } else {
            return results.get(0);
        }
    }

    @Override
    public String getName() {
        return SCORE_NAME;
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ClusterResult.class;
    }
}
