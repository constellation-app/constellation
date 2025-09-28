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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.ConnectionMode;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeDescription.class)
public final class ConnectionModeAttributeDescription extends AbstractObjectAttributeDescription<ConnectionMode> {

    public static final String ATTRIBUTE_NAME = "connection_mode";
    public static final Class<ConnectionMode> NATIVE_CLASS = ConnectionMode.class;
    public static final ConnectionMode DEFAULT_VALUE = ConnectionMode.EDGE;

    public ConnectionModeAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    protected ConnectionMode convertFromString(final String string) {
        return StringUtils.isBlank(string) ? getDefault() : ConnectionMode.valueOf(string);
    }
}
