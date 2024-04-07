/*
 * Copyright 2010-2023 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.utilities.log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.testng.annotations.Test;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
/**
 *
 * @author Guilty-Spark-343
 */
public class LogUtilitiesNGTest {
    private static final Logger LOGGER = Logger.getLogger(LogUtilitiesNGTest.class.getName());
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
    
    private static Handler[] existingLogHandlers;
    
    // removes all handlers from a logger
    private void removeHandlers(final Logger logger, final Handler[] handlers) {
        for (final Handler h : handlers) {
            logger.removeHandler(h);
        }
    }
    
    /**
     * Attaches customLogHandler to the LogUtilities logger, which will also
     * receive logging events, and removes the class console logger.
     */
    @BeforeMethod
    public void attachLogCapturer() {
        // remove the existing handlers, but store them so they can be restored
        existingLogHandlers = LOGGER.getParent().getHandlers();
        removeHandlers(LOGGER.getParent(), existingLogHandlers);
        
        // add a custom handler based off the first existing handler
        logCapturingStream = new ByteArrayOutputStream();
        customLogHandler = new StreamHandler(logCapturingStream, 
                existingLogHandlers[0].getFormatter());
        customLogHandler.setFormatter(new ConstellationLogFormatter());
        LOGGER.getParent().addHandler(customLogHandler);
    }
    
    /**
     * Removes the Handler from the FontUtilities logger and restores
     * the console logger.
     */
    @AfterMethod
    public void removeLogCapturer() {
        removeHandlers(LOGGER.getParent(), LOGGER.getParent().getHandlers());
        for (final Handler h : existingLogHandlers) {
            LOGGER.getParent().addHandler(h);
        }
    }
    
    /**
     * Gets any logs captured so far by the customLogHandler.
     * 
     * @return any logs captured
     * @throws IOException if logs can't be retrieved
     */
    public String getCapturedLog() throws IOException {
        customLogHandler.flush();
        return logCapturingStream.toString();
    }
    
    @Test
    public void testFormat() throws IOException {
        LOGGER.log(Level.INFO, "Test message");
        String out = getCapturedLog();
        
        // Test the format of the log message
        assertTrue(out.contains("["));
        assertTrue(out.contains("Z] - "));
        assertTrue(out.contains(": "));
        assertTrue(out.contains(" INFO "));
        assertTrue(out.contains(LogUtilitiesNGTest.class.getName()));
        assertTrue(out.contains("Test message"));
    }
    
    @Test
    public void testFormatParams() throws IOException {
        LOGGER.log(Level.INFO, "Test message: {0}", "testParams");
        String out = getCapturedLog();
        
        // Test if the paramater is added to the log
        assertTrue(out.contains("testParams"));
    }
    
    @Test
    public void testFormatNoParams() throws IOException {
        LOGGER.log(Level.INFO, "Test message: {0}");
        String out = getCapturedLog();
        
        assertFalse(out.contains("testParams"));
        assertTrue(out.contains("Test message: {0}"));
    }
    
    @Test
    public void testFormatDuplicate() throws IOException {
        LOGGER.log(Level.INFO, "Test message");
        LOGGER.log(Level.INFO, "Test message");
        String out = getCapturedLog();
        
        assertTrue(out.contains("Last record repeated again."));
    }
    
    @Test
    public void testFormatDuplicateMany() throws IOException {
        for(int i=0; i < 20; i++){
            LOGGER.log(Level.INFO, "Test message");
        }
        
        final String out = getCapturedLog();
        assertTrue(out.contains("Last record repeated more than 10 times"));

        LOGGER.log(Level.INFO, "New message");
        final String newOut = getCapturedLog();
        assertTrue(newOut.contains("New message"));
    }
    
    @Test
    public void testLogWithThrowable() throws IOException {
        LOGGER.log(Level.INFO, "Test message", new Exception("Example exception"));
        
        final String out = getCapturedLog();
        
        assertTrue(out.contains("java.lang.Exception: Example exception"));
    }
    
    @Test
    public void testLogWithNullThrowable() throws IOException {
        LOGGER.log(Level.INFO, "Test message", new Exception());
        
        final String out = getCapturedLog();
        
        assertTrue(out.contains("Test message"));
    }
    
}
