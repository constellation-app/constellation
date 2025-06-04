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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Matrix44fNGTest {

    private static final float F1 = 123F;
    private static final float F2 = 321F;
    private static final float F3 = 456F;
    private static final float F4 = 654F;
    private static final float F5 = 789F;
    private static final float F6 = 987F;
    private static final float F7 = 191F;
    private static final float F8 = 828F;
    private static final float F9 = 373F;

    private static final Matrix44f M1 = new Matrix44f();
    private static final Matrix44f M2 = new Matrix44f();
    private static final float[] M1_R1 = {101F, 102F, 103F, 104F};
    private static final float[] M1_R2 = {201F, 202F, 203F, 204F};
    private static final float[] M1_R3 = {301F, 302F, 303F, 304F};
    private static final float[] M1_R4 = {401F, 402F, 403F, 404F};
    private static final float[] M2_R1 = {656.79047F, 566.4892F, 138.49348F, 134.44435F};
    private static final float[] M2_R2 = {967.43665F, 267.54318F, 789.1595F, 498.02588F};
    private static final float[] M2_R3 = {460.65683F, 741.18677F, 503.92276F, 77.53038F};
    private static final float[] M2_R4 = {340.8639F, 112.199844F, 861.1616F, 153.07498F};

    @BeforeClass
    public void before() {
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
     * Can create a new Matrix44f.
     */
    @Test
    public void testConstructor() {
        final Matrix44f m = new Matrix44f();
        for (int i = 0; i < m.a.length; i++) {
            assertEquals(m.a[i], 0F);
        }
    }

    /**
     * Can set individual values in a matrix.
     */
    @Test
    public void testSet() {
        final Matrix44f m = new Matrix44f();
        m.set(0, 0, F1);
        m.set(2, 3, F2);
        for (int i = 0; i < m.a.length; i++) {
            switch (i) {
                case 0 -> assertEquals(m.a[i], F1);
                case 11 -> assertEquals(m.a[i], F2);
                default -> assertEquals(m.a[i], 0F);
            }
        }
    }

    /**
     * Can set individual values in a matrix using the transposed method.
     */
    @Test
    public void testSetTransposed() {
        final Matrix44f m = new Matrix44f();
        m.setTransposed(0, 0, F1);
        m.setTransposed(3, 1, F2);
        for (int i = 0; i < m.a.length; i++) {
            switch (i) {
                case 0 -> assertEquals(m.a[i], F1);
                case 7 -> assertEquals(m.a[i], F2);
                default -> assertEquals(m.a[i], 0F);
            }
        }
    }

    /**
     * Can get an individual value from a matrix.
     */
    @Test
    public void testGet() {
        final Matrix44f m = new Matrix44f();
        m.set(0, 0, F1);
        m.set(2, 3, F2);
        assertEquals(m.get(0, 0), F1);
        assertEquals(m.get(2, 3), F2);
        assertEquals(m.get(3, 3), 0F);
    }

    /**
     * Can set a matrix to the identity matrix values.
     */
    @Test
    public void testMakeIdentity() {
        final Matrix44f m = new Matrix44f();
        for (int i = 0; i < m.a.length; i++) {
            m.a[i] = F1;
        }
        m.makeIdentity();
        assertEquals(m.toString(), Matrix44f.identity().toString());
    }

    /**
     * Can get a new matrix equivalent to the identity matrix.
     */
    @Test
    public void testIdentity() {
        final Matrix44f m = Matrix44f.identity();

        // assert it is not the same object, but a copy
        assertFalse(m == Matrix44f.identity());
        // assert the contents are equivalent to the identity matrix
        for (int i = 0; i < m.a.length; i++) {
            if (i == 0 || i == 5 || i == 10 || i == 15) {
                assertEquals(m.a[i], 1F);
            } else {
                assertEquals(m.a[i], 0F);
            }
        }
    }

    /**
     * Can make a matrix into a scaling matrix, via both the float and vector
     * methods.
     */
    @Test
    public void testMakeScalingMatrix() {
        final Matrix44f expected = Matrix44f.identity();

        // float method
        expected.a[0] = F1;
        expected.a[5] = F2;
        expected.a[10] = F3;

        final Matrix44f mFloat = new Matrix44f();
        mFloat.makeScalingMatrix(F1, F2, F3);
        assertEquals(mFloat.toString(), expected.toString());

        // vector method
        expected.a[0] = F3;
        expected.a[5] = F1;
        expected.a[10] = F2;

        final Matrix44f mVector = new Matrix44f();
        mVector.makeScalingMatrix(new Vector3f(F3, F1, F2));
        assertEquals(mVector.toString(), expected.toString());
    }

    /**
     * Can make a matrix into a rotation matrix.
     */
    @Test
    public void testMakeRotationMatrix() {
        final Matrix44f m = new Matrix44f();

        // identity matrix is returned if the magnitude is zero
        m.makeRotationMatrix(0F, 0F, 0F, 0F);
        assertEquals(m.toString(), Matrix44f.identity().toString());

        // successfully made into a rotation matrix
        final Matrix44f expected = new Matrix44f();
        expected.setRow(-0.6246143F, 0.024156004F, 0.78055966F, 0F, 0);
        expected.setRow(0.7240664F, -0.3565212F, 0.5904409F, 0F, 1);
        expected.setRow(0.29254875F, 0.9339749F, 0.20519769F, 0F, 2);
        expected.setRow(0F, 0F, 0F, 1F, 3);

        final Matrix44f m2 = copyMatrix(M2);
        m2.makeRotationMatrix(F1, F2, F3, F4);
        assertEquals(m2.toString(), expected.toString());
    }

    /**
     * Can make a matrix into a translation matrix.
     */
    @Test
    public void testMakeTranslationMatrix() {
        final Matrix44f expected = Matrix44f.identity();
        expected.a[12] = F1;
        expected.a[13] = F2;
        expected.a[14] = F3;

        final Matrix44f m = new Matrix44f();
        m.makeTranslationMatrix(F1, F2, F3);

        assertEquals(m.toString(), expected.toString());
    }

    /**
     * Can make a matrix into a perspective matrix.
     */
    @Test
    public void testMakePerspectiveMatrix() {
        final Matrix44f expected = new Matrix44f();
        expected.setRow(-7.5886905E-4F, 0F, 0F, 0F, 0);
        expected.setRow(0F, -0.24359696F, 0F, 0F, 1);
        expected.setRow(-0F, -0F, -5.6060605F, -1F, 2);
        expected.setRow(0F, 0F, -3012.3635F, 0F, 3);

        final Matrix44f m = copyMatrix(M2);
        m.makePerspectiveMatrix(F1, F2, F3, F4);

        assertEquals(m.toString(), expected.toString());
    }

    /**
     * Can make a matrix into a orthographic projection matrix.
     */
    @Test
    public void testMakeOrthographicMatrix() {
        final Matrix44f expected = new Matrix44f();
        expected.setRow(0.01010101F, 0F, 0F, 0F, 0);
        expected.setRow(0F, 0.01010101F, 0F, 0F, 1);
        expected.setRow(0F, 0F, -0.01010101F, 0F, 2);
        expected.setRow(-2.2424242F, -5.6060605F, -8.969697F, 1F, 3);

        final Matrix44f m = copyMatrix(M2);
        m.makeOrthographicMatrix(F1, F2, F3, F4, F5, F6);

        assertEquals(m.toString(), expected.toString());
    }

    /**
     * Can copy a matrix.
     */
    @Test
    public void testSetCopy() {
        final Matrix44f m = new Matrix44f();
        m.set(M1);
        assertEquals(m.toString(), M1.toString());
    }

    /**
     * Can multiply two matrices.
     */
    @Test
    public void testMultiply() {
        final Matrix44f expected = new Matrix44f();
        expected.setRow(275798.88F, 277295.1F, 278791.3F, 280287.53F, 0);
        expected.setRow(588732.6F, 591254.9F, 593777F, 596299.1F, 1);
        expected.setRow(378275.3F, 380058.62F, 381841.9F, 383625.22F, 2);
        expected.setRow(377572.12F, 379039.47F, 380506.72F, 381974.03F, 3);

        final Matrix44f m = new Matrix44f();
        m.multiply(copyMatrix(M1), copyMatrix(M2));

        assertEquals(m.toString(), expected.toString());
    }

    /**
     * Can multiply a matrix by a four element float array.
     */
    @Test
    public void testMultiplyFloat() {
        final float[] expected
                = new float[]{476454F, 478008F, 479562F, 481116F};

        final Matrix44f m = copyMatrix(M1);
        final float[] f = m.multiply(F1, F2, F3, F4);

        for (int i = 0; i < f.length; i++) {
            assertEquals(f[i], expected[i]);
        }
    }

    /**
     * Can invert a matrix.
     */
    @Test
    public void testInvert() {
        final Matrix44f expected = new Matrix44f();
        expected.setRow(0.0051596523F, -0.0017109562F, -0.0037717712F, 0.0029452317F, 0);
        expected.setRow(-0.002145812F, 7.6098804E-4F, 0.003036878F, -0.0021293473F, 1);
        expected.setRow(-2.5895317E-4F, -4.133917E-4F, 1.181713E-4F, 0.0015125453F, 2);
        expected.setRow(-0.008459768F, 0.0055777747F, 0.005508143F, -0.0069740787F, 3);

        final Matrix44f m = new Matrix44f();
        m.invert(copyMatrix(M2));
        assertEquals(m.toString(), expected.toString());
    }

    /**
     * Unable to invert a matrix because the 4x4 determinant is zero.
     */
    @Test
    public void testUnableInvert() {
        final Matrix44f m = new Matrix44f();
        m.invert(copyMatrix(M1));
        for (float f : m.a) {
            assertEquals(f, Float.NaN);
        }
    }

    /**
     * Can get a 3x3 rotation matrix version of a matrix.
     */
    @Test
    public void testGetRotationMatrix33f() {
        final Matrix33f expected = new Matrix33f();
        expected.setMatrixColumn(new Vector3f(M1_R1[0], M1_R1[1], M1_R1[2]), 0);
        expected.setMatrixColumn(new Vector3f(M1_R2[0], M1_R2[1], M1_R2[2]), 1);
        expected.setMatrixColumn(new Vector3f(M1_R3[0], M1_R3[1], M1_R3[2]), 2);

        final Matrix33f m33 = new Matrix33f();
        m33.setMatrixColumn(new Vector3f(F1, F2, F3), 0);
        m33.setMatrixColumn(new Vector3f(F4, F5, F6), 1);
        m33.setMatrixColumn(new Vector3f(F7, F8, F9), 2);

        final Matrix44f m44 = copyMatrix(M1);
        m44.getRotationMatrix(m33);
        assertEquals(m33.toString(), expected.toString());
    }

    /**
     * Can get a rotation matrix.
     */
    @Test
    public void testGetRotationMatrix() {
        final Matrix44f src = copyMatrix(M1);
        final Matrix44f dst = copyMatrix(M2);

        src.getRotationMatrix(dst);
        for (int i = 0; i < dst.a.length; i++) {
            if (i == 3 || i == 7 || (i > 10 && i < 15)) {
                assertEquals(dst.a[i], 0F);
            } else if (i == 15) {
                assertEquals(dst.a[i], 1F);
            } else {
                assertEquals(dst.a[i], src.a[i]);
            }
        }
    }

    /**
     * Can inject a 3x3 rotation matrix into a 4x4 matrix.
     */
    @Test
    public void testSetRotationMatrix() {
        final Matrix44f expected = copyMatrix(M1);
        expected.setRow(F1, F2, F3, M1_R1[3], 0);
        expected.setRow(F4, F5, F6, M1_R2[3], 1);
        expected.setRow(F7, F8, F9, M1_R3[3], 2);
        expected.setRow(M1_R4[0], M1_R4[1], M1_R4[2], M1_R4[3], 3);

        final Matrix33f m33 = new Matrix33f();
        m33.setMatrixColumn(new Vector3f(F1, F2, F3), 0);
        m33.setMatrixColumn(new Vector3f(F4, F5, F6), 1);
        m33.setMatrixColumn(new Vector3f(F7, F8, F9), 2);

        final Matrix44f m44 = copyMatrix(M1);
        m44.setRotationMatrix(m33);
        assertEquals(m44.toString(), expected.toString());
    }

    /**
     * Can set the first three values in a matrix row from a 3 value vector.
     */
    @Test
    public void testSetRowVector() {
        final Matrix44f m = copyMatrix(M1);
        final Vector3f v = new Vector3f(F1, F2, F3);

        m.setRow(v, 2);
        for (int i = 0; i < m.a.length; i++) {
            if (i > 7 && i < 11) {
                // compare the row set to the vector values
                int vectorIndex = i - 8;
                assertEquals(m.a[i], v.a[vectorIndex]);
            } else {
                assertEquals(m.a[i], M1.a[i]);
            }
        }
    }

    /**
     * Can set a matrix row.
     */
    @Test
    public void testSetRow() {
        final Matrix44f m = copyMatrix(M1);

        m.setRow(M2_R2[0], M2_R2[1], M2_R2[2], M2_R2[3], 1);
        for (int i = 0; i < m.a.length; i++) {
            if (i > 3 && i < 8) {
                // compare the row set to the float values
                int floatArrayIndex = i - 4;
                assertEquals(m.a[i], M2_R2[floatArrayIndex]);
            } else {
                assertEquals(m.a[i], M1.a[i]);
            }
        }
    }

    /**
     * Can create an array of empty matrices.
     */
    @Test
    public void testCreateArray() {
        int num = 3;
        final Matrix44f expected = new Matrix44f();

        final Matrix44f arr[] = Matrix44f.createArray(num);

        // the correct number were created, and they are all distinct objects
        assertEquals(arr.length, num);
        assertFalse(arr[0] == arr[1]);
        assertFalse(arr[0] == arr[2]);
        assertFalse(arr[1] == arr[2]);
        // each matrix is equivalent to the expected matrix
        for (Matrix44f arr1 : arr) {
            assertEquals(arr1.toString(), expected.toString());
        }
    }

    /**
     * Can get a String representation of a matrix.
     */
    @Test
    public void testToString() {
        assertEquals(M1.toString(), "[101.0,102.0,103.0,104.0, "
                + "201.0,202.0,203.0,204.0, 301.0,302.0,303.0,304.0, "
                + "401.0,402.0,403.0,404.0]");
    }
}
