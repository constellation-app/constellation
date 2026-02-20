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
import org.testng.annotations.Test;

/**
 * @author groombridge34a
 */
public class Matrix33fNGTest {

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
    private static final Vector3f V1 = new Vector3f(F1, F2, F3);
    private static final Vector3f V2 = new Vector3f(F4, F5, F6);
    private static final Vector3f V3 = new Vector3f(F7, F8, F9);

    // convenience method to get a new matrix
    private Matrix33f getMatrix() {
        final Matrix33f m = new Matrix33f();
        m.setMatrixColumn(new Vector3f(V1), 0);
        m.setMatrixColumn(new Vector3f(V2), 1);
        m.setMatrixColumn(new Vector3f(V3), 2);
        return m;
    }

    // convenience method to return an identity matrix
    private Matrix33f getIdentityMatrix() {
        final Matrix33f m = new Matrix33f();
        m.a[0] = 1F;
        m.a[4] = 1F;
        m.a[8] = 1F;
        return m;
    }

    /**
     * Can create a new Matrix33f.
     */
    @Test
    public void testConstructor() {
        final Matrix33f m = new Matrix33f();
        for (int i = 0; i < m.a.length; i++) {
            assertEquals(m.a[i], 0F);
        }
    }

    /**
     * Can set a matrix to the identity matrix values.
     */
    @Test
    public void testIdentity() {
        final Matrix33f m = getMatrix();
        m.makeIdentity();
        assertEquals(m.toString(), getIdentityMatrix().toString());
    }

    /**
     * Can make a matrix into a rotation matrix.
     */
    @Test
    public void testMakeRotationMatrix() {
        final Matrix33f m = new Matrix33f();

        // identity matrix is returned if the magnitude is zero
        m.makeRotationMatrix(0F, 0F, 0F, 0F);
        assertEquals(m.toString(), getIdentityMatrix().toString());

        // successfully made into a rotation matrix
        final Matrix33f expected = new Matrix33f();
        expected.setMatrixColumn(new Vector3f(0.864157F, 0.4328264F, -0.2566983F), 0);
        expected.setMatrixColumn(new Vector3f(-0.36762178F, 0.8913256F, 0.2653167F), 1);
        expected.setMatrixColumn(new Vector3f(0.34363782F, -0.1349074F, 0.92936164F), 2);

        final Matrix33f m2 = getMatrix();
        m2.makeRotationMatrix(F10, F11, F12, F13);
        assertEquals(m2.toString(), expected.toString());
    }

    /**
     * Can make a matrix into a scaling matrix, via both the float and vector
     * methods.
     */
    @Test
    public void testMakeScalingMatrix() {
        final Matrix33f expected = getIdentityMatrix();

        // float method
        expected.a[0] = F1;
        expected.a[4] = F2;
        expected.a[8] = F3;

        final Matrix33f mFloat = new Matrix33f();
        mFloat.makeScalingMatrix(F1, F2, F3);
        assertEquals(mFloat.toString(), expected.toString());

        // vector method
        expected.a[0] = F3;
        expected.a[4] = F1;
        expected.a[8] = F2;

        final Matrix33f mVector = new Matrix33f();
        mVector.makeScalingMatrix(new Vector3f(F3, F1, F2));
        assertEquals(mVector.toString(), expected.toString());
    }

    /**
     * Can multiply two matrices.
     */
    @Test
    public void testMultiply() {
        final Matrix33f expected = new Matrix33f();
        expected.setMatrixColumn(new Vector3f(251186.42F, 581126.44F, 402664.84F), 0);
        expected.setMatrixColumn(new Vector3f(631130.4F, 1416873.6F, 1087399.2F), 1);
        expected.setMatrixColumn(new Vector3f(561367.2F, 867936.0F, 887482.4F), 2);

        final Matrix33f m1 = getMatrix();
        final Matrix33f m2 = getMatrix();
        for (int i = 0; i < m2.a.length; i++) {
            m2.a[i] = m2.a[i] * 0.8F; // fill m2 with slightly different values
        }

        final Matrix33f m = new Matrix33f();
        m.multiply(m1, m2);
        assertEquals(m.toString(), expected.toString());
    }

    /**
     * Can get a matrix column.
     */
    @Test
    public void testGetMatrixColumn() {
        final Matrix33f m = getMatrix();
        final Vector3f expected = V2;
        final Vector3f v = new Vector3f();
        m.getMatrixColumn(v, 1);
        assertEquals(v.toString(), expected.toString());
    }

    /**
     * Can invert a matrix.
     */
    @Test
    public void testInverse() {
        final Matrix33f expected = new Matrix33f();
        expected.setMatrixColumn(new Vector3f(123F, 654F, 195F), 0);
        expected.setMatrixColumn(new Vector3f(321F, 789F, 951F), 1);
        expected.setMatrixColumn(new Vector3f(456F, 987F, 286F), 2);

        final Matrix33f m = getMatrix();
        m.inverse();
        assertEquals(m.toString(), expected.toString());
    }

    /**
     * Can set the values in a matrix column from a 3 value vector.
     */
    @Test
    public void testSetMatrixColumn() {
        final Matrix33f mBase = getMatrix();
        final Matrix33f m = getMatrix();
        final Vector3f v = new Vector3f(F11, F12, F13);

        m.setMatrixColumn(v, 1);
        for (int i = 0; i < m.a.length; i++) {
            if (i > 2 && i < 6) {
                // compare the row set to the vector values
                int vectorIndex = i - 3;
                assertEquals(m.a[i], v.a[vectorIndex]);
            } else {
                assertEquals(m.a[i], mBase.a[i]);
            }
        }
    }

    /**
     * Can get a String representation of a matrix.
     */
    @Test
    public void testToString() {
        assertEquals(getMatrix().toString(), "[123.0,321.0,456.0, "
                + "654.0,789.0,987.0, 195.0,951.0,286.0]");
    }
}
