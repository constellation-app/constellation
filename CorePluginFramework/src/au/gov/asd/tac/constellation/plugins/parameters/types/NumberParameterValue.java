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

/**
 * A convenience interface to unify the Integer and Float ParameterValue
 * classes.
 *
 * @author algol
 */
public abstract class NumberParameterValue extends ParameterValue {

    /**
     * The value of this ParameterValue as a Number.
     *
     * @return The value of this ParameterValue as a Number.
     */
    public abstract Number getNumberValue();

    /**
     * Set the value of this ParameterValue.
     *
     * @param n A Number.
     * @return True if the new value was different to the previous value, false
     * otherwise.
     */
    public abstract boolean setNumberValue(Number n);

    /**
     * The minimum value (may be null).
     *
     * @return The minimum value (may be null).
     */
    public abstract Number getMinimumValue();

    /**
     * The maximum value (may be null).
     *
     * @return The maximum value (may be null).
     */
    public abstract Number getMaximumValue();

    /**
     * The step value (may be null).
     *
     * @return The step value (may be null).
     */
    public abstract Number getStepValue();
}
