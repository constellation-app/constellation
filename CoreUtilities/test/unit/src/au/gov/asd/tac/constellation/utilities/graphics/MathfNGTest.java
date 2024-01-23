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
package au.gov.asd.tac.constellation.utilities.graphics;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

/**
 * @author groombridge34a
 */
public class MathfNGTest {
    
    private static final float F1 = 1.23F;
    private static final float F2 = 3.21F;
    private static final float F3 = 4.56F;
    private static final float F4 = 6.54F;
    private static final float F5 = 7.89F;
    private static final float F6 = 9.87F;
    private static final float F7 = 1.91F;
    private static final float F8 = 8.28F;
    private static final float F9 = 3.73F;
    private static final float F10 = 10.11F;
    private static final float F11 = 20.22F;
    private static final float F12 = 30.33F;
    private static final float F13 = 40.44F;
    private static final float F14 = 50.55F;
    private static final float F15 = 60.66F;
    private static final float F16 = 70.77F;
    private static final float F17 = 80.88F;
    private static final float F18 = 90.99F;
    
    /**
     * Can convert degrees to radians.
     */
    @Test
    public void testDegToRad() {
        assertEquals(Mathf.degToRad(90.99D), 1.5880750863896405D);
    }
    
    /**
     * Can convert radians to degrees.
     */
    @Test
    public void testRadToDeg() {
        assertEquals(Mathf.radToDeg(1.87D), 107.14310768946395D);
    }
    
    /**
     * Can get the smallest power of two greater than or equal to the argument.
     */
    @Test
    public void testIsPowerOfTwo() {
        assertEquals(Mathf.isPowerOfTwo(1), 1);
        assertEquals(Mathf.isPowerOfTwo(2), 2);
        assertEquals(Mathf.isPowerOfTwo(3), 4);
    }
    
    /**
     * Can get the square of the distance between two points.
     */
    @Test
    public void testDistanceSquared() {
        assertEquals(Mathf.distanceSquared(
                new Vector3f(F1, F2, F3), new Vector3f(F4, F5, F6)), 
                78.2946F);
    }
    
    /**
     * Can get the distance between two points.
     */
    @Test
    public void testDistance() {
        assertEquals(Mathf.distance(
                new Vector3f(F1, F2, F3), new Vector3f(F4, F5, F6)), 
                8.848424F);
    }
    
//    /**
//     * Can get the distance of a point to a plane.
//     */
//    @Test
//    public void testDistanceToPlane() {
//        assertEquals(Mathf.distanceToPlane(
//                new Vector3f(F1, F2, F3), new Vector4f(F4, F5, F6, F7)), 
//                5.6429143F);
//    }
    
    /**
     * Can get the plane equation from three points.
     */
    @Test
    public void testPlaneEquation() {
        final Vector4f planeEq = new Vector4f();
        Mathf.planeEquation(planeEq, new Vector3f(F1, F2, F3), 
                new Vector3f(F4, F5, F6), new Vector3f(F7, F8, F9));
        assertEquals(planeEq.a, new float[] {0.7757828F, -0.20191793F, 
            -0.59782124F, 2.4200084F});
    }
    
    
    @Test
    public void testPlaneIntersectionPoint() {
        
        // Initialise a plane that sits verticaly y and horisintaly x at z = 1
        final Vector4f plane = new Vector4f();
        final Vector3f planePoint1 = new Vector3f(0,0,1);
        final Vector3f planePoint2 = new Vector3f(0,1,1);
        final Vector3f planePoint3 = new Vector3f(1,0,1);
        Mathf.planeEquation(plane, planePoint1, planePoint2, planePoint3);
        
        // Initialise a line that does not pass through the plane at
        final Vector3f invalidLineIntitialPoint = new Vector3f(3,2,-2);
        final Vector3f invalidLineFinalPoint = new Vector3f(2,2,-2);
        
        // Test for invalid point
        final Vector3f invalidIntersectionPoint = Mathf.planeIntersectionPoint(invalidLineIntitialPoint, invalidLineFinalPoint, plane);
        assertEquals(invalidIntersectionPoint.getX(), Float.NEGATIVE_INFINITY);
        assertEquals(invalidIntersectionPoint.getY(), Float.NaN);
        assertEquals(invalidIntersectionPoint.getZ(), Float.NaN);
        
        // Initialise a line that does passes thorugh at point (3, 2, 1)
        final Vector3f validLineIntitialPoint = new Vector3f(3,2,-2);
        final Vector3f validLineFinalPoint = new Vector3f(3,2,2);
        
        // Test for valid point
        final Vector3f validIntersectionPoint = Mathf.planeIntersectionPoint(validLineIntitialPoint, validLineFinalPoint, plane);
        assertEquals(validIntersectionPoint.getX(), 3.0F);
        assertEquals(validIntersectionPoint.getY(), 2.0F);
        assertEquals(validIntersectionPoint.getZ(), 1.0F);
    }
    
