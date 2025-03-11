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
package au.gov.asd.tac.constellation.plugins.parameters;

import au.gov.asd.tac.constellation.plugins.parameters.types.BooleanParameterType.BooleanParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ColorParameterType.ColorParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRange;
import au.gov.asd.tac.constellation.plugins.parameters.types.DateTimeRangeParameterType.DateTimeRangeParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.FloatParameterType.FloatParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.IntegerParameterType.IntegerParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.LocalDateParameterType.LocalDateParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.NumberParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterList;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType.ParameterListParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.ParameterValue;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * A PluginParameter is the object that holds the current state of a single parameter for a plugin in Constellation.
 * <p>
 * There are two main components of a PluginParameter. The first is a {@link ParameterValue} object which holds the
 * current value of the parameter and allows setting and getting of this value in a consistent manner. The second is the
 * {@link PluginParameterType} which acts as a controller that interfaces between the parameter and the parameter value.
 * <p>
 * The type and value classes are in a one-to-one correspondence, and the usual pattern for implementing a new type of
 * PluginParameter is to implement a {@link PluginParameterType} and then the corresponding {@link ParameterValue} as an
 * inner class within the type. This class should typically not be extended as its functionality is limited to that
 * which is universal to all parameters.
 *
 * @author sirius
 * @param <V> The type of {@link ParameterValue} which this PluginParameter stores.
 */
public class PluginParameter<V extends ParameterValue> {

    private final String id;
    private final PluginParameterType<V> type;

    private String name;
    private String description;
    private String icon;
    private String error = null;
    private boolean visible = true;
    private boolean enabled = true;
    private String helpID;
    private boolean isSuppressed = false;
    private String requestBodyExample;
    private boolean required = false;

    private final List<ParameterChange> suppressedEvents = new ArrayList<>();

    private static final List<ParameterChange> ALL_PARAMETER_EVENTS = new ArrayList<>(Arrays.asList(ParameterChange.values()));

    private Map<String, Object> properties = new HashMap<>();

    private final V value;

    private final List<PluginParameterListener> listeners = new ArrayList<>();

    // Used for parameters which belong to other parameters, eg. parameters that are lists of other parameters.
    private PluginParameter<?> enclosingParameter = null;

    /**
     * Create a new PluginParameter with the given value, type and id
     *
     * @param value The initial value of the parameter.
     * @param type The {@link PluginParameterType} of this parameter.
     * @param id The String id of the parameter. This is the key used to retrieve the value of the parameter from
     * {@link PluginParameters#getParameters()}.
     */
    public PluginParameter(final V value, final PluginParameterType<V> type, final String id) {
        this.type = type;
        this.id = id;
        this.name = id;
        this.helpID = null;
        this.value = value;

        // Allow the parameter type to initialise the parameter
        type.init(this);
    }

    /**
     * Get the name for this parameter. This is the name of the parameter from the user's perspective and is typically
     * shown next to the parameter's input widget when displayed by a GUI.
     *
     * @return The String name of this parameter.
     */
    public final String getName() {
        return name;
    }

    /**
     * Set the name for this parameter. This is the name of the parameter from the user's perspective and is typically
     * shown next to the parameter's input widget when displayed by a GUI.
     * <p>
     * This will fire a {@link ParameterChange} event.
     *
     * @param name The String name to set.
     */
    public final void setName(final String name) {
        if (!Objects.equals(name, this.name)) {
            this.name = StringUtils.defaultString(name);
            fireChangeEvent(ParameterChange.NAME);
        }
    }

    /**
     * Is the parameter visible?
     *
     * @return True if the parameter is visible, false otherwise.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set the visibility of the parameter.
     * <p>
     * This will fire a {@link ParameterChange} event.
     *
     * @param visible A boolean indicating the visibility of the parameter.
     */
    public void setVisible(final boolean visible) {
        if (this.visible != visible) {

            this.visible = visible;
            fireChangeEvent(ParameterChange.VISIBLE);
        }
    }

