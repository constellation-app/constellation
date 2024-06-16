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
package au.gov.asd.tac.constellation.utilities.graphics;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * Tests for the Frustum class. 
 * 
 * Verifying the projection matrix is easy as a getter exposes the data. 
 * Verifying the corners and plane equations is only possible indirectly - 
 * running a transform will modify these fields, and testing if a point is 
 * inside or outside the transformed frustum can give an indication that 
 * creation and transformation of the frustum was successful. This is only 
 * possible with perspective frustums as the distance calculation used to test
 * if the point is within each plane is not intended for use with orthographic
 * frustums.
 * 
 * Verifying points inside the frustum using testSphere:
 * testSphere checks if a given point is inside the frustum. A frustum has six 
 * sides, each assertion places the point just outside only one of the sides. 
 * For example, a test may position the point too near, if the point was to be
 * moved slightly away the point would be inside the frustum.
 * 
 * @author groombridge34a
 */
public class FrustumNGTest {
    
    /* Convenience method to assert the projection matrix and transformed 
    planes created by the symmetric perspective frustum tests. */
    private void assertSymmetricPerspective(final Frustum frustum) {
        // first assert the projection matrix
        assertEquals(frustum.getProjectionMatrix().a, new float[] {
            0.57735026F, 0F, 0F, 0F,
            0F, 0.57735026F, 0F, 0F,
            0F, 0F, -1.1702127F, -1F,
            0F, 0F, -17.361702F, 0F
        });
        
        // then assert the bounds of the frustum after a transform
        final Frame camera = new Frame();
        camera.setOrigin(0.001f, 0.002f, 0.003f);
        camera.setForwardVector(0.004f, 0.005f, 1.006f);
        camera.setUpVector(0.007f, 1.008f, 0.009f);
        frustum.transform(camera);
        
        // See the class comment for more details on the below assertions.
        
        // Inside the frustum
        assertTrue(frustum.testSphere(new Vector3f(-1F, 1F, -20F), 0F));
        // Outside the near plane
        assertFalse(frustum.testSphere(
                new Vector3f(13.18168F, 13.911181F, -7.8679086F), 0F));
        // Outside the far plane
        assertFalse(frustum.testSphere(
                new Vector3f(-179.97665F, -176.32008F, -104.905175F), 0F));
        // Outside the right plane
        assertFalse(frustum.testSphere(
                new Vector3f(180.80468F, 177.34007F, -100.31184F), 0F));
        // Outside the left plane
        assertFalse(frustum.testSphere
        (new Vector3f(-178.91329F, 179.84499F, -101.725136F), 0F));
        // Outside the top plane
        assertFalse(frustum.testSphere(
                new Vector3f(-177.31329F, 179.845F, -101.625036F), 0F));
        // Outside the bottom plane
        assertFalse(frustum.testSphere(
                new Vector3f(178.23131F, -178.822F, -103.49288F), 0F));
    }
    
    private static final float SYM_PERS_FOV = 120F;
    private static final float SYM_PERS_ASPECT = 1F;
    private static final float SYM_PERS_NEAR = 8F;
    private static final float SYM_PERS_FAR = 102F;
    
    /**
     * Can create a symmetric perspective projection frustum via the 
     * constructor, transform the frustum using a frame and then test that the
     * sides are as expected.
     */
    @Test
    public void testConstructorSymmetricPerspective() {
        assertSymmetricPerspective(new Frustum(SYM_PERS_FOV, SYM_PERS_ASPECT, 
                SYM_PERS_NEAR, SYM_PERS_FAR));
    }
    
    /**
     * Can create a symmetric perspective projection frustum via the setter 
     * method, transform the frustum using a frame and then test that the sides 
     * are as expected.
     */
    @Test
    public void testSetSymmetricPerspective() {
        final Frustum frustum = new Frustum();
        frustum.setPerspective(SYM_PERS_FOV, SYM_PERS_ASPECT, SYM_PERS_NEAR, 
                SYM_PERS_FAR);
        assertSymmetricPerspective(frustum);
    }
    