    /**
     * Can test the distance of a ray to the center of a sphere.
     */
    @Test
    public void testRaySphereTest() {
        // calculated distance to intersection is positive
        assertEquals(Mathf.raySphereTest(new Vector3f(F1, F2, F3), 
                new Vector3f(F4, F5, F6), new Vector3f(F7, F8, F9), 200F), 
                -197.4008F);
        
        // calculated distance to intersection is negative
        assertEquals(Mathf.raySphereTest(new Vector3f(-1000F, -1000F, -1000F), 
                new Vector3f(F4, F5, F6), new Vector3f(F7, F8, F9), 200F), 
                -43485F);
    }
    
    /**
     * Can perform a three dimensional Catmull-Rom "spline" interpolation
     * between p1 and p2.
     */
    @Test
    public void testCatmullRom() {
        final float[] vOut = new float[3];
        Mathf.catmullRom(vOut, new float[] {F1, F12, F2}, 
                new float[] {F11, F3, F10}, new float[] {F4, F9, F5}, 
                new float[] {F8, F6, F7}, 0.432F);
        assertEquals(vOut, new float[] {15.769627F, 2.0940328F, 9.985788F});
    }
    
    /**
     * Can check if two floats are within an error range.
     */
    @Test
    public void testCompareFloats() {
        // difference between floats is under the error tolerance
        assertTrue(Mathf.closeEnough(F2, F1, 1.99F));
        // difference between floats is over the error tolerance
        assertFalse(Mathf.closeEnough(F5, F3, 3.33F));
    }
    
    // convenience method to create a Vector2f
    private Vector2f getVector2f(final float f1, final float f2) {
        final Vector2f v = new Vector2f();
        v.set(f1, f2);
        return v;
    }
    
    /**
     * Can find the tangent basis for a triangle.
     */
    @Test
    public void testCalculateTangentBasis() {
        final Vector3f tangent = new Vector3f();
        Mathf.calculateTangentBasis(tangent, 
                new Vector3f[] {
                    new Vector3f(F18, F1, F17),
                    new Vector3f(F2, F16, F3),
                    new Vector3f(F15, F4, F14)
                }, 
                new Vector2f[] {
                    getVector2f(F5, F13),
                    getVector2f(F6, F12),
                    getVector2f(F7, F11)
                },
                new Vector3f(F8, F10, F9));
        assertEquals(tangent.a, new float[] {-0.5688425F, 0.6512103F, -0.50233793F});
    }
    
    /**
     * Can calculate the next "smooth" step for a point to take between two 
     * other points.
     * (that was a guess as the method isn't commented)
     */
    @Test
    public void testSmoothStep() {
        // The decision points hinge on the result of the formula
        //      (x - edge1) / (edge2 - edge1)
        
        // result is greater than 1
        assertEquals(Mathf.smoothStep(F1, F2, F18), 1F);
        
        // result is less than 1
        assertEquals(Mathf.smoothStep(F2, F1, F3), 0F);
        
        // formula result is in between 0 and 1
        assertEquals(Mathf.smoothStep(F1, F18, F9), 0.0022839985F);
    }
    
    /**
     * Can get a planar shadow matrix.
     */
    @Test
    public void testMakePlanarShadowMatrix() {
        final float[] proj = new float[16];
        Mathf.makePlanarShadowMatrix(proj, 
                new float[] {F10, F3, F9, F4}, new float[] {F8, F5, F7});
        assertEquals(proj, new float[] {
            -43.1027F, 79.7679F, 19.310099F, 0F,
            37.756798F, -90.83509F, 8.7095995F, 0F,
            30.8844F, 29.429699F, -119.68919F, 0F,
            54.1512F, 51.600597F, 12.4914F, -126.81349F
        });
    }
    
    /**
     * Can determine the point on a ray closest to another given point in space.
     */
    @Test
    public void testClosestPointOnRay() {
        assertEquals(Mathf.closestPointOnRay(new Vector3f(F9, F1, F13), 
                new Vector3f(F10, F2, F14), new Vector3f(F11, F3, F15), 
                new Vector3f(F12, F4, F16)), 
                1.11899023E10F);
    }
}
