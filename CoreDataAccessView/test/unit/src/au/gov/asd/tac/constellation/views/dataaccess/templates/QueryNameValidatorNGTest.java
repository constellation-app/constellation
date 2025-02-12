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
package au.gov.asd.tac.constellation.views.dataaccess.templates;

import au.gov.asd.tac.constellation.graph.processing.RecordStore;
import au.gov.asd.tac.constellation.plugins.PluginException;
import au.gov.asd.tac.constellation.plugins.PluginInteraction;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import au.gov.asd.tac.constellation.views.dataaccess.CoreGlobalParameters;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.testng.annotations.Test;

/**
 *
 * @author arcturus
 */
public class QueryNameValidatorNGTest {

    /**
     * Test of validatePreQuery method, of class QueryNameValidator.
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test(expectedExceptions = PluginException.class)
    public void testValidatePreQueryWhenBlank() throws PluginException {
        System.out.println("testValidatePreQueryWhenBlank");

        final RecordStoreQueryPlugin plugin = mock(RecordStoreQueryPlugin.class);
        final RecordStore recordStore = mock(RecordStore.class);
        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);
        final QueryNameValidator instance = new QueryNameValidator();

        when(parameters.getStringValue(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID)).thenReturn("");

        instance.validatePreQuery(plugin, recordStore, interaction, parameters);
    }

    /**
     * Test of validatePreQuery method, of class QueryNameValidator.
     * @throws au.gov.asd.tac.constellation.plugins.PluginException
     */
    @Test
    public void testValidatePreQueryWhenNotBlank() throws PluginException {
        System.out.println("testValidatePreQueryWhenNotBlank");

        final RecordStoreQueryPlugin plugin = mock(RecordStoreQueryPlugin.class);
        final RecordStore recordStore = mock(RecordStore.class);
        final PluginInteraction interaction = mock(PluginInteraction.class);
        final PluginParameters parameters = mock(PluginParameters.class);
        final QueryNameValidator instance = new QueryNameValidator();

        when(parameters.getStringValue(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID)).thenReturn("my query name");

        instance.validatePreQuery(plugin, recordStore, interaction, parameters);

        verify(parameters, times(1)).getStringValue(CoreGlobalParameters.QUERY_NAME_PARAMETER_ID);
    }
}
