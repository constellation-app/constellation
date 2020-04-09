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
package au.gov.asd.tac.constellation.views.layers;

import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateGraphBitmaskPlugin;
import au.gov.asd.tac.constellation.views.layers.utilities.UpdateGraphQueriesPlugin;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * Controls interaction of UI to layers and filtering of nodes.
 *
 * @author aldebaran30701
 */
public class LayersViewController {

    private final LayersViewTopComponent parent;
    private LayersViewPane pane = null;

    public LayersViewController(final LayersViewTopComponent parent) {
        this.parent = parent;
    }

    /**
     * Runs a plugin which updates the bitmask that should be used to show
     * elements.
     */
    public void execute() {
        // ensure pane is set to the content of the parent view.
        pane = parent.getContent();
        if (pane == null) {
            return;
        }

        final Iterator it = pane.getLayers().getChildren().iterator();
        it.next(); // skip default layer

        int newBitmask = 0b0;
        while (it.hasNext()) {
            final HBox layerBox = (HBox) (it.next());
            final CheckBox visibilityCheckBox = (CheckBox) layerBox.getChildren().get(1);
            final Text layerIdText = (Text) layerBox.getChildren().get(0);

            // only add layer id to list when it is checked
            newBitmask |= visibilityCheckBox.isSelected() ? (1 << Integer.parseInt(layerIdText.getText()) - 1) : 0;
        }

        // if the newBitmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        newBitmask = (newBitmask > 1) ? newBitmask & ~0b1 : newBitmask;

        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(newBitmask)).executeLater(GraphManager.getDefault().getActiveGraph());
    }

    /**
     * Grab all queries entered into textfields and store them in the qraph's
     * queries.
     */
    public void submit() {
        // ensure pane is set to the content of the parent view.
        pane = parent.getContent();
        if (pane == null) {
            return;
        }

        final Iterator it = pane.getLayers().getChildren().iterator();
        it.next(); // skip default layer

        final List<String> layerQueries = new ArrayList<>();
        while (it.hasNext()) {
            final HBox layerBox = (HBox) (it.next());
            final TextArea queryTextArea = (TextArea) (layerBox.getChildren().get(2));
            layerQueries.add(queryTextArea.getText().isBlank() ? null : queryTextArea.getText());
        }
        
        PluginExecution.withPlugin(new UpdateGraphQueriesPlugin(layerQueries)).executeLater(GraphManager.getDefault().getActiveGraph());
    }
}
