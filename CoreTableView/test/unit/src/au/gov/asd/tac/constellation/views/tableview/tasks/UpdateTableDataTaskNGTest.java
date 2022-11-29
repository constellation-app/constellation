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
package au.gov.asd.tac.constellation.views.tableview.tasks;

import au.gov.asd.tac.constellation.graph.Graph;
import au.gov.asd.tac.constellation.views.tableview.components.ProgressBar;
import au.gov.asd.tac.constellation.views.tableview.components.Table;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import au.gov.asd.tac.constellation.views.tableview.state.TableViewState;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class UpdateTableDataTaskNGTest {
    private static final Logger LOGGER = Logger.getLogger(UpdateTableDataTaskNGTest.class.getName());

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (!FxToolkit.isFXApplicationThreadRunning()) {
            FxToolkit.registerPrimaryStage();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            FxToolkit.cleanupStages();
        } catch (TimeoutException ex) {
            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages", ex);
        }
    }

    @Test
    public void updateDataTask() {
        final TablePane tablePane = mock(TablePane.class);
        final Graph graph = mock(Graph.class);
        final TableViewState tableViewState = new TableViewState();

        final Table table = mock(Table.class);

        final ProgressBar progressBar = mock(ProgressBar.class);

        when(tablePane.getTable()).thenReturn(table);
        when(tablePane.getProgressBar()).thenReturn(progressBar);

        final TriggerDataUpdateTask updateTableDataTask
                = new TriggerDataUpdateTask(tablePane, graph, tableViewState);

        updateTableDataTask.run();

        verify(table).updateData(graph, tableViewState, progressBar);
    }
}
