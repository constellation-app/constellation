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
package au.gov.asd.tac.constellation.plugins.importexport.translator;

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import org.openide.util.lookup.ServiceProvider;

/**
 * The DefaultAttributeTranslator is an AttributeTranslator that does not alter
 * the input field.
 *
 * @author sirius
 */
@ServiceProvider(service = AttributeTranslator.class)
public class DefaultAttributeTranslator extends AttributeTranslator {

    public DefaultAttributeTranslator() {
        super("None", Integer.MIN_VALUE);
    }

    @Override
    public String translate(final String value, final PluginParameters parameters) {
        return value;
    }

    @Override
    public String getParameterValues(final PluginParameters parameters) {
        return null;
    }

    @Override
    public void setParameterValues(final PluginParameters parameters, final String values) {
        //Method must be overridden, intentionally left blank
    }
}