    /**
     * suppressEvent allows a call to the parameter to stop firing a {@link ParameterChange} event.
     * <p>
     * Specify {@link ParameterChange} event enumerated types as well as a boolean to engage or disengage them. NOTE:
     * passing suppress as false will enable all events.
     *
     * @param suppress if true, the listed events will not fire. if no events are listed, all events will not fire. If
     * false, all events are enabled.
     * @param eventsToSuppress the events to suppress. pass an empty {@link List} when specifying all events or enabling
     * events. Pass {@link ParameterChange} enumerated types when specifying certain events to suppress.
     */
    public void suppressEvent(final boolean suppress, final List<ParameterChange> eventsToSuppress) {
        if (suppress) {
            suppressedEvents.addAll(eventsToSuppress.isEmpty() ? ALL_PARAMETER_EVENTS : eventsToSuppress);
        } else {
            suppressedEvents.clear();
        }
        isSuppressed = suppress;
    }

    /**
     * Checks whether the event passed is enabled or disabled
     *
     * @param event the {@link ParameterChange} event to check
     * @return true if the event is suppressed, false otherwise
     */
    public boolean eventIsSuppressed(final ParameterChange event) {
        return suppressedEvents.contains(event) && isSuppressed;
    }

    /**
     * Is this parameter enabled? Disabled parameters are typically displayed to the user but not editable.
     *
     * @return True if this parameter is currently enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether or not this parameter is enabled. Disabled parameters are typically displayed to the user but not
     * editable. This will fire a
     * <p>
     * {@link ParameterChange} event.
     *
     * @param enabled A boolean indicating whether or not this parameter should be enabled.
     */
    public void setEnabled(final boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            fireChangeEvent(ParameterChange.ENABLED);
        }
    }

    /**
     * Get the named property of this parameter.
     *
     * @param key The String name of the property to retrieve.
     * @return The current value of the desired property for this parameter.
     */
    public Object getProperty(final String key) {
        return properties.get(key);
    }

    /**
     * Set the named property of this parameter to the specified value.
     * <p>
     * This will fire a {@link ParameterChange} event.
     *
     * @param key The String name of the property to set.
     * @param value the value of the property to set.
     */
    public void setProperty(final String key, final Object value) {
        final Object currentObject = properties.get(key);
        if (value == null) {
            if (currentObject != null) {
                properties.remove(key);
                fireChangeEvent(ParameterChange.PROPERTY);
            }
        } else {
            if (!value.equals(currentObject)) {
                properties.put(key, value);
                fireChangeEvent(ParameterChange.PROPERTY);
            }
        }
    }

    /**
     * Get the {@link PluginParameterType} of this parameter used to control the parameter and interface between the
     * parameter and its value.
     *
     * @return The {@link PluginParameterType} for this parameter.
     */
    public final PluginParameterType<?> getType() {
        return type;
    }

    /**
     * Get the ID of this parameter. The ID is the key used to retrieve the value of the parameter from
     * {@link PluginParameters#getParameters()}.
     *
     * @return The String ID of this parameter.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the description for this parameter. This is the description for the parameter presented to the user and is
     * typically shown next to the parameter's input widget when displayed by a GUI.
     *
     * @return The String description for this parameter.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description for this parameter. This is the description for the parameter presented to the user and is
     * typically shown next to the parameter's input widget when displayed by a GUI.
     * <p>
     * This will fire a {@link ParameterChange} event.
     *
     * @param description The String description to set for this parameter.
     */
    public void setDescription(final String description) {
        if (!Objects.equals(description, this.description)) {
            this.description = description;
            fireChangeEvent(ParameterChange.DESCRIPTION);
        }
    }

    /**
     * Get the icon for this parameter. This is typically shown next to the parameter's input widget when displayed by a
     * GUI.
     *
     * @return The String name of an icon in CONSTELLATION for this parameter.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Set the icon for this parameter. This is typically shown next to the parameter's input widget when displayed by a
     * GUI.
     *
     * @param icon The String name of an icon in CONSTELLATION to set for this parameter.
     */
    public void setIcon(final String icon) {
        if (!Objects.equals(icon, this.icon)) {
            this.icon = icon;
            fireChangeEvent(ParameterChange.ICON);
        }
    }

    /**
     * This parameter's helpID.
     *
     * @return This parameter's helpID.
     */
    public String getHelpID() {
        return helpID;
    }

    /**
     * Set this parameter's helpID.
     * <p>
     * If set, the GUI representation of this parameter can display a help indicator, which when selected will use the
     * helpID to display help.
     *
     * @param helpID the help id for the parameter.
     */
    public void setHelpID(final String helpID) {
        this.helpID = helpID;
    }

    /**
     * Request that this parameter load recent values. Some parameter's input widgets will display a list of recent
     * valued next to them.
     *
     * @return True if recent values were found, false otherwise.
     */
    public boolean loadToRecentValue() {
        final List<String> recentValues = RecentParameterValues.getRecentValues(id);
        if (CollectionUtils.isNotEmpty(recentValues)) {
            setStringValue(recentValues.get(0));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Request that this parameter store recent values.
     */
    public void storeRecentValue() {
        RecentParameterValues.storeRecentValue(id, getStringValue());
    }

    /**
     * Get the current error message for this parameter. This will be present if the current value of the parameter is
     * invalid.
     *
     * @return A String of the current error message.
     */
    public final String getError() {
        return error;
    }

    /**
     * Set the current error message for this parameter. This should set when the current value of the parameter becomes
     * invalid.
     *
     * @param error A String containing the error message to be set.
     */
    public final void setError(final String error) {
        if (!Objects.equals(error, this.error)) {
            boolean errorChange = true;
            if (error != null && this.error != null) {
                errorChange = false;
            }
            this.error = error;
            if (errorChange) {
                fireChangeEvent(ParameterChange.ERROR);
            }
        }
    }

    /**
     * Add a listener to this parameter that will listen for {@link ParameterChange} events.
     *
     * @param listener The {@link PluginParameterListener} to add.
     */
    public void addListener(final PluginParameterListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener from this parameter.
     *
     * @param listener The {@link PluginParameterListener} to remove.
     */
    public void removeListener(final PluginParameterListener listener) {
        listeners.remove(listener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    /**
     * Create a new instance of PluginParameter.
     * <p>
     * Override to create an instance of a subclass.
     *
     * @param value the initial value for the parameter.
     * @param type the type of the parameter.
     * @param id the id for the parameter.
     *
     * @return A new instance of PluginParameter.
     */
    protected PluginParameter<V> create(final ParameterValue value, final PluginParameterType<V> type, final String id) {
        final PluginParameter<V> p = new PluginParameter(value.copy(), type, id);
        if (p.value instanceof ParameterListParameterValue parameterListParameterValue) {
            parameterListParameterValue.setEnclosingParameter(p);
        }
        return p;
    }

    /**
     * Clone this plugin parameter. This will be a deep copy in that the current value will also be copied.
     *
     * @return a copy of this parameter.
     */
    @SuppressWarnings("unchecked") // Below cast will always work.
    public final PluginParameter<V> copy() {
        final PluginParameter<V> copy = (PluginParameter<V>) create(value, type, id);
        copy.setName(name);
        copy.setDescription(description);
        copy.setIcon(icon);
        copy.setHelpID(helpID);
        copy.setEnabled(enabled);
        copy.setVisible(visible);
        copy.setError(error);
        copy.setRequired(required);
        copy.enclosingParameter = enclosingParameter;
        copy.properties = new HashMap<>(properties);
        return copy;
    }

    /**
     * Set the current value of this parameter as an object. This will delegate the setting operation to this
     * parameter's {@link ParameterValue} object. Note that this delegated operation is deprecated.
     *
     * @param objectValue The object value to set.
     */
    public final void setObjectValue(final Object objectValue) {
        if (value.setObjectValue(objectValue)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Fire a {@link ParameterChange} event without changing the object value.
     */
    public void fireNoChange() {
        fireChangeEvent(ParameterChange.NO_CHANGE);
    }

    /**
     * Get the current value of this parameter as an object. This will delegate the getting operation to this
     * parameter's {@link ParameterValue} object. Note that this delegated operation is deprecated.
     *
     * @return An object representing the current value of this parameter.
     */
    public Object getObjectValue() {
        return value.getObjectValue();
    }

    /**
     * Get the {@link ParameterValue} object that describes this parameter's current value.
     *
     * @return This parameter's {@link ParameterValue} object.
     */
    public V getParameterValue() {
        return value;
    }

    /**
     * Set the current value of this parameter as a String. This will delegate the setting operation to this parameter's
     * {@link ParameterValue} object.
     *
     * @param stringValue The String value to set.
     */
    public final void setStringValue(final String stringValue) {
        setError(value.validateString(stringValue));
        if (getError() == null && value.setStringValue(stringValue)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the current value of this parameter as a String. This will delegate the getting operation to this parameter's
     * {@link ParameterValue} object.
     *
     * @return A String representing the current value of this parameter.
     */
    public final String getStringValue() {
        return value.toString();
    }

    /**
     * Validate the specified string value as a value for this parameter.
     *
     * @param stringValue The string value to validate for this parameter.
     * @return A String containing the error message if the supplied value was invalid for this parameter. Otherwise,
     * null.
     */
    public final String validateString(final String stringValue) {
        return value.validateString(stringValue);
    }

    /**
     * Set an enclosing parameter for which this is a subparameter. This is only used in special cases such as
     * {@link au.gov.asd.tac.constellation.plugins.parameters.types.ParameterListParameterType} where a parameter can
     * contain several other parameters.
     *
     * @param enclosingParameter the enclosing parameter.
     */
    public void setEnclosingParameter(final PluginParameter<?> enclosingParameter) {
        this.enclosingParameter = enclosingParameter;
    }

    /**
     * Fire the specified {@link ParameterChange} event by notifying all of this parameter's listeners about it. No
     * event is fired when the {@link ParameterChange} NO_EVENT is passed
     *
     * @param change The {@link ParameterChange} event to fire. NO_EVENT will not fire an event
     */
    public void fireChangeEvent(final ParameterChange change) {
        if (!eventIsSuppressed(change)) {
            listeners.stream().forEach(listener -> listener.parameterChanged(this, change));
            if (enclosingParameter != null && !change.equals(ParameterChange.ERROR)) {
                enclosingParameter.fireChangeEvent(change);
            }
        }
    }

    /**
     * Get the value of this parameter as a boolean.
     *
     * @return The boolean value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link BooleanParameterValue}.
     */
    public boolean getBooleanValue() {
        return ((BooleanParameterValue) value).getValue();
    }

    /**
     * Set the value of this parameter as a boolean.
     *
     * @param b The boolean value to set.
     * @throws ClassCastException if this parameter does not hold a {@link BooleanParameterValue}.
     */
    public void setBooleanValue(final boolean b) {
        if (((BooleanParameterValue) value).set(b)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the value of this parameter as a Color.
     *
     * @return The Color value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link ColorParameterValue}.
     */
    public ConstellationColor getColorValue() {
        return ((ColorParameterValue) value).get();
    }

    /**
     * Set the value of this parameter as a Color.
     *
     * @param c The Color value to set.
     * @throws ClassCastException if this parameter does not hold a {@link ColorParameterValue}.
     */
    public void setColorValue(final ConstellationColor c) {
        if (((ColorParameterValue) value).set(c)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the value of this parameter as a DateTimeRange.
     *
     * @return The DateTimeRange value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link DateTimeRangeParameterValue}.
     */
    public DateTimeRange getDateTimeRangeValue() {
        return ((DateTimeRangeParameterValue) value).get();
    }

    /**
     * Set the value of this parameter as a DateTimeRange.
     *
     * @param dtr The DateTimeRange value to set.
     * @throws ClassCastException if this parameter does not hold a {@link DateTimeRangeParameterValue}.
     */
    public void setDateTimeRangeValue(final DateTimeRange dtr) {
        if (((DateTimeRangeParameterValue) value).set(dtr)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the value of this parameter as an integer.
     *
     * @return The integer value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link IntegerParameterValue}.
     */
    public int getIntegerValue() {
        return ((IntegerParameterValue) value).get();
    }

    /**
     * Set the value of this parameter as an integer.
     *
     * @param i The integer value to set.
     * @throws ClassCastException if this parameter does not hold a {@link IntegerParameterValue}.
     */
    public void setIntegerValue(final int i) {
        if (((IntegerParameterValue) value).set(i)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the value of this parameter as a float.
     *
     * @return The float value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link FloatParameterValue}.
     */
    public float getFloatValue() {
        return ((FloatParameterValue) value).get();
    }

    /**
     * Set the value of this parameter as a float.
     *
     * @param f The float value to set.
     * @throws ClassCastException if this parameter does not hold a {@link FloatParameterValue}.
     */
    public void setFloatValue(final float f) {
        if (((FloatParameterValue) value).set(f)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the value of this parameter as a LocalDate.
     *
     * @return The LocalDate value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link LocalDateParameterValue}.
     */
    public LocalDate getLocalDateValue() {
        return ((LocalDateParameterValue) value).get();
    }

    /**
     * Get the value of this parameter as a MultiChoiceParameterValue.
     *
     * @return The MultiChoiceParameterValue value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link MultiChoiceParameterValue}.
     */
    public MultiChoiceParameterValue getMultiChoiceValue() {
        return ((MultiChoiceParameterValue) value);
    }

    /**
     * Get the value of this parameter as a ParameterListParameterValue.
     *
     * @return The ParameterListParameterValue value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link ParameterListParameterValue}.
     */
    public ParameterListParameterValue getParameterListValue() {
        return ((ParameterListParameterValue) value);
    }

    /**
     * Set the value of this parameter as a ParameterList.
     *
     * @param paramListValue The ParameterList value to set.
     * @throws ClassCastException if this parameter does not hold a {@link ParameterListParameterValue}.
     */
    public final void setParameterListValue(final ParameterList paramListValue) {
        if (((ParameterListParameterValue) value).set(paramListValue)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the value of this parameter as a SingleChoiceParameterValue.
     *
     * @return The SingleChoiceParameterValue value of this parameter (as a ParameterValue).
     * @throws ClassCastException if this parameter does not hold a {@link SingleChoiceParameterValue}.
     */
    public ParameterValue getSingleChoice() {
        return ((SingleChoiceParameterValue) value).getChoiceData();
    }

    /**
     * Set the value of this parameter as a LocalDate.
     *
     * @param ld The LocalDate value to set.
     * @throws ClassCastException if this parameter does not hold a {@link LocalDateParameterValue}.
     */
    public void setLocalDateValue(final LocalDate ld) {
        if (((LocalDateParameterValue) value).set(ld)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Get the value of this parameter as a Number.
     *
     * @return The Number value of this parameter.
     * @throws ClassCastException if this parameter does not hold a {@link NumberParameterValue}.
     */
    public Number getNumberValue() {
        return ((NumberParameterValue) value).getNumberValue();
    }

    /**
     * Set the value of this parameter as a Number.
     *
     * @param n The Number value to set.
     * @throws ClassCastException if this parameter does not hold a {@link NumberParameterValue}.
     */
    public void setNumberValue(final Number n) {
        if (((NumberParameterValue) value).setNumberValue(n)) {
            fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Build the parameter id of the plugin parameter in a consistent way
     *
     * @param pluginClass The plugin class
     * @param parameter The parameter name
     *
     * @return A unique parameter id
     */
    public static String buildId(final Class<?> pluginClass, final String parameter) {
        return pluginClass.getSimpleName() + SeparatorConstants.PERIOD + parameter;
    }

    /**
     * Get the swagger Request Body Example value.
     *
     */
    public final String getRequestBodyExampleJson() {
        return requestBodyExample;
    }

    /**
     * Set the swagger Request Body Example value.
     *
     * @param requestBodyExample The Request Body Example in Json format.
     */
    public final void setRequestBodyExampleJson(final String requestBodyExample) {
        if (!Objects.equals(requestBodyExample, this.requestBodyExample)) {
            this.requestBodyExample = requestBodyExample;
        }
    }

    /**
     * Is the parameter required?
     *
     * @return True if the parameter is required, false otherwise.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Set whether the parameter is required.
     *
     * @param required A boolean indicating whether the parameter is required.
     */
    public void setRequired(final boolean required) {
        this.required = required;
    }
}
