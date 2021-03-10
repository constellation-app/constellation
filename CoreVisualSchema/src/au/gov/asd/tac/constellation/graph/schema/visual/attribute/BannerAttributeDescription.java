/*
 * Copyright 2010-2020 Australian Signals Directorate
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
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Banner;
import org.openide.util.lookup.ServiceProvider;

/**
 * A placeholder attribute type to hold a banner instance.
 *
 * @author algol
 */
@ServiceProvider(service = AttributeDescription.class)
public final class BannerAttributeDescription extends AbstractObjectAttributeDescription<Banner> {

    public static final String ATTRIBUTE_NAME = "banner";
    public static final Class<Banner> NATIVE_CLASS = Banner.class;
    public static final Banner DEFAULT_VALUE = null;

    public BannerAttributeDescription() {
        super(ATTRIBUTE_NAME, NATIVE_CLASS, DEFAULT_VALUE);
    }
}
