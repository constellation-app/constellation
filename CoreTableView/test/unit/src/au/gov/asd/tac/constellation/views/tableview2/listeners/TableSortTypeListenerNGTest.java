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
package au.gov.asd.tac.constellation.views.tableview2.listeners;

import au.gov.asd.tac.constellation.views.tableview2.components.TableViewPane;
import au.gov.asd.tac.constellation.views.tableview2.service.TableService;
import au.gov.asd.tac.constellation.views.tableview2.state.TablePreferences;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Pagination;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.same;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
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
    private TableSortTypeListener tableSortTypeListener;
    
    private TableViewPane tablePane;
    private TableService tableService;
    
    public TableSortTypeListenerNGTest() {
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
        tablePane = mock(TableViewPane.class);
        tableService = spy(new TableService(null));
        when(tablePane.getTableService()).thenReturn(tableService);
        
        tableSortTypeListener = new TableSortTypeListener(tablePane);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void changedSortingListenerActive() {
        tableService.setSortingListenerActive(true);
        
        tableSortTypeListener.changed(null, null, null);
        
        verify(tableService, times(0)).updatePagination(anyInt());
    }
    
    @Test
    public void changedListenerInActive() throws InterruptedException {
        final int maxRowsPerPage = 5;
        
        tableService.setSortingListenerActive(false);
        
        final TablePreferences tablePreferences = new TablePreferences();
        tablePreferences.setMaxRowsPerPage(maxRowsPerPage);
        tableService.setTablePreferences(tablePreferences);
        
        final Pagination pagination = mock(Pagination.class);
        when(tableService.getPagination()).thenReturn(pagination);
        
        Mockito.doAnswer(mockInvocation -> {
            // This verifies then when update pagination is called, the
            // sortingListenerActive flag is true
            assertTrue(tableService.isSortingListenerActive());
            
            return pagination;
        }).when(tableService).updatePagination(maxRowsPerPage);
        
        tableSortTypeListener.changed(null, null, null);

        // Once the listener is complete the flag should be returned to false.
        assertFalse(tableService.isSortingListenerActive());
        
        verify(tableService).updatePagination(maxRowsPerPage);
        
        // This verification is dependent on code completing in the UI thread so
        // the following ensures that the verification does not occur until
        // the required code is run.
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> latch.countDown());
        latch.await();
        
        verify(tablePane).setCenter(same(pagination));
    }
}
