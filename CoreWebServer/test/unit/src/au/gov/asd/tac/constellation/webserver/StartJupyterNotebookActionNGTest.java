/*
 * Copyright 2010-2025 Australian Signals Directorate
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

import java.io.File;
import java.io.InputStream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
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

    /**
     * Test of actionPerformed method, of class StartJupyterNotebookAction.
     */
    @Test
    public void testActionPerformedInvalidPath() throws Exception {
        System.out.println("actionPerformedInvalidPath");

        final ProcessBuilder mockProcessBuilder = mock(ProcessBuilder.class);
        final Process mockProcess = mock(Process.class);
        final InputStream mockInputStream = mock(InputStream.class);
        final int inputStreamReadResult = -1;

        when(mockProcessBuilder.redirectErrorStream(anyBoolean())).thenReturn(mockProcessBuilder);
        when(mockProcessBuilder.start()).thenReturn(mockProcess);
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);
        when(mockInputStream.read(any())).thenReturn(inputStreamReadResult);

        try (MockedStatic<WebServer> webserverMock = Mockito.mockStatic(WebServer.class); MockedConstruction<ProcessBuilder> mockProcessBuilderConstructor = Mockito.mockConstruction(ProcessBuilder.class, (mock, context) -> {
            when(mock.directory(any(File.class))).thenReturn(mockProcessBuilder);
        }); MockedConstruction<File> mockFileConstructor = Mockito.mockConstruction(File.class, (mock, context) -> {
            when(mock.exists()).thenReturn(false);
        })) {
            webserverMock.when(WebServer::start).thenReturn(0);

            // Create and run instance
            StartJupyterNotebookAction instance = new StartJupyterNotebookAction();
            instance.actionPerformed(null);

            // Assert the following functions were run
            webserverMock.verify(() -> WebServer.start(), never());

            assertEquals(mockProcessBuilderConstructor.constructed().size(), 0);
            assertTrue(mockFileConstructor.constructed().size() >= 2);

            verify(mockProcessBuilder, times(0)).redirectErrorStream(anyBoolean());
            verify(mockProcessBuilder, times(0)).start();
            verify(mockProcess, times(0)).getInputStream();
            verify(mockInputStream, times(0)).read(any());
        }
    }
}
