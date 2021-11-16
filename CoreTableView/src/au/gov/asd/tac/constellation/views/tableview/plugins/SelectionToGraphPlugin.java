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
package au.gov.asd.tac.constellation.views.tableview.plugins;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInfo;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.PluginType;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.templates.PluginTags;
import au.gov.asd.tac.constellation.plugins.templates.SimpleEditPlugin;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 * Copy the table selection to the graph.
 * <p/>
 * When the table gets a graph change event, a check is made to see if it was
 * this plugin that did the changes. If it was, then the table does not need to
 * perform an update. This is how a endless event loop is prevented from being
 * created.
 *
 * @author formalhaunt
 */
@PluginInfo(pluginType = PluginType.SELECTION, tags = {PluginTags.SELECT})
public class SelectionToGraphPlugin extends SimpleEditPlugin {

    public static final String SELECT_ON_GRAPH_PLUGIN = "Table View: Select on Graph";

    private final TableView<ObservableList<String>> table;
    private final Map<ObservableList<String>, Integer> rowToElementIdIndex;
    private final GraphElementType elementType;

    /**
     * Creates a new table selection to graph plugin.
     *
     * @param table the table to get the selection from
     * @param rowToElementIdIndex a map of the table rows to the graph element
     * IDs
     * @param elementType the type of graph element that the table is currently
     * displaying
     */
    public SelectionToGraphPlugin(final TableView<ObservableList<String>> table,
            final Map<ObservableList<String>, Integer> rowToElementIdIndex,
            final GraphElementType elementType) {
        this.table = table;
        this.rowToElementIdIndex = rowToElementIdIndex;
        this.elementType = elementType;
    }

    @Override
    public void edit(final GraphWriteMethods graph,
            final PluginInteraction interaction,
            final PluginParameters parameters) throws InterruptedException, PluginException {
        // Convert all the rows in the table to graph element IDs
        final Set<Integer> elements = table.getItems().stream()
                .map(item -> rowToElementIdIndex.get(item))
                .collect(Collectors.toSet());

        // Convert only the selected rows in the table to graph element IDs
        final Set<Integer> selectedElements = table.getSelectionModel().getSelectedItems().stream()
                .map(selectedItem -> rowToElementIdIndex.get(selectedItem))
                .collect(Collectors.toSet());

        final boolean isVertex = elementType == GraphElementType.VERTEX;

        // get the correct attribute ID based on the type of elements the table
        // is currently displaying
        final int selectedAttributeId = isVertex ? VisualConcept.VertexAttribute.SELECTED.ensure(graph)
                : VisualConcept.TransactionAttribute.SELECTED.ensure(graph);

        // iterates over the graph elements and sets its selected attribute to true
        // if the element also exists in the selectedElements set
        elements.forEach(element
                -> graph.setBooleanValue(selectedAttributeId, element, selectedElements.contains(element))
        );
    }

    @Override
    public String getName() {
        return SELECT_ON_GRAPH_PLUGIN;
    }
}
