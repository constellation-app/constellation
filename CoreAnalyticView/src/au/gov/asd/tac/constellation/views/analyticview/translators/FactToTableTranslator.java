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
package au.gov.asd.tac.constellation.views.analyticview.translators;

import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.FactResult;
import au.gov.asd.tac.constellation.views.analyticview.results.FactResult.ElementFact;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.TableVisualisation;
import java.util.Set;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = InternalVisualisationTranslator.class)
public class FactToTableTranslator extends AbstractTableTranslator<FactResult, ElementFact> {

    private static final String IDENTIFIER_COLUMN_NAME = "Identifier";

    @Override
    public String getName() {
        return "Fact -> Table Visualisation";
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return FactResult.class;
    }

    @Override
    public TableVisualisation<ElementFact> buildVisualisation() {
        final TableVisualisation<ElementFact> tableVisualisation = new TableVisualisation<>(this);
        final Set<String> factNames = result.getUniqueFactNames();
        tableVisualisation.addColumn(IDENTIFIER_COLUMN_NAME, (100 / 3) * 2);

        factNames.forEach(factName -> tableVisualisation.addColumn(factName, (100 / (factNames.size() + 2))));
        tableVisualisation.populateTable(result.isIgnoreNullResults()
                ? result.get().stream().filter(elementScore -> !elementScore.isNull()).collect(Collectors.toList()) : result.get());
        result.addResultListener(tableVisualisation);
        tableVisualisation.setSelectionModelListener(change -> result.setSelectionOnGraph(tableVisualisation.getSelectedItems()));
        return tableVisualisation;
    }

    @Override
    public Object getCellData(final ElementFact cellValue, final String columnName) {
        if (cellValue == null) {
            return null;
        } else if (IDENTIFIER_COLUMN_NAME.equals(columnName)) {
            return cellValue.getIdentifier();
        } else if (cellValue.getFactName().equals(columnName)) {
            return cellValue.getFactValue();
        } else {
            throw new UnrecognisedColumnException(columnName);
        }
    }

    @Override
    public String getCellText(final ElementFact cellValue, final Object cellItem, final String columnName) {
        if (cellValue == null) {
            return null;
        }
        if (IDENTIFIER_COLUMN_NAME.equals(columnName)) {
            return cellItem.toString();
        } else if (cellValue.getFactName().equals(columnName)) {
            return cellItem.toString();
        } else {
            throw new UnrecognisedColumnException(columnName);
        }
    }

    @Override
    public ConstellationColor getCellColor(final ElementFact cellValue, final Object cellItem, final String columnName) {
        final float intensity;
        if (cellValue == null) {
            intensity = 0F;
        } else if (IDENTIFIER_COLUMN_NAME.equals(columnName)) {
            intensity = cellValue.getFactValue() ? 1F : 0F;
        } else if (cellValue.getFactName().equals(columnName)) {
            intensity = (boolean) cellItem ? 1F : 0F;
        } else {
            throw new UnrecognisedColumnException(columnName);
        }

        return ConstellationColor.getColorValue(intensity, intensity, 0F, 0.3F);
    }
}
