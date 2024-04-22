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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute;

import au.gov.asd.tac.constellation.graph.attribute.AbstractObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.utilities.icon.ConstellationIcon;
import au.gov.asd.tac.constellation.utilities.icon.DefaultIconProvider;
import au.gov.asd.tac.constellation.utilities.icon.IconManager;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values represent icons in
 * CONSTELLATION. The icons are stored by name as strings.
 * <p>
 * Attribute values of this type can be set and retrieved as strings.
 *
 * @author algol
 */
@ServiceProvider(service = AttributeDescription.class)
public final class IconAttributeDescription extends AbstractObjectAttributeDescription<ConstellationIcon> {

    public static final String ATTRIBUTE_NAME = "icon";
    public static final Class<ConstellationIcon> NATIVE_CLASS = ConstellationIcon.class;
    public static final ConstellationIcon DEFAULT_VALUE = DefaultIconProvider.EMPTY;

    public IconAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    protected ConstellationIcon convertFromString(final String string) {
        return StringUtils.isBlank(string) ? getDefault() : IconManager.getIcon(string);
    }

    @Override
    public String getString(final int id) {
        return ((ConstellationIcon) data[id]).getExtendedName();
    }
}
