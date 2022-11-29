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
package au.gov.asd.tac.constellation.plugins.parameters;

import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;

/**
 * A PluginParameterType defines the behavior for a type of
 * {@link PluginParameter}. PluginParameterTypes are in one-to-one
 * correspondence with {@link ParameterValue} objects. Their main job is
 * interfacing between the parameter and the value for the purpose of copying
 * and validation. In the future getting and setting may also be here but it is
 * currently done through {@link PluginParameter} directly.
 * <p>
 * Implementations of this class also acts as factories to build new
 * {@link PluginParameter} objects with the given type-value pair from static
 * methods. On occasion, they also contain static methods to set properties of a
 * parameter peculiar to the type; for example the set of choices for a
 * {@link au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType}.
 * <p>
 * Note that whilst this class and its implementations have public constructors
 * for the purposes of lookup, they are typically singletons, with the single
 * instances used to construct and interact with all {@link PluginParameters} of
 * that given type.
 *
 * @author sirius
 * @param <V> The type of {@link ParameterValue} that this type corresponds to.
 */
public abstract class PluginParameterType<V extends ParameterValue> {

    private final String id;

    /**
     * Create a new PluginParameterType with the given ID
     *
     * @param id A String ID
     */
    protected PluginParameterType(final String id) {
        this.id = id;
    }

    /**
     * Get the ID corresponding to this parameter.
     *
     * @return The String ID of this parameter.
     */
    public final String getId() {
        return id;
    }

    /**
     * Initialize the value of a parameter.
     * <p>
     * By default, a PluginParameter has a value of null. Some
     * PluginParameterTypes (eg Boolean) really need to override this to provide
     * a sensible default value. Otherwise, plugins that expect to cast to a
     * boolean from a getObjectValue() will get a nasty surprise.
     *
     * @param parameter The parameter whose value will be initialized.
     */
    public void init(final PluginParameter<V> parameter) {
    }

    /**
     * Validate the String value of the parameter.
     *
     * @param param A parameter instance.
     * @param stringValue the value to validate.
     *
     * @return Null if the value is valid, an error message otherwise.
     */
    public String validateString(final PluginParameter<V> param, final String stringValue) {
        return null;
    }

    /**
     * Validate the Object value of the parameter.
     *
     * @param param A parameter instance.
     * @param objectValue the object value to validate.
     *
     * @return Null if the value is valid, an error message otherwise.
     */
    public String validateObject(final PluginParameter<V> param, final V objectValue) {
        return null;
    }

    /**
     * Copies the given value to the specified parameter.
     *
     * @param value The value to copy.
     * @param toParam the parameter to copy the value to.
     *
     * @return The copied value
     */
    public V copyValue(final V value, final PluginParameter<V> toParam) {
        return value;
    }

    /**
     * Does this type of parameter require a label when being displayed?
     *
     * @return True if this type of parameter requires a label, false otherwise.
     */
    public boolean requiresLabel() {
        return true;
    }
}
