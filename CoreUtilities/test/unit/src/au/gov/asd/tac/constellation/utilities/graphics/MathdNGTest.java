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

import java.util.Arrays;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;

/**
 * @author groombridge34a
 */
public class MathdNGTest {
    
    private static final double D1 = 1.23D;
    private static final double D2 = 3.21D;
    private static final double D3 = 4.56D;
    private static final double D4 = 6.54D;
    private static final double D5 = 7.89D;
    private static final double D6 = 9.87D;
    private static final double D7 = 1.91D;
    private static final double D8 = 8.28D;
    private static final double D9 = 3.73D;
    private static final double D10 = 10.11D;
    private static final double D11 = 20.22D;
    private static final double D12 = 30.33D;
    private static final double D13 = 40.44D;
    private static final double D14 = 50.55D;
    private static final double D15 = 60.66D;
    private static final double D16 = 70.77D;
    private static final double D17 = 80.88D;
    private static final double D18 = 90.99D;
    
    private static final Matrix44d M1 = new Matrix44d();
    private static final Matrix44d M2 = new Matrix44d();
    private static final double[] M1_R1 = {1.01D, 1.02D, 1.03D, 1.04D};
    private static final double[] M1_R2 = {2.01D, 2.02D, 2.03D, 2.04D};
    private static final double[] M1_R3 = {3.01D, 3.02D, 3.03D, 3.04D};
    private static final double[] M1_R4 = {4.01D, 4.02D, 4.03D, 4.04D};
    private static final double[] M2_R1 = {65.679F, 56.648F, 13.849F, 13.444F};
    private static final double[] M2_R2 = {96.743F, 26.754F, 78.915F, 49.802F};
    private static final double[] M2_R3 = {46.065F, 74.118F, 50.392F, 7.753F};
    private static final double[] M2_R4 = {34.086F, 11.219F, 86.116F, 15.307F};
    
    private static final Matrix33d M3 = new Matrix33d();
    private static final Matrix33d M4 = new Matrix33d();
    private static final double[] M3_R1 = {5.05D, 8.08D, 12.13D};
    private static final double[] M3_R2 = {6.06D, 9.09D, 14.15D};
    private static final double[] M3_R3 = {7.07D, 10.11D, 16.17D};
    private static final double[] M4_R1 = {54.568D, 45.537D, 2.738D, 2.333D};
    private static final double[] M4_R2 = {85.632D, 15.643D, 67.804D, 38.791D};
    private static final double[] M4_R3 = {35.942D, 63.007D, 49.281D, 6.642D};
    
    @BeforeClass
    public void before() {
        M1.setA(new double[] {
            M1_R1[0], M1_R1[1], M1_R1[2], M1_R1[3], 
            M1_R2[0], M1_R2[1], M1_R2[2], M1_R2[3], 
            M1_R3[0], M1_R3[1], M1_R3[2], M1_R3[3], 
            M1_R4[0], M1_R4[1], M1_R4[2], M1_R4[3]});
        M2.setA(new double[] {
            M2_R1[0], M2_R1[1], M2_R1[2], M2_R1[3], 
            M2_R2[0], M2_R2[1], M2_R2[2], M2_R2[3], 
            M2_R3[0], M2_R3[1], M2_R3[2], M2_R3[3], 
            M2_R4[0], M2_R4[1], M2_R4[2], M2_R4[3]});
        M3.setA(new double[] {
            M3_R1[0], M3_R1[1], M3_R1[2], 
            M3_R2[0], M3_R2[1], M3_R2[2], 
            M3_R3[0], M3_R3[1], M3_R3[2]});
        M4.setA(new double[] {
            M4_R1[0], M4_R1[1], M4_R1[2], 
            M4_R2[0], M4_R2[1], M4_R2[2], 
            M4_R3[0], M4_R3[1], M4_R3[2]});
    }
    
    // convenience method to get a copy of a 4x4 matrix, as it's clunky
    private Matrix44d copyMatrix(final Matrix44d m) {
        final Matrix44d ret = new Matrix44d();
        ret.setA(Arrays.copyOf(m.getA(), m.getA().length));
        return ret;
    }
    
    // convenience method to get a copy of a 3x3 matrix, as it's clunky
    private Matrix33d copyMatrix(final Matrix33d m) {
        final Matrix33d ret = new Matrix33d();
        ret.setA(Arrays.copyOf(m.getA(), m.getA().length));
        return ret;
    }
    
    /**
     * Can add two 3d vectors.
     */
    @Test
    public void testAdd() {
        final Vector3d result = new Vector3d();
        Mathd.add(result, new Vector3d(D1, D2, D3), new Vector3d(D4, D5, D6));
        assertEquals(result.a, new double[] {7.77D, 11.1D, 14.43D});
    }
    
