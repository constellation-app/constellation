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

import au.gov.asd.tac.constellation.help.utilities.Generator;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import processing.core.PApplet;

/**
 *
 * @author Quasar985
 */
public class StartJupyterNotebookActionNGTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of isWindows method, of class StartJupyterNotebookAction.
     */
    @Test
    public void testIsWindows() {
        System.out.println("isWindows");
        boolean expResult = System.getProperty("os.name").toLowerCase().contains("win");
        boolean result = StartJupyterNotebookAction.isWindows();
        assertEquals(result, expResult);
    }

    /**
     * Test of actionPerformed method, of class StartJupyterNotebookAction.
     */
    @Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");

        // Mocks
        Process processMock = mock(Process.class);
        try {
            when(processMock.waitFor()).thenReturn(0); // Return success
        } catch (InterruptedException ex) {
        }

        try (MockedStatic<Generator> generatorMock = Mockito.mockStatic(Generator.class); MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class); MockedStatic<PApplet> execute = Mockito.mockStatic(PApplet.class)) {
            generatorMock.when(Generator::getBaseDirectory).thenReturn("");
            webserverMock.when(WebServer::start).thenReturn(0);

            // Return our mocked process when exec is called
            execute.when(() -> PApplet.exec(any(String[].class))).thenReturn(processMock);

            // Create and run instance
            StartJupyterNotebookAction instance = new StartJupyterNotebookAction();
            instance.actionPerformed(null);

            // Assert the following functions were run
            webserverMock.verify(() -> WebServer.start(), times(1));
            execute.verify(() -> PApplet.exec(any(String[].class)), times(1));

            try {
                verify(processMock, times(1)).waitFor();
            } catch (InterruptedException ex) {
            }
        }
    }

}
