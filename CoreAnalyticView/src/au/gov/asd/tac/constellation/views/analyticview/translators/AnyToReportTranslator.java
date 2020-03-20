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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.ReportVisualisation;
import java.util.Map;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = InternalVisualisationTranslator.class)
public class AnyToReportTranslator extends InternalVisualisationTranslator<AnalyticResult, ReportVisualisation> {

    @Override
    public String getName() {
        return "Any -> Report Visualisation";
    }

    @Override
    public Class<? extends AnalyticResult> getResultType() {
        return AnalyticResult.class;
    }

    @Override
    public ReportVisualisation buildVisualisation() {
        final ReportVisualisation report = new ReportVisualisation();
        report.populateReport(
                question.getPlugins(),
                result.size(),
                question.getAggregator() == null
                ? "None" : question.getAggregator().getName(),
                question.getExceptions());

        if (result.hasMetadata()) {
            final Map<String, String> metadata = result.getMetadata();
            metadata.forEach((metadataKey, metadataValue) -> {
                report.extendReport(metadataKey, metadataValue);
            });
        }
        return report;
    }
}
