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
package au.gov.asd.tac.constellation.plugins.gui;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterListener;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInput;
import au.gov.asd.tac.constellation.utilities.gui.field.framework.ConstellationInputListener;
import static org.geotools.gml3.GML.stringValue;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Andromeda-224
 */
public class ParameterInputPaneNGTest {
    
    ParameterInputPaneImpl parameterInputPaneMock;
    ConstellationInput inputMock;
    PluginParameter pluginParamMock;
    
    public ParameterInputPaneNGTest() {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        inputMock = mock(ConstellationInput.class);
        pluginParamMock = mock(PluginParameter.class);
        parameterInputPaneMock = spy(new ParameterInputPaneImpl(inputMock, pluginParamMock));
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }
    
    @Test
    public void testParameterInputPane_inputReference() {
        assertEquals(parameterInputPaneMock.getInputReference(), inputMock);
    }
    
    @Test
    public void testParameterInputPane_updateField() {
        final Object value = mock(Object.class);
        parameterInputPaneMock.setFieldValue(value);
        doReturn(value).when(inputMock).getValue();
        assertEquals(parameterInputPaneMock.getFieldValue(), value);
        verify(inputMock, times(1)).setValue(Mockito.any());
    }
    
    @Test
    public void testParameterInputPane_setFieldHeight() {
        final int height = 101;
        parameterInputPaneMock.setFieldHeight(height);
        verify(inputMock, times(1)).setPrefRowCount(height);
    }

    @Test
    public void testParameterInputPane_updateFieldVisibility() {
        doReturn(true).when(pluginParamMock).isVisible();
        parameterInputPaneMock.updateFieldVisibility();
        verify(inputMock, times(1)).setManaged(true);
        verify(parameterInputPaneMock, times(1)).setManaged(true);
        verify(parameterInputPaneMock, times(1)).setVisible(true);
        verify(inputMock, times(1)).setVisible(true);
    }
    
    @Test
    public void testParameterInputPane_updateFieldEnablement() {
        doReturn(true).when(pluginParamMock).isEnabled();
        parameterInputPaneMock.updateFieldEnablement();
        verify(inputMock, times(1)).setDisable(false);
    }
    
    private static class ParameterInputPaneImpl extends ParameterInputPane {

        public ParameterInputPaneImpl(ConstellationInput input, PluginParameter parameter) {
            super(input, parameter);
        }

        @Override
        public ConstellationInputListener getFieldChangeListener(PluginParameter parameter) {
            ConstellationInputListener listener = mock(ConstellationInputListener.class);
            return listener;
        }

        @Override
        public PluginParameterListener getPluginParameterListener() {
            PluginParameterListener listener = mock(PluginParameterListener.class);
            return listener;
        }
         
     }
}
