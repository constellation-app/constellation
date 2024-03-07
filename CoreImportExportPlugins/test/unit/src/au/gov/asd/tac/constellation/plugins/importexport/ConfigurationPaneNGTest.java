/*
 * Copyright 2010-2022 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.importexport;

import au.gov.asd.tac.constellation.plugins.importexport.jdbc.JDBCImportController;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.testfx.api.FxToolkit;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author altair1673
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class ConfigurationPaneNGTest extends ConstellationTest {
    private static final Logger LOGGER = Logger.getLogger(ConfigurationPaneNGTest.class.getName());
    public ConfigurationPaneNGTest() {
    }

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

            LOGGER.log(Level.WARNING, "FxToolkit timed out trying to cleanup stages: ", ex);
        }

    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test if selected run pane gets cleared
     */
    @Test
    public void testClearSelectedPane() {
        System.out.println("clearSelectedPane");

        JDBCImportController j = new JDBCImportController();

        ConfigurationPane instance = new ConfigurationPane(j, "testHelpText");

        final String[] tempColumns = {"test", "test", "test"};
        final List<String[]> tempData = new ArrayList<>();

        tempData.add(tempColumns);
        tempData.add(tempColumns);
        tempData.add(tempColumns);

        instance.setSampleData(tempColumns, tempData);


        instance.clearSelectedPane();

        RunPane runPane = (RunPane) instance.tabPane.getSelectionModel().getSelectedItem().getContent();

        assertEquals(0, runPane.getSampleDataView().getItems().size());
        assertEquals(0, runPane.getSampleDataView().getColumns().size());

    }


}
