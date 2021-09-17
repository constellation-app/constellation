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
package au.gov.asd.tac.constellation.views.tableview2.plugins;

import au.gov.asd.tac.constellation.graph.GraphElementType;
import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.views.tableview2.state.TableViewState;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import org.testfx.api.FxToolkit;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotSame;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author formalhaunt
 */
public class UpdateStatePluginNGTest {
    
    public UpdateStatePluginNGTest() {
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
    public void updateStatePlugin() throws InterruptedException, PluginException {
        final GraphWriteMethods graph = mock(GraphWriteMethods.class);

        final TableViewState tableViewState = new TableViewState();
        tableViewState.setElementType(GraphElementType.META);

        final UpdateStatePlugin updateStatePlugin
                = new UpdateStatePlugin(tableViewState);

        updateStatePlugin.edit(graph, null, null);

        final ArgumentCaptor<TableViewState> captor = ArgumentCaptor.forClass(TableViewState.class);
        verify(graph).setObjectValue(eq(0), eq(0), captor.capture());

        assertEquals(tableViewState, captor.getValue());
        assertNotSame(tableViewState, captor.getValue());

        assertEquals("Table View: Update State", updateStatePlugin.getName());
    }
}
