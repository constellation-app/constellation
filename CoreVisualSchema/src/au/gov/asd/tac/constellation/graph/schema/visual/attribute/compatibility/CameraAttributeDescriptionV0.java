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
package au.gov.asd.tac.constellation.graph.schema.visual.attribute.compatibility;

import au.gov.asd.tac.constellation.graph.attribute.AttributeDescription;
import au.gov.asd.tac.constellation.graph.attribute.ObjectAttributeDescription;
import au.gov.asd.tac.constellation.graph.schema.visual.attribute.CameraAttributeDescription;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import org.openide.util.lookup.ServiceProvider;

/**
 * A placeholder attribute type to hold a Camera instance.
 * <p>
 * This is a legacy class, use {@link CameraAttributeDescription} instead.
 * <p>
 * Note that this attribute description should no longer be used and only
 * remains to support legacy graph files.
 *
 * @author algol
 */
@Deprecated
@ServiceProvider(service = AttributeDescription.class)
public final class CameraAttributeDescriptionV0 extends ObjectAttributeDescription {

    public static final String ATTRIBUTE_NAME = "visual_state";

    // TODO: This is a complete hack, but will work as we never allow the camera
    // to have its default value changed from the camera created by invoking the
    // zero-argument constructor. See the comment in GraphJsonWriter.writeElements
    // for more information about how defaults should be set.
    @Override
    protected Object convertFromObject(Object object) {
        if (object instanceof String) {
            return new Camera();
        }
        return super.convertFromObject(object);
    }

    public CameraAttributeDescriptionV0() {
        super(ATTRIBUTE_NAME);
    }
}
