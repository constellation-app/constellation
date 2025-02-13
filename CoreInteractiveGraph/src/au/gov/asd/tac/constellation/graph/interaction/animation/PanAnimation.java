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

/**
 * Cause the camera to pan (and translate) the from one position to another.
 * This animation is finite so will update the graph to the final camera position when
 * animations are disabled.
 *
 * @author algol
 * @author capricornunicorn123
 */
public final class PanAnimation extends Animation {

    private static final int STEPS = 24;

    private final String name;
    private final Camera from;
    private final Camera to;
    private final boolean isSignificant;

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
    public void initialise(final GraphWriteMethods wg) {
        cameraAttr = VisualConcept.GraphAttribute.CAMERA.ensure(wg);
        camera = wg.getObjectValue(cameraAttr, 0);
    }

    @Override
    public void animate(final GraphWriteMethods wg) {
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
        } else {
            setFinished();
        }
    }

    @Override
    public void reset(final GraphWriteMethods wg) {
        // Method override required, intentionally left blank
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected boolean isSignificant() {
        return isSignificant;
    }
    
    @Override
    public void setFinalFrame(final GraphWriteMethods wg){        
        camera = new Camera(camera);
        camera.lookAtEye.set(to.lookAtEye);
        camera.lookAtCentre.set(to.lookAtCentre);
        camera.lookAtUp.set(to.lookAtUp);
        camera.lookAtRotation.set(to.lookAtRotation);
        wg.setObjectValue(cameraAttr, 0, camera); 
    }    
}
