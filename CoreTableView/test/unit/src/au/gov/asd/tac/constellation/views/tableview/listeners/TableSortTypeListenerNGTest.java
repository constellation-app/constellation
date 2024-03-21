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
package au.gov.asd.tac.constellation.views.tableview.listeners;

import au.gov.asd.tac.constellation.views.tableview.api.ActiveTableReference;
import au.gov.asd.tac.constellation.views.tableview.api.UserTablePreferences;
import au.gov.asd.tac.constellation.views.tableview.panes.TablePane;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Pagination;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableSortTypeListenerNGTest {
    private static final Logger LOGGER = Logger.getLogger(TableSortTypeListenerNGTest.class.getName());

    private TableSortTypeListener tableSortTypeListener;

    private TablePane tablePane;
    private ActiveTableReference activeTableReference;

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

    @BeforeMethod
    public void setUpMethod() throws Exception {
        tablePane = mock(TablePane.class);
        activeTableReference = spy(new ActiveTableReference(null));
        when(tablePane.getActiveTableReference()).thenReturn(activeTableReference);

        tableSortTypeListener = new TableSortTypeListener(tablePane);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void changedSortingListenerActive() {
        activeTableReference.setSortingListenerActive(true);

        tableSortTypeListener.changed(null, null, null);

        verify(activeTableReference, times(0)).updatePagination(anyInt(), any(TablePane.class));
    }

    @Test
    public void changedListenerInActive() throws InterruptedException {
        final int maxRowsPerPage = 5;

        activeTableReference.setSortingListenerActive(false);

        final UserTablePreferences userTablePreferences = new UserTablePreferences();
        userTablePreferences.setMaxRowsPerPage(maxRowsPerPage);
        activeTableReference.setUserTablePreferences(userTablePreferences);

        final Pagination pagination = mock(Pagination.class);
        when(activeTableReference.getPagination()).thenReturn(pagination);

        Mockito.doAnswer(mockInvocation -> {
            // This verifies then when update pagination is called, the
            // sortingListenerActive flag is true
            assertTrue(activeTableReference.isSortingListenerActive());

            return pagination;
        }).when(activeTableReference).updatePagination(maxRowsPerPage, tablePane);

        tableSortTypeListener.changed(null, null, null);

        // Once the listener is complete the flag should be returned to false.
        assertFalse(activeTableReference.isSortingListenerActive());

        verify(activeTableReference).updatePagination(maxRowsPerPage, tablePane);
    }
}
