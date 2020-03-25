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
package au.gov.asd.tac.constellation.layers;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.manager.GraphManager;
import au.gov.asd.tac.constellation.layers.utilities.UpdateGraphBitmaskPlugin;
import au.gov.asd.tac.constellation.layers.views.LayersViewPane;
import au.gov.asd.tac.constellation.layers.views.LayersViewTopComponent;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 *
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

        int tempMask = 0b0;
        Iterator it = pane.getQueries().getChildren().iterator();
        it.next();

        while (it.hasNext()) {
            HBox queryBox = (HBox) (it.next());
            // This is the checkbox
            CheckBox queryCB = (CheckBox) queryBox.getChildren().get(1);
            Text queryID = (Text) queryBox.getChildren().get(0);

            // only add layer id to list when it is checked
            tempMask |= queryCB.isSelected() ? (1 << Integer.parseInt(queryID.getText()) - 1) : 0;
        }

        // if the tempmask is 1, it means none of the boxes are checked. therefore display default layer 1 (All nodes)
        tempMask = (tempMask > 1) ? tempMask & ~0b1 : tempMask;

        PluginExecution.withPlugin(new UpdateGraphBitmaskPlugin(tempMask)).executeLater(GraphManager.getDefault().getActiveGraph());
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

        List<String> layerQueries = new ArrayList<>();
        Iterator it = pane.getQueries().getChildren().iterator();
        it.next();

        while (it.hasNext()) {
            HBox queryBox = (HBox) (it.next());
            TextArea tempTA = (TextArea) (queryBox.getChildren().get(2));
            layerQueries.add(tempTA.getText().equals("") ? null : tempTA.getText());
        }

        StoreGraph.setLayerQueries(layerQueries);
    }
}
