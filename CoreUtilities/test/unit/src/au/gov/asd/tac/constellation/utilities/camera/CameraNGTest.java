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
package au.gov.asd.tac.constellation.utilities.camera;

import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
import au.gov.asd.tac.constellation.utilities.testing.ConstellationTest; 
 public class CameraNGTest extends ConstellationTest {

    private static final Vector3f ZERO_VECTOR = new Vector3f(0F, 0F, 0F);

    private static final Vector3f MIN_EXTREME = new Vector3f(Float.MAX_VALUE,
            Float.MAX_VALUE, Float.MAX_VALUE);
    private static final Vector3f MAX_EXTREME = new Vector3f(-Float.MAX_VALUE,
            -Float.MAX_VALUE, -Float.MAX_VALUE);

    // helper to assert a Camera object with no other initialise
    private void assertNewCamera(final Camera c) {
        assertEquals(c.lookAtEye.toString(), new Vector3f(0F, 0F, 10F).toString());
        assertEquals(c.lookAtCentre.toString(), ZERO_VECTOR.toString());
        assertEquals(c.lookAtUp.toString(), new Vector3f(0F, 1F, 0F).toString());
        assertEquals(c.lookAtRotation.toString(), ZERO_VECTOR.toString());
        assertEquals(c.lookAtPreviousEye.toString(), new Vector3f(0F, 0F, 10F).toString());
        assertEquals(c.lookAtPreviousCentre.toString(), ZERO_VECTOR.toString());
        assertEquals(c.lookAtPreviousUp.toString(), new Vector3f(0F, 1F, 0F).toString());
        assertEquals(c.lookAtPreviousRotation.toString(), ZERO_VECTOR.toString());
        assertEquals(c.getVisibilityLow(), 0F);
        assertEquals(c.getVisibilityHigh(), 1F);

        final BoundingBox bb = c.boundingBox;
        assertEquals(bb.getMin().toString(), MIN_EXTREME.toString());
        assertEquals(bb.getMax().toString(), MAX_EXTREME.toString());
        assertEquals(bb.getMin2().toString(), MIN_EXTREME.toString());
        assertEquals(bb.getMax2().toString(), MAX_EXTREME.toString());
        assertTrue(bb.isEmpty());

        final Frame objectFrame = c.getObjectFrame();
        assertEquals(objectFrame.getOrigin().toString(), ZERO_VECTOR.toString());
        assertEquals(objectFrame.getUpVector().toString(),
                new Vector3f(0F, 1F, 0F).toString());
        assertEquals(objectFrame.getForwardVector().toString(),
                new Vector3f(0F, 0F, 1F).toString());
    }

    /**
     * Can create a Camera.
     */
    @Test
    public void testConstructor() {
        assertNewCamera(new Camera());
    }

    /**
     * Copy constructor creates a basic Camera if the object passed in is null.
     */
    @Test
    public void testCopyConstructorNull() {
        assertNewCamera(new Camera(null));
    }

    private static final Vector3f V1 = new Vector3f(1.1F, 1.2F, 1.3F);
    private static final Vector3f V2 = new Vector3f(2.1F, 2.2F, 2.3F);
    private static final Vector3f V3 = new Vector3f(3.1F, 3.2F, 3.3F);
    private static final Vector3f V4 = new Vector3f(4.1F, 4.2F, 4.3F);
    private static final Vector3f V5 = new Vector3f(5.1F, 5.2F, 5.3F);
    private static final Vector3f V6 = new Vector3f(6.1F, 6.2F, 6.3F);
    private static final Vector3f V7 = new Vector3f(7.1F, 7.2F, 7.3F);
    private static final Vector3f V8 = new Vector3f(8.1F, 8.2F, 8.3F);
    private static final Vector3f V9 = new Vector3f(9.1F, 9.2F, 9.3F);
    private static final Vector3f V10 = new Vector3f(10.1F, 10.2F, 10.3F);
    private static final Vector3f V11 = new Vector3f(11.1F, 11.2F, 11.3F);
    private static final Vector3f V12 = new Vector3f(12.1F, 12.2F, 12.3F);
    private static final Vector3f V13 = new Vector3f(13.1F, 13.2F, 13.3F);
    private static final Vector3f V14 = new Vector3f(14.1F, 14.2F, 14.3F);
    private static final Vector3f V15 = new Vector3f(15.1F, 15.2F, 15.3F);

    // helper to assert a Camera object with no other initialise
    private void assertCamerasEqual(final Camera c1, final Camera c2) {
        assertEquals(c1.lookAtEye.toString(), c2.lookAtEye.toString());
        assertEquals(c1.lookAtCentre.toString(), c2.lookAtCentre.toString());
        assertEquals(c1.lookAtUp.toString(), c2.lookAtUp.toString());
        assertEquals(c1.lookAtRotation.toString(), c2.lookAtRotation.toString());
        assertEquals(c1.lookAtPreviousEye.toString(), c2.lookAtPreviousEye.toString());
        assertEquals(c1.lookAtPreviousCentre.toString(), c2.lookAtPreviousCentre.toString());
        assertEquals(c1.lookAtPreviousUp.toString(), c2.lookAtPreviousUp.toString());
        assertEquals(c1.lookAtPreviousRotation.toString(), c2.lookAtPreviousRotation.toString());
        assertEquals(c1.getVisibilityLow(), c2.getVisibilityLow());
        assertEquals(c1.getVisibilityHigh(), c2.getVisibilityHigh());

        final BoundingBox bb1 = c1.boundingBox;
        final BoundingBox bb2 = c2.boundingBox;
        assertEquals(bb1.getMin().toString(), bb2.getMin().toString());
        assertEquals(bb1.getMax().toString(), bb2.getMax().toString());
        assertEquals(bb1.getMin2().toString(), bb2.getMin2().toString());
        assertEquals(bb1.getMax2().toString(), bb2.getMax2().toString());
        assertEquals(bb1.isEmpty(), bb2.isEmpty());

        final Frame of1 = c1.getObjectFrame();
        final Frame of2 = c2.getObjectFrame();
        assertEquals(of1.getOrigin().toString(),
                of2.getOrigin().toString());
        assertEquals(of1.getUpVector().toString(),
                of2.getUpVector().toString());
        assertEquals(of1.getForwardVector().toString(),
                of2.getForwardVector().toString());
    }

    /**
     * Can create a Camera from another Camera
     */
    @Test
    public void testCopyConstructor() {
        final Camera c = new Camera();
        c.lookAtCentre.set(V1);
        c.lookAtEye.set(V2);
        c.lookAtUp.set(V3);
        c.lookAtRotation.set(V4);
        c.lookAtPreviousEye.set(V5);
        c.lookAtPreviousCentre.set(V6);
        c.lookAtPreviousUp.set(V7);
        c.lookAtPreviousRotation.set(V8);
        c.boundingBox.set(V9, V10, V11, V12);
        c.setVisibilityLow(999F);
        c.setVisibilityHigh(998F);
        c.setMixRatio(654);
        c.setObjectFrame(new Frame(V13, V14, V15));

        // Assert a Camera with all fields set can be copied
        final Camera c2 = new Camera(c);
        assertCamerasEqual(c, c2);

        // Assert a Camera without a Frame can be copied
        c.setObjectFrame(null);
        final Camera c3 = new Camera(c);
        assertNull(c3.getObjectFrame());
    }

    /**
     * Can calculate a mix from the mix ratio.
     */
    @Test
    public void testGetMix() {
        Camera c = new Camera();
        c.setMixRatio(18);
        assertEquals(c.getMix(), 0.9F);
    }
    
    @Test
    public void testAreSame() {
        final Camera c = new Camera();
        c.lookAtCentre.set(V1);
        c.lookAtEye.set(V2);
        c.lookAtUp.set(V3);
        c.lookAtRotation.set(V4);
        c.lookAtPreviousEye.set(V5);
        c.lookAtPreviousCentre.set(V6);
        c.lookAtPreviousUp.set(V7);
        c.lookAtPreviousRotation.set(V8);
        c.boundingBox.set(V9, V10, V11, V12);
        c.setVisibilityLow(999F);
        c.setVisibilityHigh(998F);
        c.setMixRatio(654);
        c.setObjectFrame(new Frame(V13, V14, V15));
        
        final Camera c2 = new Camera();
        c2.lookAtCentre.set(V1);
        c2.lookAtEye.set(V2);
        c2.lookAtUp.set(V3);
        c2.lookAtRotation.set(V4);
        c2.lookAtPreviousEye.set(V5);
        c2.lookAtPreviousCentre.set(V6);
        c2.lookAtPreviousUp.set(V7);
        c2.lookAtPreviousRotation.set(V8);
        c2.boundingBox.set(V9, V10, V11, V12);
        c2.setVisibilityLow(999F);
        c2.setVisibilityHigh(998F);
        c2.setMixRatio(654);
        c2.setObjectFrame(new Frame(V13, V14, V15));
        
        assertTrue(c.areSame(c2));
        
        c2.lookAtCentre.set(V2);
        assertFalse(c.areSame(c2));
        c2.lookAtCentre.set(V1);
        
        c2.lookAtEye.set(V3);
        assertFalse(c.areSame(c2));
        c2.lookAtEye.set(V2);
        
        c2.lookAtUp.set(V4);
        assertFalse(c.areSame(c2));
        c2.lookAtUp.set(V3);
        
        c2.lookAtRotation.set(V5);
        assertFalse(c.areSame(c2));
        c2.lookAtRotation.set(V4);
        
        c2.lookAtPreviousEye.set(V6);
        assertFalse(c.areSame(c2));
        c2.lookAtPreviousEye.set(V5);
        
        c2.lookAtPreviousCentre.set(V7);
        assertFalse(c.areSame(c2));
        c2.lookAtPreviousCentre.set(V6);
        
        c2.lookAtPreviousUp.set(V8);
        assertFalse(c.areSame(c2));
        c2.lookAtPreviousUp.set(V7);
        
        c2.lookAtPreviousRotation.set(V9);
        assertFalse(c.areSame(c2));
        c2.lookAtPreviousRotation.set(V8);
        
        c2.boundingBox.set(V8, V10, V11, V12);
        assertFalse(c.areSame(c2));
        c2.boundingBox.set(V9, V10, V11, V12);
        
        c.setVisibilityLow(444F);
        assertFalse(c.areSame(c2));
        c.setVisibilityLow(999F);
        
        c.setVisibilityHigh(777F);
        assertFalse(c.areSame(c2));
        c.setVisibilityHigh(998F);
        
        c.setMixRatio(235);
        assertFalse(c.areSame(c2));
        c.setMixRatio(654);
        
        c.setObjectFrame(new Frame(V13, V8, V15));
        assertFalse(c.areSame(c2));
        c.setObjectFrame(new Frame(V13, V14, V15));
        
        assertTrue(c.areSame(c2));
    }

    /**
     * Can generate a String representation of a Camera.
     */
    @Test
    public void testToString() {
        Camera c = new Camera();
        c.lookAtEye.set(V1);
        c.lookAtCentre.set(V2);
        c.lookAtUp.set(V3);
        assertEquals(c.toString(), "Camera[eye: 3f[1.100000,1.200000,1.300000]; "
                + "centre: 3f[2.100000,2.200000,2.300000]; "
                + "up: 3f[3.100000,3.200000,3.300000]]");
    }

}
