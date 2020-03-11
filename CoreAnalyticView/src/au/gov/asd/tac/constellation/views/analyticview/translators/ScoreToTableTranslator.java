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
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult.ElementScore;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.TableVisualisation;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = InternalVisualisationTranslator.class)
public class ScoreToTableTranslator extends AbstractTableTranslator<ScoreResult, ElementScore> {

    private static final String IDENTIFIER_COLUMN_NAME = "Identifier";

    @Override
    public String getName() {
        return "Multi-Score -> Table Visualisation";
    }

    @Override
    public Class<? extends AnalyticResult> getResultType() {
        return ScoreResult.class;
    }

    @Override
    public TableVisualisation buildVisualisation() {
        final TableVisualisation<ElementScore> tableVisualisation = new TableVisualisation(this);
        final Set<String> scoreNames = result.getUniqueScoreNames();
        tableVisualisation.addColumn(IDENTIFIER_COLUMN_NAME, (100 / (scoreNames.size() + 2)) * 2);
        scoreNames.forEach(scoreName -> {
            tableVisualisation.addColumn(scoreName, (100 / (scoreNames.size() + 2)));
        });
        tableVisualisation.populateTable(result.getIgnoreNullResults()
                ? result.get().stream().filter(elementMultiScore -> !elementMultiScore.isNull()).collect(Collectors.toList()) : result.get());
        result.addResultListener(tableVisualisation);
        tableVisualisation.setSelectionModelListener((change) -> {
            result.setSelectionOnGraph(tableVisualisation.getSelectedItems());
        });
        return tableVisualisation;
    }

    @Override
    public Object getCellData(final ElementScore cellValue, final String columnName) {
        if (cellValue == null) {
            return null;
        }
        switch (columnName) {
            case IDENTIFIER_COLUMN_NAME:
                return cellValue.getIdentifier();
            default:
                if (cellValue.getNames().contains(columnName)) {
                    return cellValue.getNamedScores().get(columnName);
                } else {
                    throw new UnrecognisedColumnException("Column not recognised: " + columnName);
                }
        }
    }

    @Override
    public String getCellText(final ElementScore cellValue, final Object cellItem, final String columnName) {
        if (cellValue == null) {
            return null;
        }
        switch (columnName) {
            case IDENTIFIER_COLUMN_NAME:
                return cellItem.toString();
            default:
                if (cellValue.getNames().contains(columnName)) {
                    return cellItem.toString();
                } else {
                    throw new UnrecognisedColumnException("Column not recognised: " + columnName);
                }
        }
    }

    @Override
    public ConstellationColor getCellColor(final ElementScore cellValue, final Object cellItem, final String columnName) {
        final float intensity;
        if (cellValue == null) {
            intensity = 0f;
        } else {
            switch (columnName) {
                case IDENTIFIER_COLUMN_NAME:
                    intensity = Math.max(0f, Math.min(1f, cellValue.getNamedScores().values().stream()
                            .reduce((x, y) -> x + y).get()
                            / cellValue.getNamedScores().size()));
                    break;
                default:
                    if (cellValue.getNames().contains(columnName)) {
                        intensity = Math.max(0f, Math.min(1f, (float) cellItem));
                    } else {
                        throw new UnrecognisedColumnException("Column not recognised: " + columnName);
                    }
            }
        }

        return ConstellationColor.getColorValue(intensity, intensity, 0f, 0.3f);
    }
}
