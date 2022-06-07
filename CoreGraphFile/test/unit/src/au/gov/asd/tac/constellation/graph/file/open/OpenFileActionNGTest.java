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
package au.gov.asd.tac.constellation.graph.file.open;

import au.gov.asd.tac.constellation.graph.StoreGraph;
import au.gov.asd.tac.constellation.graph.file.GraphFilePluginRegistry;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginExecution;
import java.awt.event.ActionEvent;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for OpenFileAction.
 *
 * @author sol695510
 */
public class OpenFileActionNGTest {

    private static MockedStatic<PluginExecution> pluginExecutionStaticMock;
    private static PluginExecution pluginExecutionMock;
    private static Thread threadMock;

    public OpenFileActionNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        pluginExecutionStaticMock = Mockito.mockStatic(PluginExecution.class);
        pluginExecutionMock = Mockito.mock(PluginExecution.class);
        threadMock = Mockito.mock(Thread.class);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        pluginExecutionStaticMock.close();
    }

    /**
     * Test of actionPerformed method, of class OpenFileAction.
     *
     * @throws InterruptedException
     * @throws PluginException
     */
    @Test
    public void testActionPerformed() throws InterruptedException, PluginException {
        System.out.println("testActionPerformed");

        final OpenFileAction instance = new OpenFileAction();
        final ActionEvent e = null;

        pluginExecutionStaticMock.when(()
                -> PluginExecution.withPlugin(GraphFilePluginRegistry.OPEN_FILE)).thenReturn(pluginExecutionMock);

        instance.actionPerformed(e);

        verify(pluginExecutionMock, times(1)).executeNow(Mockito.any(StoreGraph.class));

        // If a PluginException is thrown during plugin execution.
        pluginExecutionStaticMock.when(()
                -> PluginExecution.withPlugin(GraphFilePluginRegistry.OPEN_FILE).executeNow(Mockito.any(StoreGraph.class))).thenThrow(PluginException.class);

        instance.actionPerformed(e);

        verifyNoInteractions(threadMock);

        // If an InterruptedException is thrown during plugin execution.
        pluginExecutionStaticMock.when(()
                -> PluginExecution.withPlugin(GraphFilePluginRegistry.OPEN_FILE).executeNow(Mockito.any(StoreGraph.class))).thenThrow(InterruptedException.class);

        instance.actionPerformed(e);

        verify(threadMock, times(1)).currentThread().interrupt();
    }
}
