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
package au.gov.asd.tac.constellation.views.dataaccess.plugins.clean;

import au.gov.asd.tac.constellation.functionality.dialog.ItemsDialog;
import au.gov.asd.tac.constellation.functionality.dialog.ItemsRow;
import au.gov.asd.tac.constellation.graph.Graph;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.stage.Window;

/**
 * A dialog that can be used to display the lead node to select when performing
 * a merge
 *
 * @author arcturus
 */
public class LeadNodeSelectionDialog extends ItemsDialog<Integer> {

    private final Graph graph;
    private int leadVertexId;
    private static final String HELP_MESSAGE = "Select the lead identifier by highlighting a single row and click Continue.";

    public LeadNodeSelectionDialog(Window owner, ObservableList<ItemsRow<Integer>> rows, Graph graph) {
        super("Lead Node Identifier", HELP_MESSAGE, "Identifier", "", rows);
        this.graph = graph;
        this.leadVertexId = -1;
    }

    @Override
    protected void selectRows(List<ItemsRow<Integer>> selectedRows) throws InterruptedException {
        if (selectedRows.size() > 0) {
            this.leadVertexId = selectedRows.get(0).getItem();
        }
    }

    public int getLeadVertexId() {
        return this.leadVertexId;
    }
}
