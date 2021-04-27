/*
 * Copyright 2010-2021 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author twilight_sparkle
 */
@ServiceProvider(service = AttributeDescription.class)
public final class DrawFlagsAttributeDescription extends AbstractObjectAttributeDescription<DrawFlags> {

    public static final String ATTRIBUTE_NAME = "draw_flags";
    public static final Class<DrawFlags> NATIVE_CLASS = DrawFlags.class;
    public static final DrawFlags DEFAULT_VALUE = DrawFlags.ALL;

    public DrawFlagsAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    public String getName() {
        return ATTRIBUTE_NAME;
    }

    @Override
    protected DrawFlags convertFromString(final String string) {
        if (StringUtils.isBlank(string)) {
            return getDefault();
        } else {
            return new DrawFlags(Integer.parseInt(string));
        }
    }

    @Override
    public String getString(final int id) {
        return String.valueOf(((DrawFlags) data[id]).getFlags());
    }

    @Override
    public int getInt(final int id) {
        return ((DrawFlags) data[id]).getFlags();
    }

    @Override
    public void setInt(final int id, final int value) {
        data[id] = new DrawFlags(value);
    }
}
