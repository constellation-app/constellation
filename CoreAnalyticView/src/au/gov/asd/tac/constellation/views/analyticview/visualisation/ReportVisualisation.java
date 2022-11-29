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
package au.gov.asd.tac.constellation.views.analyticview.visualisation;

import au.gov.asd.tac.constellation.utilities.javafx.JavafxStyleManager;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.analyticview.analytics.AnalyticPlugin;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author cygnus_x-1
 */
public class ReportVisualisation extends InternalVisualisation {

    private final VBox report;
    private final Label pluginsRunValue;
    private final Label numberOfResultsValue;
    private final Label aggregationMethodValue;
    private final Label exceptionsValue;

    public ReportVisualisation() {
        final HBox pluginsReportBox = new HBox();
        final Label pluginsRunLabel = new Label("Plugins Run: ");
        pluginsRunLabel.setStyle(JavafxStyleManager.CSS_FONT_WEIGHT_BOLD);
        this.pluginsRunValue = new Label();
        pluginsRunValue.setWrapText(true);
        pluginsReportBox.getChildren().addAll(pluginsRunLabel, pluginsRunValue);

        final HBox numberOfResultsReportBox = new HBox();
        final Label numberOfResultsLabel = new Label("Number of Results: ");
        numberOfResultsLabel.setStyle(JavafxStyleManager.CSS_FONT_WEIGHT_BOLD);
        this.numberOfResultsValue = new Label();
        numberOfResultsValue.setWrapText(true);
        numberOfResultsReportBox.getChildren().addAll(numberOfResultsLabel, numberOfResultsValue);

        final HBox aggregationMethodReportBox = new HBox();
        final Label aggregationMethodLabel = new Label("Aggregation Method: ");
        aggregationMethodLabel.setStyle(JavafxStyleManager.CSS_FONT_WEIGHT_BOLD);
        this.aggregationMethodValue = new Label();
        aggregationMethodValue.setWrapText(true);
        aggregationMethodReportBox.getChildren().addAll(aggregationMethodLabel, aggregationMethodValue);

        final HBox exceptionsReportBox = new HBox();
        final Label exceptionsLabel = new Label("Exceptions: ");
        exceptionsLabel.setStyle(JavafxStyleManager.CSS_FONT_WEIGHT_BOLD);
        this.exceptionsValue = new Label();
        exceptionsValue.setWrapText(true);
        exceptionsReportBox.getChildren().addAll(exceptionsLabel, exceptionsValue);

        this.report = new VBox(pluginsReportBox, numberOfResultsReportBox, aggregationMethodReportBox, exceptionsReportBox);
    }

    public void populateReport(final List<AnalyticPlugin<?>> plugins, final int numberOfResults, final String aggregationMethod, final List<Exception> exceptions) {
        final StringBuilder pluginsString = new StringBuilder();
        plugins.forEach(plugin -> {
            pluginsString.append(plugin.getName());
            if (plugins.indexOf(plugin) < plugins.size() - 1) {
                pluginsString.append(", ");
            }
        });
        final StringBuilder exceptionsString = new StringBuilder();
        exceptions.forEach(exception -> {
            exceptionsString.append(exception.getMessage());
            if (exceptions.indexOf(exception) < exceptions.size() - 1) {
                exceptionsString.append(", ");
            }
        });
        pluginsRunValue.setText(pluginsString.toString());
        numberOfResultsValue.setText(String.valueOf(numberOfResults));
        aggregationMethodValue.setText(aggregationMethod);
        exceptionsValue.setText(exceptionsString.toString());
    }

    public void extendReport(final String extensionTitle, final String extensionContent) {
        final HBox spacerReportBox = new HBox();
        final Label spacerLabel = new Label(SeparatorConstants.NEWLINE);
        spacerReportBox.getChildren().add(spacerLabel);

        final HBox extensionReportBox = new HBox();
        final Label extensionLabel = new Label(String.format("%s: ", extensionTitle));
        extensionLabel.setStyle(JavafxStyleManager.CSS_FONT_WEIGHT_BOLD);
        final Label extensionValue = new Label(extensionContent);
        extensionValue.setWrapText(true);
        extensionReportBox.getChildren().addAll(extensionLabel, extensionValue);

        report.getChildren().addAll(spacerReportBox, extensionReportBox);
    }

    @Override
    public String getName() {
        return "Report";
    }

    @Override
    public Node getVisualisation() {
        return report;
    }
}
