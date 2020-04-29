/*
 * Copyright 2010-2019 Australian Signals Directorate
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

import java.util.Objects;

/**
 * An implementation of {@link ParameterValue} corresponding to this type. It
 * holds String values.
 *
 * @author algol
 */
public class StringParameterValue extends ParameterValue {

    private String s;

    /**
     * Constructs a new StringParameterValue
     */
    public StringParameterValue() {
        this.s = null;
    }

    /**
     * Constructs a new StringParameterValue holding the specified String.
     *
     * @param s The String that this parameter value should hold.
     */
    public StringParameterValue(final String s) {
        this.s = s;
    }

    /**
     * Get the current value from this parameter value.
     *
     * @return The String that this parameter value is holding.
     */
    public String get() {
        return s;
    }

    /**
     * Set the current value
     *
     * @param news The String for this parameter value to hold.
     * @return True if the new value was different to the current value, false
     * otherwise.
     */
    public boolean set(final String news) {
        if (!Objects.equals(s, news)) {
            s = news;
            return true;
        }

        return false;
    }

    @Override
    public String validateString(final String s) {
        return null;
    }

    @Override
    public boolean setStringValue(final String s) {
        return set(s);
    }

    @Override
    public Object getObjectValue() {
        return s;
    }

    @Override
    public boolean setObjectValue(final Object o) {
        if (o == null || o instanceof String) {
            return setStringValue((String) o);
        } else {
            throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
        }
    }

    @Override
    protected StringParameterValue createCopy() {
        return new StringParameterValue(s);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof StringParameterValue && Objects.equals(s, ((StringParameterValue) o).s);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(s);
    }

    @Override
    public String toString() {
        return s;
    }
}
