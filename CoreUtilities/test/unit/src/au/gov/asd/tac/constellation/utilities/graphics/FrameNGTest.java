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
package au.gov.asd.tac.constellation.utilities.graphics;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class FrameNGTest {

    private static final float F1 = 123F;
    private static final float F2 = 321F;
    private static final float F3 = 456F;
    private static final float F4 = 654F;
    private static final float F5 = 789F;
    private static final float F6 = 987F;
    private static final float F7 = 195F;
    private static final float F8 = 951F;
    private static final float F9 = 286F;
    private static final float F10 = 101.1F;
    private static final float F11 = 202.2F;
    private static final float F12 = 303.3F;
    private static final float F13 = 404.4F;
    private static final float F14 = 505.5F;
    private static final float F15 = 606.6F;
    private static final float F16 = 707.7F;
    private static final float F17 = 808.8F;
    private static final float F18 = 909.9F;
    private static final Vector3f V1 = new Vector3f(F1, F2, F3);
    private static final Vector3f V2 = new Vector3f(F4, F5, F6);
    private static final Vector3f V3 = new Vector3f(F7, F8, F9);
    private static final Vector3f V4 = new Vector3f(F10, F11, F12);
    private static final Vector3f V5 = new Vector3f(F13, F14, F15);
    private static final Vector3f V6 = new Vector3f(F16, F17, F18);

    // convenience method to get a new Frame object with values in all vectors
    private Frame getFrame() {
        final Frame f = new Frame();
        f.setOrigin(new Vector3f(V1));
        f.setForwardVector(new Vector3f(V2));
        f.setUpVector(new Vector3f(V3));
        return f;
    }

    // convenience method to construct a new 4x4 matrix filled with junk
    private Matrix44f getMatrix44f() {
        final Matrix44f m = new Matrix44f();
        for (int i = 1; i <= m.a.length; i++) {
            m.a[i - 1] = i * -0.214F;
        }
        return m;
    }

    /**
     * Can get a new Frame.
     */
    @Test
    public void testConstructor() {
        // empty frame
        final Frame f1 = new Frame();
        assertEquals(f1.getOrigin().a, new float[]{0F, 0F, 0F});
        assertEquals(f1.getForwardVector().a, new float[]{0F, 0F, -1F});
        assertEquals(f1.getUpVector().a, new float[]{0F, 1F, 0F});

        // from another frame
        final Frame f2 = getFrame();

        final Frame f3 = new Frame(f2);
        assertEquals(f3.getOrigin().a, f2.getOrigin().a);
        assertEquals(f3.getForwardVector().a, f2.getForwardVector().a);
        assertEquals(f3.getUpVector().a, f2.getUpVector().a);

        // from a camera
        final Frame f4 = new Frame(
                new Vector3f(V1), new Vector3f(V2), new Vector3f(V3));
        assertEquals(f4.getOrigin().a, new float[]{0F, 0F, 0F});
        assertEquals(f4.getForwardVector().a,
                new float[]{-0.6001069F, -0.5289078F, -0.6001069F});
        assertEquals(f4.getUpVector().a, new float[]{195F, 951F, 286F});
    }

    /**
     * Can get the lookAtEye and lookAtCentre of the frame.
     */
    @Test
    public void testGetLookAt() {
        final Frame f = getFrame();
        final Vector3f lookAtEye = new Vector3f(V4);
        final Vector3f lookAtCentre = new Vector3f(V5);
        final Vector3f lookAtUp = new Vector3f(V6);

        f.getLookAt(lookAtEye, lookAtCentre, lookAtUp);
        assertEquals(lookAtEye.a, new float[]{-249.6F, -283.5F, -380.40002F});
        assertEquals(lookAtCentre.a, new float[]{404.4F, 505.5F, 606.6F});
        assertEquals(lookAtUp.a, new float[]{195F, 951F, 286F});

    }

    /**
     * Can get and set values within the Frame.
     */
    @Test
    public void testGetterSetter() {
        final Frame f = new Frame();

        // set Origin by float, normal get
        f.setOrigin(F3, F1, F2);
        assertEquals(f.getOrigin().a, new float[]{F3, F1, F2});

        // set Origin by vector, mutate get
        f.setOrigin(new Vector3f(V1));
        final Vector3f vOrigin = new Vector3f();
        f.getOrigin(vOrigin);
        assertEquals(vOrigin.a, V1.a);

        // set Forward by float, normal get
        f.setForwardVector(F6, F4, F5);
        assertEquals(f.getForwardVector().a, new float[]{F6, F4, F5});

        // set Forward by vector, mutate get
        f.setForwardVector(new Vector3f(V2));
        final Vector3f vForward = new Vector3f();
        f.getForwardVector(vForward);
        assertEquals(vForward.a, V2.a);

        // set Up by float, normal get
        f.setUpVector(F9, F7, F8);
        assertEquals(f.getUpVector().a, new float[]{F9, F7, F8});

        // set Up by vector, mutate get
        f.setUpVector(new Vector3f(V3));
        final Vector3f vUp = new Vector3f();
        f.getUpVector(vUp);
        assertEquals(vUp.a, V3.a);
    }

    /**
     * Can get x, y and z axis values.
     */
    @Test
    public void testGetAxis() {
        final Frame f = getFrame();
        final Vector3f x = new Vector3f();
        final Vector3f y = new Vector3f();
        final Vector3f z = new Vector3f();

        f.getXAxis(x);
        assertEquals(x.a, new float[]{
            712983F, -5421F, -468099F});

        f.getYAxis(y);
        assertEquals(y.a, V3.a);

        f.getZAxis(z);
        assertEquals(z.a, new float[]{F4, F5, F6});
    }

    /**
     * Can translate along orthonormal axis to world coordinates.
     */
    @Test
    public void testTranslateWorld() {
        final Frame f = new Frame();
        f.setOrigin(new Vector3f(V1));
        f.translateWorld(F18, F17, F16);
        assertEquals(f.getOrigin().a, new float[]{1032.9F, 1129.8F, 1163.7F});
    }

    /**
     * Can translate along orthonormal axis to local coordinates.
     */
    @Test
    public void testTranslateLocal() {
        final Frame f = getFrame();
        f.translateLocal(F18, F17, F16);
        assertEquals(f.getOrigin().a, new float[]{6.493639E8F, -3604703, -4.24993024E8F});
    }

    /**
     * Can move forward along the z axis.
     */
    @Test
    public void testMoveForward() {
        final Frame f = new Frame();
        f.setOrigin(new Vector3f(V1));
        f.setForwardVector(new Vector3f(V2));
        f.moveForward(F18);
        assertEquals(f.getOrigin().a, new float[]{595197.6F, 718232.1F, 898527.3F});
    }

    /**
     * Can move up along the y axis.
     */
    @Test
    public void testMoveUp() {
        final Frame f = new Frame();
        f.setOrigin(new Vector3f(V1));
        f.setUpVector(new Vector3f(V2));
        f.moveUp(F18);
        assertEquals(f.getOrigin().a, new float[]{595197.6F, 718232.1F, 898527.3F});
    }

    /**
     * Can move right along the x axis.
     */
    @Test
    public void testMoveRight() {
        final Frame f = getFrame();
        f.moveRight(F18);
        assertEquals(f.getOrigin().a, new float[]{6.4874336E8F, -4932247F, -4.25922848E8F});
    }

    /**
     * Can get the model matrix for the frame.
     */
    @Test
    public void testGetMatrix() {
        final Frame f = getFrame();

        // full matrix
        final Matrix44f m1 = getMatrix44f();
        f.getMatrix(m1, false);
        assertEquals(m1.a, new float[]{
            712983F, -5421F, -468099F, 0F,
            195F, 951F, 286F, 0F,
            654F, 789F, 987F, 0F,
            123F, 321F, 456F, 1F});

        // full matrix through the convenience method
        final Matrix44f m2 = getMatrix44f();
        f.getMatrix(m2);
        assertEquals(m2.a, new float[]{
            712983F, -5421F, -468099F, 0F,
            195F, 951F, 286F, 0F,
            654F, 789F, 987F, 0F,
            123F, 321F, 456F, 1F});

        // rotation matrix only
        final Matrix44f m3 = getMatrix44f();
        f.getMatrix(m3, true);
        assertEquals(m3.a, new float[]{
            712983F, -5421F, -468099F, 0F,
            195F, 951F, 286F, 0F,
            654F, 789F, 987F, 0F,
            0F, 0F, 0F, 1F});
    }

    /**
     * Can assemble the camera matrix.
     */
    @Test
    public void testGetCameraMatrix() {
        final Frame f = getFrame();

        // full matrix
        final Matrix44f m1 = getMatrix44f();
        f.getCameraMatrix(m1, false);
        assertEquals(m1.a, new float[]{
            -712983F, 195F, -654F, 0F,
            5421F, 951F, -789F, 0F,
            468099F, 286F, -987F, 0F,
            -1.27496384E8F, -459672F, 783783F, 1F});

        // full matrix through the convenience method
        final Matrix44f m2 = getMatrix44f();
        f.getCameraMatrix(m2);
        assertEquals(m2.a, new float[]{
            -712983F, 195F, -654F, 0F,
            5421F, 951F, -789F, 0F,
            468099F, 286F, -987F, 0F,
            -1.27496384E8F, -459672F, 783783F, 1F});

        // rotation matrix only
        final Matrix44f m3 = getMatrix44f();
        f.getCameraMatrix(m3, true);
        assertEquals(m3.a, new float[]{
            -712983F, 195F, -654F, 0F,
            5421F, 951F, -789F, 0F,
            468099F, 286F, -987F, 0F,
            0F, 0F, 0F, 1F});
    }

    /**
     * Can rotate around local x.
     */
    @Test
    public void testRotateLocalX() {
        final Frame f = getFrame();
        f.rotateLocalX(F18);
        assertEquals(f.getUpVector().a, new float[]{-399.67316F, 695.71515F, -616.81744F});
        assertEquals(f.getForwardVector().a, new float[]{-131.48895F, 1400.0886F, -216.49115F});
    }

    /**
     * Can rotate around local y.
     */
    @Test
    public void testRotateLocalY() {
        final Frame f = getFrame();
        f.rotateLocalY(F18);
        assertEquals(f.getForwardVector().a, new float[]{-253.29901F, 967.5198F, 1012.00354F});
    }

    /**
     * Can rotate around local z.
     */
    @Test
    public void testRotateLocalZ() {
        final Frame f = getFrame();
        f.rotateLocalZ(F18);
        assertEquals(f.getUpVector().a, new float[]{763.08527F, 647.0276F, 152.57185F});
    }

    /**
     * Can reset axes to make sure they are orthonormal.
     */
    @Test
    public void testNormalise() {
        final Frame f = getFrame();
        f.normalize();
        assertEquals(f.getUpVector().a, new float[]{0.19268042F, 0.9396876F, 0.28259796F});
        assertEquals(f.getForwardVector().a, new float[]{0.51391613F, -0.34197506F, 0.7867295F});
    }

    /**
     * Can rotate in world coordinates.
     */
    @Test
    public void testRotateWorld() {
        final Frame f = getFrame();
        f.rotateWorld(F10, F11, F12, F13);
        assertEquals(f.getUpVector().a, new float[]{-82.81727F, 893.46826F, 468.05743F});
        assertEquals(f.getForwardVector().a, new float[]{614.27563F, 853.1708F, 958.73413F});
    }

    /**
     * Can rotate around a local axis.
     */
    @Test
    public void testRotateLocal() {
        final Frame f = getFrame();
        f.rotateLocal(F10, F11, F12, F13);
        assertEquals(f.getUpVector().a, new float[]{444.20593F, 614.6699F, 670.13727F});
        assertEquals(f.getForwardVector().a, new float[]{782.4481F, 26.721893F, 1188.0518F});
    }

    /**
     * Can convert local coordinates to world coordinates.
     */
    @Test
    public void testLocalToWorld() {
        final Frame f = getFrame();
        final Vector3f local = new Vector3f(V4);

        // full vector
        final Vector3f w1 = new Vector3f(V5);
        f.localToWorld(local, w1, false);
        assertEquals(w1.a, new float[]{7.2320496E7F, -116146.19F, -4.6967168E7F});

        // full vector through the convenience method
        final Vector3f w2 = new Vector3f(V6);
        f.localToWorld(local, w2);
        assertEquals(w2.a, new float[]{7.2320496E7F, -116146.19F, -4.6967168E7F});

        // rotation vector only
        final Vector3f w3 = new Vector3f(V5);
        f.localToWorld(local, w3, true);
        assertEquals(w3.a, new float[]{7.2320376E7F, -116467.19F, -4.6967624E7F});
    }

    /**
     * Can convert world coordinates to local coordinates.
     */
    @Test
    public void testWorldToLocal() {
        final Frame f = getFrame();
        final Vector3f world = new Vector3f(V4);
        final Vector3f local = new Vector3f(V5);

        f.worldToLocal(world, local);
        assertEquals(local.a, new float[]{7.767577E-5F, -0.035130844F, -0.10769265F});
    }

    /**
     * Can transform a point by frame matrix.
     */
    @Test
    public void testTransformPoint() {
        final Frame f = getFrame();
        final Vector3f src = new Vector3f(V4);
        final Vector3f dst = new Vector3f(V5);

        f.transformPoint(src, dst);
        assertEquals(dst.a, new float[]{7.2320496E7F, -116146.19F, -4.6967168E7F});
    }

    /**
     * Can rotate a vector by frame matrix.
     */
    @Test
    public void testRotateVector() {
        final Frame f = getFrame();
        final Vector3f src = new Vector3f(V4);
        final Vector3f dst = new Vector3f(V5);
        f.rotateVector(src, dst);
        assertEquals(dst.a, new float[]{7.2320376E7F, -116467.19F, -4.6967624E7F});
    }

    /**
     * Can create an array of new Frame objects.
     */
    @Test
    public void testCreateArray() {
        final int length = 3;
        final Frame frames[] = Frame.createArray(length);

        assertEquals(frames.length, length);
        for (Frame f : frames) {
            assertEquals(f.getOrigin().a, new float[]{0F, 0F, 0F});
            assertEquals(f.getForwardVector().a, new float[]{0F, 0F, -1F});
            assertEquals(f.getUpVector().a, new float[]{0F, 1F, 0F});
        }
    }
    
    @Test
    public void testAreSame() {
        final Frame f = getFrame();
        final Frame f2 = getFrame();
        assertTrue(f.areSame(f2));
        
        f.setForwardVector(V1);
        assertFalse(f.areSame(f2));
        f.setForwardVector(f2.getForwardVector());
        
        f.setOrigin(V3);
        assertFalse(f.areSame(f2));
        f.setOrigin(f2.getOrigin());
        
        f.setUpVector(V2);
        assertFalse(f.areSame(f2));
        f.setUpVector(f2.getUpVector());
        
        assertTrue(f.areSame(f2));
    }
    
    /**
     * Can get a String representation of a Frame.
     */
    @Test
    public void testToString() {
        assertEquals(getFrame().toString(),
                "o=(3f[123.000000,321.000000,456.000000]) "
                + "f=(3f[654.000000,789.000000,987.000000]) "
                + "u=(3f[195.000000,951.000000,286.000000])");
    }
}
