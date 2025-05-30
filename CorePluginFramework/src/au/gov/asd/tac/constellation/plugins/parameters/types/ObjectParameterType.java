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
import au.gov.asd.tac.constellation.plugins.parameters.types.ObjectParameterType.ObjectParameterValue;
import java.util.Objects;
import org.openide.util.lookup.ServiceProvider;

/**
 * The ObjectParameterType defines {@link PluginParameter} objects that hold an
 * arbitrary Object value.
 *
 * @author sirius
 */
@ServiceProvider(service = PluginParameterType.class)
public class ObjectParameterType extends PluginParameterType<ObjectParameterValue> {

    /**
     * A String ID with which to distinguish parameters that have this type.
     */
    public static final String ID = "object";

    /**
     * The singleton instance of the type that should be used to construct all
     * parameters that have this type.
     */
    public static final ObjectParameterType INSTANCE = new ObjectParameterType();

    /**
     * Construct a new {@link PluginParameter} of this type.
     *
     * @param id The String id of the parameter to construct.
     * @return A {@link PluginParameter} of BooleanParameterType.
     */
    public static PluginParameter<ObjectParameterValue> build(final String id) {
        return new PluginParameter<>(new ObjectParameterValue(), INSTANCE, id);
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
    public ObjectParameterType() {
        super(ID);
    }

    /**
     * An implementation of {@link ParameterValue} corresponding to this type.
     * It holds Object values.
     */
    public static class ObjectParameterValue extends ParameterValue {

        private Object o;

        /**
         * Constructs a new ObjectParameterValue
         */
        public ObjectParameterValue() {
            o = null;
        }

        /**
         * Constructs a new ObjectParameterValue holding the specified Object.
         *
         * @param o The Object that this parameter value should hold.
         */
        public ObjectParameterValue(final Object o) {
            this.o = o;
        }

        @Override
        public String validateString(final String s) {
            return null;
        }

        @Override
        public boolean setStringValue(String s) {
            if (!Objects.equals(o, s)) {
                o = s;
                return true;
            }

            return false;
        }

        @Override
        public Object getObjectValue() {
            return o;
        }

        @Override
        public boolean setObjectValue(final Object o) {
            if (!Objects.equals(this.o, o)) {
                this.o = o;
                return true;
            }

            return false;
        }

        @Override
        protected ObjectParameterValue createCopy() {
            return new ObjectParameterValue(o);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(o);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final ObjectParameterValue other = (ObjectParameterValue) obj;
            return Objects.equals(this.o, other.o);
        }

        @Override
        public String toString() {
            return String.valueOf(o);
        }
    }
}
