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
package au.gov.asd.tac.constellation.webserver;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Quasar985
 */
public class StartJupyterNotebookActionNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Not currently required
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Not currently required
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        // Not currently required
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        // Not currently required
    }

    /**
     * Test of actionPerformed method, of class StartJupyterNotebookAction.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");

        try (MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class)) {
            webserverMock.when(WebServer::start).thenReturn(0);

            // Create and run instance
            StartJupyterNotebookAction instance = new StartJupyterNotebookAction();
            instance.actionPerformed(null);

            // Assert the following functions were run
            webserverMock.verify(() -> WebServer.start(), times(1));
        }
    }
}
