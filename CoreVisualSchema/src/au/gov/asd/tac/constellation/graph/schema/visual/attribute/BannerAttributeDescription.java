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

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.objects.Banner;
import org.openide.util.lookup.ServiceProvider;

/**
 * A placeholder attribute type to hold a banner instance.
 *
 * @author algol
 */
@ServiceProvider(service = AttributeDescription.class)
public final class BannerAttributeDescription extends ObjectAttributeDescription {

    public BannerAttributeDescription() {
        super(Banner.ATTRIBUTE_NAME);
    }
}