    /**
     * Can subtract two 3d vectors.
     */
    @Test
    public void testSubtract() {
        final Vector3d result = new Vector3d();
        Mathd.subtract(result, new Vector3d(D4, D5, D6), new Vector3d(D1, D2, D3));
        assertEquals(result.a, new double[] {5.3100000000000005D, 4.68D, 5.31D});
    }
    
    /**
     * Can get the cross product of two 3d vectors.
     */
    @Test
    public void testCrossProduct() {
        final Vector3d result = new Vector3d();
        Mathd.crossProduct(result, new Vector3d(D1, D2, D3), new Vector3d(D4, D5, D6));
        assertEquals(result.a, new double[] {-4.2956999999999965D, 17.682299999999998D, -11.288700000000002D});
    }
    
    /**
     * Can get the dot product of two 3d vectors.
     */
    @Test
    public void testDotProduct() {
        assertEquals(Mathd.dotProduct(new Vector3d(D1, D2, D3), new Vector3d(D4, D5, D6)), 78.3783D);
    }
    
    /**
     * Can get the angle between two 3d vectors.
     */
    @Test
    public void testGetAngleBetweenVectors() {
        final double d = Mathd.getAngleBetweenVectors(
                new Vector3d(0.12D, 0.34D, 0.56D),
                new Vector3d(0.78D, 0.99D, 1.01D));
        assertEquals(d, 0.0916836222806647D);
    }
    
    /**
     * Can get the square of the distance between two points.
     */
    @Test
    public void testDistanceSquared() {
        assertEquals(Mathd.distanceSquared(new Vector3d(D1, D2, D3), new Vector3d(D4, D5, D6)), 78.2946D);
    }
    
    /**
     * Can get the distance between two points.
     */
    @Test
    public void testDistance() {
        assertEquals(Mathd.getDistance(new Vector3d(D1, D2, D3), new Vector3d(D4, D5, D6)), 8.848423588413928D);
    }
    
    /**
     * Can multiply two 4x4 matrices.
     */
    @Test
    public void testMatrixMultiply44() {
        final Matrix44d m = new Matrix44d();
        Mathd.matrixMultiply(m, copyMatrix(M1), copyMatrix(M2));
        assertEquals(m.getA(), new double[] {
            275.7941993808746D, 277.2903993797302D, 278.78659937858583D, 280.2827993774414D, 
            588.7261308860778D, 591.2482708358764D, 593.770410785675D, 596.2925507354737D, 
            378.27226499080655D, 380.05554491996764D, 381.8388248491287D, 383.6221047782898D, 
            377.5672708034515D, 379.03455076217654D, 380.50183072090147D, 381.9691106796265D});
    }
    
    /**
     * Can multiply two 3x3 matrices.
     */
    @Test
    public void testMatrixMultiply33() {
        final Matrix33d m = new Matrix33d();
        Mathd.matrixMultiply(m, copyMatrix(M3), copyMatrix(M4));
        assertEquals(m.getA(), new double[] {
            570.88028D, 882.5219500000001D, 1350.53185D, 
            1006.61246D, 1519.59987D, 2356.4552900000003D, 
            911.7461900000001D, 1361.3759D, 2124.39928D});
    }
    
    /**
     * Can transform a 3d vector.
     */
    @Test
    public void testTransformVector3() {
        final Vector3d result = new Vector3d();
        Mathd.transformVector(result, new Vector3d(D1, D2, D3), copyMatrix(M1));
        assertEquals(result.a, new double[] {25.43D, 25.529999999999998D, 25.629999999999995D});
    }
    
    /**
     * Can transform a 4d vector.
     */
    @Test
    public void testTransformVector4() {
        final Vector4d result = new Vector4d();
        final Vector4d v = new Vector4d();
        v.set(D1, D2, D3, D4);
        Mathd.transformVector(result, v, copyMatrix(M2));
        assertEquals(result.getA(), new double[] {
            824.3090130615234D, 566.9076994514465D, 1063.337557554245D, 
            311.86199438095093D});
    }
    
    /**
     * Can transform a 3d vector.
     */
    @Test
    public void testRotateVector3() {
        final Vector3d result = new Vector3d();
        Mathd.rotateVector(result, new Vector3d(D1, D2, D3), copyMatrix(M3));
        assertEquals(result.a, new double[] {
            57.903299999999994D, 85.21889999999999D, 134.0766D});
    }
    
