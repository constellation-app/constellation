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
package au.gov.asd.tac.constellation.utilities;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for the GraphicsCardUtilities class.
 *
 * @author groombridge34a
 */
public class GraphicsCardUtilitiesNGTest {

    private static final String DXDIAG_ABSOLUTE_PATH = System.getProperty("user.home") + "/dxdiag.txt";
    private static final String CARD_NAME = "testGraphicsCard";
    private static final String DRIVER_VERSION = "testDriverVersion";
    private static final String DXDIAG_OUTPUT = new StringBuilder()
            .append("dummy line").append(SeparatorConstants.NEWLINE)
            .append("    Card name: ").append(CARD_NAME).append(SeparatorConstants.NEWLINE)
            .append("another line").append(SeparatorConstants.NEWLINE)
            .append("Driver Version: ").append(DRIVER_VERSION).append(SeparatorConstants.NEWLINE)
            .append("   final line   ").toString();

    @AfterMethod
    public void tearDownMethod() throws Exception {
        GraphicsCardUtilities.clear();
        new File(DXDIAG_ABSOLUTE_PATH).delete();
    }

    /**
     * A mocked dxdiag call is dissected without errors and all getters retrieve
     * correct data.
     *
     * @throws IOException if the test was unable to read to or write from the
     * dxdiag file.
     */
    @Test
    public void testLoadGraphicsCardInfo() throws IOException {
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_ABSOLUTE_PATH))) {
                    writer.write(DXDIAG_OUTPUT);
                }
                return null;
            });

            // Assert getters
            assertEquals(GraphicsCardUtilities.getGraphicsCard(), CARD_NAME);
            assertEquals(GraphicsCardUtilities.getGraphicsDriver(), DRIVER_VERSION);
            assertNull(GraphicsCardUtilities.getError());

            // An extra newline is inserted by the code for some reason
            final String expDxDiag = DXDIAG_OUTPUT + SeparatorConstants.NEWLINE;
            assertEquals(GraphicsCardUtilities.getDxDiagInfo(), expDxDiag);
        }
    }

    /**
     * A mocked dxdiag call is run but returns nothing.
     *
     * @throws IOException if the test was unable to read to or write from the
     * dxdiag file.
     */
    @Test
    public void testNoGraphicsCardInfo() throws IOException {
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                new File(DXDIAG_ABSOLUTE_PATH).createNewFile();
                return null;
            });

            // Assert the getters return nulls or empty Strings
            assertNull(GraphicsCardUtilities.getGraphicsCard());
            assertNull(GraphicsCardUtilities.getGraphicsDriver());
            assertNull(GraphicsCardUtilities.getError());
            assertEquals(GraphicsCardUtilities.getDxDiagInfo(), "");
        }
    }

    /**
     * A mocked dxdiag call doesn't contain card name or driver name.
     *
     * @throws IOException if the test was unable to read to or write from the
     * dxdiag file.
     */
    @Test
    public void testNoCardOrDriver() throws IOException {
        final String dummyDxDiag = new StringBuilder()
                .append("dummy line 1").append(SeparatorConstants.NEWLINE)
                .append("dummy line 2").append(SeparatorConstants.NEWLINE)
                .toString();

        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dummy dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_ABSOLUTE_PATH))) {
                    writer.write(dummyDxDiag);
                }
                return null;
            });

            // Assert the targetted getters return nulls
            assertNull(GraphicsCardUtilities.getGraphicsCard());
            assertNull(GraphicsCardUtilities.getGraphicsDriver());
            assertNull(GraphicsCardUtilities.getError());

            // Assert the whole dxdiag output is returned
            assertEquals(GraphicsCardUtilities.getDxDiagInfo(), dummyDxDiag);
        }
    }

    // Create and assert an existing dxdiag file
    private File createDxDiagFile() throws IOException {
        final File dxDiagFile = new File(DXDIAG_ABSOLUTE_PATH);
        dxDiagFile.createNewFile();
        assertTrue(dxDiagFile.exists());
        return dxDiagFile;
    }

    /**
     * A dxdiag file already exists and must be removed.
     *
     * @throws IOException if the test was unable to read to or write from the
     * dxdiag file.
     */
    @Test
    public void testFileExists() throws IOException {
        final File dxDiagFile = createDxDiagFile();

        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to throw an Exception if the dxdiag
            // file wasn't deleted
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                if (dxDiagFile.exists()) {
                    throw new IOException("Test Failure: dxDiagFile exists");
                }
                new File(DXDIAG_ABSOLUTE_PATH).createNewFile();
                return null;
            });

            // Assert that an IOException wasn't thrown and caught
            assertNull(GraphicsCardUtilities.getError());
        }
    }
    
    /**
     * When an IOException is thrown and caught the getter exposes the error.
     *
     * @throws IOException if the test was unable to read to or write from the
     * dxdiag file.
     */
    @Test
    public void testException() throws IOException {
        final String err = "Test Failure: dxDiagFile exists";
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to throw an IOException
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                throw new IOException(err);
            });

            // Assert that an IOException was caught
            assertTrue(GraphicsCardUtilities.getError().toString().contains(err));
        }
    }

    /**
     * The code is only run once unless the clear() method is not run.
     *
     * @throws IOException if the test was unable to read to or write from the
     * dxdiag file.
     */
    @Test
    public void testRunOnce() throws IOException {
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_ABSOLUTE_PATH))) {
                    writer.write(DXDIAG_ABSOLUTE_PATH);
                }
                return null;
            });

            // Verify that using getters will run exec a single time...
            verify(runtime, times(0)).exec(anyString());
            GraphicsCardUtilities.getDxDiagInfo();
            verify(runtime, times(1)).exec(anyString());
            GraphicsCardUtilities.getDxDiagInfo();
            verify(runtime, times(1)).exec(anyString());

            // until clear() is called, which will cause exec() to be run again
            GraphicsCardUtilities.clear();
            GraphicsCardUtilities.getDxDiagInfo();
            verify(runtime, times(2)).exec(anyString());
        }
    }

    /**
     * dxdiag information can be cleared from the utils class
     *
     * @throws IOException if the test was unable to read to or write from the
     * dxdiag file.
     */
    @Test
    public void testClear() throws IOException {
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_ABSOLUTE_PATH))) {
                    writer.write(DXDIAG_OUTPUT);
                }
                return null;
            });

            // Assert that data is present
            assertNotNull(GraphicsCardUtilities.getGraphicsCard());
            assertNotNull(GraphicsCardUtilities.getGraphicsDriver());
            assertTrue(GraphicsCardUtilities.getDxDiagInfo().contains(DXDIAG_OUTPUT));
            assertNull(GraphicsCardUtilities.getError());
        }

        GraphicsCardUtilities.clear();

        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Now have the mock return an empty dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                new File(DXDIAG_ABSOLUTE_PATH).createNewFile();
                return null;
            });

            // Assert that the only data present is an empty String from the dxdiag getter
            assertNull(GraphicsCardUtilities.getGraphicsCard());
            assertNull(GraphicsCardUtilities.getGraphicsDriver());
            assertEquals(GraphicsCardUtilities.getDxDiagInfo(), "");
            assertNull(GraphicsCardUtilities.getError());
        }

        GraphicsCardUtilities.clear();

        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Finally have the mock return an error
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                throw new IOException();
            });

            // Assert that only an error is present
            assertNull(GraphicsCardUtilities.getGraphicsCard());
            assertNull(GraphicsCardUtilities.getGraphicsDriver());
            assertNull(GraphicsCardUtilities.getDxDiagInfo());
            assertNotNull(GraphicsCardUtilities.getError());
        }
    }
}
