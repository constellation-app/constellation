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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.logging.DefaultConstellationLogger;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import org.apache.commons.lang3.StringUtils;

/**
 * The PasswordParameterType defines {@link PluginParameter} objects that hold
 * passwords and informs the plugin framework to treat them as special Passwords
 * and obfuscate them when used by {@link DefaultConstellationLogger}.
 *
 * @author arcturus
 */
public class PasswordParameterType extends PluginParameterType<PasswordParameterValue> {

    /**
     * A Password ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "password";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final PasswordParameterType INSTANCE = new PasswordParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of PasswordParameterType.
     */
    public static PluginParameter<PasswordParameterValue> build(String id) {
        return new PluginParameter<>(new PasswordParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link PasswordParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link PasswordParameterValue} describing the initial value
     * of the parameter being constructed.
     * @return A {@link PluginParameter} of PasswordParameterType.
     */
    public static PluginParameter<PasswordParameterValue> build(String id, final PasswordParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * Constructs a new instance of this type.
     * <p>
     * Note: This constructor should not be called directly; it is public for
     * the purposes of lookup (which may be removed for types in the future). To
     * buildId parameters from the type, the static method
     * {@link #build buildId()} should be used, or the singleton
     * {@link #INSTANCE INSTANCE}.
     */
    public PasswordParameterType() {
        super(ID);
    }

    @Override
    public String validateString(PluginParameter<PasswordParameterValue> param, String stringValue) {
        if (StringUtils.isBlank(stringValue)) {
            return "Parameter is Empty!";
        }
        return null;
    }

}
