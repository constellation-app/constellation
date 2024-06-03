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
import au.gov.asd.tac.constellation.graph.schema.analytic.attribute.objects.RawData;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 * This describes a type of attribute whose values are {@link RawData} objects.
 * <p>
 * Attribute values can be set either directly using
 * {@link #setObject setObject()} or using {@link #setString setString()} which
 * will utilise the {@link RawData#RawData(String) RawData(String)} constructor.
 * <p>
 * Attribute values can be retrieved either directly using
 * {@link #getObject getObject()} or using {@link #getString getString()} which
 * will utilise {@link RawData#toString RawData.toString()}.
 *
 * @author cygnus_x-1
 */
@ServiceProvider(service = AttributeDescription.class)
public class RawAttributeDescription extends AbstractObjectAttributeDescription<RawData> {

    public static final String ATTRIBUTE_NAME = "raw";
    public static final Class<RawData> NATIVE_CLASS = RawData.class;
    public static final RawData DEFAULT_VALUE = new RawData(null, null);

    public RawAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }

    @Override
    protected RawData convertFromString(final String originalString) {
        if (StringUtils.isBlank(originalString)) {
            return getDefault();
        } else {
            return new RawData(originalString);
        }
    }
}