    /* Convenience method to assert the projection matrix and transformed 
    planes created by the perspective frustum tests. */
    private void assertPerspective(final Frustum frustum) {
        // first assert the projection matrix
        assertEquals(frustum.getProjectionMatrix().a, new float[] {
            1.35F, 0F, 0F, 0F,
            0F, 1.8F, 0F, 0F,
            0.25F, 0.33333334F, -1.57446817F, -1F,
            0F, 0F, -69.510635F, 0F
        });
        
        // then assert the bounds of the frustum after a transform
        final Frame camera = new Frame();
        camera.setOrigin(0.004f, 0.005f, 0.006f);
        camera.setForwardVector(0.007f, 0.008f, 1.009f);
        camera.setUpVector(0.003f, 1.002f, 0.001f);
        frustum.transform(camera);
        
        /* See the class comment for more details on the below assertions.
        Note that most of the tests use a sphere of radius 10, so if you have to
        update the tests make sure only the value you're testing is -10F or 
        further away from the plane! */
        
        // Inside the frustum
        assertTrue(frustum.testSphere(-2F, 3F, -18F, 18F));
        // Outside the near plane
        assertFalse(frustum.testSphere(25.438646F, -9.874849F, -16.272248F, 10F));
        // Outside the far plane
        assertFalse(frustum.testSphere(-208.28409F, 174.75368F, -133.36F, 10F));
        // Outside the right plane
        assertFalse(frustum.testSphere(47.728646F, 20.185148F, -27.042248F, 10F));
        // Outside the left plane
        assertFalse(frustum.testSphere(-39.502388F, -9.7534895F, -27.351847F, 10F));
        // Outside the top plane
        assertFalse(frustum.testSphere(-14.912388F, 39.46507F, -27.321848F, 10F));
        // Outside the bottom plane
        assertFalse(frustum.testSphere(209.98608F, -192.80766F, -120.90631F, 10F));
    }
    
    private static final float PERS_FOV = 110F;
    private static final float PERS_ASPECT = 1.2F;
    private static final float PERS_XMIN = -15F;
    private static final float PERS_XMAX = 25F;
    private static final float PERS_YMIN = -10F;
    private static final float PERS_YMAX = 20F;
    private static final float PERS_NEAR = 27F;
    private static final float PERS_FAR = 121F;
    
    /**
     * Can create a perspective projection frustum via the constructor, 
     * transform the frustum using a frame and then test that the sides are as 
     * expected.
     */
    @Test
    public void testConstructorPerspective() {
        assertPerspective(new Frustum(PERS_FOV, PERS_ASPECT, PERS_XMIN, 
                PERS_XMAX, PERS_YMIN, PERS_YMAX, PERS_NEAR, PERS_FAR));
    }
    
    /**
     * Can create a perspective projection frustum via the setter method, 
     * transform the frustum using a frame and then test that the sides are as 
     * expected.
     */
    @Test
    public void testSetPerspective() {
        final Frustum frustum = new Frustum();
        frustum.setPerspective(PERS_FOV, PERS_ASPECT, PERS_XMIN, PERS_XMAX, 
                PERS_YMIN, PERS_YMAX, PERS_NEAR, PERS_FAR);
        assertPerspective(frustum);
    }
    
    /**
     * Can create an orthographic projection with reasonable defaults.
     */
    @Test
    public void createOrthographicDefault() {
        final Frustum frustum = new Frustum();
        
        // assert the projection matrix
        assertEquals(frustum.getProjectionMatrix().a, new float[] {
            1F, 0F, 0F, 0F,
            0F, 1F, 0F, 0F,
            0F, 0F, -1F, 0F,
            -0F, -0F, -0F, 1F
        });
    }
    
    /* Convenience method to assert the projection matrix created by the 
    orthographic frustum tests. */
    private void assertOrthographic(final Frustum frustum) {
        assertEquals(frustum.getProjectionMatrix().a, new float[] {
            0.06666667F, 0F, 0F, 0F,
            0F, 0.028571429F, 0F, 0F,
            0F, 0F, -0.022222223F, 0F,
            -0.33333334F, -0.14285715F, 0.11111111F, 1F
        });
    }
    
    /**
     * Can create an orthographic projection via the setter.
     */
    @Test
    public void testSetOrthographic() {
        final Frustum frustum = new Frustum();
        frustum.setOrthographic(-10F, 20F, -30F, 40F, -50F, 40F);
        assertOrthographic(frustum);
    }
    
    /**
     * Can create an orthographic projection via the constructor.
     */
    @Test
    public void testConstructorOrthographic() {
        assertOrthographic(new Frustum(-10F, 20F, -30F, 40F, -50F, 40F));
    }
    
    /**
     * Makes a copy of a Frustum.
     */
    @Test 
    public void testCopy() {
        Frustum original = new Frustum(PERS_FOV, PERS_ASPECT, PERS_XMIN, 
                PERS_XMAX, PERS_YMIN, PERS_YMAX, PERS_NEAR, PERS_FAR);
        Frustum copy = original.getCopy();
        
        assertPerspective(original.getCopy());
    }
    
}
