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

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.views.analyticview.results.AnalyticResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ClusterResult;
import au.gov.asd.tac.constellation.views.analyticview.results.ClusterResult.ClusterData;
import au.gov.asd.tac.constellation.views.analyticview.visualisation.TableVisualisation;
import java.util.List;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = InternalVisualisationTranslator.class)
public class ClusterToTableTranslator extends AbstractTableTranslator<ClusterResult, ClusterData> {

    private static final String IDENTIFIER_COLUMN_NAME = "Identifier";
    private static final String CLUSTER_COLUMN_NAME = "Cluster Number";

    @Override
    public String getName() {
        return "Cluster -> Table Visualisation";
    }

    @Override
    public Class<? extends AnalyticResult<?>> getResultType() {
        return ClusterResult.class;
    }

    @Override
    public TableVisualisation<ClusterData> buildVisualisation() {
        final TableVisualisation<ClusterData> tableVisualisation = new TableVisualisation<>(this);
        tableVisualisation.addColumn(IDENTIFIER_COLUMN_NAME, (100 / 3) * 2);
        tableVisualisation.addColumn(CLUSTER_COLUMN_NAME, (100 / 3));
        List<ClusterData> displayResult = result.get();
        if (result.getIgnoreTransactions()) {
            displayResult = displayResult.stream()
                    .filter(clusterData -> clusterData.getElementType() != GraphElementType.TRANSACTION)
                    .collect(Collectors.toList());
        }
        if (result.isIgnoreNullResults()) {
            displayResult = displayResult.stream()
                    .filter(clusterData -> !clusterData.isNull())
                    .collect(Collectors.toList());
        }
        tableVisualisation.populateTable(displayResult);
        result.addResultListener(tableVisualisation);
        tableVisualisation.setSelectionModelListener(change -> result.setSelectionOnGraph(tableVisualisation.getSelectedItems()));
        return tableVisualisation;
    }

    @Override
    public Object getCellData(final ClusterData cellValue, final String columnName) {
        if (cellValue == null) {
            return null;
        }
        switch (columnName) {
            case IDENTIFIER_COLUMN_NAME:
                return cellValue.getIdentifier();
            case CLUSTER_COLUMN_NAME:
                return cellValue.getClusterNumber();
            default:
                throw new UnrecognisedColumnException(columnName);
        }
    }

    @Override
    public String getCellText(final ClusterData cellValue, final Object cellItem, final String columnName) {
        if (cellValue == null) {
            return null;
        }
        switch (columnName) {
            case IDENTIFIER_COLUMN_NAME:
            case CLUSTER_COLUMN_NAME:
                return cellItem.toString();
            default:
                throw new UnrecognisedColumnException(columnName);
        }
    }

    @Override
    public ConstellationColor getCellColor(final ClusterData cellValue, final Object cellItem, final String columnName) {
        return ConstellationColor.getColorValue(0F, 0F, 0F, 0.3F);
    }
}
