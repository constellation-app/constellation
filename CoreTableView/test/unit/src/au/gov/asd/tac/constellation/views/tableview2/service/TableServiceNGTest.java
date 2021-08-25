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
package au.gov.asd.tac.constellation.views.tableview2.service;

import java.util.HashMap;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class TableServiceNGTest {
    public FxRobot fx = new FxRobot();
    
    private SortedList<ObservableList<String>> sortedRowList;
    private Map<Integer, ObservableList<String>> elementIdToRowIndex;
    private Map<ObservableList<String>, Integer> rowToElementIdIndex;
    
    private TableService tableService;
    
//    static {
//        System.setProperty("java.awt.headless", "true");
//        System.setProperty("testfx.robot", "glass");
//        System.setProperty("testfx.headless", "true");
//        System.setProperty("prism.order", "sw");
//        System.setProperty("prism.text", "t2k");
//    }
    
    public TableServiceNGTest() {
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
        sortedRowList = new SortedList<>(FXCollections.observableArrayList());
        rowToElementIdIndex = new HashMap<>();
        elementIdToRowIndex = new HashMap<>();
        
        tableService = new TableService(sortedRowList, elementIdToRowIndex, rowToElementIdIndex);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void saveSortDetails() {
        assertEquals(
                Map.of("", TableColumn.SortType.ASCENDING), 
                tableService.getTablePreferences().getSortByColumn()
        );
        
        tableService.saveSortDetails("ABC", TableColumn.SortType.DESCENDING);
     
        assertEquals(
                Map.of("ABC", TableColumn.SortType.DESCENDING), 
                tableService.getTablePreferences().getSortByColumn()
        );
    }
    
    
}
