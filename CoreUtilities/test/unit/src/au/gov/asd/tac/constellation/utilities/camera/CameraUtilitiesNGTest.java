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
package au.gov.asd.tac.constellation.utilities.camera;

import au.gov.asd.tac.constellation.utilities.graphics.Frame;
import au.gov.asd.tac.constellation.utilities.graphics.Vector3f;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class CameraUtilitiesNGTest {

    private static final Vector3f ZERO_VECTOR = new Vector3f(0F, 0F, 0F);

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
    private static final Vector3f V16 = new Vector3f(16.1F, 16.2F, 16.3F);
    private static final Vector3f V17 = new Vector3f(17.1F, 17.2F, 17.3F);
    private static final Vector3f V18 = new Vector3f(18.1F, 18.2F, 18.3F);

    private static final int MIX_RATIO = 654;

    private static final Camera CAMERA = new Camera();

    /**
     * Sets up a Camera object used as the basic template for testing.
     */
    @BeforeClass
    public void beforeClass() {
        CAMERA.lookAtCentre.set(new Vector3f(V1));
        CAMERA.lookAtEye.set(new Vector3f(V2));
        CAMERA.lookAtUp.set(new Vector3f(V3));
        CAMERA.lookAtRotation.set(new Vector3f(V4));
        CAMERA.lookAtPreviousEye.set(new Vector3f(V5));
        CAMERA.lookAtPreviousCentre.set(new Vector3f(V6));
        CAMERA.lookAtPreviousUp.set(new Vector3f(V7));
        CAMERA.lookAtPreviousRotation.set(new Vector3f(V8));
        CAMERA.boundingBox.set(new Vector3f(V9),
                new Vector3f(V10),
                new Vector3f(V11),
                new Vector3f(V12));
        CAMERA.setVisibilityLow(999F);
        CAMERA.setVisibilityHigh(998F);
        CAMERA.setMixRatio(MIX_RATIO);
        CAMERA.setObjectFrame(
                new Frame(
                        new Vector3f(V13),
                        new Vector3f(V14),
                        new Vector3f(V15)));
    }

    /**
     * Can move the Camera eye to the origin point.
     */
    @Test
    public void testMoveEyeToOrigin() {
        final Camera c = new Camera();
        c.lookAtCentre.set(new Vector3f(112.946351F, 3.484323F, 99.185F));
        c.lookAtEye.set(new Vector3f(70.66574F, 35.118F, 1.84154F));
        CameraUtilities.moveEyeToOrigin(c);
        assertEquals(c.lookAtEye.toString(), ZERO_VECTOR.toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(0.381789F, -0.285649F, 0.879001F).toString());
    }

    // get a region BoundingBox mocked out with a cameraDistance and centre
    private BoundingBox getMockedRegionBoundingBox(final float cameraDistance,
            final Vector3f centre) {
        final BoundingBox region = mock(BoundingBox.class);
        when(region.getCameraDistance(anyFloat(), anyFloat())).thenReturn(cameraDistance);
        when(region.getCentre(anyFloat())).thenReturn(centre);
        return region;
    }

    // get a region BoundingBox with values for cameraDistance and centre
    private BoundingBox getMockedRegionBoundingBox() {
        return getMockedRegionBoundingBox(1234F, new Vector3f(V16));
    }

    /**
     * Can refocus the camera.
     */
    @Test
    public void testRefocus() {
        final Camera c = new Camera(CAMERA);
        final BoundingBox region = getMockedRegionBoundingBox();

        // refocus a Camera
        CameraUtilities.refocus(c, new Vector3f(V17), new Vector3f(V18), region);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(21117.5F, 21241F, 21364.5F).toString());
        assertEquals(c.lookAtCentre.toString(), V16.toString());
        assertEquals(c.lookAtUp.toString(), V18.toString());
        assertEquals(c.lookAtRotation.toString(), V16.toString());
        assertEquals(c.lookAtPreviousEye.toString(), V2.toString());
        assertEquals(c.lookAtPreviousCentre.toString(), V1.toString());
        assertEquals(c.lookAtPreviousUp.toString(), V3.toString());
        assertEquals(c.lookAtPreviousRotation.toString(), V4.toString());
        final Frame of1 = c.getObjectFrame();
        final Frame of2 = new Frame();
        assertEquals(of1.getOrigin().toString(), of2.getOrigin().toString());
        assertEquals(of1.getUpVector().toString(), V15.toString());
        assertEquals(of1.getForwardVector().toString(),
                new Vector3f(-0.57735026F, -0.57735026F, -0.57735026F).toString());

        // refocus a Camera without an object frame
        c.setObjectFrame(null);
        CameraUtilities.refocus(c, new Vector3f(V17), new Vector3f(V18), region);
        final Frame of3 = c.getObjectFrame();
        assertEquals(of3.getOrigin().toString(), of2.getOrigin().toString());
        assertEquals(of3.getUpVector().toString(), of2.getUpVector().toString());
        assertEquals(of3.getForwardVector().toString(),
                new Vector3f(0F, 0F, 1F).toString());

        // refocus where the distance is small
        final BoundingBox region2 = getMockedRegionBoundingBox(1F, new Vector3f(V16));
        CameraUtilities.refocus(c, new Vector3f(V17), new Vector3f(V18), region2);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(289.700012F, 291.400024F, 293.099976F).toString());

        // refocus where the distance is large
        final BoundingBox region3 = getMockedRegionBoundingBox(1000000F, new Vector3f(V16));
        CameraUtilities.refocus(c, new Vector3f(V17), new Vector3f(V18), region3);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(8549999F, 8599999F, 8649998F).toString());
    }

    /**
     * Can refocus a Camera on the x axis.
     */
    @Test
    public void testRefocusXAxis() {
        final BoundingBox region = getMockedRegionBoundingBox();

        // refocus in the forward direction
        final Camera c = new Camera(CAMERA);
        CameraUtilities.refocusOnXAxis(c, region, false);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(1250.099976F, 16.2F, 16.3F).toString());
        assertEquals(c.lookAtUp.toString(),
                new Vector3f(0F, 1F, 0F).toString());

        // refocus in the reverse direction
        final Camera c2 = new Camera(CAMERA);
        CameraUtilities.refocusOnXAxis(c2, region, true);
        assertEquals(c2.lookAtEye.toString(),
                new Vector3f(-1217.900024F, 16.2F, 16.3F).toString());
        assertEquals(c2.lookAtUp.toString(),
                new Vector3f(0F, 1F, 0F).toString());
    }

    /**
     * Can refocus a Camera on the y axis.
     */
    @Test
    public void testRefocusYAxis() {
        final BoundingBox region = getMockedRegionBoundingBox();

        // refocus in the forward direction
        final Camera c = new Camera(CAMERA);
        CameraUtilities.refocusOnYAxis(c, region, false);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(16.1F, 1250.199951F, 16.3F).toString());
        assertEquals(c.lookAtUp.toString(),
                new Vector3f(0F, 0F, -1F).toString());

        // refocus in the reverse direction
        final Camera c2 = new Camera(CAMERA);
        CameraUtilities.refocusOnYAxis(c2, region, true);
        assertEquals(c2.lookAtEye.toString(),
                new Vector3f(16.1F, -1217.800049F, 16.3F).toString());
        assertEquals(c2.lookAtUp.toString(),
                new Vector3f(0F, 0F, 1F).toString());
    }

    /**
     * Can refocus a Camera on the z axis.
     */
    @Test
    public void testRefocusZAxis() {
        final BoundingBox region = getMockedRegionBoundingBox();

        // refocus in the forward direction
        final Camera c = new Camera(CAMERA);
        CameraUtilities.refocusOnZAxis(c, region, false);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(16.1F, 16.2F, 1250.300049F).toString());
        assertEquals(c.lookAtUp.toString(),
                new Vector3f(0F, 1F, 0F).toString());

        // refocus in the reverse direction
        final Camera c2 = new Camera(CAMERA);
        CameraUtilities.refocusOnZAxis(c2, region, true);
        assertEquals(c2.lookAtEye.toString(),
                new Vector3f(16.1F, 16.2F, -1217.699951F).toString());
        assertEquals(c2.lookAtUp.toString(),
                new Vector3f(0F, 1F, 0F).toString());
    }

    /**
     * Can change the mix ratio of a Camera.
     */
    @Test
    public void testChangeMixRatio() {
        // to the upper limit
        final Camera c = new Camera(CAMERA);
        CameraUtilities.changeMixRatio(c, true, true);
        assertEquals(c.getMixRatio(), Camera.MIX_RATIO_MAX);

        // to the lower limit
        CameraUtilities.changeMixRatio(c, false, true);
        assertEquals(c.getMixRatio(), Camera.MIX_RATIO_MIN);

        // increase but not to the limit
        final int midMixRatio = (Camera.MIX_RATIO_MAX + Camera.MIX_RATIO_MIN) / 2;
        c.setMixRatio(midMixRatio);
        CameraUtilities.changeMixRatio(c, true, false);
        assertEquals(c.getMixRatio(), midMixRatio + 1);

        // decrease but not to the limit
        c.setMixRatio(midMixRatio);
        CameraUtilities.changeMixRatio(c, false, false);
        assertEquals(c.getMixRatio(), midMixRatio - 1);

        // increase when the mix ration is over the upper limit does nothing
        final int bigMixRatio = Camera.MIX_RATIO_MAX + 20;
        c.setMixRatio(bigMixRatio);
        CameraUtilities.changeMixRatio(c, true, false);
        assertEquals(c.getMixRatio(), bigMixRatio);

        // decrease when the mix ration is under the lower limit does nothing
        final int smallMixRatio = Camera.MIX_RATIO_MIN - 20;
        c.setMixRatio(smallMixRatio);
        CameraUtilities.changeMixRatio(c, false, false);
        assertEquals(c.getMixRatio(), smallMixRatio);
    }

    /**
     * Can set the rotation centre of a Camera.
     */
    @Test
    public void testSetRotationCentre() {
        final Camera c = new Camera(CAMERA);
        CameraUtilities.setRotationCentre(c, new Vector3f(V10));
        assertEquals(c.lookAtRotation.toString(), V10.toString());
    }

    /**
     * Can pan the view of the Camera.
     */
    @Test
    public void testPan() {
        final Camera c = new Camera(CAMERA);
        CameraUtilities.pan(c, 1234F, -4321F);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(-13464.245117F, -13682.509766F, -14328.245117F).toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(-13465.245117F, -13683.509766F, -14329.245117F).toString());
    }

    /**
     * Can set the Camera to the previous Camera position.
     */
    @Test
    public void testSetCurrentToPrevious() {
        final Camera c = new Camera(CAMERA);
        CameraUtilities.setCurrentToPrevious(c);
        assertEquals(c.lookAtCentre.toString(), V6.toString());
        assertEquals(c.lookAtEye.toString(), V5.toString());
        assertEquals(c.lookAtUp.toString(), V7.toString());
        assertEquals(c.lookAtRotation.toString(), V8.toString());
    }

    /**
     * Can set the previous Camera position to the current Camera position.
     */
    @Test
    public void testSetPreviousToCurrent() {
        final Camera c = new Camera(CAMERA);
        CameraUtilities.setPreviousToCurrent(c);
        assertEquals(c.lookAtPreviousCentre.toString(), V1.toString());
        assertEquals(c.lookAtPreviousEye.toString(), V2.toString());
        assertEquals(c.lookAtPreviousUp.toString(), V3.toString());
        assertEquals(c.lookAtPreviousRotation.toString(), V4.toString());
    }

    /**
     * Can move the Camera eye and centre relative to a rotation point.
     */
    @Test
    public void testRotate() {
        final Camera c = new Camera(CAMERA);
        CameraUtilities.rotate(c, 17F, -618F, 2222F);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(1.562712F, 2.200309F, 3.049615F).toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(0.294065F, 1.200462F, 2.424421F).toString());
        assertEquals(c.lookAtUp.toString(),
                new Vector3f(4.148406F, 3.147604F, 1.903505F).toString());

        // rotate around a zero point, which causes the centre to change ever so
        // slightly for some reason...
        CameraUtilities.rotate(c, 0F, 0F, 0F);
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(0.294069F, 1.200464F, 2.424422F).toString());

        // rotate Camera around its centre of rotation
        CameraUtilities.rotate(c, 17F, -618F);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(2.200911F, 2.000314F, 2.414548F).toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(1.251366F, 0.900471F, 1.471822F).toString());
        assertEquals(c.lookAtUp.toString(),
                new Vector3f(2.980435F, 3.628568F, 2.947968F).toString());
    }

    /**
     * Can spin the Camera in place about the axis between it and the eye.
     */
    @Test
    public void testSpin() {
        final Camera c = new Camera(CAMERA);
        CameraUtilities.spin(c, -618F);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(2.099996F, 2.199996F, 2.299996F).toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(1.099992F, 1.199991F, 1.299991F).toString());
        assertEquals(c.lookAtUp.toString(),
                new Vector3f(3.277264F, 3.087054F, 3.235682F).toString());
    }

    /**
     * Can zoom a Camera in and out.
     */
    @Test
    public void testZoom() {
        final Camera c = new Camera(CAMERA);

        // distance to closest node is at maximum
        CameraUtilities.zoom(c, 100, new Vector3f(V9), Float.MAX_VALUE);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(-207.268860F, -222.833054F, -218.615417F).toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(-208.268860F, -223.833054F, -219.615417F).toString());

        // zooming in
        CameraUtilities.zoom(c, 13, new Vector3f(V14), 33F);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(-297.029968F, -319.330688F, -313.330109F).toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(-298.029968F, -320.330688F, -314.330109F).toString());

        // zooming out
        CameraUtilities.zoom(c, -78, new Vector3f(V17), 12.087F);
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(-99.809128F, -107.293777F, -105.223038F).toString());
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(-100.809128F, -108.293777F, -106.223038F).toString());
    }

    /**
     * Can zoom so that the bounds of the supplied bounding box are visible.
     */
    @Test
    public void testZoomToBoundingBox() {
        final Camera c = new Camera(CAMERA);
        BoundingBox bb = new BoundingBox();

        // empty box doesn't cause the Camera to zoom
        CameraUtilities.zoomToBoundingBox(c, bb);
        assertEquals(c.lookAtCentre.toString(), V1.toString());
        assertEquals(c.lookAtEye.toString(), V2.toString());
        assertEquals(c.lookAtUp.toString(), V3.toString());
        assertEquals(c.lookAtRotation.toString(), V4.toString());
        assertEquals(c.lookAtPreviousCentre.toString(), V6.toString());
        assertEquals(c.lookAtPreviousEye.toString(), V5.toString());
        assertEquals(c.lookAtPreviousUp.toString(), V7.toString());
        assertEquals(c.lookAtPreviousRotation.toString(), V8.toString());

        // zoom to a box
        bb.set(new Vector3f(V2), new Vector3f(V17), new Vector3f(V4), new Vector3f(V15));
        CameraUtilities.zoomToBoundingBox(c, bb);
        assertEquals(c.lookAtCentre.toString(),
                new Vector3f(9.600006F, 9.699982F, 9.800049F).toString());
        assertEquals(c.lookAtEye.toString(),
                new Vector3f(193.235336F, 193.335327F, 193.435394F).toString());
        assertEquals(c.lookAtUp.toString(), V3.toString());
        assertEquals(c.lookAtRotation.toString(),
                new Vector3f(9.600006F, 9.699982F, 9.800049F).toString());
        assertEquals(c.lookAtPreviousCentre.toString(), V1.toString());
        assertEquals(c.lookAtPreviousEye.toString(), V2.toString());
        assertEquals(c.lookAtPreviousUp.toString(), V3.toString());
        assertEquals(c.lookAtPreviousRotation.toString(), V4.toString());
    }

    /**
     * Can find the focus vector for a Camera.
     */
    @Test
    public void testGetFocusVector() {
        assertEquals(CameraUtilities.getFocusVector(new Camera(CAMERA)).toString(),
                new Vector3f(-1F, -1F, -1F).toString());
    }

}
