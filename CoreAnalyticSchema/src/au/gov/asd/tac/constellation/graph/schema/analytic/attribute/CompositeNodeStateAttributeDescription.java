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
package au.gov.asd.tac.constellation.graph.schema.analytic.attribute;

import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.CompositeNodeState;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are
 * {@link CompositeNodeState} objects.
 * <p>
 * Attribute values can be set either directly using
 * {@link #setObject setObject()} or using {@link #setString setString()} which
 * will utilise {@link CompositeNodeState#createFromString(String)}.
 * <p>
 * Attribute values can be retrieved either directly using
 * {@link #getObject getObject()} or using {@link #getString getString()} which
 * will utilise {@link CompositeNodeState#convertToString()}.
 * <p>
 * Note that 'null' is considered a legitimate attribute value for attributes of
 * this type.
 *
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeDescription.class)
public class CompositeNodeStateAttributeDescription extends AbstractObjectAttributeDescription<CompositeNodeState> {

    public static final String ATTRIBUTE_NAME = "composite_node_state";
    public static final Class<CompositeNodeState> NATIVE_CLASS = CompositeNodeState.class;
    public static final CompositeNodeState DEFAULT_VALUE = null;

    public CompositeNodeStateAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    protected CompositeNodeState convertFromString(final String string) {
        return StringUtils.isBlank(string) ? getDefault() : CompositeNodeState.createFromString(string);
    }

    @Override
    public String getString(final int id) {
        return data[id] == null ? null : ((CompositeNodeState) data[id]).convertToString();
    }
}
