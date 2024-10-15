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
package au.gov.asd.tac.constellation.plugins;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.plugins.text.TextPluginInteraction;
import org.mockito.Mockito;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class TextPluginInteractionNGTest {

    public final int CURRENT_STEP = 1;
    public final int TOTAL_STEP = 2;
    public final String TEST_MESSAGE = "Test Message";
    public final String RUNNING_STATE = "Running State";
    public final Boolean CANCELLABLE_TRUE = true;
    public final Boolean CANCELLABLE_FALSE = false;
    public final int SELECTED_ITEMS = 1;
    public static final String GRAPH_ID = "Graph ID";
    public static final String HELP_ID = "Help_ID";
    public static final String DIALOG_BOX = "Dialog box";
    public static final String DISCLAIMER_MESSAGE = "Disclaimer Message";
    
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
        interaction.confirm(TEST_MESSAGE);
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
        when(interaction.getCurrentMessage()).thenReturn(TEST_MESSAGE);
        String currentMessage = interaction.getCurrentMessage();
        verify(interaction, times(1)).getCurrentMessage();
        assertEquals(currentMessage, TEST_MESSAGE);
    }
    
    @Test
    public void testSetProgressAllParameters() throws Exception {
        final PluginInteraction interaction = Mockito.mock(TextPluginInteraction.class);
        final PluginParameters params = spy(PluginParameters.class);
        interaction.setProgress(CURRENT_STEP, TOTAL_STEP, TEST_MESSAGE, CANCELLABLE_TRUE, params, SELECTED_ITEMS);
        verify(interaction, times(1)).setProgress(CURRENT_STEP, TOTAL_STEP, TEST_MESSAGE, CANCELLABLE_TRUE, params, SELECTED_ITEMS);
    }

    @Test
    public void testSetProgressPluginParameters() throws Exception {
        final PluginInteraction interaction = Mockito.mock(TextPluginInteraction.class);
        final PluginParameters params = spy(PluginParameters.class);
        interaction.setProgress(CURRENT_STEP, TOTAL_STEP, TEST_MESSAGE, CANCELLABLE_TRUE, params);
        verify(interaction, times(1)).setProgress(CURRENT_STEP, TOTAL_STEP, TEST_MESSAGE, CANCELLABLE_TRUE, params);       
    }
    
    @Test
    public void testSetProgressCancellable() throws Exception {
        final PluginInteraction interaction = Mockito.mock(TextPluginInteraction.class);        
        interaction.setProgress(CURRENT_STEP, TOTAL_STEP, CANCELLABLE_TRUE);
        verify(interaction, times(1)).setProgress(CURRENT_STEP, TOTAL_STEP, CANCELLABLE_TRUE);               
    }

    @Test
    public void testSetExecutionStageAllParameters() throws Exception {
        final PluginInteraction interaction = Mockito.mock(TextPluginInteraction.class);
        interaction.setExecutionStage(CURRENT_STEP, TOTAL_STEP, RUNNING_STATE, TEST_MESSAGE, CANCELLABLE_TRUE);
        verify(interaction, times(1)).setExecutionStage(CURRENT_STEP, TOTAL_STEP, RUNNING_STATE, TEST_MESSAGE, CANCELLABLE_TRUE);       
    }
}
