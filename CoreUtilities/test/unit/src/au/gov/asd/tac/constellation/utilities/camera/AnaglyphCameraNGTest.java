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

import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class AnaglyphCameraNGTest {

    private static final float F1 = -471.78F;
    private static final float F2 = -105.53F;
    private static final float F3 = 280.1F;
    private static final float F4 = 851.96F;
    private static final float F5 = -538.745F;
    private static final float F6 = 912.85F;

    private static final Matrix44f M1 = new Matrix44f();
    private static final float[] M1_R1 = {656.79047F, 566.4892F, 138.49348F, 134.44435F};
    private static final float[] M1_R2 = {967.43665F, 267.54318F, 789.1595F, 498.02588F};
    private static final float[] M1_R3 = {460.65683F, 741.18677F, 503.92276F, 77.53038F};
    private static final float[] M1_R4 = {340.8639F, 112.199844F, 861.1616F, 153.07498F};

    private static final Matrix44f M2 = new Matrix44f();
    private static final float[] M2_R1 = {92.38F, 575.41F, 174.24F, -292.78F};
    private static final float[] M2_R2 = {-56.49F, 383.34F, -26.2F, -147.31F};
    private static final float[] M2_R3 = {779.09F, -528.8F, -764.66F, -97.3F};
    private static final float[] M2_R4 = {403.65F, 542.94F, 311.68F, 319.01F};

    @BeforeClass
    public void setUpMethod() {
        M1.setRow(M1_R1[0], M1_R1[1], M1_R1[2], M1_R1[3], 0);
        M1.setRow(M1_R2[0], M1_R2[1], M1_R2[2], M1_R2[3], 1);
        M1.setRow(M1_R3[0], M1_R3[1], M1_R3[2], M1_R3[3], 2);
        M1.setRow(M1_R4[0], M1_R4[1], M1_R4[2], M1_R4[3], 3);
        M2.setRow(M2_R1[0], M2_R1[1], M2_R1[2], M2_R1[3], 0);
        M2.setRow(M2_R2[0], M2_R2[1], M2_R2[2], M2_R2[3], 1);
        M2.setRow(M2_R3[0], M2_R3[1], M2_R3[2], M2_R3[3], 2);
        M2.setRow(M2_R4[0], M2_R4[1], M2_R4[2], M2_R4[3], 3);
    }

    // convenience method to get a copy of a Matrix, as it's clunky
    private Matrix44f copyMatrix(final Matrix44f m) {
        final Matrix44f ret = new Matrix44f();
        ret.set(m);
        return ret;
    }

    /**
     * Can apply either a left or a right frustum. Testing both left and right
     * methods in the same test case because they are almost identical. Creating
     * unit tests first to give assurance that a future refactor doesn't break
     * anything.
     */
    @Test
    public void testApplyFrustum() {
        final AnaglyphCamera sc = new AnaglyphCamera(F1, F2, F3, F4, F5, F6);

        // apply a left frustum
        final Matrix44f expected1 = new Matrix44f();
        expected1.setRow(M1_R1[0], M1_R1[1], M1_R1[2], M1_R1[3], 0);
        expected1.setRow(M1_R2[0], M1_R2[1], M1_R2[2], M1_R2[3], 1);
        expected1.setRow(M1_R3[0], M1_R3[1], M1_R3[2], M1_R3[3], 2);
        expected1.setRow(34996.41F, 30003.002F, 8168.77F, 7247.0312F, 3);
        assertEquals(sc.applyLeftFrustum(copyMatrix(M1)).toString(), expected1.toString());

        // using an equivalent matrix to apply a left frustum yields the same result
        assertEquals(sc.applyLeftFrustum(copyMatrix(M1)).toString(), expected1.toString());

        // apply a right frustum
        final Matrix44f expected2 = new Matrix44f();
        expected2.setRow(-6437.1655F, M1_R1[1], M1_R1[2], M1_R1[3], 0);
        expected2.setRow(-25310.898F, M1_R2[1], M1_R2[2], M1_R2[3], 1);
        expected2.setRow(-3630.2336F, M1_R3[1], M1_R3[2], M1_R3[3], 2);
        expected2.setRow(-7736.1377F, M1_R4[1], M1_R4[2], M1_R4[3], 3);
        assertEquals(sc.applyRightFrustum(copyMatrix(M1)).toString(), expected2.toString());

        // using an equivalent matrix to apply a right frustum yields the same result
        assertEquals(sc.applyRightFrustum(copyMatrix(M1)).toString(), expected2.toString());
    }

    /**
     * Can get the projection matrix from the frustum.
     */
    @Test
    public void testGetProjectionMatrix() {
        final Matrix44f expected = new Matrix44f();
        expected.setRow(-0.0014013189F, 0F, 0F, 0F, 0);
        expected.setRow(0F, -1.1938678F, 0F, 0F, 1);
        expected.setRow(4.1351226E-4F, 0F, -1.5762731F, -1.0F, 2);
        expected.setRow(0F, 0F, 271.87408F, 0F, 3);

        final AnaglyphCamera sc = new AnaglyphCamera(F6, F5, F4, F3, F2, F1);
        sc.applyRightFrustum(copyMatrix(M1));

        assertEquals(sc.getProjectionMatrix().toString(), expected.toString());
    }

    /**
     * Can get the model-view-projection matrix for the current eye.
     */
    @Test
    public void testGetMvPMatrix() {
        final Matrix44f expected = new Matrix44f();
        expected.setRow(-13.829945F, -6.3021326F, -177848.28F, -174.24F, 0);
        expected.setRow(-6.988373F, -4.198501F, -89450.28F, 26.2F, 1);
        expected.setRow(-4.6575613F, 5.7916403F, -58868.332F, 764.66F, 2);
        expected.setRow(15.206118F, -5.946508F, 193637.56F, -311.68F, 3);

        final AnaglyphCamera sc = new AnaglyphCamera(F3, F6, F2, F5, F1, F4);
        sc.applyRightFrustum(copyMatrix(M1));

        assertEquals(sc.getMvpMatrix(copyMatrix(M2)).toString(), expected.toString());
    }
}
