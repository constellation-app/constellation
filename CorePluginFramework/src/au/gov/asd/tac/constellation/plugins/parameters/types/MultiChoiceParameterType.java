/*
 * Copyright 2010-2023 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.plugins.parameters.types.MultiChoiceParameterType.MultiChoiceParameterValue;
import au.gov.asd.tac.constellation.utilities.text.SeparatorConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;

/**
 * The MultiChoiceParameterType defines {@link PluginParameter} objects that
 * hold a (possibly empty) list of object values chosen from a finite collection
 * of options.
 *
 * @author twinkle2_little
 */
@ServiceProvider(service = PluginParameterType.class)
public class MultiChoiceParameterType extends PluginParameterType<MultiChoiceParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this
     * type.Single
     */
    public static final String ID = "multichoice";

    // For string representation.
    public static final String CHECK_MARK = "\u2713 ";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final MultiChoiceParameterType INSTANCE = new MultiChoiceParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type. The collection of
     * options will be {@link StringParameterValue} objects.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of MultiChoiceParameterType.
     */
    public static PluginParameter<MultiChoiceParameterValue> build(final String id) {
        return new PluginParameter<>(new MultiChoiceParameterValue(StringParameterValue.class), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type where the collection
     * of options are instances of the given {@link ParameterValue} class.
     *
     * @param id The String id of the parameter to construct.
     * @param innerClass The {@link ParameterValue} class to type the collection
     * of options for the constructed parameter.
     * @return A {@link PluginParameter} of MultiChoiceParameterType.
     */
    public static PluginParameter<MultiChoiceParameterValue> build(final String id, final Class<? extends ParameterValue> innerClass) {
        return new PluginParameter<>(new MultiChoiceParameterValue(innerClass), INSTANCE, id);
    }

    /**
     * Construct a new {@link PluginParameter} of this type with initial value
     * represented by the given {@link MultiChoiceParameterValue}.
     *
     * @param id The String id of the parameter to construct.
     * @param pv A {@link MultiChoiceParameterValue} describing the initial
     * value of the parameter being constructed.
     * @return A {@link PluginParameter} of MultiChoiceParameterType.
     */
    public static PluginParameter<MultiChoiceParameterValue> build(final String id, final MultiChoiceParameterValue pv) {
        return new PluginParameter<>(pv, INSTANCE, id);
    }

    /**
     * Get the collection of options for the given parameter as a list of
     * Strings.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return A List of Strings representing the collection of options for the
     * given parameter.
     */
    public static List<String> getOptions(final PluginParameter<MultiChoiceParameterValue> parameter) {
        return parameter.getMultiChoiceValue().getOptions();
    }

    /**
     * Get the collection of options for the given parameter as a list of
     * {@link ParameterValue}.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return A List of {@link ParameterValue} objects representing the
     * collection of options for the given parameter.
     */
    public static List<ParameterValue> getOptionsData(final PluginParameter<MultiChoiceParameterValue> parameter) {
        return parameter.getMultiChoiceValue().getOptionsData();
    }

    /**
     * Set the collection of options for the given parameter from a list of
     * Strings.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param options A List of Strings to set as the options for the given
     * parameter.
     */
    public static void setOptions(final PluginParameter<MultiChoiceParameterValue> parameter, final List<String> options) {
        final MultiChoiceParameterValue mc = parameter.getMultiChoiceValue() == null
                ? new MultiChoiceParameterValue() : new MultiChoiceParameterValue(parameter.getMultiChoiceValue());
        mc.setOptions(options);
        parameter.setObjectValue(mc);
    }

    /**
     * Set the collection of options for the given parameter from a list of
     * {@link ParameterValue} objects.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param options A List of {@link ParameterValue} objects to set as the
     * options for the given parameter.
     */
    public static void setOptionsData(final PluginParameter<MultiChoiceParameterValue> parameter, final List<? extends ParameterValue> options) {
        final MultiChoiceParameterValue mc = parameter.getMultiChoiceValue() == null
                ? new MultiChoiceParameterValue() : new MultiChoiceParameterValue(parameter.getMultiChoiceValue());
        mc.setOptionsData(options);
        parameter.setObjectValue(mc);
    }

    /**
     * Get the collection of selected values for the given parameter as a list
     * of Strings.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return A List of Strings representing the collection of selected values
     * for the given parameter.
     */
    public static List<String> getChoices(final PluginParameter<MultiChoiceParameterValue> parameter) {
        return parameter.getMultiChoiceValue().getChoices();
    }

    /**
     * Get the collection of selected values for the given parameter as a list
     * of {@link ParameterValue}.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @return A List of {@link ParameterValue} objects representing the
     * collection of selected values for the given parameter.
     */
    public static List<? extends ParameterValue> getChoicesData(final PluginParameter<MultiChoiceParameterValue> parameter) {
        return parameter.getMultiChoiceValue().getChoicesData();
    }

    /**
     * Set the list of selected values for the given parameter from a list of
     * Strings
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param choices A List of Strings objects to set as the selected values
     * for the given parameter.
     */
    public static void setChoices(final PluginParameter<MultiChoiceParameterValue> parameter, final List<String> choices) {
        final MultiChoiceParameterValue mc = parameter.getMultiChoiceValue() == null
                ? new MultiChoiceParameterValue() : new MultiChoiceParameterValue(parameter.getMultiChoiceValue());
        mc.setChoices(choices);
        parameter.setObjectValue(mc);
    }

    /**
     * Set the list of selected values for the given parameter from a list of
     * {@link ParameterValue} objects.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param choices A List of {@link ParameterValue} objects to set as the
     * selected values for the given parameter.
     */
    public static void setChoicesData(final PluginParameter<MultiChoiceParameterValue> parameter, final List<? extends ParameterValue> choices) {
        final MultiChoiceParameterValue mc = parameter.getMultiChoiceValue() == null
                ? new MultiChoiceParameterValue() : new MultiChoiceParameterValue(parameter.getMultiChoiceValue());
        mc.setChoicesData(choices);
        parameter.setObjectValue(mc);
    }

    /**
     * Set both the collection of options and list of selected values for the
     * given parameter from lists of Strings.
     *
     * @param parameter A {@link PluginParameter} of this type.
     * @param options A List of Strings to set as the options for the given
     * parameter.
     * @param choices A List of Strings objects to set as the selected values
     * for the given parameter.
     */
    public static void setState(final PluginParameter<MultiChoiceParameterValue> parameter, final List<String> options, final List<String> choices) {
        MultiChoiceParameterValue mc = parameter.getMultiChoiceValue() == null
                ? new MultiChoiceParameterValue() : new MultiChoiceParameterValue(parameter.getMultiChoiceValue());
        mc.setOptions(options);
        mc.setChoices(choices);
        parameter.setObjectValue(mc);
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
    public MultiChoiceParameterType() {
        super(ID);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds a collection of options and a list of values representing the
     * current selection from these options.
     */
    public static class MultiChoiceParameterValue extends ParameterValue {
        
        private static final Logger LOGGER = Logger.getLogger(MultiChoiceParameterValue.class.getName());

        // innerClass is the type of choice and the type of the elements of options.
        // If it's a nested class, make sure it's a static nested class rather than an inner class,
        // to avoid possible NoSuchMethodExceptions
        private final List<ParameterValue> options;
        private final List<ParameterValue> choices;
        private final Class<? extends ParameterValue> innerClass;

        /**
         * Constructs a new MultiChoiceParameterValue where the collection of
         * options are {@link StringParameterValue} objects.
         */
        public MultiChoiceParameterValue() {
            options = new ArrayList<>();
            choices = new ArrayList<>();
            innerClass = StringParameterValue.class;
        }

        /**
         * Constructs a new MultiChoiceParameterValue where the collection of
         * options are instances of the given {@link ParameterValue} class.
         *
         * @param innerClass The {@link ParameterValue} class to type the
         * collection of options.
         */
        public MultiChoiceParameterValue(final Class<? extends ParameterValue> innerClass) {
            options = new ArrayList<>();
            choices = new ArrayList<>();
            this.innerClass = innerClass;
        }

        /**
         * Constructs a new MultiChoiceParameterValue from an existing
         * MultiChoiceParametrValue.
         * <p>
         * Note that this is not a deep copy of the {@link ParameterValue}
         * objects forming the collection of options.
         *
         * @param mc The {@link MultiChoiceParameterValue} to copy.
         */
        public MultiChoiceParameterValue(final MultiChoiceParameterValue mc) {
            options = new ArrayList<>(mc.options);
            choices = new ArrayList<>(mc.choices);
            innerClass = mc.innerClass;
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
            final List<String> optionStrings = new ArrayList<>();
            options.stream().forEach(option -> optionStrings.add(option.toString()));

            return Collections.unmodifiableList(optionStrings);
        }

        /**
         * Set the collection of options from a list of Strings.
         *
         * @param options A list of Strings to set the collection of options
         * from.
         */
        public void setOptions(final List<String> options) {
            this.options.clear();
            for (final String option : options) {
                final StringParameterValue doOption = new StringParameterValue(option);
                this.options.add(doOption);
            }
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
            choices.clear();
        }

        /**
         * Get the currently selected values as a list of Strings.
         *
         * @return A list of Strings representing the currently selected values.
         */
        public List<String> getChoices() {
            final List<String> choiceStrings = new ArrayList<>();
            choices.stream().forEach(choice -> choiceStrings.add(choice.toString()));

            return Collections.unmodifiableList(choiceStrings);
        }

        /**
         * Set the currently selected values from a list of Strings.
         *
         * @param choices A list of Strings to set the selected values from.
         */
        public void setChoices(final List<String> choices) {
            this.choices.clear();
            for (final String choice : choices) {
                final StringParameterValue doCheck = new StringParameterValue(choice);
                if (options.contains(doCheck)) {
                    this.choices.add(doCheck);
                }
            }
        }

        /**
         * Get the currently selected values.
         *
         * @return A list of {@link ParameterValue} objects representing the
         * currently selected values.
         */
        public List<ParameterValue> getChoicesData() {
            return Collections.unmodifiableList(choices);
        }

        /**
         * Set the currently selected values from a list of
         * {@link ParameterValue} objects.
         *
         * @param choices A list of {@link ParameterValue} objects to set the
         * selected values from.
         */
        public void setChoicesData(final List<? extends ParameterValue> choices) {
            this.choices.clear();
            for (final ParameterValue choice : choices) {
                if (options.contains(choice)) {
                    this.choices.add(choice);
                }
            }
        }

        @Override
        public String validateString(final String s) {
            return null;
        }

        @Override
        public boolean setStringValue(final String s2) {
            options.clear();
            choices.clear();
            if (s2 != null) {
                final String[] c = s2.split(SeparatorConstants.NEWLINE);
                for (String s : c) {
                    try {
                        final ParameterValue dobj = innerClass.getDeclaredConstructor().newInstance();
                        if (s.startsWith(CHECK_MARK)) {
                            s = s.substring(CHECK_MARK.length());
                            dobj.setStringValue(s);
                            choices.add(dobj);
                        } else {
                            dobj.setStringValue(s);
                        }

                        options.add(dobj);
                    } catch (final IllegalAccessException | IllegalArgumentException
                            | InstantiationException | NoSuchMethodException
                            | SecurityException | InvocationTargetException ex) {
                        LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    }
                }
            }

            return true;
        }

        @Override
        public Object getObjectValue() {
            return this;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            final MultiChoiceParameterValue mc = (MultiChoiceParameterValue) o;

            options.clear();
            options.addAll(mc.options);

            choices.clear();
            choices.addAll(mc.choices);

            return true;
        }

        @Override
        protected MultiChoiceParameterValue createCopy() {
            return new MultiChoiceParameterValue(this);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            return this.getClass() == o.getClass() && Objects.equals(choices, ((MultiChoiceParameterValue) o).choices);
        }

        @Override
        public int hashCode() {
            return choices.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder();
            for (final ParameterValue choice : options) {
                if (b.length() > 0) {
                    b.append(SeparatorConstants.NEWLINE);
                }

                if (choices.contains(choice)) {
                    b.append(CHECK_MARK);
                }

                b.append(choice.toString());
            }

            return b.toString();
        }
    }
}