    /**
     * Can make a 3x3 scaling matrix.
     */
    @Test
    public void testMakeScalingMatrix33() {
        // using the doubles method
        final Matrix33d m1 = copyMatrix(M3);
        Mathd.makeScalingMatrix(m1, D1, D2, D3);
        assertEquals(m1.getA(), new double[] {
            D1, 0F, 0F,
            0F, D2, 0F,
            0F, 0F, D3});
        
        // using the vector method
        final Matrix33d m2 = copyMatrix(M4);
        Mathd.makeScalingMatrix(m2, new Vector3d(D4, D5, D6));
        assertEquals(m2.getA(), new double[] {
            D4, 0F, 0F,
            0F, D5, 0F,
            0F, 0F, D6});
    }
    
    /**
     * Can make a 4x4 scaling matrix.
     */
    @Test
    public void testMakeScalingMatrix44() {
        // using the doubles method
        final Matrix44d m1 = copyMatrix(M1);
        Mathd.makeScalingMatrix(m1, D7, D8, D9);
        assertEquals(m1.getA(), new double[] {
            D7, 0F, 0F, 0F,
            0F, D8, 0F, 0F,
            0F, 0F, D9, 0F,
            0F, 0F, 0F, 1F});
        
        // using the vector method
        final Matrix44d m2 = copyMatrix(M2);
        Mathd.makeScalingMatrix(m2, new Vector3d(D10, D11, D12));
        assertEquals(m2.getA(), new double[] {
            D10, 0F, 0F, 0F,
            0F, D11, 0F, 0F,
            0F, 0F, D12, 0F,
            0F, 0F, 0F, 1F});
    }
    
    /**
     * Can make a 3x3 rotation matrix.
     */
    @Test
    public void testMakeRotationMatrix33() {
        final Matrix33d m1 = copyMatrix(M3);
        final Matrix33d identity33 = new Matrix33d();
        identity33.identity();

        // identity matrix is returned if the magnitude is zero
        Mathd.makeRotationMatrix(m1, 0D, 0D, 0D, 0D);
        assertEquals(m1.getA(), identity33.getA());

        // successfully made into a rotation matrix
        final Matrix33d m2 = copyMatrix(M4);
        Mathd.makeRotationMatrix(m2, D1, D2, D3, D4);
        assertEquals(m2.getA(), new double[] {
            0.4271055495613798D, 0.8490941209819682D, -0.3108376155611322D, 
            -0.5852453544323359D, 0.5216443276644285D, 0.6207858491709487D, 
            0.6892522939020523D, -0.0832248107801089D, 0.719725577023197D});
    }
    
    /**
     * Can make a 4x4 rotation matrix.
     */
    @Test
    public void testMakeRotationMatrix44() {
        final Matrix44d m1 = copyMatrix(M1);
        final Matrix44d identity44 = new Matrix44d();
        identity44.identity();

        // identity matrix is returned if the magnitude is zero
        Mathd.makeRotationMatrix(m1, 0D, 0D, 0D, 0D);
        assertEquals(m1.getA(), identity44.getA());

        // successfully made into a rotation matrix
        final Matrix44d m2 = copyMatrix(M2);
        Mathd.makeRotationMatrix(m2, D5, D6, D7, D8);
        assertEquals(m2.getA(), new double[] {
            0.5589836199169947D, 0.7504799679705509D, 0.35258634451636583D, 0.0D, 
            -0.520198525374364D, -0.013729040864614989D, 0.8539350137072931D, 0.0D, 
            0.6457017940681519D, -0.6607505816205222D, 0.3827242637007897D, 0.0D, 
            0.0D, 0.0D, 0.0D, 1.0D});
    }
    
    /**
     * Can make a 4x4 translation matrix.
     */
    @Test
    public void testMakeTranslationMatrix() {
        final Matrix44d m = copyMatrix(M1);
        Mathd.makeTranslationMatrix(m, D1, D2, D3);
        assertEquals(m.getA(), new double[] {
            1D, 0D, 0D, 0D,
            0D, 1D, 0D, 0D,
            0D, 0D, 1D, 0D,
            D1, D2, D3, 1D});
    }
    
    /**
     * Can invert a 4x4 matrix.
     */
    @Test
    public void testInvert() {
        final Matrix44d m = new Matrix44d();
        Mathd.invertMatrix(m, copyMatrix(M2));
        assertEquals(m.getA(), new double[] {
            0.051592217523495354D, -0.01710738552898974D, -0.037714071542736975D, 0.02944883916107982D, 
            -0.021455704776316496D, 0.007608652064829005D, 0.030366860010860388D, -0.021291554933531992D, 
            -0.0025899242357718213D, -0.004133664641347953D, 0.0011821506051511632D, 0.015125007187779248D, 
            -0.08459049976527729D, 0.05577418800733334D, 0.055075057287938094D, -0.0697346445301143D});
    }

