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
package au.gov.asd.tac.constellation.graph.attribute.interaction;

/**
 * The attribute interaction that is used when no specific interaction is
 * provided for a given attribute type.
 * <p>
 * Not searchable by lookup as a singleton instance should only ever be used by
 * AbstractAttributeInteraction when lookup fails for a type specific
 * interaction.
 *
 * @author twilight_sparkle
 */
public final class DefaultAttributeInteraction extends AbstractAttributeInteraction<Object> {

    public static final DefaultAttributeInteraction DEFAULT_ATTRIBUTE_INTERACTION = new DefaultAttributeInteraction();

    private DefaultAttributeInteraction() {
    }

    @Override
    public String getDataType() {
        return "";
    }

    @Override
    public String getDisplayText(final Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override
    protected Class<Object> getValueType() {
        return Object.class;
    }
}
