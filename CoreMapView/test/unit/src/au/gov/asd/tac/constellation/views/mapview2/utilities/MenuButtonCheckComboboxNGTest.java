/*
 * Copyright 2010-2024 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.views.mapview2.utilities;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
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
public class MenuButtonCheckComboboxNGTest {

    private static final Logger LOGGER = Logger.getLogger(MenuButtonCheckComboboxNGTest.class.getName());

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
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testSelectItem() {
        final MenuButtonCheckCombobox component = new MenuButtonCheckCombobox(FXCollections.observableArrayList("Option1", "Option2"), false, false);
        component.selectItem("Option1");

        assertEquals(component.getOptionMap().get("Option1").isSelected(), true);
    }

    @Test
    public void testGetOptionMap() {
        final MenuButtonCheckCombobox component = new MenuButtonCheckCombobox(FXCollections.observableArrayList("Option1", "Option2"), false, false);

        final Set<String> optionsSet = new HashSet<>();
        optionsSet.add("Option1");
        optionsSet.add("Option2");

        for (final String key : component.getOptionMap().keySet()) {
            assertEquals(optionsSet.contains(key), true);
        }
    }

    @Test
    public void testRevertLastAction() {
        final MenuButtonCheckCombobox component = new MenuButtonCheckCombobox(FXCollections.observableArrayList("Option1", "Option2"), false, false);
        component.selectItem("Option1");

        assertEquals(component.getOptionMap().get("Option1").isSelected(), true);
        component.revertLastAction();
        assertEquals(component.getOptionMap().get("Option1").isSelected(), false);
    }
}
