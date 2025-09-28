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

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import org.openide.util.lookup.ServiceProvider;

/**
 * A placeholder attribute type to hold a Camera instance.
 *
 * @author algol
 */
@ServiceProvider(service = AttributeDescription.class)
public final class CameraAttributeDescription extends ObjectAttributeDescription {

    public static final String ATTRIBUTE_NAME = "camera";
    public static final int ATTRIBUTE_VERSION = 1;

    public CameraAttributeDescription() {
        super(ATTRIBUTE_NAME);
    }

    // TODO: This is a complete hack, but will work as we never allow the camera
    // to have its default value changed from the camera created by invoking the
    // zero-argument constructor. See the comment in GraphJsonWriter.writeElements
    // for more information about how defaults should be set.
    @Override
    protected Object convertFromObject(final Object object) {
        return object instanceof String ? new Camera() : super.convertFromObject(object);
    }

    @Override
    public int getVersion() {
        return ATTRIBUTE_VERSION;
    }
}
