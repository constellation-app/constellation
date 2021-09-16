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
package au.gov.asd.tac.constellation.views.tableview2.tasks;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.tableview2.components.ProgressBar;
import au.gov.asd.tac.constellation.views.tableview2.components.Table;
import au.gov.asd.tac.constellation.views.tableview2.components.TablePane;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class UpdateTableSelectionTaskNGTest {
    
    public UpdateTableSelectionTaskNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        FxToolkit.registerPrimaryStage();
        FxToolkit.showStage();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FxToolkit.hideStage();
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void updateSelectionTask() {
        final TablePane tablePane = mock(TablePane.class);
        final Graph graph = mock(Graph.class);
        final TableViewState tableViewState = new TableViewState();
        
        final Table table = mock(Table.class);
        
        final ProgressBar progressBar = mock(ProgressBar.class);
        
        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getProgressBar()).thenReturn(progressBar);
        
        final TriggerSelectionUpdateTask updateTableSelectionTask
                = new TriggerSelectionUpdateTask(tablePane, graph, tableViewState);
        
        updateTableSelectionTask.run();
        
        verify(table).updateSelection(graph, tableViewState);
    }
}
