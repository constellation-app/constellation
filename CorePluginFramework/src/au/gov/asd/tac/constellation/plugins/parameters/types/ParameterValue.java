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
import javafx.scene.layout.Region;

/**
 * ParameterValue objects hold the current values for {@link PluginParameter}
 * objects. They are responsible for getting, setting and validating values of
 * different types in a consistent manner (typically as strings).
 * <p>
 * They are in one-to-one correspondence with
 * {@link au.gov.asd.tac.constellation.plugins.parameters.PluginParameterType}
 * objects and are often implemented as static inner classes within them. All
 * subclasses must have a zero-argument public constructor. Implementors must
 * also implement equals(), hashCode(), toString().
 *
 * @author algol
 */
public abstract class ParameterValue {

    /**
     * A GuiInit instance used to initialise the GUI control if non-null.
     */
    private GuiInit gi;

    /**
     * Set the GuiInit instance.
     *
     * @param gi A GuiInit instance.
     */
    public void setGuiInit(final GuiInit gi) {
        this.gi = gi;
    }

    /**
     * The GuiInit instance (may be null).
     *
     * @return The GuiInit instance (may be null).
     */
    public GuiInit getGuiInit() {
        return gi;
    }

    /**
     * Validates the specified string as a candidate value.
     *
     * @param s The String to validate.
     * @return Null if the string is valid, a description if the problem
     * otherwise.
     */
    public abstract String validateString(String s);

    /**
     * Set the value, return true if the new value is not equal to the old
     * value;
     *
     * @param s The string to set.
     * @return True if the new value was different to the old value, false if
     * they were equal.
     */
    public abstract boolean setStringValue(String s);

    /**
     * Get the current value held by the ParameterValue. Will retrieve the whole
     * object when of type Single or MultiChoiceParameterType
     *
     * @return The current value as an arbitrary Object.
     */
    public abstract Object getObjectValue();

    /**
     * Set the current value held by the ParameterValue.
     *
     * @param o The object to set the value from.
     *
     * @return True if the new value was different to the old value, false if
     * they were equal.
     */
    public abstract boolean setObjectValue(Object o);

    /**
     * Copy this ParameterValue. This will return a deep copy.
     *
     * @return A copy of this instance.
     */
    public ParameterValue copy() {
        final ParameterValue newValue = createCopy();
        newValue.gi = gi;

        return newValue;
    }

    /**
     * Allows each instance of a subclass to create its own type-correct copy.
     * Implementations should ensure that this is a deep copy of any underlying
     * Objects that form the value.
     *
     * @return A type-correct copy of a ParameterValue instance.
     */
    protected abstract ParameterValue createCopy();

    @FunctionalInterface
    public static interface GuiInit {

        /**
         * Initialise a region.
         * <p>
         * The type of region is dependent on the GUI used to represent it.
         *
         * @param region A region to be initialised.
         */
        void init(Region region);
    }
}