    /**
     * Unable to invert a 4x4 matrix because the 4x4 determinant is zero.
     */
    @Test
    public void testUnableInvert() {
        final Matrix44d m = new Matrix44d();
        Mathd.invertMatrix(m, new Matrix44d());
        for (double d : m.getA()) {
            assertEquals(d, Double.NaN);
        }
    }
    
    /**
     * Can get the distance a point is from a plane.
     */
    @Test
    public void testGetDistanceToPlane() {
        final Vector4d plane = new Vector4d();
        plane.set(D4, D5, D6, D7);
        assertEquals(Mathd.getDistanceToPlane(new Vector3d(D3, D2, D1), plane), 69.1994D);
    }
    
    /**
     * Can get the plane equation from three points.
     */
    @Test
    public void testGetPlaneEquation() {
        final Vector4d planeEq = new Vector4d();
        Mathd.getPlaneEquation(planeEq, new Vector3d(D3, D2, D1), 
                new Vector3d(D4, D5, D6), new Vector3d(D12, D10, D11));
        assertEquals(planeEq.getA(), new double[] {-0.1356231359237909D, -0.8578200895113308D, 0.4957328504678396D, 2.762292581068417D});
    }
    
    /**
     * Can test the distance of a ray to the center of a sphere.
     */
    @Test
    public void testRaySphereTest() {
        // calculated distance to intersection is positive
        assertEquals(Mathd.raySphereTest(new Vector3d(D1, D2, D3), new Vector3d(D4, D5, D6), new Vector3d(D7, D8, D9), 200D), -197.4008032677102D);

        // calculated distance to intersection is negative
        assertEquals(Mathd.raySphereTest(new Vector3d(-1000D, -1000D, -1000D), new Vector3d(D4, D5, D6), new Vector3d(D7, D8, D9), 200D), -43484.95313337911D);
    }
    
    /**
     * Can perform a three dimensional Catmull-Rom "spline" interpolation
     * between p1 and p2.
     */
    @Test
    public void testCatmullRom() {
        final double[] vOut = new double[3];
        Mathd.catmullRom(vOut, new double[] {D1, D12, D2}, 
                new double[] {D11, D3, D10}, new double[] {D4, D9, D5}, 
                new double[] {D8, D6, D7}, 0.432D);
        assertEquals(vOut, new double[] {15.76962848256D, 2.09403313152D, 9.98578804224D});
    }
    
    /**
     * Can check if two doubles are within an error range.
     */
    @Test
    public void testCompareDoubles() {
        // difference between doubles is under the error tolerance
        assertTrue(Mathd.closeEnough(D2, D1, 1.99D));
        // difference between doubles is over the error tolerance
        assertFalse(Mathd.closeEnough(D5, D3, 3.33D));
    }
    
    /**
     * Can calculate the next "smooth" step for a point to take between two 
     * other points.
     */
    @Test
    public void testSmoothStep() {
        // The decision points hinge on the result of the formula
        //      (x - edge1) / (edge2 - edge1)

        // result is greater than 1
        assertEquals(Mathd.m3dSmoothStep(D1, D2, D17), 1D);

        // result is less than 1
        assertEquals(Mathd.m3dSmoothStep(D2, D1, D3), 0D);

        // formula result is in between 0 and 1
        assertEquals(Mathd.m3dSmoothStep(D1, D18, D9), 0.0022839983390191836D);
    }
    
    /**
     * Can get a planar shadow matrix.
     */
    @Test
    public void testMakePlanarShadowMatrix() {
        final double[] proj = new double[16];
        Mathd.m3dMakePlanarShadowMatrix(proj, 
                new double[] {D10, D3, D9, D4}, new double[] {D8, D5, D7});
        assertEquals(proj, new double[] {
            -43.10269999999999D, 79.7679D, 19.3101D, 0D, 
            37.75679999999999D, -90.8351D, 8.709599999999998D, 0D, 
            30.884399999999996D, 29.4297D, -119.68919999999999D, 0D, 
            54.151199999999996D, 51.6006D, 12.491399999999999D, -126.81349999999999D
        });
    }
    
    /**
     * Can determine the point on a ray closest to another given point in space.
     */
    @Test
    public void testClosestPointOnRay() {
        assertEquals(Mathd.closestPointOnRay(new Vector3d(D9, D1, D13), 
                new Vector3d(D10, D2, D14), new Vector3d(D11, D3, D15), 
                new Vector3d(D12, D4, D16)), 
                1.118990511176196E10D);
    }
}
