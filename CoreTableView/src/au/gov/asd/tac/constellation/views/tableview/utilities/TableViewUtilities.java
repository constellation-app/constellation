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
package au.gov.asd.tac.constellation.views.tableview.utilities;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.ReadableGraph;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import au.gov.asd.tac.constellation.views.tableview.plugins.SelectionToGraphPlugin;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Table View Utilities.
 *
 * @author cygnus_x-1
 */
public class TableViewUtilities {
    
    private TableViewUtilities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieve data from the given table as comma-separated values.
     *
     * @param table the table to retrieve data from.
     * @param pagination the current pagination of the table
     * @param includeHeader if true, the table headers will be included in the
     * output.
     * @param selectedOnly if true, only the data from selected rows in the
     * table will be included in the output.
     * @return a String of comma-separated values representing the table.
     */
    public static String getTableData(final TableView<ObservableList<String>> table, final Pagination pagination,
            final boolean includeHeader, final boolean selectedOnly) {
        final List<Integer> visibleIndices = table.getVisibleLeafColumns().stream()
                .map(column -> table.getColumns().indexOf(column))
                .toList();

        final StringBuilder data = new StringBuilder();
        if (includeHeader) {
            data.append(visibleIndices.stream()
                    .filter(Objects::nonNull)
                    .map(index -> table.getColumns().get(index).getText())
                    .reduce((header1, header2) -> header1 + SeparatorConstants.COMMA + header2)
                    .get());
            data.append(SeparatorConstants.NEWLINE);
        }

        // get the current page index so we can go back to it afterwards
        final int currentPage = pagination.getCurrentPageIndex();
        if (selectedOnly) {
            for (int i = 0; i < pagination.getPageCount(); i++) {
                final TableView<ObservableList<String>> page = (TableView<ObservableList<String>>) pagination.getPageFactory().call(i);
                page.getSelectionModel().getSelectedItems().forEach(selectedItem -> {
                    data.append(visibleIndices.stream()
                            .filter(Objects::nonNull)
                            .map(index -> selectedItem.get(index))
                            .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2)
                            .get());
                    data.append(SeparatorConstants.NEWLINE);
                });
            }
        } else {
            for (int i = 0; i < pagination.getPageCount(); i++) {
                final TableView<ObservableList<String>> page = (TableView<ObservableList<String>>) pagination.getPageFactory().call(i);
                page.getItems().forEach(item -> {
                    data.append(visibleIndices.stream()
                            .filter(Objects::nonNull)
                            .map(index -> item.get(index))
                            .reduce((cell1, cell2) -> cell1 + SeparatorConstants.COMMA + cell2)
                            .get());
                    data.append(SeparatorConstants.NEWLINE);
                });
            }
        }
        // Call the page factory function once more to go back to the original page index
        pagination.getPageFactory().call(currentPage);

        return data.toString();
    }

    /**
     * Copy the given text to the system clipboard.
     *
     * @param text the text to copy.
     */
    public static void copyToClipboard(final String text) {
        final ClipboardContent content = new ClipboardContent();
        content.putString(text);

        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Copy selection information from a table to a graph.
     *
     * @param table the table to retrieve data from.
     * @param index the index of the selection.
     * @param elementType the element type of the selection.
     * @param graph the graph to copy to.
     */
    public static void copySelectionToGraph(final TableView<ObservableList<String>> table,
            final Map<ObservableList<String>, Integer> index, final GraphElementType elementType, final Graph graph) {
        PluginExecution.withPlugin(new SelectionToGraphPlugin(table, index, elementType)).executeLater(graph);
    }

    /**
     * Based on the tables current element type (vertex or transaction) get all
     * selected elements of that type in the graph and return their element IDs.
     *
     * @param graph the graph to read from
     * @param state the current table state
     * @return the IDs of the selected elements
     */
    public static List<Integer> getSelectedIds(final Graph graph, final TableViewState state) {
        final List<Integer> selectedIds = new ArrayList<>();
        try (final ReadableGraph readableGraph = graph.getReadableGraph()) {
            final boolean isVertex = state.getElementType() == GraphElementType.VERTEX;
            final int selectedAttributeId = isVertex
                    ? VisualConcept.VertexAttribute.SELECTED.get(readableGraph)
                    : VisualConcept.TransactionAttribute.SELECTED.get(readableGraph);
            final int elementCount = isVertex
                    ? readableGraph.getVertexCount()
                    : readableGraph.getTransactionCount();
            for (int elementPosition = 0; elementPosition < elementCount; elementPosition++) {
                final int elementId = isVertex
                        ? readableGraph.getVertex(elementPosition)
                        : readableGraph.getTransaction(elementPosition);
                if (selectedAttributeId != Graph.NOT_FOUND
                        && readableGraph.getBooleanValue(selectedAttributeId, elementId)) {
                    selectedIds.add(elementId);
                }
            }
            return selectedIds;
        }
    }
}
