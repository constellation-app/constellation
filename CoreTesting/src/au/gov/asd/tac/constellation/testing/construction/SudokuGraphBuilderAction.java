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
package au.gov.asd.tac.constellation.testing.construction;

import au.gov.asd.tac.constellation.graph.node.GraphNode;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import au.gov.asd.tac.constellation.testing.CoreTestingPluginRegistry;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Create a sudoku puzzle graph.
 *
 * @author algol
 */
@ActionID(category = "Graph", id = "au.gov.asd.tac.constellation.testing.construction.SudokuGraphBuilderAction")
@ActionRegistration(displayName = "#CTL_SudokuGraphBuilderAction", surviveFocusChange = true)
//@ActionReferences({
//    @ActionReference(path = "Menu/Experimental/Build Graph", position = 0)
//})
@Messages("CTL_SudokuGraphBuilderAction=Sudoku Graph")
public final class SudokuGraphBuilderAction implements ActionListener {

    private final GraphNode context;

    public SudokuGraphBuilderAction(final GraphNode context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginExecution.withPlugin(CoreTestingPluginRegistry.SUDOKU_GRAPH_BUILDER)
                .executeLater(context.getGraph());
    }
}
