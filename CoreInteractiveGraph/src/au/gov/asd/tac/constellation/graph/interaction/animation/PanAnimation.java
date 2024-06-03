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
package au.gov.asd.tac.constellation.graph.interaction.animation;

import au.gov.asd.tac.constellation.graph.GraphWriteMethods;
import au.gov.asd.tac.constellation.graph.schema.visual.concept.VisualConcept;
import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.camera.Graphics3DUtilities;
import au.gov.asd.tac.constellation.utilities.visual.VisualChange;
import au.gov.asd.tac.constellation.utilities.visual.VisualChangeBuilder;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Pan (and translate) the camera from one position to another.
 * <p>
 * (Easing algorithms taken from d3 transitions.)
 *
 * @author algol
 */
public final class PanAnimation extends Animation {

    private static final int STEPS = 12;

    private final String name;
    private final Camera from;
    private final Camera to;
    private final boolean isSignificant;
    private final long panAnimationId = VisualChangeBuilder.generateNewId();

    private Camera camera;
    private int cameraAttr;
    private int step;

    public PanAnimation(final String name, final Camera from, final Camera to, final boolean isSignificant) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.isSignificant = isSignificant;
        step = 0;
    }

    private static float reflect(final float t) {
        return 0.5F * (t < 0.5F ? easeCircle(2 * t) : (2 - easeCircle(2 - 2 * t)));
    }

    private static float easeCircle(final float t) {
        return 1 - (float) Math.sqrt(1 - t * t);
    }

    @Override
    public void initialise(GraphWriteMethods wg) {
        cameraAttr = VisualConcept.GraphAttribute.CAMERA.ensure(wg);
        camera = wg.getObjectValue(cameraAttr, 0);
    }

    @Override
    public List<VisualChange> animate(GraphWriteMethods wg) {
        if (step <= STEPS) {
            final float t = step / (float) STEPS;
            final float mix = reflect(t);

            camera = new Camera(camera);
            camera.lookAtEye.set(Graphics3DUtilities.mix(from.lookAtEye, to.lookAtEye, mix));
            camera.lookAtCentre.set(Graphics3DUtilities.mix(from.lookAtCentre, to.lookAtCentre, mix));
            camera.lookAtUp.set(Graphics3DUtilities.mix(from.lookAtUp, to.lookAtUp, mix));
            camera.lookAtRotation.set(Graphics3DUtilities.mix(from.lookAtRotation, to.lookAtRotation, mix));

            wg.setObjectValue(cameraAttr, 0, camera);
            step++;
            return Arrays.asList(new VisualChangeBuilder(VisualProperty.CAMERA).forItems(1).withId(panAnimationId).build());
        } else {
            setFinished();
            return Collections.emptyList();
        }
    }

    @Override
    public void reset(GraphWriteMethods wg) {
        // Method override required, intentionally left blank
    }

    @Override
    public long getIntervalInMillis() {
        return 15;
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected boolean isSignificant() {
        return isSignificant;
    }
}
