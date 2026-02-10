/*
 * Copyright 2010-2025 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.analyticview.utilities;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.analyticview.results.ScoreResult;
import javafx.scene.control.TableView;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

/**
 * Utility class for Analytic View results exporting
 *
 * @author Delphinus8821
 */
public class AnalyticExportUtilities {

    private AnalyticExportUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Export the data in the Analytic View results table to CSV
     *
     * @param table
     * @param includeHeader
     * @return
     */
    public static String getTableData(final TableView<ScoreResult.ElementScore> table, final boolean includeHeader) {
        final MutableIntList visibleIndices = new IntArrayList(table.getVisibleLeafColumns().stream()
                .mapToInt(column -> table.getColumns().indexOf(column))
                .toArray());

        final StringBuilder data = new StringBuilder();
        if (includeHeader) {
            data.append(visibleIndices.primitiveStream()
                    .mapToObj(index -> table.getColumns().get(index).getText())
                    .reduce((header1, header2) -> header1 + SeparatorConstants.COMMA + header2)
                    .get());
            data.append(SeparatorConstants.NEWLINE);
        }

        for (int i = 0; i < table.getItems().size(); i++) {
            final String item = table.getColumns().get(0).getCellData(i).toString();
            final String itemData = table.getColumns().get(1).getCellData(i).toString();
            data.append(item);
            data.append(SeparatorConstants.COMMA);
            data.append(itemData);
            data.append(SeparatorConstants.NEWLINE);
        }

        return data.toString();
    }

}
