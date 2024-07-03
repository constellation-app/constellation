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
package au.gov.asd.tac.constellation.plugins.parameters.types;

import au.gov.asd.tac.constellation.plugins.parameters.ParameterChange;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameter;
import au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType;
import au.gov.asd.tac.constellation.plugins.parameters.types.SingleChoiceParameterType.SingleChoiceParameterValue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.openide.util.lookup.ServiceProvider;

/**
 * The ChoiceParameterType defines {@link PluginParameter} objects that hold a
 * single object value chosen from a finite collection of options.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class SingleChoiceParameterType extends PluginParameterType<SingleChoiceParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "choice";

    /**
     * The property of this type referring to the collection of options.
     */
    public static final String CHOICES = "choices";
    /**
     * The property of this type referring to whether object values can edited
     * as strings by the user (rather than chosen from the collection of
     * options).
     */
    public static final String EDITABLE = "editable";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final SingleChoiceParameterType INSTANCE = new SingleChoiceParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type. The collection of
     * options will be {@link StringParameterValue} objects.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of ChoiceParameterType.
     */
    public static PluginParameter<SingleChoiceParameterValue> build(final String id) {
        return new PluginParameter<>(new SingleChoiceParameterValue(StringParameterValue.class), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type where the collection
     * of options are instances of the given {@link ParameterValue} class.
     *
     * @param id The String id of the parameter to construct.
     * @param innerClass The {@link ParameterValue} class to type the collection
     * of options for the constructed parameter.
     * @return A {@link PluginParameter} of ChoiceParameterType.
     */
    public static PluginParameter<SingleChoiceParameterValue> build(final String id, final Class<? extends ParameterValue> innerClass) {
        return new PluginParameter<>(new SingleChoiceParameterValue(innerClass), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link SingleChoiceParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link SingleChoiceParameterValue} describing the initial
     * value of the parameter being constructed.
     * @return A {@link PluginParameter} of ChoiceParameterType.
     */
    public static PluginParameter<SingleChoiceParameterValue> build(final String id, final SingleChoiceParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * Get the collection of options for the given parameter as a list of
     * Strings.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return A list of Strings representing the collection of options for the
     * given parameter.
     */
    public static List<String> getOptions(final PluginParameter<SingleChoiceParameterValue> parameter) {
        return parameter.getParameterValue().getOptions();
    }

    /**
     * Get the collection of options for the given parameter as a list of
     * {@link ParameterValue} objects.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return A list of {@link ParameterValue} objects representing the
     * collection of options for the given parameter.
     */
    public static List<ParameterValue> getOptionsData(final PluginParameter<SingleChoiceParameterValue> parameter) {
        return parameter.getParameterValue().getOptionsData();
    }

    /**
     * Set the collection of options for the given parameter from a list of
     * strings.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param options A list of Strings to set as the options for the given
     * parameter.
     */
    public static void setOptions(final PluginParameter<SingleChoiceParameterValue> parameter, final List<String> options) {
        //Change only if the options are changed.
        if (optionsChanged(parameter, options)) {
            final SingleChoiceParameterValue parameterValue = parameter.getParameterValue();

            //Clear the existing selection
            parameter.setObjectValue(null);

            parameterValue.setOptions(options);
            parameter.setProperty(CHOICES, new Object());
        }
    }

    /**
     * Check whether the available options list is changed
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param options A list of Strings to set as the options for the given
     * parameter.
     */
    private static boolean optionsChanged(final PluginParameter<SingleChoiceParameterValue> parameter, final List<String> options) {
        final SingleChoiceParameterValue parameterValue = parameter.getParameterValue();
        return !options.equals(parameterValue.getOptions());
    }

    /**
     * Set the collection of options for the given parameter from a list of
     * {@link ParameterValue} objects.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param options A list of {@link ParameterValue} objects to set as the
     * options for the given parameter.
     */
    public static void setOptionsData(final PluginParameter<SingleChoiceParameterValue> parameter, final List<? extends ParameterValue> options) {
        final SingleChoiceParameterValue parameterValue = parameter.getParameterValue();

        //Clear the existing selection
        parameter.setObjectValue(null);

        parameterValue.setOptionsData(options);
        parameter.setProperty(CHOICES, new Object());
    }

    /**
     * Get the current selected value for this parameter as a String.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return The {@link String} representation of the current selection for
     * the given parameter.
     */
    public static String getChoice(final PluginParameter<SingleChoiceParameterValue> parameter) {
        return parameter.getParameterValue().getChoice();
    }

    /**
     * Get the current selected value for this parameter.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return A {@link ParameterValue} object representing the current
     * selection for the given parameter.
     */
    public static ParameterValue getChoiceData(final PluginParameter<SingleChoiceParameterValue> parameter) {
        return parameter.getParameterValue().getChoiceData();
    }

    /**
     * Set the current selected choice from a string.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param choice A {@link String} object to set as the chosen value for the
     * given parameter.
     */
    public static void setChoice(final PluginParameter<SingleChoiceParameterValue> parameter, final String choice) {
        final SingleChoiceParameterValue parameterValue = parameter.getParameterValue();
        if (parameterValue.setChoice(choice)) {
            parameter.fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Set the current selected choice for the given parameter to the given
     * {@link ParameterValue} object.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param choice A {@link ParameterValue} object to set as the chosen value
     * for the given parameter.
     */
    public static void setChoiceData(final PluginParameter<SingleChoiceParameterValue> parameter, final ParameterValue choice) {
        final SingleChoiceParameterValue parameterValue = parameter.getParameterValue();
        if (parameterValue.setChoiceData(choice)) {
            parameter.fireChangeEvent(ParameterChange.VALUE);
        }
    }

    /**
     * Is the given parameter's choice editable by the user?
     *
     * @param parameter A {@link PluginParameter}.
     * @return True if the parameter's choice can be edited by the user, False
     * otherwise.
     */
    public static boolean isEditable(final PluginParameter<?> parameter) {
        final Boolean isEditable = (Boolean) parameter.getProperty(EDITABLE);

        return isEditable != null && isEditable;
    }

    /**
     * Set whether or not the given parameter's choice is editable by the user.
     *
     * @param parameter A {@link PluginParameter}.
     * @param editable Whether or not the parameter's choice can be edited by
     * the user.
     */
    public static void setEditable(final PluginParameter<?> parameter, boolean editable) {
        parameter.setProperty(EDITABLE, editable);
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
    public SingleChoiceParameterType() {
        super(ID);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds a collection of options and a current choice.
     */
    public static class SingleChoiceParameterValue extends ParameterValue {
        
        private static final Logger LOGGER = Logger.getLogger(SingleChoiceParameterValue.class.getName());

        // innerClass is the type of choice and the type of the elements of options.
        // If it's a nested class, make sure it's a static nested class rather than an inner class,
        // to avoid possible NoSuchMethodExceptions
        private final List<ParameterValue> options;
        private ParameterValue choice;
        private final Class<? extends ParameterValue> innerClass;

        /**
         * Constructs a new SingleChoiceParameterValue where the collection of
         * options are {@link StringParameterValue} objects.
         */
        public SingleChoiceParameterValue() {
            options = new ArrayList<>();
            choice = null;
            innerClass = StringParameterValue.class;
        }

        /**
         * Constructs a new SingleChoiceParameterValue where the collection of
         * options are instances of the given {@link ParameterValue} class.
         *
         * @param innerClass The {@link ParameterValue} class to type the
         * collection of options.
         */
        public SingleChoiceParameterValue(final Class<? extends ParameterValue> innerClass) {
            options = new ArrayList<>();
            choice = null;
            this.innerClass = innerClass;
        }

        /**
         * Constructs a new SingleChoiceParameterValue from an existing
         * SingleChoiceParametrValue.
         * <p>
         * Note that this is not a deep copy of the {@link ParameterValue}
         * objects forming the collection of options.
         *
         * @param sc The {@link SingleChoiceParameterValue} to copy.
         */
        public SingleChoiceParameterValue(final SingleChoiceParameterValue sc) {
            options = new ArrayList<>();
            options.addAll(sc.options);
            choice = sc.choice != null ? sc.choice.copy() : null;
            innerClass = sc.innerClass;
        }

        /**
         * Get the type of {@link ParameterValue} objects backing the collection
         * of options.
         *
         * @return A class object which extends {@link ParameterValue}.
         */
        public Class<? extends ParameterValue> getInnerClass() {
            return innerClass;
        }

        /**
         * Get the collection of options as a list of Strings.
         *
         * @return A list of Strings representing the options.
         */
        public List<String> getOptions() {            
            final List<String> optionStrings = options.stream().map(Object::toString).toList();

            return Collections.unmodifiableList(optionStrings);
        }

        /**
         * Set the collection of options from a list of Strings.
         *
         * @param options A list of Strings to set the collection of options
         * from.
         */
        public void setOptions(final Iterable<String> options) {
            this.options.clear();
            for (final String option : options) {
                final StringParameterValue doOption = new StringParameterValue(option);
                this.options.add(doOption);
            }
            choice = null;
        }

        /**
         * Get the collection of options as a list of {@link ParameterValue}.
         *
         * @return A list of {@link ParameterValue} objects representing the
         * options.
         */
        public List<ParameterValue> getOptionsData() {
            return Collections.unmodifiableList(options);
        }

        /**
         * Set the collection of options from a list of {@link ParameterValue}
         * objects.
         *
         * @param options A list of {@link ParameterValue} objects to form the
         * collection of options.
         */
        public void setOptionsData(final List<? extends ParameterValue> options) {
            this.options.clear();
            this.options.addAll(options);
            choice = null;
        }

        /**
         * Get the currently selected value as a String.
         *
         * @return A String representing the currently selected value.
         */
        public String getChoice() {
            return choice != null ? choice.toString() : null;
        }

        /**
         * Set the currently selected value as a String.
         *
         * @param choice A String to set the selected value from.
         *
         * @return true if the choice was set, false otherwise.
         */
        public boolean setChoice(final String choice) {
            final StringParameterValue doCheck = new StringParameterValue(choice);
            if (options.contains(doCheck)) {
                this.choice = doCheck;
                return true;
            } else {
                //clear the choice
                this.choice = null;
                return false;
            }
        }

        /**
         * Get the currently selected value.
         *
         * @return A {@link ParameterValue} object representing the currently
         * selected value.
         */
        public ParameterValue getChoiceData() {
            return choice;
        }

        /**
         * Set the currently selected value from a {@link ParameterValue}
         * objects.
         *
         * @param choice A {@link ParameterValue} object to set the selected
         * value from.
         *
         * @return true if the choice was set, false otherwise.
         */
        public boolean setChoiceData(final ParameterValue choice) {
            if (options.contains(choice)) {
                this.choice = choice;
                return true;
            } else {
                //clear the choice
                this.choice = null;
                return false;
            }
        }

        @Override
        public String validateString(final String s) {
            // Pass the validation to the inner DataObject.
            try {
                final ParameterValue validator = innerClass.getDeclaredConstructor().newInstance();
                return validator.validateString(s);
            } catch (final IllegalAccessException | IllegalArgumentException
                    | InstantiationException | NoSuchMethodException
                    | SecurityException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                return ex.getMessage();
            }
        }

        @Override
        public boolean setStringValue(final String s) {
            try {
                if (choice == null) {
                    if (s != null) {
                        final ParameterValue newChoice = innerClass.getDeclaredConstructor().newInstance();
                        newChoice.setStringValue(s);
                        choice = newChoice;
                        return true;
                    }
                } else {
                    final ParameterValue newChoice = innerClass.getDeclaredConstructor().newInstance();
                    newChoice.setStringValue(s);
                    if (!choice.equals(newChoice)) {
                        choice = newChoice;
                        return true;
                    }
                }
            } catch (final IllegalAccessException | IllegalArgumentException
                    | InstantiationException | NoSuchMethodException
                    | SecurityException | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            return false;
        }

        /**
         * getObjectValue will return the whole SingleChoiceParameterType
         * similar to how the MultiChoiceParameterType does also.
         *
         * @return this whole object.
         */
        @Override
        public Object getObjectValue() {
            return this;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            boolean valueChanged = false;
            if (o == null) {
                options.clear();
                choice = null;
                valueChanged = true;
            } else if (o instanceof SingleChoiceParameterValue singleChoiceParameterValue) {
                if (!Objects.equals(options, singleChoiceParameterValue.options)) {
                    options.clear();
                    options.addAll(singleChoiceParameterValue.options);
                    valueChanged = true;
                }
                if (!Objects.equals(choice, singleChoiceParameterValue.choice)) {
                    choice = singleChoiceParameterValue.choice;
                    valueChanged = true;
                }
            } else if (o instanceof ParameterValue) {
                setChoiceData(this.innerClass.cast(o));
                valueChanged = true;
            } else {
                throw new IllegalArgumentException("Invalid argument");
            }

            return valueChanged;
        }

        @Override
        protected SingleChoiceParameterValue createCopy() {
            return new SingleChoiceParameterValue(this);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final SingleChoiceParameterValue other = (SingleChoiceParameterValue) obj;
            return Objects.equals(this.choice, other.choice);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(choice);
        }

        @Override
        public String toString() {
            return choice != null ? choice.toString() : null;
        }
    }
}
