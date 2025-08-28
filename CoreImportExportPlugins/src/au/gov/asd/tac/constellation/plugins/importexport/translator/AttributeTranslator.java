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

import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * An AttributeTranslator translates imported data fields into another format
 * before the data is passes to the graph. Often the data in a file is in a
 * format that is not accepted by the attribute it is intended for. Dates and
 * Datetimes are common examples because the attribute has only a few formats
 * that it accepts natively. In these cases the user is required to specify a
 * AttributeTranslator that can translate the imported date format into a format
 * the attribute understands.
 * <p>
 * New AttributeTranslators can be registered with the delimited importer by
 * extending this class and registering a {@link ServiceProvider} of this class.
 *
 * @author sirius
 */
public abstract class AttributeTranslator implements Comparable<AttributeTranslator> {

    private static List<AttributeTranslator> translators = null;

    private final String label;
    private final int priority;
    private final Set<String> attributeTypes;

    /**
     * Returns all the currently registered AttributeTranslators.
     *
     * @return all the currently registered AttributeTranslators.
     */
    public static List<AttributeTranslator> getTranslators() {
        if (translators == null) {
            translators = new ArrayList<>(Lookup.getDefault().lookupAll(AttributeTranslator.class));
            Collections.sort(translators);
        }

        return new ArrayList<>(translators);
    }

    /**
     * Returns the AttributeTranslator with the specified class name, or
     * DefaultAttributeTranslator if no such AttributeTranslator exists.
     *
     * @param name The name of a translator class.
     *
     * @return the AttributeTranslator with the specified class name, or
     * DefaultAttributeTranslator if no such AttributeTranslator exists.
     */
    public static AttributeTranslator getTranslator(final String name) {
        for (final AttributeTranslator t : getTranslators()) {
            if (t.getClass().getName().equals(name)) {
                return t;
            }
        }

        return new DefaultAttributeTranslator();
    }

    /**
     * Creates a new AttributeTranslator with the specified label, priority and
     * supported attribute types.
     *
     * @param label the label of the new AttributeTranslator.
     * @param priority the priority of the new AttributeTranslator.
     * @param attributeTypes the types of attribute that this
     * AttributeTranslator supports.
     */
    protected AttributeTranslator(final String label, final int priority, final String... attributeTypes) {
        this.label = label;
        this.priority = priority;

        this.attributeTypes = new HashSet<>();
        this.attributeTypes.addAll(Arrays.asList(attributeTypes));
    }

    /**
     * Creates a new AttributeTranslator with the specified label and priority.
     * This AttributeTranslator supports all attribute types.
     *
     * @param label the label of the new AttributeTranslator.
     * @param priority the priority of the new AttributeTranslator.
     */
    protected AttributeTranslator(final String label, final int priority) {
        this.label = label;
        this.priority = priority;
        attributeTypes = null;
    }

    /**
     * Returns the label of the AttributeTranslator. This is the identifier that
     * will be displayed in the UI.
     *
     * @return the label of the AttributeTranslator.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Returns the priority of the AttributeTranslator. When a group of
     * AttributeTranslators are displayed in a UI, they will be sorted by
     * increasing priority.
     *
     * @return the priority of the AttributeTranslator.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns true if this AttributeTranslation supports the specified
     * attribute type.
     *
     * @param attributeType the name of the attribute type.
     *
     * @return true if this AttributeTranslation supports the specified
     * attribute type.
     */
    public boolean appliesToAttributeType(final String attributeType) {
        return attributeTypes == null || attributeTypes.contains(attributeType);
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public int compareTo(final AttributeTranslator other) {
        if (priority > other.priority) {
            return 1;
        } else if (priority < other.priority) {
            return -1;
        } else {
            return label.compareTo(other.label);
        }
    }

    /**
     * Returns the PluginParameters that are required to configure this
     * AttributeTranslator. The default implementation returns null which
     * indicates that no parameters are required. Subclasses can override this
     * method to provide their own parameters.
     *
     * @return the PluginParameters that are required to configure this
     * AttributeTranslator.
     */
    public PluginParameters createParameters() {
        return null;
    }

    /**
     * Translates a given input string based on the specified
     * {@link PluginParameter}s.
     *
     * @param value the value to translate.
     * @param parameters the parameters used to configure the
     * AttributeTranslator. These will be the same parameters provided by the {@link AttributeTranslator#createParameters()
     * } method but with different values set.
     *
     * @return the translated version of the input string.
     */
    public abstract String translate(final String value, final PluginParameters parameters);

    /**
     * Gets the current values of this AttributeTranslator's parameters as a
     * String.
     * <p>
     * The String must be something that can be parsed back by
     * {@link #setParameterValues(PluginParameters, String) }. This is necessary
     * for serialising the values.
     *
     * @param parameters the parameters.
     * @return the current values of this AttributeTranslator's parameters as a
     * String.
     */
    public abstract String getParameterValues(final PluginParameters parameters);

    /**
     * Sets the current values of this AttributeTranslator's parameters as a
     * String.
     * <p>
     * This method must handle all values returned from
     * {@link #setParameterValues(PluginParameters, String)}. This is necessary
     * for deserialising the values.
     *
     * @param parameters the parameters.
     * @param values The new values of this AttributeTranslator's parameters as
     * a String.
     */
    public abstract void setParameterValues(final PluginParameters parameters, final String values);
}
