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
package au.gov.asd.tac.constellation.utilities;

import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testng.annotations.AfterMethod;

/**
 * Unit tests for the GraphicsCardUtilities class.
 * 
 * @author groombridge34a
 */
public class GraphicsCardUtilitiesNGTest {
    
    private static final String DXDIAG_FILENAME = System.getProperty("user.home") + "/dxdiag.txt";
    private static final String CARD_NAME = "testGraphicsCard";
    private static final String DRIVER_VERSION = "testDriverVersion";
    private static final String DXDIAG = new StringBuilder()
            .append("dummy line").append(SeparatorConstants.NEWLINE)
            .append("    Card name: ").append(CARD_NAME).append(SeparatorConstants.NEWLINE)
            .append("another line").append(SeparatorConstants.NEWLINE)
            .append("Driver Version: ").append(DRIVER_VERSION).append(SeparatorConstants.NEWLINE)
            .append("   final line   ").toString();
  
    @AfterMethod
    private void tearDownMethod() {
        GraphicsCardUtilities.clear();
        new File(DXDIAG_FILENAME).delete();
    }
    
    /**
     * A mocked dxdiag call is dissected without errors and all getters retrieve
     * correct data.
     */
    @Test
    public void testLoadGraphicsCardInfo() {
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_FILENAME))) {
                    writer.write(DXDIAG);
                }
                return null;
            });
            
            // Assert getters
            assertEquals(GraphicsCardUtilities.getGraphicsCard(), CARD_NAME);
            assertEquals(GraphicsCardUtilities.getGraphicsDriver(), DRIVER_VERSION);
            assertNull(GraphicsCardUtilities.getError());
            
            // An extra newline is inserted by the code for some reason
            final String expDxDiag = DXDIAG + SeparatorConstants.NEWLINE;
            assertEquals(GraphicsCardUtilities.getDxDiagInfo(), expDxDiag);
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
    }
    
    /**
     * A mocked dxdiag call is run but returns nothing.
     */
    @Test
    public void testNoGraphicsCardInfo() {
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                new File(DXDIAG_FILENAME).createNewFile();
                return null;
            });
            
            // Assert the getters return nulls or empty Strings
            assertNull(GraphicsCardUtilities.getGraphicsCard());
            assertNull(GraphicsCardUtilities.getGraphicsDriver());
            assertNull(GraphicsCardUtilities.getError());
            assertEquals(GraphicsCardUtilities.getDxDiagInfo(), "");
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
    }
    
    /**
     * A mocked dxdiag call doesn't contain card name or driver name.
     */
    @Test
    public void testNoCardOrDriver() {
        final String dummyDxDiag = new StringBuilder()
                .append("dummy line 1").append(SeparatorConstants.NEWLINE)
                .append("dummy line 2").append(SeparatorConstants.NEWLINE)
                .toString();
        
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dummy dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_FILENAME))) {
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
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
    }
    
    // Create and assert an existing dxdiag file
    private File createDxDiagFile() {
        final File dxDiagFile = new File(DXDIAG_FILENAME);
        try {
            dxDiagFile.createNewFile();
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
        
        assertTrue(dxDiagFile.exists());
        return dxDiagFile;
    }

    /**
     * A dxdiag file already exists and must be removed.
     */
    @Test
    public void testFileExists() {
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
                new File(DXDIAG_FILENAME).createNewFile();
                return null;
            });
            
            // Assert that an IOException wasn't thrown and caught
            assertNull(GraphicsCardUtilities.getError());
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
    }
    
    /**
     * A dxdiag file already exists but cannot be removed.
     */
    //@Test
    //public void testReadOnlyFileExists() {}
    //FIXME: refactor needed to inject file creation into the class

    /**
     * When an IOException is thrown and caught the getter exposes the error.
     */
    @Test
    public void testException() {
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
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
    }
    
    /**
     * The code is only run once unless the clear() method is not run.
     */
    @Test
    public void testRunOnce() {        
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_FILENAME))) {
                    writer.write(DXDIAG_FILENAME);
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
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
    }
    
    /**
     * dxdiag information can be cleared from the utils class
     */
    @Test
    public void testClear() {        
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Mock the call to dxdiag by Runtime.exec() to create a dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(DXDIAG_FILENAME))) {
                    writer.write(DXDIAG);
                }
                return null;
            });
            
            // Assert that data is present
            assertNotNull(GraphicsCardUtilities.getGraphicsCard());
            assertNotNull(GraphicsCardUtilities.getGraphicsDriver());
            assertTrue(GraphicsCardUtilities.getDxDiagInfo().contains(DXDIAG));
            assertNull(GraphicsCardUtilities.getError());
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
        
        GraphicsCardUtilities.clear();
        
        try (final MockedStatic<Runtime> runtimeMockedStatic = mockStatic(Runtime.class)) {
            // Now have the mock return an empty dxdiag file
            final Runtime runtime = mock(Runtime.class);
            runtimeMockedStatic.when(() -> Runtime.getRuntime()).thenReturn(runtime);
            when(runtime.exec(anyString())).thenAnswer(invocation -> {
                new File(DXDIAG_FILENAME).createNewFile();
                return null;
            });
            
            // Assert that the only data present is an empty String from the dxdiag getter
            assertNull(GraphicsCardUtilities.getGraphicsCard());
            assertNull(GraphicsCardUtilities.getGraphicsDriver());
            assertEquals(GraphicsCardUtilities.getDxDiagInfo(), "");
            assertNull(GraphicsCardUtilities.getError());
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
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
        } catch (IOException e) {
            fail("Test threw Exception: " + e.toString());
        }
    }    
    
}
