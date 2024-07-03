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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.ReportVisualisation;
import java.util.List;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = InternalVisualisationTranslator.class)
public class AnyToReportTranslator extends InternalVisualisationTranslator<AnalyticResult<?>, ReportVisualisation> {

    @Override
    public String getName() {
        return "Any -> Report Visualisation";
    }

    @Override
    @SuppressWarnings("rawtypes") //raw type needed to return AnalyticResult class
    public Class<? extends AnalyticResult> getResultType() {
        return AnalyticResult.class;
    }

    @Override
    public ReportVisualisation buildVisualisation() {
        final ReportVisualisation report = new ReportVisualisation();
        @SuppressWarnings("unchecked") // return type of getPlugins will be List<AnalyticPlugin<?>>
        final List<AnalyticPlugin<?>> questionPlugins = (List<AnalyticPlugin<?>>) question.getPlugins();
        @SuppressWarnings("unchecked") // return type of getExceptions will always be List<Exception>
        final List<Exception> questionExceptions = question.getExceptions();
        report.populateReport(
                questionPlugins,
                result.size(),
                question.getAggregator() == null
                ? "None" : question.getAggregator().getName(),
                questionExceptions);

        if (result.hasMetadata()) {
            @SuppressWarnings("unchecked") //the return type for getMetadata is actually Map<String, String>
            final Map<String, String> metadata = result.getMetadata();
            metadata.forEach(report::extendReport);
        }
        return report;
    }
}
