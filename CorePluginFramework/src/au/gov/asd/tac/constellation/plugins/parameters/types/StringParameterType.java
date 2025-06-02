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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * The StringParameterType defines {@link PluginParameter} objects that hold
 * String values.
 * <p>
 * If you need to handle passwords then you must use
 * {@link PasswordParameterType}.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class StringParameterType extends PluginParameterType<StringParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "string";

    /**
     * The property of this type referring to the number of lines required for a
     * GUI input for a parameter of this type.
     */
    public static final String LINES = "lines";

    /**
     * The property of this type referring to whether or not a parameter of this
     * type is a label (and hence any GUI inputs for it should not be editable).
     */
    public static final String IS_LABEL = "islabel";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final StringParameterType INSTANCE = new StringParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of StringParameterType.
     */
    public static PluginParameter<StringParameterValue> build(final String id) {
        return new PluginParameter<>(new StringParameterValue(), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link StringParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link StringParameterValue} describing the initial value of
     * the parameter being constructed.
     * @return A {@link PluginParameter} of StringParameterType.
     */
    public static PluginParameter<StringParameterValue> build(final String id, final StringParameterValue pv) {
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
    public StringParameterType() {
        super(ID);
    }

    /**
     * Gets the number of lines that should be shown in a GUI input for the
     * given parameter.
     *
     * @param parameter A {@link PluginParameter}. return The integer number of
     * lines that should be shown.
     *
     * @return the number of lines that should be shown in a GUI input for the
     * given parameter.
     */
    public static Integer getLines(final PluginParameter<?> parameter) {
        return (Integer) parameter.getProperty(LINES);
    }

    /**
     * Sets the number of lines that should be shown in a GUI input for the
     * given parameter.
     *
     * @param parameter A {@link PluginParameter}.
     * @param lines The integer number of lines that should be shown.
     */
    public static void setLines(final PluginParameter<?> parameter, final int lines) {
        parameter.setProperty(LINES, lines);
    }

    /**
     * Is the given parameter a label (and hence a GUI input for it should not
     * be editable)?
     *
     * @param parameter A {@link PluginParameter}.
     * @return True if the given parameter is a label, false otherwise.
     */
    public static boolean isLabel(final PluginParameter<?> parameter) {
        final Boolean isLabel = (Boolean) parameter.getProperty(IS_LABEL);

        return isLabel != null && isLabel;
    }

    /**
     * Set whether the given parameter is a label (and hence whether a GUI input
     * for it should not be editable).
     *
     * @param parameter A {@link PluginParameter}.
     * @param isLabel Whether or not the given parameter is a label.
     */
    public static void setIsLabel(final PluginParameter<?> parameter, final boolean isLabel) {
        parameter.setProperty(IS_LABEL, isLabel);
    }

    /**
     *
     * @param param
     * @param stringValue
     * @return null when the string is valid, will return "parameter is Empty!"
     * when invalid
     */
    @Override
    public String validateString(final PluginParameter<StringParameterValue> param, final String stringValue) {
        if (StringUtils.isBlank(stringValue)) {
            return "Parameter is Empty!";
        }
        return null;
    }
}
