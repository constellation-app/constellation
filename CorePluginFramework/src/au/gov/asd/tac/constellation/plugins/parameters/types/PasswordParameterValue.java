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

import java.util.Objects;

/**
 * An implementation of {@link ParameterValue} corresponding to this type. It
 * holds passwords.
 *
 * @author algol
 * @author arcturus
 */
public class PasswordParameterValue extends ParameterValue {

    private String s;

    /**
     * Constructs a new PasswordParameterValue
     */
    public PasswordParameterValue() {
        this.s = null;
    }

    /**
     * Constructs a new PasswordParameterValue holding the specified String.
     *
     * @param s The String that this parameter value should hold.
     */
    public PasswordParameterValue(final String s) {
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
        return get();
    }

    @Override
    public boolean setObjectValue(final Object o) {
        if (o == null || o instanceof String s) {
            return setStringValue((String) o);
        } else {
            throw new IllegalArgumentException(String.format("Unexpected class %s", o.getClass()));
        }
    }

    @Override
    protected PasswordParameterValue createCopy() {
        return new PasswordParameterValue(s);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }
        return this.getClass() == o.getClass() && Objects.equals(s, ((PasswordParameterValue) o).s);
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
