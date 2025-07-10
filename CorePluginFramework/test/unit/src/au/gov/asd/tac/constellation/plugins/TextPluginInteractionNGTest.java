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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class TextPluginInteractionNGTest {

    private final int currentStep = 1;
    private final int totalStep = 2;
    private final String testMessage = "Test Message";
    private final String runningState = "Running State";
    private final Boolean cancellableTrue = true;
    private final int selectedItems = 1;
    private static final String GRAPH_ID = "Graph ID";
    private static final String HELP_ID = "Help_ID";
    private static final String DIALOG_BOX = "Dialog box";
    private static final String DISCLAIMER_MESSAGE = "Disclaimer Message";
    
    private static final Logger LOGGER = Logger.getLogger(TextPluginInteraction.class.getName());
    private static OutputStream logCapturingStream;
    private static StreamHandler customLogHandler;
  
    private static Handler[] existingLogHandlers;
    
    /**
     * Removes all handlers from a logger
     */
    private void removeHandlers(final Logger logger, final Handler[] handlers) {
        for (final Handler h : handlers) {
            logger.removeHandler(h);
        }
    }
    
    /**
     * Attaches customLogHandler to the logger, which will also
     * receive logging events, and removes the class console logger.
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpMethod() throws Exception {
        // remove the existing handlers, but store them so they can be restored
        existingLogHandlers = LOGGER.getParent().getHandlers();
        removeHandlers(LOGGER.getParent(), existingLogHandlers);
        
        // add a custom handler based off the first existing handler
        logCapturingStream = new ByteArrayOutputStream();
        customLogHandler = new StreamHandler(logCapturingStream, 
                existingLogHandlers[0].getFormatter());
        LOGGER.getParent().addHandler(customLogHandler);
    }
    
    /**
     * Removes the Handler from the logger and restores the console logger.
     * Needs to be done for each test so that the captured logs are cleared.
     */
    @AfterMethod
    public void tearDownMethod() {
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
    public void testIsInteractive() {
        final PluginInteraction interaction = new TextPluginInteraction();
        assertFalse(interaction.isInteractive());
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testSetBusy() {
        final PluginInteraction interaction = new TextPluginInteraction();
        interaction.setBusy(GRAPH_ID, true);
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testConfirm() {
        final PluginInteraction interaction = new TextPluginInteraction();
        interaction.confirm(testMessage);
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPrompt() {
        final PluginInteraction interaction = new TextPluginInteraction();
        PluginParameters parameters = Mockito.mock(PluginParameters.class);
        interaction.prompt(DIALOG_BOX, parameters, HELP_ID);
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testPromptDisclaimer() {
        final PluginInteraction interaction = new TextPluginInteraction();
        PluginParameters parameters = Mockito.mock(PluginParameters.class);
        interaction.prompt(DIALOG_BOX, parameters, DISCLAIMER_MESSAGE, HELP_ID);
    }
    
    @Test
    public void testGetCurrentMessage() {
        final PluginInteraction interaction =  spy(TextPluginInteraction.class);
        when(interaction.getCurrentMessage()).thenReturn(testMessage);
        String currentMessage = interaction.getCurrentMessage();
        verify(interaction, times(1)).getCurrentMessage();
        assertEquals(currentMessage, testMessage);
    }
    
    @Test
    public void testSetProgressAllParameters() throws Exception {
        final PluginInteraction interaction = new TextPluginInteraction();
        final PluginParameters params = spy(PluginParameters.class);
        interaction.setProgress(currentStep, totalStep, testMessage, cancellableTrue, params, selectedItems);
        
        assertTrue(getCapturedLog().contains("currentStep=" + currentStep));
        assertTrue(getCapturedLog().contains("totalSteps=" + totalStep));
        assertTrue(getCapturedLog().contains("message=" + testMessage));
        assertTrue(getCapturedLog().contains("parameters="));
        assertTrue(getCapturedLog().contains("selected=" + selectedItems));       
    }

    @Test
    public void testSetProgressPluginParameters() throws Exception {
        final PluginInteraction interaction = new TextPluginInteraction();
        final PluginParameters params = spy(PluginParameters.class);
        interaction.setProgress(currentStep, totalStep, testMessage, cancellableTrue, params);
        assertTrue(getCapturedLog().contains("currentStep=" + currentStep));
        assertTrue(getCapturedLog().contains("totalSteps=" + totalStep));
        assertTrue(getCapturedLog().contains("message=" + testMessage));
        assertTrue(getCapturedLog().contains("parameters="));
        assertFalse(getCapturedLog().contains("selected=" + selectedItems));      
    }
    
    @Test
    public void testSetProgressCancellable() throws Exception {
        final PluginInteraction interaction = new TextPluginInteraction();
        interaction.setProgress(currentStep, totalStep, cancellableTrue);
        assertTrue(getCapturedLog().contains("currentStep="+ currentStep));
        assertTrue(getCapturedLog().contains("totalSteps="+ totalStep));
        assertFalse(getCapturedLog().contains("message=" + testMessage));
        assertFalse(getCapturedLog().contains("parameters="));
        assertFalse(getCapturedLog().contains("selected=" + selectedItems));                
    }

    @Test
    public void testSetExecutionStageAllParameters() throws Exception {
        
        final PluginInteraction interaction = new TextPluginInteraction();
        interaction.setExecutionStage(currentStep, totalStep, runningState, testMessage, cancellableTrue);        
        assertTrue(getCapturedLog().contains("currentStep="+ currentStep));
        assertTrue(getCapturedLog().contains("totalSteps="+ totalStep));
        assertTrue(getCapturedLog().contains("message=" + testMessage));
        assertFalse(getCapturedLog().contains("parameters="));
        assertFalse(getCapturedLog().contains("selected=" + selectedItems)); 
    }        
    
    @Test
    public void testNotify() throws IOException {
        final PluginInteraction interaction = new TextPluginInteraction();
        interaction.notify(PluginNotificationLevel.FATAL, testMessage);
        assertTrue(getCapturedLog().contains("level=" + PluginNotificationLevel.FATAL));
        assertTrue(getCapturedLog().contains("message=" + testMessage));
    }
}
